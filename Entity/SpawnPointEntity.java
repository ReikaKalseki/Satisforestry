package Reika.Satisforestry.Entity;

import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;

public interface SpawnPointEntity {

	public void setSpawn(WorldLocation loc);

	public WorldLocation getSpawn();

}
