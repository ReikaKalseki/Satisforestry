package Reika.Satisforestry.Blocks;

import java.util.List;
import java.util.Locale;

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

public class BlockTerrain extends Block {

	public BlockTerrain(Material mat) {
		super(mat);
		this.setCreativeTab(Satisforestry.tabCreative);
	}

	@Override
	public int damageDropped(int meta) {
		return meta;
	}

	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
		return TerrainType.list[world.getBlockMetadata(x, y, z)].hardness;
	}

	@Override
	public float getExplosionResistance(Entity e, World world, int x, int y, int z, double ex, double ey, double ez) {
		return TerrainType.list[world.getBlockMetadata(x, y, z)].resistance;
	}

	@Override
	public void registerBlockIcons(IIconRegister ico) {
		for (int i = 0; i < TerrainType.list.length; i++) {
			TerrainType t = TerrainType.list[i];
			String base = "satisforestry:terrain/"+t.name().toLowerCase(Locale.ENGLISH);
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
		TerrainType t = TerrainType.list[meta];
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
		for (int i = 0; i < TerrainType.list.length; i++) {
			li.add(new ItemStack(it, 1, i));
		}
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		TerrainType t = TerrainType.list[world.getBlockMetadata(x, y, z)];
		float w = t.width()/2F;
		this.setBlockBounds(0.5F-w, 0, 0.5F-w, 0.5F+w, t.height(), 0.5F+w);
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	public static enum TerrainType {
		POISONROCK("Spore Rock", 3, 45),
		PONDROCK("Pond Rock", 1, 30),
		SPIKES("Spikes", 0.75F, 5),
		;

		private IIcon iconTop;
		private IIcon iconSide;
		private IIcon iconBottom;

		public final String name;
		public final float hardness;
		public final float resistance;

		public static final TerrainType[] list = values();

		private TerrainType(String s, float h, float r) {
			hardness = h;
			resistance = r;
			name = s;
		}

		public float width() {
			switch(this) {
				case SPIKES:
					return 0.25F;
				default:
					return 1;
			}
		}

		public float height() {
			switch(this) {
				default:
					return 1;
			}
		}

		public boolean hasSideIcons() {
			switch(this) {
				case POISONROCK:
				case SPIKES:
					return true;
				case PONDROCK:
					return false;
			}
			return false;
		}
	}

}
