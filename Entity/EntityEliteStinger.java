package Reika.Satisforestry.Entity;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import Reika.DragonAPI.Extras.IconPrefabs;
import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.DragonAPI.Instantiable.Effects.EntityBlurFX;
import Reika.DragonAPI.Libraries.ReikaAABBHelper;
import Reika.DragonAPI.Libraries.ReikaEntityHelper.ClassEntitySelector;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.MathSci.ReikaVectorHelper;
import Reika.Satisforestry.SFPacketHandler.SFPackets;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Registry.SFEntities;
import Reika.Satisforestry.Registry.SFSounds;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityEliteStinger extends EntitySpider implements SpawnPointEntity {

	private static final int POISON_DURATION = 150;
	private static final int POISON_MAX_RATE = 500;
	private static final int JUMP_MAX_RATE = 60;//100;

	private WorldLocation spawnPoint;

	private int poisonGasTick;
	private int poisonGasCooldown;
	private int jumpCooldown;
	private boolean isLeaping;

	public EntityEliteStinger(World world) {
		super(world);
		this.setSize(1.6F, 1F);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(18, 0F);
		dataWatcher.addObject(19, 0F);
		dataWatcher.addObject(20, 0);
	}

	public void setSpawn(WorldLocation loc) {
		spawnPoint = loc;
	}

	public WorldLocation getSpawn() {
		return spawnPoint;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (poisonGasTick > 0) {
			poisonGasTick--;
		}
		else {
			rotationPitch = -15;
			if (poisonGasCooldown > 0)
				poisonGasCooldown--;
		}

		if (onGround) {
			if (jumpCooldown > 0)
				jumpCooldown--;
		}

		if (!worldObj.isRemote) {
			if (entityToAttack != null && poisonGasTick == 0 && poisonGasCooldown == 0 && rand.nextInt(150) == 0) {
				this.startPoisonCloud(ReikaRandomHelper.getRandomBetween(1.5, 2));
			}

			Vec3 vec = this.getLookVec();
			dataWatcher.updateObject(18, (float)vec.xCoord);
			dataWatcher.updateObject(19, (float)vec.zCoord);
			dataWatcher.updateObject(20, poisonGasTick);

			if (poisonGasTick > 0) {
				rotationPitch = 0;
				rotationYawHead = rotationYaw;
			}
		}

		if (worldObj.isRemote) {
			poisonGasTick = dataWatcher.getWatchableObjectInt(20);
			if (poisonGasTick > 0) {
				rotationPitch = 0;
				rotationYawHead = rotationYaw;
			}
			this.doParticleTrail();
		}
	}

	@Override
	public void onDeath(DamageSource src) {
		super.onDeath(src);

		if (!worldObj.isRemote)
			this.startPoisonCloud(2);
		SFSounds.STINGERGAS.playSound(this);
	}

	/*
	@Override
	protected void jump() {
		motionY = 0.5;

		float f = rotationYaw * 3.1416F / 180F;
		float v = 0.4F;
		motionX -= MathHelper.sin(f) * v;
		motionZ += MathHelper.cos(f) * v;

		isAirBorne = true;
		ForgeHooks.onLivingJump(this);
	}*/

	@Override
	protected void attackEntity(Entity e, float dist) {
		if (dist >= 6) {
			if (onGround && jumpCooldown <= 0) {
				this.jumpAt(e);
			}
		}
		else if (attackTime <= 0 && dist < 2.0F && e.boundingBox.maxY > boundingBox.minY && e.boundingBox.minY < boundingBox.maxY) {
			attackTime = 20;
			this.attackEntityAsMob(e);
		}
	}

	private void jumpAt(Entity e) {
		double dx = e.posX - posX;
		double dz = e.posZ - posZ;
		double dd = ReikaMathLibrary.py3d(dx, 0, dz);
		double vat = 1;//0.8;
		double vf = 2;//1;//0.75;//0.5;
		motionX = dx/dd * vf * vat + motionX * (1-vat);
		motionZ = dz/dd * vf * vat + motionZ * (1-vat);
		motionY = 0.375+dd/40;//0.4;
		//ReikaSoundHelper.playSoundAtEntity(worldObj, this, "mob.cat.hiss", 0.8F, 1.9F+rand.nextFloat()*0.1F);
		SFSounds s = rand.nextBoolean() ? SFSounds.STINGERJUMP1 : SFSounds.STINGERJUMP2;
		s.playSound(this);
		jumpCooldown = JUMP_MAX_RATE;
		isLeaping = true;
	}

	@Override
	protected void fall(float amt) {
		super.fall(amt);
		if (isLeaping) {
			AxisAlignedBB box = ReikaAABBHelper.getEntityCenteredAABB(this, 1).expand(3, 0, 3);
			List<EntityLivingBase> li = worldObj.getEntitiesWithinAABBExcludingEntity(this, box, new ClassEntitySelector(EntityLivingBase.class, false));
			for (EntityLivingBase e : li) {
				this.attackEntityAsMob(e);
			}
		}
		isLeaping = false;
	}

	@Override
	public boolean attackEntityFrom(DamageSource src, float amt) {
		if (src == DamageSource.fall || src == DamageSource.drown)
			return false;
		return super.attackEntityFrom(src, amt);
	}

	@Override
	public String getCommandSenderName() {
		return SFEntities.ELITESTINGER.entityName;
	}

	@Override
	protected boolean isMovementBlocked() {
		return super.isMovementBlocked() || poisonGasTick > 0;
	}

	@Override
	protected boolean isMovementCeased() {
		return super.isMovementCeased() || poisonGasTick > 0;
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

	private void startPoisonCloud(double lenFactor) {
		poisonGasTick = POISON_DURATION;
		poisonGasCooldown = POISON_MAX_RATE;
		//ReikaSoundHelper.playSoundAtEntity(worldObj, this, "mob.chicken.plop", 2F, 0.8F);
		//ReikaSoundHelper.playSoundAtEntity(worldObj, this, "mob.cat.hiss", 2, 0.5F);
		SFSounds.STINGERGAS.playSound(this);
		EntityStingerPoison e = new EntityStingerPoison(this, (int)(POISON_DURATION*lenFactor));
		worldObj.spawnEntityInWorld(e);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30); //15 hearts
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(1.2);
	}

	@Override
	protected Entity findPlayerToAttack() {
		//return worldObj.getClosestVulnerablePlayerToEntity(this, 24); //no light limit, 24 range instead of 16

		//The above plus bypass usual hooks and modifiers, plus range 27
		return getTarget(this, 27, 0.9);
	}

	/** This bypasses the usual DAPI hooks, including CC peace broker! */
	static EntityPlayer getTarget(EntityLiving src, double range, double invisFactor) {
		double minDist = 0;
		EntityPlayer ret = null;

		for (EntityPlayer ep : ((List<EntityPlayer>)src.worldObj.playerEntities)) {
			if (ep.capabilities.disableDamage || !ep.isEntityAlive())
				continue;
			double dd = ep.getDistanceSqToEntity(src);

			if (invisFactor < 1 && ep.isInvisible()) {
				double f = Math.max(0.1, ep.getArmorVisibility());
				range *= invisFactor*f;
			}

			if (dd <= range*range && (ret == null || dd < minDist)) {
				minDist = dd;
				ret = ep;
			}
		}

		return ret;
	}

	@Override
	protected boolean isValidLightLevel() {
		return true;
	}

	@Override
	public float getBlockPathWeight(int x, int y, int z) {
		return 1F;
	}

	@Override
	public boolean attackEntityAsMob(Entity e) {
		boolean flag = super.attackEntityAsMob(e);
		if (flag && e instanceof EntityLivingBase) {
			((EntityLivingBase)e).addPotionEffect(new PotionEffect(Potion.poison.id, 20, 1));
			if (e instanceof EntityPlayerMP) {
				ReikaPacketHelper.sendDataPacket(Satisforestry.packetChannel, SFPackets.MOBDAMAGE.ordinal(), (EntityPlayerMP)e, this.getEntityId());
			}
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

		spawnPoint = WorldLocation.readFromNBT("spawnLocation", NBT);

		poisonGasTick = NBT.getInteger("gastime");
		poisonGasCooldown = NBT.getInteger("gascool");
		jumpCooldown = NBT.getInteger("jumpcool");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound NBT) {
		super.writeEntityToNBT(NBT);

		if (spawnPoint != null)
			spawnPoint.writeToNBT("spawnLocation", NBT);

		NBT.setInteger("gastime", poisonGasTick);
		NBT.setInteger("gascool", poisonGasCooldown);
		NBT.setInteger("jumpcool", jumpCooldown);
	}

}
