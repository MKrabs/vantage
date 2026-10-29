package de.mkrabs.vantage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.client.CameraType;
import org.junit.jupiter.api.Test;

class VantageConfigTest {
	@Test
	void sanitizeKeepsEachExitLineInsideItsZoneSoTheViewCannotFlicker() {
		VantageConfig config = new VantageConfig();
		config.downEnter = 40;
		config.downLeave = 60;
		config.upEnter = -30;
		config.upLeave = -70;
		config.sanitize();
		assertEquals(40, config.downLeave);
		assertEquals(-30, config.upLeave);
	}

	@Test
	void sanitizeRepairsMissingAndOutOfRangeValuesFromAHandEditedFile() {
		VantageConfig config = new VantageConfig();
		config.upView = null;
		config.downEnter = 200;
		config.easeSeconds = -1;
		config.sanitize();
		assertEquals(CameraType.THIRD_PERSON_FRONT, config.upView);
		assertEquals(90, config.downEnter);
		assertEquals(0, config.easeSeconds);
	}
}
