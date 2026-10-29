package de.mkrabs.vantage;

/** Decides when to force first person because the third-person camera is pushed too close to the head. */
public class CloseCamera {
	private final VantageConfig config;
	private boolean forcing;
	private double since;

	public CloseCamera(VantageConfig config) {
		this.config = config;
	}

	/**
	 * @param thirdPerson riding and the wanted view is third person or selfie
	 * @param room        blocks of free space behind the camera's head
	 * @param now         seconds, any steady clock
	 * @return true while first person should be forced
	 */
	public boolean tick(boolean thirdPerson, float room, double now) {
		if (!config.closeCamera || !thirdPerson) {
			forcing = false;
		} else if (!forcing && room < config.closeEnter) {
			forcing = true;
			since = now;
		} else if (forcing && room > config.closeLeave && now - since >= config.closeMinSeconds) {
			forcing = false;
		}
		return forcing;
	}
}
