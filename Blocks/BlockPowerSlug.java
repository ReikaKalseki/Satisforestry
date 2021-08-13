package Reika.Satisforestry.Blocks;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Extras.IconPrefabs;
import Reika.DragonAPI.Instantiable.Effects.EntityBlurFX;
import Reika.DragonAPI.Interfaces.Block.Submergeable;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.ModInteract.ItemHandlers.ThaumItemHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockCaveSpawner.TileCaveSpawner;
import Reika.Satisforestry.Registry.SFBlocks;
import Reika.Satisforestry.Registry.SFSounds;
import Reika.Satisforestry.Render.EntitySlugStreak;

public class BlockPowerSlug extends BlockContainer implements Submergeable {

	private static final float width = 6/16F;
	private static final float length = 14/16F;
	private static final float height = 5/16F;

	public BlockPowerSlug(Material mat) {
		super(mat);
		this.setResistance(6000);
		this.setCreativeTab(Satisforestry.tabCreative);
		float max = Math.max(length, width);
		this.setBlockBounds(0.5F-max/2F, 0, 0.5F-max/2F, 0.5F+max/2F, height, 0.5F+max/2F);
		this.setStepSound(soundTypeSnow);
		if (ModList.THAUMCRAFT.isLoaded()) {
			this.setStepSound(ThaumItemHelper.BlockEntry.TAINT.getBlock().stepSound);
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return meta <= 2 ? new TilePowerSlug(meta) : new TilePowerSlugInert();
	}

	@Override
	public final void getSubBlocks(Item it, CreativeTabs tab, List li) {
		for (int i = 0; i < 6; i++) {
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

	public static int getColor(int tier) {
		switch(tier) {
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
	public int colorMultiplier(IBlockAccess iba, int x, int y, int z) {
		return getColor(iba.getBlockMetadata(x, y, z));
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
	public void setBlockBoundsBasedOnState(IBlockAccess iba, int x, int y, int z) {
		TileEntity te = iba.getTileEntity(x, y, z);
		if (te instanceof TilePowerSlug) {
			TilePowerSlug ts = (TilePowerSlug)te;
			float ra = (float)Math.sin(Math.toRadians(ts.angle%180));
			float rb = (float)Math.sin(Math.abs(Math.toRadians(90-ts.angle%180)));
			float l2 = length*rb+width*ra;
			float l1 = width*rb+length*ra;
			this.setBlockBounds(0.5F-l1/2F, 0, 0.5F-l2/2F, 0.5F+l1/2F, height, 0.5F+l2/2F);
		}
	}

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {

	}

	@Override
	public int getRenderType() {
		return -1;//Satisforestry.proxy.slugRender;
	}

	@Override
	public int damageDropped(int meta) {
		return meta;/*
		int base = meta;
		if (base <= 2)
			base += 3;
		return base;*/
	}

	@Override
	public boolean isSubmergeable(IBlockAccess iba, int x, int y, int z) {
		return true;
	}

	public static TilePowerSlug generatePowerSlugAt(World world, int x, int y, int z, int tier) {
		Block b = world.getBlock(x, y-1, z);
		if (world.getBlock(x, y, z).isAir(world, x, y, z) && (b == Blocks.grass || b == Blocks.dirt || b == Blocks.sand || b == SFBlocks.TERRAIN.getBlockInstance())) {
			world.setBlock(x, y, z, SFBlocks.SLUG.getBlockInstance(), tier, 3);
			TilePowerSlug te = (TilePowerSlug)world.getTileEntity(x, y, z);
			te.angle = world.rand.nextFloat()*360;
			//te.setMobType(getMobTypeForTier(tier));
			te.mobLimit = 3;
			return te;
		}
		return null;
	}
	/*
	private static Class<? extends EntityMob> getMobTypeForTier(int tier) {
		switch(tier) {
			case 0:
				return null;
			case 1:
				return EntitySpider.class;
			case 2:
				return EntityEliteStinger.class;
			default:
				throw new IllegalStateException("Invalid slug meta: "+tier);
		}
	}
	 */
	public static class TilePowerSlugInert extends TilePowerSlug {

		public TilePowerSlugInert() {
			super(0);
			this.setMobType(null);
			mobLimit = 0;
			activeRadius = 0;
		}

		@Override
		public boolean canUpdate() {
			return false;
		}

	}

	public static class TilePowerSlug extends TileCaveSpawner {

		private static final UUID healthBonus = UUID.fromString("cea3577b-784d-46e2-ae4c-3de297a10b66");

		public float angle;

		private int tier;

		private float healthBuff = 0;

		private int soundtick;

		public TilePowerSlug() {
			this(0);
		}

		public TilePowerSlug(int meta) {
			tier = meta;
			respawnTime = 2400; //2 min
			mobLimit = 3;
			activeRadius = 8;
			spawnRadius = 6;
		}

		@Override
		public boolean shouldRenderInPass(int pass) {
			return pass <= 1;
		}

		@Override
		public void updateEntity() {
			super.updateEntity();
			if (!worldObj.isRemote) {
				if (soundtick > 0) {
					soundtick--;
				}
				else {
					float f = 1-0.1F*tier;
					SFSounds.SLUG.playSoundAtBlock(this, 0.7F, f);
					soundtick = (int)(59/f);
				}
			}
			else {
				if (DragonAPICore.rand.nextInt(4) == 0) {
					int x = xCoord;
					int y = yCoord;
					int z = zCoord;
					int c = this.getBlockType().colorMultiplier(worldObj, x, y, z);
					if (c == 0xF26030)
						return;
					if (DragonAPICore.rand.nextInt(2) == 0) {
						double px = ReikaRandomHelper.getRandomPlusMinus(x+0.5, 0.5);
						double pz = ReikaRandomHelper.getRandomPlusMinus(z+0.5, 0.5);
						double py = ReikaRandomHelper.getRandomBetween(y, y+0.5);
						float s = (float)ReikaRandomHelper.getRandomBetween(3.5, 7.5);
						int l = ReikaRandomHelper.getRandomBetween(30, 80);
						EntityBlurFX fx = new EntityBlurFX(worldObj, px, py, pz, IconPrefabs.FADE_GENTLE.getIcon()).setColor(c).setScale(s).setLife(l).setAlphaFading();
						Minecraft.getMinecraft().effectRenderer.addEffect(fx);
					}

					double px = ReikaRandomHelper.getRandomPlusMinus(x+0.5, 1.5);
					double pz = ReikaRandomHelper.getRandomPlusMinus(z+0.5, 1.5);
					double py = ReikaRandomHelper.getRandomBetween(y+0.5, y+2);
					double v = -0.04;
					double vx = (px-(x+0.5))*v;
					double vy = (py-(y+0.25))*v;
					double vz = (pz-(z+0.5))*v;
					EntitySlugStreak fx = new EntitySlugStreak(worldObj, px, py, pz, vx, vy, vz, IconPrefabs.FADE.getIcon());
					fx.setColor(c).setScale(0.7F).setLife(20);
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);
				}
			}
		}

		public final void setSingleStrongEnemy(Class<? extends EntityMob> c, float boost) {
			mobLimit = 1;
			activeRadius = 12;
			spawnRadius = 2;
			this.setMobType(c);
			healthBuff = boost;
		}

		public final void setEnemyBoost(float boost) {
			healthBuff = boost;
		}

		public final void setNoSpawns() {
			mobLimit = 0;
			activeRadius = 0;
			this.setMobType(null);
		}

		@Override
		protected void onSpawnEntity(EntityMob e) {
			super.onSpawnEntity(e);
			if (healthBuff > 0) {
				AttributeModifier m = new AttributeModifier(healthBonus, "slugHealth", healthBuff-1, 2);
				e.getEntityAttribute(SharedMonsterAttributes.maxHealth).applyModifier(m);
			}
			//ReikaEntityHelper.setAlwaysHostile(e);
		}

		public int getTier() {
			return tier;
		}

		@Override
		public void writeToNBT(NBTTagCompound NBT) {
			super.writeToNBT(NBT);

			NBT.setFloat("angle", angle);

			NBT.setInteger("tier", tier);
		}

		@Override
		public void readFromNBT(NBTTagCompound NBT) {
			super.readFromNBT(NBT);

			angle = NBT.getFloat("angle");

			tier = NBT.getInteger("tier");
		}

	}

}
