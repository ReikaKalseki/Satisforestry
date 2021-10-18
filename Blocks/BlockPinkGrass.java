package Reika.Satisforestry.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.Instantiable.Math.Noise.Simplex3DGenerator;
import Reika.DragonAPI.Instantiable.Rendering.TessellatorVertexList;
import Reika.DragonAPI.Libraries.ReikaInventoryHelper;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.Libraries.Rendering.ReikaColorAPI;
import Reika.DragonAPI.Libraries.Rendering.ReikaRenderHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.Generator.PinkTreeGeneratorBase.PinkTreeTypes;
import Reika.Satisforestry.Registry.SFBlocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class BlockPinkGrass extends BlockTallGrass {

	public static enum GrassTypes {
		PEACH_FRINGE(),
		TINY_PINK_LUMPS(),
		RED_STRANDS(2),
		BLUE_MUSHROOM_STALK("bluemushroom", 1),
		BLUE_MUSHROOM_TOP("bluemushroom", 3),
		VINE(3),
		STALKS(2),
		FERN(),
		PALEBERRY_NEW("paleberry", 1),
		PALEBERRY_EMPTY("paleberry", 1),
		PALEBERRY_STALK("paleberry", 1),
		TREE_VINE(3),
		;

		public final String nameKey;
		private IIcon[] icons;

		private static final Simplex3DGenerator renderNoise = new Simplex3DGenerator(DragonAPICore.getLaunchTime());
		private static final Random renderRand = new Random();

		public static final GrassTypes[] list = values();

		private GrassTypes() {
			this(null, 1);
		}

		private GrassTypes(int n) {
			this(null, n);
		}

		private GrassTypes(String s, int n) {
			nameKey = s;
			icons = new IIcon[n];
		}

		public IIcon getIcon(IBlockAccess iba, int x, int y, int z) {
			switch(this) {
				case STALKS:
					return this.matchAt(iba, x, y+1, z) ? icons[0] : icons[1+renderRand.nextInt(icons.length-1)];
				default:
					return icons[renderRand.nextInt(icons.length)];
			}
		}

		public float getHeight(IBlockAccess world, int x, int y, int z) {
			switch(this) {
				case RED_STRANDS:
					return 0.375F;
				case TINY_PINK_LUMPS:
					return 0.125F;
				case BLUE_MUSHROOM_TOP:
					return 0.75F;
				case STALKS:
					return this.matchAt(world, x, y+1, z) ? 1 : (float)ReikaRandomHelper.getRandomBetween(1.25, 2, renderRand);
				case FERN:
					return 0.5F;
				default:
					return 1;
			}
		}

		public int getColor(int base) {
			switch(this) {
				case PEACH_FRINGE:
				case RED_STRANDS:
				case BLUE_MUSHROOM_STALK:
				case BLUE_MUSHROOM_TOP:
				case STALKS:
				case PALEBERRY_EMPTY:
				case PALEBERRY_NEW:
				case PALEBERRY_STALK:
					return 0xffffff;
				case VINE:
					return ReikaColorAPI.mixColors(0xa0a0a0, base, 0.25F);
				case TREE_VINE:
					return ReikaColorAPI.getColorWithBrightnessMultiplier(ReikaColorAPI.getModifiedSat(base, 0.5F), 0.67F);
				default:
					return base;
			}
		}

		public ForgeDirection getDirection() {
			switch(this) {
				case VINE:
				case TREE_VINE:
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
				case TREE_VINE:
					return (at == SFBlocks.LEAVES.getBlockInstance() && PinkTreeTypes.getLeafType(world, dx, dy, dz) == PinkTreeTypes.JUNGLE) || this.matchAt(world, dx, dy, dz);
				case VINE:
				case BLUE_MUSHROOM_STALK:
				case STALKS:
					return at.isSideSolid(world, dx, dy, dz, side) || this.matchAt(world, dx, dy, dz);
				case BLUE_MUSHROOM_TOP:
					return BLUE_MUSHROOM_STALK.canExistAt(world, x, y, z);
				case PALEBERRY_NEW:
				case PALEBERRY_EMPTY:
					return at == b && world.getBlockMetadata(dx, dy, dz) == PALEBERRY_STALK.ordinal();
				default:
					return at.canSustainPlant(world, dx, dy, dz, side, (IPlantable)b);
			}
		}

		public int getLight() {
			switch(this) {
				case BLUE_MUSHROOM_TOP:
					return 7;
				default:
					return 0;
			}
		}

		@SideOnly(Side.CLIENT)
		public void render(IBlockAccess world, int x, int y, int z, Block b, RenderBlocks rb, Tessellator v5) {
			this.prepareRandom(world, x, y, z);
			IIcon ico = this.getIcon(world, x, y, z);
			v5.setColorOpaque_I(b.colorMultiplier(world, x, y, z));
			v5.setBrightness(b.getMixedBrightnessForBlock(world, x, y, z));
			float h = this.getHeight(world, x, y, z);
			switch(this) {
				case STALKS:
					ReikaRenderHelper.renderCropTypeTex(world, x, y, z, ico, v5, rb, 0.1875, h);
					break;
				case FERN:
					v5.setColorOpaque_I(ReikaColorAPI.mixColors(b.colorMultiplier(world, x, y, z), 0xa00000, 0.75F));
					int n = ReikaRandomHelper.getRandomBetween(3, 7, renderRand);
					double da = 360D/n;
					for (int i = 0; i < n; i++) {
						double ang = Math.toRadians(ReikaRandomHelper.getRandomPlusMinus(i*da, da/2.5, renderRand));
						double r = ReikaRandomHelper.getRandomBetween(0.25, 0.5, renderRand);
						double dr = ReikaRandomHelper.getRandomBetween(0, 0.1875, renderRand);
						double px = r*Math.cos(ang);
						double pz = r*Math.sin(ang);
						double px2 = r*Math.cos(ang+Math.PI/2);
						double pz2 = r*Math.sin(ang+Math.PI/2);
						float u = ico.getMinU();
						float v = ico.getMinV();
						float du = ico.getMaxU();
						float dv = ico.getMaxV();
						double h2 = ReikaRandomHelper.getRandomPlusMinus(h, 0.125, renderRand);

						v5.addVertexWithUV(x+0.5-px+px2*(1+dr), y+h2, z+0.5-pz+pz2*(1+dr), u, v);
						v5.addVertexWithUV(x+0.5+px+px2*(1+dr), y+h2, z+0.5+pz+pz2*(1+dr), du, v);
						v5.addVertexWithUV(x+0.5+px+px2*dr, y, z+0.5+pz+pz2*dr, du, dv);
						v5.addVertexWithUV(x+0.5-px+px2*dr, y, z+0.5-pz+pz2*dr, u, dv);

						v5.addVertexWithUV(x+0.5-px+px2*dr, y, z+0.5-pz+pz2*dr, u, dv);
						v5.addVertexWithUV(x+0.5+px+px2*dr, y, z+0.5+pz+pz2*dr, du, dv);
						v5.addVertexWithUV(x+0.5+px+px2*(1+dr), y+h2, z+0.5+pz+pz2*(1+dr), du, v);
						v5.addVertexWithUV(x+0.5-px+px2*(1+dr), y+h2, z+0.5-pz+pz2*(1+dr), u, v);
					}
					break;
				case PALEBERRY_NEW:
					PALEBERRY_EMPTY.render(world, x, y, z, b, rb, v5);
					World w = Minecraft.getMinecraft().theWorld;
					float rawSun = 4+11*ReikaWorldHelper.getSunIntensity(w, false, 0);
					float light = rawSun*w.getSavedLightValue(EnumSkyBlock.Sky, x, y, z)/15F;
					if (light >= 4)
						v5.setBrightness(240);
					ReikaRenderHelper.renderCrossTex(world, x, y, z, berryIcon, v5, rb, 1);
					break;
				case TREE_VINE:
					renderNoise.setFrequency(1/2D);
					ReikaRenderHelper.renderCropTypeTex(world, x, y, z, ico, v5, rb, ReikaMathLibrary.normalizeToBounds(renderNoise.getValue(x, y, z), 0.03125, 0.375), 1);
					break;
				case BLUE_MUSHROOM_STALK:
				case BLUE_MUSHROOM_TOP:
					TessellatorVertexList tv5 = new TessellatorVertexList();
					float u = ico.getMinU();
					float du = ico.getMaxU();
					float v = ico.getMinV();
					float dv = ico.getMaxV();

					tv5.addVertexWithUV(x, y+1, 	z, 	u, v);
					tv5.addVertexWithUV(x+1, y+1, 	z+1, 	du, v);
					tv5.addVertexWithUV(x+1, y, 	z+1, 	du, dv);
					tv5.addVertexWithUV(x, y, 	z, 	u, dv);

					tv5.addVertexWithUV(x, y, 	z, 	u, dv);
					tv5.addVertexWithUV(x+1, y, 	z+1, 	du, dv);
					tv5.addVertexWithUV(x+1, y+1, 	z+1, 	du, v);
					tv5.addVertexWithUV(x, y+1, 	z, 	u, v);

					tv5.addVertexWithUV(x, y+1, 	z+1, 	u, v);
					tv5.addVertexWithUV(x+1, y+1, 	z, 	du, v);
					tv5.addVertexWithUV(x+1, y, 	z, 	du, dv);
					tv5.addVertexWithUV(x, y, 	z+1, 	u, dv);

					tv5.addVertexWithUV(x, y, 	z+1, 	u, dv);
					tv5.addVertexWithUV(x+1, y, 	z, 	du, dv);
					tv5.addVertexWithUV(x+1, y+1, 	z, 	du, v);
					tv5.addVertexWithUV(x, y+1, 	z+1, 	u, v);

					double maxWiggle = 15;//10;

					this.prepareRandom(world, x, y, z);
					double ax = ReikaRandomHelper.getRandomPlusMinus(0D, maxWiggle, renderRand);
					double az = ReikaRandomHelper.getRandomPlusMinus(0D, maxWiggle, renderRand);
					//ReikaJavaLibrary.pConsole(x+", "+y+", "+z+" > "+ax+", "+az+" @ "+y+" 0");
					tv5.rotateNonOrthogonal(ax, 0, az, x+0.5, y, z+0.5);

					for (int i = y-1; i >= 0; i--) {
						Block below = world.getBlock(x, i, z);
						int mb = world.getBlockMetadata(x, i, z);
						if (below == SFBlocks.GRASS.getBlockInstance() && (mb == BLUE_MUSHROOM_STALK.ordinal() || mb == BLUE_MUSHROOM_TOP.ordinal())) {
							this.prepareRandom(world, x, i, z);
							ax = ReikaRandomHelper.getRandomPlusMinus(0D, maxWiggle, renderRand);
							az = ReikaRandomHelper.getRandomPlusMinus(0D, maxWiggle, renderRand);
							//ReikaJavaLibrary.pConsole(x+", "+i+", "+z+" > "+ax+", "+az+" @ "+y+" "+(y-i));
							double sz = Math.sin(Math.toRadians(ax));
							double sx = Math.sin(Math.toRadians(az));
							double cx = Math.cos(Math.toRadians(ax));
							double cz = Math.cos(Math.toRadians(az));

							tv5.offset(sx, -1/28D*Math.sqrt(cx*cx+cz*cz), -sz);
						}
						else {
							break;
						}
					}
					//ReikaJavaLibrary.pConsole(dz);

					tv5.render();
					break;
				default:
					ReikaRenderHelper.renderCrossTex(world, x, y, z, ico, v5, rb, 1); //not h, since icon is already part of a block
					break;
			}
		}

		private void prepareRandom(IBlockAccess world, int x, int y, int z) {
			renderRand.setSeed(ChunkCoordIntPair.chunkXZ2Int(x, z) ^ y);
			renderRand.nextBoolean();
			renderRand.nextBoolean();
		}

		public void updateTick(World world, int x, int y, int z, Random rand) {
			switch(this) {
				case PALEBERRY_EMPTY:
					if (!Satisforestry.isPinkForest(world, x, z))
						return;
					for (int i = 2; i < 6; i++) {
						ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i];
						if (this.matchAt(world, x+dir.offsetX, y, z+dir.offsetZ))
							return;
					}
					world.setBlockMetadataWithNotify(x, y, z, PALEBERRY_NEW.ordinal(), 3);
					ReikaSoundHelper.playBreakSound(world, x, y, z, Blocks.leaves, 0.7F, 0.25F);
					break;
				default:
					break;
			}
		}

		private boolean matchAt(IBlockAccess world, int x, int y, int z) {
			return world.getBlock(x, y, z) == SFBlocks.GRASS.getBlockInstance() && world.getBlockMetadata(x, y, z) == this.ordinal();
		}

		public boolean onClicked(World world, int x, int y, int z, EntityPlayer ep) {
			switch(this) {
				case PALEBERRY_NEW:
					ItemStack is = new ItemStack(Satisforestry.paleberry);
					if (!ReikaInventoryHelper.addToIInv(is, ep.inventory)) {
						ReikaItemHelper.dropItem(ep, is);
					}
					ReikaSoundHelper.playBreakSound(world, x, y, z, Blocks.leaves, 0.7F, 0.25F);
					world.setBlockMetadataWithNotify(x, y, z, PALEBERRY_EMPTY.ordinal(), 3);
					return true;
				default:
					return false;
			}
		}

		public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int fortune) {
			switch(this) {
				case BLUE_MUSHROOM_STALK:
				case BLUE_MUSHROOM_TOP:
					return ReikaJavaLibrary.makeListFrom(new ItemStack(Item.getItemFromBlock(Blocks.brown_mushroom)));
				case STALKS:
				case PALEBERRY_EMPTY:
				case PALEBERRY_STALK:
					return new ArrayList();
				case TREE_VINE:
					ArrayList<ItemStack> ret = new ArrayList();
					ItemStack add = null;
					if (world.rand.nextInt(Math.max(1, 10-fortune)) == 0) {
						add = world.rand.nextInt(6) == 0 ? ForgeHooks.getGrassSeed(world) : new ItemStack(Items.wheat_seeds);
					}
					if (add != null)
						ret.add(add);
					return ret;
				default:
					return null;
			}
		}

		public boolean isShearable() {
			switch(this) {
				case PALEBERRY_NEW:
				case PALEBERRY_EMPTY:
				case PALEBERRY_STALK:
					//case BLUE_MUSHROOM_STALK:
					//case BLUE_MUSHROOM_TOP:
					return false;
				default:
					return true;
			}
		}
	}

	private static IIcon berryIcon;

	public BlockPinkGrass() {
		super();
		this.setTickRandomly(true);
		this.setCreativeTab(Satisforestry.tabCreative);
		this.setStepSound(soundTypeGrass);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		super.updateTick(world, x, y, z, rand);
		GrassTypes gr = GrassTypes.list[world.getBlockMetadata(x, y, z)];
		gr.updateTick(world, x, y, z, rand);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int s, float a, float b, float c) {
		GrassTypes gr = GrassTypes.list[world.getBlockMetadata(x, y, z)];
		return gr.onClicked(world, x, y, z, ep);
	}

	@Override
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		GrassTypes gr = GrassTypes.list[world.getBlockMetadata(x, y, z)];
		return gr.getColor(super.colorMultiplier(world, x, y, z));
	}

	@Override
	public void registerBlockIcons(IIconRegister ico) {
		for (int i = 0; i < GrassTypes.list.length; i++) {
			GrassTypes gr = GrassTypes.list[i];
			for (int k = 0; k < gr.icons.length; k++) {
				String s = "Satisforestry:foliage/"+gr.name().toLowerCase(Locale.ENGLISH);
				if (gr.icons.length > 1)
					s = s+"_"+k;
				gr.icons[k] = ico.registerIcon(s);
			}
		}

		berryIcon = ico.registerIcon("Satisforestry:foliage/paleberry_berry");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int s, int meta) {
		return GrassTypes.list[meta].icons[0];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess iba, int x, int y, int z, int s) {
		return GrassTypes.list[iba.getBlockMetadata(x, y, z)].getIcon(iba, x, y, z);
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
		gr.prepareRandom(world, x, y, z);
		float f = 0.4F; //from parent
		this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, gr.getHeight(world, x, y, z), 0.5F + f);
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		GrassTypes gr = GrassTypes.list[world.getBlockMetadata(x, y, z)];
		return gr.canExistAt(world, x, y, z);
	}

	@Override
	protected void checkAndDropBlock(World world, int x, int y, int z) { //identical except it propagates block updates
		if (!this.canBlockStay(world, x, y, z)) {
			this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
			world.setBlock(x, y, z, Blocks.air, 0, 3);
		}
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		return GrassTypes.list[world.getBlockMetadata(x, y, z)].getLight();
	}

	@Override
	public int getRenderType() {
		return Satisforestry.proxy.grassRender;
	}

	@Override
	public boolean canReplace(World world, int x, int y, int z, int side, ItemStack is) {
		return world.getBlock(x, y, z).isReplaceable(world, x, y, z) && GrassTypes.list[is.getItemDamage()].canExistAt(world, x, y, z);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
		ArrayList<ItemStack> ret = GrassTypes.list[meta].getDrops(world, x, y, z, fortune);
		if (ret == null)
			ret = super.getDrops(world, x, y, z, meta, fortune);
		return ret;
	}

	@Override
	public boolean isShearable(ItemStack item, IBlockAccess world, int x, int y, int z) {
		return GrassTypes.list[world.getBlockMetadata(x, y, z)].isShearable();
	}

}
