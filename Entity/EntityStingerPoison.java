package Reika.Satisforestry.Entity;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import Reika.DragonAPI.Base.InertEntity;
import Reika.DragonAPI.Extras.IconPrefabs;
import Reika.DragonAPI.Instantiable.Effects.EntityBlurFX;
import Reika.DragonAPI.Interfaces.Entity.DestroyOnUnload;
import Reika.DragonAPI.Libraries.ReikaAABBHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.ReactorCraft.API.RadiationHandler;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;


public class EntityStingerPoison extends InertEntity implements IEntityAdditionalSpawnData, DestroyOnUnload {

	private int lifespan;

	public EntityStingerPoison(EntityEliteStinger e, int life) {
		super(e.worldObj);
		lifespan = life;
		this.setLocationAndAngles(e.posX, e.posY+e.getEyeHeight()/2, e.posZ, 0, 0);
	}

	public EntityStingerPoison(World world) {
		super(world);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (worldObj.isRemote) {
			if (lifespan-ticksExisted >= 40)
				this.spawnLifeParticle();
		}
		else {
			if (ticksExisted > lifespan) {
				this.setDead();
			}
			else {
				double f = 0.33+0.67*ticksExisted/lifespan;
				AxisAlignedBB box = ReikaAABBHelper.getEntityCenteredAABB(this, f*9);
				List<EntityLivingBase> li = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, box);
				for (EntityLivingBase e : li) {
					if (e instanceof EntityEliteStinger)
						continue;
					if (RadiationHandler.hasHazmatSuit(e))
						continue;
					e.attackEntityFrom(DamageSource.magic, 2); //poison uses magic
					if (e.isPotionActive(Potion.poison))
						continue;
					e.addPotionEffect(new PotionEffect(Potion.poison.id, 40, 1));
				}
			}
		}
	}

	@Override
	public final boolean canRenderOnFire() {
		return false;
	}

	@SideOnly(Side.CLIENT)
	private void spawnLifeParticle() {
		for (int i = 0; i < 3; i++) {
			double dx = ReikaRandomHelper.getRandomPlusMinus(0, 2);
			double dz = ReikaRandomHelper.getRandomPlusMinus(0, 2);
			double dy = ReikaRandomHelper.getRandomPlusMinus(0, 0.125);
			double v = ReikaRandomHelper.getRandomBetween(0.05, 0.12);
			double vy = ReikaRandomHelper.getRandomBetween(0, v);
			EntityBlurFX fx = new EntityBlurFX(worldObj, posX+dx, posY+dy, posZ+dz, dx*v/2, vy, dz*v/2, IconPrefabs.FADE_GENTLE.getIcon());
			int l = ReikaRandomHelper.getRandomBetween(Math.min(100, lifespan-ticksExisted-10), Math.max(20, lifespan-ticksExisted));
			float s = (float)ReikaRandomHelper.getRandomBetween(8F, 15F);
			fx.setColor(0xA4DB00).setScale(s).setRapidExpand().setAlphaFading().setLife(l).setColliding();
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag) {
		lifespan = tag.getInteger("life");
		isDead = tag.getBoolean("dead");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		tag.setInteger("life", lifespan);
		tag.setBoolean("dead", isDead);
	}

	@Override
	public void writeSpawnData(ByteBuf buf) {
		buf.writeInt(lifespan);
	}

	@Override
	public void readSpawnData(ByteBuf buf) {
		lifespan = buf.readInt();
	}

	@Override
	public void destroy() {
		this.setDead();
	}

	@Override
	protected void entityInit() {

	}

}
