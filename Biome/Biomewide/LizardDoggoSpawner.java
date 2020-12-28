package Reika.Satisforestry.Biome.Biomewide;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.Satisforestry.Biome.BiomeFootprint;
import Reika.Satisforestry.Biome.DecoratorPinkForest;

public class LizardDoggoSpawner {

	public static final LizardDoggoSpawner instance = new LizardDoggoSpawner();

	private LizardDoggoSpawner() {

	}

	public Collection<WorldLocation> createDoggoSpawnPoints(World world, BiomeFootprint bf, Random rand) {
		HashSet<WorldLocation> ret = new HashSet();
		ArrayList<Coordinate> blocks = new ArrayList(bf.getCoords());
		int n = MathHelper.clamp_int(Math.round(bf.getArea()/24000F), 1, 6);
		for (int i = 0; i < n && !blocks.isEmpty(); i++) {
			int idx = rand.nextInt(blocks.size());
			Coordinate c = blocks.remove(idx);
			if (this.isValidDoggoSpawnArea(world, c)) {
				boolean flag = true;
				for (WorldLocation has : ret) {
					if (c.getTaxicabDistanceTo(has.xCoord, has.yCoord, has.zCoord) <= 64) {
						flag = false;
						break;
					}
				}
				if (flag)
					ret.add(new WorldLocation(world, c));
			}
		}
		return ret;
	}

	private boolean isValidDoggoSpawnArea(World world, Coordinate c) {
		c = c.setY(DecoratorPinkForest.getTrueTopAt(world, c.xCoord, c.zCoord)+1);
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

}
