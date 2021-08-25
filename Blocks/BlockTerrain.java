package Reika.Satisforestry.Blocks;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Rendering.ReikaRenderHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Registry.SFBlocks;

public class BlockTerrain extends Block {

	public BlockTerrain(Material mat) {
		super(mat);
		this.setCreativeTab(Satisforestry.tabCreative);
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		Item override = TerrainType.list[meta].getDrop();
		return override != null ? override : super.getItemDropped(meta, rand, fortune);
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

	@Override
	public void breakBlock(World world, int x, int y, int z, Block old, int oldmeta) {
		super.breakBlock(world, x, y, z, old, oldmeta);
		for (int i = 0; i < 6; i++) {
			ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i];
			int dx = x+dir.offsetX;
			int dy = y+dir.offsetY;
			int dz = z+dir.offsetZ;
			Block b = world.getBlock(dx, dy, dz);
			if ((b == SFBlocks.CAVESHIELD.getBlockInstance() && world.getBlockMetadata(x, y, z) == 0) || b == SFBlocks.SPAWNER.getBlockInstance()) {
				if (world.isRemote)
					ReikaRenderHelper.spawnDropParticles(world, dx, dy, dz, b, 0);
				else
					world.setBlock(dx, dy, dz, this, TerrainType.CAVECRACKS.ordinal(), 3);
				ReikaSoundHelper.playBreakSound(world, dx, dy, dz, Blocks.stone);
			}
		}
	}

	public static enum TerrainType {
		POISONROCK(3, 45),
		PONDROCK(1, 30),
		OUTCROP(2, 30),
		CAVECRACKS(Blocks.stone.blockHardness*1.5F, 6000),
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

		public Item getDrop() {
			switch(this) {
				case CAVECRACKS:
					return Item.getItemFromBlock(Blocks.cobblestone);
				default:
					return null;
			}
		}

		public boolean hasSideIcons() {
			switch(this) {
				case POISONROCK:
					return true;
				case PONDROCK:
				case OUTCROP:
				case CAVECRACKS:
					return false;
			}
			return false;
		}

		public boolean isTerrain() {
			switch(this) {
				case POISONROCK:
					return false;
				case PONDROCK:
				case OUTCROP:
				case CAVECRACKS:
					return true;
			}
			return false;
		}
	}

}
