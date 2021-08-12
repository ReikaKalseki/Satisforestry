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
import net.minecraftforge.client.MinecraftForgeClient;

import Reika.Satisforestry.Blocks.TileNodeHarvester;
import Reika.Satisforestry.Registry.SFBlocks;
import Reika.Satisforestry.Registry.SFEntities;
import Reika.Satisforestry.Registry.SFShaders;
import Reika.Satisforestry.Render.DecorationRenderer;
import Reika.Satisforestry.Render.PinkGrassRenderer;
import Reika.Satisforestry.Render.PowerSlugRenderer;
import Reika.Satisforestry.Render.RedBambooRenderer;
import Reika.Satisforestry.Render.RenderEliteStinger;
import Reika.Satisforestry.Render.RenderFlyingManta;
import Reika.Satisforestry.Render.RenderLizardDoggo;
import Reika.Satisforestry.Render.ResourceNodeRenderer;
import Reika.Satisforestry.Render.SFMinerItemRenderer;
import Reika.Satisforestry.Render.SFMinerRenderer;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class SFClient extends SFCommon {

	public static RedBambooRenderer bamboo;
	private static PinkGrassRenderer grass;
	private static DecorationRenderer deco;
	public static ResourceNodeRenderer resource;
	public static PowerSlugRenderer slug;

	@Override
	public void registerSounds() {
		sounds.register();
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

		slugRender = RenderingRegistry.getNextAvailableRenderId();
		slug = new PowerSlugRenderer(slugRender);
		RenderingRegistry.registerBlockHandler(slugRender, slug);

		RenderingRegistry.registerEntityRenderingHandler(SFEntities.ELITESTINGER.getObjectClass(), new RenderEliteStinger());
		RenderingRegistry.registerEntityRenderingHandler(SFEntities.MANTA.getObjectClass(), new RenderFlyingManta());
		RenderingRegistry.registerEntityRenderingHandler(SFEntities.DOGGO.getObjectClass(), RenderLizardDoggo.instance);

		ClientRegistry.bindTileEntitySpecialRenderer(TileNodeHarvester.class, new SFMinerRenderer());
		MinecraftForgeClient.registerItemRenderer(SFBlocks.HARVESTER.getItem(), new SFMinerItemRenderer());

		SFShaders.registerAll();
	}

	// Override any other methods that need to be handled differently client side.

	@Override
	public World getClientWorld()
	{
		return FMLClientHandler.instance().getClient().theWorld;
	}

}
