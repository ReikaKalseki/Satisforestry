package Reika.Satisforestry.Biome;

import java.util.Collection;
import java.util.Random;

import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySpider;

import Reika.DragonAPI.Instantiable.Data.Maps.MultiMap;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;

@Deprecated
public class TieredMobSpawns {

	public static final TieredMobSpawns instance = new TieredMobSpawns();

	private final MultiMap<Integer, SpawnEntry> data = new MultiMap();

	private TieredMobSpawns() {
		this.addEntry(0, 0.25, EntityCaveSpider.class);
		this.addEntry(0, 0.75, EntitySpider.class);

		this.addEntry(1, 1, EntityCaveSpider.class);
		this.addEntry(1, 1, EntitySpider.class, 2);

		this.addEntry(1, 1, EntitySpider.class, 2);
	}

	private void addEntry(int tier, double wt, Class<? extends EntityMob> c) {
		this.addEntry(tier, wt, c, 1);
	}

	private void addEntry(int tier, double wt, Class<? extends EntityMob> c, float f) {
		data.addValue(tier, new SpawnEntry(c, f));
	}

	public SpawnEntry getRandomSpawn(int tier, Random rand) {
		Collection<SpawnEntry> c = data.get(tier);
		return c == null || c.isEmpty() ? null : ReikaJavaLibrary.getRandomCollectionEntry(rand, c);
	}

	public static class SpawnEntry {

		public final Class<? extends EntityMob> entityClass;
		public final float healthFactor;
		public final float damageFactor;

		private SpawnEntry(Class<? extends EntityMob> c) {
			this(c, 1);
		}

		private SpawnEntry(Class<? extends EntityMob> c, float f) {
			this(c, f, 1);
		}

		private SpawnEntry(Class<? extends EntityMob> c, float f, float d) {
			entityClass = c;
			healthFactor = f;
			damageFactor = d;
		}

	}

}
