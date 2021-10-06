package Reika.Satisforestry.Entity;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
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
		this.spawnLifeParticle();
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
			ReikaPacketHelper.sendDataPacketWithRadius(Satisforestry.packetChannel, SFPackets.SPITTERFIREHIT.ordinal(), this, 32, shootingEntity.getEntityId());
			this.setDead();
		}
	}

	@Override
	public final boolean canRenderOnFire() {
		return false;
	}

	protected void spawnLifeParticle() {
		float s = (float)ReikaRandomHelper.getRandomBetween(type.isAlpha() ? 1 : 0.8, type.isAlpha() ? 1.3 : 1.1);
		int l = ReikaRandomHelper.getRandomBetween(4, 9);
		SpitterFireParticle fx = new SpitterFireParticle(worldObj, posX, posY, posZ, type);
		fx.setScale(s).setLife(l).setRapidExpand();
		Minecraft.getMinecraft().effectRenderer.addEffect(fx);
	}

	@SideOnly(Side.CLIENT)
	public final void doHitFX() {
		for (int i = 0; i < 6; i++) {
			double v0 = ReikaRandomHelper.getRandomBetween(0.0625, 0.125);
			float s = (float)ReikaRandomHelper.getRandomBetween(1, 1.5);
			if (type.isAlpha())
				s *= 1.2;
			int l = ReikaRandomHelper.getRandomBetween(8, 15);
			double[] v = ReikaPhysicsHelper.polarToCartesian(v0, 90-rand.nextDouble()*30, rand.nextDouble());
			SpitterFireParticle fx = new SpitterFireParticle(worldObj, posX, posY, posZ, v[0], v[1], v[2], type);
			fx.setGravity(0.125F).setScale(s).setLife(2000).setColliding(Double.NaN, l);
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
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setFloat("damage", damageAmount);
		tag.setInteger("type", type.ordinal());
	}

	@Override
	public void writeSpawnData(ByteBuf buf) {
		buf.writeInt(type.ordinal());
	}

	@Override
	public void readSpawnData(ByteBuf buf) {
		type = SpitterType.list[buf.readInt()];
	}

	public int getRenderColor() {
		return type.coreColor;
	}

}
