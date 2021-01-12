package Reika.Satisforestry.Blocks;

import java.util.List;
import java.util.Random;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Base.BlockCustomLeaf;
import Reika.DragonAPI.Libraries.Rendering.ReikaColorAPI;
import Reika.Satisforestry.Satisforestry;

public class BlockPinkLeaves extends BlockCustomLeaf {

	public static enum LeafTypes {
		TREE,
		GIANTTREE,
		JUNGLE;

		private final String fastIcon;
		private final String fancyIcon;

		private static final LeafTypes[] list = values();

		private LeafTypes() {
			String base = "satisforestry:pinkleaf";//_"+this.name().toLowerCase(Locale.ENGLISH);
			fancyIcon = base+"_fancy";
			fastIcon = base+"_fast";
		}

		public boolean isValidLogMeta(int meta) {
			return meta%4 == this.ordinal();
		}

		public int getMetaDropped() {
			switch(this) {
				case GIANTTREE:
					return TREE.getMetaDropped();
				default:
					return this.ordinal();
			}
		}
	}

	public BlockPinkLeaves() {
		super();
		this.setLightOpacity(0);
		this.setCreativeTab(Satisforestry.tabCreative);
		this.setStepSound(soundTypeGrass);
	}

	@Override
	public void getSubBlocks(Item i, CreativeTabs cr, List li) {
		for (int m = 0; m < LeafTypes.list.length; m++)
			li.add(new ItemStack(i, 1, m));
	}

	@Override
	public int getLightOpacity(IBlockAccess world, int x, int y, int z) {
		return 0;
	}

	@Override
	public int getRenderColor(int mta) {
		return Satisforestry.pinkforest.getBiomeFoliageColor(0, 64, 0);
	}

	@Override
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		LeafTypes l = this.getLeafType(world, x, y, z);
		if (l == LeafTypes.GIANTTREE) {
			y -= 60; //was 18 then 24 then 50
		}
		int ret = Satisforestry.pinkforest.getBiomeFoliageColor(x, y, z);
		if (l == LeafTypes.JUNGLE) {
			ret = ReikaColorAPI.getModifiedHue(ret, 355);
			ret = ReikaColorAPI.getModifiedSat(ret, 0.95F);
			ret = ReikaColorAPI.getColorWithBrightnessMultiplier(ret, 0.8F);
		}
		return ret;
	}

	public static LeafTypes getLeafType(IBlockAccess world, int x, int y, int z) {
		return getLeafType(world.getBlockMetadata(x, y, z));
	}

	public static LeafTypes getLeafType(int meta) {
		return LeafTypes.list[meta%8];
	}
	/*
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ico) {
		blockIcon = ico.registerIcon("Satisforestry:pink-tree-leaf");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int s, int meta) {
		return blockIcon;
	}
	 */
	@Override
	public boolean shouldRandomTick() {
		return true;
	}

	@Override
	public boolean decays() {
		return true;
	}

	@Override
	public boolean isNatural() {
		return true;
	}

	@Override
	public boolean allowModDecayControl() {
		return true;
	}

	@Override
	public boolean showInCreative() {
		return true;
	}

	@Override
	public CreativeTabs getCreativeTab() {
		return Satisforestry.tabCreative;
	}

	@Override
	public boolean shouldTryDecay(World world, int x, int y, int z, int meta) {
		return true;
	}

	@Override
	public String getFastGraphicsIcon(int meta) {
		return this.getLeafType(meta).fastIcon;
	}

	@Override
	public String getFancyGraphicsIcon(int meta) {
		return this.getLeafType(meta).fancyIcon;
	}

	@Override
	public boolean isMatchingLeaf(IBlockAccess iba, int thisX, int thisY, int thisZ, int lookX, int lookY, int lookZ) {
		return iba.getBlock(lookX, lookY, lookZ) == iba.getBlock(thisX, thisY, thisZ);// && this.getLeafType(iba, lookX, lookY, lookZ) == this.getLeafType(iba, thisX, thisY, thisZ);
	}

	@Override
	public boolean isValidLog(IBlockAccess iba, int thisX, int thisY, int thisZ, int lookX, int lookY, int lookZ) {
		return iba.getBlock(lookX, lookY, lookZ) == Satisforestry.log;// && this.getLeafType(iba, thisX, thisY, thisZ).isValidLogMeta(iba.getBlockMetadata(lookX, lookY, lookZ));
	}

	@Override
	protected int getNumberSidesToPropagate(World world, int x, int y, int z) {
		return 1;
	}

	@Override
	public int getMaximumLogSearchRadius() {
		return 18;
	}

	@Override
	public int getMaximumLogSearchDepth() {
		return 12;
	}

	@Override
	protected void onLeafDecay(World world, int x, int y, int z) {

	}

	@Override
	public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		return 0;
	}

	@Override
	public boolean isFlammable(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		return false;
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		return 0;
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return Item.getItemFromBlock(this);
	}

	@Override
	public int quantityDropped(Random rand) {
		return 1;
	}

	@Override
	public int damageDropped(int meta) {
		return this.getLeafType(meta).getMetaDropped();
	}

	@Override
	protected int getMetaLimit() {
		return LeafTypes.list.length;
	}

}
