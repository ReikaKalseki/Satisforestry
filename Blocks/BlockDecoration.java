package Reika.Satisforestry.Blocks;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
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

import Reika.DragonAPI.Extras.IconPrefabs;
import Reika.DragonAPI.Instantiable.Effects.EntityBlurFX;
import Reika.DragonAPI.Instantiable.Math.Noise.Simplex3DGenerator;
import Reika.DragonAPI.Instantiable.Math.Noise.SimplexNoiseGenerator;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.Rendering.ReikaRenderHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Registry.SFBlocks;

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
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		DecorationType t = DecorationType.list[world.getBlockMetadata(x, y, z)];
		t.randomDisplayTick(world, x, y, z, rand);
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
		private static final SimplexNoiseGenerator renderNoise2D = new SimplexNoiseGenerator(0);
		private static final Simplex3DGenerator renderNoise3D = new Simplex3DGenerator(0);

		public static final DecorationType[] list = values();

		private DecorationType(String s, float h, float r) {
			hardness = h;
			resistance = r;
			name = s;
		}

		public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
			switch(this) {
				case TENDRILS:
					if (rand.nextInt(4) == 0 && !ReikaWorldHelper.isAirBlock(world, x+1, y, z) && !ReikaWorldHelper.isAirBlock(world, x-1, y, z) && !ReikaWorldHelper.isAirBlock(world, x, y, z+1) && !ReikaWorldHelper.isAirBlock(world, x, y, z-1)) {
						double px = ReikaRandomHelper.getRandomBetween(x, x+1D);
						double py = ReikaRandomHelper.getRandomBetween(y, y+0.5D);
						double pz = ReikaRandomHelper.getRandomBetween(z, z+1D);
						int c = 0xBAFF21;//0xC9FF21;
						EntityBlurFX fx = new EntityBlurFX(world, px, py, pz, IconPrefabs.FADE.getIcon());
						float s = (float)ReikaRandomHelper.getRandomBetween(3.5, 7.5);
						float g = -(float)ReikaRandomHelper.getRandomBetween(0.01, 0.03);
						fx.setRapidExpand().setAlphaFading().forceIgnoreLimits();
						fx.setColor(c).setScale(s).setGravity(g);
						Minecraft.getMinecraft().effectRenderer.addEffect(fx);
					}
					break;
				default:
					break;
			}
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
		public boolean render(IBlockAccess world, int x, int y, int z, Block b, RenderBlocks rb, Tessellator v5) {
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
					return true;
				case TENDRILS:
					boolean flag = false;
					int div = 4;
					double f = 1;//1.5;//3.2;
					for (int i = 0; i < div; i++) {
						for (int j = 0; j < div; j++) {
							for (int k = 0; k < div; k++) {
								double x1 = x+i/(double)div;
								double y1 = y+j/(double)div;
								double z1 = z+k/(double)div;
								double noise = renderNoise3D.getValue(x1*f, y1*f, z1*f);
								if (Math.abs(noise) < 0.125) {
									ReikaRenderHelper.renderBlockSubCube(x, y, z, (x1-x)*16, (y1-y)*16, (z1-z)*16, 16D/div, v5, rb, SFBlocks.CAVESHIELD.getBlockInstance(), 0);
									flag = true;
								}
							}
						}
					}

					return flag;
			}
			return false;
		}

		private void prepareRandom(IBlockAccess world, int x, int y, int z) {
			renderRand.setSeed(ChunkCoordIntPair.chunkXZ2Int(x, z) ^ y);
			renderRand.nextBoolean();
			renderRand.nextBoolean();
		}
	}

}
