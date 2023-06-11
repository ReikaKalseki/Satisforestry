package Reika.Satisforestry.Miner;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.APIStripper.Strippable;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Exception.RegistrationException;
import Reika.DragonAPI.Extras.IconPrefabs;
import Reika.DragonAPI.Instantiable.HybridTank;
import Reika.DragonAPI.Instantiable.Effects.EntityBlurFX;
import Reika.DragonAPI.Libraries.ReikaAABBHelper;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaEngLibrary;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.Libraries.Rendering.ReikaColorAPI;
import Reika.DragonAPI.Libraries.Rendering.ReikaRenderHelper;
import Reika.DragonAPI.ModInteract.ItemHandlers.IC2Handler;
import Reika.DragonAPI.ModInteract.Power.ReikaEUHelper;
import Reika.RotaryCraft.API.ItemFetcher;
import Reika.RotaryCraft.API.Power.PowerTransferHelper;
import Reika.RotaryCraft.API.Power.ShaftPowerReceiver;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockFrackerMulti.TileFrackerFluidPort;
import Reika.Satisforestry.Blocks.BlockFrackingNode.TileFrackingNode;
import Reika.Satisforestry.Blocks.BlockSFMultiBase.TileMinerConnection;
import Reika.Satisforestry.Blocks.BlockSFMultiBase.TilePowerConnection;
import Reika.Satisforestry.Blocks.BlockSFMultiBase.TileShaftConnection;
import Reika.Satisforestry.Config.NodeResource.Purity;
import Reika.Satisforestry.Config.ResourceFluid;
import Reika.Satisforestry.Registry.SFBlocks;
import Reika.Satisforestry.Registry.SFSounds;

import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ic2.api.energy.tile.IEnergySink;


public abstract class TileFrackingPressurizer extends TileResourceHarvesterBase<TileFrackingNode, Boolean> {

	private boolean structure;

	private final HybridTank inputFluid = new HybridTank("frackingin", 4000);

	public final Thumper thumper1 = new Thumper(0);
	public final Thumper thumper2 = new Thumper(1);
	public final Thumper thumper3 = new Thumper(2);
	public final Thumper thumper4 = new Thumper(3);

	private boolean activeState;
	public double ventExtension = 0;

	public TileFrackingPressurizer() {
		super();
	}

	@Override
	public final Block getTileEntityBlockID() {
		return SFBlocks.FRACKER.getBlockInstance();
	}

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		super.updateEntity(world, x, y, z, meta);
		if (world.isRemote) {
			boolean recover = thumper1.update(activeState) == ThumperStage.RECOVERING;
			recover |= thumper2.update(activeState) == ThumperStage.RECOVERING;
			recover |= thumper3.update(activeState) == ThumperStage.RECOVERING;
			recover |= thumper4.update(activeState) == ThumperStage.RECOVERING;
			if (recover && activeState) {
				ventExtension = Math.min(ventExtension+0.2, 1);
				this.spawnVentParticles();
			}
			else {
				ventExtension = Math.max(ventExtension-0.1, 0);
			}
		}
		else {
			activeState = this.hasStructure() && this.getState() == MachineState.OPERATING;
		}
	}

	@SideOnly(Side.CLIENT)
	private void spawnVentParticles() {
		double vv = 0.0625;
		double r = 4.25;
		for (int i = 0; i < 2; i++) {
			double vx = 0;
			double vy = vv;
			double vz = 0;
			double px = 0;
			double py = 5.25;
			double pz = 0;
			switch(rand.nextInt(4)) {
				case 0:
					px = r;
					vx = vv;
					break;
				case 1:
					px = -r;
					vx = -vv;
					break;
				case 2:
					pz = r;
					vz = vv;
					break;
				case 3:
					pz = -r;
					vz = -vv;
					break;
			}
			vx = ReikaRandomHelper.getRandomPlusMinus(vx, 0.015);
			vy = ReikaRandomHelper.getRandomPlusMinus(vy, 0.015);
			vz = ReikaRandomHelper.getRandomPlusMinus(vz, 0.015);
			px = ReikaRandomHelper.getRandomPlusMinus(px, 0.125);
			py = ReikaRandomHelper.getRandomPlusMinus(py, 0.125);
			pz = ReikaRandomHelper.getRandomPlusMinus(pz, 0.125);
			EntityBlurFX fx = new EntityBlurFX(worldObj, px+xCoord+0.5, py+yCoord+0.5, pz+zCoord+0.5, vx, vy, vz, IconPrefabs.FADE_BASICBLEND.getIcon());
			fx.setBasicBlend().setAlphaFading();
			TileFrackingNode tf = this.getResourceNode();
			if (tf != null)
				fx.setColor(ReikaColorAPI.mixColors(tf.getResource().color, 0xffffff, rand.nextFloat()));
			fx.setLife(ReikaRandomHelper.getRandomBetween(20, 80)).setScale((float)ReikaRandomHelper.getRandomBetween(5D, 12D));
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}
	}

	public class Thumper { //worth noting that SF fracker has a lot of random in the "wait" times

		public static final double CYCLE_TIME = 100; //5s

		public final int index;
		public final double phaseOffset;

		private double position;
		private double lastPosition;

		private long tick;

		private ThumperStage stage = ThumperStage.RISING;

		private Thumper(int idx) {
			index = idx;
			phaseOffset = 0.0333*idx;
		}

		private ThumperStage update(boolean active) {
			lastPosition = position;
			if (!active) {
				tick = 0;
				position = Math.max(0, position-0.04);
				return ThumperStage.RECOVERING;
			}
			tick++;
			double frac = (phaseOffset+(tick%CYCLE_TIME)/CYCLE_TIME)%1D;
			ThumperStage put = this.calcStage(frac);
			//ReikaJavaLibrary.pConsole(tick+"+"+phaseOffset+">"+frac+">"+put);
			if (put != stage) {
				this.setStage(put);
			}
			switch(stage) {
				case RISING:
					position += 0.01667;
					break;
				case DROPPING:
					position -= 0.2;
					break;
				case PAUSE:
					break;
				case RECOVERING:
					break;
			}
			return put;
		}

		private void setStage(ThumperStage put) {
			stage = put;
			switch(stage) {
				case RISING: {
					float p = (float)ReikaRandomHelper.getRandomBetween(0.75, 1.25);
					float v = (float)ReikaRandomHelper.getRandomBetween(0.5F, 0.67F);
					if (Minecraft.getMinecraft().thePlayer.getDistanceSq(xCoord, yCoord, zCoord) <= 2000)
						ReikaSoundHelper.playClientSound(SFSounds.FRACKHISS, TileFrackingPressurizer.this, v, p, false);
					break;
				}
				case PAUSE:
					position = 1;
					break;
				case DROPPING:
					break;
				case RECOVERING:
					float p = (float)ReikaRandomHelper.getRandomBetween(0.95, 1.03);
					float v = (float)ReikaRandomHelper.getRandomBetween(0.8, 1.5);
					if (Minecraft.getMinecraft().thePlayer.getDistanceSq(xCoord, yCoord, zCoord) <= 2000)
						ReikaSoundHelper.playClientSound(SFSounds.FRACKTHUMP, TileFrackingPressurizer.this, v, p, false);
					ReikaRenderHelper.rockScreen(20);
					position = 0;
					break;
			}
		}

		private ThumperStage calcStage(double frac) {
			if (frac < 0.6) { //3s
				return ThumperStage.RISING;
			}
			else if (frac < 0.75) { //3.75s
				return ThumperStage.PAUSE;
			}
			else if (frac < 0.8) { //4s
				return ThumperStage.DROPPING;
			}
			else { //remaining 1s
				return ThumperStage.RECOVERING;
			}
		}

		public double getRenderPosition() {
			return ReikaMathLibrary.linterpolate(ReikaRenderHelper.getPartialTickTime(), 0, 1, lastPosition, position);
		}
	}

	private static enum ThumperStage {
		RISING,
		PAUSE,
		DROPPING,
		RECOVERING;
	}

	@Override
	protected void doOperationCycle(TileFrackingNode te) {
		te.pressurize(this.getOverclockingLevel(false));
	}

	@Override
	protected void doActivityFX(World world, int x, int y, int z) {

	}

	@Override
	protected boolean isReady(TileFrackingNode te) {
		ResourceFluid rf = te.getResource();
		return rf.requiredInput == null || rf.requiredInputAmount <= inputFluid.getLevel() && rf.requiredInput == inputFluid.getActualFluid();
	}

	@Override
	public final boolean hasStructure() {
		return structure;
	}

	@Override
	protected final TilePowerConnection getWirePowerConnection() {
		return this.hasStructure() ? (TilePowerConnection)worldObj.getTileEntity(xCoord-2, yCoord+11, zCoord-2) : null;
	}

	@Override
	protected final TileShaftConnection getShaftPowerConnection() {
		return this.hasStructure() ? (TileShaftConnection)worldObj.getTileEntity(xCoord+1, yCoord+9, zCoord+1) : null;
	}

	protected final TileFrackerFluidPort[] getInputs() {
		if (!this.hasStructure())
			return null;
		return new TileFrackerFluidPort[] {
				(TileFrackerFluidPort)worldObj.getTileEntity(xCoord+2, yCoord+1, zCoord),
				(TileFrackerFluidPort)worldObj.getTileEntity(xCoord-2, yCoord+1, zCoord),
				(TileFrackerFluidPort)worldObj.getTileEntity(xCoord, yCoord+1, zCoord+2),
				(TileFrackerFluidPort)worldObj.getTileEntity(xCoord, yCoord+1, zCoord-2),
		};
	}

	@Override
	protected void onUpdateInputs(boolean has) {
		TileFrackerFluidPort[] arr = this.getInputs();
		if (arr != null) {
			for (TileFrackerFluidPort te : arr)
				te.connectTo(has ? this : null);
		}
	}

	@Override
	protected void writeSyncTag(NBTTagCompound NBT) {
		super.writeSyncTag(NBT);

		NBT.setBoolean("struct", structure);
		NBT.setBoolean("active", activeState);

		inputFluid.writeToNBT(NBT);
	}

	@Override
	protected void readSyncTag(NBTTagCompound NBT) {
		super.readSyncTag(NBT);

		structure = NBT.getBoolean("struct");
		activeState = NBT.getBoolean("active");

		inputFluid.readFromNBT(NBT);
	}

	@Override
	protected void animateWithTick(World world, int x, int y, int z) {

	}

	@Override
	protected void updateStructureState(Boolean flag) {
		boolean put = flag != null && flag.booleanValue();
		if (put != structure)
			FrackerStructure.toggleRSLamps(this, put);
		structure = put;
		if (!structure)
			inputFluid.empty();
	}

	@Override
	public final AxisAlignedBB getRenderBoundingBox() {
		AxisAlignedBB box = ReikaAABBHelper.getBlockAABB(this);
		if (this.hasStructure() || forceRenderer) {
			box = box.expand(5, 8, 5).offset(0, 6, 0);
		}
		return box;
	}

	@Override
	public TileFrackingNode getResourceNode() {
		TileEntity te = this.getAdjacentTileEntity(ForgeDirection.DOWN);
		return te instanceof TileFrackingNode ? (TileFrackingNode)te : null;
	}

	@Override
	protected float computeProgressFactor(int stepTime) {
		TileFrackingNode te = this.getResourceNode();
		return te == null ? 0 : te.getPressure();
	}

	public final boolean canAccept(Fluid f) {
		TileFrackingNode te = this.getResourceNode();
		if (te == null)
			return false;
		ResourceFluid rf = te.getResource();
		return f != null && f == rf.requiredInput;
	}

	public final int addFrackingFluid(FluidStack resource, boolean doFill) {
		return this.canAccept(resource.getFluid()) ? inputFluid.fill(resource, doFill) : 0;
	}

	private static abstract class TileFrackingPressurizerBasicEnergy extends TileFrackingPressurizer {

		private final long energyPerCycle;
		protected final long maxEnergy;
		private final long maxFlowRate;
		private final long standbyCost;

		private long energy;

		protected TileFrackingPressurizerBasicEnergy(long c, long m, long f, long s) {
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

	public static class TileFrackingPressurizerRF extends TileFrackingPressurizerBasicEnergy implements IEnergyReceiver {

		public TileFrackingPressurizerRF() {
			super(12000, 600000, 16000, 300);
		}

		@Override
		protected String getTEName() {
			return "Fracking Node Pressurizer (RF)";
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
	public static class TileFrackingPressurizerEU extends TileFrackingPressurizerBasicEnergy implements IEnergySink {

		public TileFrackingPressurizerEU() {
			super(2048, 6144, 3072, 8);
		}

		@Override
		protected String getTEName() {
			return "Fracking Node Pressurizer (EU)";
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


	public static class TileFrackingPressurizerRC extends TileFrackingPressurizer implements ShaftPowerReceiver {

		private static final int MINPOWER_BASE = 524288;
		private static final int STANDBY_BASE = 16384;
		public static final int MINTORQUE = 8192;

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
			return "Fracking Node Pressurizer (Shaft)";
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
		new TileFrackingPressurizerRF();
		new TileFrackingPressurizerEU();
		new TileFrackingPressurizerRC();
	}

}
