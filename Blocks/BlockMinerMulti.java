package Reika.Satisforestry.Blocks;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.APIStripper.Strippable;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Base.BlockMultiBlock;
import Reika.DragonAPI.Instantiable.Data.BlockStruct.StructuredBlockArray;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Libraries.ReikaInventoryHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.RotaryCraft.API.Power.ShaftPowerReceiver;
import Reika.Satisforestry.MinerStructure;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.TileNodeHarvester.TileNodeHarvesterRC;
import Reika.Satisforestry.Registry.SFBlocks;

import cofh.api.energy.IEnergyReceiver;
import ic2.api.energy.tile.IEnergySink;

public class BlockMinerMulti extends BlockMultiBlock<ForgeDirection> {

	private static final int SEARCH = 12;
	private static final int SEARCHY = 16;

	public static enum MinerBlocks {
		ORANGE,
		DARK,
		SILVER,
		GRAY,
		DRILL,
		CONVEYOR,
		HUB,
		POWER,
		;

		public static final MinerBlocks[] list = values();
	}

	public BlockMinerMulti(Material mat) {
		super(mat);
		this.setCreativeTab(Satisforestry.tabCreative);
		this.setResistance(30);
		this.setLightOpacity(0);
	}

	@Override
	public boolean hasTileEntity(int meta) {
		if (meta < 8)
			return false;
		MinerBlocks mb = MinerBlocks.list[meta&7];
		return mb == MinerBlocks.HUB || mb == MinerBlocks.POWER || mb == MinerBlocks.CONVEYOR;
	}

	@Override
	public TileEntity createTileEntity(World world, int meta) {
		if (meta < 8)
			return null;
		switch(MinerBlocks.list[meta&7]) {
			case HUB:
				return new TileMinerShaftConnection();
			case POWER:
				return new TileMinerPowerConnection();
			case CONVEYOR:
				return new TileMinerConveyorPort();
			default:
				return null;
		}
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	protected final String getFullIconPath(int i) {
		return "satisforestry:miner/"+i;
	}

	@Override
	public final ArrayList<String> getMessages(World world, int x, int y, int z, int side) {
		TileEntity te = this.getTileEntityForPosition(world, x, y, z);
		return te instanceof TileNodeHarvester ? ((TileNodeHarvester)te).getMessages(world, x, y, z, side) : new ArrayList();
	}

	public final String getName(int meta) {
		return StatCollector.translateToLocal("multiblock.sfminer."+(meta&7));
	}

	@Override
	public int getNumberTextures() {
		return 9;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int s, float a, float b, float c) {
		int meta = world.getBlockMetadata(x, y, z);
		if (meta >= 8) {
			TileEntity te = this.getTileEntityForPosition(world, x, y, z);
			//ReikaJavaLibrary.pConsole(te);
			if (te instanceof TileNodeHarvester) {
				ep.openGui(Satisforestry.instance, 0, world, te.xCoord, te.yCoord, te.zCoord);
				return true;
			}
		}
		return false;
	}

	@Override
	public ForgeDirection checkForFullMultiBlock(World world, int x, int y, int z, ForgeDirection placeDir) {
		TileEntity te = this.getTileEntityForPosition(world, x, y, z);
		//ReikaJavaLibrary.pConsole(te);
		if (te instanceof TileNodeHarvester) {
			return MinerStructure.getStructureDirection(world, te.xCoord, te.yCoord, te.zCoord);
		}
		return null;
	}

	@Override
	public void breakMultiBlock(World world, int x, int y, int z) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		blocks.extraSpread = true;
		blocks.recursiveMultiAddWithBounds(world, x, y, z, x-SEARCH, y-SEARCHY, z-SEARCH, x+SEARCH, y+SEARCHY, z+SEARCH, this, SFBlocks.HARVESTER.getBlockInstance());
		for (int i = 0; i < 6; i++) {
			ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i];
			blocks.recursiveMultiAddWithBounds(world, x+dir.offsetX, y+dir.offsetY, z+dir.offsetZ, x-SEARCH, y-SEARCHY, z-SEARCH, x+SEARCH, y+SEARCHY, z+SEARCH, this, SFBlocks.HARVESTER.getBlockInstance());
		}
		for (int i = 0; i < blocks.getSize(); i++) {
			Coordinate c = blocks.getNthBlock(i);
			int meta = c.getBlockMetadata(world);
			if (meta >= 8) {
				TileEntity te = world.getTileEntity(c.xCoord, c.yCoord, c.zCoord);
				if (te instanceof IInventory) {
					ReikaItemHelper.dropInventory(world, c.xCoord, c.yCoord, c.zCoord);
				}
				world.setBlockMetadataWithNotify(c.xCoord, c.yCoord, c.zCoord, meta-8, 3);
			}
		}
		TileEntity te = this.getTileEntityForPosition(world, x, y, z);
		if (te instanceof TileNodeHarvester) {
			((TileNodeHarvester)te).setHasStructure(null);
		}
	}

	@Override
	protected void onCreateFullMultiBlock(World world, int x, int y, int z, ForgeDirection dir) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		blocks.extraSpread = true;
		blocks.recursiveMultiAddWithBounds(world, x, y, z, x-SEARCH, y-SEARCHY, z-SEARCH, x+SEARCH, y+SEARCHY, z+SEARCH, this, SFBlocks.HARVESTER.getBlockInstance());
		for (int i = 0; i < blocks.getSize(); i++) {
			Coordinate c = blocks.getNthBlock(i);
			if (c.getBlock(world) != this)
				continue;
			int meta = c.getBlockMetadata(world);
			if (meta < 8) {
				world.setBlockMetadataWithNotify(c.xCoord, c.yCoord, c.zCoord, meta+8, 3);
			}
		}
		TileEntity te = this.getTileEntityForPosition(world, x, y, z);
		if (te instanceof TileNodeHarvester) {
			((TileNodeHarvester)te).setHasStructure(dir);
		}
	}

	@Override
	public int getNumberVariants() {
		return 8;
	}

	@Override
	public int getTextureIndex(IBlockAccess world, int x, int y, int z, int side, int meta) {
		return meta >= 8 ? 8 : meta;
	}

	@Override
	public int getItemTextureIndex(int meta, int side) {
		return meta&7;
	}

	@Override
	public boolean canTriggerMultiBlockCheck(World world, int x, int y, int z, int meta) {
		return true;
	}

	@Override
	protected TileEntity getTileEntityForPosition(World world, int x, int y, int z) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		blocks.extraSpread = true;
		blocks.recursiveMultiAddWithBounds(world, x, y, z, x-SEARCH, y-SEARCHY, z-SEARCH, x+SEARCH, y+SEARCHY, z+SEARCH, this, SFBlocks.HARVESTER.getBlockInstance());
		for (int i = 0; i < blocks.getSize(); i++) {
			Coordinate c = blocks.getNthBlock(i);
			Block b = c.getBlock(world);
			//ReikaJavaLibrary.pConsole(b);
			if (b == SFBlocks.HARVESTER.getBlockInstance())
				return c.getTileEntity(world);
		}
		return null;
	}

	public static abstract class TileMinerConnection extends TileEntity {

		private Coordinate rootLoc;
		private TileNodeHarvester root;

		@Override
		public boolean canUpdate() {
			return false;
		}

		public final void connectTo(TileNodeHarvester te) {
			root = te;
			rootLoc = te != null ? new Coordinate(te) : null;
		}

		public String getName() {
			return root != null ? root.getName() : "Miner Input";
		}

		@Override
		public void writeToNBT(NBTTagCompound NBT) {
			super.writeToNBT(NBT);

			if (rootLoc != null)
				rootLoc.writeToNBT("root", NBT);
		}

		@Override
		public void readFromNBT(NBTTagCompound NBT) {
			super.readFromNBT(NBT);

			root = null;
			if (NBT.hasKey("root")) {
				rootLoc = Coordinate.readFromNBT("root", NBT);
			}
			else {
				rootLoc = null;
			}
		}

		protected final TileNodeHarvester getRoot() {
			if (root == null && rootLoc != null && worldObj != null) {
				TileEntity te = rootLoc.getTileEntity(worldObj);
				if (te instanceof TileNodeHarvester)
					root = (TileNodeHarvester)te;
				else
					rootLoc = null;
			}
			return root;
		}

	}

	public static class TileMinerConveyorPort extends TileMinerConnection implements ISidedInventory {

		private ItemStack currentSlot;

		@Override
		public int getSizeInventory() {
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return currentSlot;
		}

		@Override
		public ItemStack decrStackSize(int slot, int decr) {
			return ReikaInventoryHelper.decrStackSize(this, slot, decr);
		}

		@Override
		public ItemStack getStackInSlotOnClosing(int slot) {
			return ReikaInventoryHelper.getStackInSlotOnClosing(this, slot);
		}

		@Override
		public void setInventorySlotContents(int slot, ItemStack is) {
			currentSlot = is;
		}

		@Override
		public String getInventoryName() {
			return this.getName();
		}

		@Override
		public boolean hasCustomInventoryName() {
			return false;
		}

		@Override
		public int getInventoryStackLimit() {
			return 64;
		}

		@Override
		public boolean isUseableByPlayer(EntityPlayer ep) {
			return false;
		}

		@Override
		public void openInventory() {

		}

		@Override
		public void closeInventory() {

		}

		@Override
		public boolean isItemValidForSlot(int slot, ItemStack is) {
			return false;
		}

		@Override
		public int[] getAccessibleSlotsFromSide(int side) {
			return new int[0];
		}

		@Override
		public boolean canInsertItem(int slot, ItemStack is, int side) {
			return false;
		}

		@Override
		public boolean canExtractItem(int slot, ItemStack is, int side) {
			return this.getRoot() != null && ForgeDirection.VALID_DIRECTIONS[side] == this.getRoot().getDirection();
		}

		@Override
		public void writeToNBT(NBTTagCompound NBT) {
			super.writeToNBT(NBT);

			NBTTagCompound tag = new NBTTagCompound();
			if (currentSlot != null)
				currentSlot.writeToNBT(tag);
			NBT.setTag("contents", tag);
		}

		@Override
		public void readFromNBT(NBTTagCompound NBT) {
			super.readFromNBT(NBT);

			NBTTagCompound tag = NBT.getCompoundTag("content");
			currentSlot = ItemStack.loadItemStackFromNBT(tag);
		}

	}

	public static class TileMinerShaftConnection extends TileMinerConnection implements ShaftPowerReceiver {

		@Override
		public boolean canReadFrom(ForgeDirection dir) {
			return dir == ForgeDirection.UP;
		}

		@Override
		public boolean isReceiving() {
			return this.getRoot() instanceof TileNodeHarvesterRC && this.getRoot().hasStructure();
		}

		@Override
		public int getMinTorque(int available) {
			return TileNodeHarvester.TileNodeHarvesterRC.MINTORQUE;
		}

		@Override
		public int getOmega() {
			return ((TileNodeHarvesterRC)this.getRoot()).getOmega();
		}

		@Override
		public int getTorque() {
			return ((TileNodeHarvesterRC)this.getRoot()).getTorque();
		}

		@Override
		public long getPower() {
			return ((TileNodeHarvesterRC)this.getRoot()).getPower();
		}

		@Override
		public int getIORenderAlpha() {
			return ((TileNodeHarvesterRC)this.getRoot()).getIORenderAlpha();
		}

		@Override
		public void setIORenderAlpha(int io) {
			((TileNodeHarvesterRC)this.getRoot()).setIORenderAlpha(io);
		}

		@Override
		public void setOmega(int omega) {
			((TileNodeHarvesterRC)this.getRoot()).setOmega(omega);
		}

		@Override
		public void setTorque(int torque) {
			((TileNodeHarvesterRC)this.getRoot()).setTorque(torque);
		}

		@Override
		public void setPower(long power) {
			((TileNodeHarvesterRC)this.getRoot()).setPower(power);
		}

		@Override
		public void noInputMachine() {
			((TileNodeHarvesterRC)this.getRoot()).noInputMachine();
		}

	}

	@Strippable(value={"ic2.api.energy.tile.IEnergySink"})
	public static class TileMinerPowerConnection extends TileMinerConnection implements IEnergyReceiver, IEnergySink {

		//RF
		@Override
		public boolean canConnectEnergy(ForgeDirection from) {
			return from != ForgeDirection.DOWN;
		}

		@Override
		public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
			return this.getRoot() instanceof IEnergyReceiver ? ((IEnergyReceiver)this.getRoot()).receiveEnergy(from, maxReceive, simulate) : 0;
		}

		@Override
		public int getEnergyStored(ForgeDirection from) {
			return this.getRoot() instanceof IEnergyReceiver ? ((IEnergyReceiver)this.getRoot()).getEnergyStored(from) : 0;
		}

		@Override
		public int getMaxEnergyStored(ForgeDirection from) {
			return this.getRoot() instanceof IEnergyReceiver ? ((IEnergyReceiver)this.getRoot()).getMaxEnergyStored(from) : 0;
		}

		//EU
		@Override
		@ModDependent(ModList.IC2)
		public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) {
			return this.canConnectEnergy(direction);
		}

		@Override
		@ModDependent(ModList.IC2)
		public double getDemandedEnergy() {
			return this.getRoot() instanceof IEnergySink ? ((IEnergySink)this.getRoot()).getDemandedEnergy() : 0;
		}

		@Override
		@ModDependent(ModList.IC2)
		public int getSinkTier() {
			return this.getRoot() instanceof IEnergySink ? ((IEnergySink)this.getRoot()).getSinkTier() : 0;
		}

		@Override
		@ModDependent(ModList.IC2)
		public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage) {
			return this.getRoot() instanceof IEnergySink ? ((IEnergySink)this.getRoot()).injectEnergy(directionFrom, amount, voltage) : 0;
		}

	}

}
