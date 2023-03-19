package Reika.Satisforestry.Blocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.APIStripper.Strippable;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Effects.EntityLiquidParticleFX;
import Reika.DragonAPI.Libraries.ReikaAABBHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.Satisforestry.SFClient;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockCaveSpawner.TileCaveSpawner;
import Reika.Satisforestry.Config.BiomeConfig;
import Reika.Satisforestry.Config.NodeResource.NodeEffect;
import Reika.Satisforestry.Config.NodeResource.Purity;
import Reika.Satisforestry.Config.ResourceFluid;
import Reika.Satisforestry.Entity.EntitySpitter;
import Reika.Satisforestry.Entity.EntitySpitter.SpitterType;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import framesapi.IMoveCheck;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import vazkii.botania.api.mana.ILaputaImmobile;

@Strippable(value = {"mcp.mobius.waila.api.IWailaDataProvider", "framesapi.IMoveCheck", "vazkii.botania.api.mana.ILaputaImmobile"})
public class BlockFrackingNode extends BlockContainer implements PointSpawnBlock, IWailaDataProvider, IMoveCheck, ILaputaImmobile {

	private static IIcon itemIcon;
	private static final IIcon[] overlayIcon = new IIcon[10];

	public BlockFrackingNode(Material mat) {
		super(mat);
		this.setCreativeTab(Satisforestry.tabCreative);
		this.setResistance(60000);
		this.setBlockUnbreakable();
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return meta == 0 ? new TileFrackingNode() : null;
	}

	@Override
	public IIcon getIcon(IBlockAccess iba, int x, int y, int z, int s) {
		return Blocks.grass.getIcon(iba, x, y, z, s);
	}

	@Override
	public void registerBlockIcons(IIconRegister ico) {
		blockIcon = ico.registerIcon("satisforestry:nodes/frackingnode");

		for (int i = 0; i < 10; i++)
			overlayIcon[i] = ico.registerIcon("satisforestry:nodes/frackingnode_overlay_"+i);
		itemIcon = ico.registerIcon("satisforestry:nodes/frackingnode_item");
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

	public static IIcon getItem() {
		return itemIcon;
	}

	public static IIcon getOverlay(int idx) {
		return overlayIcon[idx];
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
		if (te instanceof TileFrackingNode) {
			((TileFrackingNode)te).addWaila(tip);
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

	public static class TileFrackingNode extends TileCaveSpawner {

		private static WeightedRandom<ResourceFluid> resourceSet = new WeightedRandom();

		private Purity mainPurity = Purity.NORMAL;
		private ResourceFluid resource;

		private float pressure = 0;

		public TileFrackingNode() {
			this.initSpawner(3);
		}

		public static Purity getRelativePurity(Purity base, Random rand) {
			float f = rand.nextFloat();
			if (f <= 0.4)
				return base;
			else if (f <= 0.7F)
				return base.higherOrSelf();
			else
				return base.lowerOrSelf();
		}

		private void initSpawner(int n) {
			this.setSpawnParameters(EntitySpitter.class, n, 5, 3, 16);
		}

		@Override
		protected void onSpawnEntity(EntityMob e, ArrayList<EntityLiving> spawned) {
			super.onSpawnEntity(e, spawned);
			((EntitySpitter)e).setSpitterType(this.getSpitterType());
		}

		private SpitterType getSpitterType() {
			return new Coordinate(this).hashCode()%2 == 0 ? SpitterType.GREEN : SpitterType.RED;
		}

		public void generate(Random rand) {
			if (resourceSet.isEmpty()) {
				for (ResourceFluid ri : BiomeConfig.instance.getFluidDrops()) {
					resourceSet.addEntry(ri, ri.spawnWeight);
				}
			}
			resourceSet.setRNG(rand);
			resource = resourceSet.getRandomEntry();
			mainPurity = resource.getRandomPurity(rand);
			this.initSpawner(1+mainPurity.ordinal());
		}

		@Override
		public void updateEntity() {
			super.updateEntity();
			if (worldObj.isRemote) {
				if (worldObj.getBlock(xCoord, yCoord+1, zCoord).isAir(worldObj, xCoord, yCoord+1, zCoord)) {
					fluidFountainParticles(worldObj, xCoord, yCoord, zCoord, System.identityHashCode(this), resource);
				}
			}
			else {
				if (resource == null) {
					this.generate(worldObj.rand);
					return;
				}
				boolean was = this.isPressurized();
				if (pressure > 0) {
					pressure = Math.max(0, pressure*0.99F-0.03F);
				}
				if (was != this.isPressurized()) {
					this.markDirty();
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				}
				Collection<NodeEffect> c = resource.getEffects();
				if (c.isEmpty())
					return;
				AxisAlignedBB box = ReikaAABBHelper.getBlockAABB(this).expand(12, 3, 12).offset(0, 2, 0);
				List<EntityPlayer> li = worldObj.getEntitiesWithinAABB(EntityPlayer.class, box);
				for (EntityPlayer ep : li) {
					for (NodeEffect e : c) {
						e.apply(this, ep);
					}
				}
			}
		}

		@SideOnly(Side.CLIENT)
		public static void fluidFountainParticles(World world, int x, int y, int z, int randSeed, ResourceFluid rf) {
			Fluid f = rf.generateRandomFluid(Purity.PURE, false, 1).getFluid();
			int n = 1+DragonAPICore.rand.nextInt(8);
			double fac = 0.6+0.4*Math.sin(world.getTotalWorldTime()*0.08143+randSeed%1000D);
			n *= fac;
			for (int i = 0; i < n; i++) {
				double vx = ReikaRandomHelper.getRandomPlusMinus(0, 0.03125);
				double vz = ReikaRandomHelper.getRandomPlusMinus(0, 0.03125);
				double vy = ReikaRandomHelper.getRandomBetween(0.2, 0.4);
				if (fac >= 0.75)
					vy *= 0.25+fac;

				double dx = ReikaRandomHelper.getRandomPlusMinus(x+0.5, 0.125);
				double dz = ReikaRandomHelper.getRandomPlusMinus(z+0.5, 0.125);

				EntityLiquidParticleFX fx = new EntityLiquidParticleFX(world, dx, y+1.05, dz, vx, vy, vz, f);
				fx.setGravity((float)ReikaRandomHelper.getRandomBetween(0.3, 0.7));
				fx.setLife(ReikaRandomHelper.getRandomBetween(20, 60));
				Minecraft.getMinecraft().effectRenderer.addEffect(fx);
			}
		}

		public void pressurize(float overclockFactor) {
			boolean was = this.isPressurized();
			pressure = Math.min(pressure+0.1F, overclockFactor);
			if (was != this.isPressurized()) {
				this.markDirty();
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}

		public boolean isPressurized() {
			return pressure >= 0.8F;
		}

		public float getPressure() {
			return pressure;
		}

		@Override
		public void writeToNBT(NBTTagCompound NBT) {
			super.writeToNBT(NBT);

			NBT.setInteger("purity", mainPurity.ordinal());
			if (resource != null)
				NBT.setString("resource", resource.id);

			NBT.setFloat("pressure", pressure);
		}

		@Override
		public void readFromNBT(NBTTagCompound NBT) {
			super.readFromNBT(NBT);

			mainPurity = Purity.list[NBT.getInteger("purity")];
			if (NBT.hasKey("resource"))
				resource = BiomeConfig.instance.getFluidByID(NBT.getString("resource"));

			pressure = NBT.getFloat("pressure");
		}

		public ResourceFluid getResource() {
			return resource;
		}

		public Purity getPurity() {
			return mainPurity;
		}

		@Override
		protected boolean isEmptyTimeoutActive(World world) {
			return false;
		}

		public void addWaila(List<String> tip) {
			tip.add(resource.displayName);
			tip.add(mainPurity.getDisplayName());
			tip.add("Pressurized: "+(this.isPressurized() ? "Yes" : "No"));
		}

	}
}
