package Reika.Satisforestry.Blocks;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Instantiable.Math.Noise.SimplexNoiseGenerator;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.Satisforestry.SFClient;
import Reika.Satisforestry.Satisforestry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockRedBamboo extends Block implements IPlantable, IShearable {

	private IIcon stemIcon;
	private IIcon topIcon;

	private static final IIcon[] leaves = new IIcon[8];

	private final SimplexNoiseGenerator growthLimit;

	public BlockRedBamboo() {
		super(Material.leaves);
		this.setTickRandomly(true);
		this.setCreativeTab(Satisforestry.tabCreative);
		this.setStepSound(soundTypeGrass);

		growthLimit = (SimplexNoiseGenerator)new SimplexNoiseGenerator(this.getClass().getName().hashCode()).setFrequency(1/2D);
	}

	public static IIcon getRandomLeaf(Random rand) {
		return leaves[rand.nextInt(leaves.length)];
	}

	@Override
	public IIcon getIcon(int s, int meta) {
		return s <= 1 ? topIcon : stemIcon;
	}
	/*
	@Override
	public IIcon getIcon(IBlockAccess iba, int x, int y, int z, int s) {
		return s <= 1 ? topIcon : stemIcon;
	}
	 */
	@Override
	public void registerBlockIcons(IIconRegister ico) {
		stemIcon = ico.registerIcon("Satisforestry:bamboo/side");
		topIcon = ico.registerIcon("Satisforestry:bamboo/top");

		for (int i = 0; i < leaves.length; i++) {
			leaves[i] = ico.registerIcon("Satisforestry:bamboo/leaf_"+i);
		}
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		/*
		if (world.getBlock(x, y-1, z) == Blocks.reeds || this.checkStability(world, x, y, z)) {
			if (world.isAirBlock(x, y+1, z)) {
				int l;

				for (l = 1; world.getBlock(x, y-l, z) == this; ++l) {
					;
				}

				if (l < 3) {
					int i1 = world.getBlockMetadata(x, y, z);

					if (i1 == 15) {
						world.setBlock(x, y+1, z, this);
						world.setBlockMetadataWithNotify(x, y, z, 0, 4);
					}
					else {
						world.setBlockMetadataWithNotify(x, y, z, i1+1, 4);
					}
				}
			}
		}*/
		if (this.checkStability(world, x, y, z)) {
			if (this.tryGrow(world, x, y, z, rand)) {

			}
		}
	}

	private boolean tryGrow(World world, int x, int y, int z, Random rand) {
		int h = 0;
		if (world.getBlock(x, y-1, z) == this)
			return false;
		while (world.getBlock(x, y+h, z) == this) {
			h++;
		}
		int meta = world.getBlockMetadata(x, y+h-1, z);
		if (meta < 8) {
			if (meta == 0) {
				int limit = (int)ReikaMathLibrary.normalizeToBounds(growthLimit.getValue(x, z), 3, 8);
				if (h < limit) {
					world.setBlock(x, y+h, z, this, 7, 3);
					ReikaSoundHelper.playPlaceSound(world, x, y+h, z, Blocks.leaves, 0.85F, 0.6F);
					return true;
				}
				else {
					return false;
				}
			}
			else {
				world.setBlockMetadataWithNotify(x, y+h-1, z, meta-1, 3);
				return true;
			}
		}
		else {
			return false;
		}
	}

	@Override
	public int damageDropped(int meta) {
		return 0;
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		Block b = world.getBlock(x, y-1, z);
		return b == this || b == Blocks.sand || b.canSustainPlant(world, x, y-1, z, ForgeDirection.UP, this);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block b) {
		this.checkStability(world, x, y, z);
	}

	protected final boolean checkStability(World world, int x, int y, int z) {
		if (!this.canBlockStay(world, x, y, z)) {
			this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
			world.setBlockToAir(x, y, z);
			return false;
		}
		else {
			return true;
		}
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		return this.canPlaceBlockAt(world, x, y, z);
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
		return Satisforestry.proxy.bambooRender;//0;//1;
	}

	@Override
	public final int getRenderBlockPass() {
		return 1;
	}

	@Override
	public boolean canRenderInPass(int pass) {
		SFClient.bamboo.setRenderPass(pass);
		return pass <= 1;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		float s = 0.375F;//0.125F;
		this.setBlockBounds(0.5F-s, 0, 0.5F-s, 0.5F+s, 1, 0.5F+s);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}

	@Override
	public Item getItemDropped(int dmg, Random rand, int fortune) {
		return Items.reeds;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		return 0xffffff;//world.getBiomeGenForCoords(x, z).getBiomeGrassColor(x, y, z);
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
		return EnumPlantType.Plains;
	}

	@Override
	public Block getPlant(IBlockAccess world, int x, int y, int z) {
		return this;
	}

	@Override
	public int getPlantMetadata(IBlockAccess world, int x, int y, int z) {
		return world.getBlockMetadata(x, y, z);
	}

	@Override
	public boolean isLeaves(IBlockAccess world, int x, int y, int z) {
		return false;
	}

	@Override
	public boolean isShearable(ItemStack item, IBlockAccess world, int x, int y, int z) {
		return true;
	}
	/*
	@Override
	public ArrayList<ItemStack> onSheared(ItemStack item, IBlockAccess world, int x, int y, int z, int fortune) {
		return ReikaJavaLibrary.makeListFrom(new ItemStack(this));
	}*/

	@Override
	public ArrayList<ItemStack> onSheared(ItemStack item, IBlockAccess world, int x, int y, int z, int fortune) {
		int meta = world.getBlockMetadata(x, y, z);
		ItemStack is = new ItemStack(this);
		if (world instanceof World) {
			int dy = y+1;
			while (world.getBlock(x, dy, z) == this && world.getBlockMetadata(x, dy, z) == meta) {
				dy++;
			}
			for (int i = dy-1; i > y; i--) {
				((World)world).setBlock(x, i, z, Blocks.air);
				ReikaItemHelper.dropItem((World)world, x+0.5, i+0.5, z+0.5, is);
			}
		}
		return ReikaJavaLibrary.makeListFrom(is.copy());
	}

	@Override
	public void harvestBlock(World world, EntityPlayer ep, int x, int y, int z, int meta) {
		ItemStack is = ep.getCurrentEquippedItem();
		if (is != null && is.getItem() instanceof ItemShears) {
			return;
		}
		super.harvestBlock(world, ep, x, y, z, meta);
	}
}