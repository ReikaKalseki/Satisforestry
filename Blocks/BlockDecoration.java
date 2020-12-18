package Reika.Satisforestry.Blocks;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import Reika.Satisforestry.Satisforestry;

public class BlockDecoration extends Block {

	public BlockDecoration(Material mat) {
		super(mat);
		this.setCreativeTab(Satisforestry.tabCreative);
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return null;
	}

	@Override
	public int damageDropped(int meta) {
		return meta;
	}

	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
		return DecorationType.list[world.getBlockMetadata(x, y, z)].hardness;
	}

	@Override
	public float getExplosionResistance(Entity e, World world, int x, int y, int z, double ex, double ey, double ez) {
		return DecorationType.list[world.getBlockMetadata(x, y, z)].resistance;
	}

	@Override
	public void registerBlockIcons(IIconRegister ico) {
		for (int i = 0; i < DecorationType.list.length; i++) {
			DecorationType t = DecorationType.list[i];
			String base = "satisforestry:decoration/"+t.name().toLowerCase(Locale.ENGLISH);
			if (t.hasSideIcons()) {
				t.iconSide = ico.registerIcon(base+"_side");
				t.iconTop = ico.registerIcon(base+"_top");
				t.iconBottom = ico.registerIcon(base+"_bottom");
			}
			else {
				IIcon icon = ico.registerIcon(base);
				t.iconBottom = t.iconSide = t.iconTop = icon;
			}
		}
	}

	@Override
	public IIcon getIcon(int s, int meta) {
		DecorationType t = DecorationType.list[meta];
		switch(s) {
			case 0:
				return t.iconBottom;
			case 1:
				return t.iconTop;
			default:
				return t.iconSide;
		}
	}

	@Override
	public void getSubBlocks(Item it, CreativeTabs tab, List li) {
		for (int i = 0; i < DecorationType.list.length; i++) {
			li.add(new ItemStack(it, 1, i));
		}
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		DecorationType t = DecorationType.list[world.getBlockMetadata(x, y, z)];
		float w = t.width(x, y, z)/2F;
		this.setBlockBounds(0.5F-w, 0, 0.5F-w, 0.5F+w, t.height(x, y, z), 0.5F+w);
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	public static enum DecorationType {
		SPIKES("Stone Spikes", 0.75F, 5),
		;

		private IIcon iconTop;
		private IIcon iconSide;
		private IIcon iconBottom;

		public final String name;
		public final float hardness;
		public final float resistance;

		public static final DecorationType[] list = values();

		private DecorationType(String s, float h, float r) {
			hardness = h;
			resistance = r;
			name = s;
		}

		public float width(int x, int y, int z) {
			switch(this) {
				case SPIKES:
					return 0.25F;
				default:
					return 1;
			}
		}

		public float height(int x, int y, int z) {
			switch(this) {
				default:
					return 1;
			}
		}

		public boolean hasSideIcons() {
			switch(this) {
				default:
					return false;
			}
		}
	}

}
