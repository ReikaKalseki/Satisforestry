package Reika.Satisforestry.Biome.Biomewide;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Data.Immutable.DecimalPosition;
import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.DragonAPI.Libraries.ReikaNBTHelper.NBTTypes;
import Reika.Satisforestry.Biome.BiomeFootprint;
import Reika.Satisforestry.Biome.PinkForestPersistentData;
import Reika.Satisforestry.Biome.Biomewide.UraniumCave.CachedCave;
import Reika.Satisforestry.Biome.Biomewide.UraniumCave.CentralCave;

public class BiomewideFeatureGenerator {

	public static final BiomewideFeatureGenerator instance = new BiomewideFeatureGenerator();

	private final HashMap<WorldLocation, CachedCave> caveNetworks = new HashMap();

	private BiomewideFeatureGenerator() {

	}

	public void generateUniqueCenterFeatures(World world, int x, int z, Random rand, BiomeFootprint bf) {
		Collection<Coordinate> rivers = PinkRivers.instance.generateRivers(world, x, z, rand, bf);
		if (!rivers.isEmpty()) {
			CachedCave at = caveNetworks.get(new WorldLocation(world, x, 0, z));
			CentralCave cc = UraniumCave.instance.generate(world, rand, x, z, rivers, at);
			if (cc != null) {
				caveNetworks.put(new WorldLocation(world, cc.center.to2D()), new CachedCave(cc));
				PinkForestPersistentData.initNetworkData(world).setDirty(true);
			}
		}
	}

	public boolean isInCave(World world, int x, int y, int z) {
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

	public void readFromNBT(NBTTagCompound NBT) {
		NBTTagList li = NBT.getTagList("caves", NBTTypes.COMPOUND.ID);
		for (Object o : li.tagList) {
			NBTTagCompound tag = (NBTTagCompound)o;
			Coordinate center = Coordinate.readTag(tag.getCompoundTag("center"));
			DecimalPosition node = DecimalPosition.readTag(tag.getCompoundTag("node"));
			DecimalPosition off = DecimalPosition.readTag(tag.getCompoundTag("offset"));
			double radius = tag.getDouble("radius");
			double inner = tag.getDouble("inner");
			HashMap<Coordinate, Double> map = new HashMap();
			NBTTagList tunnels = tag.getTagList("tunnels", NBTTypes.COMPOUND.ID);
			for (Object o2 : tunnels.tagList) {
				NBTTagCompound tag2 = (NBTTagCompound)o2;
				Coordinate end = Coordinate.readTag(tag2.getCompoundTag("endpoint"));
				double ang = tag2.getDouble("angle");
				map.put(end, ang);
			}
			WorldLocation key = WorldLocation.readTag(tag);
			caveNetworks.put(key, new CachedCave(center, node, radius, inner, off, map));
		}
	}

	public void writeToNBT(NBTTagCompound NBT) {
		NBTTagList li = new NBTTagList();
		for (Entry<WorldLocation, CachedCave> e : caveNetworks.entrySet()) {
			NBTTagCompound cave = new NBTTagCompound();
			CachedCave cv = e.getValue();
			cave.setTag("key", e.getKey().writeToTag());
			cave.setTag("center", cv.center.writeToTag());
			cave.setTag("node", cv.nodeRoom.writeToTag());
			cave.setTag("offset", cv.innerOffset.writeToTag());
			cave.setDouble("radius", cv.outerRadius);
			cave.setDouble("inner", cv.innerRadius);
			NBTTagList tunnels = new NBTTagList();
			for (Entry<Coordinate, Double> e2 : cv.tunnels.entrySet()) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setTag("endpoint", e2.getKey().writeToTag());
				tag.setDouble("angle", e2.getValue());
				tunnels.appendTag(tag);
			}
			cave.setTag("tunnels", tunnels);
			li.appendTag(cave);
		}
		NBT.setTag("caves", li);
	}
}
