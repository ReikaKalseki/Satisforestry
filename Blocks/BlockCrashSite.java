package Reika.Satisforestry.Blocks;

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.APIStripper.Strippable;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Instantiable.Data.BlockStruct.StructuredBlockArray;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Interfaces.TileEntity.InertIInv;
import Reika.DragonAPI.Libraries.ReikaInventoryHelper;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.ModRegistry.PowerTypes;
import Reika.RotaryCraft.API.Power.ShaftPowerReceiver;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Config.AlternateRecipe;
import Reika.Satisforestry.Config.BiomeConfig;
import Reika.Satisforestry.Registry.SFBlocks;
import Reika.Satisforestry.Registry.SFSounds;

import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import framesapi.IMoveCheck;
import ic2.api.energy.tile.IEnergySink;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import vazkii.botania.api.mana.ILaputaImmobile;

@Strippable(value = {"mcp.mobius.waila.api.IWailaDataProvider", "framesapi.IMoveCheck", "vazkii.botania.api.mana.ILaputaImmobile"})
public class BlockCrashSite extends BlockContainer implements IWailaDataProvider, IMoveCheck, ILaputaImmobile {

	public BlockCrashSite(Material mat) {
		super(mat);
		this.setCreativeTab(Satisforestry.tabCreative);
		this.setResistance(60000);
		this.setBlockUnbreakable();
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return meta == 0 ? new TileCrashSite() : null;
	}

	@Override
	public IIcon getIcon(int s, int meta) {
		switch(meta) {
			case 0:
				return blockIcon;
			case 1:
			case 2:
			case 3:
			case 4:
				return SFBlocks.MINERMULTI.getBlockInstance().getIcon(0, meta-1);
			default:
				return Blocks.bedrock.blockIcon;
		}
	}

	@Override
	public void registerBlockIcons(IIconRegister ico) {
		blockIcon = ico.registerIcon("satisforestry:crashsite");
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
		if (te instanceof TileCrashSite) {
			((TileCrashSite)te).addWaila(tip);
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

	@Override
	public final boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int s, float a, float b, float c) {
		TileEntity te = this.getTileEntityForPosition(world, x, y, z);
		//ReikaJavaLibrary.pConsole(te);
		if (te instanceof TileCrashSite) {
			world.markBlockForUpdate(x, y, z);
			if (!((TileCrashSite)te).isOpened)
				ep.openGui(Satisforestry.instance, 0, world, te.xCoord, te.yCoord, te.zCoord);
			return true;
		}
		return false;
	}

	protected final TileEntity getTileEntityForPosition(World world, int x, int y, int z) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		blocks.extraSpread = true;
		int s = 8;
		int sy = 8;
		blocks.recursiveMultiAddWithBounds(world, x, y, z, x-s, y-sy, z-s, x+s, y+sy, z+s, this, SFBlocks.CRASHSITE.getBlockInstance());
		for (int i = 0; i < blocks.getSize(); i++) {
			Coordinate c = blocks.getNthBlock(i);
			//ReikaJavaLibrary.pConsole(b);
			if (c.getBlock(world) == SFBlocks.CRASHSITE.getBlockInstance() && c.getBlockMetadata(world) == 0)
				return c.getTileEntity(world);
		}
		return null;
	}

	@Strippable(value={"ic2.api.energy.tile.IEnergySink"})
	public static class TileCrashSite extends TileEntity implements InertIInv, IEnergyReceiver, IEnergySink, ShaftPowerReceiver {

		private static WeightedRandom<AlternateRecipe> recipeSet = new WeightedRandom();

		private AlternateRecipe recipe;

		private ItemStack currentItem;

		private long ticksEnoughPower;

		private boolean isOpened = false;

		private long powerAmount;

		private int omega;
		private int torque;

		public float progressFactor;

		public void generate(Random rand) {
			if (recipeSet.isEmpty()) {
				for (AlternateRecipe ri : BiomeConfig.instance.getAlternateRecipes()) {
					recipeSet.addEntry(ri, ri.spawnWeight);
				}
			}
			recipeSet.setRNG(rand);
			recipe = recipeSet.getRandomEntry();
		}

		public void addWaila(List<String> tip) {
			if (recipe != null)
				tip.add("Stores "+recipe.getDisplayName());
		}

		@Override
		public void updateEntity() {
			super.updateEntity();
			if (worldObj.isRemote) {

			}
			else if (recipe != null && !isOpened) {
				boolean met = this.isRequirementMet();
				if (recipe.unlockPower != null) {
					if (powerAmount >= recipe.unlockPower.amount) {
						ticksEnoughPower++;
					}
					else {
						ticksEnoughPower = 0;
					}
					progressFactor = ticksEnoughPower/(float)recipe.unlockPower.ticksToHold;
				}
				powerAmount = 0;
				if (met != this.isRequirementMet())
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}

		public boolean tryOpen(EntityPlayerMP ep) {
			if (isOpened)
				return false;
			if (this.isRequirementMet() && !recipe.playerHas(ep.worldObj, ep.getUniqueID())) {
				recipe.giveToPlayer(ep);
				currentItem = null;
				isOpened = true;
				this.markDirty();
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				ep.closeScreen();
				return true;
			}
			else {
				return false;
			}
		}

		public AlternateRecipe getRecipe() {
			return recipe;
		}

		public boolean isRequirementMet() {
			return recipe != null && recipe.matchesItem(currentItem) && (recipe.unlockPower == null || ticksEnoughPower >= recipe.unlockPower.ticksToHold);
		}

		@Override
		public void writeToNBT(NBTTagCompound NBT) {
			super.writeToNBT(NBT);

			if (recipe != null)
				NBT.setString("recipe", recipe.id);

			NBT.setBoolean("open", isOpened);
			NBT.setLong("enough", ticksEnoughPower);

			if (currentItem != null) {
				NBTTagCompound tag = new NBTTagCompound();
				currentItem.writeToNBT(tag);
				NBT.setTag("item", tag);
			}
		}

		@Override
		public void readFromNBT(NBTTagCompound NBT) {
			super.readFromNBT(NBT);

			if (NBT.hasKey("recipe"))
				recipe = BiomeConfig.instance.getAltRecipeByID(NBT.getString("recipe"));

			isOpened = NBT.getBoolean("open");
			ticksEnoughPower = NBT.getLong("enough");

			NBTTagCompound tag = NBT.getCompoundTag("item");
			currentItem = tag.hasNoTags() ? null : ItemStack.loadItemStackFromNBT(tag);
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

		public final int[] getAccessibleSlotsFromSide(int var1) {
			return new int[0];
		}

		public final boolean canInsertItem(int i, ItemStack is, int side) {
			return false;
		}

		public final ItemStack getStackInSlot(int par1) {
			return par1 == 0 ? currentItem : null;
		}

		public final void setInventorySlotContents(int par1, ItemStack is) {
			if (par1 == 0) {
				currentItem = is;
				if (worldObj != null)
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}

		public boolean isUseableByPlayer(EntityPlayer var1) {
			return var1.getDistanceSq(xCoord+0.5, yCoord+0.5, zCoord+0.5) <= 8;
		}

		public final ItemStack decrStackSize(int par1, int par2) {
			return ReikaInventoryHelper.decrStackSize(this, par1, par2);
		}

		public final ItemStack getStackInSlotOnClosing(int par1) {
			return ReikaInventoryHelper.getStackInSlotOnClosing(this, par1);
		}

		public final String getInventoryName() {
			return recipe.getDisplayName();
		}

		public void openInventory() {}

		public void closeInventory() {}

		@Override
		public final boolean hasCustomInventoryName() {
			return true;
		}

		@Override
		public int getSizeInventory() {
			return 1;
		}

		@Override
		public int getInventoryStackLimit() {
			return 64;
		}

		@Override
		public boolean isItemValidForSlot(int slot, ItemStack is) {
			return slot == 0;
		}

		@Override
		public boolean canConnectEnergy(ForgeDirection from) {
			return recipe != null && recipe.unlockPower != null && recipe.unlockPower.type == PowerTypes.RF;
		}

		@Override
		public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) {
			return recipe != null && recipe.unlockPower != null && recipe.unlockPower.type == PowerTypes.EU;
		}

		@Override
		public boolean canReadFrom(ForgeDirection dir) {
			return true;
		}

		@Override
		public boolean isReceiving() {
			return recipe != null && recipe.unlockPower != null && recipe.unlockPower.type == PowerTypes.ROTARYCRAFT;
		}

		@Override
		public int getMinTorque(int available) {
			return 1;
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
			return recipe != null && recipe.unlockPower != null && recipe.unlockPower.type == PowerTypes.ROTARYCRAFT ? powerAmount : 0;
		}

		@Override
		public String getName() {
			return this.getInventoryName();
		}

		@Override
		public int getIORenderAlpha() {
			return 0;
		}

		@Override
		public void setIORenderAlpha(int io) {

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
			powerAmount = power;
		}

		@Override
		public void noInputMachine() {
			this.setOmega(0);
			this.setTorque(0);
			this.setPower(0);
		}

		@Override
		public double getDemandedEnergy() {
			return 1000000;
		}

		@Override
		public int getSinkTier() {
			return 4;
		}

		@Override
		public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage) {
			powerAmount += (long)amount;
			return 0;
		}

		@Override
		public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
			if (!simulate)
				powerAmount += maxReceive;
			return maxReceive;
		}

		@Override
		public int getEnergyStored(ForgeDirection from) {
			return 0;
		}

		@Override
		public int getMaxEnergyStored(ForgeDirection from) {
			return Integer.MAX_VALUE;
		}

		@SideOnly(Side.CLIENT)
		public static void reactToLockGuiStatus(boolean success) {
			if (success)
				ReikaSoundHelper.playClientSound(SFSounds.CRASHOPEN, Minecraft.getMinecraft().thePlayer, 1, 1);
			else
				ReikaSoundHelper.playClientSound(SFSounds.CRASHFAIL, Minecraft.getMinecraft().thePlayer, 1, 1);
		}

	}
}
