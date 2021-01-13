package Reika.Satisforestry;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import Reika.DragonAPI.Instantiable.MetadataItemBlock;
import Reika.Satisforestry.Biome.Generator.PinkTreeGeneratorBase.PinkTreeTypes;
import Reika.Satisforestry.Blocks.BlockPinkSapling;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class ItemPinkSapling extends MetadataItemBlock {

	public ItemPinkSapling(Block b) {
		super(b);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass(int meta, int pass) {
		return BlockPinkSapling.getIconLayer(pass);
	}

	@Override
	public int getRenderPasses(int metadata) {
		return 2;
	}

	@Override
	public int getColorFromItemStack(ItemStack stack, int pass) {
		switch(pass) {
			case 0:
				return 0xffffff;
			case 1:
				return PinkTreeTypes.getLeafType(stack.getItemDamage()).getBasicRenderColor();
		}
		return 0xffffff;
	}


}
