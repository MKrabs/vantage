package de.mkrabs.vantage.config;

import de.mkrabs.vantage.VantageClient;
import de.mkrabs.vantage.VantageConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Settings: on/off, then the Ranges and Tight spaces sections between rulers, scrolling above pinned bottom buttons.
 * Edits apply live; Done saves, Cancel reverts.
 */
public class VantageScreen extends Screen {
	private static final int MAX_WIDTH = 360, ROW = 22, GAP = 4, TOP = 20, FOOTER_BUTTONS = 3, RULER_COLOR = 0x60FFFFFF;
	private static final List<Integer> LAYERS = List.of(1, 2, 3, 4, 5);

	private final Screen parent;
	private final VantageConfig config = VantageClient.CONFIG;
	private final VantageConfig snapshot = config.copy();
	private boolean resetArmed;
	private int hintX, hintY, footerY, viewBottom, scroll, maxScroll;
	private final List<Ruler> rulers = new ArrayList<>();

	private record Ruler(int y, String title) {
	}

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
		rulers.clear();
		int w = Math.min(MAX_WIDTH, width - 20);
		int left = (width - w) / 2;
		int half = (w - GAP) / 2;
		footerY = height - 24;
		viewBottom = footerY - GAP;

		// footer first: it then wins clicks over content scrolled underneath it
		int third = (w - 2 * GAP) / 3;
		addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> {
			config.copyFrom(snapshot);
			minecraft.gui.setScreen(parent);
		}).bounds(left, footerY, third, 20).build());
		addRenderableWidget(Button.builder(Component.literal(resetArmed ? "Click again to reset" : "Reset to defaults"), b -> {
			if (resetArmed) {
				config.copyFrom(new VantageConfig());
			}
			resetArmed = !resetArmed;
			rebuildWidgets();
		}).bounds(left + third + GAP, footerY, third, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
				.bounds(left + 2 * (third + GAP), footerY, third, 20).build());

		// content, laid out from the top and shifted by the scroll amount at the end
		int y = TOP;
		Checkbox enabled = addRenderableWidget(Checkbox.builder(Component.literal("Vantage enabled"), font)
				.pos(left, y).selected(config.enabled).onValueChange((c, v) -> {
					config.enabled = v;
					rebuildWidgets();
				}).build());
		y += ROW;

		y = ruler(y, "Ranges");
		int radarHeight = 180 + 20 * Math.max(0, RadarWidget.rings(config) - 2);
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
			addRenderableWidget(NumberSlider.angle(x, y, angleW, "From", () -> r.from, v -> r.from = v));
			x += angleW + GAP;
			addRenderableWidget(NumberSlider.angle(x, y, angleW, "To", () -> r.to, v -> r.to = v));
			x += angleW + GAP;
			addRenderableWidget(CycleButton.builder((Integer l) -> Component.literal("Layer " + l), r.layer)
					.withValues(LAYERS).displayOnlyValue()
					.create(x, y, layerW, 20, Component.literal("Layer"), (b, v) -> {
						r.layer = v;
						rebuildWidgets(); // radar height follows the ring count
					}));
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
		y += ROW;

		y = ruler(y, "Tight spaces");
		addRenderableWidget(Checkbox.builder(Component.literal("Too close: 1st person"), font)
				.pos(left, y).selected(config.closeCamera).onValueChange((c, v) -> {
					config.closeCamera = v;
					rebuildWidgets();
				}).build());
		y += ROW;

		addRenderableWidget(new EaseSlider(left, y, half, 20, config));
		hintX = left + half / 2;
		hintY = y + 22;
		NumberSlider stay = addRenderableWidget(new NumberSlider(left + half + GAP, y, half, 20, 0, 5, 0.1f,
				v -> String.format("Stay at least %.1f s", v), () -> config.closeMinSeconds, v -> config.closeMinSeconds = v));
		y += ROW + 12;
		NumberSlider below = addRenderableWidget(new NumberSlider(left, y, half, 20, 0.5f, 4, 0.5f,
				v -> String.format("Switch below %.1f blocks", v), () -> config.closeEnter, v -> config.closeEnter = v));
		NumberSlider above = addRenderableWidget(new NumberSlider(left + half + GAP, y, half, 20, 1, 6, 0.5f,
				v -> String.format("Back above %.1f blocks", v), () -> config.closeLeave, v -> config.closeLeave = v));
		stay.active = below.active = above.active = config.closeCamera;
		y += ROW;

		maxScroll = Math.max(0, y - viewBottom);
		scroll = Math.clamp(scroll, 0, maxScroll);
		for (GuiEventListener c : content()) {
			AbstractWidget widget = (AbstractWidget) c;
			widget.setY(widget.getY() - scroll);
			if (!config.enabled && widget != enabled) {
				widget.active = false;
			}
		}
	}

	private List<? extends GuiEventListener> content() {
		return children().subList(FOOTER_BUTTONS, children().size());
	}

	/** Remembers a titled ruler at y and returns the y below it, with half a row of space under the title. */
	private int ruler(int y, String title) {
		rulers.add(new Ruler(y + 6, title));
		return y + 12 + ROW / 2;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (maxScroll > 0) {
			scroll -= (int) (scrollY * 20);
			rebuildWidgets();
		}
		return true;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		// content scrolled out of the visible area must not take clicks
		if (event.y() < TOP || event.y() >= viewBottom && event.y() < footerY) {
			return false;
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		g.centeredText(font, title, width / 2, 7, 0xFFFFFFFF);
		int rulerLeft = (width - Math.min(MAX_WIDTH, width - 20)) / 2, rulerRight = width - rulerLeft, mid = width / 2;
		g.enableScissor(0, TOP, width, viewBottom);
		for (GuiEventListener c : content()) {
			((Renderable) c).extractRenderState(g, mouseX, mouseY, delta);
		}
		g.centeredText(font, "Hold Shift for any value", hintX, hintY - scroll, 0xFF9A9A9A);
		for (Ruler r : rulers) {
			int y = r.y - scroll, textW = font.width(r.title) + 8;
			g.fill(rulerLeft, y, mid - textW / 2, y + 1, RULER_COLOR);
			g.fill(mid + textW / 2, y, rulerRight, y + 1, RULER_COLOR);
			g.centeredText(font, r.title, mid, y - 4, 0xFF9A9A9A);
		}
		if (maxScroll > 0) {
			int track = viewBottom - TOP, bar = Math.max(10, track * track / (track + maxScroll));
			int barY = TOP + (track - bar) * scroll / maxScroll;
			g.fill(rulerRight + 4, barY, rulerRight + 6, barY + bar, 0x80FFFFFF);
		}
		g.disableScissor();
		for (GuiEventListener c : children().subList(0, FOOTER_BUTTONS)) {
			((Renderable) c).extractRenderState(g, mouseX, mouseY, delta);
		}
	}

	@Override
	public void onClose() {
		config.sanitize();
		config.save();
		minecraft.gui.setScreen(parent);
	}
}
