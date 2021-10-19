package Reika.Satisforestry.Entity;

import java.util.ArrayList;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import Reika.DragonAPI.APIPacketHandler.PacketIDs;
import Reika.DragonAPI.DragonAPIInit;
import Reika.DragonAPI.Auxiliary.Trackers.TickScheduler;
import Reika.DragonAPI.Instantiable.InertItem;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom.DynamicWeight;
import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.DragonAPI.Instantiable.Event.ScheduledTickEvent;
import Reika.DragonAPI.Instantiable.Event.ScheduledTickEvent.ScheduledSoundEvent;
import Reika.DragonAPI.Instantiable.IO.PacketTarget;
import Reika.DragonAPI.Libraries.ReikaInventoryHelper;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.MathSci.ReikaMusicHelper.MusicKey;
import Reika.DragonAPI.Libraries.Registry.ReikaParticleHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.API.LizardDoggo;
import Reika.Satisforestry.Config.BiomeConfig;
import Reika.Satisforestry.Config.DoggoDrop;
import Reika.Satisforestry.Entity.AI.EntityAIComeGetPaleberry;
import Reika.Satisforestry.Entity.AI.EntityAIDepositItem;
import Reika.Satisforestry.Entity.AI.EntityAIRunFromPlayer;
import Reika.Satisforestry.Entity.AI.EntityAISlowlyBackFromPlayer;
import Reika.Satisforestry.Registry.SFEntities;
import Reika.Satisforestry.Registry.SFSounds;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityLizardDoggo extends EntityTameable implements SpawnPointEntity, LizardDoggo {

	private static final int SNEEZE_LENGTH_1 = 8;
	private static final int SNEEZE_LENGTH_2 = 40;

	private static final ArrayList<MusicKey>[] melody = new ArrayList[4];

	static {
		melody[0] = new ArrayList();
		melody[1] = new ArrayList();
		melody[2] = new ArrayList();
		melody[3] = new ArrayList();

		melody[0].add(MusicKey.G5);
		melody[0].add(MusicKey.A4);
		melody[0].add(MusicKey.E5);
		melody[0].add(MusicKey.A5);

		melody[1].add(MusicKey.E5);
		melody[1].add(MusicKey.C6);
		melody[1].add(MusicKey.A5);
		melody[1].add(MusicKey.G5);
		melody[1].add(MusicKey.E5);
		melody[1].add(MusicKey.G5);

		melody[2].add(MusicKey.E5);
		melody[2].add(MusicKey.C6);
		melody[2].add(MusicKey.A5);
		melody[2].add(MusicKey.G5);
		melody[2].add(MusicKey.E5);
		melody[2].add(MusicKey.D5);

		melody[3].add(MusicKey.E5);
		melody[3].add(MusicKey.G5);
		melody[3].add(MusicKey.E5);
		melody[3].add(MusicKey.G5);
		melody[3].add(MusicKey.A5);
	}

	private WorldLocation spawnPoint;
	private ItemStack foundItem;
	private long lastItemTick;

	private boolean backwards;
	private boolean lured;
	private boolean running;
	private boolean isJumpingInPlace;
	private int sprintJumpTick;
	private int sneezeTick1 = 0;
	private int sneezeTick2 = 0;
	private int healTick = 0;
	private int songIndex = 0;
	private int itemDeposit = 0;

	private EntityItem renderItem;
	private boolean needsItemUpdate;

	private final WeightedRandom<DoggoDropHook> dropTable = new WeightedRandom();

	public EntityLizardDoggo(World w) {
		super(w);
		lastItemTick = w.getTotalWorldTime();
		noClip = false;
		this.setSize(1, 0.7F);

		this.setAIMoveSpeed(0.025F);

		this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(75);

		//args: priority (lower is more priority), task
		tasks.addTask(1, new EntityAISwimming(this));
		EntityAIDepositItem deposit = new EntityAIDepositItem(this, 12);
		tasks.addTask(1, deposit);
		tasks.addTask(2, aiSit);
		tasks.addTask(3, new EntityAIRunFromPlayer(this, 24, 0.4, 0.7));
		tasks.addTask(4, new EntityAIComeGetPaleberry(this, 10, 0.28));
		tasks.addTask(5, new EntityAISlowlyBackFromPlayer(this, 8, 0.2));
		EntityAIFollowOwner seek = new EntityAIFollowOwner(this, 0.4, 4, 2.5F); //args: speed, dist to start follow, dist to consider "reached them"
		tasks.addTask(6, seek);
		tasks.addTask(5, new EntityAIFollowOwner(this, 0.6, 12, 2.5F));
		EntityAIWatchClosest look = new EntityAIWatchClosest(this, EntityPlayer.class, 30);
		look.setMutexBits(4);
		tasks.addTask(7, look); //max dist
		tasks.addTask(8, new EntityAIWander(this, 0.5)); //speed
		tasks.addTask(9, new EntityAILookIdle(this));
		//targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
		//targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
		//targetTasks.addTask(3, new EntityAIHurtByTarget(this, true));
	}

	@Override
	protected final void entityInit() {
		super.entityInit();

		dataWatcher.addObject(14, 0);
		dataWatcher.addObject(15, 0);
		dataWatcher.addObject(19, 0);
		dataWatcher.addObject(20, 0);
	}

	@Override
	public EntityAgeable createChild(EntityAgeable e) {
		return null;
	}

	@Override
	protected boolean isAIEnabled() {
		return true;
	}

	public void setSpawn(WorldLocation loc) {
		spawnPoint = loc;
	}

	public WorldLocation getSpawn() {
		return spawnPoint;
	}

	public void setLured(boolean lure) {
		lured = lure;
	}

	public void setBackwards(boolean back) {
		backwards = back;
	}

	@Override
	protected boolean canDespawn() {
		return !this.isTamed();
	}

	@Override
	protected void despawnEntity() {
		if (this.isTamed()) {

		}
		else {
			super.despawnEntity();
		}
	}

	@Override
	public boolean isBreedingItem(ItemStack is) {
		return false;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		long tick = worldObj.getTotalWorldTime();
		if (!worldObj.isRemote && this.isTamed()) {
			if (foundItem == null && ticksExisted >= 900) {
				long last = this.ticksSinceLastItem();
				if (last > 20*15 && (rand.nextInt(10000) == 0 || last >= 20*60*15)) {
					this.generateItem();
				}
			}
			else if (tick%100 == 0)
				ReikaPacketHelper.sendEntitySyncPacket(DragonAPIInit.packetChannel, this, 128);

			if (this.hasItem())
				lastItemTick = tick;

			if (itemDeposit > 0)
				itemDeposit--;
		}

		if (worldObj.isRemote) {
			if (this.isTamed() && this.getHealth() > 0 && ticksExisted%9 == 0)
				ReikaSoundHelper.playClientSound(SFSounds.DOGGOPANT, this, 0.4F+rand.nextFloat()*0.1F, 0.9F+rand.nextFloat()*0.1F);
			if (needsItemUpdate) {
				renderItem = foundItem != null ? new InertItem(worldObj, foundItem) : null;
				needsItemUpdate = false;
			}
		}
		else {
			this.updateFlags();
		}
	}

	public long ticksSinceLastItem() {
		return worldObj.getTotalWorldTime()-lastItemTick;
	}

	private void updateFlags() {
		int flags = 0;
		for (DoggoFlags f : DoggoFlags.list) {
			if (f.evaluate(this)) {
				flags |= f.getBit();
			}
		}
		dataWatcher.updateObject(15, flags);
	}

	public void generateItem() {
		if (foundItem != null)
			return;
		foundItem = this.getRandomDrop();
		lastItemTick = worldObj.getTotalWorldTime();
		ReikaPacketHelper.sendEntitySyncPacket(DragonAPIInit.packetChannel, this, 128);
		needsItemUpdate = true;
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();

		velocityChanged = true;

		if (!worldObj.isRemote) {
			if (sprintJumpTick > 0)
				sprintJumpTick--;

			running = false;

			if (sneezeTick1 > 0)
				sneezeTick1--;
			if (sneezeTick2 > 0)
				sneezeTick2--;
			if (healTick > 0)
				healTick--;

			if (this.isSneezing()) {
				rotationYawHead = rotationYaw;
				rotationPitch = 0;
			}

			if (onGround && this.isTamed()) {
				if (!this.isSneezing() && healTick == 0 && rand.nextInt(300) == 0)
					this.sneeze();
				if (this.isSitting()) {

				}
				else {
					isJumpingInPlace = false;
					double vel = ReikaMathLibrary.py3d(motionX, 0, motionZ);
					boolean flag = vel > 0.125;
					if (flag || (!flag && !this.isSneezing() && rand.nextInt(150) == 0)) {
						if (sprintJumpTick == 0) {
							sprintJumpTick = 12;
							isJumpingInPlace = !flag;
							this.jump();
						}
					}
					running = flag;
				}
			}
			dataWatcher.updateObject(14, sprintJumpTick);
			dataWatcher.updateObject(20, sneezeTick1);
			dataWatcher.updateObject(19, sneezeTick2);
		}
	}

	private void sneeze() {
		if (rand.nextBoolean()) {
			sneezeTick1 = SNEEZE_LENGTH_1;
			SFSounds.DOGGOSNEEZE1.playSound(this);
		}
		else {
			sneezeTick2 = SNEEZE_LENGTH_2;
			SFSounds.DOGGOSNEEZE2.playSound(this);
		}
	}

	@Override
	protected void jump() {
		motionY = sprintJumpTick == 0 || isJumpingInPlace ? 0.45 : 0.35;//0.42;
		if (this.isPotionActive(Potion.jump)) {
			motionY += (this.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
		}

		if (sprintJumpTick > 0 && !isJumpingInPlace) {
			float f = rotationYaw * 0.017453292F;
			double v = 0.45;//0.35;//0.2;
			motionX -= MathHelper.sin(f) * v;
			motionZ += MathHelper.cos(f) * v;
		}
		else {
			this.playLivingSound();
		}

		isJumpingInPlace = false;
		isAirBorne = true;
		ForgeHooks.onLivingJump(this);
	}

	public float getSneezeTick2() {
		return Math.min(1, dataWatcher.getWatchableObjectInt(19)*1.5F/SNEEZE_LENGTH_2);
	}

	public float getSneezeTick1() {
		return dataWatcher.getWatchableObjectInt(20)/(float)SNEEZE_LENGTH_1;
	}

	public int getSprintJumpTick() {
		return dataWatcher.getWatchableObjectInt(14);
	}

	public boolean isSneezing() {
		return sneezeTick1 > 0 || sneezeTick2 > 0;
	}

	private ItemStack getRandomDrop() {
		if (dropTable.isEmpty()) {
			for (DoggoDrop dd : BiomeConfig.instance.getDoggoDrops()) {
				dropTable.addEntry(new DoggoDropHook(dd), dd.baseWeight);
			}
		}
		return dropTable.isEmpty() ? null : dropTable.getRandomEntry().drop.generateItem(rand);
	}

	@Override
	public final boolean interact(EntityPlayer ep) {
		if (this.isSneezing())
			return false;
		if (!this.isTamed()) {
			ItemStack is = ep.getCurrentEquippedItem();
			if (is != null && is.getItem() == Satisforestry.paleberry && is.stackSize > 0) {
				this.setTamed(true);
				this.setPathToEntity((PathEntity)null);
				this.setAttackTarget((EntityLivingBase)null);
				this.setHealth(20.0F);
				this.func_152115_b(ep.getUniqueID().toString());
				this.playTameEffect(true);
				worldObj.setEntityState(this, (byte)7);
				if (ep.capabilities.isCreativeMode)
					is.stackSize--;
				if (is.stackSize <= 0)
					is = null;
				ep.setCurrentItemOrArmor(0, is);
				isJumpingInPlace = true;
				this.jump();
				return true;
			}
		}
		if (super.interact(ep))
			return true;
		if (this.isTamed() && onGround && !this.isSitting() && rand.nextInt(5) == 0) {
			isJumpingInPlace = true;
			this.jump();
		}
		if (!worldObj.isRemote && !ep.isSneaking() && this.isTamed()) {
			ItemStack is = ep.getCurrentEquippedItem();
			if (is != null && is.getItem() == Satisforestry.paleberry && this.getHealth() < this.getMaxHealth()) {
				if (healTick == 0) {
					//this.playTameEffect(true); does not work onserverside
					for (int i = 0; i < 7; i++) {
						int[] x = ReikaJavaLibrary.splitDoubleToInts(ReikaRandomHelper.getRandomPlusMinus(posX, width));
						int[] y = ReikaJavaLibrary.splitDoubleToInts(ReikaRandomHelper.getRandomBetween(posY, posY+height));
						int[] z = ReikaJavaLibrary.splitDoubleToInts(ReikaRandomHelper.getRandomPlusMinus(posZ, width));
						ArrayList<Integer> li = ReikaJavaLibrary.makeIntListFromArray(ReikaParticleHelper.HEART.ordinal(), 1, x[0], x[1], y[0], y[1], z[0], z[1]);
						ReikaPacketHelper.sendDataPacket(DragonAPIInit.packetChannel, PacketIDs.PARTICLEWITHPOS.ordinal(), new PacketTarget.RadiusTarget(this, 64), li);
					}
					healTick = 50;
					this.heal(4);
					ArrayList<MusicKey> line = melody[songIndex];
					int t = 5;
					for (int i = 0; i < line.size(); i++) {
						MusicKey m = line.get(i);
						float f = (float)m.getRatio(MusicKey.G5);
						TickScheduler.instance.scheduleEvent(new ScheduledTickEvent(new ScheduledSoundEvent(SFSounds.DOGGOSING, this, 1, f)), t);
						t += i == 0 || (songIndex == 3 && i == 1) ? 6 : 3;
					}
					songIndex = (songIndex+1)%melody.length;
					if (!ep.capabilities.isCreativeMode)
						is.stackSize--;
					ep.setCurrentItemOrArmor(0, is);
				}
				return true;
			}
			this.playLivingSound();
			if (foundItem != null && this.func_152114_e(ep)) {
				if (ep.getCurrentEquippedItem() == null) {
					ep.setCurrentItemOrArmor(0, foundItem);
					foundItem = null;
					needsItemUpdate = true;
					ReikaPacketHelper.sendEntitySyncPacket(DragonAPIInit.packetChannel, this, 128);
					return true;
				}
			}
			if (onGround) {
				aiSit.setSitting(!this.isSitting());
				isJumping = false;
			}
		}
		return false;
	}

	@Override
	protected boolean isMovementBlocked() {
		return super.isMovementBlocked() || this.isSneezing();
	}

	@Override
	protected boolean isMovementCeased() {
		return super.isMovementCeased() || this.isSneezing();
	}

	@Override
	public void playLivingSound() {
		if (sneezeTick1 > 0 || sneezeTick2 > 0 || healTick > 0)
			return;
		float v = 0.7F+rand.nextFloat()*0.3F;
		float p = 0.75F+rand.nextFloat()*0.75F;
		this.getRandomLivingSound(true).playSound(this, v, p);
	}

	private SFSounds getRandomLivingSound(boolean allowHurt) {
		if (allowHurt && this.getHealth() < this.getMaxHealth()*0.33 && rand.nextBoolean())
			return this.getRandomHurtSound();
		switch(rand.nextInt(6)) {
			case 0:
				return SFSounds.DOGGO1;
			case 1:
				return SFSounds.DOGGO2;
			case 2:
				return SFSounds.DOGGO3;
			case 3:
				return SFSounds.DOGGO4;
			case 4:
				return SFSounds.DOGGO5;
			case 5:
				return SFSounds.DOGGO6;
		};
		return null;
	}

	public void playHurtSound() {
		this.getRandomHurtSound().playSound(this);
	}

	private SFSounds getRandomHurtSound() {
		return rand.nextBoolean() ? SFSounds.DOGGOHURT : SFSounds.DOGGOHURT2;
	}

	public void playDeathSound() {

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
		return this.getHurtSound();//"DIEKEY";
	}

	@Override
	protected String func_146067_o(int dist) {
		return this.getHurtSound();
	}

	@Override
	public int getTalkInterval() {
		return 220;
	}

	@Override
	public String getCommandSenderName() {
		return SFEntities.DOGGO.entityName;
	}

	@SideOnly(Side.CLIENT)
	public EntityItem getHeldItemForRender() {
		return renderItem;
	}

	public boolean hasItem() {
		return foundItem != null;
	}

	public ItemStack takeItem() {
		ItemStack ret = foundItem.copy();
		foundItem = null;
		return ret;
	}

	public boolean justDepositedItem() {
		return itemDeposit > 0;
	}

	public boolean tryPutItemInChest(IInventory te) {
		if (ReikaInventoryHelper.addToIInv(foundItem, te)) {
			foundItem = null;
			needsItemUpdate = true;
			ReikaPacketHelper.sendEntitySyncPacket(DragonAPIInit.packetChannel, this, 128);
			itemDeposit = 90;
			return true;
		}
		return false;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound NBT) {
		super.readEntityFromNBT(NBT);

		spawnPoint = WorldLocation.readFromNBT("spawnLocation", NBT);
		lastItemTick = NBT.getLong("lastItem");
		if (NBT.hasKey("foundItem")) {
			foundItem = ItemStack.loadItemStackFromNBT(NBT.getCompoundTag("foundItem"));
		}
		else {
			foundItem = null;
		}
		needsItemUpdate = true;

		sprintJumpTick = NBT.getInteger("sprintJump");
		backwards = NBT.getBoolean("backwards");
		healTick = NBT.getInteger("healing");
		itemDeposit = NBT.getInteger("deposit");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound NBT) {
		super.writeEntityToNBT(NBT);

		if (spawnPoint != null)
			spawnPoint.writeToNBT("spawnLocation", NBT);
		NBT.setLong("lastItem", lastItemTick);
		if (foundItem != null) {
			NBTTagCompound tag = new NBTTagCompound();
			foundItem.writeToNBT(tag);
			NBT.setTag("foundItem", tag);
		}

		NBT.setInteger("sprintJump", sprintJumpTick);
		NBT.setBoolean("backwards", backwards);
		NBT.setInteger("healing", healTick);
		NBT.setInteger("deposit", itemDeposit);
	}

	private class DoggoDropHook implements DynamicWeight {

		private final DoggoDrop drop;

		private DoggoDropHook(DoggoDrop dd) {
			drop = dd;
		}

		@Override
		public double getWeight() {
			return drop.getNetWeight(EntityLizardDoggo.this);
		}

	}

	public static enum DoggoFlags {

		BACKWARDS,
		SPRINTING,
		JUMP,
		LURED,
		;

		private static final DoggoFlags[] list = values();

		private DoggoFlags() {

		}

		private int getBit() {
			return 1 << this.ordinal();
		}

		private boolean evaluate(EntityLizardDoggo e) {
			switch(this) {
				case BACKWARDS:
					return e.backwards;
				case JUMP:
					return e.isJumpingInPlace;
				case LURED:
					return e.lured;
				case SPRINTING:
					break;
			}
			return false;
		}

		@SideOnly(Side.CLIENT)
		public boolean get(EntityLizardDoggo e) {
			return (e.dataWatcher.getWatchableObjectInt(15) & this.getBit()) != 0;
		}

	}

}
