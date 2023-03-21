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

import net.minecraft.client.audio.SoundCategory;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;

import Reika.DragonAPI.Extras.ThrottleableEffectRenderer;
import Reika.DragonAPI.Instantiable.Event.Client.ChunkWorldRenderEvent;
import Reika.DragonAPI.Instantiable.Rendering.ParticleEngine;
import Reika.DragonAPI.Libraries.ReikaRegistryHelper;
import Reika.DragonAPI.Libraries.Rendering.ReikaColorAPI;
import Reika.Satisforestry.Blocks.BlockPowerSlug.TilePowerSlug;
import Reika.Satisforestry.Entity.EntityEliteStinger;
import Reika.Satisforestry.Entity.EntitySpitter;
import Reika.Satisforestry.Entity.EntitySpitter.SpitterType;
import Reika.Satisforestry.Entity.EntitySpitterFireball;
import Reika.Satisforestry.Miner.TileFrackingPressurizer;
import Reika.Satisforestry.Miner.TileNodeHarvester;
import Reika.Satisforestry.Registry.SFBlocks;
import Reika.Satisforestry.Registry.SFEntities;
import Reika.Satisforestry.Registry.SFShaders;
import Reika.Satisforestry.Render.DecorationRenderer;
import Reika.Satisforestry.Render.FrackingNodeAuxRenderer;
import Reika.Satisforestry.Render.FrackingNodeRenderer;
import Reika.Satisforestry.Render.PinkGrassRenderer;
import Reika.Satisforestry.Render.PowerSlugItemRenderer;
import Reika.Satisforestry.Render.PowerSlugRenderer;
import Reika.Satisforestry.Render.RedBambooRenderer;
import Reika.Satisforestry.Render.RenderCullingSystem;
import Reika.Satisforestry.Render.RenderEliteStinger;
import Reika.Satisforestry.Render.RenderFlyingManta;
import Reika.Satisforestry.Render.RenderLizardDoggo;
import Reika.Satisforestry.Render.RenderSpitter;
import Reika.Satisforestry.Render.RenderSpitterFire;
import Reika.Satisforestry.Render.ResourceNodeRenderer;
import Reika.Satisforestry.Render.SFFrackerItemRenderer;
import Reika.Satisforestry.Render.SFFrackerRenderer;
import Reika.Satisforestry.Render.SFMinerItemRenderer;
import Reika.Satisforestry.Render.SFMinerRenderer;
import Reika.Satisforestry.Render.SpitterFireParticle;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class SFClient extends SFCommon {

	public static RedBambooRenderer bamboo;
	private static PinkGrassRenderer grass;
	private static DecorationRenderer deco;
	public static ResourceNodeRenderer resource;
	public static FrackingNodeRenderer fracking;
	public static FrackingNodeAuxRenderer frackingAux;
	//public static PowerSlugRenderer slug;

	public static SoundCategory sfCategory;

	private static ParticleEngine SFParticleEngine;

	@Override
	public void loadMusicEngine() {
		Satisforestry.registerEventHandler(SFMusic.instance);
	}

	@Override
	public void registerSounds() {
		sounds.register();
		sfCategory = ReikaRegistryHelper.addSoundCategory("SF_MUSIC");
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

		frackingRender = RenderingRegistry.getNextAvailableRenderId();
		fracking = new FrackingNodeRenderer(frackingRender);
		RenderingRegistry.registerBlockHandler(frackingRender, fracking);

		frackingAuxRender = RenderingRegistry.getNextAvailableRenderId();
		frackingAux = new FrackingNodeAuxRenderer(frackingAuxRender);
		RenderingRegistry.registerBlockHandler(frackingAuxRender, frackingAux);
		/*
		slugRender = RenderingRegistry.getNextAvailableRenderId();
		slug = new PowerSlugRenderer(slugRender);
		RenderingRegistry.registerBlockHandler(slugRender, slug);*/

		RenderingRegistry.registerEntityRenderingHandler(SFEntities.ELITESTINGER.getObjectClass(), new RenderEliteStinger());
		RenderingRegistry.registerEntityRenderingHandler(SFEntities.MANTA.getObjectClass(), new RenderFlyingManta());
		RenderingRegistry.registerEntityRenderingHandler(SFEntities.SPITTER.getObjectClass(), new RenderSpitter());
		RenderingRegistry.registerEntityRenderingHandler(SFEntities.DOGGO.getObjectClass(), RenderLizardDoggo.instance);

		RenderingRegistry.registerEntityRenderingHandler(SFEntities.SPITTERFIRE.getObjectClass(), RenderSpitterFire.instance);
		RenderingRegistry.registerEntityRenderingHandler(SFEntities.SPITTERSPLITFIRE.getObjectClass(), RenderSpitterFire.instance);

		ClientRegistry.bindTileEntitySpecialRenderer(TileNodeHarvester.class, new SFMinerRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileFrackingPressurizer.class, new SFFrackerRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TilePowerSlug.class, new PowerSlugRenderer());
		MinecraftForgeClient.registerItemRenderer(SFBlocks.HARVESTER.getItem(), new SFMinerItemRenderer());
		MinecraftForgeClient.registerItemRenderer(SFBlocks.FRACKER.getItem(), new SFFrackerItemRenderer());
		MinecraftForgeClient.registerItemRenderer(SFBlocks.SLUG.getItem(), new PowerSlugItemRenderer());

		SFShaders.registerAll();

		SFParticleEngine = new ParticleEngine() {

			@Override
			protected void registerClasses() {
				ThrottleableEffectRenderer.getRegisteredInstance().registerDelegateRenderer(SpitterFireParticle.class, this);
			}
		};

		SFParticleEngine.register();
	}

	// Override any other methods that need to be handled differently client side.

	@Override
	public World getClientWorld()
	{
		return FMLClientHandler.instance().getClient().theWorld;
	}

	@Override
	public void registerRenderCullingSystem() {
		ChunkWorldRenderEvent.addHandler(RenderCullingSystem.instance);
	}

	@Override
	public void activateDamageShader(Entity hitter) {
		int r1 = 0;
		int g1 = 0;
		int b1 = 0;
		int a1 = 127;
		int r2 = r1;
		int g2 = g1;
		int b2 = b1;
		int a2 = a1;
		float f = 1;
		float nX = 1;
		float nY = 1;
		float add = 1;
		float mult = 0;
		float rf = 0.05F;
		float min = 0;
		if (hitter instanceof EntityEliteStinger) {
			r1 = 192;
			g1 = 255;
			b1 = 48;
			a1 = 255;
			r2 = r1;
			g2 = g1;
			b2 = b1;
			a2 = a1;
		}
		if (hitter instanceof EntitySpitter) {
			mult = 1;
			add = 0;
			rf = 0.025F;
		}
		if (hitter instanceof EntitySpitterFireball) {
			SpitterType s = ((EntitySpitterFireball)hitter).getSpitterType();
			r1 = ReikaColorAPI.getRed(s.coreColor);
			g1 = ReikaColorAPI.getGreen(s.coreColor);
			b1 = ReikaColorAPI.getBlue(s.coreColor);
			r2 = ReikaColorAPI.getRed(s.edgeColor);
			g2 = ReikaColorAPI.getGreen(s.edgeColor);
			b2 = ReikaColorAPI.getBlue(s.edgeColor);
			a1 = 255;
			a2 = a1;
			f = 0.33F;
			nX = 0.15F;
			nY = 0.075F;
			add = 0;
			mult = 1;
			rf = 0.01F;
			min = 0.3F;
		}
		SFShaders.MOBDAMAGE.getShader().setField("red1", r1);
		SFShaders.MOBDAMAGE.getShader().setField("green1", g1);
		SFShaders.MOBDAMAGE.getShader().setField("blue1", b1);
		SFShaders.MOBDAMAGE.getShader().setField("alpha1", a1);
		SFShaders.MOBDAMAGE.getShader().setField("red2", r2);
		SFShaders.MOBDAMAGE.getShader().setField("green2", g2);
		SFShaders.MOBDAMAGE.getShader().setField("blue2", b2);
		SFShaders.MOBDAMAGE.getShader().setField("alpha2", a2);
		SFShaders.MOBDAMAGE.getShader().setField("fadeFactor", f);
		SFShaders.MOBDAMAGE.getShader().setField("noiseScaleX", nX);
		SFShaders.MOBDAMAGE.getShader().setField("noiseScaleY", nY);
		SFShaders.MOBDAMAGE.getShader().setField("additiveScale", add);
		SFShaders.MOBDAMAGE.getShader().setField("multiplyScale", mult);
		SFShaders.MOBDAMAGE.getShader().setField("minimumEffect", min);
		SFShaders.MOBDAMAGE.setIntensity(1);
		SFShaders.MOBDAMAGE.lingerTime = 20;
		SFShaders.MOBDAMAGE.rampDownAmount = rf;
		SFShaders.MOBDAMAGE.rampDownFactor = 1F;
	}

}
