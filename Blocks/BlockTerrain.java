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
	public boolean isReplaceableOreGen(World world, int x, int y, int z, Block target) {
		return super.isReplaceableOreGen(world, x, y, z, target);
	}

	public static enum TerrainType {
		POISONROCK(3, 45),
		PONDROCK(1, 30),
		OUTCROP(2, 30),
		;

		private IIcon iconTop;
		private IIcon iconSide;
		private IIcon iconBottom;

		public final float hardness;
		public final float resistance;

		public static final TerrainType[] list = values();

		private TerrainType(float h, float r) {
			hardness = h;
			resistance = r;
		}

		public boolean hasSideIcons() {
			switch(this) {
				case POISONROCK:
					return true;
				case PONDROCK:
				case OUTCROP:
					return false;
			}
			return false;
		}
	}

}
