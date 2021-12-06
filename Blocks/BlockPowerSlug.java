package Reika.Satisforestry.Blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
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
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.Extras.IconPrefabs;
import Reika.DragonAPI.Instantiable.Data.Maps.CountMap;
import Reika.DragonAPI.Instantiable.Effects.EntityBlurFX;
import Reika.DragonAPI.Interfaces.Block.Submergeable;
import Reika.DragonAPI.Libraries.ReikaNBTHelper;
import Reika.DragonAPI.Libraries.ReikaNBTHelper.NBTIO;
import Reika.DragonAPI.Libraries.ReikaNBTHelper.NBTTypes;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.World.ReikaBlockHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockCaveSpawner.TileCaveSpawner;
import Reika.Satisforestry.Entity.EntityEliteStinger;
import Reika.Satisforestry.Entity.EntitySpitter;
import Reika.Satisforestry.Entity.EntitySpitter.SpitterType;
import Reika.Satisforestry.Registry.SFBlocks;
import Reika.Satisforestry.Registry.SFOptions;
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
				return SFOptions.BLUEGREEN.getState() ? 0x5bd7ff : 0x94FF7F;
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
			float lz = length*rb+width*ra;
			float lx = width*rb+length*ra;
			if (ts.mounting.offsetY == 0) {
				switch(ts.mounting) {
					case EAST:
						this.setBlockBounds(1-height, 0.5F-lx/2, 0.5F-lz/2, 1, 0.5F+lx/2, 0.5F+lz/2);
						break;
					case WEST:
						this.setBlockBounds(0, 0.5F-lx/2, 0.5F-lz/2, height, 0.5F+lx/2, 0.5F+lz/2);
						break;
					case SOUTH:
						this.setBlockBounds(0.5F-lx/2, 0.5F-lz/2, 1-height, 0.5F+lz/2, 0.5F+lz/2, 1);
						break;
					case NORTH:
						this.setBlockBounds(0.5F-lx/2, 0.5F-lz/2, 0, 0.5F+lx/2, 0.5F+lz/2, height);
						break;
					default:
						break;
				}
			}
			else {
				float bottom = ts.mounting == ForgeDirection.DOWN ? 0 : 1-height;
				float top = ts.mounting == ForgeDirection.DOWN ? height : 1;
				this.setBlockBounds(0.5F-lx/2F, bottom, 0.5F-lz/2F, 0.5F+lx/2F, top, 0.5F+lz/2F);
			}
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
		return generatePowerSlugAt(world, x, y, z, rand, tier, gas, reachDifficulty, allowSpawns, Integer.MAX_VALUE, ForgeDirection.DOWN);
	}

	public static TilePowerSlug generatePowerSlugAt(World world, int x, int y, int z, Random rand, int tier, boolean gas, int reachDifficulty, boolean allowSpawns, int maxSpawnRadius, ForgeDirection dir) {
		while (dir.offsetY != 0 && y > 0 && y < 256 && ReikaWorldHelper.softBlocks(world, x+dir.offsetX, y+dir.offsetY, z+dir.offsetZ))
			y--;
		Block b = world.getBlock(x+dir.offsetX, y+dir.offsetY, z+dir.offsetZ);
		Block b1 = world.getBlock(x, y, z);
		if (y < 256 && (b1.isAir(world, x, y, z) || ReikaBlockHelper.isLiquid(b1) || b1 == Blocks.vine) && canExistOn(b)) {
			world.setBlock(x, y, z, SFBlocks.SLUG.getBlockInstance(), tier, 3);
			TilePowerSlug te = (TilePowerSlug)world.getTileEntity(x, y, z);
			if (te == null) {
				te = new TilePowerSlug();
				world.setTileEntity(x, y, z, te);
			}
			te.setDirection(dir);
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
								if (rand.nextBoolean()) {
									te.setNoSpawns();
								}
								else {
									te.setSingleStrongEnemy(EntitySpider.class, 2);
								}
							}
							else {
								double f = rand.nextDouble();
								if (f <= 0.45) {
									te.setDefaultSpawn(rand.nextBoolean() ? EntityCaveSpider.class : EntitySpider.class);
									te.setEnemyBoost(1.5F);
								}
								else if (f < 0.95) {
									te.setDefaultSpawn(EntitySpitter.class);
									te.spawnCallback = new SpitterDistribution(SpitterType.BASIC);
								}
								else {
									te.setDefaultSpawn(EntitySpitter.class, 1);
									te.spawnCallback = new SpitterDistribution(SpitterType.RED);
								}
							}
						}
						else {
							double f = rand.nextDouble();
							if (f <= 0.1) {
								boolean stinger = rand.nextInt(5) == 0;
								te.setSingleStrongEnemy(stinger ? EntityEliteStinger.class : EntitySpider.class, stinger ? 1 : 3);
							}
							else if (f < 0.3) {
								te.setDefaultSpawn(EntitySpider.class);
								te.setEnemyBoost(2);
							}
							else if (f < 0.6) {
								te.setDefaultSpawn(EntitySpitter.class);
								te.spawnCallback = new SpitterDistribution(SpitterType.BASIC);
								te.setEnemyBoost(1.5F);
							}
							else {
								te.setDefaultSpawn(EntitySpitter.class, 1);
								te.spawnCallback = new SpitterDistribution(f >= 0.95 ? SpitterType.GREEN : SpitterType.RED);
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
						double f = rand.nextDouble();
						switch(power) {
							case 4:
								if (f < 0.3) {
									te.setDefaultSpawn(EntityEliteStinger.class);
									te.setEnemyBoost(2);
								}
								else if (f < 0.6) {
									te.spawnCallback = new SpitterDistribution(rand.nextDouble() < 0.4 ? SpitterType.RED : SpitterType.GREEN, 1, 1, SpitterType.BASIC, 4, 1.5F);
									te.setDefaultSpawn(EntitySpitter.class, ((SpitterDistribution)te.spawnCallback).totalCount());
								}
								else {
									//te.setDefaultSpawn(EntitySpitter.class, 2, rand.nextBoolean() ? SpitterType.GREEN : SpitterType.RED);
									te.spawnCallback = new SpitterDistribution(rand.nextDouble() < 0.6 ? SpitterType.RED : SpitterType.GREEN, 2, 1, SpitterType.BASIC, 4, 1);
									te.setDefaultSpawn(EntitySpitter.class, ((SpitterDistribution)te.spawnCallback).totalCount());
								}
								break;
							case 3:
							case 2:
								if (f < 0.2) {
									te.setDefaultSpawn(EntityEliteStinger.class);
								}
								else if (f < 0.5) {
									te.setDefaultSpawn(EntitySpider.class);
									te.setEnemyBoost(power);
								}
								else {
									te.spawnCallback = new SpitterDistribution(rand.nextDouble() < 0.5 ? SpitterType.RED : SpitterType.GREEN, 1, 1, SpitterType.BASIC, 4, 1.5F);
									te.setDefaultSpawn(EntitySpitter.class, ((SpitterDistribution)te.spawnCallback).totalCount());
								}
								break;
							case 1:
								if (f < 0.15) {
									te.setSingleStrongEnemy(EntityEliteStinger.class, 3);
								}
								else if (f < 0.3) {
									te.setSingleStrongEnemy(EntitySpider.class, 4);
								}
								else if (f < 0.7) {
									te.spawnCallback = new SpitterDistribution(rand.nextDouble() < 0.55 ? SpitterType.RED : SpitterType.GREEN, 1, 1, SpitterType.BASIC, 3, 1.5F);
									te.setDefaultSpawn(EntitySpitter.class, ((SpitterDistribution)te.spawnCallback).totalCount());
								}
								else {
									te.setDefaultSpawn(EntitySpitter.class, 1);
									te.spawnCallback = new SpitterDistribution(rand.nextDouble() < 0.7 ? SpitterType.GREEN : SpitterType.RED);
								}
								break;
							case 0:
								if (f < 0.15) {
									te.setSingleStrongEnemy(EntityEliteStinger.class, 2);
								}
								else if (f < 0.3) {
									te.setDefaultSpawn(EntitySpider.class);
									te.setEnemyBoost(2.5F);
								}
								else if (f < 0.5) {
									te.spawnCallback = new SpitterDistribution(rand.nextDouble() < 0.6 ? SpitterType.RED : SpitterType.GREEN, 1, 1, SpitterType.BASIC, 4, 1);
									te.setDefaultSpawn(EntitySpitter.class, ((SpitterDistribution)te.spawnCallback).totalCount());
								}
								else if (f < 0.9) {
									te.setDefaultSpawn(EntitySpitter.class, 1);
									te.spawnCallback = new SpitterDistribution(rand.nextBoolean() ? SpitterType.GREEN : SpitterType.RED);
								}
								else {
									te.setDefaultSpawn(EntitySpitter.class);
									te.spawnCallback = new SpitterDistribution(SpitterType.BASIC);
									te.setEnemyBoost(1.5F);
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

	static class SpitterDistribution extends SpawnCallback {

		private static final NBTIO<SpitterType> enu = (NBTIO<SpitterType>)ReikaNBTHelper.getEnumConverter(SpitterType.class);

		private final CountMap<SpitterType> data = new CountMap();
		private final HashMap<SpitterType, Float> buff = new HashMap();

		SpitterDistribution() {

		}

		private SpitterDistribution(SpitterType only) {
			this(new SpitterType[] {only}, new int[] {1}, new float[] {1});
		}

		private SpitterDistribution(SpitterType t1, int amt1, float b1, SpitterType t2, int amt2, float b2) {
			this(new SpitterType[] {t1, t2}, new int[] {amt1, amt2}, new float[] {b1, b2});
		}

		private SpitterDistribution(SpitterType[] t, int[] amt, float[] b) {
			for (int i = 0; i < t.length; i++) {
				SpitterType st = t[i];
				data.increment(st, amt[i]);
				buff.put(st, b[i]);
			}
		}

		@Override
		public void onSpawn(EntityMob e, ArrayList<EntityLiving> spawned) {
			CountMap<SpitterType> has = new CountMap();
			for (EntityLiving e2 : spawned) {
				SpitterType s = ((EntitySpitter)e2).getSpitterType();
				has.increment(s, 1);
			}

			for (SpitterType s : data.keySet()) {
				int needed = data.get(s)-has.get(s);
				if (needed > 0) {
					((EntitySpitter)e).setSpitterType(s);
					Float i = buff.get(s);
					if (i != null)
						TilePowerSlug.buffHealth(e, i.floatValue());
					break;
				}
			}
		}

		public int totalCount() {
			return data.getTotalCount();
		}

		@Override
		protected void readFromNBT(NBTTagCompound tag) {
			buff.clear();
			data.clear();
			NBTTagList li = tag.getTagList("buffs", NBTTypes.COMPOUND.ID);
			data.readFromNBT(tag.getCompoundTag("counts"), enu);
			ReikaNBTHelper.readMapFromNBT(buff, li, enu, null);
		}

		@Override
		protected void writeToNBT(NBTTagCompound tag) {
			NBTTagCompound counts = new NBTTagCompound();
			NBTTagList li = new NBTTagList();
			data.writeToNBT(counts, enu);
			ReikaNBTHelper.writeMapToNBT(buff, li, enu, null);
			tag.setTag("buffs", li);
			tag.setTag("counts", counts);
		}

	}

	private static abstract class SpawnCallback {

		public abstract void onSpawn(EntityMob e, ArrayList<EntityLiving> spawned);

		protected abstract void readFromNBT(NBTTagCompound tag);
		protected abstract void writeToNBT(NBTTagCompound tag);

	}

	public static class TilePowerSlug extends TileCaveSpawner {

		private static final int FOLLOW_RANGE = 32;

		private static final UUID healthBonus = UUID.fromString("cea3577b-784d-46e2-ae4c-3de297a10b66");

		public float angle;
		private ForgeDirection mounting = ForgeDirection.DOWN;

		private int tier;

		private float healthBuff = 0;

		private int soundtick;

		private SpawnCallback spawnCallback = null;

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
			this.setDefaultSpawn(e, 3);
		}

		protected final void setDefaultSpawn(Class<? extends EntityMob> e, int n) {
			this.setSpawnParameters(e, n, 8+tier, 6, FOLLOW_RANGE);
		}

		protected final void setSingleStrongEnemy(Class<? extends EntityMob> c, float boost) {
			this.setSpawnParameters(c, 1, 13+tier/2, 2, FOLLOW_RANGE);
			this.setEnemyBoost(boost);
		}

		protected final void setEnemyBoost(float boost) {
			healthBuff = boost;
		}

		protected final void clampSpawnRadius(int r) {
			this.setSpawnRadius(Math.min(r, this.getSpawnRadius()));
		}

		public final void setNoSpawns() {
			this.setSpawnParameters(null, 0, 0, 0, FOLLOW_RANGE);
		}

		@Override
		protected boolean canSpawnEntityAt(EntityMob e) {
			return this.hasFloor(e);
		}

		private boolean hasFloor(EntityMob e) {
			int n = 0;
			int x1 = MathHelper.floor_double(e.boundingBox.minX);
			int x2 = MathHelper.floor_double(e.boundingBox.maxX);
			int z1 = MathHelper.floor_double(e.boundingBox.minZ);
			int z2 = MathHelper.floor_double(e.boundingBox.maxZ);
			int y0 = MathHelper.floor_double(e.boundingBox.minY);
			for (int x = x1; x <= x2; x++) {
				for (int z = z1; z <= z2; z++) {
					for (int y = y0; y >= y0-2; y--) {
						if (e.worldObj.getBlock(x, y, z).getMaterial().isSolid()) {
							n++;
							break;
						}
					}
				}
			}
			return n >= 2;
		}

		@Override
		protected void onSpawnEntity(EntityMob e, ArrayList<EntityLiving> spawned) {
			super.onSpawnEntity(e, spawned);
			if (spawnCallback != null)
				spawnCallback.onSpawn(e, spawned);
			if (healthBuff > 0) {
				buffHealth(e, healthBuff);
			}
		}

		private static void buffHealth(EntityMob e, float buff) {
			AttributeModifier m = e.getEntityAttribute(SharedMonsterAttributes.maxHealth).getModifier(healthBonus);
			if (m == null) {
				m = new AttributeModifier(healthBonus, "slugHealth", buff-1, 2);
			}
			else {
				double amt = 1+m.getAmount();
				e.getEntityAttribute(SharedMonsterAttributes.maxHealth).removeModifier(m);
				m = new AttributeModifier(healthBonus, "slugHealth", amt*buff-1, 2);
			}
			e.getEntityAttribute(SharedMonsterAttributes.maxHealth).applyModifier(m);
			e.setHealth(e.getMaxHealth());
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

		public ForgeDirection getDirection() {
			return mounting != null ? mounting : ForgeDirection.DOWN;
		}

		@Override
		public void writeToNBT(NBTTagCompound NBT) {
			super.writeToNBT(NBT);

			NBT.setFloat("angle", angle);

			NBT.setFloat("buff", healthBuff);

			NBT.setInteger("tier", tier);
			NBT.setInteger("side", mounting.ordinal());

			if (spawnCallback != null) {
				NBTTagCompound tag = new NBTTagCompound();
				spawnCallback.writeToNBT(tag);
				tag.setString("classType", spawnCallback.getClass().getName());
				NBT.setTag("callback", tag);
			}
		}

		@Override
		public void readFromNBT(NBTTagCompound NBT) {
			super.readFromNBT(NBT);

			angle = NBT.getFloat("angle");

			healthBuff = NBT.getFloat("buff");

			tier = NBT.getInteger("tier");
			mounting = ForgeDirection.VALID_DIRECTIONS[NBT.getInteger("side")];

			if (NBT.hasKey("callback")) {
				try {
					NBTTagCompound tag = NBT.getCompoundTag("callback");
					spawnCallback = (SpawnCallback)Class.forName(tag.getString("classType")).newInstance();
					spawnCallback.readFromNBT(tag);
				}
				catch (Exception e) {
					e.printStackTrace();
					Satisforestry.logger.logError("Could not reconstruct slug spawn callback: "+e.toString());
				}
			}
		}

	}

}
