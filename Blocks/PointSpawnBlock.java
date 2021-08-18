package Reika.Satisforestry.Blocks;

import Reika.Satisforestry.Biome.Biomewide.PointSpawnSystem.SpawnPoint;

public interface PointSpawnBlock {

	//public PointSpawnTile getTile(IBlockAccess iba, int x, int y, int z);

	public static interface PointSpawnTile {

		public SpawnPoint getSpawner();

	}

}
