package de.mkrabs.vantage.config;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

/** Whole degrees from -90 (down) to +90 (up), written straight into the config. */
class AngleSlider extends AbstractSliderButton {
	private final String label;
	private final Supplier<Float> get;
	private final Consumer<Float> set;

	AngleSlider(int x, int y, int width, int height, String label, Supplier<Float> get, Consumer<Float> set) {
		super(x, y, width, height, Component.empty(), (get.get() + 90) / 180);
		this.label = label;
		this.get = get;
		this.set = set;
		updateMessage();
	}

	@Override
	protected void updateMessage() {
		setMessage(Component.literal(String.format("%s %+.0f°", label, get.get())));
	}

	@Override
	protected void applyValue() {
		set.accept((float) Math.round(value * 180 - 90));
	}
}
