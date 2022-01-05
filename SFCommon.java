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

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.IO.SoundLoader;
import Reika.Satisforestry.Registry.SFSounds;

public class SFCommon {

	public static int bambooRender;
	public static int grassRender;
	public static int decoRender;
	public static int resourceRender;
	//public static int slugRender;

	protected static final SoundLoader sounds = new SoundLoader(SFSounds.class);

	/**
	 * Client side only register stuff...
	 */
	public void registerRenderers()
	{
		//unused server side. -- see ClientProxy for implementation
	}

	public void addArmorRenders() {}

	public World getClientWorld() {
		return null;
	}

	public void registerRenderCullingSystem() {

	}

	public void registerRenderInformation() {

	}

	public void registerSounds() {

	}

	public void loadMusicEngine() {

	}

	public void activateDamageShader(Entity hitter) {

	}

}
