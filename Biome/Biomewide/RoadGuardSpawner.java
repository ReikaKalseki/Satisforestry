package Reika.Satisforestry.Biome.Biomewide;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.BiomeFootprint;
import Reika.Satisforestry.Biome.DecoratorPinkForest;
import Reika.Satisforestry.Biome.Biomewide.PointSpawnSystem.SpawnPoint;

public class RoadGuardSpawner {

	RoadGuardSpawner() {
		PointSpawnSystem.registerSpawnerType("roadGuard", RoadGuardSpawnPoint.class);
	}

	public Collection<SpawnPoint> createSpawnPoints(World world, BiomeFootprint bf, Random rand) {
		HashSet<SpawnPoint> ret = new HashSet();
		ArrayList<Coordinate> blocks = new ArrayList(bf.getCoords());
		int n = Math.round(bf.getArea()/9000F);
		while (ret.size() < n && !blocks.isEmpty()) {
			int idx = rand.nextInt(blocks.size());
			Coordinate c = blocks.remove(idx);
			c = c.setY(DecoratorPinkForest.getTrueTopAt(world, c.xCoord, c.zCoord)+1);
			if (this.isValidSpawnArea(world, c)) {
				ret.add(new RoadGuardSpawnPoint(world, c));
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

		private RoadGuardSpawnPoint(World world, Coordinate c) {
			super(world, c);
			this.setSpawnParameters(EntitySpider.class, 2, 8);
		}

		@Override
		protected EntityLiving getSpawn(World world, int cx, int cy, int cz) {
			EntityLiving e = this.getRandomPlacedEntity(5, world, cx, cy, cz);
			e.setLocationAndAngles(e.posX, cy+1, e.posZ, 0, 0);
			return e;
		}

	}

}
