package de.mkrabs.vantage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

// Defaults under test: switch below 1.5 blocks, back above 3 blocks, at least 2 s.
class CloseCameraTest {
	final VantageConfig config = new VantageConfig();
	final CloseCamera close = new CloseCamera(config);

	{
		config.closeCamera = true;
	}

	@Test
	void switchesToFirstPersonWhenTheCameraIsPushedClose() {
		assertFalse(close.tick(true, 2, 0));
		assertTrue(close.tick(true, 1.4f, 0.1));
	}

	@Test
	void switchesBackOnlyWithEnoughRoomAndAfterTheMinimumTime() {
		close.tick(true, 1, 10);
		assertTrue(close.tick(true, 4, 11)); // enough room, but only 1 s
		assertTrue(close.tick(true, 2.5f, 12.5)); // long enough, but not enough room
		assertFalse(close.tick(true, 4, 13));
	}

	@Test
	void staysOutOfTheWayWhenOffOrNotInThirdPerson() {
		assertFalse(close.tick(false, 0.5f, 0)); // not riding, or first person wanted
		close.tick(true, 0.5f, 1);
		assertFalse(close.tick(false, 0.5f, 1.1)); // dismounting ends it at once
		config.closeCamera = false;
		assertFalse(close.tick(true, 0.5f, 2));
	}
}
