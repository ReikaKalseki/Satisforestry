package Reika.Satisforestry.Blocks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Instantiable.MetadataItemBlock;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.Satisforestry.UpgradeHandler;
import Reika.Satisforestry.Blocks.BlockPowerSlug.TilePowerSlug;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


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
			te.setDirection(ForgeDirection.VALID_DIRECTIONS[side].getOpposite());
			te.angle = world.rand.nextFloat()*360;
		}
		return flag;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack is, EntityPlayer ep, List li, boolean vb) {
		ArrayList<ItemStack> li2 = UpgradeHandler.instance.getSlugEquivalents(is.getItemDamage()%3);
		if (!li2.isEmpty()) {
			li.add("Usable as upgrades:");
			for (ItemStack is2 : li2) {
				li.add("  "+is2.getDisplayName()+" ("+ReikaItemHelper.getRegistrantMod(is2)+")");
			}
		}
	}

	@Override
	public boolean isValidArmor(ItemStack stack, int type, Entity e) {
		return type == 0 && e instanceof EntityPlayer;
	}

}
