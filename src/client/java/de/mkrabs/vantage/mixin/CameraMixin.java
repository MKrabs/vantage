package de.mkrabs.vantage.mixin;

import de.mkrabs.vantage.VantageClient;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class CameraMixin {
	@Unique
	private long vantage$lastNanos = System.nanoTime();
	@Unique
	private boolean vantage$measuring;

	@Shadow
	private float getMaxZoom(float desired) {
		throw new AssertionError();
	}

	// getMaxZoom returns the third-person distance after vanilla's block clipping.
	@Inject(method = "getMaxZoom", at = @At("RETURN"), cancellable = true)
	private void vantage$ease(float desired, CallbackInfoReturnable<Float> cir) {
		VantageClient.room = cir.getReturnValue();
		if (vantage$measuring) {
			return;
		}
		long now = System.nanoTime();
		float dt = (now - vantage$lastNanos) / 1e9f;
		vantage$lastNanos = now;
		cir.setReturnValue(VantageClient.EASE.apply(cir.getReturnValue(), dt));
	}

	// In first person vanilla never measures; the camera sits at the eyes facing the look direction, so measure here.
	@Inject(method = "alignWithEntity", at = @At("TAIL"))
	private void vantage$measureInFirstPerson(float partialTick, CallbackInfo ci) {
		if (VantageClient.CONFIG.closeCamera && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
			vantage$measuring = true;
			getMaxZoom(VantageClient.CONFIG.closeLeave + 1);
			vantage$measuring = false;
		}
	}
}
