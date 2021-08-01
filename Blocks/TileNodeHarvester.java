package Reika.Satisforestry.Blocks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.ASM.APIStripper.Strippable;
import Reika.DragonAPI.Base.TileEntityBase;
import Reika.DragonAPI.Exception.RegistrationException;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.ModInteract.Power.ReikaEUHelper;
import Reika.RotaryCraft.API.Power.PowerTransferHelper;
import Reika.RotaryCraft.API.Power.ShaftPowerReceiver;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockResourceNode.Purity;
import Reika.Satisforestry.Blocks.BlockResourceNode.TileResourceNode;
import Reika.Satisforestry.Registry.SFBlocks;

import cofh.api.energy.IEnergyReceiver;
import ic2.api.energy.tile.IEnergySink;


public abstract class TileNodeHarvester extends TileEntityBase {

	private int tier = 0;
	private int activityTimer = 0;

	@Override
	public final Block getTileEntityBlockID() {
		return SFBlocks.HARVESTER.getBlockInstance();
	}

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		if (world.isRemote) {
			if (activityTimer > 0) {
				this.doActivityFX(world, x, y, z);
			}
		}
		else {
			Block b = world.getBlock(x, y-1, z);
			if (b == SFBlocks.RESOURCENODE.getBlockInstance() && this.hasEnergy()) {
				TileResourceNode te = (TileResourceNode)world.getTileEntity(x, y-1, z);
				ItemStack is = te.tryHarvest(this);
				if (is != null) {
					activityTimer = 20;
					ReikaItemHelper.dropItem(world, x+0.5, y+1, z+0.5, is);
					//ReikaJavaLibrary.pConsole(world.getTotalWorldTime());
					this.useEnergy();
				}
			}
			if (activityTimer > 0) {
				activityTimer--;
			}
		}
	}

	protected void doActivityFX(World world, int x, int y, int z) {

	}

	public final int getTier() {
		return tier;
	}

	protected abstract boolean hasEnergy();

	protected abstract void useEnergy();

	@Override
	protected void writeSyncTag(NBTTagCompound NBT) {
		super.writeSyncTag(NBT);

		NBT.setInteger("activity", activityTimer);
	}

	@Override
	protected void readSyncTag(NBTTagCompound NBT) {
		super.readSyncTag(NBT);

		activityTimer = NBT.getInteger("activity");
	}

	@Override
	protected void animateWithTick(World world, int x, int y, int z) {

	}

	@Override
	public final int getRedstoneOverride() {
		return 0;
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		return pass == 0;
	}

	private static abstract class TileNodeHarvesterBasicEnergy extends TileNodeHarvester {

		protected final long energyPerCycle;
		protected final long maxEnergy;
		protected final long maxFlowRate;

		private long energy;

		protected TileNodeHarvesterBasicEnergy(long c, long m, long f) {
			energyPerCycle = c;
			maxEnergy = m;
			maxFlowRate = f;
			if (maxFlowRate*Purity.PURE.getCountdown() < energyPerCycle)
				throw new RegistrationException(Satisforestry.instance, this+" max energy flow rate too low to sustain continuous operation!");
		}

		@Override
		protected final boolean hasEnergy() {
			return energy >= energyPerCycle;
		}

		@Override
		protected final void useEnergy() {
			energy -= energyPerCycle;
		}

		protected final long addEnergy(long amt, boolean simulate) {
			long space = maxEnergy-energy;
			long add = Math.min(this.getActualMaxFlow(), Math.min(amt, space));
			if (!simulate)
				energy += add;
			return add;
		}

		protected final long getActualMaxFlow() {
			return maxFlowRate*(1+this.getTier());
		}

		protected final long getEnergy() {
			return energy;
		}

		@Override
		protected void writeSyncTag(NBTTagCompound NBT) {
			super.writeSyncTag(NBT);

			NBT.setLong("energy", energy);
		}

		@Override
		protected void readSyncTag(NBTTagCompound NBT) {
			super.readSyncTag(NBT);

			energy = NBT.getLong("energy");
		}

	}

	public static class TileNodeHarvesterRF extends TileNodeHarvesterBasicEnergy implements IEnergyReceiver {

		protected TileNodeHarvesterRF() {
			super(20000, 600000, 2500);
		}

		@Override
		protected String getTEName() {
			return "Resource Node Harvester (RF)";
		}

		@Override
		public boolean canConnectEnergy(ForgeDirection from) {
			return true;
		}

		@Override
		public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
			return (int)this.addEnergy(maxReceive, simulate);
		}

		@Override
		public int getEnergyStored(ForgeDirection from) {
			return (int)this.getEnergy();
		}

		@Override
		public int getMaxEnergyStored(ForgeDirection from) {
			return (int)maxEnergy;
		}

	}

	@Strippable(value={"ic2.api.energy.tile.IEnergySink"})
	public static class TileNodeHarvesterEU extends TileNodeHarvesterBasicEnergy implements IEnergySink {

		protected TileNodeHarvesterEU() {
			super(3072, 6144, 256);
		}

		@Override
		protected String getTEName() {
			return "Resource Node Harvester (EU)";
		}

		@Override
		public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) {
			return true;
		}

		@Override
		public double getDemandedEnergy() {
			return this.addEnergy(Integer.MAX_VALUE, true);
		}

		@Override
		public int getSinkTier() {
			return ReikaEUHelper.getIC2TierFromEUVoltage(this.getActualMaxFlow());
		}

		@Override
		public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage) {
			long add = this.addEnergy((long)amount, false);
			return amount-add;
		}
	}


	public static class TileNodeHarvesterRC extends TileNodeHarvester implements ShaftPowerReceiver {

		private static final int MINPOWER = 262144;
		private static final int MINTORQUE = 2048;

		private int torque;
		private int omega;
		private long power;
		private int iotick;

		@Override
		public void updateEntity(World world, int x, int y, int z, int meta) {
			if (!PowerTransferHelper.checkPowerFrom(this, ForgeDirection.DOWN) && !PowerTransferHelper.checkPowerFrom(this, ForgeDirection.UP)) {
				this.noInputMachine();
			}
			super.updateEntity(world, x, y, z, meta);
		}

		@Override
		protected void readSyncTag(NBTTagCompound NBT) {
			super.readSyncTag(NBT);

			omega = NBT.getInteger("speed");
			torque = NBT.getInteger("trq");
			power = NBT.getLong("pwr");
		}

		@Override
		protected void writeSyncTag(NBTTagCompound NBT) {
			super.writeSyncTag(NBT);

			NBT.setInteger("speed", omega);
			NBT.setInteger("trq", torque);
			NBT.setLong("pwr", power);
		}

		@Override
		protected boolean hasEnergy() {
			return power >= MINPOWER && torque >= MINTORQUE;
		}

		@Override
		protected void useEnergy() {

		}

		@Override
		protected String getTEName() {
			return "Resource Node Harvester (Shaft)";
		}

		@Override
		public void setOmega(int omega) {
			this.omega = omega;
		}

		@Override
		public void setTorque(int torque) {
			this.torque = torque;
		}

		@Override
		public void setPower(long power) {
			this.power = power;
		}

		@Override
		public int getOmega() {
			return omega;
		}

		@Override
		public int getTorque() {
			return torque;
		}

		@Override
		public long getPower() {
			return power;
		}

		@Override
		public void noInputMachine() {
			omega = torque = 0;
			power = 0;
		}

		@Override
		public int getIORenderAlpha() {
			return iotick;
		}

		@Override
		public void setIORenderAlpha(int io) {
			iotick = io;
		}

		@Override
		public int getMinTorque(int available) {
			return MINTORQUE;
		}

		@Override
		public boolean canReadFrom(ForgeDirection dir) {
			return true;
		}

		@Override
		public boolean isReceiving() {
			return true;
		}

	}

}
