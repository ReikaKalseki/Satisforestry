package Reika.Satisforestry.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Instantiable.Effects.ReikaModelledBreakFX;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.Rendering.ReikaRenderHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Registry.SFBlocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockTerrain extends Block {

	public BlockTerrain(Material mat) {
		super(mat);
		this.setCreativeTab(Satisforestry.tabCreative);
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return super.getItemDropped(meta, rand, fortune);
	}

	@Override
	public int damageDropped(int meta) {
		return meta;
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
		ItemStack override = TerrainType.list[meta].getDrop();
		return override != null ? ReikaJavaLibrary.makeListFrom(override) : super.getDrops(world, x, y, z, meta, fortune);
	}

	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
		return TerrainType.list[world.getBlockMetadata(x, y, z)].hardness;
	}

	@Override
	public float getExplosionResistance(Entity e, World world, int x, int y, int z, double ex, double ey, double ez) {
		return TerrainType.list[world.getBlockMetadata(x, y, z)].resistance;
	}
	/*
	@Override
	@SuppressWarnings("incomplete-switch")
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		switch(TerrainType.list[world.getBlockMetadata(x, y, z)]) {
			case CRACKS:
				float x0 = 0;
				float y0 = 0;
				float z0 = 0;
				float x1 = 1;
				float y1 = 1;
				float z1 = 1;
				double d = 0.125;//0.0625;//0.125;
				for (int i = 0; i < 6; i++) {
					ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i];
					int dx = x+dir.offsetX;
					int dy = y+dir.offsetY;
					int dz = z+dir.offsetZ;
					if (!world.getBlock(dx, dy, dz).isSideSolid(world, dx, dy, dz, dir.getOpposite())) {
						switch(dir) {
							case DOWN:
								y0 += d;
								break;
							case UP:
								y1 -= d;
								break;
							case WEST:
								x0 += d;
								break;
							case EAST:
								x1 -= d;
								break;
							case NORTH:
								z0 += d;
								break;
							case SOUTH:
								z1 -= d;
								break;
						}
					}
				}
				this.setBlockBounds(x0, y0, z0, x1, y1, z1);
				break;
			default:
				this.setBlockBounds(0, 0, 0, 1, 1, 1);
		}
	}*/

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		if (world.getBlockMetadata(x, y, z) == TerrainType.CRACKS.ordinal() && world.getBlock(x, y-1, z).isAir(world, x, y-1, z)) {
			int n = ReikaRandomHelper.getRandomBetween(1, 4);
			for (int i = 0; i < n; i++) {
				double vx = ReikaRandomHelper.getRandomPlusMinus(0D, 0.03125);
				double vz = ReikaRandomHelper.getRandomPlusMinus(0D, 0.03125);
				ReikaModelledBreakFX fx = new ReikaModelledBreakFX(world, x+rand.nextDouble(), y, z+rand.nextDouble(), vx, 0, vz, SFBlocks.CAVESHIELD.getBlockInstance(), 0, 0);
				fx.particleScale = 0.4F;
				fx.noClip = true;
				Minecraft.getMinecraft().effectRenderer.addEffect(fx);
			}
		}
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
			if ((b == SFBlocks.CAVESHIELD.getBlockInstance() && world.getBlockMetadata(dx, dy, dz) == 0) || b == SFBlocks.SPAWNER.getBlockInstance()) {
				if (world.isRemote)
					ReikaRenderHelper.spawnDropParticles(world, dx, dy, dz, b, 0);
				else
					world.setBlock(dx, dy, dz, this, TerrainType.CRACKS.ordinal(), 3);
				ReikaSoundHelper.playBreakSound(world, dx, dy, dz, Blocks.stone);
			}
		}
	}

	public static enum TerrainType {
		POISONROCK(3, 45),
		PONDROCK(1, 30),
		OUTCROP(2, 30),
		CRACKS(Blocks.stone.blockHardness*1.5F, 6000),
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

		public ItemStack getDrop() {
			switch(this) {
				case CRACKS:
					return new ItemStack(Blocks.cobblestone);
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
				case CRACKS:
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
				case CRACKS:
					return true;
			}
			return false;
		}
	}

}
