/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.Satisforestry.Registry;

import java.net.URL;

import net.minecraft.client.audio.SoundCategory;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.DragonAPI.Interfaces.Registry.CustomDistanceSound;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.Satisforestry.Satisforestry;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public enum SFSounds implements CustomDistanceSound {

	MANTA1("manta1"),
	MANTA2("manta2"),
	MANTA3("manta3"),
	MANTA4("manta4"),
	MANTAFLY("mantafly2"),
	DOGGOPANT("doggopant"),
	DOGGOHURT("doggohurt"),
	DOGGOHURT2("doggohurt2"),
	DOGGO1("doggo1"),
	DOGGO2("doggo2"),
	DOGGO3("doggo3"),
	DOGGO4("doggo4"),
	DOGGO5("doggo5"),
	DOGGO6("doggo6"),
	DOGGOSING("doggosing3"),
	DOGGOSNEEZE1("doggosneeze1"),
	DOGGOSNEEZE2("doggosneeze2"),
	DRILLLOCK("minerlock2"),
	DRILLSPINUP("drillspinup"),
	DRILLRUN("drillrun"),
	DRILLSPINDOWN("drillspindown"),
	SLUG("slugring"),
	SPITTER1("spitter2"),
	SPITTER2("spitter3"),
	SPITTERHURT("spitterhurt"),
	SPITTERBALLHIT("spitterball"),
	STINGERGAS("stingergas2"),
	STINGERJUMP1("stingerjump3"),
	STINGERJUMP2("stingerjump4"),
	;

	public static final String PREFIX = "Reika/Satisforestry/";
	public static final String SOUND_FOLDER = "Sounds/";
	private static final String SOUND_PREFIX = "Reika.Satisforestry.Sounds.";
	private static final String SOUND_DIR = "Sounds/";
	private static final String SOUND_EXT = ".ogg";

	private final String path;
	private final String name;

	private SFSounds(String n) {
		name = n;
		path = PREFIX+SOUND_FOLDER+name+SOUND_EXT;
	}

	public void playSound(Entity e) {
		this.playSound(e, 1, 1);
	}

	public void playSound(Entity e, float vol, float pitch) {
		this.playSound(e.worldObj, e.posX, e.posY, e.posZ, vol, pitch);
	}

	public void playSound(World world, double x, double y, double z, float vol, float pitch) {
		if (FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER)
			return;
		ReikaSoundHelper.playSound(this, world, x, y, z, vol/* *this.getModulatedVolume()*/, pitch);
	}

	public void playSound(World world, double x, double y, double z, float vol, float pitch, boolean attenuate) {
		ReikaSoundHelper.playSound(this, world, x, y, z, vol/* *this.getModulatedVolume()*/, pitch, attenuate);
	}

	public void playSoundAtBlock(World world, int x, int y, int z, float vol, float pitch) {
		this.playSound(world, x+0.5, y+0.5, z+0.5, vol, pitch);
	}

	public void playSoundAtBlock(World world, int x, int y, int z) {
		this.playSound(world, x+0.5, y+0.5, z+0.5, 1, 1);
	}

	public void playSoundAtBlock(TileEntity te) {
		this.playSoundAtBlock(te.worldObj, te.xCoord, te.yCoord, te.zCoord);
	}

	public void playSoundAtBlock(TileEntity te, float vol, float pitch) {
		this.playSoundAtBlock(te.worldObj, te.xCoord, te.yCoord, te.zCoord, vol, pitch);
	}

	public void playSoundAtBlock(WorldLocation loc) {
		this.playSoundAtBlock(loc.getWorld(), loc.xCoord, loc.yCoord, loc.zCoord);
	}

	public void playSoundNoAttenuation(World world, double x, double y, double z, float vol, float pitch, int broadcast) {
		ReikaPacketHelper.sendSoundPacket(this, world, x, y, z, vol, pitch, false, broadcast);
	}

	public String getName() {
		return this.name();
	}

	public String getPath() {
		return path;
	}

	public URL getURL() {
		return Satisforestry.class.getResource(SOUND_DIR+name+SOUND_EXT);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public SoundCategory getCategory() {
		return SoundCategory.MOBS;
	}

	@Override
	public boolean canOverlap() {
		return true;
	}

	@Override
	public boolean attenuate() {
		switch(this) {
			case MANTA1:
			case MANTA2:
			case MANTA3:
			case MANTA4:
				return false;
			default:
				return true;
		}
	}

	@Override
	public float getModulatedVolume() {
		return 1;
	}

	@Override
	public boolean preload() {
		return false;
	}

	@Override
	public float getAudibleDistance() {
		switch(this) {
			case SLUG:
				return 6;
			default:
				return -1;
		}
	}
}
