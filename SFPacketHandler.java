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

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import Reika.DragonAPI.Auxiliary.PacketTypes;
import Reika.DragonAPI.Interfaces.PacketHandler;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper.DataPacket;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper.PacketObj;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.Satisforestry.Blocks.BlockCrashSite.TileCrashSite;
import Reika.Satisforestry.Entity.EntitySpitter;
import Reika.Satisforestry.Entity.EntitySpitterFireball;
import Reika.Satisforestry.Registry.SFSounds;

public class SFPacketHandler implements PacketHandler {

	protected SFPackets pack;

	private static final Random rand = new Random();


	public void handleData(PacketObj packet, World world, EntityPlayer ep) {
		DataInputStream inputStream = packet.getDataIn();
		int control = Integer.MIN_VALUE;
		int len;
		int[] data = new int[0];
		long longdata = 0;
		float floatdata = 0;
		int x = 0;
		int y = 0;
		int z = 0;
		double dx = 0;
		double dy = 0;
		double dz = 0;
		NBTTagCompound NBT = null;
		String stringdata = null;
		UUID id = null;
		//System.out.print(packet.length);
		try {
			//ReikaJavaLibrary.pConsole(inputStream.readInt()+":"+inputStream.readInt()+":"+inputStream.readInt()+":"+inputStream.readInt()+":"+inputStream.readInt()+":"+inputStream.readInt()+":"+inputStream.readInt());
			PacketTypes packetType = packet.getType();
			switch(packetType) {
				case FULLSOUND:
					break;
				case SOUND:
					return;
				case STRING:
					stringdata = packet.readString();
					control = inputStream.readInt();
					pack = SFPackets.list[control];
					break;
				case DATA:
					control = inputStream.readInt();
					pack = SFPackets.list[control];
					len = pack.numInts;
					data = new int[len];
					for (int i = 0; i < len; i++)
						data[i] = inputStream.readInt();
					break;
				case POS:
					control = inputStream.readInt();
					dx = inputStream.readDouble();
					dy = inputStream.readDouble();
					dz = inputStream.readDouble();
					len = 1;
					data = new int[len];
					for (int i = 0; i < len; i++)
						data[i] = inputStream.readInt();
					break;
				case UPDATE:
					control = inputStream.readInt();
					pack = SFPackets.list[control];
					break;
				case FLOAT:
					control = inputStream.readInt();
					pack = SFPackets.list[control];
					floatdata = inputStream.readFloat();
					break;
				case SYNC:
					String name = packet.readString();
					x = inputStream.readInt();
					y = inputStream.readInt();
					z = inputStream.readInt();
					ReikaPacketHelper.updateTileEntityData(world, x, y, z, name, inputStream);
					return;
				case TANK:
					String tank = packet.readString();
					x = inputStream.readInt();
					y = inputStream.readInt();
					z = inputStream.readInt();
					int level = inputStream.readInt();
					ReikaPacketHelper.updateTileEntityTankData(world, x, y, z, tank, level);
					return;
				case RAW:
					control = inputStream.readInt();
					pack = SFPackets.list[control];
					len = 1;
					data = new int[len];
					for (int i = 0; i < len; i++)
						data[i] = inputStream.readInt();
					break;
				case PREFIXED:
					control = inputStream.readInt();
					pack = SFPackets.list[control];
					len = inputStream.readInt();
					data = new int[len];
					for (int i = 0; i < len; i++)
						data[i] = inputStream.readInt();
					break;
				case NBT:
					control = inputStream.readInt();
					pack = SFPackets.list[control];
					NBT = ((DataPacket)packet).asNBT();
					break;
				case STRINGINT:
				case STRINGINTLOC:
					stringdata = packet.readString();
					control = inputStream.readInt();
					pack = SFPackets.list[control];
					data = new int[1];
					for (int i = 0; i < data.length; i++)
						data[i] = inputStream.readInt();
					break;
				case UUID:
					control = inputStream.readInt();
					pack = SFPackets.list[control];
					long l1 = inputStream.readLong(); //most
					long l2 = inputStream.readLong(); //least
					id = new UUID(l1, l2);
					break;
			}
			if (packetType.hasCoordinates()) {
				x = inputStream.readInt();
				y = inputStream.readInt();
				z = inputStream.readInt();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
		try {
			switch (pack) {
				case MOBDAMAGE:
					if (world.isRemote) {
						Entity e = world.getEntityByID(data[0]);
						Satisforestry.proxy.activateDamageShader(e);
					}
					break;
				case SPITTERFIREHIT:
					if (world.isRemote) {
						Entity e = world.getEntityByID(data[0]);
						if (e instanceof EntitySpitterFireball) {
							MovingObjectPosition mov = new MovingObjectPosition(data[2], data[3], data[4], data[5], Vec3.createVectorHelper(0, 0, 0));
							mov.typeOfHit = data[1] > 0 ? MovingObjectType.BLOCK : MovingObjectType.ENTITY;
							mov.entityHit = data[6] == Integer.MIN_VALUE ? null : world.getEntityByID(data[6]);
							((EntitySpitterFireball)e).doHitFX(mov);
						}
					}
					break;
				case SPITTERBLAST:
					if (world.isRemote) {
						Entity e = world.getEntityByID(data[0]);
						if (e instanceof EntitySpitter) {
							((EntitySpitter)e).doBlastFX();
						}
					}
					break;
				case CRASHUNLOCK:
					if (!world.isRemote) {
						boolean ret = ((TileCrashSite)world.getTileEntity(x, y, z)).tryOpen((EntityPlayerMP)ep);
						ReikaPacketHelper.sendDataPacket(Satisforestry.packetChannel, SFPackets.CRASHUNLOCKRETURN.ordinal(), (EntityPlayerMP)ep, ret ? 1 : 0);
					}
					break;
				case CRASHUNLOCKRETURN:
					if (world.isRemote) {
						TileCrashSite.reactToLockGuiStatus(data[0] > 0);
					}
					break;
				case ALTRECIPEUNLOCK:
					if (world.isRemote) {
						ReikaSoundHelper.playClientSound(SFSounds.ALTRECIPE, ep, 1, 1);
					}
					break;
			}
		}
		catch (NullPointerException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static enum SFPackets {
		MOBDAMAGE(1),
		SPITTERFIREHIT(7),
		SPITTERBLAST(1),
		CRASHUNLOCK(0),
		CRASHUNLOCKRETURN(1),
		ALTRECIPEUNLOCK(0),
		;

		public final int numInts;

		public static final SFPackets[] list = values();

		private SFPackets(int n) {
			numInts = n;
		}
	}
}
