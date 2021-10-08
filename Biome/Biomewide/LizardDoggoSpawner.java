package Reika.Satisforestry.Biome.Biomewide;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.Satisforestry.Biome.BiomeFootprint;
import Reika.Satisforestry.Biome.DecoratorPinkForest;
import Reika.Satisforestry.Biome.Biomewide.PointSpawnSystem.SpawnPoint;
import Reika.Satisforestry.Biome.Biomewide.PointSpawnSystem.SpawnPointDefinition;
import Reika.Satisforestry.Entity.EntityLizardDoggo;

public class LizardDoggoSpawner implements SpawnPointDefinition {

	LizardDoggoSpawner() {
		PointSpawnSystem.registerSpawnerType(this);
	}

	public Collection<SpawnPoint> createDoggoSpawnPoints(World world, BiomeFootprint bf, Random rand) {
		HashSet<SpawnPoint> ret = new HashSet();
		ArrayList<Coordinate> blocks = new ArrayList(bf.getCoords());
		int n = MathHelper.clamp_int(Math.round(bf.getArea()/24000F), 1, 6);
		for (int i = 0; i < n && !blocks.isEmpty(); i++) {
			int idx = rand.nextInt(blocks.size());
			Coordinate c = blocks.remove(idx);
			c = c.setY(DecoratorPinkForest.getTrueTopAt(world, c.xCoord, c.zCoord)+1);
			if (this.isValidDoggoSpawnArea(world, c)) {
				boolean flag = true;
				for (SpawnPoint has : ret) {
					if (has.getLocation().getTaxicabDistanceTo(c.xCoord, c.yCoord, c.zCoord) <= 64) {
						flag = false;
						break;
					}
				}
				if (flag)
					ret.add(new LizardDoggoSpawnPoint(new WorldLocation(world, c)));
			}
		}
		return ret;
	}

	private boolean isValidDoggoSpawnArea(World world, Coordinate c) {
		if (!c.isEmpty(world))
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

	public static class LizardDoggoSpawnPoint extends SpawnPoint {

		private LizardDoggoSpawnPoint(WorldLocation loc) {
			super(loc);
			this.setSpawnParameters(EntityLizardDoggo.class, 1, 18.5);
		}

		@Override
		protected EntityLiving getSpawn(World world, int cx, int cy, int cz, Random rand) {
			EntityLiving e = this.getRandomPlacedEntity(7.5, world, cx, cy, cz);
			e.setLocationAndAngles(e.posX, cy+1, e.posZ, 0, 0);
			return e;
		}

		@Override
		protected boolean canBeCleared() {
			return false;
		}

		@Override
		public double getResetRadius() {
			return -1;
		}

		@Override
		public boolean clearNonPlayerDrops() {
			return false;
		}

	}

	@Override
	public SpawnPoint construct(WorldLocation loc) {
		return new LizardDoggoSpawnPoint(loc);
	}

	@Override
	public String getID() {
		return "doggo";
	}

	@Override
	public Class<? extends SpawnPoint> getSpawnerClass() {
		return LizardDoggoSpawnPoint.class;
	}

}
