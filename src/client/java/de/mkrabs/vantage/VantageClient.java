package de.mkrabs.vantage;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

public class VantageClient implements ClientModInitializer {
	public static final VantageConfig CONFIG = new VantageConfig();
	public static final CameraEase EASE = new CameraEase(CONFIG);
	private static final ViewSwitcher SWITCHER = new ViewSwitcher(CONFIG);

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(VantageClient::tick);
	}

	private static void tick(Minecraft mc) {
		if (mc.player == null) {
			return;
		}
		boolean active = CONFIG.enabled && !mc.player.isSpectator() && mc.player.getVehicle() != null;
		CameraType view = SWITCHER.tick(active, mc.player.getXRot(), mc.options.getCameraType());
		if (view != null && view != mc.options.getCameraType()) {
			// Same two calls vanilla's F5 handler makes.
			mc.options.setCameraType(view);
			mc.gameRenderer.checkEntityPostEffect(view.isFirstPerson() ? mc.getCameraEntity() : null);
		}
	}
}
