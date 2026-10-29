package de.mkrabs.vantage.config;

import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** Read-only row that draws the three pitch zones from the sliders' pending values, so it updates while dragging. */
public class PitchBarController implements Controller<Boolean> {
	private static final int HEIGHT = 120;
	private static final int UP = 0xFF9B7BD8, MIDDLE = 0xFF4A90D9, DOWN = 0xFF4CAF6A;

	private final Option<Boolean> option;
	private final Option<Float> downEnter, downLeave, upEnter, upLeave;
	private final Option<CameraType> downView, middleView, upView;

	public PitchBarController(Option<Boolean> option, Option<Float> downEnter, Option<Float> downLeave,
			Option<Float> upEnter, Option<Float> upLeave,
			Option<CameraType> downView, Option<CameraType> middleView, Option<CameraType> upView) {
		this.option = option;
		this.downEnter = downEnter;
		this.downLeave = downLeave;
		this.upEnter = upEnter;
		this.upLeave = upLeave;
		this.downView = downView;
		this.middleView = middleView;
		this.upView = upView;
	}

	@Override
	public Option<Boolean> option() {
		return option;
	}

	@Override
	public Component formatValue() {
		return Component.empty();
	}

	@Override
	public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> dim) {
		return new Bar(dim.withHeight(HEIGHT));
	}

	private class Bar extends AbstractWidget {
		private boolean focused;

		Bar(Dimension<Integer> dim) {
			super(dim);
		}

		@Override
		public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
			Dimension<Integer> d = getDimension();
			int top = d.y() + 4, bottom = d.y() + d.height() - 4;
			int left = d.x() + 8, right = left + 14;

			int yUpEnter = y(upEnter.pendingValue(), top, bottom);
			int yUpLeave = y(upLeave.pendingValue(), top, bottom);
			int yDownLeave = y(downLeave.pendingValue(), top, bottom);
			int yDownEnter = y(downEnter.pendingValue(), top, bottom);

			g.fill(left, top, right, yUpEnter, UP);
			g.fill(left, yUpEnter, right, yUpLeave, half(UP));
			g.fill(left, yUpLeave, right, yDownLeave, MIDDLE);
			g.fill(left, yDownLeave, right, yDownEnter, half(DOWN));
			g.fill(left, yDownEnter, right, bottom, DOWN);

			int textX = right + 8;
			label(g, VantageConfigScreen.viewName(upView.pendingValue()), textX, (top + yUpEnter) / 2, UP);
			label(g, VantageConfigScreen.viewName(middleView.pendingValue()), textX, (yUpLeave + yDownLeave) / 2, MIDDLE);
			label(g, VantageConfigScreen.viewName(downView.pendingValue()), textX, (yDownEnter + bottom) / 2, DOWN);

			if (client.player != null) {
				int yPlayer = y(client.player.getXRot(), top, bottom);
				g.fill(left - 3, yPlayer, right + 3, yPlayer + 1, 0xFFFFFFFF);
			}
		}

		private void label(GuiGraphicsExtractor g, Component text, int x, int centerY, int color) {
			g.text(textRenderer, text, x, centerY - textRenderer.lineHeight / 2, color, true);
		}

		private static int y(float pitch, int top, int bottom) {
			return top + Math.round((pitch + 90) / 180 * (bottom - top));
		}

		private static int half(int argb) {
			return (argb & 0x00FFFFFF) | 0x66000000;
		}

		@Override
		public void setFocused(boolean focused) {
			this.focused = focused;
		}

		@Override
		public boolean isFocused() {
			return focused;
		}
	}
}
