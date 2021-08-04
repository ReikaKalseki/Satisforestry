package Reika.Satisforestry.Blocks;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.APIStripper.Strippable;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Base.TileEntityBase;
import Reika.DragonAPI.Exception.RegistrationException;
import Reika.DragonAPI.Interfaces.TileEntity.BreakAction;
import Reika.DragonAPI.Libraries.ReikaDirectionHelper;
import Reika.DragonAPI.Libraries.ReikaInventoryHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaEngLibrary;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.ModInteract.Power.ReikaEUHelper;
import Reika.RotaryCraft.API.Power.PowerTransferHelper;
import Reika.RotaryCraft.API.Power.ShaftPowerReceiver;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockMinerMulti.TileMinerConnection;
import Reika.Satisforestry.Blocks.BlockMinerMulti.TileMinerConveyorPort;
import Reika.Satisforestry.Blocks.BlockMinerMulti.TileMinerPowerConnection;
import Reika.Satisforestry.Blocks.BlockMinerMulti.TileMinerShaftConnection;
import Reika.Satisforestry.Blocks.BlockResourceNode.Purity;
import Reika.Satisforestry.Blocks.BlockResourceNode.TileResourceNode;
import Reika.Satisforestry.Registry.SFBlocks;

import cofh.api.energy.IEnergyReceiver;
import ic2.api.energy.tile.IEnergySink;


public abstract class TileNodeHarvester extends TileEntityBase implements BreakAction {

	public static final int ACTIVITY_RAMP_TIME = 250; //12.5s

	private int tier = 0;
	private int activityTimer = 0;
	private int operationTimer = 0;

	private int activityRamp;
	private ForgeDirection structureDir = null;

	public float progressFactor;
	public float powerBar;

	private int overclockLevel;

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
			int stepTime = 0;
			boolean flag = false;
			TileResourceNode te = this.getResourceNode();
			if (te != null) {
				if (this.hasEnergy(false)) {
					flag = true;
					this.useEnergy(false);
					if (activityRamp == ACTIVITY_RAMP_TIME) {
						stepTime = (int)(te.getPurity().getCountdown()/this.getNetSpeedFactor());
						if (this.hasEnergy(true)) {
							if (operationTimer < stepTime)
								operationTimer++;
							activityTimer = 20;
							if (operationTimer >= stepTime) {
								ItemStack is = te.getRandomNodeItem();
								if (is != null) {
									if (this.trySpawnItem(is)) {
										operationTimer = 0;
										this.useEnergy(true);
									}
									//ReikaJavaLibrary.pConsole(world.getTotalWorldTime());
								}
							}
						}
						else {
							operationTimer = 0;
						}
						//ReikaJavaLibrary.pConsole(operationTimer);
						//ReikaJavaLibrary.pConsole(powerBar);
						//ReikaJavaLibrary.pConsole("F="+progressFactor);
					}
					else {
						operationTimer = 0;
					}
				}
			}
			if (flag) {
				activityRamp = Math.min(activityRamp+1, ACTIVITY_RAMP_TIME);
			}
			else {
				operationTimer = 0;
				activityRamp = Math.max(activityRamp-1, 0);
			}
			progressFactor = stepTime <= 0 ? 0 : operationTimer/(float)stepTime;
			powerBar = this.getOperationEnergyFraction();
			if (activityTimer > 0) {
				activityTimer--;
			}
		}
	}

	public TileResourceNode getResourceNode() {
		TileEntity te = this.getAdjacentTileEntity(ForgeDirection.DOWN);
		return te instanceof TileResourceNode ? (TileResourceNode)te : null;
	}

	public final float getNetSpeedFactor() {
		return (1+this.getTier())*this.getOverclockingLevel()*this.getSpeedFactor();
	}

	private boolean trySpawnItem(ItemStack is) {
		TileMinerConveyorPort te = this.getOutput();
		if (te != null) {
			ItemStack has = te.getStackInSlot(0);
			int fit = is.stackSize;
			if (has != null) {
				fit = ReikaItemHelper.areStacksCombinable(is, has, is.getMaxStackSize()) ? Math.min(has.getMaxStackSize()-has.stackSize, is.stackSize) : 0;
			}
			if (fit > 0)
				return ReikaInventoryHelper.addToIInv(ReikaItemHelper.getSizedItemStack(is, fit), te, true);
		}
		return false;
	}

	protected void doActivityFX(World world, int x, int y, int z) {

	}

	public final int getTier() {
		return tier;
	}

	public final boolean hasStructure() {
		return structureDir != null;
	}

	public final ForgeDirection getDirection() {
		return structureDir;
	}

	public final int getMineProgressScaled(int px) {
		return (int)(px*progressFactor);
	}

	public final int getOverclockingStep() {
		return overclockLevel;
	}

	public final float getOverclockingLevel() {
		return 1+this.getOverclockingStep()*0.5F;
	}

	public abstract float getSpeedFactor();

	protected abstract float getOperationEnergyFraction();

	protected abstract boolean hasEnergy(boolean operation);

	protected abstract void useEnergy(boolean operation);

	public abstract TileMinerConnection getInput();

	protected final TileMinerPowerConnection getWirePowerConnection() {
		if (!this.hasStructure())
			return null;
		ForgeDirection right = ReikaDirectionHelper.getRightBy90(structureDir);
		return (TileMinerPowerConnection)worldObj.getTileEntity(xCoord+right.offsetX*2, yCoord+13, zCoord+right.offsetZ*2);
	}

	protected final TileMinerShaftConnection getShaftPowerConnection() {
		return this.hasStructure() ? (TileMinerShaftConnection)worldObj.getTileEntity(xCoord-structureDir.offsetX, yCoord+12, zCoord-structureDir.offsetZ) : null;
	}

	public final TileMinerConveyorPort getOutput() {
		return this.hasStructure() ? (TileMinerConveyorPort)worldObj.getTileEntity(xCoord+structureDir.offsetX*8, yCoord+1, zCoord+structureDir.offsetZ*8) : null;
	}

	public final void breakBlock() {
		this.updateInputs(false);
	}

	@Override
	protected void writeSyncTag(NBTTagCompound NBT) {
		super.writeSyncTag(NBT);

		NBT.setInteger("activity", activityTimer);
		NBT.setInteger("operation", operationTimer);
		NBT.setInteger("structure", structureDir != null ? structureDir.ordinal() : -1);
		NBT.setInteger("overclock", overclockLevel);
	}

	@Override
	protected void readSyncTag(NBTTagCompound NBT) {
		super.readSyncTag(NBT);

		activityTimer = NBT.getInteger("activity");
		operationTimer = NBT.getInteger("operation");
		int struct = NBT.getInteger("structure");
		structureDir = struct == -1 ? null : dirs[struct];
		overclockLevel = NBT.getInteger("overclock");
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

	public final void setHasStructure(ForgeDirection flag) {
		//ReikaJavaLibrary.pConsole(flag);
		structureDir = flag;
		this.syncAllData(false);
		this.updateInputs(flag != null);
	}

	private void updateInputs(boolean has) {
		TileMinerConnection te = this.getWirePowerConnection();
		if (te != null)
			te.connectTo(has ? this : null);
		te = this.getShaftPowerConnection();
		if (te != null)
			te.connectTo(has ? this : null);
		te = this.getOutput();
		if (te != null)
			te.connectTo(has ? this : null);
	}

	public abstract ArrayList getMessages(World world, int x, int y, int z, int side);

	private static abstract class TileNodeHarvesterBasicEnergy extends TileNodeHarvester {

		private final long energyPerCycle;
		protected final long maxEnergy;
		private final long maxFlowRate;
		private final long standbyCost;

		private long energy;

		protected TileNodeHarvesterBasicEnergy(long c, long m, long f, long s) {
			energyPerCycle = c;
			maxEnergy = m;
			maxFlowRate = f;
			standbyCost = s;
			if (maxFlowRate*Purity.PURE.getCountdown() < energyPerCycle*this.getSpeedFactor())
				throw new RegistrationException(Satisforestry.instance, this+" max energy flow rate ("+maxFlowRate+"/"+energyPerCycle+") too low to sustain continuous operation!");
		}

		@Override
		protected final boolean hasEnergy(boolean operation) {
			return energy >= this.getActualOperationCost(operation);
		}

		@Override
		protected final void useEnergy(boolean operation) {
			energy -= this.getActualOperationCost(operation);
		}

		protected final long addEnergy(long amt, boolean simulate) {
			long space = maxEnergy-energy;
			long add = Math.min(this.getActualMaxFlow(), Math.min(amt, space));
			if (!simulate)
				energy += add;
			return add;
		}

		protected final long getActualMaxFlow() {
			return (long)(maxFlowRate*(1+this.getTier())*this.getOverclockingPowerFactor());
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

		@Override
		public final ArrayList getMessages(World world, int x, int y, int z, int side) {
			ArrayList<String> li = new ArrayList();
			li.add("Contains "+energy+" of "+maxEnergy+" "+this.getEnergyUnit());
			return li;
		}

		protected abstract String getEnergyUnit();

		@Override
		protected final float getOperationEnergyFraction() {
			return Math.min(1, energy/(float)this.getActualOperationCost(true));
		}

		protected final long getActualOperationCost(boolean operation) {
			return (long)((operation ? energyPerCycle : standbyCost)*this.getOverclockingPowerFactor());
		}

		protected final float getOverclockingPowerFactor() {
			return (float)ReikaMathLibrary.roundToNearestFraction(Math.pow(this.getOverclockingLevel(), 1.82), 0.25); //SF uses 1.6
		}

	}

	public static class TileNodeHarvesterRF extends TileNodeHarvesterBasicEnergy implements IEnergyReceiver {

		public TileNodeHarvesterRF() {
			super(20000, 600000, 2500, 100);
		}

		@Override
		protected String getTEName() {
			return "Resource Node Harvester (RF)";
		}

		@Override
		public boolean canConnectEnergy(ForgeDirection from) {
			return false;
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

		@Override
		public float getSpeedFactor() {
			return 0.4F;
		}

		@Override
		protected String getEnergyUnit() {
			return "RF";
		}

		@Override
		public TileMinerConnection getInput() {
			return this.getWirePowerConnection();
		}

	}

	@Strippable(value={"ic2.api.energy.tile.IEnergySink"})
	public static class TileNodeHarvesterEU extends TileNodeHarvesterBasicEnergy implements IEnergySink {

		public TileNodeHarvesterEU() {
			super(1536, 6144, 256, 4);
		}

		@Override
		protected String getTEName() {
			return "Resource Node Harvester (EU)";
		}

		@Override
		@ModDependent(ModList.IC2)
		public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) {
			return false;
		}

		@Override
		@ModDependent(ModList.IC2)
		public double getDemandedEnergy() {
			return this.addEnergy(Integer.MAX_VALUE, true);
		}

		@Override
		@ModDependent(ModList.IC2)
		public int getSinkTier() {
			return ReikaEUHelper.getIC2TierFromEUVoltage(this.getActualMaxFlow());
		}

		@Override
		@ModDependent(ModList.IC2)
		public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage) {
			long add = this.addEnergy((long)amount, false);
			return amount-add;
		}

		@Override
		public float getSpeedFactor() {
			return 0.8F;
		}

		@Override
		protected String getEnergyUnit() {
			return "EU";
		}

		@Override
		public TileMinerConnection getInput() {
			return this.getWirePowerConnection();
		}
	}


	public static class TileNodeHarvesterRC extends TileNodeHarvester implements ShaftPowerReceiver {

		private static final int MINPOWER_BASE = 262144;
		private static final int STANDBY_BASE = 4096;
		public static final int MINTORQUE = 2048;

		private int torque;
		private int omega;
		private long power;
		private int iotick;

		@Override
		public void updateEntity(World world, int x, int y, int z, int meta) {
			TileEntity te = this.getShaftPowerConnection();
			if (!PowerTransferHelper.checkPowerFrom(te, ForgeDirection.DOWN) && !PowerTransferHelper.checkPowerFrom(te, ForgeDirection.UP)) {
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
		protected boolean hasEnergy(boolean operation) {
			return operation ? power >= this.getMinPowerCost() && torque >= MINTORQUE && omega >= this.getMinSpeed() : (power >= this.getStandbyPowerCost());
		}

		private long getStandbyPowerCost() {
			return STANDBY_BASE*this.getPowerScalar();
		}

		private int getMinSpeed() {
			return (int)(this.getMinPowerCost()/MINTORQUE);
		}

		private long getMinPowerCost() {
			return MINPOWER_BASE*this.getPowerScalar();
		}

		private int getPowerScalar() {
			return ReikaMathLibrary.intpow2(2, this.getOverclockingStep());
		}

		@Override
		public float getOperationEnergyFraction() {
			return ReikaMathLibrary.multiMin(1F, power/(float)this.getMinPowerCost(), torque/(float)MINTORQUE, omega/(float)this.getMinSpeed());
		}

		@Override
		protected void useEnergy(boolean operation) {

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
			return false;
		}

		@Override
		public boolean isReceiving() {
			return false;
		}

		@Override
		public float getSpeedFactor() {
			return 1F;
		}

		@Override
		public final ArrayList getMessages(World world, int x, int y, int z, int side) {
			ArrayList<String> li = new ArrayList();
			String pre = ReikaEngLibrary.getSIPrefix(this.getPower());
			double base = ReikaMathLibrary.getThousandBase(this.getPower());
			li.add(String.format("%s receiving %.3f %sW @ %d rad/s.", this.getName(), base, pre, this.getOmega()));
			return li;
		}

		@Override
		public TileMinerConnection getInput() {
			return this.getShaftPowerConnection();
		}

	}

	static { //Validate values
		new TileNodeHarvesterRF();
		new TileNodeHarvesterEU();
		new TileNodeHarvesterRC();
	}

}
