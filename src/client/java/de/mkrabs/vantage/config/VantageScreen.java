package de.mkrabs.vantage.config;

import de.mkrabs.vantage.VantageClient;
import de.mkrabs.vantage.VantageConfig;
import java.util.List;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Settings: radar on top, one row per range, then on/off and ease-out. Edits apply live; saved on close. */
public class VantageScreen extends Screen {
	private static final int MAX_WIDTH = 360, ROW = 22, GAP = 4;
	private static final List<Integer> LAYERS = List.of(1, 2, 3, 4, 5);

	private final Screen parent;
	private final VantageConfig config = VantageClient.CONFIG;
	private int hintX, hintY;

	public VantageScreen(Screen parent) {
		super(Component.literal("Vantage"));
		this.parent = parent;
	}

	static Component viewName(CameraType view) {
		return Component.literal(switch (view) {
			case FIRST_PERSON -> "First person";
			case THIRD_PERSON_BACK -> "Third person";
			case THIRD_PERSON_FRONT -> "Selfie";
		}).withColor(viewColor(view));
	}

	static int viewColor(CameraType view) {
		return switch (view) {
			case FIRST_PERSON -> 0xFF4CAF6A;
			case THIRD_PERSON_BACK -> 0xFF4A90D9;
			case THIRD_PERSON_FRONT -> 0xFFB08BEA;
		};
	}

	@Override
	protected void init() {
		int w = Math.min(MAX_WIDTH, width - 20);
		int left = (width - w) / 2;
		int below = config.ranges.size() * ROW + ROW + GAP + ROW + 12 + ROW;
		int radarHeight = Math.max(60, Math.min(130, height - 24 - below - 8));

		int y = 20;
		addRenderableWidget(new RadarWidget(left, y, w, radarHeight, config));
		y += radarHeight + GAP;

		// view | from | to | layer | delete
		int avail = w - 20 - 4 * GAP;
		int viewW = avail * 30 / 100, angleW = avail * 25 / 100, layerW = avail - viewW - 2 * angleW;
		for (VantageConfig.Range r : config.ranges) {
			int x = left;
			addRenderableWidget(CycleButton.builder(VantageScreen::viewName, r.view)
					.withValues(CameraType.values()).displayOnlyValue()
					.create(x, y, viewW, 20, Component.literal("View"), (b, v) -> r.view = v));
			x += viewW + GAP;
			addRenderableWidget(new AngleSlider(x, y, angleW, 20, "From", () -> r.from, v -> r.from = v));
			x += angleW + GAP;
			addRenderableWidget(new AngleSlider(x, y, angleW, 20, "To", () -> r.to, v -> r.to = v));
			x += angleW + GAP;
			addRenderableWidget(CycleButton.builder((Integer l) -> Component.literal("Layer " + l), r.layer)
					.withValues(LAYERS).displayOnlyValue()
					.create(x, y, layerW, 20, Component.literal("Layer"), (b, v) -> r.layer = v));
			x += layerW + GAP;
			addRenderableWidget(Button.builder(Component.literal("×"), b -> {
				config.ranges.remove(r);
				rebuildWidgets();
			}).bounds(x, y, 20, 20).build());
			y += ROW;
		}

		Button add = addRenderableWidget(Button.builder(Component.literal("+ Add range"), b -> {
			config.ranges.add(new VantageConfig.Range(CameraType.THIRD_PERSON_BACK, 10, -10, 1));
			rebuildWidgets();
		}).bounds(left, y, w, 20).build());
		add.active = config.ranges.size() < VantageConfig.MAX_RANGES;
		y += ROW + GAP;

		int half = (w - GAP) / 2;
		addRenderableWidget(CycleButton.onOffBuilder(config.enabled)
				.create(left, y, half, 20, Component.literal("Vantage"), (b, v) -> config.enabled = v));
		addRenderableWidget(new EaseSlider(left + half + GAP, y, half, 20, config));
		hintX = left + half + GAP + half / 2;
		hintY = y + 22;
		y += ROW + 12;

		addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
				.bounds(width / 2 - 50, y, 100, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		super.extractRenderState(g, mouseX, mouseY, delta);
		g.centeredText(font, title, width / 2, 7, 0xFFFFFFFF);
		g.centeredText(font, "Hold Shift for any value", hintX, hintY, 0xFF9A9A9A);
	}

	@Override
	public void onClose() {
		config.sanitize();
		config.save();
		minecraft.gui.setScreen(parent);
	}
}
