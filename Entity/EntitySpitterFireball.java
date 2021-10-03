package Reika.Satisforestry.Entity;

import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;


public class EntitySpitterFireball extends EntitySmallFireball {

	private float damageAmount;

	public EntitySpitterFireball(World world, EntitySpitter e, double vx, double vy, double vz, float dmg) {
		super(world, e, vx, vy, vz);
		damageAmount = dmg;
	}

	@Override
	protected final void onImpact(MovingObjectPosition mov) {
		if (!worldObj.isRemote) {
			if (mov.entityHit != null) {
				if (!mov.entityHit.isImmuneToFire() && mov.entityHit.attackEntityFrom(DamageSource.causeFireballDamage(this, shootingEntity), damageAmount)) {
					mov.entityHit.setFire(5);
				}
			}
			this.setDead();
		}
	}

	public float getDamage() {
		return damageAmount;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);

		damageAmount = tag.getFloat("damage");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setFloat("damage", damageAmount);
	}

}
