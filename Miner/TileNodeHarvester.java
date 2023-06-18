package Reika.Satisforestry.Miner;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.APIStripper.Strippable;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Exception.RegistrationException;
import Reika.DragonAPI.Exception.UnreachableCodeException;
import Reika.DragonAPI.Libraries.ReikaAABBHelper;
import Reika.DragonAPI.Libraries.ReikaDirectionHelper;
import Reika.DragonAPI.Libraries.ReikaInventoryHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaEngLibrary;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.ModInteract.ItemHandlers.IC2Handler;
import Reika.DragonAPI.ModInteract.Power.ReikaEUHelper;
import Reika.RotaryCraft.API.ItemFetcher;
import Reika.RotaryCraft.API.Power.PowerTransferHelper;
import Reika.RotaryCraft.API.Power.ShaftPowerReceiver;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockMinerMulti.TileMinerConveyorPort;
import Reika.Satisforestry.Blocks.BlockResourceNode.TileResourceNode;
import Reika.Satisforestry.Blocks.BlockSFMultiBase.TileMinerConnection;
import Reika.Satisforestry.Blocks.BlockSFMultiBase.TilePowerConnection;
import Reika.Satisforestry.Blocks.BlockSFMultiBase.TileShaftConnection;
import Reika.Satisforestry.Config.NodeResource.Purity;
import Reika.Satisforestry.Registry.SFBlocks;
import Reika.Satisforestry.Registry.SFSounds;

import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ic2.api.energy.tile.IEnergySink;


public abstract class TileNodeHarvester extends TileResourceHarvesterBase<TileResourceNode, ForgeDirection> {

	private static final double MAX_DRILL_SPEED = 20;

	private SpoolingStates spoolState = SpoolingStates.IDLE;
	private int spoolTime;

	private ForgeDirection structureDir = null;

	@SideOnly(Side.CLIENT)
	private double lastDrillSpinAngle;
	@SideOnly(Side.CLIENT)
	private double drillSpinAngle;
	@SideOnly(Side.CLIENT)
	private double lastDrillYPos;
	@SideOnly(Side.CLIENT)
	private double drillYPos;

	public static enum SpoolingStates {
		IDLE(2),
		LOCKING(5),
		LOWER1(15, 40),
		SPINUP(160),
		LOWER2(50, 40),
		ACTIVE(5);

		public final int duration;
		public final int durationReverse;

		private static final SpoolingStates[] list = values();

		private SpoolingStates(int d) {
			this(d, d);
		}

		private SpoolingStates(int d, int r) {
			duration = d;
			durationReverse = r;
		}

		public void playEntrySound(TileNodeHarvester te, SpoolingStates last) {
			switch(this) {
				case LOCKING:
					break;
				case LOWER1:
					if (last == LOCKING)
						SFSounds.DRILLLOCK.playSoundNoAttenuation(te.worldObj, te.xCoord+0.5, te.yCoord+0.5, te.zCoord+0.5, 1, 1, 64);
					break;
				case SPINUP:
					if (last == LOWER1)
						SFSounds.DRILLSPINUP.playSoundNoAttenuation(te.worldObj, te.xCoord+0.5, te.yCoord+0.5, te.zCoord+0.5, 1, 1, 64);
					else if (last == LOWER2)
						SFSounds.DRILLSPINDOWN.playSoundNoAttenuation(te.worldObj, te.xCoord+0.5, te.yCoord+0.5, te.zCoord+0.5, 1, 1, 64);
					break;
				default:
					break;
			}
		}
	}

	public TileNodeHarvester() {
		super();
	}

	@Override
	public final Block getTileEntityBlockID() {
		return SFBlocks.HARVESTER.getBlockInstance();
	}

	private void rampSpool(boolean up) {
		SpoolingStates last = spoolState;
		int lastTime = spoolTime;
		if (up) {
			spoolTime++;
			if (spoolTime > spoolState.duration) {
				if (spoolState == SpoolingStates.ACTIVE) {
					spoolTime = spoolState.duration;
				}
				else {
					spoolState = SpoolingStates.list[last.ordinal()+1];
					spoolTime = 0;
				}
			}
		}
		else if ((spoolState != SpoolingStates.ACTIVE && spoolState != SpoolingStates.LOWER2) || runSoundTick < 50) {
			spoolTime--;
			if (spoolTime < 0) {
				if (spoolState == SpoolingStates.IDLE) {
					spoolTime = 0;
				}
				else {
					spoolState = SpoolingStates.list[last.ordinal()-1];
					spoolTime = spoolState.durationReverse;
				}
			}
		}

		if (last != spoolState)
			spoolState.playEntrySound(this, last);
		if (last != spoolState || lastTime != spoolTime)
			this.syncAllData(false);
	}

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		super.updateEntity(world, x, y, z, meta);
		if (world.isRemote) {
			lastDrillSpinAngle = drillSpinAngle;
			drillSpinAngle -= this.getDrillSpeed(MAX_DRILL_SPEED);
			lastDrillYPos = drillYPos;
			drillYPos = this.getDrillVerticalOffsetScale(0.75, 2, !isActiveThisTick);
		}
		else {
			if (spoolState.ordinal() > SpoolingStates.SPINUP.ordinal()) {
				if (runSoundTick > 0) {
					runSoundTick--;
				}
				else {
					SFSounds.DRILLRUN.playSoundNoAttenuation(worldObj, xCoord+0.5, yCoord+0.5, zCoord+0.5, 1, 1, 64);
					runSoundTick = 148;
				}
			}
			else {
				runSoundTick = 0;
			}
			this.rampSpool(isActiveThisTick);
		}
	}

	@Override
	protected void doOperationCycle(TileResourceNode te) {
		ItemStack is = te.getRandomNodeItem(false);
		if (is != null) {
			if (this.trySpawnItem(is)) {
				operationTimer = 0;
				this.useEnergy(true);
			}
			else {
				state = MachineState.WAITING;
			}
			//ReikaJavaLibrary.pConsole(world.getTotalWorldTime());
		}
	}

	@Override
	public boolean isShutdown(World world, int x, int y, int z) {
		if (super.isShutdown(world, x, y, z))
			return true;
		TileMinerConnection te = this.getOutput();
		if (te != null && te.hasRedstone())
			return true;
		if (te != null && world.isBlockIndirectlyGettingPowered(te.xCoord, te.yCoord-1, te.zCoord))
			return true;
		return false;
	}

	@SideOnly(Side.CLIENT)
	private double getDrillVerticalOffsetScale(double phase1L, double phase2L, boolean rev) {
		if (!this.isInWorld())
			return 0;
		switch(spoolState) {
			case IDLE:
			case LOCKING:
				return 0;
			case LOWER1:
				return phase1L*spoolTime/(rev ? spoolState.durationReverse : spoolState.duration);
			case SPINUP:
				return phase1L;
			case LOWER2:
				return phase1L+phase2L*spoolTime/(rev ? spoolState.durationReverse : spoolState.duration);
			case ACTIVE:
				return phase1L+phase2L;
		}
		throw new UnreachableCodeException("Spool state was not a defined value");
	}

	@SideOnly(Side.CLIENT)
	private double getDrillSpeed(double max) {
		if (spoolState.ordinal() < SpoolingStates.SPINUP.ordinal()) {
			return 0;
		}
		else if (spoolState.ordinal() == SpoolingStates.SPINUP.ordinal()) {
			return max*spoolTime/spoolState.duration;
		}
		else if (spoolState.ordinal() > SpoolingStates.SPINUP.ordinal()) {
			return max;
		}
		else {
			throw new IllegalStateException("Spoolstate was neither less, equal, or greater than spinup?!");
		}
	}

	@SideOnly(Side.CLIENT)
	public double getDrillHeight(float ptick) {
		return ReikaMathLibrary.linterpolate(ptick, 0, 1, lastDrillYPos, drillYPos);
	}

	@SideOnly(Side.CLIENT)
	public double getDrillAngle(float ptick) {
		return ReikaMathLibrary.linterpolate(ptick, 0, 1, lastDrillSpinAngle, drillSpinAngle);
	}

	@Override
	protected boolean isReady(TileResourceNode te) {
		return spoolState == SpoolingStates.ACTIVE && spoolTime == spoolState.duration;
	}

	@Override
	public TileResourceNode getResourceNode() {
		TileEntity te = this.getAdjacentTileEntity(ForgeDirection.DOWN);
		return te instanceof TileResourceNode ? (TileResourceNode)te : null;
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

	@Override
	protected void doActivityFX(World world, int x, int y, int z) {

	}

	@Override
	public final boolean hasStructure() {
		return structureDir != null;
	}

	public final ForgeDirection getDirection() {
		if (!this.isInWorld())
			return ForgeDirection.WEST;
		return structureDir;
	}

	@Override
	protected final TilePowerConnection getWirePowerConnection() {
		if (!this.hasStructure())
			return null;
		ForgeDirection right = ReikaDirectionHelper.getRightBy90(structureDir);
		return (TilePowerConnection)worldObj.getTileEntity(xCoord+right.offsetX*2, yCoord+13, zCoord+right.offsetZ*2);
	}

	@Override
	protected final TileShaftConnection getShaftPowerConnection() {
		return this.hasStructure() ? (TileShaftConnection)worldObj.getTileEntity(xCoord-structureDir.offsetX, yCoord+12, zCoord-structureDir.offsetZ) : null;
	}

	public final TileMinerConveyorPort getOutput() {
		return this.hasStructure() ? (TileMinerConveyorPort)worldObj.getTileEntity(xCoord+structureDir.offsetX*8, yCoord+1, zCoord+structureDir.offsetZ*8) : null;
	}

	@Override
	protected void writeSyncTag(NBTTagCompound NBT) {
		super.writeSyncTag(NBT);

		NBT.setInteger("structure", structureDir != null ? structureDir.ordinal() : -1);

		NBT.setInteger("spool", spoolTime);
		NBT.setInteger("spoolState", spoolState.ordinal());
	}

	@Override
	protected void readSyncTag(NBTTagCompound NBT) {
		super.readSyncTag(NBT);

		int struct = NBT.getInteger("structure");
		structureDir = struct == -1 ? null : dirs[struct];

		spoolTime = NBT.getInteger("spool");
		spoolState = SpoolingStates.list[NBT.getInteger("spoolState")];
	}

	@Override
	protected void animateWithTick(World world, int x, int y, int z) {

	}

	@Override
	protected void updateStructureState(ForgeDirection flag) {
		boolean change = (structureDir == null && flag != null) || (structureDir != null && flag == null);
		if (change)
			MinerStructure.toggleRSLamps(this, flag != null ? flag : structureDir, flag == null);
		structureDir = flag;
	}

	@Override
	protected void onUpdateInputs(boolean has) {
		TileMinerConnection te = this.getOutput();
		if (te != null)
			te.connectTo(has ? this : null);
	}

	@Override
	public final AxisAlignedBB getRenderBoundingBox() {
		ForgeDirection dir = structureDir;
		if (forceRenderer)
			dir = ForgeDirection.EAST;
		if (dir == null)
			return super.getRenderBoundingBox();
		AxisAlignedBB box = ReikaAABBHelper.getBlockAABB(this);
		box = box.addCoord(dir.offsetX*8.25, 13, dir.offsetZ*8.25);
		box = box.addCoord(-dir.offsetX*3.5, 0, -dir.offsetZ*3.5);
		box = box.addCoord(dir.offsetZ*2, 0, dir.offsetX*2);
		box = box.addCoord(-dir.offsetZ*2, 0, -dir.offsetX*2);
		box = box.expand(0.25, 0.25, 0.25);
		return box;
	}

	private static abstract class TileNodeHarvesterBasicEnergy extends TileNodeHarvester {

		private final long energyPerCycle;
		protected final long maxEnergy;
		private final long maxFlowRate;
		private final long standbyCost;

		private long energy;

		protected TileNodeHarvesterBasicEnergy(long c, long m, long f, long s) {
			super();
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
			return (long)(maxFlowRate*(1+this.getTier())*this.getOverclockingPowerFactor(false));
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
		public final String getPowerType() {
			return this.getEnergyUnit();
		}

		@Override
		protected final float getOperationEnergyFraction() {
			return Math.min(1, energy/(float)this.getActualOperationCost(true));
		}

		protected final long getActualOperationCost(boolean operation) {
			return (long)((operation ? energyPerCycle : standbyCost)*this.getOverclockingPowerFactor(false));
		}

		protected final float getOverclockingPowerFactor(boolean display) {
			return (float)ReikaMathLibrary.roundToNearestFraction(Math.pow(this.getOverclockingLevel(display), 1.82), 0.25); //SF uses 1.6
		}

		@Override
		public final String getOperationPowerCost(boolean withOverclock) {
			double amt = energyPerCycle;
			if (withOverclock)
				amt *= this.getOverclockingPowerFactor(true);
			return String.format("%.3f k%s", amt/1000D, this.getEnergyUnit());
		}

	}

	public static class TileNodeHarvesterRF extends TileNodeHarvesterBasicEnergy implements IEnergyReceiver {

		public TileNodeHarvesterRF() {
			super(2000, 600000, 2500, 100);
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

		@Override
		public ItemStack getOverclockingItem() {
			return ReikaItemHelper.lookupItem("ThermalExpansion:augment:129"); //Overclocked Modular Gearbox = T2 speed booster
		}

	}

	@Strippable(value={"ic2.api.energy.tile.IEnergySink"})
	public static class TileNodeHarvesterEU extends TileNodeHarvesterBasicEnergy implements IEnergySink {

		public TileNodeHarvesterEU() {
			super(384, 6144, 512, 4);
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

		@Override
		public ItemStack getOverclockingItem() {
			return IC2Handler.IC2Stacks.OVERCLOCK.getItem();
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
			return operation ? power >= this.getMinPowerCost(false) && torque >= MINTORQUE && omega >= this.getMinSpeed() : (power >= this.getStandbyPowerCost());
		}

		private long getStandbyPowerCost() {
			return STANDBY_BASE*this.getPowerScalar(false);
		}

		private int getMinSpeed() {
			return (int)(this.getMinPowerCost(false)/MINTORQUE);
		}

		private long getMinPowerCost(boolean display) {
			return MINPOWER_BASE*this.getPowerScalar(display);
		}

		private int getPowerScalar(boolean display) {
			return ReikaMathLibrary.intpow2(2, this.getOverclockingStep(display));
		}

		@Override
		public final String getOperationPowerCost(boolean withOverclock) {
			double amt = withOverclock ? this.getMinPowerCost(true) : MINPOWER_BASE;
			String unit = ReikaEngLibrary.getSIPrefix(amt);
			double base = ReikaMathLibrary.getThousandBase(amt);
			return String.format("%.3f %sW", base, unit);
		}

		@Override
		public float getOperationEnergyFraction() {
			return ReikaMathLibrary.multiMin(1F, power/(float)this.getMinPowerCost(false), torque/(float)MINTORQUE, omega/(float)this.getMinSpeed());
		}

		@Override
		protected void useEnergy(boolean operation) {

		}

		@Override
		protected String getTEName() {
			return "Resource Node Harvester (Shaft)";
		}

		@Override
		public final String getPowerType() {
			return "RotaryCraft shaft";
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

		@Override
		public ItemStack getOverclockingItem() {
			return new ItemStack(ItemFetcher.getItemByOrdinal(55), 1, 2); //t2 magneto upgrade
		}

	}

	static { //Validate values
		new TileNodeHarvesterRF();
		new TileNodeHarvesterEU();
		new TileNodeHarvesterRC();
	}

}
