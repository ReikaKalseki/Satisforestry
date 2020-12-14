package Reika.Satisforestry;

import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;

public class OreClusterType {

	public final String id;
	public final BlockKey oreBlock;
	public final int spawnWeight;

	public float sizeScale;
	public int maxDepth;
	public boolean canSpawnInMainRing;

	public OreClusterType(String s, BlockKey bk, int w) {
		id = s;
		spawnWeight = w;
		oreBlock = bk;
	}

}
