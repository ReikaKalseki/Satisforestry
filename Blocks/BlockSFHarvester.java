package Reika.Satisforestry.Blocks;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.APIStripper.Strippable;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Base.BlockTEBase;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Miner.TileResourceHarvesterBase;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;


@Strippable(value = {"mcp.mobius.waila.api.IWailaDataProvider"})
public abstract class BlockSFHarvester extends BlockTEBase implements IWailaDataProvider {

	private final IIcon[] overlays = new IIcon[3];

	public BlockSFHarvester(Material mat) {
		super(mat);
		this.setCreativeTab(Satisforestry.tabCreative);
		this.setHardness(Blocks.mob_spawner.blockHardness);
		this.setResistance(30);
		this.setLightOpacity(0);
	}

	@Override
	public final void getSubBlocks(Item it, CreativeTabs tab, List li) {
		for (int i = 0; i < 3+this.getSurplusVariants(); i++) {
			li.add(new ItemStack(it, 1, i));
		}
	}

	@Override
	public final void registerBlockIcons(IIconRegister ico) {
		overlays[0] = ico.registerIcon("satisforestry:miner/rf");
		overlays[1] = ico.registerIcon("satisforestry:miner/eu");
		overlays[2] = ico.registerIcon("satisforestry:miner/rc");
	}

	@Override
	public final IIcon getIcon(int s, int meta) {
		return meta <= 2 ? overlays[meta] : blockIcon;
	}

	@Override
	public final int damageDropped(int meta) {
		return meta;
	}

	protected int getSurplusVariants() {
		return 0;
	}

	@Override
	public final boolean hasTileEntity(int meta) {
		return meta <= 2+this.getSurplusVariants();
	}

	@Override
	public final boolean isOpaqueCube() {
		return false;
	}

	@Override
	public final boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public final int getRenderType() {
		return -1;
	}

	@Override
	@ModDependent(ModList.WAILA)
	public final ItemStack getWailaStack(IWailaDataAccessor acc, IWailaConfigHandler config) {
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
		if (te instanceof TileResourceHarvesterBase) {
			((TileResourceHarvesterBase)te).addWaila(tip);
		}
		ReikaJavaLibrary.removeDuplicates(tip);
		return tip;
	}

	@ModDependent(ModList.WAILA)
	public final List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor acc, IWailaConfigHandler config) {
		return currenttip;
	}

	@Override
	public final NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x, int y, int z) {
		return tag;
	}

}
