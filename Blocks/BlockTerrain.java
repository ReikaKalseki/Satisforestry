package Reika.Satisforestry.Blocks;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
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
			t.iconSide = ico.registerIcon(base+"_side");
			t.iconTop = ico.registerIcon(base+"_top");
			t.iconBottom = ico.registerIcon(base+"_bottom");
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

	public static enum TerrainType {
		POISONROCK("Spore Rock", 3, 45),
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
	}

}
