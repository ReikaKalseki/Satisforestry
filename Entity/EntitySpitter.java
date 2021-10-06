package Reika.Satisforestry.Entity;

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
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import Reika.DragonAPI.Extras.IconPrefabs;
import Reika.DragonAPI.Instantiable.Effects.EntityBlurFX;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.Satisforestry.Biome.Biomewide.PointSpawnSystem;
import Reika.Satisforestry.Biome.Biomewide.PointSpawnSystem.SpawnPoint;
import Reika.Satisforestry.Entity.AI.EntityAISpitterBlast;
import Reika.Satisforestry.Entity.AI.EntityAISpitterFireball;
import Reika.Satisforestry.Entity.AI.EntityAISpitterFireball.EntityAISpitterClusterFireball;
import Reika.Satisforestry.Entity.AI.EntityAISpitterFireball.EntityAISpitterSplittingFireball;
import Reika.Satisforestry.Registry.SFSounds;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntitySpitter extends EntityMob {

	private EntityAISpitterBlast knockbackBlast = new EntityAISpitterBlast(this, 2, 1);
	private EntityAISpitterFireball basicFireball = new EntityAISpitterFireball(this, 1.0D, 40, 2, 12, 2, 4);

	private EntityAISpitterBlast knockbackBlastBig = new EntityAISpitterBlast(this, 3, 2);

	private EntityAISpitterFireball fastFireball = new EntityAISpitterFireball(this, 1.0D, 50, 3, 18, 4, 7);
	private EntityAISpitterFireball clusterFireball = new EntityAISpitterClusterFireball(this, 1.0D, 150, 6, 24, 4, 3);

	private EntityAISpitterFireball splittingFireball = new EntityAISpitterSplittingFireball(this, 1.0D, 150, 6, 24, 2, 5);

	private int lastblast = 0;

	//attacks: close blast, fireball (maybe alpha types)
	public EntitySpitter(World world) {
		super(world);

		tasks.addTask(1, new EntityAISwimming(this));
		tasks.addTask(5, new EntityAIWander(this, 1.0D));
		tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 20F));
		tasks.addTask(6, new EntityAILookIdle(this));
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
		EntityPlayer ep = worldObj.getClosestVulnerablePlayerToEntity(this, 32.0D);
		return ep != null && this.canEntityBeSeen(ep) ? ep : null;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(32);
		this.setSpeed(0.25);
	}

	@Override
	public void onLivingUpdate() {
		if (!worldObj.isRemote) {
			this.setSpeed(this.isRunning() ? 0.4 : 0.25);
			lastblast++;
		}
		super.onLivingUpdate();
	}

	private void setSpeed(double d) {
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(d);
	}

	public void setRunning(boolean run) {
		dataWatcher.updateObject(14, (byte)(run ? 1 : 0));
	}

	public boolean isRunning() {
		return dataWatcher.getWatchableObjectByte(14) > 0;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(13, new Byte((byte)0));
		dataWatcher.addObject(14, new Byte((byte)0));
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
		int color = type.coreColor;
		Vec3 vec = this.getLookVec();
		double x0 = posX+vec.xCoord*0.5;
		double y0 = posY+this.getEyeHeight()+vec.yCoord+0.25;
		double z0 = posZ+vec.zCoord*0.5;
		for (int i = 0; i < 6; i++) {
			double x = ReikaRandomHelper.getRandomPlusMinus(x0, 0.5);
			double y = ReikaRandomHelper.getRandomPlusMinus(y0, 0.25);
			double z = ReikaRandomHelper.getRandomPlusMinus(z0, 0.5);
			IIcon icon = IconPrefabs.FADE_GENTLE.getIcon();
			EntityBlurFX fx = new EntityBlurFX(worldObj, x, y, z, icon);
			int l = ReikaRandomHelper.getRandomBetween(5, 8);
			float s = (float)ReikaRandomHelper.getRandomBetween(3.5, 6);
			fx.setRapidExpand().setAlphaFading().setLife(l).setScale(s);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}
	}

	@Override
	public void playLivingSound() {
		SFSounds s = rand.nextBoolean() ? SFSounds.SPITTER1 : SFSounds.SPITTER2;
		SpitterType t = this.getSpitterType();
		float f = (float)ReikaRandomHelper.getRandomBetween(t.isAlpha() ? 0.5 : 0.8, t.isAlpha() ? 1 : 1.5);
		s.playSound(this, 1, f);
	}

	public void playHurtSound() {
		SFSounds.SPITTERHURT.playSound(this);
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
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setByte("type", (byte)this.getSpitterType().ordinal());
		tag.setInteger("blast", lastblast);
	}

	public SpitterType getSpitterType() {
		return SpitterType.list[dataWatcher.getWatchableObjectByte(13)];
	}

	public void setSpitterType(SpitterType type) {
		dataWatcher.updateObject(13, Byte.valueOf((byte)type.ordinal()));
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

	private void setCombatTask() {
		SpitterType type = this.getSpitterType();

		tasks.removeTask(basicFireball);
		tasks.removeTask(fastFireball);
		tasks.removeTask(clusterFireball);
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
				tasks.addTask(3, basicFireball);
				break;
			case GREEN:
				tasks.addTask(3, fastFireball);
				tasks.addTask(4, clusterFireball);
				break;
			case RED:
				tasks.addTask(3, basicFireball);
				tasks.addTask(4, splittingFireball);
				break;
		}
	}

	public void resetBlastTimer() {
		lastblast = 0;
	}

	public boolean isBlastReady() {
		return lastblast >= 30;
	}

	public static enum SpitterType {
		BASIC(10, 1F, 0xFFCC4A),
		RED(15, 2F, 0xFF6E00),
		GREEN(20, 1F, 0x37E9B2),
		;

		private final int health;
		private final float blastScale;
		public final int coreColor;

		public static final SpitterType[] list = values();

		private SpitterType(int hearts, float sc, int c) {
			health = hearts*2;
			blastScale = sc;
			coreColor = c;
		}

		public boolean isAlpha() {
			return this != BASIC;
		}
	}

}
