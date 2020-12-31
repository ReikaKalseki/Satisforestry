package Reika.Satisforestry.Biome;

import net.minecraft.client.Minecraft;

import Reika.DragonAPI.Instantiable.Event.Client.NightVisionBrightnessEvent;

public class CaveNightvisionHandler {

	public static final CaveNightvisionHandler instance = new CaveNightvisionHandler();

	private float targetBrightness;
	private float currentBrightness;
	private long lastTick;

	private CaveNightvisionHandler() {

	}

	public void setBrightness(NightVisionBrightnessEvent evt) {
		long tick = Minecraft.getMinecraft().theWorld.getTotalWorldTime();
		if (tick == lastTick) {
			evt.brightness = currentBrightness;
			return;
		}
		long lastCave = evt.player.getEntityData().getLong("biomecavetick");
		boolean inCave = tick-lastCave < 50;
		float target = inCave ? Math.min(evt.brightness, 0.1F) : evt.brightness;
		if (target > currentBrightness) {
			currentBrightness = Math.min(target, currentBrightness+0.03125F*0.5F);
		}
		else if (target < currentBrightness) {
			currentBrightness = Math.max(target, currentBrightness-0.0625F*0.5F);
		}
		evt.brightness = currentBrightness;
		//ReikaJavaLibrary.pConsole(currentBrightness+" of "+target);
		lastTick = tick;
	}

}
