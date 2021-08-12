package Reika.Satisforestry.Blocks;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import Reika.DragonAPI.Extras.IconPrefabs;
import Reika.DragonAPI.Instantiable.Effects.EntityBlurFX;
import Reika.DragonAPI.Interfaces.Block.Submergeable;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockCaveSpawner.TileCaveSpawner;
import Reika.Satisforestry.Entity.EntityEliteStinger;
import Reika.Satisforestry.Render.EntitySlugStreak;

public class BlockPowerSlug extends Block implements Submergeable {

	public BlockPowerSlug(Material mat) {
		super(mat);
		this.setResistance(6000);
		this.setCreativeTab(Satisforestry.tabCreative);
	}

	@Override
	public boolean hasTileEntity(int meta) {
		return meta <= 2;
	}

	@Override
	public TileEntity createTileEntity(World world, int meta) {
		return meta <= 2 ? new TilePowerSlug(meta) : null;
	}

	@Override
	public final void getSubBlocks(Item it, CreativeTabs tab, List li) {
		for (int i = 0; i < 4; i++) {
			li.add(new ItemStack(it, 1, i));
		}
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
	public int colorMultiplier(IBlockAccess iba, int x, int y, int z) {
		switch(iba.getBlockMetadata(x, y, z)) {
			case 0:
				return 0x94FF7F;
			case 1:
				return 0xF2F268;
			case 2:
				return 0xDA7FFF;
			default:
				return 0xF26030;
		}
	}

	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
		switch(world.getBlockMetadata(x, y, z)) {
			case 0:
				return 2F; //1.33x stone
			case 1:
				return 5F; //1.6x of ores, 3.33x stone
			case 2:
				return 20F; //0.4x obsidian
			default:
				return 0;
		}
	}

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		int c = this.colorMultiplier(world, x, y, z);
		if (c == 0xF26030)
			return;
		if (rand.nextInt(2) == 0) {
			double px = ReikaRandomHelper.getRandomPlusMinus(x+0.5, 0.5, rand);
			double pz = ReikaRandomHelper.getRandomPlusMinus(z+0.5, 0.5, rand);
			double py = ReikaRandomHelper.getRandomBetween(y, y+0.5, rand);
			float s = (float)ReikaRandomHelper.getRandomBetween(3.5, 7.5, rand);
			int l = ReikaRandomHelper.getRandomBetween(30, 80);
			EntityBlurFX fx = new EntityBlurFX(world, px, py, pz, IconPrefabs.FADE_GENTLE.getIcon()).setColor(c).setScale(s).setLife(l).setAlphaFading();
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}

		double px = ReikaRandomHelper.getRandomPlusMinus(x+0.5, 1.5, rand);
		double pz = ReikaRandomHelper.getRandomPlusMinus(z+0.5, 1.5, rand);
		double py = ReikaRandomHelper.getRandomBetween(y+0.5, y+2, rand);
		double v = -0.04;
		double vx = (px-(x+0.5))*v;
		double vy = (py-(y+0.25))*v;
		double vz = (pz-(z+0.5))*v;
		EntitySlugStreak fx = new EntitySlugStreak(world, px, py, pz, vx, vy, vz, IconPrefabs.FADE.getIcon());
		fx.setColor(c).setScale(0.7F).setLife(20);
		Minecraft.getMinecraft().effectRenderer.addEffect(fx);
	}

	@Override
	public int getRenderType() {
		return Satisforestry.proxy.slugRender;
	}

	@Override
	public int damageDropped(int meta) {
		int base = meta;
		if (base <= 2)
			base += 3;
		return base;
	}

	@Override
	public boolean isSubmergeable(IBlockAccess iba, int x, int y, int z) {
		return true;
	}

	public static class TilePowerSlug extends TileCaveSpawner {

		public TilePowerSlug(int meta) {
			this.setMobType(this.getMobTypeForTier(meta));

			mobLimit = 3;
			respawnTime = 2400; //2 min
			activeRadius = 12;
			spawnRadius = 6;
		}

		private Class<? extends EntityMob> getMobTypeForTier(int meta) {
			switch(meta) {
				case 0:
					return null;
				case 1:
					return EntitySpider.class;
				case 2:
					return EntityEliteStinger.class;
				default:
					throw new IllegalStateException("Invalid slug meta: "+meta);
			}
		}

	}

}
