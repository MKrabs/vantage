package de.mkrabs.vantage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.CameraType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VantageConfig {
	private static final Logger LOG = LoggerFactory.getLogger("vantage");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static final int MAX_LAYER = 5;
	public static final int MAX_RANGES = 8;

	public boolean enabled = true;

	// Angles: +90 straight up, -90 straight down. List order breaks ties on a layer.
	public List<Range> ranges = new ArrayList<>(List.of(
			new Range(CameraType.THIRD_PERSON_BACK, 60, -90, 1),
			new Range(CameraType.THIRD_PERSON_FRONT, 90, 20, 1),
			new Range(CameraType.FIRST_PERSON, 20, -20, 2)));

	// Seconds for the camera to ease back to full distance; 0 = instant like vanilla.
	public float easeSeconds = 0.5f;

	public static class Range {
		public CameraType view;
		public float from, to;
		public int layer;

		public Range(CameraType view, float from, float to, int layer) {
			this.view = view;
			this.from = from;
			this.to = to;
			this.layer = layer;
		}

		public boolean contains(float angle) {
			return angle >= Math.min(from, to) && angle <= Math.max(from, to);
		}
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
		return config == null ? new VantageConfig() : config;
	}

	public void save() {
		try {
			Files.writeString(file(), GSON.toJson(this));
		} catch (IOException e) {
			LOG.warn("Could not save {}", file(), e);
		}
	}
}
