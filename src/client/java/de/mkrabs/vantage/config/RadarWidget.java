package de.mkrabs.vantage.config;

import de.mkrabs.vantage.VantageConfig;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Display only. A head on the left looking right; a half circle from +90 (up) to -90 (down).
 * One band per layer (layer 1 innermost) up to the highest layer in use; each range is a thick arc in its view's colour,
 * centred in its band. Ranges sharing a band sit side by side so overlaps stay visible. The white beam is the current angle.
 */
class RadarWidget extends AbstractWidget {
	private static final int GRID = 0xFF505050;

	private final VantageConfig config;

	RadarWidget(int x, int y, int width, int height, VantageConfig config) {
		super(x, y, width, height, Component.literal("Ranges"));
		this.config = config;
	}

	/** Rings drawn: layer 1 up to the highest layer any range uses; 0 without ranges. */
	static int rings(VantageConfig config) {
		return config.ranges.stream().mapToInt(r -> r.layer).max().orElse(0);
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		int x = getX(), y = getY(), w = getWidth(), h = getHeight();
		g.fill(x, y, x + w, y + h, 0x55000000);

		int cx = x + 22, cy = y + h / 2;
		int maxR = Math.min(h / 2 - 8, w - 160); // room for the angle label and the legend
		int innerR = 14;
		int rings = Math.max(1, rings(config));
		float band = (maxR - innerR) / (float) rings;

		for (int i = 0; i <= rings; i++) {
			arc(g, cx, cy, innerR + Math.round(i * band), -90, 90, 1, GRID);
		}
		g.fill(cx, cy - maxR, cx + 1, cy + maxR + 1, GRID);
		for (int deg = -45; deg <= 45; deg += 45) {
			double a = Math.toRadians(deg);
			for (int d = innerR; d <= maxR; d++) {
				dot(g, cx + (int) Math.round(d * Math.cos(a)), cy - (int) Math.round(d * Math.sin(a)), 1, GRID);
			}
		}

		int thick = Math.clamp(Math.round(band / 4), 2, 6);
		int[] count = new int[VantageConfig.MAX_LAYER + 1], seen = new int[VantageConfig.MAX_LAYER + 1];
		config.ranges.forEach(r -> count[r.layer]++);
		for (VantageConfig.Range r : config.ranges) {
			float total = count[r.layer] * (thick + 1) - 1;
			int radius = Math.round(innerR + (r.layer - 1) * band + (band - total) / 2) + seen[r.layer]++ * (thick + 1);
			for (int t = 0; t < thick; t++) {
				arc(g, cx, cy, radius + t, Math.min(r.from, r.to), Math.max(r.from, r.to), 2, VantageScreen.viewColor(r.view));
			}
		}

		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null) {
			float angle = -mc.player.getXRot();
			double a = Math.toRadians(angle);
			int tip = maxR + 6;
			for (int d = 10; d <= tip; d += 2) {
				dot(g, cx + (int) Math.round(d * Math.cos(a)), cy - (int) Math.round(d * Math.sin(a)), 1, 0xFFFFFFFF);
			}
			int lx = cx + (int) Math.round((tip + 4) * Math.cos(a));
			int ly = cy - (int) Math.round((tip + 4) * Math.sin(a)) - 4;
			g.text(mc.font, String.format("%+.0f°", angle), lx, Math.max(y + 2, Math.min(y + h - 10, ly)), 0xFFFFFFFF, true);
		}

		head(g, cx, cy);
		g.text(mc.font, "+90°", cx + 4, y + 2, 0xFF9A9A9A, false);
		g.text(mc.font, "-90°", cx + 4, y + h - 10, 0xFF9A9A9A, false);
		g.text(mc.font, "0°", cx + maxR + 6, cy - 4, 0xFF9A9A9A, false);

		int legendY = y + 8;
		for (CameraType view : CameraType.values()) {
			int lx = x + w - 92;
			g.fill(lx, legendY + 2, lx + 6, legendY + 8, VantageScreen.viewColor(view));
			g.text(mc.font, VantageScreen.viewName(view), lx + 10, legendY, 0xFFFFFFFF, false);
			legendY += 12;
		}
		g.text(mc.font, "Inner ring = layer 1", x + w - 92, y + h - 10, 0xFF9A9A9A, false);
		if (!active) {
			g.fill(x, y, x + w, y + h, 0xAA000000); // faded while Vantage is off
		}
	}

	/** A solid arc: steps of under a pixel along the circle. */
	private static void arc(GuiGraphicsExtractor g, int cx, int cy, int radius, float fromDeg, float toDeg, int size, int color) {
		float stepDeg = 45f / Math.max(1, radius);
		for (float a = fromDeg; a <= toDeg; a += stepDeg) {
			double rad = Math.toRadians(a);
			dot(g, cx + (int) Math.round(radius * Math.cos(rad)), cy - (int) Math.round(radius * Math.sin(rad)), size, color);
		}
	}

	private static void dot(GuiGraphicsExtractor g, int px, int py, int size, int color) {
		g.fill(px, py, px + size, py + size, color);
	}

	/** A small blocky head looking right. */
	private static void head(GuiGraphicsExtractor g, int cx, int cy) {
		g.fill(cx - 8, cy - 8, cx + 8, cy + 8, 0xFFC69C72); // skin
		g.fill(cx - 8, cy - 8, cx + 8, cy - 4, 0xFF4A2F1B); // hair
		g.fill(cx - 8, cy - 4, cx - 4, cy + 2, 0xFF4A2F1B); // back of the head
		g.fill(cx + 2, cy - 2, cx + 6, cy + 1, 0xFFFFFFFF); // eye
		g.fill(cx + 4, cy - 2, cx + 6, cy + 1, 0xFF3B5BA8); // pupil, looking ahead
		g.fill(cx + 2, cy + 4, cx + 7, cy + 5, 0xFF8A5A3C); // mouth
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
	}
}
