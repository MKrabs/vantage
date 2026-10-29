package de.mkrabs.vantage;

import net.minecraft.client.CameraType;

public class VantageConfig {
	public CameraType downView = CameraType.FIRST_PERSON;
	public CameraType middleView = CameraType.THIRD_PERSON_BACK;
	public CameraType upView = CameraType.THIRD_PERSON_FRONT;

	// Pitch in degrees: -90 straight up, +90 straight down.
	public float downEnter = 65;
	public float downLeave = 35;
	public float upEnter = -75;
	public float upLeave = -45;
}
