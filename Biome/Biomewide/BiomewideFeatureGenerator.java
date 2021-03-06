package Reika.Satisforestry.Biome.Biomewide;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Data.Immutable.DecimalPosition;
import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.DragonAPI.Instantiable.Data.Maps.MultiMap;
import Reika.DragonAPI.Libraries.ReikaNBTHelper.NBTTypes;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.BiomeFootprint;
import Reika.Satisforestry.Biome.PinkForestPersistentData;
import Reika.Satisforestry.Biome.Biomewide.LizardDoggoSpawner.LizardDoggoSpawnPoint;
import Reika.Satisforestry.Biome.Biomewide.MantaGenerator.MantaPath;
import Reika.Satisforestry.Biome.Biomewide.UraniumCave.CachedCave;
import Reika.Satisforestry.Biome.Biomewide.UraniumCave.CachedTunnel;
import Reika.Satisforestry.Biome.Biomewide.UraniumCave.CentralCave;
import Reika.Satisforestry.Entity.EntityFlyingManta;

public class BiomewideFeatureGenerator {

	public static final BiomewideFeatureGenerator instance = new BiomewideFeatureGenerator();

	private final HashMap<WorldLocation, CachedCave> caveNetworks = new HashMap();
	private final MultiMap<Integer, LizardDoggoSpawnPoint> doggoSpawns = new MultiMap();
	private final HashMap<WorldLocation, MantaPath> mantaPaths = new HashMap();
	private final HashSet<Integer> initialized = new HashSet();

	private BiomewideFeatureGenerator() {

	}

	public void clearOnUnload() {
		caveNetworks.clear();
		doggoSpawns.clear();
		mantaPaths.clear();
		initialized.clear();
	}

	public void generateUniqueCenterFeatures(World world, int x, int z, Random rand, BiomeFootprint bf) {
		initialized.add(world.provider.dimensionId);
		PinkForestPersistentData.initNetworkData(world);
		//bf.exportToImage(new File(world.getSaveHandler().getWorldDirectory(), "pinkforest_footprint"));
		Collection<LizardDoggoSpawnPoint> spawns = LizardDoggoSpawner.instance.createDoggoSpawnPoints(world, bf, rand);
		for (LizardDoggoSpawnPoint loc : spawns) {
			doggoSpawns.addValue(loc.location.dimensionID, loc);
			Satisforestry.logger.log("Doggo spawn locations around "+x+", "+z+": "+spawns);
		}
		MantaPath path = mantaPaths.get(new WorldLocation(world, x, 0, z));
		if (path == null)
			path = MantaGenerator.instance.generatePathAroundBiome(world, bf, rand);
		Collection<Coordinate> rivers = PinkRivers.instance.generateRivers(world, x, z, rand, bf);
		boolean flag = false;
		if (!rivers.isEmpty()) {
			CachedCave at = caveNetworks.get(new WorldLocation(world, x, 0, z));
			CentralCave cc = UraniumCave.instance.generate(world, rand, x, z, rivers, at);
			if (cc != null) {
				caveNetworks.put(new WorldLocation(world, cc.center.to2D()), new CachedCave(cc));
				flag = true;
			}
		}
		if (!flag) {
			Satisforestry.logger.logError("Failed to generate biomewide terrain features! River set: "+rivers);
		}
		if (path == null) {
			Satisforestry.logger.logError("Failed to generate manta path!");
		}
		else {
			mantaPaths.put(path.biomeCenter, path);
			EntityFlyingManta e = new EntityFlyingManta(world);
			path.clearBlocks(world);
			e.setPath(path);
			world.spawnEntityInWorld(e);
			Satisforestry.logger.log("Generated manta path around "+x+", "+z);
		}
		this.save(world);
	}

	public void save(World world) {
		PinkForestPersistentData.initNetworkData(world).setDirty(true);
	}

	public boolean isInCave(World world, double x, double y, double z) {
		if (!world.isRemote && !initialized.contains(world.provider.dimensionId)) {
			initialized.add(world.provider.dimensionId);
			PinkForestPersistentData.initNetworkData(world);
		}
		for (Entry<WorldLocation, CachedCave> e : caveNetworks.entrySet()) {
			if (e.getKey().dimensionID == world.provider.dimensionId) {
				CachedCave cv = e.getValue();
				if (cv.isInside(x, y, z)) {
					return true;
				}
			}
		}
		return false;
	}

	public MantaPath getPathAround(World world, WorldLocation loc) {
		if (!world.isRemote && !initialized.contains(world.provider.dimensionId)) {
			initialized.add(world.provider.dimensionId);
			PinkForestPersistentData.initNetworkData(world);
		}
		return mantaPaths.get(loc);
	}

	public Collection<LizardDoggoSpawnPoint> getDoggoSpawns(World world) {
		return Collections.unmodifiableCollection(doggoSpawns.get(world.provider.dimensionId));
	}

	public LizardDoggoSpawnPoint getDoggoSpawnAt(WorldLocation loc) {
		for (LizardDoggoSpawnPoint spawn : doggoSpawns.get(loc.dimensionID)) {
			if (spawn.location.equals(loc)) {
				return spawn;
			}
		}
		return null;
	}

	public void readFromNBT(NBTTagCompound NBT) {
		NBTTagList li = NBT.getTagList("caves", NBTTypes.COMPOUND.ID);
		for (Object o : li.tagList) {
			NBTTagCompound tag = (NBTTagCompound)o;
			Coordinate center = Coordinate.readTag(tag.getCompoundTag("center"));
			Coordinate tile = Coordinate.readTag(tag.getCompoundTag("tile"));
			DecimalPosition node = DecimalPosition.readTag(tag.getCompoundTag("node"));
			DecimalPosition off = DecimalPosition.readTag(tag.getCompoundTag("offset"));
			double radius = tag.getDouble("radius");
			double inner = tag.getDouble("inner");
			HashMap<Coordinate, CachedTunnel> map = new HashMap();
			NBTTagList tunnels = tag.getTagList("tunnels", NBTTypes.COMPOUND.ID);
			for (Object o2 : tunnels.tagList) {
				CachedTunnel end = CachedTunnel.readTag((NBTTagCompound)o2);
				map.put(end.endpoint, end);
			}
			WorldLocation key = WorldLocation.readTag(tag.getCompoundTag("key"));
			caveNetworks.put(key, new CachedCave(center, node, tile, radius, inner, off, map));
		}

		mantaPaths.clear();
		li = NBT.getTagList("mantas", NBTTypes.COMPOUND.ID);
		for (Object o : li.tagList) {
			NBTTagCompound tag = (NBTTagCompound)o;
			MantaPath path = MantaPath.readFromNBT(tag);
			if (path != null)
				mantaPaths.put(path.biomeCenter, path);
		}

		doggoSpawns.clear();
		li = NBT.getTagList("doggoSpawns", NBTTypes.COMPOUND.ID);
		for (Object o : li.tagList) {
			NBTTagCompound tag = (NBTTagCompound)o;
			LizardDoggoSpawnPoint c = LizardDoggoSpawnPoint.readTag(tag);
			doggoSpawns.addValue(c.location.dimensionID, c);
		}
	}

	public void writeToNBT(NBTTagCompound NBT) {
		NBTTagList li = new NBTTagList();
		for (Entry<WorldLocation, CachedCave> e : caveNetworks.entrySet()) {
			NBTTagCompound cave = new NBTTagCompound();
			CachedCave cv = e.getValue();
			cave.setTag("key", e.getKey().writeToTag());
			cave.setTag("center", cv.center.writeToTag());
			cave.setTag("tile", cv.nodeTile.writeToTag());
			cave.setTag("node", cv.nodeRoom.writeToTag());
			cave.setTag("offset", cv.innerOffset.writeToTag());
			cave.setDouble("radius", cv.outerRadius);
			cave.setDouble("inner", cv.innerRadius);
			NBTTagList tunnels = new NBTTagList();
			for (CachedTunnel e2 : cv.tunnels.values()) {
				tunnels.appendTag(e2.writeToTag());
			}
			cave.setTag("tunnels", tunnels);
			li.appendTag(cave);
		}
		NBT.setTag("caves", li);

		li = new NBTTagList();
		for (MantaPath e : mantaPaths.values()) {
			li.appendTag(e.writeToNBT());
		}
		NBT.setTag("mantas", li);

		li = new NBTTagList();
		for (LizardDoggoSpawnPoint loc : doggoSpawns.allValues(false)) {
			li.appendTag(loc.writeToTag());
		}
		NBT.setTag("doggoSpawns", li);
	}
}
