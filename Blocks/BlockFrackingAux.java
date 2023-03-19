package Reika.Satisforestry.Blocks;

import java.util.List;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.APIStripper.Strippable;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Instantiable.HybridTank;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Effects.EntityLiquidParticleFX;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.Satisforestry.SFClient;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockFrackingNode.TileFrackingNode;
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
		return meta == 0 ? new TileFrackingAux() : null;
	}

	@Override
	public void registerBlockIcons(IIconRegister ico) {
		blockIcon = ico.registerIcon("satisforestry:frackingnode");
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
				tip.add(te2.getResource().displayName);
				tip.add(((TileFrackingAux)te).getPurity().getDisplayName());
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

		private HybridTank tank = new HybridTank("fracking", 10000);

		@Override
		public void updateEntity() {
			super.updateEntity();
			TileFrackingNode te = this.getMaster();
			if (te != null && te.isPressurized()) {
				ResourceFluid res = te.getResource();
				if (worldObj.isRemote) {
					if (worldObj.getBlock(xCoord, yCoord+1, zCoord).isAir(worldObj, xCoord, yCoord+1, zCoord)) {
						this.fluidFountainParticles(worldObj, xCoord, yCoord, zCoord, res.generateRandomFluid(te.getPurity(), false).getFluid());
					}
				}
				else {
					boolean peaceful = worldObj.difficultySetting == EnumDifficulty.PEACEFUL;
					if (peaceful && !res.worksOnPeaceful())
						return;
					tank.addLiquid(res.generateRandomFluid(this.getPurity(), peaceful));
				}
			}
		}

		private Purity getPurity() {
			return Purity.list[this.getBlockMetadata()];
		}

		@SideOnly(Side.CLIENT)
		private void fluidFountainParticles(World world, int x, int y, int z, Fluid f) {
			int n = 1+DragonAPICore.rand.nextInt(8);
			n *= 0.6+0.4*Math.sin(world.getTotalWorldTime()*0.08143+System.identityHashCode(this)%1000D);
			for (int i = 0; i < n; i++) {
				double vx = ReikaRandomHelper.getRandomPlusMinus(0, 0.0625);
				double vz = ReikaRandomHelper.getRandomPlusMinus(0, 0.0625);
				double vy = ReikaRandomHelper.getRandomBetween(0.25, 0.5);

				double dx = ReikaRandomHelper.getRandomPlusMinus(x+0.5, 0.125);
				double dz = ReikaRandomHelper.getRandomPlusMinus(z+0.5, 0.125);

				EntityLiquidParticleFX fx = new EntityLiquidParticleFX(world, dx, y+1.05, dz, vx, vy, vz, f);
				fx.setGravity((float)ReikaRandomHelper.getRandomBetween(0.05, 0.08));
				Minecraft.getMinecraft().effectRenderer.addEffect(fx);
			}
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

		@Override
		public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
			return 0;
		}

		@Override
		public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
			if (from != ForgeDirection.UP)
				return null;
			return tank.drain(resource.amount, doDrain);
		}

		@Override
		public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
			if (from != ForgeDirection.UP)
				return null;
			return tank.drain(maxDrain, doDrain);
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
			return new FluidTankInfo[] {new FluidTankInfo(tank)};
		}

		public void linkTo(Coordinate c) {
			masterLocation = c;
			this.markDirty();
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

	}
}
