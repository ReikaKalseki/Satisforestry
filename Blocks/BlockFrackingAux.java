package Reika.Satisforestry.Blocks;

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.APIStripper.Strippable;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Extras.IconPrefabs;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Effects.EntityBlurFX;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.Satisforestry.SFClient;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockFrackingNode.TileFrackingNode;
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
		return meta == 0 ? new TileFrackingAux() : null;
	}

	@Override
	public IIcon getIcon(IBlockAccess iba, int x, int y, int z, int s) {
		return SFBlocks.CAVESHIELD.getBlockInstance().getIcon(iba, x, y, z, s);
	}

	@Override
	public void registerBlockIcons(IIconRegister ico) {
		blockIcon = ico.registerIcon("satisforestry:frackingnode");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		int n = 5;
		TileFrackingNode te = (TileFrackingNode)world.getTileEntity(x, y, z);
		if (te != null) {
			n = 5-te.getPurity().ordinal()*2;
		}
		if (rand.nextInt(n) == 0) {
			double px = x+0.5+rand.nextGaussian();//ReikaRandomHelper.getRandomBetween(x-1.5, x+2.5);
			double pz = z+0.5+rand.nextGaussian();//ReikaRandomHelper.getRandomBetween(z-1.5, z+2.5);
			double py = ReikaRandomHelper.getRandomBetween(y+1.0625, y+1.375);
			EntityBlurFX fx = new EntityBlurFX(world, px, py, pz, IconPrefabs.FADE.getIcon());
			fx.setScale((float)ReikaRandomHelper.getRandomBetween(0.6, 1.2)).setLife(ReikaRandomHelper.getRandomBetween(3, 6));
			fx.setAlphaFading().setRapidExpand().setColor(0xffffff);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}
	}

	@Override
	public int getRenderType() {
		return Satisforestry.proxy.frackingRender;
	}

	@Override
	public final int getRenderBlockPass() {
		return 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderInPass(int pass) {
		SFClient.fracking.setRenderPass(pass);
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

	public static class TileFrackingAux extends TileEntity implements IFluidHandler {

		private Coordinate masterLocation;

		@Override
		public void writeToNBT(NBTTagCompound NBT) {
			super.writeToNBT(NBT);

			if (masterLocation != null)
				masterLocation.writeToNBT("master", NBT);
		}

		@Override
		public void readFromNBT(NBTTagCompound NBT) {
			super.readFromNBT(NBT);

			masterLocation = Coordinate.readFromNBT("master", NBT);
		}

		@Override
		public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
			return 0;
		}

		@Override
		public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {/*
			if (from != ForgeDirection.UP)
				return null;
			TileFrackingNode te = this.getMaster();
			return te != null ? te.takeFluid(resource.amount, doDrain) : null;*/
			return null;
		}

		@Override
		public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
			if (from != ForgeDirection.UP)
				return null;
			TileFrackingNode te = this.getMaster();
			return te != null ? te.takeFluid(maxDrain, doDrain) : null;
		}

		@Override
		public boolean canFill(ForgeDirection from, Fluid fluid) {
			return false;
		}

		@Override
		public boolean canDrain(ForgeDirection from, Fluid fluid) {
			return from == ForgeDirection.UP;
		}

		@Override
		public FluidTankInfo[] getTankInfo(ForgeDirection from) {
			return new FluidTankInfo[0];
		}

		public void linkTo(Coordinate c) {
			masterLocation = c;
			this.markDirty();
		}

		public TileFrackingNode getMaster() {
			TileEntity te = masterLocation == null ? masterLocation.getTileEntity(worldObj) : null;
			return te instanceof TileFrackingNode ? (TileFrackingNode)te : null;
		}

	}
}
