package Reika.Satisforestry.Entity;

import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.Satisforestry.Entity.AI.EntityAISpitterBlast;
import Reika.Satisforestry.Entity.AI.EntityAISpitterFireball;
import Reika.Satisforestry.Entity.AI.EntityAISpitterFireball.EntityAISpitterClusterFireball;
import Reika.Satisforestry.Entity.AI.EntityAISpitterFireball.EntityAISpitterSplittingFireball;
import Reika.Satisforestry.Registry.SFSounds;

public class EntitySpitter extends EntityMob {

	private EntityAISpitterBlast knockbackBlast = new EntityAISpitterBlast(this, 2, 1);
	private EntityAISpitterFireball basicFireball = new EntityAISpitterFireball(this, 1.0D, 40, 2, 12, 2, 4);

	private EntityAISpitterBlast knockbackBlastBig = new EntityAISpitterBlast(this, 3, 2);

	private EntityAISpitterFireball fastFireball = new EntityAISpitterFireball(this, 1.0D, 50, 3, 18, 4, 7);
	private EntityAISpitterFireball clusterFireball = new EntityAISpitterClusterFireball(this, 1.0D, 150, 6, 24, 4, 3);

	private EntityAISpitterFireball splittingFireball = new EntityAISpitterSplittingFireball(this, 1.0D, 150, 6, 24, 2, 5);

	//attacks: close blast, fireball (maybe alpha types)
	public EntitySpitter(World world) {
		super(world);
		this.setSpitterType(SpitterType.BASIC);
		//isImmuneToFire = true;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
	}

	@Override
	public void onLivingUpdate() {
		if (!worldObj.isRemote) {
			this.setSpeed(this.isRunning() ? 0.4 : 0.25);
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
	public void playLivingSound() {
		SFSounds s = rand.nextBoolean() ? SFSounds.SPITTER1 : SFSounds.SPITTER2;
		s.playSound(this);
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
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setByte("type", (byte)this.getSpitterType().ordinal());
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

		if (type.isAlpha()) {
			tasks.addTask(4, knockbackBlastBig);
		}
		else {
			tasks.addTask(4, knockbackBlast);
		}
		switch(type) {
			case BASIC:
				tasks.addTask(5, basicFireball);
				break;
			case GREEN:
				tasks.addTask(5, fastFireball);
				tasks.addTask(6, clusterFireball);
				break;
			case RED:
				tasks.addTask(5, splittingFireball);
				break;
		}
	}

	private static enum SpitterType {
		BASIC(10, 1F),
		RED(15, 2F),
		GREEN(20, 1F),
		;

		private final int health;
		private final float blastScale;

		private static final SpitterType[] list = values();

		private SpitterType(int hearts, float sc) {
			health = hearts*2;
			blastScale = sc;
		}

		public boolean isAlpha() {
			return this != BASIC;
		}
	}

}
