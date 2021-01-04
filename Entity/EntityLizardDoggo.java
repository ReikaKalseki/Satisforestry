package Reika.Satisforestry.Entity;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom.DynamicWeight;
import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.Biomewide.BiomewideFeatureGenerator;
import Reika.Satisforestry.Biome.Biomewide.LizardDoggoSpawner.LizardDoggoSpawnPoint;
import Reika.Satisforestry.Config.BiomeConfig;
import Reika.Satisforestry.Config.DoggoDrop;

public class EntityLizardDoggo extends EntityTameable {

	private WorldLocation spawnPoint;
	private ItemStack foundItem;
	private long lastItemTick;

	private final WeightedRandom<DoggoDropHook> dropTable = new WeightedRandom();

	public EntityLizardDoggo(World w) {
		super(w);
		lastItemTick = w.getTotalWorldTime();
	}

	@Override
	public EntityAgeable createChild(EntityAgeable e) {
		return null;
	}

	public void setSpawn(WorldLocation loc) {
		spawnPoint = loc;
	}

	@Override
	public void setDead() {
		super.setDead();
		if (!worldObj.isRemote) {
			LizardDoggoSpawnPoint spawn = BiomewideFeatureGenerator.instance.getDoggoSpawnAt(spawnPoint);
			if (spawn != null) {
				spawn.removeDoggo();
			}
		}
	}

	@Override
	protected void despawnEntity() {

	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (foundItem == null && ticksExisted >= 900 && this.isTamed()) {
			long tick = worldObj.getTotalWorldTime();
			if (rand.nextInt(10000) == 0 || tick-lastItemTick >= 20*60*15) {
				foundItem = this.getRandomDrop();
				lastItemTick = tick;
			}
		}
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
	protected final void entityInit() {
		super.entityInit();
	}

	@Override
	public final boolean interact(EntityPlayer ep) {
		if (!this.isTamed()) {
			ItemStack is = ep.getCurrentEquippedItem();
			if (is != null && is.getItem() == Satisforestry.paleberry && is.stackSize > 0) {
				this.setTamed(true);
				this.setPathToEntity((PathEntity)null);
				this.setAttackTarget((EntityLivingBase)null);
				aiSit.setSitting(true);
				this.setHealth(20.0F);
				this.func_152115_b(ep.getUniqueID().toString());
				this.playTameEffect(true);
				worldObj.setEntityState(this, (byte)7);
				is.stackSize--;
				if (is.stackSize <= 0)
					ep.setCurrentItemOrArmor(0, null);
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
	public void readEntityFromNBT(NBTTagCompound NBT) {
		super.readEntityFromNBT(NBT);

		spawnPoint = WorldLocation.readFromNBT("spawnLocation", NBT);
		lastItemTick = NBT.getLong("lastItem");
		if (NBT.hasKey("foundItem"))
			foundItem = ItemStack.loadItemStackFromNBT(NBT.getCompoundTag("foundItem"));

	}

	@Override
	public void writeEntityToNBT(NBTTagCompound NBT) {
		super.writeEntityToNBT(NBT);

		spawnPoint.writeToNBT("spawnLocation", NBT);
		NBT.setLong("lastItem", lastItemTick);
		if (foundItem != null) {
			NBTTagCompound tag = new NBTTagCompound();
			foundItem.writeToNBT(tag);
			NBT.setTag("foundItem", tag);
		}
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
