package Reika.Satisforestry.Biome;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;

import Reika.DragonAPI.Instantiable.Event.Client.NightVisionBrightnessEvent;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.Satisforestry.Satisforestry;

public class CaveNightvisionHandler {

	public static final CaveNightvisionHandler instance = new CaveNightvisionHandler();

	private float targetBrightness;
	private float currentBrightness;
	private long lastTick;

	private CaveNightvisionHandler() {

	}

	public void setBrightness(NightVisionBrightnessEvent evt) {
		Minecraft mc = Minecraft.getMinecraft();
		long tick = mc.theWorld.getTotalWorldTime();
		if (tick == lastTick) {
			evt.brightness = currentBrightness;
			return;
		}
		//long lastCave = evt.player.getEntityData().getLong("biomecavetick");
		EntityPlayer ep = mc.thePlayer;
		int x = MathHelper.floor_double(ep.posX);
		int y = MathHelper.floor_double(ep.posY);
		int z = MathHelper.floor_double(ep.posZ);
		boolean biome = Satisforestry.isPinkForest(mc.theWorld.getBiomeGenForCoords(x, z));
		//boolean inCave = tick-lastCave < 50;
		int light = mc.theWorld.getSavedLightValue(EnumSkyBlock.Sky, x, y, z);
		float max = (float)ReikaMathLibrary.normalizeToBounds(light, 0.1, 1, 0, 15);//0.1F;
		float target = Math.min(evt.brightness, max);
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
