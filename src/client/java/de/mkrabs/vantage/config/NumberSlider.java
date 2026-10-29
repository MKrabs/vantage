package de.mkrabs.vantage.config;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

/** A value from min to max in fixed steps, written straight into the config. */
class NumberSlider extends AbstractSliderButton {
	private final float min, max, step;
	private final Function<Float, String> format;
	private final Supplier<Float> get;
	private final Consumer<Float> set;

	NumberSlider(int x, int y, int width, int height, float min, float max, float step,
			Function<Float, String> format, Supplier<Float> get, Consumer<Float> set) {
		super(x, y, width, height, Component.empty(), (get.get() - min) / (max - min));
		this.min = min;
		this.max = max;
		this.step = step;
		this.format = format;
		this.get = get;
		this.set = set;
		updateMessage();
	}

	static NumberSlider angle(int x, int y, int width, String label, Supplier<Float> get, Consumer<Float> set) {
		return new NumberSlider(x, y, width, 20, -90, 90, 1, v -> String.format("%s %+.0f°", label, v), get, set);
	}

	@Override
	protected void updateMessage() {
		setMessage(Component.literal(format.apply(get.get())));
	}

	@Override
	protected void applyValue() {
		float v = Math.round((min + (float) value * (max - min)) / step) * step;
		set.accept(v);
		value = (v - min) / (max - min); // the handle jumps to the step instead of gliding
	}
}
