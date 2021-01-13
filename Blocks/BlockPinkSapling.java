package Reika.Satisforestry.Blocks;

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockSapling;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import Reika.DragonAPI.Libraries.Rendering.ReikaRenderHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockPinkLeaves.LeafTypes;
import Reika.Satisforestry.Registry.SFBlocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPinkSapling extends BlockSapling {

	private static IIcon base;
	private static IIcon overlay;

	public BlockPinkSapling() {
		super();
		this.setCreativeTab(Satisforestry.tabCreative);
		this.setStepSound(soundTypeGrass);
	}

	@Override
	public IIcon getIcon(int s, int meta) {
		return base;
	}

	@Override
	public void registerBlockIcons(IIconRegister ico) {
		base = ico.registerIcon("satisforestry:foliage/sapling_base");
		overlay = ico.registerIcon("satisforestry:foliage/sapling_overlay");
	}

	@Override
	public void func_149878_d(World world, int x, int y, int z, Random rand) {
		BlockPinkLeaves.getLeafType(world, x, y, z).getTreeGenerator().generate(world, rand, x, y, z);
	}

	@Override
	public int getRenderType() {
		return SFBlocks.GRASS.getBlockInstance().getRenderType();
	}

	@Override
	public void getSubBlocks(Item i, CreativeTabs cr, List li) {
		for (LeafTypes l : LeafTypes.values())
			li.add(new ItemStack(i, 1, l.ordinal()));
	}

	@SideOnly(Side.CLIENT)
	public static void render(IBlockAccess world, int x, int y, int z, RenderBlocks rb) {
		LeafTypes type = BlockPinkLeaves.getLeafType(world, x, y, z);
		double h = type == LeafTypes.GIANTTREE ? 1.5 : 1;
		Tessellator.instance.setColorOpaque_I(0xffffff);
		ReikaRenderHelper.renderCrossTex(world, x, y, z, base, Tessellator.instance, rb, h);
		Tessellator.instance.setColorOpaque_I(SFBlocks.LEAVES.getBlockInstance().colorMultiplier(world, x, y, z));
		ReikaRenderHelper.renderCrossTex(world, x, y, z, overlay, Tessellator.instance, rb, h);
	}

	public static IIcon getIconLayer(int layer) {
		switch(layer) {
			case 0:
				return base;
			case 1:
				return overlay;
		}
		return Blocks.bedrock.blockIcon;
	}

}
