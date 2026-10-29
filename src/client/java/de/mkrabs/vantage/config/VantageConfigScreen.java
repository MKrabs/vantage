package de.mkrabs.vantage.config;

import de.mkrabs.vantage.VantageClient;
import de.mkrabs.vantage.VantageConfig;
import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class VantageConfigScreen {
	private VantageConfigScreen() {
	}

	public static Screen create(Screen parent) {
		VantageConfig config = VantageClient.CONFIG;
		VantageConfig defaults = new VantageConfig();

		Option<CameraType> downView = view("View", defaults.downView, () -> config.downView, v -> config.downView = v);
		Option<Float> downEnter = angle("Switch in at", 0, 90, defaults.downEnter, () -> config.downEnter, v -> config.downEnter = v);
		Option<Float> downLeave = angle("Switch back at", 0, 90, defaults.downLeave, () -> config.downLeave, v -> config.downLeave = v);
		Option<CameraType> middleView = view("View", defaults.middleView, () -> config.middleView, v -> config.middleView = v);
		Option<CameraType> upView = view("View", defaults.upView, () -> config.upView, v -> config.upView = v);
		Option<Float> upEnter = angle("Switch in at", -90, 0, defaults.upEnter, () -> config.upEnter, v -> config.upEnter = v);
		Option<Float> upLeave = angle("Switch back at", -90, 0, defaults.upLeave, () -> config.upLeave, v -> config.upLeave = v);

		Option<Boolean> pitchBar = Option.<Boolean>createBuilder()
				.name(Component.literal("Zones"))
				.description(OptionDescription.of(Component.literal(
						"Top is looking straight up, bottom straight down. Shaded bands are buffers: "
								+ "the view only switches back once you leave them. The white line is where you are looking.")))
				.binding(Binding.immutable(true))
				.customController(option -> new PitchBarController(option,
						downEnter, downLeave, upEnter, upLeave, downView, middleView, upView))
				.build();

		return YetAnotherConfigLib.createBuilder()
				.title(Component.literal("Vantage"))
				.category(ConfigCategory.createBuilder()
						.name(Component.literal("Camera"))
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Enabled"))
								.description(OptionDescription.of(Component.literal("Switch views automatically while riding.")))
								.binding(defaults.enabled, () -> config.enabled, v -> config.enabled = v)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.option(pitchBar)
						.group(OptionGroup.createBuilder()
								.name(Component.literal("Looking up"))
								.option(upView).option(upEnter).option(upLeave)
								.build())
						.group(OptionGroup.createBuilder()
								.name(Component.literal("Looking ahead"))
								.description(OptionDescription.of(Component.literal("Also the view you get when you mount.")))
								.option(middleView)
								.build())
						.group(OptionGroup.createBuilder()
								.name(Component.literal("Looking down"))
								.option(downView).option(downEnter).option(downLeave)
								.build())
						.group(OptionGroup.createBuilder()
								.name(Component.literal("Tight spaces"))
								.option(Option.<Float>createBuilder()
										.name(Component.literal("Ease-out time"))
										.description(OptionDescription.of(Component.literal(
												"When something blocks the camera it moves in at once, then eases back out over this time. 0 = instant, like vanilla.")))
										.binding(defaults.easeSeconds, () -> config.easeSeconds, v -> config.easeSeconds = v)
										.controller(opt -> FloatSliderControllerBuilder.create(opt)
												.range(0f, 2f).step(0.05f)
												.formatValue(v -> Component.literal(v == 0 ? "Off" : String.format("%.2f s", v))))
										.build())
								.build())
						.build())
				.save(config::save)
				.build()
				.generateScreen(parent);
	}

	static Component viewName(CameraType view) {
		return Component.literal(switch (view) {
			case FIRST_PERSON -> "First person";
			case THIRD_PERSON_BACK -> "Third person";
			case THIRD_PERSON_FRONT -> "Selfie";
		});
	}

	private static Option<CameraType> view(String name, CameraType def, Supplier<CameraType> get, Consumer<CameraType> set) {
		return Option.<CameraType>createBuilder()
				.name(Component.literal(name))
				.binding(def, get, set)
				.controller(opt -> EnumControllerBuilder.create(opt)
						.enumClass(CameraType.class)
						.formatValue(VantageConfigScreen::viewName))
				.build();
	}

	private static Option<Float> angle(String name, float min, float max, float def, Supplier<Float> get, Consumer<Float> set) {
		return Option.<Float>createBuilder()
				.name(Component.literal(name))
				.binding(def, get, set)
				.controller(opt -> FloatSliderControllerBuilder.create(opt)
						.range(min, max).step(1f)
						.formatValue(v -> Component.literal(String.format("%.0f°", v))))
				.build();
	}
}
