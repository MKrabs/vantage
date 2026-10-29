package de.mkrabs.vantage.mixin;

import de.mkrabs.vantage.VantageClient;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class CameraMixin {
	@Unique
	private long vantage$lastNanos = System.nanoTime();

	// getMaxZoom returns the third-person distance after vanilla's block clipping.
	@Inject(method = "getMaxZoom", at = @At("RETURN"), cancellable = true)
	private void vantage$ease(float desired, CallbackInfoReturnable<Float> cir) {
		long now = System.nanoTime();
		float dt = (now - vantage$lastNanos) / 1e9f;
		vantage$lastNanos = now;
		cir.setReturnValue(VantageClient.EASE.apply(cir.getReturnValue(), dt));
	}
}
