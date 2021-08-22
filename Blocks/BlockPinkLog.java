package Reika.Satisforestry.Blocks;

import java.util.List;

import net.minecraft.block.BlockOldLog;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.Generator.PinkTreeGeneratorBase.PinkTreeTypes;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class BlockPinkLog extends BlockOldLog {

	private IIcon jungleIcon;

	public BlockPinkLog() {
		super();
		this.setResistance(120);
		this.setCreativeTab(Satisforestry.tabCreative);
		this.setStepSound(soundTypeWood);
	}

	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
		return super.getBlockHardness(world, x, y, z)*PinkTreeTypes.getLogType(world, x, y, z).getHardnessMultiplier();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ico) {
		blockIcon = ico.registerIcon("Satisforestry:pink-log");
		jungleIcon = ico.registerIcon("Satisforestry:jungle-log");
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected IIcon getSideIcon(int meta) {
		if (meta%4 == 2)
			return jungleIcon;
		return blockIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected IIcon getTopIcon(int meta) {
		return Blocks.log.getIcon(1, 0);
	}

	@Override
	public void getSubBlocks(Item i, CreativeTabs cr, List li) {
		li.add(new ItemStack(i));
		li.add(new ItemStack(i, 1, 2));
	}

	@Override
	public int damageDropped(int worldMeta) {
		int base = worldMeta & 3;
		return base == 1 ? 0 : base;
	}

	@Override
	public int getLightOpacity(IBlockAccess world, int x, int y, int z) {
		return this.isTransparent(world, x, y, z) ? 0 : super.getLightOpacity(world, x, y, z);
	}

	private boolean isTransparent(IBlockAccess world, int x, int y, int z) {
		return world.getBlockMetadata(x, y, z)%4 == 1;
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

}
