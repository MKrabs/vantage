package de.mkrabs.vantage;

/** Smooths the third-person camera distance: snaps in when blocked, eases back out. */
public class CameraEase {
	private final VantageConfig config;
	private float current = -1;

	public CameraEase(VantageConfig config) {
		this.config = config;
	}

	/** @param target the distance vanilla allows this frame, @param dt seconds since the last frame */
	public float apply(float target, float dt) {
		if (current < 0 || target < current || config.easeSeconds <= 0) {
			current = target;
		} else {
			current = Math.min(target, current + target * dt / config.easeSeconds);
		}
		return current;
	}
}
