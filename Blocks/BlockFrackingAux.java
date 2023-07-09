package Reika.Satisforestry.Blocks;

import java.util.List;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.APIStripper.Strippable;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Instantiable.HybridTank;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.Satisforestry.SFClient;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockFrackingNode.TileFrackingNode;
import Reika.Satisforestry.Blocks.BlockFrackingPressurizer.TileFrackingExtractor;
import Reika.Satisforestry.Config.NodeResource.Purity;
import Reika.Satisforestry.Config.ResourceFluid;
import Reika.Satisforestry.Registry.SFBlocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import framesapi.IMoveCheck;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import vazkii.botania.api.mana.ILaputaImmobile;

@Strippable(value = {"mcp.mobius.waila.api.IWailaDataProvider", "framesapi.IMoveCheck", "vazkii.botania.api.mana.ILaputaImmobile"})
public class BlockFrackingAux extends BlockContainer implements IWailaDataProvider, IMoveCheck, ILaputaImmobile {

	public BlockFrackingAux(Material mat) {
		super(mat);
		this.setCreativeTab(Satisforestry.tabCreative);
		this.setResistance(60000);
		this.setBlockUnbreakable();
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileFrackingAux();
	}

	@Override
	public void registerBlockIcons(IIconRegister ico) {
		blockIcon = ico.registerIcon("satisforestry:nodes/frackingnode");
	}

	@Override
	public IIcon getIcon(int s, int meta) {
		return SFBlocks.FRACKNODE.getBlockInstance().getIcon(s, meta);
	}

	@Override
	public IIcon getIcon(IBlockAccess iba, int x, int y, int z, int s) {
		return SFBlocks.FRACKNODE.getBlockInstance().getIcon(iba, x, y, z, s);
	}

	@Override
	public int getRenderType() {
		return Satisforestry.proxy.frackingAuxRender;
	}

	@Override
	public final int getRenderBlockPass() {
		return 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderInPass(int pass) {
		SFClient.frackingAux.setRenderPass(pass);
		return pass <= 1;
	}

	@Override
	public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity e) {
		return false;
	}

	@Override
	public boolean canMove(World world, int x, int y, int z) {
		return false;
	}

	@Override
	@ModDependent(ModList.WAILA)
	public ItemStack getWailaStack(IWailaDataAccessor acc, IWailaConfigHandler config) {
		return null;
	}

	@Override
	@ModDependent(ModList.WAILA)
	public final List<String> getWailaHead(ItemStack is, List<String> tip, IWailaDataAccessor acc, IWailaConfigHandler config) {
		return tip;
	}

	@Override
	@ModDependent(ModList.WAILA)
	public final List<String> getWailaBody(ItemStack is, List<String> tip, IWailaDataAccessor acc, IWailaConfigHandler config) {
		TileEntity te = acc.getTileEntity();
		if (te instanceof TileFrackingAux) {
			TileFrackingNode te2 = ((TileFrackingAux)te).getMaster();
			if (te2 != null) {
				te2.addWaila(tip);
			}
		}
		ReikaJavaLibrary.removeDuplicates(tip);
		return tip;
	}

	@ModDependent(ModList.WAILA)
	public final List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor acc, IWailaConfigHandler config) {
		return currenttip;
	}

	@Override
	public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x, int y, int z) {
		return tag;
	}

	public static class TileFrackingAux extends TileEntity/* implements IFluidHandler*/ {

		private Coordinate masterLocation;

		private HybridTank tank = new HybridTank("fracking", 10000);

		private boolean isPressurized = false;

		@Override
		public void updateEntity() {
			super.updateEntity();
			TileFrackingNode te = this.getMaster();
			boolean flag = te != null && te.isPressurized();
			if (flag != isPressurized) {
				this.markDirty();
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
			isPressurized = flag;
			if (isPressurized) {
				ResourceFluid res = te.getResource();
				if (worldObj.isRemote) {
					if (worldObj.getBlock(xCoord, yCoord+1, zCoord).isAir(worldObj, xCoord, yCoord+1, zCoord)) {
						TileFrackingNode.fluidFountainParticles(worldObj, xCoord, yCoord, zCoord, System.identityHashCode(this), res);
					}
				}
				else {
					boolean peaceful = worldObj.difficultySetting == EnumDifficulty.PEACEFUL;
					if (peaceful && !res.worksOnPeaceful())
						return;
					tank.addLiquid(res.generateRandomFluid(this.getPurity(), peaceful, te.getOverclock()));
				}
			}
			else {
				tank.empty();
			}
		}

		public Purity getPurity() {
			return Purity.list[this.getBlockMetadata()];
		}

		@Override
		public void writeToNBT(NBTTagCompound NBT) {
			super.writeToNBT(NBT);

			if (masterLocation != null)
				masterLocation.writeToNBT("master", NBT);

			tank.writeToNBT(NBT);
		}

		@Override
		public void readFromNBT(NBTTagCompound NBT) {
			super.readFromNBT(NBT);

			masterLocation = Coordinate.readFromNBT("master", NBT);

			tank.readFromNBT(NBT);
		}

		public FluidStack drain(FluidStack resource, boolean doDrain) {
			return tank.drain(resource.amount, doDrain);
		}

		public FluidStack drain(int maxDrain, boolean doDrain) {
			return tank.drain(maxDrain, doDrain);
		}

		public void linkTo(Coordinate c) {
			masterLocation = c;
			if (worldObj != null) {
				this.markDirty();
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}

		public TileFrackingNode getMaster() {
			TileEntity te = worldObj == null || masterLocation == null ? null : masterLocation.getTileEntity(worldObj);
			return te instanceof TileFrackingNode ? (TileFrackingNode)te : null;
		}

		@Override
		public Packet getDescriptionPacket() {
			NBTTagCompound NBT = new NBTTagCompound();
			this.writeToNBT(NBT);
			S35PacketUpdateTileEntity pack = new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, NBT);
			return pack;
		}

		@Override
		public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity p)  {
			this.readFromNBT(p.field_148860_e);
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}

		public boolean hasExtractor() {
			TileEntity te = worldObj.getTileEntity(xCoord, yCoord+3, zCoord);
			return te instanceof TileFrackingExtractor && ((TileFrackingExtractor)te).hasStructure();
		}

	}
}
