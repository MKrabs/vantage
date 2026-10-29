package de.mkrabs.vantage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.client.CameraType;
import org.junit.jupiter.api.Test;

class VantageConfigTest {
	@Test
	void sanitizeRepairsAHandEditedFile() {
		VantageConfig config = new VantageConfig();
		VantageConfig.Range broken = new VantageConfig.Range(CameraType.FIRST_PERSON, 200, Float.NaN, 9);
		VantageConfig.Range noView = new VantageConfig.Range(null, 10, -10, 1);
		config.ranges = new ArrayList<>(Arrays.asList(broken, null, noView));
		config.easeSeconds = -1;
		config.sanitize();
		assertEquals(1, config.ranges.size());
		assertEquals(90, broken.from);
		assertEquals(-90, broken.to);
		assertEquals(VantageConfig.MAX_LAYER, broken.layer);
		assertEquals(0, config.easeSeconds);
	}

	@Test
	void sanitizeCapsTheNumberOfRangesAndRestoresAMissingList() {
		VantageConfig config = new VantageConfig();
		config.ranges = new ArrayList<>();
		for (int i = 0; i < 12; i++) {
			config.ranges.add(new VantageConfig.Range(CameraType.FIRST_PERSON, 10, -10, 1));
		}
		config.sanitize();
		assertEquals(VantageConfig.MAX_RANGES, config.ranges.size());

		config.ranges = null;
		config.sanitize();
		assertEquals(new VantageConfig().ranges.size(), config.ranges.size());
	}
}
