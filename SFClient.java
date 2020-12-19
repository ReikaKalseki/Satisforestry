/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.Satisforestry;

import net.minecraft.world.World;

import Reika.Satisforestry.Render.DecorationRenderer;
import Reika.Satisforestry.Render.PinkGrassRenderer;
import Reika.Satisforestry.Render.RedBambooRenderer;
import Reika.Satisforestry.Render.ResourceNodeRenderer;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class SFClient extends SFCommon {

	private static final RedBambooRenderer bamboo = new RedBambooRenderer();
	private static final PinkGrassRenderer grass = new PinkGrassRenderer();
	private static final DecorationRenderer deco = new DecorationRenderer();
	private static final ResourceNodeRenderer resource = new ResourceNodeRenderer();

	@Override
	public void registerSounds() {

	}

	@Override
	public void registerRenderers() {
		bambooRender = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(bambooRender, bamboo);

		grassRender = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(grassRender, grass);

		decoRender = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(decoRender, deco);

		resourceRender = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(resourceRender, resource);
	}

	// Override any other methods that need to be handled differently client side.

	@Override
	public World getClientWorld()
	{
		return FMLClientHandler.instance().getClient().theWorld;
	}

}
