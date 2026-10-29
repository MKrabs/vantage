package de.mkrabs.vantage;

import static net.minecraft.client.CameraType.FIRST_PERSON;
import static net.minecraft.client.CameraType.THIRD_PERSON_BACK;
import static net.minecraft.client.CameraType.THIRD_PERSON_FRONT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ViewSwitcherTest {
	final VantageConfig config = new VantageConfig();
	final ViewSwitcher switcher = new ViewSwitcher(config);

	@Test
	void mountingLookingAheadSwitchesToThirdPersonBack() {
		assertEquals(THIRD_PERSON_BACK, switcher.tick(true, 0, FIRST_PERSON));
	}

	@Test
	void dismountingRestoresTheViewFromBeforeMounting() {
		switcher.tick(true, 0, THIRD_PERSON_FRONT);
		assertEquals(THIRD_PERSON_FRONT, switcher.tick(false, 0, THIRD_PERSON_BACK));
	}

	@Test
	void lookingDownPastTheLineSwitchesToFirstPerson() {
		switcher.tick(true, 0, FIRST_PERSON);
		assertNull(switcher.tick(true, 65, THIRD_PERSON_BACK));
		assertEquals(FIRST_PERSON, switcher.tick(true, 66, THIRD_PERSON_BACK));
	}

	@Test
	void firstPersonHoldsInsideTheBufferAndLeavesAboveTheExitLine() {
		switcher.tick(true, 0, FIRST_PERSON);
		switcher.tick(true, 70, THIRD_PERSON_BACK);
		assertNull(switcher.tick(true, 50, FIRST_PERSON));
		assertNull(switcher.tick(true, 35, FIRST_PERSON));
		assertEquals(THIRD_PERSON_BACK, switcher.tick(true, 34, FIRST_PERSON));
	}

	@Test
	void lookingUpSwitchesToSelfieAndHoldsUntilBelowTheExitLine() {
		switcher.tick(true, 0, FIRST_PERSON);
		assertNull(switcher.tick(true, -75, THIRD_PERSON_BACK));
		assertEquals(THIRD_PERSON_FRONT, switcher.tick(true, -76, THIRD_PERSON_BACK));
		assertNull(switcher.tick(true, -45, THIRD_PERSON_FRONT));
		assertEquals(THIRD_PERSON_BACK, switcher.tick(true, -44, THIRD_PERSON_FRONT));
	}

	@Test
	void mountingWhileLookingDownStartsInTheDownView() {
		assertEquals(FIRST_PERSON, switcher.tick(true, 80, THIRD_PERSON_BACK));
	}

	@Test
	void manualF5StaysUntilTheNextLineCrossing() {
		switcher.tick(true, 0, FIRST_PERSON);
		assertNull(switcher.tick(true, 20, THIRD_PERSON_FRONT)); // player pressed F5
		assertNull(switcher.tick(true, -20, THIRD_PERSON_FRONT));
		assertEquals(FIRST_PERSON, switcher.tick(true, 70, THIRD_PERSON_FRONT));
	}

	@Test
	void switchingMountsKeepsTheViewFromBeforeTheFirstMount() {
		switcher.tick(true, 0, FIRST_PERSON);
		switcher.tick(true, 0, THIRD_PERSON_BACK); // new vehicle, still riding
		assertEquals(FIRST_PERSON, switcher.tick(false, 0, THIRD_PERSON_BACK));
	}

	@Test
	void disablingWhileRidingRestoresAtOnceAndThenLeavesTheViewAlone() {
		switcher.tick(true, 0, THIRD_PERSON_FRONT);
		assertEquals(THIRD_PERSON_FRONT, switcher.tick(false, 0, THIRD_PERSON_BACK));
		assertNull(switcher.tick(false, 80, THIRD_PERSON_FRONT));
	}

	@Test
	void eachZoneUsesItsConfiguredView() {
		config.upView = THIRD_PERSON_BACK; // selfie turned off
		config.downView = THIRD_PERSON_FRONT;
		switcher.tick(true, 0, FIRST_PERSON);
		assertEquals(THIRD_PERSON_BACK, switcher.tick(true, -80, THIRD_PERSON_BACK));
		switcher.tick(true, 0, THIRD_PERSON_BACK);
		assertEquals(THIRD_PERSON_FRONT, switcher.tick(true, 80, THIRD_PERSON_BACK));
	}
}
