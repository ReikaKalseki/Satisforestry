package Reika.Satisforestry.Miner;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;

import Reika.DragonAPI.Auxiliary.ChunkManager;
import Reika.DragonAPI.Base.TileEntityBase;
import Reika.DragonAPI.Instantiable.Rendering.StructureRenderer;
import Reika.DragonAPI.Interfaces.TileEntity.ChunkLoadingTile;
import Reika.Satisforestry.Blocks.BlockResourceNode.ResourceNode;
import Reika.Satisforestry.Blocks.BlockSFMultiBase.TileMinerConnection;
import Reika.Satisforestry.Blocks.BlockSFMultiBase.TilePowerConnection;
import Reika.Satisforestry.Blocks.BlockSFMultiBase.TileShaftConnection;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public abstract class TileResourceHarvesterBase<N extends ResourceNode, S> extends TileEntityBase implements ChunkLoadingTile {

	private int tier = 0;
	protected int activityTimer = 0;
	protected int operationTimer = 0;

	protected MachineState state = MachineState.ERRORED;

	public float progressFactor;
	public float powerBar;
	public int overclockDisplay;

	private OverclockingInv overclock;

	protected int runSoundTick;

	protected boolean isActiveThisTick;

	public boolean forceRenderer;

	public static enum MachineState {
		INACTIVE(0xFF6060),
		OPERATING(0x99FF32),
		WAITING(0xFFFF40),
		ERRORED(0x8370FF);

		public final int color;

		private static final MachineState[] list = values();

		private MachineState(int c) {
			color = c;
		}
	}

	public TileResourceHarvesterBase() {
		overclock = new OverclockingInv(this);
	}

	public final MachineState getState() {
		return state;
	}

	@SideOnly(Side.CLIENT)
	public final int getLightbarColorForRender() {
		if (StructureRenderer.isRenderingTiles()) {
			int idx = (int)((System.currentTimeMillis()/1000)%(MachineState.list.length+1));
			return idx == MachineState.list.length ? 0xb0d0ff : MachineState.list[idx].color;
		}
		int c = state.color;
		if (state == MachineState.OPERATING && this.getOverclockingStep(true) > 0) {
			//float f = 0.5F+(float)(0.5*Math.sin(this.getTicksExisted()*0.004));
			//c = ReikaColorAPI.mixColors(c, 0xffffff, f);
			c = 0xb0d0ff;
		}
		return c;
	}

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		if (world.isRemote) {
			overclockDisplay = this.getOverclockingStep(false);
			if (activityTimer > 0) {
				this.doActivityFX(world, x, y, z);
			}
		}
		else {
			int stepTime = 0;
			isActiveThisTick = false;
			N te = this.getResourceNode();
			state = MachineState.INACTIVE;
			if (this.hasStructure() && te != null && !this.isShutdown(world, x, y, z)) {
				if (this.hasEnergy(false)) {
					isActiveThisTick = true;
					boolean work = false;
					if (this.isReady(te)) {
						stepTime = (int)(te.getHarvestInterval()/this.getNetSpeedFactor(false));
						if (this.hasEnergy(true)) {
							work = true;
							state = MachineState.OPERATING;
							if (operationTimer < stepTime)
								operationTimer++;
							if (activityTimer == 0) {
								ChunkManager.instance.loadChunks(this);
							}
							activityTimer = 20;
							if (operationTimer >= stepTime) {
								this.doOperationCycle(te);
							}
						}
						else {
							operationTimer = 0;
							state = MachineState.WAITING;
						}
						//ReikaJavaLibrary.pConsole(operationTimer);
						//ReikaJavaLibrary.pConsole(powerBar);
						//ReikaJavaLibrary.pConsole("F="+progressFactor);
					}
					else {
						operationTimer = 0;
					}
					this.useEnergy(work);
				}
			}
			if (te == null || !this.isReady(te))
				operationTimer = 0;
			progressFactor = this.computeProgressFactor(stepTime);
			powerBar = this.getOperationEnergyFraction();
			if (activityTimer > 0) {
				activityTimer--;
				if (activityTimer == 0)
					this.unload();
			}
		}
	}

	protected float computeProgressFactor(int stepTime) {
		return stepTime <= 0 ? 0 : operationTimer/(float)stepTime;
	}

	protected abstract void doOperationCycle(N te);

	protected abstract boolean isReady(N te);

	public boolean isShutdown(World world, int x, int y, int z) {
		if (this.hasRedstoneSignal())
			return true;
		TileMinerConnection te = this.getInput();
		if (te != null && te.hasRedstone())
			return true;
		return false;
	}

	public final float getNetSpeedFactor(boolean display) {
		return (1+this.getTier())*this.getOverclockingLevel(display)*this.getSpeedFactor();
	}

	protected void doActivityFX(World world, int x, int y, int z) {

	}

	public final int getTier() {
		return tier;
	}

	public abstract boolean hasStructure();

	public final int getMineProgressScaled(int px) {
		return (int)(px*progressFactor);
	}

	public final int getOverclockingStep(boolean display) {
		return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && display ? overclockDisplay : overclock.getOverclockingLevel();
	}

	public final float getOverclockingLevel(boolean display) {
		return 1+this.getOverclockingStep(display)*0.5F;
	}

	public abstract float getSpeedFactor();

	protected abstract float getOperationEnergyFraction();

	protected abstract boolean hasEnergy(boolean operation);

	protected abstract void useEnergy(boolean operation);

	public abstract TileMinerConnection getInput();

	protected abstract TilePowerConnection getWirePowerConnection();

	protected abstract TileShaftConnection getShaftPowerConnection();

	public final void breakBlock() {
		this.updateInputs(false);
		this.unload();
	}

	public final Collection<ChunkCoordIntPair> getChunksToLoad() {
		return ChunkManager.getChunkSquare(xCoord, zCoord, 1);
	}

	protected final void unload() {
		ChunkManager.instance.unloadChunks(this);
	}

	public final ItemStack getUpgradeSlot(int slot) {
		return overclock.getStackInSlot(slot-1);
	}

	public final OverclockingInv getOverClockingHandler() {
		return overclock;
	}

	@Override
	protected void writeSyncTag(NBTTagCompound NBT) {
		super.writeSyncTag(NBT);

		NBT.setInteger("activity", activityTimer);
		NBT.setInteger("operation", operationTimer);

		NBT.setInteger("state", state.ordinal());

		NBT.setBoolean("activeThisTick", this.isActiveThisTick);

		overclock.writeToNBT(NBT, "overclock");
	}

	@Override
	protected void readSyncTag(NBTTagCompound NBT) {
		super.readSyncTag(NBT);

		activityTimer = NBT.getInteger("activity");
		operationTimer = NBT.getInteger("operation");
		int struct = NBT.getInteger("structure");

		overclock.readFromNBT(NBT, "overclock");

		isActiveThisTick = NBT.getBoolean("activeThisTick");

		state = MachineState.list[NBT.getInteger("state")];
	}

	@Override
	protected void animateWithTick(World world, int x, int y, int z) {

	}

	@Override
	public final int getRedstoneOverride() {
		return 0;
	}

	@Override
	public final boolean shouldRenderInPass(int pass) {
		return pass <= 1;
	}

	public final void setHasStructure(S flag) {
		//ReikaJavaLibrary.pConsole(flag);
		this.updateStructureState(flag);
		this.syncAllData(false);
		this.updateInputs(flag != null);
	}

	protected void updateStructureState(S flag) {

	}

	private void updateInputs(boolean has) {
		TileMinerConnection te = this.getWirePowerConnection();
		if (te != null)
			te.connectTo(has ? this : null);
		te = this.getShaftPowerConnection();
		if (te != null)
			te.connectTo(has ? this : null);
		this.onUpdateInputs(has);
	}

	protected void onUpdateInputs(boolean has) {

	}

	public abstract N getResourceNode();

	public abstract ArrayList getMessages(World world, int x, int y, int z, int side);

	public abstract ItemStack getOverclockingItem();

	public abstract String getOperationPowerCost(boolean withOverclock);

	public abstract String getPowerType();

}
