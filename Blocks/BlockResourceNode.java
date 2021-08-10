package Reika.Satisforestry.Blocks;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.APIStripper.Strippable;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Libraries.ReikaAABBHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.Java.ReikaStringParser;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.Satisforestry.SFClient;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockCaveSpawner.TileCaveSpawner;
import Reika.Satisforestry.Config.BiomeConfig;
import Reika.Satisforestry.Config.ResourceItem;
import Reika.Satisforestry.Config.ResourceItem.NodeEffect;
import Reika.Satisforestry.Entity.EntityEliteStinger;
import Reika.Satisforestry.Registry.SFBlocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import framesapi.IMoveCheck;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import vazkii.botania.api.mana.ILaputaImmobile;

@Strippable(value = {"mcp.mobius.waila.api.IWailaDataProvider", "framesapi.IMoveCheck", "vazkii.botania.api.mana.ILaputaImmobile"})
public class BlockResourceNode extends BlockContainer implements IWailaDataProvider, IMoveCheck, ILaputaImmobile {

	private static IIcon crystalIcon;
	private static IIcon overlayIcon;

	public BlockResourceNode(Material mat) {
		super(mat);
		this.setCreativeTab(Satisforestry.tabCreative);
		this.setResistance(60000);
		this.setBlockUnbreakable();
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return meta == 0 ? new TileResourceNode() : null;
	}

	@Override
	public IIcon getIcon(IBlockAccess iba, int x, int y, int z, int s) {
		return SFBlocks.CAVESHIELD.getBlockInstance().getIcon(iba, x, y, z, s);
	}

	@Override
	public void registerBlockIcons(IIconRegister ico) {
		blockIcon = ico.registerIcon("satisforestry:resourcenode");

		overlayIcon = ico.registerIcon("satisforestry:resourcenode_overlay");
		crystalIcon = ico.registerIcon("satisforestry:resourcenode_crystal");
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer ep) {
		TileResourceNode te = (TileResourceNode)world.getTileEntity(x, y, z);
		te.onManualClick();
	}

	@Override
	public int getRenderType() {
		return Satisforestry.proxy.resourceRender;
	}

	@Override
	public final int getRenderBlockPass() {
		return 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderInPass(int pass) {
		SFClient.resource.setRenderPass(pass);
		return pass <= 1;
	}

	public static IIcon getCrystal() {
		return crystalIcon;
	}

	public static IIcon getOverlay() {
		return overlayIcon;
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
		if (te instanceof TileResourceNode) {
			tip.add(((TileResourceNode)te).purity.getDisplayName());
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

	public static class TileResourceNode extends TileCaveSpawner {

		private static final int MINING_TIME = 3; //just like in SF
		private static final int MANUAL_MINING_COOLDOWN = 15;

		private static WeightedRandom<ResourceItem> resourceSet = new WeightedRandom();

		private Purity purity = Purity.NORMAL;
		private ResourceItem resource;

		private int manualMiningCycle;
		private long lastClickTick = -1;
		//private int simpleAutoOutputTimer = purity.getCountdown();

		public TileResourceNode() {
			this.initSpawner();
		}

		private void initSpawner() {
			activeRadius = 5;
			spawnRadius = 3;
			mobLimit = 2;
			respawnTime = 50;

			this.setMobType(EntityEliteStinger.class);
		}

		public void generate(Random rand) {
			if (resourceSet.isEmpty()) {
				for (ResourceItem ri : BiomeConfig.instance.getResourceDrops()) {
					resourceSet.addEntry(ri, ri.spawnWeight);
				}
			}
			resourceSet.setRNG(rand);
			resource = resourceSet.getRandomEntry();
			purity = resource.getRandomPurity(rand);
			this.initSpawner();
		}

		@Override
		public void updateEntity() {
			super.updateEntity();
			if (!worldObj.isRemote) {
				if (resource == null) {
					this.generate(worldObj.rand);
					return;
				}/*
				if (SFOptions.SIMPLEAUTO.getState()) {
					if (simpleAutoOutputTimer > 0)
						simpleAutoOutputTimer--;
					if (simpleAutoOutputTimer == 0) {
						TileEntity te = worldObj.getTileEntity(xCoord, yCoord+1, zCoord);
						if (te instanceof IInventory && !(te instanceof TileNodeHarvester)) {
							ItemStack is = this.getRandomNodeItem();
							if (is != null) {
								if (ReikaInventoryHelper.addToIInv(is, (IInventory)te)) {
									this.resetTimer();
								}
							}
						}
					}
				}*/
				Collection<NodeEffect> c = resource.getEffects();
				if (c.isEmpty())
					return;
				AxisAlignedBB box = ReikaAABBHelper.getBlockAABB(this).expand(7, 1, 7);
				List<EntityPlayer> li = worldObj.getEntitiesWithinAABB(EntityPlayer.class, box);
				for (EntityPlayer ep : li) {
					for (NodeEffect e : c) {
						e.apply(this, ep);
					}
				}
			}
		}
		/*
		private void resetTimer() {
			simpleAutoOutputTimer = purity.getCountdown();
		}*/

		public void onManualClick() {
			long time = worldObj.getTotalWorldTime();
			long dur = time-lastClickTick;
			if (dur >= MANUAL_MINING_COOLDOWN) {
				lastClickTick = time;
				manualMiningCycle++;
				if (manualMiningCycle >= MINING_TIME) {
					ItemStack is = this.getRandomNodeItem();
					if (is != null)
						ReikaItemHelper.dropItem(worldObj, xCoord+0.5, yCoord+1, zCoord+0.5, is);
					manualMiningCycle = 0;
				}
			}
		}
		/*
		public float getAutomationProgress() {
			return 1F-(autoOutputTimer/(float)purity.getCountdown());
		}*/

		public float getManualProgress() {
			return manualMiningCycle/(float)MINING_TIME;
		}

		@Override
		public void writeToNBT(NBTTagCompound NBT) {
			super.writeToNBT(NBT);

			NBT.setInteger("cycle", manualMiningCycle);
			//NBT.setInteger("timer", autoOutputTimer);
			NBT.setLong("lastClick", lastClickTick);

			NBT.setInteger("purity", purity.ordinal());
			if (resource != null)
				NBT.setString("resource", resource.id);
		}

		@Override
		public void readFromNBT(NBTTagCompound NBT) {
			super.readFromNBT(NBT);

			manualMiningCycle = NBT.getInteger("cycle");
			//autoOutputTimer = NBT.getInteger("timer");
			lastClickTick = NBT.getLong("lastClick");

			purity = Purity.list[NBT.getInteger("purity")];
			if (NBT.hasKey("resource"))
				resource = BiomeConfig.instance.getResourceByID(NBT.getString("resource"));
		}

		public ResourceItem getResource() {
			return resource;
		}

		public ItemStack getRandomNodeItem() {
			return this.getRandomNodeItem(Integer.MAX_VALUE);
		}

		private ItemStack getRandomNodeItem(int tier) {
			ItemStack ri = resource.getRandomItem(tier, purity);
			if (ri == null)
				return null;
			boolean peaceful = worldObj.difficultySetting == EnumDifficulty.PEACEFUL;
			if (peaceful && !resource.worksOnPeaceful())
				return null;
			int amt = ReikaRandomHelper.getRandomBetween(resource.minCount, resource.maxCount);
			if (peaceful)
				amt = Math.max(1, (int)(amt*resource.peacefulYieldScale));
			return ReikaItemHelper.getSizedItemStack(ri, amt);
		}

		public Purity getPurity() {
			return purity;
		}

		public int getHarvestInterval() {
			return (int)(purity.getCountdown()/resource.speedFactor);
		}

	}

	public static enum Purity {
		IMPURE(0.5F),
		NORMAL(1),
		PURE(2);

		public final float yield;

		private static final int MINING_COOLDOWN = 10; //default resource node is 120/min = 2/s = 10t
		public static final Purity[] list = values();

		private Purity(float d) {
			yield = d;
		}

		public String getDisplayName() {
			if (this == NORMAL)
				return "Normal Purity";
			return ReikaStringParser.capFirstChar(this.name());
		}

		public int getCountdown() {
			return (int)(MINING_COOLDOWN/yield);
		}

		public Purity higher() {
			return this == PURE ? null : list[this.ordinal()+1];
		}

		public Purity lower() {
			return this == IMPURE ? null : list[this.ordinal()-1];
		}
	}
}
