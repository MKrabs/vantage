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
 * Each range is an arc in its view's colour, one ring per layer (layer 1 innermost).
 * Ranges sharing a layer sit slightly apart so overlaps stay visible. The white beam is the current angle.
 */
class RadarWidget extends AbstractWidget {
	private final VantageConfig config;

	RadarWidget(int x, int y, int width, int height, VantageConfig config) {
		super(x, y, width, height, Component.literal("Ranges"));
		this.config = config;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		int x = getX(), y = getY(), w = getWidth(), h = getHeight();
		g.fill(x, y, x + w, y + h, 0x55000000);

		int cx = x + 22, cy = y + h / 2;
		int maxR = Math.min(h / 2 - 8, w - 130);
		int innerR = 16;
		float layerStep = (maxR - innerR) / (float) VantageConfig.MAX_LAYER;

		boolean[] used = new boolean[VantageConfig.MAX_LAYER + 1];
		int[] seen = new int[VantageConfig.MAX_LAYER + 1];
		for (VantageConfig.Range r : config.ranges) {
			used[r.layer] = true;
		}
		for (int layer = 1; layer <= VantageConfig.MAX_LAYER; layer++) {
			if (used[layer]) {
				arc(g, cx, cy, ringRadius(innerR, layerStep, layer) - 1, -90, 90, 6, 1, 0x40FFFFFF);
			}
		}
		for (VantageConfig.Range r : config.ranges) {
			int radius = ringRadius(innerR, layerStep, r.layer) + 3 * seen[r.layer]++;
			arc(g, cx, cy, radius, Math.min(r.from, r.to), Math.max(r.from, r.to), 1, 2, VantageScreen.viewColor(r.view));
		}

		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null) {
			double a = Math.toRadians(-mc.player.getXRot());
			for (int d = 10; d <= maxR + 4; d += 2) {
				dot(g, cx + (int) Math.round(d * Math.cos(a)), cy - (int) Math.round(d * Math.sin(a)), 1, 0xFFFFFFFF);
			}
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
	}

	private static int ringRadius(int innerR, float step, int layer) {
		return innerR + Math.round((layer - 1) * step);
	}

	private static void arc(GuiGraphicsExtractor g, int cx, int cy, int radius, float fromDeg, float toDeg, int stepDeg, int size, int color) {
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
