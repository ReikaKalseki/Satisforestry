package Reika.Satisforestry.Entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaPhysicsHelper;
import Reika.Satisforestry.SFPacketHandler.SFPackets;
import Reika.Satisforestry.Satisforestry;
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

	public EntitySpitterFireball(World world, EntitySpitter e, double vx, double vy, double vz, float dmg) {
		super(world, e, vx, vy, vz);
		damageAmount = dmg;
		type = e.getSpitterType();
	}

	public EntitySpitterFireball(World world) {
		super(world);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (worldObj.isRemote)
			;//this.spawnLifeParticle();
	}

	@Override
	protected final void onImpact(MovingObjectPosition mov) {
		if (!worldObj.isRemote) {
			if (mov.entityHit != null) {
				if (!mov.entityHit.isImmuneToFire() && mov.entityHit.attackEntityFrom(DamageSource.causeFireballDamage(this, shootingEntity), damageAmount)) {
					mov.entityHit.setFire(5);
				}
			}
			SFSounds.SPITTERBALLHIT.playSound(this);
			if (shootingEntity != null) {
				int flag = 0;
				if (mov != null && mov.typeOfHit == MovingObjectType.BLOCK && (mov.sideHit >= 0 && mov.sideHit <= 1)) {
					flag = 1;
				}
				ReikaPacketHelper.sendDataPacketWithRadius(Satisforestry.packetChannel, SFPackets.SPITTERFIREHIT.ordinal(), this, 32, this.getEntityId(), flag);
			}
			this.setDead();
		}
	}

	@Override
	public final boolean canRenderOnFire() {
		return false;
	}

	@SideOnly(Side.CLIENT)
	public EntityFX spawnLifeParticle(double x, double y, double z) {
		float s = (float)ReikaRandomHelper.getRandomBetween(type.isAlpha() ? 1.5 : 1, type.isAlpha() ? 2 : 1.5);
		int l = ReikaRandomHelper.getRandomBetween(7, 15);
		SpitterFireParticle fx = new SpitterFireParticle(worldObj, x, y, z, this.getSpitterType());
		fx.setScale(s).setLife(l).setRapidExpand();
		return fx;
	}

	@SideOnly(Side.CLIENT)
	public final void doHitFX(boolean blocksurf) {
		for (int i = 0; i < 6; i++) {
			double v0 = ReikaRandomHelper.getRandomBetween(0.125, 0.25);
			float s = (float)ReikaRandomHelper.getRandomBetween(1.2, 1.5);
			if (type.isAlpha())
				s *= 1.5;
			int l = ReikaRandomHelper.getRandomBetween(8, 15);
			double[] v = null;
			if (blocksurf) {
				double ang = Math.toDegrees(Math.atan2(motionZ, motionX));
				v = ReikaPhysicsHelper.polarToCartesian(v0, 10+rand.nextDouble()*40, ReikaRandomHelper.getRandomPlusMinus(ang, 15));
				//v = new double[3];
				//v[0] = ReikaRandomHelper.getRandomPlusMinus(motionX*0.7, 0.1);
				//v[1] = ReikaRandomHelper.getRandomBetween(0.15, 0.35);
				//v[2] = ReikaRandomHelper.getRandomPlusMinus(motionZ*0.7, 0.1);
			}
			else {
				v = ReikaPhysicsHelper.polarToCartesian(v0, 90-rand.nextDouble()*30, rand.nextDouble()*360);
			}
			SpitterFireParticle fx = new SpitterFireParticle(worldObj, posX-motionX, posY-motionY, posZ-motionZ, v[0], Math.max(0, v[1]*0.67), v[2], type);
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
		if (worldObj != null && tag.hasKey("entity"))
			shootingEntity = (EntityLivingBase)worldObj.getEntityByID(tag.getInteger("entity"));
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setFloat("damage", damageAmount);
		tag.setInteger("type", type.ordinal());
		if (shootingEntity != null)
			tag.setInteger("entity", shootingEntity.getEntityId());
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
