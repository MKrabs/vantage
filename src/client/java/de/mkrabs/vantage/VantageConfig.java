package de.mkrabs.vantage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.CameraType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VantageConfig {
	private static final Logger LOG = LoggerFactory.getLogger("vantage");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public boolean enabled = true;

	public CameraType downView = CameraType.FIRST_PERSON;
	public CameraType middleView = CameraType.THIRD_PERSON_BACK;
	public CameraType upView = CameraType.THIRD_PERSON_FRONT;

	// Pitch in degrees: -90 straight up, +90 straight down.
	public float downEnter = 65;
	public float downLeave = 35;
	public float upEnter = -75;
	public float upLeave = -45;

	// Seconds for the camera to ease back to full distance; 0 = instant like vanilla.
	public float easeSeconds = 0.5f;

	/** Repairs values a hand-edited file could break. An exit line past its enter line would flicker every tick. */
	public void sanitize() {
		VantageConfig defaults = new VantageConfig();
		if (downView == null) downView = defaults.downView;
		if (middleView == null) middleView = defaults.middleView;
		if (upView == null) upView = defaults.upView;
		downEnter = clamp(downEnter, -90, 90);
		upEnter = clamp(upEnter, -90, 90);
		downLeave = clamp(downLeave, -90, downEnter);
		upLeave = clamp(upLeave, upEnter, 90);
		easeSeconds = clamp(easeSeconds, 0, 2);
	}

	private static float clamp(float value, float min, float max) {
		return Float.isNaN(value) ? min : Math.max(min, Math.min(max, value));
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("vantage.json");
	}

	public static VantageConfig load() {
		VantageConfig config = null;
		try {
			if (Files.exists(file())) {
				config = GSON.fromJson(Files.readString(file()), VantageConfig.class);
			}
		} catch (IOException | RuntimeException e) {
			LOG.warn("Could not read {}, using defaults", file(), e);
		}
		if (config == null) {
			config = new VantageConfig();
		}
		config.sanitize();
		return config;
	}

	public void save() {
		sanitize();
		try {
			Files.writeString(file(), GSON.toJson(this));
		} catch (IOException e) {
			LOG.warn("Could not save {}", file(), e);
		}
	}
}
