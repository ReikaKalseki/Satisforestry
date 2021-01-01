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
import Reika.Satisforestry.Render.RenderEliteStinger;
import Reika.Satisforestry.Render.RenderFlyingManta;
import Reika.Satisforestry.Render.ResourceNodeRenderer;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class SFClient extends SFCommon {

	public static RedBambooRenderer bamboo;
	private static PinkGrassRenderer grass;
	private static DecorationRenderer deco;
	public static ResourceNodeRenderer resource;

	@Override
	public void registerSounds() {

	}

	@Override
	public void registerRenderers() {
		bambooRender = RenderingRegistry.getNextAvailableRenderId();
		bamboo = new RedBambooRenderer(bambooRender);
		RenderingRegistry.registerBlockHandler(bambooRender, bamboo);

		grassRender = RenderingRegistry.getNextAvailableRenderId();
		grass = new PinkGrassRenderer(grassRender);
		RenderingRegistry.registerBlockHandler(grassRender, grass);

		decoRender = RenderingRegistry.getNextAvailableRenderId();
		deco = new DecorationRenderer(decoRender);
		RenderingRegistry.registerBlockHandler(decoRender, deco);

		resourceRender = RenderingRegistry.getNextAvailableRenderId();
		resource = new ResourceNodeRenderer(resourceRender);
		RenderingRegistry.registerBlockHandler(resourceRender, resource);

		RenderingRegistry.registerEntityRenderingHandler(SFEntities.ELITESTINGER.getObjectClass(), new RenderEliteStinger());
		RenderingRegistry.registerEntityRenderingHandler(SFEntities.MANTA.getObjectClass(), new RenderFlyingManta());
	}

	// Override any other methods that need to be handled differently client side.

	@Override
	public World getClientWorld()
	{
		return FMLClientHandler.instance().getClient().theWorld;
	}

}
