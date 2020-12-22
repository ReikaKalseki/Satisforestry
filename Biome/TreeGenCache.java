package Reika.Satisforestry.Biome;

import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Data.Immutable.WorldChunk;
import Reika.DragonAPI.Instantiable.Worldgen.ChunkSplicedGenerationCache;
import Reika.DragonAPI.Libraries.ReikaNBTHelper.NBTTypes;
import Reika.Satisforestry.Biome.Generator.GiantPinkTreeGenerator;

@Deprecated
public class TreeGenCache {

	public static final TreeGenCache instance = new TreeGenCache();

	private final HashMap<WorldChunk, HashMap<Coordinate, GiantPinkTreeGenerator>> treeCache = new HashMap();
	private final HashMap<Integer, ChunkSplicedGenerationCache> blockCache = new HashMap();

	private TreeGenCache() {

	}

	public void addTree(World world, int x, int y, int z, GiantPinkTreeGenerator gen) {
		WorldChunk wc = new WorldChunk(world, x >> 4, z >> 4);
		HashMap<Coordinate, GiantPinkTreeGenerator> map = treeCache.get(wc);
		if (map == null) {
			map = new HashMap();
			treeCache.put(wc, map);
		}
		map.put(new Coordinate(x, y, z), gen);
	}

	public void generateChunk(World world, int chunkX, int chunkZ) {
		WorldChunk wc = new WorldChunk(world, chunkX, chunkZ);
		HashMap<Coordinate, GiantPinkTreeGenerator> map = treeCache.get(wc);
		if (map != null) {
			for (Entry<Coordinate, GiantPinkTreeGenerator> e : map.entrySet()) {
				Coordinate c = e.getKey();
				e.getValue().generate(world, null, c.xCoord, c.yCoord, c.zCoord);
			}
		}
		ChunkSplicedGenerationCache get = blockCache.get(world.provider.dimensionId);
		if (get != null) {
			get.generate(world, chunkX, chunkZ);
		}
	}

	public String getTreeData() {
		return treeCache.toString();
	}

	public String getBlockData() {
		return blockCache.toString();
	}

	public void generateAll() {
		for (WorldChunk wc : treeCache.keySet()) {
			World world = DimensionManager.getWorld(wc.dimensionID);
			this.generateChunk(world, wc.chunk.chunkXPos, wc.chunk.chunkZPos);
		}
	}

	public void readFromNBT(NBTTagCompound NBT) {
		treeCache.clear();
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
			treeCache.put(key, mapd);
		}

		blockCache.clear();
		li = NBT.getTagList("blocks", NBTTypes.COMPOUND.ID);
		for (Object o : li.tagList) {
			NBTTagCompound tag = (NBTTagCompound)o;
			//blockCache.put(tag.getInteger("dimension"), ChunkSplicedGenerationCache.readFromNBT(tag.getCompoundTag("blocks")));
		}
	}

	public void writeToNBT(NBTTagCompound NBT) {
		NBTTagList li = new NBTTagList();
		for (Entry<WorldChunk, HashMap<Coordinate, GiantPinkTreeGenerator>> e1 : treeCache.entrySet()) {
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

		li = new NBTTagList();
		for (Entry<Integer, ChunkSplicedGenerationCache> e : blockCache.entrySet()) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("dimension", e.getKey());
			//tag.setTag("blocks", e.getValue().writeToNBT());
			li.appendTag(tag);
		}
		NBT.setTag("blocks", li);
	}

	public void addBlock(World world, int x, int y, int z, Block b, int meta) {
		ChunkSplicedGenerationCache get = blockCache.get(world.provider.dimensionId);
		if (get == null) {
			get = new ChunkSplicedGenerationCache();
			blockCache.put(world.provider.dimensionId, get);
		}
		get.setBlock(x, y, z, b, meta);
	}

}
