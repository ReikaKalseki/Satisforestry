package Reika.Satisforestry.Biome.Biomewide;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.Satisforestry.Biome.BiomeFootprint;
import Reika.Satisforestry.Biome.DecoratorPinkForest;
import Reika.Satisforestry.Entity.EntityLizardDoggo;

public class LizardDoggoSpawner {

	public static final LizardDoggoSpawner instance = new LizardDoggoSpawner();

	private LizardDoggoSpawner() {

	}

	public Collection<LizardDoggoSpawnPoint> createDoggoSpawnPoints(World world, BiomeFootprint bf, Random rand) {
		HashSet<LizardDoggoSpawnPoint> ret = new HashSet();
		ArrayList<Coordinate> blocks = new ArrayList(bf.getCoords());
		int n = MathHelper.clamp_int(Math.round(bf.getArea()/24000F), 1, 6);
		for (int i = 0; i < n && !blocks.isEmpty(); i++) {
			int idx = rand.nextInt(blocks.size());
			Coordinate c = blocks.remove(idx);
			c = c.setY(DecoratorPinkForest.getTrueTopAt(world, c.xCoord, c.zCoord)+1);
			if (this.isValidDoggoSpawnArea(world, c)) {
				boolean flag = true;
				for (LizardDoggoSpawnPoint has : ret) {
					if (has.location.getTaxicabDistanceTo(c.xCoord, c.yCoord, c.zCoord) <= 64) {
						flag = false;
						break;
					}
				}
				if (flag)
					ret.add(new LizardDoggoSpawnPoint(world, c));
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

	public static class LizardDoggoSpawnPoint {

		public final WorldLocation location;

		private boolean doggoExists = false;
		private long lastTick;

		private LizardDoggoSpawnPoint(World world, Coordinate c) {
			location = new WorldLocation(world, c);
		}

		private LizardDoggoSpawnPoint(WorldLocation loc) {
			location = loc;
		}

		@Override
		public String toString() {
			return location.toString();
		}

		@Override
		public int hashCode() {
			return location.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof LizardDoggoSpawnPoint && location.equals(((LizardDoggoSpawnPoint)o).location);
		}

		public NBTTagCompound writeToTag() {
			NBTTagCompound ret = new NBTTagCompound();
			location.writeToNBT("loc", ret);
			ret.setBoolean("exists", doggoExists);
			ret.setLong("tick", lastTick);
			return ret;
		}

		public static LizardDoggoSpawnPoint readTag(NBTTagCompound NBT) {
			LizardDoggoSpawnPoint ret = new LizardDoggoSpawnPoint(WorldLocation.readFromNBT("loc", NBT));
			ret.doggoExists = NBT.getBoolean("exists");
			ret.lastTick = NBT.getLong("tick");
			return ret;
		}

		public void tick(World world, EntityPlayer ep) {
			long time = world.getTotalWorldTime();
			if (time == lastTick)
				return;
			lastTick = time;
			if (!doggoExists && ep.getDistanceSq(location.xCoord+0.5, location.yCoord+0.5, location.zCoord+0.5) <= 350) {
				doggoExists = this.trySpawn(world);
			}
		}

		public void removeDoggo(EntityLizardDoggo e) {
			doggoExists = false;
			BiomewideFeatureGenerator.instance.save(e.worldObj);
		}

		private boolean trySpawn(World world) {
			double r = 7.5;
			double minX = location.xCoord+0.5-r;
			double maxX = location.xCoord+0.5+r;
			double minZ = location.zCoord+0.5-r;
			double maxZ = location.zCoord+0.5+r;
			double x = ReikaRandomHelper.getRandomBetween(minX, maxX);
			double z = ReikaRandomHelper.getRandomBetween(minZ, maxZ);
			EntityLizardDoggo e = new EntityLizardDoggo(world);
			e.setLocationAndAngles(x, location.yCoord+1+world.rand.nextDouble()*1.5, z, 0, 0);
			if (e.getCanSpawnHere()) {
				e.rotationYaw = world.rand.nextFloat()*360;
				e.setSpawn(location);
				world.spawnEntityInWorld(e);
				return true;
			}
			return false;
		}

	}

}
