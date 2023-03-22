package Reika.Satisforestry.Blocks;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
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
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.RotaryCraft.API.Power.ShaftPowerReceiver;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockResourceNode.ResourceNode;
import Reika.Satisforestry.Miner.TileNodeHarvester.TileNodeHarvesterRC;
import Reika.Satisforestry.Miner.TileResourceHarvesterBase;
import Reika.Satisforestry.Registry.SFBlocks;

import cofh.api.energy.IEnergyReceiver;
import ic2.api.energy.tile.IEnergySink;

public abstract class BlockSFMultiBase<S> extends BlockMultiBlock<S> {

	public BlockSFMultiBase(Material mat) {
		super(mat);
		this.setCreativeTab(Satisforestry.tabCreative);
		this.setLightOpacity(0);
		this.setHardness(Blocks.mob_spawner.blockHardness);
		this.setResistance(30);
	}

	@Override
	public final boolean isOpaqueCube() {
		return false;
	}

	@Override
	public final boolean renderAsNormalBlock() {
		return false;
	}

	protected abstract String getIconFolderName();

	@Override
	protected final String getFullIconPath(int i) {
		return "satisforestry:"+this.getIconFolderName()+"/"+i;
	}

	@Override
	public final ArrayList<String> getMessages(World world, int x, int y, int z, int side) {
		TileEntity te = this.getTileEntityForPosition(world, x, y, z);
		return te instanceof TileResourceHarvesterBase ? ((TileResourceHarvesterBase)te).getMessages(world, x, y, z, side) : new ArrayList();
	}

	public final String getName(int meta) {
		return StatCollector.translateToLocal("multiblock."+this.getLocaleKeyName()+"."+(meta&7));
	}

	protected abstract String getLocaleKeyName();

	protected abstract SFBlocks getTileBlockType();

	@Override
	public final int getNumberTextures() {
		return this.getNumberVariants()+1;
	}

	@Override
	public final boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int s, float a, float b, float c) {
		int meta = world.getBlockMetadata(x, y, z);
		if (meta >= 8) {
			TileEntity te = this.getTileEntityForPosition(world, x, y, z);
			//ReikaJavaLibrary.pConsole(te);
			if (te instanceof TileResourceHarvesterBase) {
				ep.openGui(Satisforestry.instance, 0, world, te.xCoord, te.yCoord, te.zCoord);
				return true;
			}
		}
		return false;
	}

	protected abstract int getBlockSearchXZ();
	protected abstract int getBlockSearchY();

	@Override
	public final void breakMultiBlock(World world, int x, int y, int z) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		blocks.extraSpread = true;
		int s = this.getBlockSearchXZ();
		int sy = this.getBlockSearchY();
		blocks.recursiveMultiAddWithBounds(world, x, y, z, x-s, y-sy, z-s, x+s, y+sy, z+s, this, this.getTileBlockType().getBlockInstance());
		for (int i = 0; i < 6; i++) {
			ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i];
			blocks.recursiveMultiAddWithBounds(world, x+dir.offsetX, y+dir.offsetY, z+dir.offsetZ, x-s, y-sy, z-s, x+s, y+sy, z+s, this, this.getTileBlockType().getBlockInstance());
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
		if (te instanceof TileResourceHarvesterBase) {
			((TileResourceHarvesterBase)te).setHasStructure(null);
		}
	}

	@Override
	protected final void onCreateFullMultiBlock(World world, int x, int y, int z, S dir) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		blocks.extraSpread = true;
		int s = this.getBlockSearchXZ();
		int sy = this.getBlockSearchY();
		blocks.recursiveMultiAddWithBounds(world, x, y, z, x-s, y-sy, z-s, x+s, y+sy, z+s, this, this.getTileBlockType().getBlockInstance());
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
		if (te instanceof TileResourceHarvesterBase) {
			((TileResourceHarvesterBase)te).setHasStructure(dir);
		}
	}

	@Override
	public final int getTextureIndex(IBlockAccess world, int x, int y, int z, int side, int meta) {
		return meta >= 8 ? 8 : meta;
	}

	@Override
	public final int getItemTextureIndex(int meta, int side) {
		return meta&7;
	}

	@Override
	public final boolean canTriggerMultiBlockCheck(World world, int x, int y, int z, int meta) {
		return true;
	}

	@Override
	protected final TileEntity getTileEntityForPosition(World world, int x, int y, int z) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		blocks.extraSpread = true;
		int s = this.getBlockSearchXZ();
		int sy = this.getBlockSearchY();
		blocks.recursiveMultiAddWithBounds(world, x, y, z, x-s, y-sy, z-s, x+s, y+sy, z+s, this, this.getTileBlockType().getBlockInstance());
		for (int i = 0; i < blocks.getSize(); i++) {
			Coordinate c = blocks.getNthBlock(i);
			Block b = c.getBlock(world);
			//ReikaJavaLibrary.pConsole(b);
			if (b == this.getTileBlockType().getBlockInstance())
				return c.getTileEntity(world);
		}
		return null;
	}

	public static abstract class TileMinerConnection<T extends TileResourceHarvesterBase> extends TileEntity {

		private Coordinate rootLoc;
		private T root;

		@Override
		public boolean canUpdate() {
			return false;
		}

		public final void connectTo(T te) {
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

		protected final T getRoot() {
			if (root == null && rootLoc != null && worldObj != null) {
				TileEntity te = rootLoc.getTileEntity(worldObj);
				if (te instanceof TileResourceHarvesterBase)
					root = (T)te;
				else
					rootLoc = null;
			}
			return root;
		}

		protected final ResourceNode getNode() {
			T root = this.getRoot();
			return root != null ? root.getResourceNode() : null;
		}

		public final boolean hasRedstone() {
			return worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
		}

	}

	public static class TileShaftConnection extends TileMinerConnection implements ShaftPowerReceiver {

		@Override
		public boolean canReadFrom(ForgeDirection dir) {
			return dir == ForgeDirection.UP;
		}

		@Override
		public boolean isReceiving() {
			return this.getRoot() instanceof ShaftPowerReceiver && this.getRoot().hasStructure();
		}

		@Override
		public int getMinTorque(int available) {
			return ((ShaftPowerReceiver)this.getRoot()).getMinTorque(available);
		}

		@Override
		public int getOmega() {
			return ((ShaftPowerReceiver)this.getRoot()).getOmega();
		}

		@Override
		public int getTorque() {
			return ((ShaftPowerReceiver)this.getRoot()).getTorque();
		}

		@Override
		public long getPower() {
			return ((ShaftPowerReceiver)this.getRoot()).getPower();
		}

		@Override
		public int getIORenderAlpha() {
			return ((ShaftPowerReceiver)this.getRoot()).getIORenderAlpha();
		}

		@Override
		public void setIORenderAlpha(int io) {
			((ShaftPowerReceiver)this.getRoot()).setIORenderAlpha(io);
		}

		@Override
		public void setOmega(int omega) {
			if (this.getRoot() instanceof TileNodeHarvesterRC)
				((ShaftPowerReceiver)this.getRoot()).setOmega(omega);
		}

		@Override
		public void setTorque(int torque) {
			if (this.getRoot() instanceof TileNodeHarvesterRC)
				((ShaftPowerReceiver)this.getRoot()).setTorque(torque);
		}

		@Override
		public void setPower(long power) {
			if (this.getRoot() instanceof TileNodeHarvesterRC)
				((ShaftPowerReceiver)this.getRoot()).setPower(power);
		}

		@Override
		public void noInputMachine() {
			((ShaftPowerReceiver)this.getRoot()).noInputMachine();
		}

	}

	@Strippable(value={"ic2.api.energy.tile.IEnergySink"})
	public static class TilePowerConnection extends TileMinerConnection implements IEnergyReceiver, IEnergySink {

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