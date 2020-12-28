package Reika.Satisforestry.Entity;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import Reika.DragonAPI.Extras.IconPrefabs;
import Reika.DragonAPI.Instantiable.Effects.EntityBlurFX;
import Reika.DragonAPI.Libraries.ReikaAABBHelper;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaVectorHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityEliteStinger extends EntitySpider {

	private static final int POISON_MAX_RATE = 150;

	private int poisonGasCooldown;

	public EntityEliteStinger(World world) {
		super(world);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(18, 0F);
		dataWatcher.addObject(19, 0F);
		dataWatcher.addObject(20, 0);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();


		if (poisonGasCooldown > 0)
			poisonGasCooldown--;
		else
			rotationPitch = -15;

		if (!worldObj.isRemote) {
			if (entityToAttack != null && poisonGasCooldown == 0 && rand.nextInt(150) == 0) {
				this.generatePoisonCloud();
			}

			Vec3 vec = this.getLookVec();
			dataWatcher.updateObject(18, (float)vec.xCoord);
			dataWatcher.updateObject(19, (float)vec.zCoord);
			dataWatcher.updateObject(20, poisonGasCooldown);

			if (poisonGasCooldown > 0) {
				rotationPitch = 0;
				rotationYawHead = rotationYaw;
				double r = 6*(1F-poisonGasCooldown/(float)POISON_MAX_RATE);
				AxisAlignedBB box = ReikaAABBHelper.getEntityCenteredAABB(this, r);
				List<EntityPlayer> li = worldObj.getEntitiesWithinAABB(EntityPlayer.class, box);
				for (EntityPlayer ep : li) {
					if (!ep.isPotionActive(Potion.poison))
						ep.addPotionEffect(new PotionEffect(Potion.poison.id, 40, 0));
				}
				ReikaSoundHelper.playSoundAtEntity(worldObj, this, "game.player.hurt", 0.5F, 0.5F);
			}
		}

		if (worldObj.isRemote) {
			poisonGasCooldown = dataWatcher.getWatchableObjectInt(20);
			if (poisonGasCooldown > 0) {
				this.doCloudFX();
				rotationPitch = 0;
				rotationYawHead = rotationYaw;
			}
			this.doParticleTrail();
		}
	}

	@Override
	protected boolean isMovementBlocked() {
		return super.isMovementBlocked() || poisonGasCooldown > 0;
	}

	@Override
	protected boolean isMovementCeased() {
		return super.isMovementCeased() || poisonGasCooldown > 0;
	}

	@SideOnly(Side.CLIENT)
	private void doParticleTrail() {
		Vec3 vec = Vec3.createVectorHelper(dataWatcher.getWatchableObjectFloat(18), 0, dataWatcher.getWatchableObjectFloat(19));
		Vec3 vec2 = ReikaVectorHelper.rotateVector(vec, 0, 90, 0);
		double l1 = width/2*ReikaRandomHelper.getRandomBetween(0.7, 0.8);
		double l2 = width/5;

		for (int i = 0; i < 6; i++) {
			double x = posX+vec.xCoord*l1+vec2.xCoord*l2;
			double z = posZ+vec.zCoord*l1+vec2.zCoord*l2;
			double y = posY+this.getEyeHeight()-0.125+ReikaRandomHelper.getRandomPlusMinus(0, 0.03125);
			EntityBlurFX fx = new EntityBlurFX(worldObj, x, y, z, IconPrefabs.FADE.getIcon());
			fx.setGravity(0.03125F).setColor(0xD9FF00).setScale(0.4F).setLife(18).setRapidExpand();
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
			l2 = -l2;
		}
	}

	private void generatePoisonCloud() {
		poisonGasCooldown = POISON_MAX_RATE;
	}

	@SideOnly(Side.CLIENT)
	public void doCloudFX() {
		for (int i = 0; i < 1; i++) {
			double dx = ReikaRandomHelper.getRandomPlusMinus(0, 0.5);
			double dz = ReikaRandomHelper.getRandomPlusMinus(0, 0.5);
			double dy = ReikaRandomHelper.getRandomPlusMinus(0, 0.125);
			double v = ReikaRandomHelper.getRandomBetween(0.1, 0.16);
			double vy = ReikaRandomHelper.getRandomBetween(0, v/2);
			EntityBlurFX fx = new EntityBlurFX(worldObj, posX+dx, posY+dy, posZ+dz, dx*v, vy, dz*v, IconPrefabs.FADE_GENTLE.getIcon());
			int l = ReikaRandomHelper.getRandomBetween(100, 1200);
			float s = (float)ReikaRandomHelper.getRandomBetween(5F, 10F);
			fx.setColor(0xA4DB00).setScale(s).setRapidExpand().setAlphaFading().setLife(l).setColliding();
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30); //15 hearts
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(1.2);
	}

	@Override
	protected Entity findPlayerToAttack() {
		return worldObj.getClosestVulnerablePlayerToEntity(this, 24); //no light limit, 24 range instead of 16
	}

	@Override
	public boolean attackEntityAsMob(Entity e) {
		boolean flag = super.attackEntityAsMob(e);
		if (flag && e instanceof EntityLivingBase) {
			((EntityLivingBase)e).addPotionEffect(new PotionEffect(Potion.poison.id, 20, 1));
		}
		return flag;
	}

	@Override
	protected void dropFewItems(boolean recentHit, int looting) {
		super.dropFewItems(recentHit, looting*2+2);
		this.dropItem(Items.beef, 3);
		this.dropItem(Items.spider_eye, 1);
	}

	@Override
	public IEntityLivingData onSpawnWithEgg(IEntityLivingData e) {
		return e;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound NBT) {
		super.readEntityFromNBT(NBT);

		poisonGasCooldown = NBT.getInteger("gastime");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound NBT) {
		super.writeEntityToNBT(NBT);

		NBT.setInteger("gastime", poisonGasCooldown);
	}

}
