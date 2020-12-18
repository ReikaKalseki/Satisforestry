package Reika.Satisforestry.Blocks;

import java.util.List;
import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.Satisforestry.SFBlocks;
import Reika.Satisforestry.Satisforestry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class BlockPinkGrass extends BlockTallGrass {

	public static enum GrassTypes {
		PEACH_FRINGE(),
		TINY_PINK_LUMPS(),
		RED_STRANDS_1(),
		RED_STRANDS_2(),
		BLUE_MUSHROOM_STALK("Blue Mushroom"),
		BLUE_MUSHROOM_TOP("Blue Mushroom"),
		VINE("Cave Vine"),
		;

		public final String name;
		private IIcon icon;

		public static final GrassTypes[] list = values();

		private GrassTypes() {
			this(null);
		}

		private GrassTypes(String s) {
			name = s;
		}

		public float getHeight() {
			switch(this) {
				case PEACH_FRINGE:
					return 1;
				case RED_STRANDS_1:
				case RED_STRANDS_2:
					return 0.375F;
				case TINY_PINK_LUMPS:
					return 0.125F;
				case BLUE_MUSHROOM_TOP:
					return 0.75F;
				default:
					return 1;
			}
		}

		public boolean isSelfColor() {
			switch(this) {
				case PEACH_FRINGE:
				case RED_STRANDS_1:
				case RED_STRANDS_2:
				case BLUE_MUSHROOM_STALK:
				case BLUE_MUSHROOM_TOP:
					return true;
				default:
					return false;
			}
		}

		public ForgeDirection getDirection() {
			switch(this) {
				case VINE:
					return ForgeDirection.DOWN;
				default:
					return ForgeDirection.UP;
			}
		}

		public boolean canExistAt(World world, int x, int y, int z) {
			ForgeDirection side = this.getDirection();
			int dx = x-side.offsetX;
			int dy = y-side.offsetY;
			int dz = z-side.offsetZ;
			Block at = world.getBlock(dx, dy, dz);
			Block b = SFBlocks.GRASS.getBlockInstance();
			switch(this) {
				case VINE:
				case BLUE_MUSHROOM_STALK:
					return at.isSideSolid(world, dx, dy, dz, side) || (at == b && world.getBlockMetadata(dx, dy, dz) == this.ordinal());
				case BLUE_MUSHROOM_TOP:
					return at == b && world.getBlockMetadata(dx, dy, dz) == BLUE_MUSHROOM_STALK.ordinal();
				default:
					return at.canSustainPlant(world, dx, dy, dz, side, (IPlantable)b);
			}
		}

		public int getLight() {
			switch(this) {
				case BLUE_MUSHROOM_TOP:
					return 8;
				default:
					return 0;
			}
		}
	}

	public BlockPinkGrass() {
		this.setCreativeTab(Satisforestry.tabCreative);
		this.setStepSound(soundTypeGrass);
	}

	@Override
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		GrassTypes gr = GrassTypes.list[world.getBlockMetadata(x, y, z)];
		return gr.isSelfColor() ? 0xffffff : super.colorMultiplier(world, x, y, z);
	}

	@Override
	public void registerBlockIcons(IIconRegister ico) {
		for (int i = 0; i < GrassTypes.list.length; i++) {
			GrassTypes gr = GrassTypes.list[i];
			gr.icon = ico.registerIcon("Satisforestry:foliage/"+gr.name().toLowerCase(Locale.ENGLISH));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int s, int meta) {
		return GrassTypes.list[meta].icon;
	}

	@Override
	public void getSubBlocks(Item it, CreativeTabs cr, List li) {
		for (int i = 0; i < GrassTypes.list.length; i++) {
			li.add(new ItemStack(it, 1, i));
		}
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
		return EnumPlantType.Plains;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		GrassTypes gr = GrassTypes.list[world.getBlockMetadata(x, y, z)];
		float f = 0.4F; //from parent
		this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, gr.getHeight(), 0.5F + f);
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		GrassTypes gr = GrassTypes.list[world.getBlockMetadata(x, y, z)];
		return gr.canExistAt(world, x, y, z);
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		return GrassTypes.list[world.getBlockMetadata(x, y, z)].getLight();
	}

}
