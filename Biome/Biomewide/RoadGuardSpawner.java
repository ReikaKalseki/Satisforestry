package Reika.Satisforestry.Biome.Biomewide;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom.DynamicWeight;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.BiomeFootprint;
import Reika.Satisforestry.Biome.DecoratorPinkForest;
import Reika.Satisforestry.Biome.Biomewide.PointSpawnSystem.SpawnPoint;
import Reika.Satisforestry.Biome.Biomewide.PointSpawnSystem.SpawnPointDefinition;
import Reika.Satisforestry.Entity.EntityEliteStinger;
import Reika.Satisforestry.Entity.EntitySpitter;
import Reika.Satisforestry.Entity.EntitySpitter.SpitterType;

public class RoadGuardSpawner implements SpawnPointDefinition {

	private static final double RANDOM_FACTOR = 0.5;

	private static final WeightedRandom<SpawnEntry> types = new WeightedRandom();

	RoadGuardSpawner() {
		PointSpawnSystem.registerSpawnerType(this);

		if (types.isEmpty()) {
			types.addDynamicEntry(new SpawnEntry(EntitySpider.class, 0, 0.1, 0.5, 50, 50, 0));
			types.addDynamicEntry(new SpawnEntry(EntityCaveSpider.class, 0.2, 0.4, 0.6, 40));

			types.addDynamicEntry(new SpitterEntry(SpitterType.BASIC, 0.4, 0.75, 1.1, 50));
			types.addDynamicEntry(new SpitterEntry(SpitterType.RED, 0.8, 1.15, 1.4, 30));
			types.addDynamicEntry(new SpitterEntry(SpitterType.GREEN, 1.0, 1.3, 1.5, 20));

			types.addDynamicEntry(new SpawnEntry(EntityEliteStinger.class, 1+RANDOM_FACTOR-0.2, 1+RANDOM_FACTOR-0.1, 1+RANDOM_FACTOR, 0, 5, 8));
		}

		for (double y = 0; y <= 1+RANDOM_FACTOR; y += 0.05) {
			String s = "At R="+y+": ";
			for (SpawnEntry b : types.getValues()) {
				b.calcWeight(y);
			}
			for (SpawnEntry b : types.getValues()) {
				double f = types.getProbability(b);
				s = s+" Spawn "+b+" = "+f*100+"%;";
			}
			ReikaJavaLibrary.pConsole(s);
		}
		ReikaJavaLibrary.pConsole("------------------------");
	}

	private static class SpawnEntry implements DynamicWeight {

		private final Class<? extends EntityMob> entityClass;

		private final double inflectionPoint;
		private final double minValue;
		private final double maxValue;
		private final double inflectionPointWeight;
		private final double minValueWeight;
		private final double maxValueWeight;

		private double currentWeight;

		private SpawnEntry(Class<? extends EntityMob> c, double min, double peak, double max, double ypeak) {
			this(c, min, peak, max, 0, ypeak, 0);
		}

		private SpawnEntry(Class<? extends EntityMob> c, double min, double peak, double max, double ymin, double ypeak, double ymax) {
			entityClass = c;

			minValue = min;
			maxValue = max;
			inflectionPoint = peak;
			inflectionPointWeight = ypeak;
			minValueWeight = ymin;
			maxValueWeight = ymax;
		}

		@Override
		public double getWeight() {
			return currentWeight;
		}

		private void calcWeight(double f) {
			if (f <= minValue)
				currentWeight = minValueWeight;
			if (f >= maxValue)
				currentWeight = maxValueWeight;
			if (f == inflectionPoint)
				currentWeight = inflectionPointWeight;
			if (f < inflectionPoint)
				currentWeight = ReikaMathLibrary.linterpolate(f, minValue, inflectionPoint, minValueWeight, inflectionPointWeight);
			else if (f > inflectionPoint)
				currentWeight = ReikaMathLibrary.linterpolate(f, inflectionPoint, maxValue, inflectionPointWeight, maxValueWeight);
		}

		protected void createEntity(EntityMob e) {

		}

		@Override
		public String toString() {
			return entityClass.getSimpleName();
		}

		public int getEntityCount(double f) {
			return (int)Math.round(ReikaMathLibrary.linterpolate(f, minValue, maxValue, this.getMinCount(), this.getMaxCount()));
		}

		protected int getMinCount() {
			return 2;
		}

		protected int getMaxCount() {
			return 5;
		}

		public double getSpawnRange() {
			return 8;
		}
	}

	private static class SpitterEntry extends SpawnEntry {

		private final SpitterType type;

		private SpitterEntry(SpitterType t, double min, double peak, double max, double ypeak) {
			this(t, min, peak, max, 0, ypeak, 0);
		}

		private SpitterEntry(SpitterType t, double min, double peak, double max, double ymin, double ypeak, double ymax) {
			super(EntitySpitter.class, min, peak, max, ymin, ypeak, ymax);
			type = t;
		}

		@Override
		protected void createEntity(EntityMob e) {
			((EntitySpitter)e).setSpitterType(type);
		}

		@Override
		public String toString() {
			return super.toString()+" ("+type+")";
		}

		@Override
		public int getEntityCount(double f) {
			return type.isAlpha() ? 1 : super.getEntityCount(f);
		}

		@Override
		protected int getMinCount() {
			return 1;
		}

		@Override
		protected int getMaxCount() {
			return 3;
		}

		@Override
		public double getSpawnRange() {
			return type.isAlpha() ? 16 : 12;
		}

	}

	public Collection<SpawnPoint> createSpawnPoints(World world, BiomeFootprint bf, Random rand) {
		HashSet<SpawnPoint> ret = new HashSet();
		ArrayList<Coordinate> blocks = new ArrayList(bf.getCoords());
		int n = Math.round(bf.getArea()/4500F);
		while (ret.size() < n && !blocks.isEmpty()) {
			int idx = rand.nextInt(blocks.size());
			Coordinate c = blocks.remove(idx);
			c = c.setY(DecoratorPinkForest.getTrueTopAt(world, c.xCoord, c.zCoord)+1);
			if (this.isValidSpawnArea(world, c)) {
				ret.add(new RoadGuardSpawnPoint(new WorldLocation(world, c), (float)ReikaRandomHelper.getRandomPlusMinus(0, RANDOM_FACTOR)));
			}
		}
		return ret;
	}

	private boolean isValidSpawnArea(World world, Coordinate c) {
		if (!Satisforestry.pinkforest.isRoad(world, c.xCoord, c.zCoord))
			return false;
		for (int i = -1; i <= 1; i++) {
			for (int k = -1; k <= 1; k++) {
				Coordinate c2 = c.offset(i, 0, k);
				Coordinate c2b = c2.offset(0, -1, 0);
				Coordinate c2a = c2.offset(0, 1, 0);
				if (!c2.softBlock(world) || !c2a.softBlock(world))
					return false;
				if (!DecoratorPinkForest.isTerrain(world, c2b.xCoord, c2b.yCoord, c2b.zCoord))
					return false;
			}
		}
		return true;
	}

	public static class RoadGuardSpawnPoint extends SpawnPoint {

		public final float roadValue;
		private float dangerOffset;

		private SpitterType spitter;

		private SpawnEntry spawnType;

		private RoadGuardSpawnPoint(WorldLocation loc, float f) {
			this(loc);
			dangerOffset = f;
		}

		private RoadGuardSpawnPoint(WorldLocation loc) {
			super(loc);
			roadValue = (float)Satisforestry.pinkforest.getRoadFactor(loc.getWorld(), loc.xCoord, loc.zCoord);
		}

		@Override
		protected EntityLiving getSpawn(World world, int cx, int cy, int cz, Random rand) {
			this.setSpawnData(rand);
			EntityLiving e = this.getRandomPlacedEntity(4, world, cx, cy, cz);
			if (e instanceof EntitySpitter) {
				((EntitySpitter)e).setSpitterType(spitter);
			}
			e.setLocationAndAngles(e.posX, cy+1, e.posZ, 0, 0);
			return e;
		}

		private void setSpawnData(Random rand) {
			if (spawnType == null) {
				double f = roadValue+dangerOffset;
				for (SpawnEntry b : types.getValues()) {
					b.calcWeight(f);
				}
				spawnType = types.getRandomEntry();
				this.setSpawnParameters(spawnType.entityClass, spawnType.getEntityCount(f), spawnType.getSpawnRange());
			}
		}

		@Override
		public void writeToTag(NBTTagCompound NBT) {
			super.writeToTag(NBT);

			NBT.setFloat("bias", dangerOffset);
		}

		@Override
		public void readFromTag(NBTTagCompound NBT) {
			super.readFromTag(NBT);

			dangerOffset = NBT.getFloat("bias");
			spawnType = null;
		}

	}

	@Override
	public SpawnPoint construct(WorldLocation loc) {
		return new RoadGuardSpawnPoint(loc);
	}

	@Override
	public String getID() {
		return "roadGuard";
	}

	@Override
	public Class<? extends SpawnPoint> getSpawnerClass() {
		return RoadGuardSpawnPoint.class;
	}

}
