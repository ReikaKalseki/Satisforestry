package Reika.Satisforestry.Blocks;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Libraries.IO.ReikaRenderHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.Satisforestry.SFBlocks;
import Reika.Satisforestry.Satisforestry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockDecoration extends Block {

	public BlockDecoration(Material mat) {
		super(mat);
		this.setCreativeTab(Satisforestry.tabCreative);
		this.setLightOpacity(0);
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

	}

	@Override
	public IIcon getIcon(int s, int meta) {
		return SFBlocks.CAVESHIELD.getBlockInstance().blockIcon;
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
		t.prepareRandom(world, x, y, z);
		float w = t.width(x, y, z)/2F;
		this.setBlockBounds(0.5F-w, 0, 0.5F-w, 0.5F+w, t.height(x, y, z), 0.5F+w);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, List li, Entity e) {
		DecorationType t = DecorationType.list[world.getBlockMetadata(x, y, z)];
		if (t == DecorationType.TENDRILS && !(e instanceof EntitySpider))
			return;
		super.addCollisionBoxesToList(world, x, y, z, mask, li, e);
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public int getRenderType() {
		return Satisforestry.proxy.decoRender;
	}

	public static enum DecorationType {
		STALAGMITE("Stone Spikes", 1.5F, 15),
		STALACTITE("Stone Spikes", 1.5F, 15),
		TENDRILS("Stony Tendrils", 2.5F, 60F),
		;

		public final String name;
		public final float hardness;
		public final float resistance;

		private static final Random renderRand = new Random();

		public static final DecorationType[] list = values();

		private DecorationType(String s, float h, float r) {
			hardness = h;
			resistance = r;
			name = s;
		}

		public float width(int x, int y, int z) {
			switch(this) {
				case STALAGMITE:
				case STALACTITE:
					return (float)ReikaRandomHelper.getRandomBetween(5D, 8D, renderRand)/16F;
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

		@SideOnly(Side.CLIENT)
		public void render(IBlockAccess world, int x, int y, int z, Block b, RenderBlocks rb, Tessellator v5) {
			this.prepareRandom(world, x, y, z);
			v5.setColorOpaque_I(b.colorMultiplier(world, x, y, z));
			v5.setBrightness(b.getMixedBrightnessForBlock(world, x, y, z));
			float h = this.height(x, y, z);
			switch(this) {
				case STALAGMITE:
				case STALACTITE:
					int y0 = y;
					double w = ReikaRandomHelper.getRandomBetween(5D, 8D, renderRand);
					h = (float)ReikaRandomHelper.getRandomBetween(10D, 16D, renderRand);
					double dy = this == STALACTITE ? 16-h : 0;
					while (w >= 1 && (world.getBlock(x, y0, z).isAir(world, x, y0, z) || world.getBlock(x, y0, z) == b)) {
						ReikaRenderHelper.renderBlockSubCube(x, y, z, 8-w, dy, 8-w, w*2, h, w*2, v5, rb, SFBlocks.CAVESHIELD.getBlockInstance(), 0);
						if (this == STALAGMITE)
							dy += h;
						w -= ReikaRandomHelper.getRandomBetween(1D, 3D, renderRand);
						h *= ReikaRandomHelper.getRandomBetween(0.5, 1, renderRand);
						if (this == STALACTITE) {
							dy -= h;
							y0 = (int)(y-dy/16D);
						}
						else {
							y0 = (int)(y+dy/16D);
						}
					}
					break;
				case TENDRILS:
					IIcon ico = SFBlocks.CAVESHIELD.getBlockInstance().getIcon(0, 0);
					float u = ico.getMinU();
					float v = ico.getMinV();
					float du = ico.getMaxU();
					float dv = ico.getMaxV();

					int n = ReikaRandomHelper.getRandomBetween(4, 10, renderRand);
					for (int i = 0; i < n; i++) {
						ForgeDirection side1 = ForgeDirection.VALID_DIRECTIONS[2+renderRand.nextInt(4)];
						ForgeDirection side2 = ForgeDirection.VALID_DIRECTIONS[2+renderRand.nextInt(4)];

						double da = renderRand.nextDouble();
						double db = renderRand.nextDouble();

						double xa = da;
						double za = da;
						double xb = db;
						double zb = db;

						double h0 = ReikaRandomHelper.getRandomBetween(0.75, 0.9375, renderRand);
						double h1 = ReikaRandomHelper.getRandomBetween(0.75, 0.9375, renderRand);

						switch(side1) {
							case EAST:
								xa = 1;
								break;
							case WEST:
								xa = 0;
								break;
							case NORTH:
								za = 0;
								break;
							case SOUTH:
								za = 1;
								break;
							default:
								break;
						}
						switch(side2) {
							case EAST:
								xb = 1;
								break;
							case WEST:
								xb = 0;
								break;
							case NORTH:
								zb = 0;
								break;
							case SOUTH:
								zb = 1;
								break;
							default:
								break;
						}

						double u1 = ico.getInterpolatedU(16*x1);
						double u2 = ico.getInterpolatedU(16*x2);
						double u3 = ico.getInterpolatedU(16*x3);
						double u4 = ico.getInterpolatedU(16*x4);
						double v1 = ico.getInterpolatedV(16*z1);
						double v2 = ico.getInterpolatedV(16*z2);
						double v3 = ico.getInterpolatedV(16*z3);
						double v4 = ico.getInterpolatedV(16*z4);

						v5.addVertexWithUV(x+x4, y+h0, z+z4, u4, v4);
						v5.addVertexWithUV(x+x3, y+h0, z+z3, u3, v3);
						v5.addVertexWithUV(x+x2, y+h0, z+z2, u2, v2);
						v5.addVertexWithUV(x+x1, y+h0, z+z1, u1, v1);

						v5.addVertexWithUV(x+x1, y+h0, z+z1, u1, v1);
						v5.addVertexWithUV(x+x2, y+h0, z+z2, u2, v2);
						v5.addVertexWithUV(x+x3, y+h0, z+z3, u3, v3);
						v5.addVertexWithUV(x+x4, y+h0, z+z4, u4, v4);
					}
					break;
			}
		}

		private void prepareRandom(IBlockAccess world, int x, int y, int z) {
			renderRand.setSeed(ChunkCoordIntPair.chunkXZ2Int(x, z) ^ y);
			renderRand.nextBoolean();
			renderRand.nextBoolean();
		}
	}

}
