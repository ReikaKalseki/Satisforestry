package Reika.Satisforestry.Biome.Biomewide;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Data.Immutable.DecimalPosition;
import Reika.DragonAPI.Instantiable.Effects.LightningBolt;
import Reika.DragonAPI.Instantiable.Math.Spline;
import Reika.DragonAPI.Instantiable.Math.Spline.BasicSplinePoint;
import Reika.DragonAPI.Instantiable.Math.Spline.SplineType;
import Reika.DragonAPI.Instantiable.Math.Noise.SimplexNoiseGenerator;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.Satisforestry.SFBlocks;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.DecoratorPinkForest;
import Reika.Satisforestry.Biome.DecoratorPinkForest.OreClusterType;
import Reika.Satisforestry.Biome.DecoratorPinkForest.OreSpawnLocation;
import Reika.Satisforestry.Blocks.BlockCaveSpawner.TileCaveSpawner;
import Reika.Satisforestry.Blocks.BlockDecoration.DecorationType;
import Reika.Satisforestry.Blocks.BlockGasEmitter.TileGasVent;
import Reika.Satisforestry.Blocks.BlockPinkGrass.GrassTypes;
import Reika.Satisforestry.Blocks.BlockResourceNode.TileResourceNode;

public class UraniumCave {

	public static final UraniumCave instance = new UraniumCave();

	private final WeightedRandom<SpawnListEntry> caveSpawns = new WeightedRandom();

	private UraniumCave() {
		List<SpawnListEntry> li = Satisforestry.pinkforest.getSpawnableList(EnumCreatureType.monster);
		for (SpawnListEntry e : li) {
			if (EntitySpider.class.isAssignableFrom(e.entityClass)) {
				caveSpawns.addEntry(e, e.itemWeight);
			}
		}
	}

	public CentralCave generate(World world, Random rand, int x, int z, Collection<Coordinate> rivers, CachedCave cache) {
		caveSpawns.setRNG(rand);
		OreSpawnLocation.setRNG(rand);

		int top = DecoratorPinkForest.getTrueTopAt(world, x, z);
		CentralCave cc = cache != null ? cache.reconstruct() : new CentralCave(x, ReikaRandomHelper.getRandomBetween(40, Math.min(72, top-50), rand), z);

		Satisforestry.logger.log("Generating biome cave @ "+cc.center+", from rivers "+rivers+" and cache "+cache);

		cc.calculate(world, rand);

		Collection<Tunnel> tunnels = new ArrayList();
		Vec3 avg = Vec3.createVectorHelper(0, 0, 0);
		double d = cc.outerCircleRadius*ReikaRandomHelper.getRandomBetween(1.35, 1.75, rand);
		if (cache != null) {
			for (Entry<Coordinate, Double> e : cache.tunnels.entrySet()) {
				Tunnel add = new Tunnel(cc, e.getKey(), d, e.getValue());
				tunnels.add(add);
				avg.xCoord += add.startingDX;
				avg.zCoord += add.startingDZ;
			}
		}
		else {
			for (Coordinate c : rivers) {
				if (tunnels.size() >= 5)
					continue;
				double dx = c.xCoord-cc.center.xCoord;
				double dz = c.zCoord-cc.center.zCoord;
				double dd = ReikaMathLibrary.py3d(dx, 0, dz);
				double dr = 9;
				Coordinate endpoint = c.offset((int)(dx/dd*dr), 0, (int)(dz/dd*dr));
				double rootAngle = (Math.toDegrees(Math.atan2(endpoint.yCoord-cc.center.yCoord, endpoint.xCoord-cc.center.xCoord))%360+360)%360;
				double nearestLow = -99999;
				double nearestHigh = 99999;
				boolean flag = false;
				for (Tunnel t : tunnels) {
					double da = (Math.abs(t.startingAngle-rootAngle)%360+360)%360;
					if (rootAngle-da > nearestLow)
						nearestLow = t.startingAngle;
					if (rootAngle+da < nearestHigh)
						nearestHigh = t.startingAngle;
					flag |= da < 30;
				}
				if (flag) {
					if (nearestHigh-nearestLow < 45)
						continue;
					rootAngle = (nearestHigh+nearestLow)/2D;
				}
				Tunnel add = new Tunnel(cc, endpoint, d, rootAngle);
				tunnels.add(add);
				avg.xCoord += add.startingDX;
				avg.zCoord += add.startingDZ;
			}
		}
		//avg.xCoord /= tunnels.size();
		//avg.zCoord /= tunnels.size();
		avg.normalize();

		HashSet<Coordinate> carveSet = new HashSet();
		carveSet.addAll(cc.carve.keySet());

		for (Tunnel t : tunnels) {
			t.calculate(world, rand);
			carveSet.addAll(t.carve.keySet());
		}

		double dr = cc.outerCircleRadius+5;
		ResourceNodeRoom rm = cache != null ? cache.reconstructNode(cache.nodeTile) : new ResourceNodeRoom(cc.center.offset(-avg.xCoord*dr, 0, -avg.zCoord*dr));
		rm.calculate(world, rand);

		carveSet.addAll(rm.carve.keySet());

		Tunnel path = new Tunnel(cc, new Coordinate(rm.center), cc.outerCircleRadius, Math.toDegrees(Math.atan2(-avg.xCoord, -avg.zCoord)), 2);
		path.targetY = path.endpoint.yCoord;
		path.isToBiomeEdge = false;
		path.calculate(world, rand);
		cc.nodeRoom = rm;

		carveSet.addAll(path.carve.keySet());
		tunnels.add(path);

		cc.generate(world, rand);

		for (Tunnel t : tunnels) {
			t.generate(world, rand);
		}

		rm.generate(world, rand);

		HashSet<Coordinate> flatFloor = new HashSet(cc.footprint.keySet());
		HashSet<Coordinate> pits = new HashSet();
		HashMap<Coordinate, Integer> remainingFloor = new HashMap();
		SimplexNoiseGenerator floorLevel = (SimplexNoiseGenerator)new SimplexNoiseGenerator(rand.nextLong()).setFrequency(1/15D);
		SimplexNoiseGenerator holes = (SimplexNoiseGenerator)new SimplexNoiseGenerator(rand.nextLong()).setFrequency(1/6D);

		for (Entry<Coordinate, Integer> e : cc.footprint.entrySet()) {
			Coordinate c = e.getKey();
			int yat = e.getValue()-1;
			int h = (int)Math.round(ReikaMathLibrary.normalizeToBounds(floorLevel.getValue(c.xCoord, c.zCoord), 4, 7)); //was 3,6
			int floor = cc.lowestFloor-1-h;
			boolean hole = Math.abs(holes.getValue(c.xCoord, c.zCoord)) >= 0.4;//rand.nextInt(5) == 0;
			for (int i = hole ? 0 : 1; i < h; i++) {
				Coordinate c2 = c.setY(yat-i);
				c2.setBlock(world, Blocks.air);
				carveSet.add(c2);
			}
			if (hole) {
				pits.add(c);
				flatFloor.remove(c);
				flatFloor.remove(c.offset(1, 0, 0));
				flatFloor.remove(c.offset(-1, 0, 0));
				flatFloor.remove(c.offset(0, 0, 1));
				flatFloor.remove(c.offset(0, 0, -1));
			}
			else {
				remainingFloor.put(c, e.getValue());
			}
			if (rand.nextInt(36) == 0) {
				world.setBlock(c.xCoord, floor, c.zCoord, SFBlocks.GASEMITTER.getBlockInstance(), 1, 2);
				TileGasVent te = (TileGasVent)world.getTileEntity(c.xCoord, floor, c.zCoord);
				te.activeRadius = 5.5;
				te.activeHeight = 3.5;
				te.yOffset = 0;
			}
		}

		this.generateCasing(world, rand, carveSet);

		rm.generateResourceNode(world, rand);

		this.generateDecorations(world, rand, carveSet, remainingFloor, pits, cc);

		for (int i = 0; i < 15; i++) {
			Coordinate c = ReikaJavaLibrary.getRandomCollectionEntry(rand, flatFloor);
			c = c.setY(remainingFloor.get(c));
			while (c.offset(0, -1, 0).softBlock(world))
				c = c.offset(0, -1, 0);
			if (c.yCoord+1 < cc.footprint.get(c.to2D()))
				continue;
			DecoratorPinkForest.generateOreClumpAt(world, c.xCoord, c.yCoord, c.zCoord, rand, OreSpawnLocation.CAVE_MAIN_RING, 4, pits);

			TileCaveSpawner lgc = this.generateSpawnerAt(world, c.xCoord, c.yCoord-1, c.zCoord, rand);
			lgc.activeRadius = 10;//8;
			lgc.spawnRadius = 8;
			lgc.respawnTime = 12;
			lgc.mobLimit = 6;
		}

		for (Entry<DecimalPosition, Integer> e : rm.disks.entrySet()) {
			DecimalPosition p = e.getKey();
			int px = MathHelper.floor_double(p.xCoord);
			int py = MathHelper.floor_double(p.yCoord);
			int pz = MathHelper.floor_double(p.zCoord);
			DecoratorPinkForest.generateOreClumpAt(world, px, py-1, pz, rand, OreSpawnLocation.CAVE_RESOURCE_ROOM, 3, rm.nodeClearArea);
		}
		OreClusterType spike = OreSpawnLocation.CAVE_RESOURCE_NODE.getRandomOreSpawn();
		if (spike != null) {
			rm.generateOreSpike(world, rand, spike.oreBlock);
		}

		for (int i = 0; i < 6; i++) {
			Coordinate c = ReikaJavaLibrary.getRandomCollectionEntry(rand, rm.adjacentFloor);
			while (c.offset(0, -1, 0).softBlock(world) || c.offset(0, -1, 0).getBlock(world) == SFBlocks.CAVESHIELD.getBlockInstance())
				c = c.offset(0, -1, 0);
			TileCaveSpawner lgc = this.generateSpawnerAt(world, c.xCoord, c.yCoord, c.zCoord, rand);
			lgc.activeRadius = 6;
			lgc.spawnRadius = 5;
			lgc.respawnTime = 4;
			lgc.mobLimit = 8;

			/*
			for (Coordinate c2 : c.getAdjacentCoordinates()) {
				if (!carveSet.contains(c2)) {
					c2.setBlock(world, SFBlocks.CAVESHIELD.getBlockInstance());
				}
			}*/
		}

		return cc;
	}

	private void generateDecorations(World world, Random rand, HashSet<Coordinate> carveSet, HashMap<Coordinate, Integer> floor, HashSet<Coordinate> pits, CentralCave cc) {
		for (int i = 0; i < 40; i++) {
			Coordinate c = ReikaJavaLibrary.getRandomCollectionEntry(rand, floor.keySet());
			if (pits.contains(c))
				continue;
			int y = floor.get(c);
			if (!c.setY(y).isEmpty(world))
				continue;
			boolean flag = true;
			for (int i2 = 0; i2 <= 2; i2++) {
				if (!carveSet.contains(c.offset(0, y+i2, 0))) {
					flag = false;
					break;
				}
				if (!carveSet.contains(c.offset(1, y+i2, 0)) || !carveSet.contains(c.offset(-1, y+i2, 0))) {
					flag = false;
					break;
				}
				if (!carveSet.contains(c.offset(0, y+i2, 1)) || !carveSet.contains(c.offset(0, y+i2, -1))) {
					flag = false;
					break;
				}
			}
			if (!flag)
				continue;
			world.setBlock(c.xCoord, y, c.zCoord, SFBlocks.DECORATION.getBlockInstance(), DecorationType.STALAGMITE.ordinal(), 2);
		}

		for (int i = 0; i < 20; i++) {
			Coordinate c = ReikaJavaLibrary.getRandomCollectionEntry(rand, floor.keySet());
			if (pits.contains(c))
				continue;
			int y = floor.get(c);
			if (!c.setY(y).isEmpty(world))
				continue;
			int h = ReikaRandomHelper.getRandomBetween(1, 3, rand);
			this.generateMushroom(world, c.xCoord, y, c.zCoord, h, carveSet);

			for (int i2 = 0; i2 < 3 ; i2++) {
				Coordinate c2 = c.offset(ReikaRandomHelper.getRandomPlusMinus(0, 2, rand), 0, ReikaRandomHelper.getRandomPlusMinus(0, 2, rand));
				if (!floor.containsKey(c2))
					continue;
				y = floor.get(c2);
				if (!c2.setY(y).isEmpty(world))
					continue;
				h = ReikaRandomHelper.getRandomBetween(1, 3, rand);
				this.generateMushroom(world, c2.xCoord, y, c2.zCoord, h, carveSet);
			}
		}

		for (int i = 0; i < 120; i++) {
			Coordinate c = ReikaJavaLibrary.getRandomCollectionEntry(rand, cc.carve.keySet());
			while (carveSet.contains(c.offset(0, 1, 0))) {
				c = c.offset(0, 1, 0);
			}
			if (!c.isEmpty(world))
				continue;
			if (c.offset(0, 1, 0).softBlock(world))
				continue;
			if (rand.nextInt(3) > 0) {
				c.setBlock(world, SFBlocks.DECORATION.getBlockInstance(), DecorationType.STALACTITE.ordinal());
			}
			else {
				int l = Math.min(5, c.yCoord-cc.footprint.get(c.to2D())/2);
				this.generateVine(world, c.xCoord, c.yCoord, c.zCoord, l, carveSet);
			}
		}

		for (int i = 0; i < 20; i++) {
			Coordinate root = ReikaJavaLibrary.getRandomCollectionEntry(rand, floor.keySet());
			HashSet<Coordinate> place = new HashSet();
			place.add(root);
			int n = ReikaRandomHelper.getRandomBetween(0, 3, rand);
			for (int i2 = 0; i2 < n; i2++) {
				HashSet<Coordinate> toAdd = new HashSet();
				for (Coordinate c : place) {
					for (Coordinate c2 : c.getAdjacentCoordinates()) {
						if (c2.yCoord == c.yCoord && floor.containsKey(c2)) {
							toAdd.add(c2);
						}
					}
				}
				place.addAll(toAdd);
			}
			for (Coordinate c : place) {
				int y = floor.get(c);
				if (!c.setY(y).isEmpty(world))
					continue;
				world.setBlock(c.xCoord, y, c.zCoord, SFBlocks.GRASS.getBlockInstance(), GrassTypes.STALKS.ordinal(), 2);
				if (rand.nextInt(4) == 0 && world.getBlock(c.xCoord, y+1, c.zCoord).isAir(world, c.xCoord, y+1, c.zCoord))
					world.setBlock(c.xCoord, y+1, c.zCoord, SFBlocks.GRASS.getBlockInstance(), GrassTypes.STALKS.ordinal(), 2);
			}
		}

		for (Tunnel t : cc.tunnels) {
			for (int i = 0; i < 16; i++) {
				Coordinate c = ReikaJavaLibrary.getRandomCollectionEntry(rand, t.carve.keySet());
				while (carveSet.contains(c.offset(0, -1, 0))) {
					c = c.offset(0, -1, 0);
				}
				if (!c.isEmpty(world))
					continue;
				int h = ReikaRandomHelper.getRandomBetween(0, 2, rand);
				this.generateMushroom(world, c.xCoord, c.yCoord, c.zCoord, h, carveSet);

				for (int i2 = 0; i2 <= 5; i2++) {
					Coordinate c2 = c.offset(ReikaRandomHelper.getRandomPlusMinus(0, 2, rand), 0, ReikaRandomHelper.getRandomPlusMinus(0, 2, rand));
					while (carveSet.contains(c2.offset(0, -1, 0))) {
						c2 = c2.offset(0, -1, 0);
					}
					if (!carveSet.contains(c2))
						continue;
					if (!c2.isEmpty(world))
						continue;
					if (DecoratorPinkForest.getTrueTopAt(world, c2.xCoord, c2.zCoord) <= c2.yCoord+10)
						continue;
					int n = rand.nextInt(10);
					switch(n) {
						case 0:
						case 1:
							c2.setBlock(world, SFBlocks.DECORATION.getBlockInstance(), DecorationType.STALAGMITE.ordinal());
							if (c2.offset(0, 1, 0).getBlock(world) == SFBlocks.GRASS.getBlockInstance())
								ReikaJavaLibrary.pConsole("Placed stagmite into mushroom!");
							break;
						case 2:
						case 3:
						case 4:
						case 5:
							h = ReikaRandomHelper.getRandomBetween(0, 2, rand);
							if (c2.getBlock(world) == SFBlocks.DECORATION.getBlockInstance())
								ReikaJavaLibrary.pConsole("Placed mushroom into stagmite!");
							this.generateMushroom(world, c2.xCoord, c2.yCoord, c2.zCoord, h, carveSet);
							break;
						case 6:
						case 7:
						case 8:
						case 9:
							c2.setBlock(world, SFBlocks.GRASS.getBlockInstance(), GrassTypes.STALKS.ordinal());
							if (rand.nextInt(5) == 0 && c2.offset(0, 1, 0).isEmpty(world))
								c2.offset(0, 1, 0).setBlock(world, SFBlocks.GRASS.getBlockInstance(), GrassTypes.STALKS.ordinal());
							break;
					}
				}
			}

			for (int i = 0; i < 80; i++) {
				Coordinate c = ReikaJavaLibrary.getRandomCollectionEntry(rand, t.carve.keySet());
				while (carveSet.contains(c.offset(0, 1, 0))) {
					c = c.offset(0, 1, 0);
				}
				if (!c.isEmpty(world))
					continue;
				if (c.offset(0, 1, 0).softBlock(world))
					continue;
				if (rand.nextInt(5) >= 1) {
					c.setBlock(world, SFBlocks.DECORATION.getBlockInstance(), DecorationType.STALACTITE.ordinal());
				}
				else {
					this.generateVine(world, c.xCoord, c.yCoord, c.zCoord, rand.nextBoolean() ? 3 : 2, carveSet);
				}
			}
		}

		for (int i = 0; i < 10; i++) {
			Coordinate c = ReikaJavaLibrary.getRandomCollectionEntry(rand, cc.nodeRoom.carve.keySet());
			while (carveSet.contains(c.offset(0, -1, 0))) {
				c = c.offset(0, -1, 0);
			}
			if (!c.isEmpty(world))
				continue;
			if (c.getTaxicabDistanceTo(cc.nodeRoom.resourceNode) <= 2)
				continue;
			int h = ReikaRandomHelper.getRandomBetween(0, 2, rand);
			this.generateMushroom(world, c.xCoord, c.yCoord, c.zCoord, h, carveSet);

			for (int i2 = 0; i2 <= 5; i2++) {
				Coordinate c2 = c.offset(ReikaRandomHelper.getRandomPlusMinus(0, 2, rand), 0, ReikaRandomHelper.getRandomPlusMinus(0, 2, rand));
				while (carveSet.contains(c2.offset(0, -1, 0))) {
					c2 = c2.offset(0, -1, 0);
				}
				if (!carveSet.contains(c2))
					continue;
				if (!c2.isEmpty(world))
					continue;
				if (c2.getTaxicabDistanceTo(cc.nodeRoom.resourceNode) <= 2)
					continue;
				if (DecoratorPinkForest.getTrueTopAt(world, c2.xCoord, c2.zCoord) <= c2.yCoord+10)
					continue;
				int n = rand.nextInt(5);
				switch(n) {
					case 0:
					case 1:
						c.setBlock(world, SFBlocks.GRASS.getBlockInstance(), GrassTypes.STALKS.ordinal());
						if (rand.nextInt(4) == 0 && c.offset(0, 1, 0).isEmpty(world))
							c.offset(0, 1, 0).setBlock(world, SFBlocks.GRASS.getBlockInstance(), GrassTypes.STALKS.ordinal());
						break;
					case 2:
						c2.setBlock(world, SFBlocks.DECORATION.getBlockInstance(), DecorationType.STALAGMITE.ordinal());
						if (c2.offset(0, 1, 0).getBlock(world) == SFBlocks.GRASS.getBlockInstance())
							ReikaJavaLibrary.pConsole("Placed stagmite into mushroom!");
						break;
					case 3:
					case 4:
						if (c2.getBlock(world) == SFBlocks.DECORATION.getBlockInstance())
							ReikaJavaLibrary.pConsole("Placed mushroom into stagmite!");
						h = ReikaRandomHelper.getRandomBetween(0, 2, rand);
						this.generateMushroom(world, c2.xCoord, c2.yCoord, c2.zCoord, h, carveSet);
						break;
				}
			}
		}

		for (int i = 0; i < 20; i++) {
			Coordinate c = ReikaJavaLibrary.getRandomCollectionEntry(rand, cc.nodeRoom.carve.keySet());
			while (carveSet.contains(c.offset(0, 1, 0))) {
				c = c.offset(0, 1, 0);
			}
			if (!c.isEmpty(world))
				continue;
			if (c.getTaxicabDistanceTo(cc.nodeRoom.resourceNode) <= 4)
				continue;
			if (c.offset(0, 1, 0).softBlock(world))
				continue;
			if (rand.nextInt(5) >= 1) {
				c.setBlock(world, SFBlocks.DECORATION.getBlockInstance(), DecorationType.STALACTITE.ordinal());
			}
			else {
				this.generateVine(world, c.xCoord, c.yCoord, c.zCoord, rand.nextBoolean() ? 3 : 2, carveSet);
			}
		}
	}

	private void generateVine(World world, int x, int y, int z, int l, HashSet<Coordinate> carveSet) {
		if (!GrassTypes.VINE.canExistAt(world, x, y, z))
			return;
		for (int k = 0; k < l; k++) {
			Coordinate c = new Coordinate(x, y-k, z);
			if (carveSet.contains(c) && c.isEmpty(world)) {
				c.setBlock(world, SFBlocks.GRASS.getBlockInstance(), GrassTypes.VINE.ordinal());
			}
			else {
				break;
			}
		}
	}

	private void generateMushroom(World world, int x, int y, int z, int h, HashSet<Coordinate> carveSet) {
		if (!GrassTypes.BLUE_MUSHROOM_STALK.canExistAt(world, x, y, z))
			return;
		for (int k = 0; k < h; k++) {
			Coordinate c = new Coordinate(x, y+k, z);
			if (carveSet.contains(c) && c.isEmpty(world)) {
				c.setBlock(world, SFBlocks.GRASS.getBlockInstance(), GrassTypes.BLUE_MUSHROOM_STALK.ordinal(), 2);
			}
			else {
				h = k-1;
				break;
			}
		}
		world.setBlock(x, y+h, z, SFBlocks.GRASS.getBlockInstance(), GrassTypes.BLUE_MUSHROOM_TOP.ordinal(), 2);
	}

	private void generateCasing(World world, Random rand, HashSet<Coordinate> carveSet) {
		HashSet<Coordinate> secondLayer = new HashSet();
		for (Coordinate c : carveSet) {
			for (Coordinate c2 : c.getAdjacentCoordinates()) {
				if (carveSet.contains(c2))
					continue;
				Block b = c2.getBlock(world);
				if (this.isSpecialCaveBlock(b))
					continue;
				if (c2.yCoord <= DecoratorPinkForest.getTrueTopAt(world, c2.xCoord, c2.zCoord)-15) {
					/*
					if (c2.softBlock(world) || b == Blocks.planks || !b.getMaterial().blocksMovement() || !b.isOpaqueCube() || b == Blocks.gravel || b == Blocks.sand || b == Blocks.dirt) {
						c2.setBlock(world, Blocks.cobblestone);
						secondLayer.add(c2);
					}
					else if (b.isReplaceableOreGen(world, c2.xCoord, c2.yCoord, c2.zCoord, Blocks.stone) || ReikaBlockHelper.isOre(b, c.getBlockMetadata(world))) {
						c2.setBlock(world, Blocks.mossy_cobblestone);
						secondLayer.add(c2);
					}
					 */
					c2.setBlock(world, SFBlocks.CAVESHIELD.getBlockInstance());
					secondLayer.add(c2);
				}
			}
		}
		for (Coordinate c : secondLayer) {
			boolean allEmpty = true;
			for (Coordinate c2 : c.getAdjacentCoordinates()) {
				if (!carveSet.contains(c2))
					allEmpty = false;
				if (carveSet.contains(c2) || secondLayer.contains(c2))
					continue;
				if (c2.softBlock(world))
					c2.setBlock(world, Blocks.stone);
			}
			if (allEmpty) {
				c.setBlock(world, Blocks.air);
			}
		}
	}

	private boolean isSpecialCaveBlock(Block b) {
		return b == SFBlocks.CAVESHIELD.getBlockInstance() || b == SFBlocks.RESOURCENODE.getBlockInstance() || b == SFBlocks.SPAWNER.getBlockInstance() || b == SFBlocks.GASEMITTER.getBlockInstance();
	}

	private TileCaveSpawner generateSpawnerAt(World world, int x, int y, int z, Random rand) {
		return this.generateSpawnerAt(world, x, y, z, rand, null);
	}

	private TileCaveSpawner generateSpawnerAt(World world, int x, int y, int z, Random rand, Class<? extends EntityMob> mob) {
		if (mob == null) {
			caveSpawns.setRNG(rand);
			SpawnListEntry e = caveSpawns.getRandomEntry();
			mob = e.entityClass;
		}
		world.setBlock(x, y, z, SFBlocks.SPAWNER.getBlockInstance());
		TileCaveSpawner te = (TileCaveSpawner)world.getTileEntity(x, y, z);
		if (te == null) {
			te = new TileCaveSpawner();
			world.setTileEntity(x, y, z, te);
		}
		te.setMobType(mob);
		return te;
	}

	public SpawnListEntry getRandomSpawn(Random rand) {
		caveSpawns.setRNG(rand);
		return caveSpawns.getRandomEntry();
	}

	private static class Tunnel extends UraniumCavePiece {

		private final Coordinate endpoint;
		private final CentralCave cave;
		private final double startingAngle;
		private final double initialRun;

		private final double startingDX;
		private final double startingDZ;
		private final double radius;
		private final DecimalPosition boltStart;

		private boolean isToBiomeEdge = true;
		private int targetY = -1;

		private Tunnel(CentralCave cc, Coordinate c, double d, double ang) {
			this(cc, c, d, ang, 3);
		}

		private Tunnel(CentralCave cc, Coordinate c, double d, double ang, double r) {
			super(cc.center);
			cave = cc;
			cave.tunnels.add(this);
			endpoint = c;
			initialRun = d;
			startingAngle = ang;
			radius = r;

			startingDX = Math.cos(Math.toRadians(startingAngle));
			startingDZ = Math.sin(Math.toRadians(startingAngle));

			boltStart = center.offset(startingDX*initialRun, 0, startingDZ*initialRun);
		}

		@Override
		protected void calculate(World world, Random rand) {
			if (targetY < 0) {
				targetY = DecoratorPinkForest.getTrueTopAt(world, endpoint.xCoord, endpoint.zCoord);
				while (world.getBlock(endpoint.xCoord, targetY+1, endpoint.zCoord) == Blocks.water) {
					targetY++;
				}
				targetY += 5;
			}
			double dyf = 0;//(targetY-boltStart.yCoord)*ReikaRandomHelper.getRandomBetween(0, 0.25, rand);
			Coordinate end = new Coordinate(endpoint.xCoord, targetY, endpoint.zCoord);
			double dd = ReikaMathLibrary.py3d(endpoint.xCoord-center.xCoord, 0, endpoint.zCoord-center.zCoord);
			int n = (int)Math.max(4, dd/16);
			LightningBolt b = new LightningBolt(new DecimalPosition(boltStart.xCoord, boltStart.yCoord+dyf, boltStart.zCoord), new DecimalPosition(end), n);
			b.setRandom(rand);
			//b.variance = 10;//15;
			b.setVariance(10, 8, 10);
			b.maximize();
			Spline path = new Spline(SplineType.CENTRIPETAL);
			if (!boltStart.equals(center))
				path.addPoint(new BasicSplinePoint(center));

			for (int i = 0; i <= b.nsteps; i++) {
				DecimalPosition pos = b.getPosition(i);
				path.addPoint(new BasicSplinePoint(pos));
			}

			List<DecimalPosition> li = path.get(24, false);
			int last = 0;
			for (int i = 0; i < li.size(); i++) {
				DecimalPosition p = li.get(i);
				int px = MathHelper.floor_double(p.xCoord);
				int pz = MathHelper.floor_double(p.zCoord);
				double w = 0;//2.5;
				double r = 2.25;
				if (this.carveAt(world, p, r, w, /*this.getAngleAt(li, i)*/0)) {
					last = i;
				}
			}
			DecimalPosition p = li.get(last);
			for (double i = -radius; i <= radius; i++) {
				for (double j = 1; j <= radius; j++) {
					for (double k = -radius; k <= radius; k++) {
						if (ReikaMathLibrary.isPointInsideEllipse(i, j, k, radius+0.5, radius*0.75, radius+0.5)) {
							int dx = MathHelper.floor_double(p.xCoord+i);
							int dy = MathHelper.floor_double(p.yCoord+j);
							int dz = MathHelper.floor_double(p.zCoord+k);
							Coordinate c = new Coordinate(dx, dy, dz);
							if (world.getBlock(dx, dy, dz) == Blocks.dirt) {
								Block above = world.getBlock(dx, dy+1, dz);
								if (above == Blocks.water || above == Blocks.flowing_water || above.isAir(world, dx, dy+1, dz) || DecoratorPinkForest.isTerrain(world, dx, dy+1, dz)) {
									world.setBlock(dx, dy, dz, Blocks.water);
								}
							}
						}
					}
				}
			}
		}

		private double getAngleAt(List<DecimalPosition> li, int i) {
			DecimalPosition pre = i == 0 ? null : li.get(i-1);
			DecimalPosition at = li.get(i);
			DecimalPosition post = i == li.size()-1 ? null : li.get(i+1);
			double ang1 = pre == null ? -1 : Math.toDegrees(Math.atan2(at.zCoord-pre.zCoord, at.xCoord-pre.xCoord));
			double ang2 = post == null ? -1 : Math.toDegrees(Math.atan2(post.zCoord-at.zCoord, post.xCoord-at.xCoord));
			if (pre == null)
				return ang2;
			else if (post == null)
				return ang1;
			return (ang1+ang2)/2D;
		}

		@Override
		protected boolean skipCarve(Coordinate c) {
			return c.to2D().getDistanceTo(cave.center.xCoord, 0, cave.center.zCoord) <= cave.innerCircleRadius+4;//cave.footprint.containsKey(c.to2D());
		}

		@Override
		public String toString() {
			return "Tunnel "+cave.center+" > "+endpoint+" @ "+targetY;
		}

		@Override
		protected void generate(World world, Random rand) {
			super.generate(world, rand);

			int n = ReikaRandomHelper.getRandomBetween(3, 6, rand);
			for (int i = 0; i < 5; i++) {
				Coordinate c = ReikaJavaLibrary.getRandomCollectionEntry(rand, carve.keySet());
				if (c.yCoord >= DecoratorPinkForest.getTrueTopAt(world, c.xCoord, c.zCoord)-15)
					continue;
				Coordinate below = c.offset(0, -1, 0);
				while (carve.containsKey(below)) {
					c = below;
					below = c.offset(0, -1, 0);
				}
				TileCaveSpawner lgc = instance.generateSpawnerAt(world, below.xCoord, below.yCoord, below.zCoord, rand, EntityCaveSpider.class);
				lgc.activeRadius = isToBiomeEdge ? 12 : 8;
				lgc.spawnRadius = 3;
				lgc.respawnTime = isToBiomeEdge ? 2 : 4;
				lgc.mobLimit = isToBiomeEdge ? 4 : 6;

				OreClusterType ore = (isToBiomeEdge ? OreSpawnLocation.CAVE_ENTRY_TUNNEL : OreSpawnLocation.CAVE_NODE_TUNNEL).getRandomOreSpawn();

				if (ore != null) {
					for (Coordinate c2 : below.getAdjacentCoordinates()) {
						if (c2.softBlock(world)) {
							c2.setBlock(world, ore.oreBlock.blockID, ore.oreBlock.metadata);
						}
					}
				}
			}
		}

	}

	static class CentralCave extends UraniumCavePiece {

		private static final int MIN_TUNNEL_WIDTH = 3;

		//private final int rmax = 40; //was 24 then 36
		//private final LobulatedCurve outer = LobulatedCurve.fromMinMaxRadii(18, rmax, 5, true); //was 16

		private final HashMap<Coordinate, Integer> footprint = new HashMap();

		private final ArrayList<Tunnel> tunnels = new ArrayList();
		private ResourceNodeRoom nodeRoom;

		private SimplexNoiseGenerator floorHeightNoise;
		private SimplexNoiseGenerator ceilingHeightNoise;

		private SimplexNoiseGenerator xOffset1;
		private SimplexNoiseGenerator zOffset1;
		private SimplexNoiseGenerator xOffset2;
		private SimplexNoiseGenerator zOffset2;

		private DecimalPosition innerCircleOffset;
		private DecimalPosition innerCircleCenter;
		private double innerCircleRadius;
		private double outerCircleRadius;

		private int lowestFloor = 255;

		public CentralCave(int x, int y, int z) {
			super(new DecimalPosition(x+0.5, y+0.5, z+0.5));
		}

		@Override
		protected void calculate(World world, Random rand) {

			floorHeightNoise = (SimplexNoiseGenerator)new SimplexNoiseGenerator(rand.nextLong()).setFrequency(1/16D).addOctave(2.6, 0.17);
			ceilingHeightNoise = (SimplexNoiseGenerator)new SimplexNoiseGenerator(rand.nextLong()).setFrequency(1/8D).addOctave(1.34, 0.41);

			double f = 1/5D;
			xOffset1 = (SimplexNoiseGenerator)new SimplexNoiseGenerator(rand.nextLong()).setFrequency(f);
			zOffset1 = (SimplexNoiseGenerator)new SimplexNoiseGenerator(rand.nextLong()).setFrequency(f);
			xOffset2 = (SimplexNoiseGenerator)new SimplexNoiseGenerator(rand.nextLong()).setFrequency(f);
			zOffset2 = (SimplexNoiseGenerator)new SimplexNoiseGenerator(rand.nextLong()).setFrequency(f);

			if (innerCircleOffset == null) {
				int dr = 10;
				//outerCircleOffset = new DecimalPosition(ReikaRandomHelper.getRandomPlusMinus(0, dr, rand), 0, ReikaRandomHelper.getRandomPlusMinus(0, dr, rand));
				/*do {
				outerCircleRadius = ReikaRandomHelper.getRandomBetween(24D, 36D, rand);
				innerCircleRadius = ReikaRandomHelper.getRandomBetween(6D, 15D, rand);
			} while(outerCircleRadius-innerCircleRadius >= 9);
				 */
				outerCircleRadius = ReikaRandomHelper.getRandomBetween(24D, 36D, rand);
				innerCircleRadius = ReikaRandomHelper.getRandomBetween(Math.max(6D, outerCircleRadius-MIN_TUNNEL_WIDTH*6), outerCircleRadius-MIN_TUNNEL_WIDTH*4, rand);
				double maxr = outerCircleRadius-innerCircleRadius-MIN_TUNNEL_WIDTH-3;
				double offr = ReikaRandomHelper.getRandomBetween(maxr*0.75, maxr, rand);
				double offa = rand.nextDouble()*360;
				double offX = offr*Math.cos(Math.toRadians(offa));
				double offZ = offr*Math.sin(Math.toRadians(offa));
				//innerCircleCenter = center.offset(ReikaRandomHelper.getRandomPlusMinus(0, maxr, rand), 0, ReikaRandomHelper.getRandomPlusMinus(0, maxr, rand));
				innerCircleOffset = new DecimalPosition(offX, 0, offZ);
				//outerCircleCenter = center.offset(outerCircleOffset);
			}

			innerCircleCenter = center.offset(innerCircleOffset);

			double r = outerCircleRadius;//+outerCircleOffset.xCoord+outerCircleOffset.zCoord;

			for (double i = -r; i <= r; i++) {
				for (double k = -r; k <= r; k++) {
					int x = MathHelper.floor_double(center.xCoord+i);
					int z = MathHelper.floor_double(center.zCoord+k);
					double di = i-innerCircleOffset.xCoord;
					double dk = k-innerCircleOffset.zCoord;
					boolean flag = false;
					int floor = Integer.MAX_VALUE;
					//	if (ReikaMathLibrary.py3d(i, 0, k) <= outerCircleRadius) {
					//	if (ReikaMathLibrary.py3d(di, 0, dk) >= innerCircleRadius) {
					double min = (int)(-6+MathHelper.clamp_double(floorHeightNoise.getValue(x, z)*3, -1.75, 1.75));
					double max = (int)(6+MathHelper.clamp_double(ceilingHeightNoise.getValue(x, z)*2, -2, 2));
					for (double h = min; h <= max; h++) {
						/* not working
								double d1 = 1;
								double d2 = 1;
								int diff = Math.min(h-min, max-h);
								if (diff <= 2) {
									d1 *= diff/3D;
									d2 += (3-diff)*2;
								}
								double r1 = outerCircleRadius*d1;
								double r2 = innerCircleRadius*d2;
						 */

						//add noise to edges
						if (h < -5.2)
							continue; //flatten the floor a little
						double dn = 4;//5;//3;
						double r1 = outerCircleRadius;
						double r2 = innerCircleRadius;
						int diff = (int)Math.min(h-min, max-h);
						if (diff <= 2) {
							int step = 3-diff; //1-3
							dn += step*0.7;

							r1 -= step*2;
							r2 += step;
						}
						double i2 = i+MathHelper.clamp_double(xOffset1.getValue(x, z)*dn, -3, 3);
						double k2 = k+MathHelper.clamp_double(zOffset1.getValue(x, z)*dn, -3, 3);
						double di2 = di+MathHelper.clamp_double(xOffset2.getValue(x, z)*dn, -2, 2);
						double dk2 = dk+MathHelper.clamp_double(zOffset2.getValue(x, z)*dn, -2, 2);
						if (ReikaMathLibrary.py3d(i2, 0, k2) <= r1) {
							floor = Math.min(floor, (int)(center.yCoord+h));
							if (ReikaMathLibrary.py3d(di2, 0, dk2) >= r2) {
								Coordinate c = new Coordinate(x, (int)(center.yCoord+h), z);
								carve.put(c, (int)center.yCoord);
							}
							flag = true;
						}
					}
					//}
					//}
					if (flag) {
						Coordinate c = new Coordinate(x, 0, z);
						footprint.put(c, floor);
						lowestFloor = Math.min(floor, lowestFloor);
					}
				}
			}
		}

		@Override
		protected boolean skipCarve(Coordinate c) {
			return false;
		}

	}

	private static class ResourceNodeRoom extends UraniumCavePiece {

		private final HashMap<DecimalPosition, Integer> disks = new HashMap();
		private final HashSet<Coordinate> adjacent = new HashSet();
		private final HashSet<Coordinate> adjacentFloor = new HashSet();

		private Coordinate resourceNode;
		private final HashSet<Coordinate> nodeClearArea = new HashSet();

		protected ResourceNodeRoom(DecimalPosition p) {
			super(p);
		}

		void generateOreSpike(World world, Random rand, BlockKey ore) {
			/*
			placeOreIfEmpty(world, ore, resourceNode.offset(0, 1, 0));
			placeOreIfEmpty(world, ore, resourceNode.offset(0, 2, 0));
			placeOreIfEmpty(world, ore, resourceNode.offset(0, 2, 0));
			 */
			for (int i = 1; i <= 3; i++) {
				Coordinate c = resourceNode.offset(0, i, 0);
				if (c.softBlock(world)) {
					c.setBlock(world, ore.blockID, ore.metadata);
					if (i <= 2) {
						for (Coordinate c2 : c.getAdjacentCoordinates()) {
							if (c2.yCoord == c.yCoord && c2.softBlock(world)) {
								c2.setBlock(world, ore.blockID, ore.metadata);
							}
						}
					}
					if (i == 1) {
						Coordinate c2 = resourceNode.offset(1, i, 0);
						if (c2.yCoord == c.yCoord && c2.softBlock(world)) {
							c2.setBlock(world, ore.blockID, ore.metadata);
						}
						c2 = resourceNode.offset(-1, i, 0);
						if (c2.yCoord == c.yCoord && c2.softBlock(world)) {
							c2.setBlock(world, ore.blockID, ore.metadata);
						}
						c2 = resourceNode.offset(0, i, 1);
						if (c2.yCoord == c.yCoord && c2.softBlock(world)) {
							c2.setBlock(world, ore.blockID, ore.metadata);
						}
						c2 = resourceNode.offset(0, i, -1);
						if (c2.yCoord == c.yCoord && c2.softBlock(world)) {
							c2.setBlock(world, ore.blockID, ore.metadata);
						}
					}
				}
				else {
					break;
				}
			}
		}

		@Override
		protected void calculate(World world, Random rand) {
			int n = ReikaRandomHelper.getRandomBetween(4, 7, rand); //was 3-6
			double dr = 7;//8;//5;
			for (int i = 0; i < n; i++) {
				DecimalPosition c = center.offset(ReikaRandomHelper.getRandomPlusMinus(1, dr, rand), ReikaRandomHelper.getRandomPlusMinus(0, 1.75D, rand), ReikaRandomHelper.getRandomPlusMinus(1, dr, rand));
				int r = ReikaRandomHelper.getRandomBetween(4, 7, rand);
				disks.put(c, r);
			}
			for (Entry<DecimalPosition, Integer> e : disks.entrySet()) {
				DecimalPosition ctr = e.getKey();
				int r = e.getValue();
				int ry = (int)(r/2.5);
				for (int i = -r; i <= r; i++) {
					for (int j = -ry; j <= ry; j++) {
						for (int k = -r; k <= r; k++) {
							if (ReikaMathLibrary.isPointInsideEllipse(i, j, k, r, ry, r)) {
								int dx = MathHelper.floor_double(ctr.xCoord+i);
								int dy = MathHelper.floor_double(ctr.yCoord+j);
								int dz = MathHelper.floor_double(ctr.zCoord+k);
								Coordinate c = new Coordinate(dx, dy, dz);
								carve.put(c, dy);
								adjacent.addAll(c.getAdjacentCoordinates());
							}
						}
					}
				}
			}
			boolean flag = true;
			while (flag) {
				adjacent.removeAll(carve.keySet());
				flag = false;
				for (Coordinate c : adjacent) {
					int adjacentSides = 0;
					for (Coordinate c2 : c.getAdjacentCoordinates()) {
						if (!carve.containsKey(c2)) {
							adjacentSides++;
						}
					}
					if (adjacentSides <= 1) {
						carve.put(c, c.yCoord);
						flag = true;
					}
				}
			}
			for (Coordinate c : adjacent) {
				if (carve.containsKey(c.offset(0, 1, 0))) {
					boolean flag2 = false;
					for (Coordinate c2 : c.getAdjacentCoordinates()) {
						if (!c2.isEmpty(world)) {
							flag2 = true;
							break;
						}
					}
					if (flag2)
						adjacentFloor.add(c);
				}
			}
		}

		@Override
		protected void generate(World world, Random rand) {
			super.generate(world, rand);

			//this.generateResourceNode(world, rand);
		}

		void generateResourceNode(World world, Random rand) {
			if (resourceNode == null) {
				Coordinate c = ReikaJavaLibrary.getRandomCollectionEntry(rand, disks.keySet()).getCoordinate();//new Coordinate(center);
				Coordinate below = c.offset(0, -1, 0);
				while (carve.containsKey(below)) {
					c = below;
					below = c.offset(0, -1, 0);
				}
				resourceNode = below;
			}

			this.generateResourceNodeAt(world, rand);
		}

		private void generateResourceNodeAt(World world, Random rand) {
			resourceNode.setBlock(world, SFBlocks.RESOURCENODE.getBlockInstance());
			TileResourceNode te = (TileResourceNode)resourceNode.getTileEntity(world);
			te.generate(rand);

			HashSet<Coordinate> cleared = new HashSet();
			int r = 2;//1;
			for (int i = -r; i <= r; i++) {
				for (int k = -r; k <= r; k++) {
					if (Math.abs(i) == 2 && Math.abs(k) == 2)
						continue;
					Coordinate below = resourceNode.offset(i, -1, k);
					Coordinate above = resourceNode.offset(i, 1, k);
					Coordinate above2 = resourceNode.offset(i, 2, k);
					Coordinate at = resourceNode.offset(i, 0, k);
					cleared.add(above);
					cleared.add(above2);
					nodeClearArea.add(at.to2D());
					if (i != 0 || k != 0) {
						at.setBlock(world, SFBlocks.CAVESHIELD.getBlockInstance());
					}
					below.setBlock(world, SFBlocks.CAVESHIELD.getBlockInstance());
				}
			}
			for (Coordinate c2 : cleared) {
				for (Coordinate c3 : c2.getAdjacentCoordinates()) {
					if (!carve.containsKey(c3) && !cleared.contains(c3) && !instance.isSpecialCaveBlock(c3.getBlock(world))) {
						c3.setBlock(world, SFBlocks.CAVESHIELD.getBlockInstance());
					}
				}
				c2.setBlock(world, Blocks.air);
			}
		}

	}

	private static abstract class UraniumCavePiece {

		protected final DecimalPosition center;

		protected final HashMap<Coordinate, Integer> carve = new HashMap();

		protected UraniumCavePiece(DecimalPosition p) {
			center = p;
		}

		protected abstract void calculate(World world, Random rand);

		protected void generate(World world, Random rand) {
			for (Coordinate c : carve.keySet()) {
				c.setBlock(world, Blocks.air);
				/*
				for (Coordinate c2 : c.getAdjacentCoordinates()) {
					if (this.skipCarve(c2))
						continue;
					if (c2.yCoord <= 58 && c2.softBlock(world) && !carve.containsKey(c2)) {
						c2.setBlock(world, Blocks.stone);
					}
				}
				 */
			}
		}

		protected final boolean hasAdjacentHorizontalCarve(Coordinate c) {
			for (int i = 0; i < 4; i++) {
				ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i+2];
				if (carve.containsKey(c.offset(dir, 1)))
					return true;
			}
			return false;
		}

		protected final boolean carveAt(World world, DecimalPosition p, double r, double w, double angle) {
			return this.carveAt(world, p, r, r, w, angle);
		}

		protected final boolean carveAt(World world, DecimalPosition p, double r, double h, double w, double angle) {
			angle += 90;
			boolean flag = false;
			double ax = w > 0 ? Math.abs(Math.cos(Math.toRadians(angle))) : 0;
			double az = w > 0 ? Math.abs(Math.sin(Math.toRadians(angle))) : 0;
			for (double i = -r; i <= r; i++) {
				for (double j = -r; j <= r; j++) {
					for (double k = -r; k <= r; k++) {
						if (ReikaMathLibrary.isPointInsideEllipse(i, j, k, r+0.5+ax*w, h+0.5, r+0.5+az*w)) {
							int dx = MathHelper.floor_double(p.xCoord+i);
							int dy = MathHelper.floor_double(p.yCoord+j);
							int dz = MathHelper.floor_double(p.zCoord+k);
							Coordinate c = new Coordinate(dx, dy, dz);
							if (this.skipCarve(c))
								continue;
							Block b = c.getBlock(world);
							if (c.isEmpty(world) || instance.isSpecialCaveBlock(b) || DecoratorPinkForest.isTerrain(world, c.xCoord, c.yCoord, c.zCoord)) {
								if (!carve.containsKey(c)) {
									flag = true;
								}
								carve.put(c, MathHelper.floor_double(p.yCoord));
							}
						}
					}
				}
			}
			return flag;
		}

		protected boolean skipCarve(Coordinate c) {
			return false;
		}
	}

	public static class CachedCave {

		public final Coordinate center;
		public final double outerRadius;
		public final double innerRadius;
		final DecimalPosition innerOffset;
		public final DecimalPosition nodeRoom;
		public final Coordinate nodeTile;
		final HashMap<Coordinate, Double> tunnels = new HashMap();

		CachedCave(CentralCave cc) {
			center = new Coordinate(cc.center);
			outerRadius = cc.outerCircleRadius;
			innerRadius = cc.innerCircleRadius;
			innerOffset = cc.innerCircleOffset;
			nodeRoom = cc.nodeRoom.center;
			nodeTile = cc.nodeRoom.resourceNode;
			for (Tunnel t : cc.tunnels) {
				tunnels.put(t.endpoint, t.startingAngle);
			}
		}

		public ResourceNodeRoom reconstructNode(Coordinate node) {
			ResourceNodeRoom rm = new ResourceNodeRoom(nodeRoom);
			rm.resourceNode = node;
			return rm;
		}

		public CentralCave reconstruct() {
			CentralCave ret = new CentralCave(center.xCoord, center.yCoord, center.zCoord);
			ret.outerCircleRadius = outerRadius;
			ret.innerCircleRadius = innerRadius;
			ret.innerCircleOffset = innerOffset;
			return ret;
		}

		public CachedCave(Coordinate ctr, DecimalPosition node, Coordinate tile, double radius, double inner, DecimalPosition off, HashMap<Coordinate, Double> map) {
			center = ctr;
			nodeRoom = node;
			nodeTile = tile;
			outerRadius = radius;
			innerRadius = inner;
			innerOffset = off;
			tunnels.putAll(map);
		}

		public boolean isInside(int x, int y, int z) {
			return (center.getDistanceTo(x, y, z) <= outerRadius && Math.abs(center.yCoord-y) <= 10) || nodeRoom.getDistanceTo(x, y, z) <= 9;
		}

		@Override
		public String toString() {
			return center+" @ R = ["+innerRadius+", "+outerRadius+"] + "+innerOffset+" & "+nodeRoom+" via "+tunnels;
		}

	}
}
