package Reika.Satisforestry.Entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.CritterPet.API.TamedCritter;
import Reika.DragonAPI.Interfaces.Entity.TameHostile;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.MathSci.ReikaPhysicsHelper;
import Reika.Satisforestry.SFPacketHandler.SFPackets;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.SpitterDamage;
import Reika.Satisforestry.Entity.EntitySpitter.SpitterType;
import Reika.Satisforestry.Registry.SFSounds;
import Reika.Satisforestry.Render.SpitterFireParticle;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;


public class EntitySpitterFireball extends EntitySmallFireball implements IEntityAdditionalSpawnData {

	private SpitterType type;
	private float damageAmount;

	public EntitySpitterFireball(World world, EntitySpitter e, double vx, double vy, double vz, double sp, float dmg) {
		super(world, e, vx, vy, vz);
		Vec3 vec = e.getLookVec();
		type = e.getSpitterType();
		double d = type.isAlpha() ? 0.8 : 0.6;
		if (e.riddenByEntity != null)
			d *= 2.5;
		this.setLocationAndAngles(e.posX+vec.xCoord*d, e.posY+e.getEyeHeight()+vec.yCoord*d, e.posZ+vec.zCoord*d, 0, 0);
		damageAmount = dmg;
		double d3 = ReikaMathLibrary.py3d(vx, vy, vz);
		accelerationX = vx / d3 * 0.1D*sp;
		accelerationY = vy / d3 * 0.1D*sp;
		accelerationZ = vz / d3 * 0.1D*sp;
	}

	public EntitySpitterFireball(World world) {
		super(world);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (worldObj.isRemote)
			;//this.spawnLifeParticle();
		else {
			if (ticksExisted%3 == 0)
				SFSounds.SPITTERBALLAMBIENT.playSound(this, 0.5F, 1.1F);
		}
	}

	@Override
	protected final void onImpact(MovingObjectPosition mov) {
		if (!worldObj.isRemote) {
			if (shootingEntity != null && mov.entityHit instanceof EntityLivingBase && this.shouldHurt((EntityLivingBase)mov.entityHit)) {
				SpitterDamage.doDamage((EntitySpitter)shootingEntity, this, (EntityLivingBase)mov.entityHit, damageAmount);
			}
			SFSounds.SPITTERBALLHIT.playSound(this, 1, 1.2F+rand.nextFloat()*0.5F);
			int id = mov.entityHit != null ? mov.entityHit.getEntityId() : Integer.MIN_VALUE;
			ReikaPacketHelper.sendDataPacketWithRadius(Satisforestry.packetChannel, SFPackets.SPITTERFIREHIT.ordinal(), this, 32, this.getEntityId(), mov.typeOfHit == MovingObjectType.BLOCK ? 1 : 0, mov.blockX, mov.blockY, mov.blockZ, mov.sideHit, id);
			this.setDead();
		}
	}

	private boolean shouldHurt(EntityLivingBase e) {
		EntitySpitter spitter = (EntitySpitter)shootingEntity;
		if (spitter instanceof TamedCritter) {
			if (e instanceof TameHostile)
				return false;
			String n = ((TamedCritter)spitter).getMobOwner();
			if (n != null && n.equalsIgnoreCase(e.getCommandSenderName()))
				return false;
		}
		return true;
	}

	@Override
	public final boolean canRenderOnFire() {
		return false;
	}

	@SideOnly(Side.CLIENT)
	public EntityFX spawnLifeParticle(double x, double y, double z) {
		float s = (float)ReikaRandomHelper.getRandomBetween(type.isAlpha() ? 1.5 : 1, type.isAlpha() ? 2 : 1.5);
		int l = ReikaRandomHelper.getRandomBetween(7, 15);
		SpitterFireParticle fx = new SpitterFireParticle(worldObj, x, y, z, type);
		fx.setScale(s*3).setLife(l).setRapidExpand();
		return fx;
	}

	@SideOnly(Side.CLIENT)
	public final void doHitFX(MovingObjectPosition mov) {
		for (int i = 0; i < 6; i++) {
			double v0 = ReikaRandomHelper.getRandomBetween(0.125, 0.25);
			float s = (float)ReikaRandomHelper.getRandomBetween(2, 3.5);
			if (type.isAlpha())
				s *= 1.75;
			int l = ReikaRandomHelper.getRandomBetween(8, 15);
			double[] v = null;
			if (mov.typeOfHit == MovingObjectType.BLOCK && mov.sideHit >= 0 && mov.sideHit <= 1) {
				double ang = Math.toDegrees(Math.atan2(motionZ, motionX));
				v = ReikaPhysicsHelper.polarToCartesian(v0, 10+rand.nextDouble()*40, ReikaRandomHelper.getRandomPlusMinus(ang, 15));
				//v = new double[3];
				//v[0] = ReikaRandomHelper.getRandomPlusMinus(motionX*0.7, 0.1);
				//v[1] = ReikaRandomHelper.getRandomBetween(0.15, 0.35);
				//v[2] = ReikaRandomHelper.getRandomPlusMinus(motionZ*0.7, 0.1);
			}
			else {
				double da = mov.typeOfHit == MovingObjectType.ENTITY ? 90-rand.nextDouble()*60 : 90-rand.nextDouble()*30;
				v = ReikaPhysicsHelper.polarToCartesian(v0, da, rand.nextDouble()*360);
			}
			double px = mov.typeOfHit == MovingObjectType.ENTITY ? mov.entityHit.posX : mov.blockX;//posX-motionX
			double py = mov.typeOfHit == MovingObjectType.ENTITY ? mov.entityHit.posY : Math.max(posY, mov.blockY);//posY-motionY
			double pz = mov.typeOfHit == MovingObjectType.ENTITY ? mov.entityHit.posZ : mov.blockZ;//posZ-motionZ
			if (mov.typeOfHit == MovingObjectType.BLOCK && mov.sideHit >= 0) {
				switch(ForgeDirection.VALID_DIRECTIONS[mov.sideHit]) {
					case UP:
						py++;
						break;
					case EAST:
						px++;
						break;
					case SOUTH:
						pz++;
						break;
					default:
						break;
				}
			}
			SpitterFireParticle fx = new SpitterFireParticle(worldObj, px-motionX*0.25, py-motionY*0.25, pz-motionZ*0.25, v[0], Math.max(0, v[1]*0.67), v[2], type);
			fx.setGravity(0.25F).setScale(s).setLife(l+10).setColliding(Double.NaN, Integer.MIN_VALUE).setRapidExpand();
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}
	}

	public final float getDamage() {
		return damageAmount;
	}

	public final SpitterType getSpitterType() {
		return type;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);

		damageAmount = tag.getFloat("damage");
		type = SpitterType.list[tag.getInteger("type")];
		//if (worldObj != null && tag.hasKey("entity"))
		//	shootingEntity = (EntityLivingBase)worldObj.getEntityByID(tag.getInteger("entity"));
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setFloat("damage", damageAmount);
		tag.setInteger("type", type.ordinal());
		//if (shootingEntity != null)
		//	tag.setInteger("entity", shootingEntity.getEntityId());
	}

	@Override
	public void writeSpawnData(ByteBuf buf) {
		buf.writeInt(type.ordinal());
	}

	@Override
	public void readSpawnData(ByteBuf buf) {
		type = SpitterType.list[buf.readInt()];
	}

	public int getRenderColor(int pass) {
		return pass == 1 ? type.edgeColor : type.coreColor;
	}

}
