package Reika.Satisforestry.Entity;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.API.Spitter;
import Reika.Satisforestry.Biome.Biomewide.PointSpawnSystem;
import Reika.Satisforestry.Biome.Biomewide.PointSpawnSystem.SpawnPoint;
import Reika.Satisforestry.Entity.AI.EntityAIChasePlayer;
import Reika.Satisforestry.Entity.AI.EntityAIRunToNewPosition;
import Reika.Satisforestry.Entity.AI.EntityAISpitterBlast;
import Reika.Satisforestry.Entity.AI.EntityAISpitterFireball;
import Reika.Satisforestry.Entity.AI.EntityAISpitterFireball.EntityAISpitterClusterFireball;
import Reika.Satisforestry.Entity.AI.EntityAISpitterFireball.EntityAISpitterSplittingFireball;
import Reika.Satisforestry.Registry.SFSounds;
import Reika.Satisforestry.Render.SpitterFireParticle;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntitySpitter extends EntityMob implements Spitter {

	private EntityAISpitterBlast knockbackBlast = new EntityAISpitterBlast(this, 2, 1);
	private EntityAISpitterFireball basicFireball = new EntityAISpitterFireball(this, 40, 2, 15, 1, 4);

	private EntityAISpitterBlast knockbackBlastBig = new EntityAISpitterBlast(this, 3, 2);

	private EntityAISpitterFireball fastFireball = new EntityAISpitterFireball(this, 50, 3, 10, 1.85, 7);
	private EntityAISpitterFireball clusterFireball = new EntityAISpitterClusterFireball(this, 150, 10, 50, 4, 3);

	private EntityAISpitterFireball basicFireballForAlpha = new EntityAISpitterFireball(this, 40, 3, 7.5, 1, 8);
	private EntityAISpitterFireball splittingFireball = new EntityAISpitterSplittingFireball(this, 150, 7.5, 50, 1, 5);

	private int lastblast = 0;
	private int headshake = 0;
	private int reposition = 0;
	private long lastAttack;

	//attacks: close blast, fireball (maybe alpha types)
	public EntitySpitter(World world) {
		super(world);

		tasks.addTask(1, new EntityAISwimming(this));
		//tasks.addTask(1, new EntityAIShakeHead(this));
		tasks.addTask(5, new EntityAIRunToNewPosition(this));
		tasks.addTask(5, new EntityAIChasePlayer(this));
		tasks.addTask(6, new EntityAIWander(this, 1.0D));
		tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 20F));
		tasks.addTask(7, new EntityAILookIdle(this));
		//targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));

		this.setSpitterType(SpitterType.BASIC);
		//isImmuneToFire = true;
	}

	@Override
	public void setAttackTarget(EntityLivingBase e) {
		super.setAttackTarget(e);
	}

	@Override
	protected Entity findPlayerToAttack() {
		EntityPlayer ep = worldObj.getClosestVulnerablePlayerToEntity(this, 40.0D);
		return ep != null && this.canEntityBeSeen(ep) ? ep : null;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(40);
		this.setRunning(false);
	}

	@Override
	public void onLivingUpdate() {
		if (!worldObj.isRemote) {
			this.setRunning(entityToAttack != null);
			lastblast++;
			if (headshake > 250) {
				rotationYawHead = (float)(rotationYaw+30*Math.sin(this.getHeadshake()/20D));
				headshake--;
			}
			if (reposition > 0)
				reposition--;
			dataWatcher.updateObject(15, (byte)(headshake > 0 ? 1 : 0));
		}
		super.onLivingUpdate();
	}

	@Override
	public float getEyeHeight() {
		return height/2;
	}

	private void setSpeed(double d) {
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(d*this.getSpitterType().movementSpeed);
	}

	@Override
	public float getAIMoveSpeed() {
		return (float)this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();
	}

	public void setRunning(boolean run) {
		dataWatcher.updateObject(14, (byte)(run ? 1 : 0));
		this.setSpeed(this.isRunning() ? 0.65 : 0.2);
	}

	public boolean isRunning() {
		return dataWatcher.getWatchableObjectByte(14) > 0;
	}

	public boolean isHeadShaking() {
		return dataWatcher.getWatchableObjectByte(15) > 0;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(13, new Byte((byte)0));
		dataWatcher.addObject(14, new Byte((byte)0));
		dataWatcher.addObject(15, new Byte((byte)0));
	}

	@Override
	public boolean isAIEnabled() {
		return true;
	}

	@Override
	public boolean canEntityBeSeen(Entity e) {
		SpawnPoint p = PointSpawnSystem.instance.getSpawn(this);
		return (p == null || e.getDistanceSq(p.getLocation().xCoord+0.5, p.getLocation().yCoord+0.5, p.getLocation().zCoord+0.5) <= p.getAutoClearRadius()) && super.canEntityBeSeen(e);
	}

	@SideOnly(Side.CLIENT)
	public final void doBlastFX() {
		SpitterType type = this.getSpitterType();
		Vec3 vec = this.getLookVec();
		double x0 = posX+vec.xCoord*0.5;
		double y0 = posY+this.getEyeHeight()+vec.yCoord*0.25;
		double z0 = posZ+vec.zCoord*0.5;
		double d = type.isAlpha() ? 0.875 : 0.5;
		for (int i = 0; i < 12; i++) {
			double x = ReikaRandomHelper.getRandomPlusMinus(x0, d);
			double y = ReikaRandomHelper.getRandomPlusMinus(y0, d/2);
			double z = ReikaRandomHelper.getRandomPlusMinus(z0, d);
			int l = ReikaRandomHelper.getRandomBetween(12, 20);
			float s = (float)ReikaRandomHelper.getRandomBetween(4.5, 8);
			SpitterFireParticle fx = new SpitterFireParticle(worldObj, x, y, z, type);
			fx.setRapidExpand().setAlphaFading().setLife(l).setScale(s);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}
	}

	@Override
	public void playLivingSound() {
		SFSounds s = rand.nextBoolean() ? SFSounds.SPITTER1 : SFSounds.SPITTER2;
		SpitterType t = this.getSpitterType();
		float f = (float)ReikaRandomHelper.getRandomBetween(t.isAlpha() ? 0.66 : 0.9, t.isAlpha() ? 0.9 : 1.2);
		s.playSound(this, 1, f);
	}

	public void playHurtSound() {
		float f = (float)ReikaRandomHelper.getRandomBetween(0.8, 1.3);
		if (this.getSpitterType().isAlpha())
			f *= 0.8;
		SFSounds.SPITTERHURT.playSound(this, 1, f);
	}

	public void playDeathSound() {
		this.playHurtSound();
	}

	@Override
	public void playSound(String s, float vol, float p) {
		if ("HURTKEY".equals(s)) {
			this.playHurtSound();
		}
		else if ("DIEKEY".equals(s)) {
			this.playDeathSound();
		}
		else {
			super.playSound(s, vol, p);
		}
	}

	@Override
	protected String getHurtSound() {
		return "HURTKEY";
	}

	@Override
	protected String getDeathSound() {
		return "DIEKEY";
	}

	@Override
	public EnumCreatureAttribute getCreatureAttribute() {
		return EnumCreatureAttribute.UNDEFINED;
	}

	@Override
	protected void dropFewItems(boolean player, int looting) {
		int n = this.getSpitterType().isAlpha() ? 2+rand.nextInt(4) : 1+rand.nextInt(3);
		for (int i = 0; i < n; i++) {
			ReikaItemHelper.dropItem(this, new ItemStack(Items.beef));
		}
	}

	@Override
	protected void dropRareDrop(int amt) {

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
	public boolean getCanSpawnHere() {
		return super.getCanSpawnHere();
	}

	@Override
	public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
		data = super.onSpawnWithEgg(data);
		return data;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);

		if (tag.hasKey("type", 99)) {
			byte b0 = tag.getByte("type");
			this.setSpitterType(SpitterType.list[b0]);
		}
		lastblast = tag.getInteger("blast");
		headshake = tag.getInteger("headshake");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setByte("type", (byte)this.getSpitterType().ordinal());
		tag.setInteger("blast", lastblast);
		tag.setInteger("headshake", headshake);
	}

	public SpitterType getSpitterType() {
		return SpitterType.list[dataWatcher.getWatchableObjectByte(13)];
	}

	public void setSpitterType(SpitterType type) {
		if (type == null) {
			Satisforestry.logger.logError("Tried to set null spitter type!");
			Thread.dumpStack();
			type = SpitterType.BASIC;
		}
		SpitterType old = this.getSpitterType();
		dataWatcher.updateObject(13, Byte.valueOf((byte)type.ordinal()));
		if (old != type || ticksExisted < 5) {
			this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(type.health);
			this.setHealth(this.getMaxHealth());

			if (type.isAlpha()) {
				this.setSize(0.72F, 2.34F);
			}
			else {
				this.setSize(0.6F, 1.8F);
			}

			this.setCombatTask();
		}
	}

	private void setCombatTask() {
		SpitterType type = this.getSpitterType();

		tasks.removeTask(basicFireball);
		tasks.removeTask(fastFireball);
		tasks.removeTask(clusterFireball);
		tasks.removeTask(basicFireballForAlpha);
		tasks.removeTask(splittingFireball);
		tasks.removeTask(knockbackBlast);
		tasks.removeTask(knockbackBlastBig);

		if (type.isAlpha()) {
			tasks.addTask(2, knockbackBlastBig);
		}
		else {
			tasks.addTask(2, knockbackBlast);
		}
		switch(type) {
			case BASIC:
				tasks.addTask(4, basicFireball);
				break;
			case GREEN:
				tasks.addTask(4, fastFireball);
				tasks.addTask(3, clusterFireball);
				break;
			case RED:
				tasks.addTask(4, basicFireballForAlpha);
				tasks.addTask(3, splittingFireball);
				break;
		}
	}

	public void resetBlastTimer() {
		lastblast = 0;
	}

	public boolean isBlastReady() {
		SpitterType type = this.getSpitterType();
		return lastblast >= (type.isAlpha() ? 90 : 30);
	}

	public void initiateHeadShake() {
		headshake = 300;
	}

	public boolean canContinueHeadshake() {
		return headshake >= 250;
	}

	public boolean canInitiateHeadshake() {
		return headshake <= 0;
	}

	public int getHeadshake() {
		return Math.max(headshake-250, 0);
	}

	public void setRepositioned() {
		reposition = 50;
	}

	public boolean canReposition() {
		return reposition <= 0;
	}

	public void updateAttackTime() {
		lastAttack = worldObj.getTotalWorldTime();
	}

	public long getAttackTime() {
		return worldObj.getTotalWorldTime()-lastAttack;
	}

	public static enum SpitterType {
		BASIC(10, 1F, 2, 0xFFD34A, 0xF15F00, 1),
		RED(15, 2F, 6, 0xFF6E00, 0xC80D00, 0.8),
		GREEN(20, 1F, 4, 0x37E9B2, 0x138855, 0.92),
		;

		private final int health;
		private final float blastScale;
		public final int burnDuration;
		public final int coreColor;
		public final int edgeColor;
		public final double movementSpeed;

		public static final SpitterType[] list = values();

		private SpitterType(int hearts, float sc, int b, int c, int c2, double sp) {
			health = hearts*2;
			burnDuration = b;
			blastScale = sc;
			coreColor = c;
			edgeColor = c2;
			movementSpeed = sp;
		}

		public boolean isAlpha() {
			return this != BASIC;
		}

		public double getPursuitDistance() {
			return this.isAlpha() ? 20 : 12;
		}

		public String getName() {
			return StatCollector.translateToLocal("spitter.type."+this.name().toLowerCase(Locale.ENGLISH));
		}
	}

}
