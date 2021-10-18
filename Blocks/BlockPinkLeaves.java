package Reika.Satisforestry.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Base.BlockCustomLeaf;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.RotaryCraft.API.Interfaces.LeafBlockWithExtras;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.Generator.PinkTreeGeneratorBase.PinkTreeTypes;
import Reika.Satisforestry.Blocks.BlockPinkGrass.GrassTypes;
import Reika.Satisforestry.Registry.SFBlocks;

public class BlockPinkLeaves extends BlockCustomLeaf implements LeafBlockWithExtras {

	public BlockPinkLeaves() {
		super();
		this.setLightOpacity(0);
		this.setCreativeTab(Satisforestry.tabCreative);
		this.setStepSound(soundTypeGrass);
	}

	@Override
	public void getSubBlocks(Item i, CreativeTabs cr, List li) {
		for (int m = 0; m < PinkTreeTypes.list.length; m++)
			li.add(new ItemStack(i, 1, m));
	}

	@Override
	public int getLightOpacity(IBlockAccess world, int x, int y, int z) {
		return 0;
	}

	@Override
	public int getRenderColor(int meta) {
		return PinkTreeTypes.getLeafType(meta).getBasicRenderColor();
	}

	@Override
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		return PinkTreeTypes.getLeafType(world, x, y, z).getRenderColor(world, x, y, z);
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
		return PinkTreeTypes.getLeafType(meta).fastIcon;
	}

	@Override
	public String getFancyGraphicsIcon(int meta) {
		return PinkTreeTypes.getLeafType(meta).fancyIcon;
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
	public int quantityDropped(Random rand) {
		return 1;
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return Item.getItemFromBlock(SFBlocks.SAPLING.getBlockInstance());
	}

	@Override
	protected int func_150123_b(int meta) {
		return (int)(100/PinkTreeTypes.getLeafType(meta).getSaplingDropChance());
	}

	@Override
	public int damageDropped(int meta) {
		return PinkTreeTypes.getLeafType(meta).getTypeDropped().ordinal();
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> ret = super.getDrops(world, x, y, z, metadata, fortune);
		if (ReikaRandomHelper.doWithChance(fortune*fortune*0.004*PinkTreeTypes.getLeafType(metadata).getBerryModifier())) {
			ret.add(new ItemStack(Satisforestry.paleberry, 1, 1));
		}
		return ret;
	}

	@Override
	protected int getMetaLimit() {
		return PinkTreeTypes.list.length;
	}

	@Override
	public void onPreWoodcutterBreak(World world, int x, int y, int z) {
		int y2 = y;
		while (world.getBlock(x, y2-1, z) == SFBlocks.GRASS.getBlockInstance() && world.getBlockMetadata(x, y2-1, z) == GrassTypes.TREE_VINE.ordinal()) {
			y2--;
		}
		for (; y2 < y; y2++) {
			world.setBlockToAir(x, y2, z);
		}
	}

	@Override
	public ArrayList<ItemStack> getExtraDrops(World world, int x, int y, int z, int fortune) {
		ArrayList<ItemStack> ret = new ArrayList();
		int y2 = y;
		while (world.getBlock(x, y2-1, z) == SFBlocks.GRASS.getBlockInstance() && world.getBlockMetadata(x, y2-1, z) == GrassTypes.TREE_VINE.ordinal()) {
			y2--;
			ret.addAll(SFBlocks.GRASS.getBlockInstance().getDrops(world, x, y2, z, GrassTypes.TREE_VINE.ordinal(), fortune));
		}
		return ret;
	}

}
