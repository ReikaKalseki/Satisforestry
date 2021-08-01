/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.Satisforestry.Render;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Registry.SFOptions;
import Reika.Satisforestry.Registry.SFShaders;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;


public class ShaderActivation {

	public static final ShaderActivation instance = new ShaderActivation();

	private ShaderActivation() {

	}

	@SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = true) //Not highest because of Dualhotbar
	public void renderHUD(RenderGameOverlayEvent.Pre evt) {
		EntityPlayer ep = Minecraft.getMinecraft().thePlayer;
		int gsc = evt.resolution.getScaleFactor();
		boolean biome = Satisforestry.isPinkForest(ep.worldObj, MathHelper.floor_double(ep.posX), MathHelper.floor_double(ep.posZ));
		if (ep.isPotionActive(Potion.poison) && (SFOptions.GLOBALSHADER.getState() || biome)) {
			if (ep.posY < 70 && biome) {
				SFShaders.CAVEGAS.setIntensity(1);
				SFShaders.CAVEGAS.lingerTime = 0;
				SFShaders.CAVEGAS.rampDownAmount = 0.025F;
				SFShaders.CAVEGAS.rampDownFactor = 0.925F;
			}
			else {
				SFShaders.POISONGAS.setIntensity(Math.min(1, SFShaders.POISONGAS.getIntensity()+0.02F));
				SFShaders.POISONGAS.lingerTime = 10;
				SFShaders.POISONGAS.rampDownAmount = 0.0075F;
				SFShaders.POISONGAS.rampDownFactor = 0.96F;
			}
		}
	}

}
