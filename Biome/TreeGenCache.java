package Reika.Satisforestry.Biome;

import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Data.Immutable.WorldChunk;
import Reika.DragonAPI.Libraries.ReikaNBTHelper.NBTTypes;
import Reika.Satisforestry.Biome.Generator.GiantPinkTreeGenerator;

public class TreeGenCache {

	public static final TreeGenCache instance = new TreeGenCache();

	private final HashMap<WorldChunk, HashMap<Coordinate, GiantPinkTreeGenerator>> data = new HashMap();

	private TreeGenCache() {

	}

	public void add(World world, int x, int y, int z, GiantPinkTreeGenerator gen) {
		WorldChunk wc = new WorldChunk(world, x >> 4, z >> 4);
		HashMap<Coordinate, GiantPinkTreeGenerator> map = data.get(wc);
		if (map == null) {
			map = new HashMap();
			data.put(wc, map);
		}
		map.put(new Coordinate(x, y, z), gen);
	}

	public void generate(World world, int chunkX, int chunkZ) {
		WorldChunk wc = new WorldChunk(world, chunkX, chunkZ);
		HashMap<Coordinate, GiantPinkTreeGenerator> map = data.get(wc);
		if (map != null) {
			for (Entry<Coordinate, GiantPinkTreeGenerator> e : map.entrySet()) {
				Coordinate c = e.getKey();
				e.getValue().generate(world, null, c.xCoord, c.yCoord, c.zCoord);
			}
		}
	}

	public String getTreeData() {
		for (WorldChunk wc : data.keySet()) {
			World world = DimensionManager.getWorld(wc.dimensionID);
			this.generate(world, wc.chunk.chunkXPos, wc.chunk.chunkZPos);
		}
		return data.toString();
	}

	public void readFromNBT(NBTTagCompound NBT) {
		data.clear();
		NBTTagList li = NBT.getTagList("trees", NBTTypes.COMPOUND.ID);
		for (Object o1 : li.tagList) {
			HashMap<Coordinate, GiantPinkTreeGenerator> mapd = new HashMap();
			NBTTagCompound tag = (NBTTagCompound)o1;
			WorldChunk key = WorldChunk.readFromTag(tag.getCompoundTag("key"));
			NBTTagList map = tag.getTagList("map", NBTTypes.COMPOUND.ID);
			for (Object o2 : map.tagList) {
				NBTTagCompound sub = (NBTTagCompound)o2;
				Coordinate loc = Coordinate.readTag(sub.getCompoundTag("loc"));
				GiantPinkTreeGenerator gen = GiantPinkTreeGenerator.readNBT(sub.getCompoundTag("gen"));
				mapd.put(loc, gen);
			}
			data.put(key, mapd);
		}
	}

	public void writeToNBT(NBTTagCompound NBT) {
		NBTTagList li = new NBTTagList();
		for (Entry<WorldChunk, HashMap<Coordinate, GiantPinkTreeGenerator>> e1 : data.entrySet()) {
			NBTTagCompound tag = new NBTTagCompound();
			NBTTagList map = new NBTTagList();
			for (Entry<Coordinate, GiantPinkTreeGenerator> e2 : e1.getValue().entrySet()) {
				NBTTagCompound sub = new NBTTagCompound();
				sub.setTag("loc", e2.getKey().writeToTag());
				sub.setTag("gen", e2.getValue().getNBT());
				map.appendTag(sub);
			}
			tag.setTag("key", e1.getKey().writeToTag());
			tag.setTag("map", map);
			li.appendTag(tag);
		}
		NBT.setTag("trees", li);
	}

}
