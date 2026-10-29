package de.mkrabs.vantage;

import net.minecraft.client.CameraType;

/** Decides, once per tick, which camera view to switch to. Returns null for "leave it alone". */
public class ViewSwitcher {
	private enum Zone { DOWN, MIDDLE, UP }

	private final VantageConfig config;
	private boolean wasActive;
	private CameraType beforeMount;
	private Zone zone;

	public ViewSwitcher(VantageConfig config) {
		this.config = config;
	}

	/** @param active riding, mod enabled, not spectating */
	public CameraType tick(boolean active, float pitch, CameraType current) {
		CameraType result = null;
		if (active && !wasActive) {
			beforeMount = current;
			zone = Zone.MIDDLE; // no hysteresis on mount: pick by the enter lines
			zone = nextZone(pitch);
			result = viewFor(zone);
		} else if (!active && wasActive) {
			result = beforeMount;
		} else if (active) {
			Zone next = nextZone(pitch);
			if (next != zone) {
				zone = next;
				result = viewFor(zone);
			}
		}
		wasActive = active;
		return result;
	}

	private Zone nextZone(float pitch) {
		if (zone == Zone.DOWN && pitch >= config.downLeave || zone == Zone.UP && pitch <= config.upLeave) {
			return zone;
		}
		return pitch > config.downEnter ? Zone.DOWN : pitch < config.upEnter ? Zone.UP : Zone.MIDDLE;
	}

	private CameraType viewFor(Zone zone) {
		return switch (zone) {
			case DOWN -> config.downView;
			case MIDDLE -> config.middleView;
			case UP -> config.upView;
		};
	}
}
