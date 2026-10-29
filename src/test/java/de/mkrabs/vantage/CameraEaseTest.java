package de.mkrabs.vantage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CameraEaseTest {
	final VantageConfig config = new VantageConfig();
	final CameraEase ease = new CameraEase(config);

	@Test
	void snapsInInstantlyWhenBlocked() {
		ease.apply(4, 0.05f);
		assertEquals(1, ease.apply(1, 0.05f));
	}

	@Test
	void easesBackOutOverTheConfiguredTime() {
		ease.apply(1, 0.05f); // blocked at 1 block
		assertEquals(3, ease.apply(4, 0.25f), 1e-4); // half of 0.5 s: 2 of 4 blocks back
		assertEquals(4, ease.apply(4, 0.25f), 1e-4);
		assertEquals(4, ease.apply(4, 0.05f), 1e-4); // never overshoots
	}

	@Test
	void zeroEaseTimeIsInstantLikeVanilla() {
		config.easeSeconds = 0;
		ease.apply(1, 0.05f);
		assertEquals(4, ease.apply(4, 0.05f));
		ease.apply(1, 0.05f);
		assertEquals(4, ease.apply(4, 0)); // two frames at the same instant
	}

	@Test
	void startsAtFullDistanceAfterAPauseInsteadOfEasingFromAnOldValue() {
		ease.apply(1, 0.05f); // blocked, then e.g. first person for a while
		assertEquals(4, ease.apply(4, 2f));
	}
}
