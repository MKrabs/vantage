package de.mkrabs.vantage.config;

import de.mkrabs.vantage.VantageConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

/** Ease-out time, 0-2 s. Snaps to a few presets unless Shift is held. */
class EaseSlider extends AbstractSliderButton {
	private static final float MAX = 2;
	private static final float[] PRESETS = {0, 0.2f, 0.5f, 0.667f, 1};
	private static final float SNAP_DISTANCE = 0.03f;

	private final VantageConfig config;

	EaseSlider(int x, int y, int width, int height, VantageConfig config) {
		super(x, y, width, height, Component.empty(), config.easeSeconds / MAX);
		this.config = config;
		updateMessage();
	}

	static float snap(float seconds, boolean free) {
		if (!free) {
			for (float preset : PRESETS) {
				if (Math.abs(seconds - preset) <= SNAP_DISTANCE) {
					return preset;
				}
			}
		}
		return Math.round(seconds * 100) / 100f;
	}

	@Override
	protected void updateMessage() {
		float s = config.easeSeconds;
		setMessage(Component.literal("Ease-out: " + (s == 0 ? "Off" : String.format("%.2f s", s))));
	}

	@Override
	protected void applyValue() {
		config.easeSeconds = snap((float) value * MAX, Minecraft.getInstance().hasShiftDown());
		value = config.easeSeconds / MAX;
	}
}
