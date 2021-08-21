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
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.Extras.IconPrefabs;
import Reika.DragonAPI.Instantiable.Effects.EntityBlurFX;
import Reika.DragonAPI.Interfaces.Block.Submergeable;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.World.ReikaBlockHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockCaveSpawner.TileCaveSpawner;
import Reika.Satisforestry.Entity.EntityEliteStinger;
import Reika.Satisforestry.Registry.SFBlocks;
import Reika.Satisforestry.Registry.SFSounds;
import Reika.Satisforestry.Render.EntitySlugStreak;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPowerSlug extends BlockContainer implements PointSpawnBlock, Submergeable {

	private static final float width = 6/16F;
	private static final float length = 14/16F;
	private static final float height = 5/16F;

	public BlockPowerSlug(Material mat) {
		super(mat);
		this.setResistance(6000);
		this.setCreativeTab(Satisforestry.tabCreative);
		float max = Math.max(length, width);
		this.setBlockBounds(0.5F-max/2F, 0, 0.5F-max/2F, 0.5F+max/2F, height, 0.5F+max/2F);
		this.setStepSound(new SoundType("", 0.25F, 0.6F) {
			@Override
			public String getBreakSound() {
				return "mob.slime.big";
			}

			@Override
			public String getStepResourcePath() {
				return "mob.slime.big";
			}

			@Override
			public String func_150496_b() { //place sound
				return "mob.slime.big";
			}
		});
	}
	/*
	public void updateStepSounds() {
		if (ModList.TWILIGHT.isLoaded()) {
			this.setStepSound(TwilightForestHandler.BlockEntry.FIREFLY.getBlock().stepSound);
		}
		else if (ModList.TINKERER.isLoaded()) {
			this.setStepSound(TinkerBlockHandler.getInstance().congealedSlime.stepSound);
		}
		else if (ModList.THAUMCRAFT.isLoaded()) {
			this.setStepSound(ThaumItemHelper.BlockEntry.TAINT.getBlock().stepSound);
		}
	}
	 */
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
				return 1.875F; //1.25x stone
			case 1:
				return 4F; //1.28x of ores, 2.66x stone
			case 2:
				return 10F; //0.2x obsidian
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

	@Override
	public boolean renderLiquid(int meta) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderBlockPass() {
		return 1;
	}

	@Override
	public boolean canRenderInPass(int pass) {
		return true;
	}

	public static TilePowerSlug generatePowerSlugAt(World world, int x, int y, int z, Random rand, int tier, boolean gas, int reachDifficulty, boolean allowSpawns) {
		return generatePowerSlugAt(world, x, y, z, rand, tier, gas, reachDifficulty, allowSpawns, Integer.MAX_VALUE);
	}

	public static TilePowerSlug generatePowerSlugAt(World world, int x, int y, int z, Random rand, int tier, boolean gas, int reachDifficulty, boolean allowSpawns, int maxSpawnRadius) {
		while (y > 0 && ReikaWorldHelper.softBlocks(world, x, y-1, z))
			y--;
		Block b = world.getBlock(x, y-1, z);
		Block b1 = world.getBlock(x, y, z);
		if ((b1.isAir(world, x, y, z) || ReikaBlockHelper.isLiquid(b1)) && canExistOn(b)) {
			world.setBlock(x, y, z, SFBlocks.SLUG.getBlockInstance(), tier, 3);
			TilePowerSlug te = (TilePowerSlug)world.getTileEntity(x, y, z);
			if (allowSpawns) {
				switch(tier) {
					case 0:
						if (gas || reachDifficulty > 0 || rand.nextBoolean())
							te.setNoSpawns();
						else
							te.setDefaultSpawn(rand.nextInt(4) == 0 ? EntityCaveSpider.class : EntitySpider.class);
						break;
					case 1:
						if (gas) {
							if (rand.nextInt(3+reachDifficulty) > 0) {
								te.setNoSpawns();
							}
							else {
								te.setDefaultSpawn(EntityCaveSpider.class);
							}
						}
						else if (reachDifficulty > 0) {
							if (reachDifficulty >= 2) {
								if (rand.nextBoolean())
									te.setNoSpawns();
								else
									te.setSingleStrongEnemy(EntitySpider.class, 2);
							}
							else {
								te.setDefaultSpawn(rand.nextBoolean() ? EntityCaveSpider.class : EntitySpider.class);
								te.setEnemyBoost(1.5F);
							}
						}
						else {
							if (rand.nextBoolean()) {
								te.setSingleStrongEnemy(rand.nextInt(4) == 0 ? EntityEliteStinger.class : EntitySpider.class, 3);
							}
							else {
								te.setDefaultSpawn(EntitySpider.class);
								te.setEnemyBoost(2);
							}
						}
						break;
					case 2:
						int power = 4;
						if (gas)
							power--;
						power -= reachDifficulty;
						if (power < 0)
							power = 0;
						switch(power) {
							case 4:
								te.setDefaultSpawn(EntityEliteStinger.class);
								te.setEnemyBoost(2);
								break;
							case 3:
							case 2:
								if (rand.nextBoolean()) {
									te.setDefaultSpawn(EntityEliteStinger.class);
								}
								else {
									te.setDefaultSpawn(EntitySpider.class);
									te.setEnemyBoost(power);
								}
								break;
							case 1:
								te.setSingleStrongEnemy(rand.nextInt(3) == 0 ? EntityEliteStinger.class : EntitySpider.class, 4);
								break;
							case 0:
								if (rand.nextBoolean()) {
									te.setSingleStrongEnemy(rand.nextInt(4) == 0 ? EntityEliteStinger.class : EntitySpider.class, 3);
								}
								else {
									te.setDefaultSpawn(EntitySpider.class);
									te.setEnemyBoost(2.5F);
								}
								break;
						}
						break;
				}
				te.clampSpawnRadius(maxSpawnRadius);
			}
			else {
				te.setNoSpawns();
			}
			te.angle = world.rand.nextFloat()*360;
			//te.setMobType(getMobTypeForTier(tier));
			return te;
		}
		return null;
	}

	private static boolean canExistOn(Block b) {
		if (b == Blocks.grass || b == Blocks.dirt || b == Blocks.sand || b == Blocks.gravel || b == Blocks.stone)
			return true;
		if (b == SFBlocks.LOG.getBlockInstance() || b == SFBlocks.LEAVES.getBlockInstance() || b == SFBlocks.TERRAIN.getBlockInstance())
			return true;
		return false;
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
			this.setNoSpawns();
		}

		@Override
		public boolean canUpdate() {
			return false;
		}

	}

	public static class TilePowerSlug extends TileCaveSpawner {

		private static final int FOLLOW_RANGE = 32;

		private static final UUID healthBonus = UUID.fromString("cea3577b-784d-46e2-ae4c-3de297a10b66");

		public float angle;
		private ForgeDirection mounting = ForgeDirection.DOWN;

		private int tier;

		private float healthBuff = 0;

		private int soundtick;

		public TilePowerSlug() {
			this(0);
		}

		public TilePowerSlug(int meta) {
			tier = meta;
			this.setDefaultSpawn(EntityCaveSpider.class);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public double getMaxRenderDistanceSquared() {
			return 65536;
		}

		@Override
		public boolean shouldRenderInPass(int pass) {
			return pass <= 1;
		}

		public void setDirection(ForgeDirection dir) {
			mounting = dir;
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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
				this.doFX(worldObj, xCoord, yCoord, zCoord);
			}
		}

		@SideOnly(Side.CLIENT)
		private void doFX(World world, int x, int y, int z) {
			EntityPlayer ep = Minecraft.getMinecraft().thePlayer;
			double dist = ep.getDistance(xCoord+0.5, yCoord+0.5, zCoord+0.5);
			if (dist <= 128 && DragonAPICore.rand.nextInt(4+(int)(dist/32)) == 0) {
				int c = this.getBlockType().colorMultiplier(world, x, y, z);
				if (c == 0xF26030)
					return;
				if (DragonAPICore.rand.nextInt(2) == 0) {
					double px = ReikaRandomHelper.getRandomPlusMinus(x+0.5, 0.5);
					double pz = ReikaRandomHelper.getRandomPlusMinus(z+0.5, 0.5);
					double py = ReikaRandomHelper.getRandomBetween(y, y+0.5);
					float s = (float)ReikaRandomHelper.getRandomBetween(3.5, 7.5);
					int l = ReikaRandomHelper.getRandomBetween(30, 80);
					EntityBlurFX fx = new EntityBlurFX(world, px, py, pz, IconPrefabs.FADE_GENTLE.getIcon()).setColor(c).setScale(s).setLife(l).setAlphaFading();
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);
				}

				if (dist <= 64) {
					double px = ReikaRandomHelper.getRandomPlusMinus(x+0.5, 1.5);
					double pz = ReikaRandomHelper.getRandomPlusMinus(z+0.5, 1.5);
					double py = ReikaRandomHelper.getRandomBetween(y+0.5, y+2);
					double v = -0.04;
					double vx = (px-(x+0.5))*v;
					double vy = (py-(y+0.25))*v;
					double vz = (pz-(z+0.5))*v;
					EntitySlugStreak fx = new EntitySlugStreak(world, px, py, pz, vx, vy, vz, IconPrefabs.FADE.getIcon());
					fx.setColor(c).setScale(0.7F).setLife(20);
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);
				}
			}
		}

		@Override
		protected boolean isEmptyTimeoutActive(World world) {
			return false;
		}

		protected final void setDefaultSpawn(Class<? extends EntityMob> e) {
			this.setSpawnParameters(e, 3, 8, 6, FOLLOW_RANGE);
		}

		protected final void setSingleStrongEnemy(Class<? extends EntityMob> c, float boost) {
			this.setSpawnParameters(c, 1, 12, 2, FOLLOW_RANGE);
			healthBuff = boost;
		}

		protected final void setEnemyBoost(float boost) {
			healthBuff = boost;
		}

		protected final void setNoSpawns() {
			this.setSpawnParameters(null, 0, 0, 0, FOLLOW_RANGE);
		}

		@Override
		protected void onSpawnEntity(EntityMob e) {
			super.onSpawnEntity(e);
			if (healthBuff > 0) {
				AttributeModifier m = new AttributeModifier(healthBonus, "slugHealth", healthBuff-1, 2);
				e.getEntityAttribute(SharedMonsterAttributes.maxHealth).applyModifier(m);
			}
		}

		@Override
		protected boolean denyPassivation() {
			return true;
		}

		@Override
		protected double getResetRadius(double base) {
			return FOLLOW_RANGE*0.75;
		}

		@Override
		protected double getAutoClearRadius(double base) {
			return Math.max(this.getActivationRadius()*4, FOLLOW_RANGE*1.5);
		}

		public int getTier() {
			return tier;
		}

		@Override
		public void writeToNBT(NBTTagCompound NBT) {
			super.writeToNBT(NBT);

			NBT.setFloat("angle", angle);

			NBT.setInteger("tier", tier);
			NBT.setInteger("side", mounting.ordinal());
		}

		@Override
		public void readFromNBT(NBTTagCompound NBT) {
			super.readFromNBT(NBT);

			angle = NBT.getFloat("angle");

			tier = NBT.getInteger("tier");
			mounting = ForgeDirection.VALID_DIRECTIONS[NBT.getInteger("side")];
		}

	}

}
