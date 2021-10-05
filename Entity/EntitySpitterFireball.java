package Reika.Satisforestry.Entity;

import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;
import Reika.Satisforestry.SFPacketHandler.SFPackets;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Entity.EntitySpitter.SpitterType;
import Reika.Satisforestry.Registry.SFSounds;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
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
	protected final void onImpact(MovingObjectPosition mov) {
		if (!worldObj.isRemote) {
			if (mov.entityHit != null) {
				if (!mov.entityHit.isImmuneToFire() && mov.entityHit.attackEntityFrom(DamageSource.causeFireballDamage(this, shootingEntity), damageAmount)) {
					mov.entityHit.setFire(5);
				}
			}
			SFSounds.SPITTERBALLHIT.playSound(this);
			ReikaPacketHelper.sendDataPacketWithRadius(Satisforestry.packetChannel, SFPackets.SPITTERFIREHIT.ordinal(), this, 32, type.ordinal());
			this.setDead();
		}
	}

	@Override
	public final boolean canRenderOnFire() {
		return false;-
	}

	public final float getDamage() {
		return damageAmount;
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

}
