package de.mkrabs.vantage;

import static net.minecraft.client.CameraType.FIRST_PERSON;
import static net.minecraft.client.CameraType.THIRD_PERSON_BACK;
import static net.minecraft.client.CameraType.THIRD_PERSON_FRONT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.CameraType;
import org.junit.jupiter.api.Test;

// Angles: +90 straight up, -90 straight down.
class ViewSwitcherTest {
	final VantageConfig config = new VantageConfig();
	final ViewSwitcher switcher = new ViewSwitcher(config);

	void ranges(VantageConfig.Range... ranges) {
		config.ranges = new ArrayList<>(List.of(ranges));
	}

	static VantageConfig.Range range(CameraType view, float from, float to, int layer) {
		return new VantageConfig.Range(view, from, to, layer);
	}

	@Test
	void mountingPicksTheViewOfTheRangeCoveringTheAngle() {
		ranges(range(THIRD_PERSON_FRONT, 90, 20, 1), range(THIRD_PERSON_BACK, 10, -90, 1));
		assertEquals(THIRD_PERSON_BACK, switcher.tick(true, 0, FIRST_PERSON));
	}

	@Test
	void onOneLayerTheHeldRangeStaysUntilYouLeaveIt() {
		ranges(range(THIRD_PERSON_BACK, 60, -90, 1), range(THIRD_PERSON_FRONT, 90, 20, 1));
		switcher.tick(true, 0, FIRST_PERSON);
		assertNull(switcher.tick(true, 60, THIRD_PERSON_BACK)); // overlap: third person holds
		assertEquals(THIRD_PERSON_FRONT, switcher.tick(true, 61, THIRD_PERSON_BACK));
		assertNull(switcher.tick(true, 20, THIRD_PERSON_FRONT)); // overlap: selfie holds
		assertEquals(THIRD_PERSON_BACK, switcher.tick(true, 19, THIRD_PERSON_FRONT));
	}

	@Test
	void aHigherLayerWinsWithoutBufferWhileTheLowerLayerKeepsTracking() {
		ranges(range(THIRD_PERSON_BACK, 60, -90, 1), range(THIRD_PERSON_FRONT, 90, 20, 1),
				range(FIRST_PERSON, 20, -20, 2));
		assertEquals(FIRST_PERSON, switcher.tick(true, 0, THIRD_PERSON_BACK));
		assertEquals(THIRD_PERSON_BACK, switcher.tick(true, 21, FIRST_PERSON)); // layer 1 held third person underneath
		assertEquals(THIRD_PERSON_FRONT, switcher.tick(true, 70, THIRD_PERSON_BACK));
		assertEquals(FIRST_PERSON, switcher.tick(true, 10, THIRD_PERSON_FRONT)); // layer 2 cuts in at once
		assertEquals(THIRD_PERSON_BACK, switcher.tick(true, 30, FIRST_PERSON)); // selfie was released underneath at 10
	}

	@Test
	void dismountingRestoresTheViewFromBeforeMounting() {
		ranges(range(THIRD_PERSON_BACK, 90, -90, 1));
		switcher.tick(true, 0, THIRD_PERSON_FRONT);
		assertEquals(THIRD_PERSON_FRONT, switcher.tick(false, 0, THIRD_PERSON_BACK));
	}

	@Test
	void mountingInsideAnOverlapPicksTheRangeHigherInTheList() {
		ranges(range(THIRD_PERSON_BACK, 60, -90, 1), range(THIRD_PERSON_FRONT, 90, 20, 1));
		assertEquals(THIRD_PERSON_BACK, switcher.tick(true, 40, FIRST_PERSON));
	}

	@Test
	void anAngleNoRangeCoversLeavesTheViewAlone() {
		ranges(range(THIRD_PERSON_FRONT, 90, 30, 1));
		assertNull(switcher.tick(true, 0, FIRST_PERSON));
		assertEquals(THIRD_PERSON_FRONT, switcher.tick(true, 40, FIRST_PERSON));
		assertNull(switcher.tick(true, 0, THIRD_PERSON_FRONT));
	}

	@Test
	void manualF5StaysUntilTheActiveRangeChanges() {
		ranges(range(THIRD_PERSON_BACK, 60, -90, 1), range(THIRD_PERSON_FRONT, 90, 20, 1));
		switcher.tick(true, 0, FIRST_PERSON);
		assertNull(switcher.tick(true, 30, FIRST_PERSON)); // player pressed F5
		assertEquals(THIRD_PERSON_FRONT, switcher.tick(true, 70, FIRST_PERSON));
	}

	@Test
	void switchingMountsKeepsTheViewFromBeforeTheFirstMount() {
		ranges(range(THIRD_PERSON_BACK, 90, -90, 1));
		switcher.tick(true, 0, FIRST_PERSON);
		switcher.tick(true, 0, THIRD_PERSON_BACK); // new vehicle, still riding
		assertEquals(FIRST_PERSON, switcher.tick(false, 0, THIRD_PERSON_BACK));
	}

	@Test
	void disablingWhileRidingRestoresAtOnceAndThenLeavesTheViewAlone() {
		ranges(range(THIRD_PERSON_BACK, 90, -90, 1));
		switcher.tick(true, 0, THIRD_PERSON_FRONT);
		assertEquals(THIRD_PERSON_FRONT, switcher.tick(false, 0, THIRD_PERSON_BACK));
		assertNull(switcher.tick(false, 80, THIRD_PERSON_FRONT));
	}

	@Test
	void withNoRangesOnlyTheDismountRestoreHappens() {
		ranges();
		assertNull(switcher.tick(true, 0, FIRST_PERSON));
		assertEquals(FIRST_PERSON, switcher.tick(false, 0, THIRD_PERSON_BACK));
	}

	@Test
	void deletingOrMovingTheHeldRangeInTheSettingsTakesEffectWhileRiding() {
		VantageConfig.Range selfie = range(THIRD_PERSON_FRONT, 90, -90, 1);
		ranges(selfie, range(THIRD_PERSON_BACK, 90, -90, 1));
		switcher.tick(true, 0, FIRST_PERSON);
		config.ranges.remove(selfie);
		assertEquals(THIRD_PERSON_BACK, switcher.tick(true, 0, THIRD_PERSON_FRONT));
		config.ranges.add(selfie);
		selfie.layer = 2;
		assertEquals(THIRD_PERSON_FRONT, switcher.tick(true, 0, THIRD_PERSON_BACK));
	}

	@Test
	void defaultsGiveFirstPersonAheadSelfieAboveSixtyAndThirdPersonBelowTwenty() {
		assertEquals(FIRST_PERSON, switcher.tick(true, 0, THIRD_PERSON_BACK));
		assertEquals(THIRD_PERSON_BACK, switcher.tick(true, 30, FIRST_PERSON));
		assertEquals(THIRD_PERSON_FRONT, switcher.tick(true, 61, THIRD_PERSON_BACK));
		assertEquals(THIRD_PERSON_BACK, switcher.tick(true, -30, THIRD_PERSON_FRONT));
	}
}
