package de.mkrabs.vantage;

import java.util.Arrays;
import net.minecraft.client.CameraType;

/** Decides, once per tick, which camera view to switch to. Returns null for "leave it alone". */
public class ViewSwitcher {
	private final VantageConfig config;
	private boolean wasActive;
	private CameraType beforeMount;
	private final VantageConfig.Range[] held = new VantageConfig.Range[VantageConfig.MAX_LAYER + 1];
	private VantageConfig.Range winner;

	public ViewSwitcher(VantageConfig config) {
		this.config = config;
	}

	/**
	 * @param active riding, mod enabled, not spectating
	 * @param angle  +90 straight up, -90 straight down
	 */
	public CameraType tick(boolean active, float angle, CameraType current) {
		CameraType result = null;
		if (active && !wasActive) {
			beforeMount = current;
			Arrays.fill(held, null);
			winner = null;
		}
		if (active) {
			VantageConfig.Range next = null;
			for (int layer = 1; layer <= VantageConfig.MAX_LAYER; layer++) {
				VantageConfig.Range h = held[layer];
				// The settings screen can delete or re-layer a range mid-ride.
				if (h == null || !h.contains(angle) || h.layer != layer || !config.ranges.contains(h)) {
					held[layer] = firstContaining(layer, angle);
				}
				if (held[layer] != null) {
					next = held[layer];
				}
			}
			if (next != winner && next != null) {
				result = next.view;
			}
			winner = next;
		} else if (wasActive) {
			result = beforeMount;
		}
		wasActive = active;
		return result;
	}

	private VantageConfig.Range firstContaining(int layer, float angle) {
		for (VantageConfig.Range range : config.ranges) {
			if (range.layer == layer && range.contains(angle)) {
				return range;
			}
		}
		return null;
	}
}
