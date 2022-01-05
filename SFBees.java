/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.Satisforestry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.ChromatiCraft.ModInterface.Bees.ChromaBeeHelpers;
import Reika.ChromatiCraft.ModInterface.Bees.TileEntityLumenAlveary;
import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Instantiable.Rendering.ColorBlendList;
import Reika.DragonAPI.Libraries.ReikaAABBHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.ModInteract.Bees.BasicFlowerProvider;
import Reika.DragonAPI.ModInteract.Bees.BasicGene;
import Reika.DragonAPI.ModInteract.Bees.BeeAlleleRegistry.Effect;
import Reika.DragonAPI.ModInteract.Bees.BeeAlleleRegistry.Fertility;
import Reika.DragonAPI.ModInteract.Bees.BeeAlleleRegistry.Flower;
import Reika.DragonAPI.ModInteract.Bees.BeeAlleleRegistry.Flowering;
import Reika.DragonAPI.ModInteract.Bees.BeeAlleleRegistry.Life;
import Reika.DragonAPI.ModInteract.Bees.BeeAlleleRegistry.Speeds;
import Reika.DragonAPI.ModInteract.Bees.BeeAlleleRegistry.Territory;
import Reika.DragonAPI.ModInteract.Bees.BeeAlleleRegistry.Tolerance;
import Reika.DragonAPI.ModInteract.Bees.BeeSpecies;
import Reika.DragonAPI.ModInteract.Bees.BeeSpecies.BeeBranch;
import Reika.DragonAPI.ModInteract.Bees.BeeSpecies.BeeBreeding;
import Reika.DragonAPI.ModInteract.Bees.ReikaBeeHelper;
import Reika.DragonAPI.ModInteract.ItemHandlers.ForestryHandler;
import Reika.Satisforestry.Blocks.BlockPinkGrass.GrassTypes;
import Reika.Satisforestry.Blocks.BlockPowerSlug;
import Reika.Satisforestry.Entity.EntityEliteStinger;
import Reika.Satisforestry.Entity.EntitySpitter;
import Reika.Satisforestry.Entity.EntitySpitter.SpitterType;
import Reika.Satisforestry.Registry.SFBlocks;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.FlowerManager;
import forestry.api.apiculture.IAlleleBeeEffect;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.core.EnumHumidity;
import forestry.api.core.EnumTemperature;
import forestry.api.genetics.IAlleleFlowers;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IFlowerProvider;

public class SFBees {

	private static final AllelePaleberry pbflower = new AllelePaleberry();
	private static final AlleleSlug slugflower = new AlleleSlug();
	private static final AlleleSlugEffect slugEffect = new AlleleSlugEffect();

	private static final BeeBranch branch = new BeeBranch("branch.pinkforest", "Pink Birch", "Silva Roseus", "Bees native to a strange elevated forest");

	private static final BeeSpecies baseSpecies = new BaseSFBee();
	private static final BeeSpecies paleberrySpecies = new PaleberryBee();
	private static final BeeSpecies slugSpecies = new SlugBee(); //effect: can spawn slugs in caves or on trees, but also spawns guard mobs around itself and the slug

	private static ColorBlendList paleberryColor;
	private static ColorBlendList slugColor;

	public static void register() {
		baseSpecies.register();
		paleberrySpecies.register();
		slugSpecies.register();

		baseSpecies.addBreeding("Exotic", "Forest", 15);

		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			loadColorData();
		}

		if (Loader.isModLoaded("extrabees")) {
			paleberrySpecies.addBreeding("extrabees.species.fruit", baseSpecies, 18);
		}
		else {
			paleberrySpecies.addBreeding("Unweary", baseSpecies, 15);
		}

		if (ModList.MAGICBEES.isLoaded()) {
			slugSpecies.addBreeding("magicbees.speciesDoctoral", baseSpecies, 5);
		}
		else if (Loader.isModLoaded("extrabees")) {
			slugSpecies.addBreeding("extrabees.species.unusual", baseSpecies, 4);
		}
		else {
			slugSpecies.addBreeding("Secluded", baseSpecies, 2);
		}
	}

	@SideOnly(Side.CLIENT)
	private static void loadColorData() {
		paleberryColor = new ColorBlendList(18F, 0xFF6672, 0xFF4032, 0xFF668C, 0xFF7266, 0xFF4C4C);
		List<Integer> li = new ArrayList();
		for (int i = 0; i < 8; i++)
			for (int k = 0; k < 3; k++)
				li.add(BlockPowerSlug.getColor(k));
		slugColor = new ColorBlendList(15F, li);
	}

	static abstract class SFBee extends BeeSpecies {

		protected SFBee(String name, String uid, String latinName) {
			super(name, uid, latinName, "Reika", branch);
		}

		@Override
		public EnumTemperature getTemperature() {
			return EnumTemperature.COLD;
		}

		@Override
		public EnumHumidity getHumidity() {
			return EnumHumidity.DAMP;
		}

		@Override
		public boolean hasEffect() {
			return false;
		}

		@Override
		public boolean isSecret() {
			return false;
		}

		@Override
		public boolean isCounted() {
			return true;
		}

		@Override
		public boolean isDominant() {
			return false;
		}

		@Override
		public boolean isNocturnal() {
			return false;
		}

		@Override
		public Territory getTerritorySize() {
			return Territory.DEFAULT;
		}

		@Override
		public boolean isCaveDwelling() {
			return false;
		}

		@Override
		public int getTemperatureTolerance() {
			return 0;
		}

		@Override
		public int getHumidityTolerance() {
			return 0;
		}

		@Override
		public Tolerance getHumidityToleranceDir() {
			return Tolerance.NONE;
		}

		@Override
		public Tolerance getTemperatureToleranceDir() {
			return Tolerance.NONE;
		}

		@Override
		public IAlleleBeeEffect getEffectAllele() {
			return Effect.NONE.getAllele();
		}

		@Override
		public boolean isTolerantFlyer() {
			return false;
		}

		@Override
		public boolean isJubilant(IBeeGenome ibg, IBeeHousing ibh) {
			return isPinkForest(ibh);
		}

		@Override
		public int getBeeStripeColor() {
			return this.getOutlineColor();
		}

		@Override
		protected BeeBreeding createBreeding(IAlleleBeeSpecies p1, IAlleleBeeSpecies p2, int chance) {
			return new SFBeeBreeding(p1, p2, chance, this);
		}

	}

	private static boolean isPinkForest(IBeeHousing ibh) {
		World world = ibh.getWorld();
		ChunkCoordinates c = ibh.getCoordinates();
		return Satisforestry.isPinkForest(world, c.posX, c.posZ) || (ModList.CHROMATICRAFT.isLoaded() && isLumenAlveary(ibh, world, c, false, true));
	}

	@ModDependent(ModList.CHROMATICRAFT)
	private static boolean isLumenAlveary(IBeeHousing ibh, World world, ChunkCoordinates cc, boolean omni, boolean infi) {
		TileEntityLumenAlveary te = ChromaBeeHelpers.getLumenAlvearyController(ibh, world, cc);
		return te != null && (!omni || te.hasOmnipresence()) && (!infi || te.hasInfiniteAwareness());
	}

	private static class SFBeeBreeding extends BeeBreeding {

		private SFBeeBreeding(IAlleleBeeSpecies p1, IAlleleBeeSpecies p2, int chance, SFBee bee) {
			super(p1, p2, chance, bee);
		}

		@Override
		protected boolean canBeBred(IBeeHousing ibh, IBeeGenome ig1, IBeeGenome ig2) {
			return isPinkForest(ibh);
		}
	}

	static class BaseSFBee extends SFBee {

		private BaseSFBee() {
			super("Roseate", "bee.pinkforest", "Silva Roseus");

			this.addProduct(ForestryHandler.Combs.HONEY.getItem(), 15);
		}

		@Override
		public String getDescription() {
			return "This bee is native to a strange vibrantly-hued forest.";
		}

		@Override
		public IAlleleFlowers getFlowerAllele() {
			return Flower.VANILLA.getAllele();
		}

		@Override
		public Speeds getProductionSpeed() {
			return Speeds.SLOWER;
		}

		@Override
		public Fertility getFertility() {
			return Fertility.HIGH;
		}

		@Override
		public Flowering getFloweringRate() {
			return Flowering.AVERAGE;
		}

		@Override
		public Life getLifespan() {
			return Life.SHORTENED;
		}

		@Override
		public int getOutlineColor() {
			return 0xFF5990;
		}

		@Override
		public int getTemperatureTolerance() {
			return 1;
		}

		@Override
		public Tolerance getTemperatureToleranceDir() {
			return Tolerance.BOTH;
		}

	}

	static class PaleberryBee extends SFBee {

		private PaleberryBee() {
			super("Paleberry", "bee.paleberry", "Pomus Pallidus");

			this.addSpecialty(new ItemStack(Satisforestry.paleberry, 1, 1), 10);
			this.addProduct(ForestryHandler.Combs.HONEY.getItem(), 10);
		}

		@Override
		public String getDescription() {
			return "This bee collects chunks of a special species of berries from its environment.";
		}

		@Override
		public int getOutlineColor() {
			return paleberryColor != null ? paleberryColor.getColor(DragonAPICore.getSystemTimeAsInt()/40D) : 0xFF6672;
		}

		@Override
		public IAlleleFlowers getFlowerAllele() {
			return pbflower;
		}

		@Override
		public Speeds getProductionSpeed() {
			return Speeds.NORMAL;
		}

		@Override
		public Fertility getFertility() {
			return Fertility.NORMAL;
		}

		@Override
		public Flowering getFloweringRate() {
			return Flowering.FASTEST;
		}

		@Override
		public Life getLifespan() {
			return Life.SHORTER;
		}

	}

	static class SlugBee extends SFBee {

		private SlugBee() {
			super("Sluggy", "bee.powerslug", "Potentia Limax");

			this.addProduct(ForestryHandler.Combs.MOSSY.getItem(), 5); //add furtive comb if present
		}

		@Override
		public String getDescription() {
			return "This bee can attract various creatures, including strange glowing slugs of unknown value.";
		}

		@Override
		public int getOutlineColor() {
			return slugColor != null ? slugColor.getColor(DragonAPICore.getSystemTimeAsInt()/10D) : 0xFFFFFF;
		}

		@Override
		public IAlleleFlowers getFlowerAllele() {
			return slugflower;
		}

		@Override
		public Speeds getProductionSpeed() {
			return Speeds.FASTER;
		}

		@Override
		public Fertility getFertility() {
			return Fertility.LOW;
		}

		@Override
		public Flowering getFloweringRate() {
			return Flowering.SLOWEST;
		}

		@Override
		public Life getLifespan() {
			return Life.ELONGATED;
		}

		@Override
		public IAlleleBeeEffect getEffectAllele() {
			return slugEffect;
		}

		@Override
		public EnumTemperature getTemperature() {
			return EnumTemperature.NORMAL;
		}

		@Override
		public EnumHumidity getHumidity() {
			return EnumHumidity.NORMAL;
		}

	}

	static final class AlleleSlugEffect extends BasicGene implements IAlleleBeeEffect {

		private long lastWorldTick;

		public AlleleSlugEffect() {
			super("effect.powerslug", "Mysterious Attraction", EnumBeeChromosome.EFFECT);
		}

		@Override
		public boolean isCombinable() {
			return false;
		}

		@Override
		public IEffectData validateStorage(IEffectData ied) {
			return ied;
		}

		@Override
		public IEffectData doEffect(IBeeGenome ibg, IEffectData ied, IBeeHousing ibh) {
			World world = ibh.getWorld();
			long time = world.getTotalWorldTime();
			//if (lastWorldTick != time) {
			//lastWorldTick = time;
			ChunkCoordinates cc = ibh.getCoordinates();
			if (world.rand.nextInt(25000) == 0) {
				int[] r = ReikaBeeHelper.getEffectiveTerritory(ibh, cc, ibg, time);
				r = Arrays.copyOf(r, r.length);
				r[0] = Math.min(r[0], 24);
				r[1] = Math.min(r[1], 24);
				r[2] = Math.min(r[2], 24);
				this.trySpawnPowerSlug(world, cc, r, ibh);
			}
			if (world.rand.nextInt(2000) == 0) {
				int[] r = ReikaBeeHelper.getEffectiveTerritory(ibh, cc, ibg, time);
				AxisAlignedBB box = ReikaAABBHelper.getBlockAABB(cc.posX, cc.posY, cc.posZ).expand(r[0], r[1], r[2]);
				for (EntityPlayer ep : ((List<EntityPlayer>)world.playerEntities)) {
					if (ep.boundingBox.intersectsWith(box) || isLumenAlveary(ibh, world, cc, true, false)) {
						int n = ReikaRandomHelper.getRandomBetween(1, 3);
						this.spawnMobsAround(world, ep.posX, ep.posY, ep.posZ, 6, 1, n);
					}
				}
			}
			else if (world.rand.nextInt(200) == 0) {
				EntityPlayer ep = world.getClosestPlayer(cc.posX+0.5, cc.posY+0.5, cc.posZ+0.5, 64);
				if (ep != null) {
					AxisAlignedBB box = ReikaAABBHelper.getBlockAABB(cc.posX, cc.posY, cc.posZ).expand(12, 4, 12);
					List<EntityMob> li = world.getEntitiesWithinAABB(EntityMob.class, box);
					if (li.size() <= 2) {
						int n = ReikaRandomHelper.getRandomBetween(1, 4);
						this.spawnMobsAround(world, cc.posX+0.5, cc.posY+0.5, cc.posZ+0.5, 9, 4, n);
					}
				}
			}
			//}
			return ied;
		}

		private void trySpawnPowerSlug(World world, ChunkCoordinates cc, int[] r, IBeeHousing ibh) {
			for (int x = cc.posX-r[0]; x <= cc.posX+r[0]; x++) {
				for (int y = cc.posY-r[1]; y <= cc.posY+r[1]; y++) {
					for (int z = cc.posZ-r[2]; z <= cc.posZ+r[2]; z++) {
						if (Math.abs(x-cc.posX)+Math.abs(y-cc.posY)+Math.abs(z-cc.posZ) <= 5)
							continue;
						if (world.getBlock(x, y, z) == SFBlocks.SLUG.getBlockInstance()) {
							return;
						}
					}
				}
			}
			int x = ReikaRandomHelper.getRandomPlusMinus(cc.posX, r[0]);
			int y = ReikaRandomHelper.getRandomPlusMinus(cc.posY, r[1]);
			int z = ReikaRandomHelper.getRandomPlusMinus(cc.posZ, r[2]);
			if (BlockPowerSlug.canReplace(world, x, y, z)) {
				for (int i = 0; i < 6; i++) {
					ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i];
					if (BlockPowerSlug.canExistOn(world, x+dir.offsetX, y+dir.offsetY, z+dir.offsetZ)) {
						int tier = 0;
						if (world.rand.nextInt(48) == 0)
							tier = 2;
						else if (world.rand.nextInt(12) == 0)
							tier = 1;
						BlockPowerSlug.generatePowerSlugAt(world, x, y, z, world.rand, tier, false, -1, true, 6, dir);
						break;
					}
				}
			}
		}

		private void spawnMobsAround(World world, double x, double y, double z, double rxz, double ry, int n) {
			Class<? extends EntityMob> c = world.rand.nextBoolean() ? EntityCaveSpider.class : EntitySpider.class;
			SpitterType s = SpitterType.BASIC;
			if (world.rand.nextInt(16) == 0) {
				c = EntityEliteStinger.class;
			}
			else if (world.rand.nextInt(4) == 0) {
				c = EntitySpitter.class;
				if (world.rand.nextInt(3) == 0) {
					s = world.rand.nextBoolean() ? SpitterType.RED : SpitterType.GREEN;
				}
			}
			String nn = (String)EntityList.classToStringMapping.get(c);
			for (int i = 0; i < n; i++) {
				EntityMob e = (EntityMob)EntityList.createEntityByName(nn, world);
				if (e instanceof EntitySpitter) {
					((EntitySpitter)e).setSpitterType(s);
				}
				double dx = ReikaRandomHelper.getRandomPlusMinus(x, rxz);
				double dz = ReikaRandomHelper.getRandomPlusMinus(z, rxz);
				double dy = ReikaRandomHelper.getRandomPlusMinus(y, ry);
				double h = 0;
				while (h <= 2) {
					e.setLocationAndAngles(dx, dy+h, dz, 0, 0);
					if (world.getCollidingBoundingBoxes(e, e.boundingBox).isEmpty()) {
						AxisAlignedBB offset = e.boundingBox.copy();
						double oy = 0;
						while (world.getCollidingBoundingBoxes(e, offset).isEmpty()) {
							oy += 0.5;
							offset = offset.offset(0, -0.5, 0);
						}
						e.setLocationAndAngles(dx, dy+h-oy+0.5, dz, 0, 0);
						world.spawnEntityInWorld(e);
						break;
					}
					h += 0.5;
				}
			}
		}

		@Override
		@SideOnly(Side.CLIENT)
		public IEffectData doFX(IBeeGenome ibg, IEffectData ied, IBeeHousing ibh) {
			World world = ibh.getWorld();
			ChunkCoordinates c = ibh.getCoordinates();


			return ied;
		}
	}

	private static final class AllelePaleberry extends BasicGene implements IAlleleFlowers {

		private final FlowerProviderPaleberry flowers = new FlowerProviderPaleberry();

		public AllelePaleberry() {
			super("flower.paleberry", "Ripe Paleberry", EnumBeeChromosome.FLOWER_PROVIDER);
		}

		@Override
		public IFlowerProvider getProvider() {
			return flowers;
		}
	}

	private static final class FlowerProviderPaleberry extends BasicFlowerProvider {

		private FlowerProviderPaleberry() {
			super(SFBlocks.GRASS.getBlockInstance(), GrassTypes.PALEBERRY_NEW.ordinal(), "paleberry");
			FlowerManager.flowerRegistry.registerAcceptableFlowerRule(this, this.getFlowerType());
		}

		@Override
		public String getDescription() {
			return "Ripe Paleberries";
		}
	}

	private static final class AlleleSlug extends BasicGene implements IAlleleFlowers {

		private final FlowerProviderSlug flowers = new FlowerProviderSlug();

		public AlleleSlug() {
			super("flower.slug", "Power Slug", EnumBeeChromosome.FLOWER_PROVIDER);
		}

		@Override
		public IFlowerProvider getProvider() {
			return flowers;
		}
	}

	private static final class FlowerProviderSlug extends BasicFlowerProvider {

		private FlowerProviderSlug() {
			super(SFBlocks.SLUG.getBlockInstance(), "slug");
			FlowerManager.flowerRegistry.registerAcceptableFlowerRule(this, this.getFlowerType());
		}

		@Override
		public String getDescription() {
			return "Power Slugs";
		}
	}

}
