package de.mkrabs.vantage;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

public class VantageClient implements ClientModInitializer {
	public static final VantageConfig CONFIG = VantageConfig.load();
	public static final CameraEase EASE = new CameraEase(CONFIG);
	private static final ViewSwitcher SWITCHER = new ViewSwitcher(CONFIG);
	private static final CloseCamera CLOSE = new CloseCamera(CONFIG);

	/** Free space behind the camera, written by CameraMixin every frame it measures. */
	public static volatile float room = Float.MAX_VALUE;

	private static CameraType wanted = CameraType.FIRST_PERSON;
	private static boolean forcing;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(VantageClient::tick);
	}

	private static void tick(Minecraft mc) {
		if (mc.player == null) {
			return;
		}
		CameraType shown = mc.options.getCameraType();
		if (!forcing) {
			wanted = shown; // includes a manual F5
		}
		boolean active = CONFIG.enabled && !mc.player.isSpectator() && mc.player.getVehicle() != null;
		// Minecraft's pitch is negative when looking up; the settings use +90 for straight up.
		CameraType view = SWITCHER.tick(active, -mc.player.getXRot(), wanted);
		if (view != null) {
			wanted = view;
		}
		boolean force = CLOSE.tick(active && !wanted.isFirstPerson(), room, System.nanoTime() / 1e9);
		CameraType target = force ? CameraType.FIRST_PERSON : forcing || view != null ? wanted : null;
		forcing = force;
		if (target != null && target != shown) {
			// Same two calls vanilla's F5 handler makes.
			mc.options.setCameraType(target);
			mc.gameRenderer.checkEntityPostEffect(target.isFirstPerson() ? mc.getCameraEntity() : null);
		}
	}
}
