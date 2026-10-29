package de.mkrabs.vantage.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EaseSliderTest {
	@Test
	void snapsToTheNearbyPresets() {
		assertEquals(0.5f, EaseSlider.snap(0.52f, false));
		assertEquals(0.667f, EaseSlider.snap(0.69f, false));
		assertEquals(0f, EaseSlider.snap(0.02f, false));
	}

	@Test
	void keepsValuesAwayFromPresetsToTheHundredth() {
		assertEquals(1.37f, EaseSlider.snap(1.3712f, false), 1e-6);
	}

	@Test
	void holdingShiftSkipsTheSnapping() {
		assertEquals(0.52f, EaseSlider.snap(0.52f, true), 1e-6);
	}
}
