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
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.ChromatiCraft.ModInterface.Bees.ChromaBeeHelpers;
import Reika.ChromatiCraft.ModInterface.Bees.TileEntityLumenAlveary;
import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.IO.DirectResourceManager;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Instantiable.Rendering.ColorBlendList;
import Reika.DragonAPI.Libraries.ReikaAABBHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.Rendering.ReikaColorAPI;
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
import Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Size;
import Reika.DragonAPI.ModInteract.Bees.ButterflySpecies;
import Reika.DragonAPI.ModInteract.Bees.ButterflySpecies.BasicButterflySpecies;
import Reika.DragonAPI.ModInteract.Bees.ButterflySpecies.ButterflyBranch;
import Reika.DragonAPI.ModInteract.Bees.ReikaBeeHelper;
import Reika.DragonAPI.ModInteract.Bees.TreeAlleleRegistry.Heights;
import Reika.DragonAPI.ModInteract.Bees.TreeAlleleRegistry.Maturation;
import Reika.DragonAPI.ModInteract.Bees.TreeAlleleRegistry.Saplings;
import Reika.DragonAPI.ModInteract.Bees.TreeAlleleRegistry.Sappiness;
import Reika.DragonAPI.ModInteract.Bees.TreeAlleleRegistry.Yield;
import Reika.DragonAPI.ModInteract.Bees.TreeSpecies;
import Reika.DragonAPI.ModInteract.Bees.TreeSpecies.BasicTreeSpecies;
import Reika.DragonAPI.ModInteract.Bees.TreeSpecies.NoLocaleDescriptionFruit;
import Reika.DragonAPI.ModInteract.Bees.TreeSpecies.TreeBranch;
import Reika.DragonAPI.ModInteract.ItemHandlers.ForestryHandler;
import Reika.DragonAPI.ModInteract.ItemHandlers.MagicBeesHandler;
import Reika.Satisforestry.Biome.Generator.PinkTreeGeneratorBase;
import Reika.Satisforestry.Biome.Generator.PinkTreeGeneratorBase.PinkTreeTypes;
import Reika.Satisforestry.Blocks.BlockPinkGrass.GrassTypes;
import Reika.Satisforestry.Blocks.BlockPinkLeaves;
import Reika.Satisforestry.Blocks.BlockPowerSlug;
import Reika.Satisforestry.Blocks.BlockTerrain.TerrainType;
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
import forestry.api.arboriculture.EnumGermlingType;
import forestry.api.arboriculture.EnumTreeChromosome;
import forestry.api.arboriculture.IAlleleFruit;
import forestry.api.arboriculture.IFruitProvider;
import forestry.api.arboriculture.ITree;
import forestry.api.arboriculture.ITreeGenome;
import forestry.api.core.EnumHumidity;
import forestry.api.core.EnumTemperature;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IAlleleFlowers;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IFlowerProvider;
import forestry.api.genetics.IFruitFamily;
import forestry.api.lepidopterology.IAlleleButterflyEffect;
import forestry.api.world.ITreeGenData;

public class SFBees {

	private static final AllelePaleberry pbflower = new AllelePaleberry();
	private static final AlleleSlug slugflower = new AlleleSlug();
	private static final AlleleSlugEffect slugEffect = new AlleleSlugEffect();

	private static final BeeBranch branch = new BeeBranch("branch.pinkforest", "Pink Birch", "Silva Roseus", "These bees seem to be native to a strangely colored and elevated forest.");
	private static final TreeBranch treeBranch = new TreeBranch("branch.pinkforesttree", "Pink Birch", "Silva Roseus", "These trees populate a rare elevated forest, and are far more durable than they look.");
	private static final ButterflyBranch butterflyBranch = new ButterflyBranch("branch.pinkforestfly", "Pink Birch", "Silva Roseus", "These butterflies can rarely be found flitting among giant pink trees.");

	private static final BeeSpecies baseSpecies = new BaseSFBee();
	private static final BeeSpecies paleberrySpecies = new PaleberryBee();
	private static final BeeSpecies slugSpecies = new SlugBee();

	private static final TreeSpecies treeSpecies = new PinkTree();

	private static final ButterflySpecies basicPinkSpecies = new BasicSFButterfly();
	private static final ButterflySpecies grassSpriteSpecies = new GrassSpriteButterfly();
	private static final ButterflySpecies paleberryFlySpecies = new PaleberryButterfly();

	private static final Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Life blinkLife = Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Life.createNew("sprite", 1, false);

	private static ColorBlendList paleberryColor;
	private static ColorBlendList slugColor;

	private static final IAlleleFruit paleberryFruit = new PaleberryFruit();

	private static final IFruitFamily paleberryFamily = new IFruitFamily() {

		@Override
		public String getUID() {
			return "paleberry";
		}

		@Override
		public String getName() {
			return "Paleberries";
		}

		@Override
		public String getScientific() {
			return "Pomus Pallidus";
		}

		@Override
		public String getDescription() {
			return "Paleberries";
		}

	};

	public static void register() {
		baseSpecies.register();
		paleberrySpecies.register();
		slugSpecies.register();

		baseSpecies.addBreeding("Exotic", "Forest", 15);

		treeSpecies.register();
		treeSpecies.addSuitableFruit(paleberryFamily);

		basicPinkSpecies.register();
		grassSpriteSpecies.register();
		paleberryFlySpecies.register();

		ITree ii = treeSpecies.constructIndividual();
		AlleleManager.ersatzSaplings.put(PinkTreeTypes.TREE.getBaseLeaf(), ii);
		AlleleManager.ersatzSpecimen.put(PinkTreeTypes.TREE.getBaseLeaf(), ii);
		AlleleManager.ersatzSaplings.put(PinkTreeTypes.TREE.getSapling(), ii);
		AlleleManager.ersatzSpecimen.put(PinkTreeTypes.TREE.getSapling(), ii);

		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			loadColorData();
		}

		if (Loader.isModLoaded("ExtraBees")) {
			paleberrySpecies.addBreeding((IAlleleBeeSpecies)AlleleManager.alleleRegistry.getAllele("extrabees.species.fruit"), baseSpecies, 18);
		}
		else {
			paleberrySpecies.addBreeding("Unweary", baseSpecies, 15);
		}

		if (ModList.MAGICBEES.isLoaded()) {
			slugSpecies.addBreeding((IAlleleBeeSpecies)AlleleManager.alleleRegistry.getAllele("magicbees.speciesDoctoral"), baseSpecies, 5);
		}
		else if (Loader.isModLoaded("ExtraBees")) {
			slugSpecies.addBreeding((IAlleleBeeSpecies)AlleleManager.alleleRegistry.getAllele("extrabees.species.unusual"), baseSpecies, 4);
		}
		else {
			slugSpecies.addBreeding("Secluded", baseSpecies, 2);
		}
	}

	@SideOnly(Side.CLIENT)
	private static void loadColorData() {
		paleberryColor = new ColorBlendList(18F, 0xFF6672, 0xFF4032, 0xFF668C, 0xFF7266, 0xFF4C4C);
		List<Integer> li = new ArrayList();
		for (int k = 0; k < 3; k++)
			for (int i = 0; i < 8; i++)
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
			this.addProduct(SFBlocks.GRASS.getStackOfMetadata(GrassTypes.PEACH_FRINGE.ordinal()), 2);
			this.addSpecialty(SFBlocks.GRASS.getStackOfMetadata(GrassTypes.BLUE_MUSHROOM_TOP.ordinal()), 1);
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

			this.addProduct(ForestryHandler.Combs.MOSSY.getItem(), 5);
			if (ModList.MAGICBEES.isLoaded()) {
				this.addProduct(MagicBeesHandler.Combs.FURTIVE.getItem(), 5);
			}
			this.addSpecialty(SFBlocks.TERRAIN.getStackOfMetadata(TerrainType.OUTCROP.ordinal()), 1);
			this.addSpecialty(SFBlocks.TERRAIN.getStackOfMetadata(TerrainType.POISONROCK.ordinal()), 1);
			this.addSpecialty(SFBlocks.TERRAIN.getStackOfMetadata(TerrainType.PONDROCK.ordinal()), 1);
		}

		@Override
		public String getDescription() {
			return "This bee can attract various creatures, including strange glowing slugs of unknown value.";
		}

		@Override
		public int getOutlineColor() {
			return slugColor != null ? slugColor.getColor(DragonAPICore.getSystemTimeAsInt()/15D) : 0xFFFFFF;
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

		@Override
		public boolean hasEffect() {
			return true;
		}

		@Override
		public boolean isSecret() {
			return true;
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

	private static class PinkTree extends BasicTreeSpecies {

		protected PinkTree() {
			super("Pink Birch", "tree.pinkforest", "Rosea Silva", "Reika", treeBranch);
		}

		@Override
		public IAlleleFruit getFruitAllele() {
			return paleberryFruit;
		}

		@Override
		public boolean isFireproof() {
			return true;
		}

		@Override
		protected final String getIconMod(boolean pollen) {
			return "satisforestry";
		}

		@Override
		protected final String getIconFolderRoot(boolean pollen) {
			return "forestry/trees";
		}

		@Override
		protected final String getSaplingIconName() {
			return "sapling";//"dye-sapling-"+color.name().toLowerCase(Locale.ENGLISH);
		}

		@Override
		public int getLeafColour(boolean pollinated) {
			return this.getIconColour(0);
		}

		@Override
		public IIcon getLeafIcon(boolean pollinated, boolean fancy) {
			return ((BlockPinkLeaves)SFBlocks.LEAVES.getBlockInstance()).getIcon(PinkTreeTypes.TREE.getBaseLeaf().getItemDamage(), fancy);
		}

		@Override
		public int getGermlingColour(EnumGermlingType type, int renderPass) {
			return this.getIconColour(renderPass);
		}

		@Override
		public String getDescription() {
			// TODO Auto-generated method stub
			return "";
		}

		@Override
		public int getIconColour(int renderPass) {
			return Satisforestry.pinkforest.getBiomeFoliageColor(0, 114, 0);
		}

		@Override
		public boolean isDominant() {
			return false;
		}

		@Override
		public Yield getYield() {
			return Yield.LOWER;
		}

		@Override
		public Heights getHeight() {
			return Heights.SMALL;
		}

		@Override
		public int getGirth() {
			return 1;
		}

		@Override
		public Sappiness getSappiness() {
			return Sappiness.LOW;
		}

		@Override
		public Maturation getMaturation() {
			return Maturation.FASTER;
		}

		@Override
		public Saplings getSaplingRate() {
			return Saplings.HIGHEST;
		}

		@Override
		public Territory getTerritorySize() {
			return Territory.DEFAULT;
		}

		@Override
		protected BlockKey getLogBlock(ITreeGenome genes, World world, int x, int y, int z, Random rand, ITreeGenData data) {
			return BlockKey.fromItem(PinkTreeTypes.TREE.getBaseLog());
		}

		@Override
		protected boolean generate(World world, int x, int y, int z, Random rand, ITreeGenData data) {
			world.setBlock(x, y, z, Blocks.air);-
			PinkTreeGeneratorBase gen = PinkTreeTypes.TREE.constructTreeGenerator();
			gen.allowSlugs = false;
			gen.isSaplingGrowth = true;
			return gen.generate(world, rand, x, y, z);
		}

	}

	private static class PaleberryFruit implements IAlleleFruit {

		private final IFruitProvider fruit;

		private PaleberryFruit() {
			fruit = new BerryProvider();
			AlleleManager.alleleRegistry.registerAllele(this, EnumTreeChromosome.FRUITS);
		}

		@Override
		public String getUID() {
			return "fruit.paleberry";
		}

		@Override
		public boolean isDominant() {
			return false;
		}

		@Override
		public String getName() {
			return "Paleberries";
		}

		@Override
		public String getUnlocalizedName() {
			return this.getName();//"chromaberry."+color.name().toLowerCase(Locale.ENGLISH);
		}

		@Override
		public IFruitProvider getProvider() {
			return fruit;
		}

	}

	private static class BerryProvider implements IFruitProvider, NoLocaleDescriptionFruit {

		private BerryProvider() {

		}

		@Override
		public IFruitFamily getFamily() {
			return paleberryFamily;
		}

		@Override
		public int getColour(ITreeGenome genome, IBlockAccess world, int x, int y, int z, int ripeningTime) {
			float f = ripeningTime/(float)this.getRipeningPeriod();
			int c = 0xff4a22;
			if (f <= 0.5)
				return 0xffffff;
			else if (f >= 1)
				return c;
			return ReikaColorAPI.mixColors(c, 0xffffff, (f-0.5F)/0.5F);
		}

		@Override
		public boolean markAsFruitLeaf(ITreeGenome genome, World world, int x, int y, int z) {
			return genome != null && world != null && world.rand.nextFloat() < genome.getYield();
		}

		@Override
		public int getRipeningPeriod() {
			return 12;
		}

		@Override
		public ItemStack[] getProducts() {
			return new ItemStack[] {new ItemStack(Satisforestry.paleberry, 1, 1)};
		}

		@Override
		public ItemStack[] getSpecialty() {
			return new ItemStack[] {new ItemStack(Satisforestry.paleberry)};
		}

		@Override
		public ItemStack[] getFruits(ITreeGenome genome, World world, int x, int y, int z, int ripeningTime) {
			float f = ripeningTime/(float)this.getRipeningPeriod();
			if (world.rand.nextFloat() < 0.2*genome.getYield())
				return f < 0.75 ? new ItemStack[0] : this.getSpecialty();
				else
					return f < 0.95 ? new ItemStack[0] : this.getProducts();
		}

		/** This is in fact a locale key, and is automatically prepended with "for." */
		@Override
		public String getDescription() {
			return this.getDirectDescription();
		}

		@Override
		public String getDirectDescription() {
			return "Paleberries";
		}

		@Override
		public short getIconIndex(ITreeGenome genome, IBlockAccess world, int x, int y, int z, int ripeningTime, boolean fancy) {
			return 1000;
		}

		@Override
		public boolean requiresFruitBlocks() {
			return false;
		}

		@Override
		public boolean trySpawnFruitBlock(ITreeGenome genome, World world, int x, int y, int z) {
			return false;
		}

		@Override
		public void registerIcons(IIconRegister register) {

		}

	}

	private static abstract class SFButterfly extends BasicButterflySpecies {

		protected SFButterfly(String name, String uid, String latinName) {
			super(name, uid, latinName, "Reika", butterflyBranch);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public final String getEntityTexture() {
			return DirectResourceManager.getResource("Reika/Satisforestry/Textures/Butterflies/"+this.getTextureName().toLowerCase(Locale.ENGLISH)+".png").toString();
		}

		protected abstract String getTextureName();

		@Override
		public final EnumSet<Type> getSpawnBiomes() {
			return EnumSet.of(Type.FOREST, BiomeDictionary.getTypesForBiome(Satisforestry.pinkforest));
		}

		@Override
		public final boolean strictSpawnMatch() {
			return true;
		}

		@Override
		public final EnumTemperature getTemperature() {
			return EnumTemperature.COLD;
		}

		@Override
		public final EnumHumidity getHumidity() {
			return EnumHumidity.DAMP;
		}

		@Override
		public boolean isDominant() {
			return false;
		}

		@Override
		public final int getTemperatureTolerance() {
			return 1;
		}

		@Override
		public final int getHumidityTolerance() {
			return 0;
		}

		@Override
		public final Tolerance getHumidityToleranceDir() {
			return Tolerance.NONE;
		}

		@Override
		public final Tolerance getTemperatureToleranceDir() {
			return Tolerance.UP;
		}

	}

	private static class BasicSFButterfly extends SFButterfly {

		protected BasicSFButterfly() {
			super("Pink Morpho", "butterfly.pinkforest", "Rosea Silva");
		}

		@Override
		public float getRarity() {
			return 0.2F;
		}

		@Override
		public float getFlightDistance() {
			return 0;
		}

		@Override
		public String getDescription() {
			return "This butterfly is pretty but not particularly special.";
		}

		@Override
		protected String getTextureName() {
			return "basic";
		}

		@Override
		public int getMetabolism() {
			return 2;
		}

		@Override
		public Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Speeds getSpeed() {
			return Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Speeds.NORMAL;
		}

		@Override
		public Size getSize() {
			return Size.AVERAGE;
		}

		@Override
		public Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Fertility getFertility() {
			return Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Fertility.NORMAL;
		}

		@Override
		public Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Territory getTerritorySize() {
			return Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Territory.DEFAULT;
		}

		@Override
		public Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Life getLifespan() {
			return Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Life.SHORTENED;
		}

	}

	private static class GrassSpriteButterfly extends SFButterfly {

		protected GrassSpriteButterfly() {
			super("Grass Sprite", "butterfly.grasssprite", "Spiritus Pratum");
		}

		@Override
		public float getRarity() {
			return 0;
		}

		@Override
		public float getFlightDistance() {
			return 1;
		}

		@Override
		public String getDescription() {
			return "This butterfly spends its short life darting about, often bursting from grass or foliage at the slightest disturbances.";
		}

		@Override
		protected String getTextureName() {
			return "sprite";
		}

		@Override
		public int getMetabolism() {
			return 4;
		}

		@Override
		public Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Speeds getSpeed() {
			return Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Speeds.FASTEST;
		}

		@Override
		public Size getSize() {
			return Size.SMALLEST;
		}

		@Override
		public Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Fertility getFertility() {
			return Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Fertility.LOW;
		}

		@Override
		public Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Territory getTerritorySize() {
			return Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Territory.DEFAULT;
		}

		@Override
		public Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Life getLifespan() {
			return blinkLife;
		}

	}

	private static class PaleberryButterfly extends SFButterfly {

		protected PaleberryButterfly() {
			super("Paleberry Sucker", "butterfly.paleberry", "Pomus Pallidus");
		}

		@Override
		public float getRarity() {
			return 0.7F;
		}

		@Override
		public float getFlightDistance() {
			return 6;
		}

		@Override
		public String getDescription() {
			return "This butterfly hops from paleberry to paleberry, helping fertilize them.";
		}

		@Override
		protected String getTextureName() {
			return "paleberry";
		}

		@Override
		public int getMetabolism() {
			return 6;
		}

		@Override
		public Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Speeds getSpeed() {
			return Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Speeds.SLOWER;
		}

		@Override
		public Size getSize() {
			return Size.LARGEST;
		}

		@Override
		public Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Fertility getFertility() {
			return Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Fertility.HIGH;
		}

		@Override
		public Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Territory getTerritorySize() {
			return Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Territory.LARGE;
		}

		@Override
		public Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Life getLifespan() {
			return Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry.Life.LONG;
		}

		@Override
		/** Unimplemented */
		public IAlleleButterflyEffect getEffect() {
			return super.getEffect();
		}

	}

	public static ButterflySpecies getPaleberryButterfly() {
		return paleberryFlySpecies;
	}

	public static ButterflySpecies getSpriteButterfly() {
		return grassSpriteSpecies;
	}

	public static ButterflySpecies getPinkButterfly() {
		return basicPinkSpecies;
	}

}
