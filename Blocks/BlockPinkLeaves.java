package Reika.Satisforestry.Blocks;

import java.util.Random;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import Reika.DragonAPI.Base.BlockCustomLeaf;
import Reika.Satisforestry.Satisforestry;

public class BlockPinkLeaves extends BlockCustomLeaf {

	public static enum LeafTypes {
		TREE,
		GIANTTREE;

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
	}

	public BlockPinkLeaves() {
		super();
		this.setLightOpacity(0);
		this.setCreativeTab(Satisforestry.tabCreative);
		this.setStepSound(soundTypeGrass);
	}

	@Override
	public int getRenderColor(int mta) {
		return Satisforestry.pinkforest.getBiomeFoliageColor(0, 64, 0);
	}

	@Override
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		LeafTypes l = this.getLeafType(world, x, y, z);
		if (l == LeafTypes.GIANTTREE) {
			y -= 50; //was 18 then 24
		}
		return Satisforestry.pinkforest.getBiomeFoliageColor(x, y, z);
	}

	private LeafTypes getLeafType(IBlockAccess world, int x, int y, int z) {
		return this.getLeafType(world.getBlockMetadata(x, y, z));
	}

	private LeafTypes getLeafType(int meta) {
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
		return iba.getBlock(lookX, lookY, lookZ) == iba.getBlock(thisX, thisY, thisZ) && this.getLeafType(iba, lookX, lookY, lookZ) == this.getLeafType(iba, thisX, thisY, thisZ);
	}

	@Override
	public boolean isValidLog(IBlockAccess iba, int thisX, int thisY, int thisZ, int lookX, int lookY, int lookZ) {
		return iba.getBlock(lookX, lookY, lookZ) == Satisforestry.log && this.getLeafType(iba, thisX, thisY, thisZ).isValidLogMeta(iba.getBlockMetadata(lookX, lookY, lookZ));
	}

	@Override
	public int getMaximumLogSearchRadius() {
		return 18;
	}

	@Override
	public int getMaximumLogSearchDepth() {
		return 8;
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
		return 0;
	}

	@Override
	protected int getMetaLimit() {
		return LeafTypes.list.length;
	}

}
