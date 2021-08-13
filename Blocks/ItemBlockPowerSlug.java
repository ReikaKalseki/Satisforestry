package Reika.Satisforestry.Blocks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.MetadataItemBlock;
import Reika.Satisforestry.Blocks.BlockPowerSlug.TilePowerSlug;


public class ItemBlockPowerSlug extends MetadataItemBlock {

	public ItemBlockPowerSlug(Block b) {
		super(b);
	}

	@Override
	public int getMetadata(int meta) {
		return meta;//%3+3;
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
		boolean flag = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
		if (flag) {
			TilePowerSlug te = (TilePowerSlug)world.getTileEntity(x, y, z);
			te.setNoSpawns();
			te.angle = world.rand.nextFloat()*360;
		}
		return flag;
	}

}
