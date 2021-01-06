package Reika.Satisforestry.Entity;

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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import Reika.DragonAPI.DragonAPIInit;
import Reika.DragonAPI.Instantiable.InertItem;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom.DynamicWeight;
import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.Biomewide.BiomewideFeatureGenerator;
import Reika.Satisforestry.Biome.Biomewide.LizardDoggoSpawner.LizardDoggoSpawnPoint;
import Reika.Satisforestry.Config.BiomeConfig;
import Reika.Satisforestry.Config.DoggoDrop;
import Reika.Satisforestry.Registry.SFEntities;
import Reika.Satisforestry.Registry.SFSounds;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityLizardDoggo extends EntityTameable {

	private WorldLocation spawnPoint;
	private ItemStack foundItem;
	private long lastItemTick;

	private int sprintJumpTick;

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
		tasks.addTask(2, aiSit);
		tasks.addTask(6, new EntityAIFollowOwner(this, 0.4, 4, 2.5F)); //args: speed, dist to start follow, dist to consider "reached them"
		tasks.addTask(5, new EntityAIFollowOwner(this, 0.6, 15, 2.5F));
		tasks.addTask(7, new EntityAIWander(this, 0.5)); //speed
		tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 15)); //max dist
		tasks.addTask(9, new EntityAILookIdle(this));
		//targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
		//targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
		//targetTasks.addTask(3, new EntityAIHurtByTarget(this, true));
	}

	@Override
	protected final void entityInit() {
		super.entityInit();

		dataWatcher.addObject(15, (byte)0);
		dataWatcher.addObject(14, 0);
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

	@Override
	public void setDead() {
		super.setDead();
		if (!worldObj.isRemote && spawnPoint != null) {
			LizardDoggoSpawnPoint spawn = BiomewideFeatureGenerator.instance.getDoggoSpawnAt(spawnPoint);
			if (spawn != null) {
				spawn.removeDoggo(this);
			}
		}
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

		if (!worldObj.isRemote && foundItem == null && ticksExisted >= 900 && this.isTamed()) {
			long tick = worldObj.getTotalWorldTime();
			if (rand.nextInt(10000) == 0 || worldObj.getTotalWorldTime()-lastItemTick >= 20*60*15) {
				this.generateItem();
			}
		}

		if (worldObj.isRemote) {
			if (this.getHealth() > 0 && ticksExisted%9 == 0)
				ReikaSoundHelper.playClientSound(SFSounds.DOGGOPANT, this, 0.4F+rand.nextFloat()*0.1F, 0.9F+rand.nextFloat()*0.1F);
			if (needsItemUpdate) {
				renderItem = foundItem != null ? new InertItem(worldObj, foundItem) : null;
			}
		}
	}

	private void generateItem() {
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
			this.heal(0.1F);

			if (sprintJumpTick > 0)
				sprintJumpTick--;

			boolean run = false;

			if (onGround && this.isTamed()) {
				double vel = ReikaMathLibrary.py3d(motionX, 0, motionZ);
				if (vel > 0.125) {
					if (sprintJumpTick == 0) {
						sprintJumpTick = 12;
						this.jump();
					}
					run = true;
				}
			}
			dataWatcher.updateObject(15, run ? (byte)1 : (byte)0);
			dataWatcher.updateObject(14, sprintJumpTick);
		}
	}

	@Override
	protected void jump() {
		motionY = sprintJumpTick == 0 ? 0.55 : 0.35;//0.42;
		if (this.isPotionActive(Potion.jump)) {
			motionY += (this.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
		}

		if (sprintJumpTick > 0) {
			float f = rotationYaw * 0.017453292F;
			double v = 0.45;//0.35;//0.2;
			motionX -= MathHelper.sin(f) * v;
			motionZ += MathHelper.cos(f) * v;
		}
		else {
			//make sound
		}

		isAirBorne = true;
		ForgeHooks.onLivingJump(this);
	}

	public boolean isSprintingToPlayer() {
		return dataWatcher.getWatchableObjectByte(15) > 0;
	}

	public int getSprintJumpTick() {
		return dataWatcher.getWatchableObjectInt(14);
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
				return true;
			}
		}
		if (super.interact(ep))
			return true;
		this.playLivingSound();
		if (!worldObj.isRemote && foundItem != null && this.func_152114_e(ep)) {
			if (ep.getCurrentEquippedItem() == null) {
				ep.setCurrentItemOrArmor(0, foundItem);
				foundItem = null;
				return true;
			}
		}
		aiSit.setSitting(!this.isSitting());
		isJumping = false;
		return false;
	}

	@Override
	public void playLivingSound() {

	}

	@Override
	public int getTalkInterval() {
		return 120;
	}

	@Override
	public String getCommandSenderName() {
		return SFEntities.DOGGO.entityName;
	}

	@SideOnly(Side.CLIENT)
	public EntityItem getHeldItemForRender() {
		return renderItem;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound NBT) {
		super.readEntityFromNBT(NBT);

		spawnPoint = WorldLocation.readFromNBT("spawnLocation", NBT);
		lastItemTick = NBT.getLong("lastItem");
		if (NBT.hasKey("foundItem")) {
			foundItem = ItemStack.loadItemStackFromNBT(NBT.getCompoundTag("foundItem"));
		}
		needsItemUpdate = true;

		sprintJumpTick = NBT.getInteger("sprintJump");
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

}
