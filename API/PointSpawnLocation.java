package Reika.Satisforestry.API;

import net.minecraft.entity.EntityLiving;

/** An active mob spawning point. These are not like vanilla mob spawners, but more like the spawns in Satisfactory: A spawn point has a defined profile of
 which mobs it will spawn (count and type), and will fill that cap when a player approaches. Should a player leave, the mobs despawn and will reappear the
 the next time it is approached. If despawned or killed from non-player sources, spawnpoint mobs will infinitely replenish themselves to fill this cap.
 If a player kills every mob defined by the spawn point, it is usually permanently deactivated. Partially-cleared spawn points reset if departed from.

 Point spawn locations are NOT necessarily tied to a block; Some indeed are, but several others are "ambient" abstract not-in-world objects.
 */
public interface PointSpawnLocation {

	/** The player distance at which the point will activate and spawn its mobs until the cap is full. */
	public double getActivationRadius();

	/** The distance at which the partial clearing will reset. */
	public double getResetRadius();

	/** The player distance from the point at which all entities it spawned will be deleted, regardless of their distance from the player. */
	public double getAutoClearRadius();

	/** Whether player kills will permanently decrement from the spawn cap */
	public boolean canBeCleared();

	public int getDimension();

	public int getX();

	public int getY();

	public int getZ();

	/** Whether this spawn point is tied to a block. */
	public boolean isBlock();

	/** Which entity class this point spawns. Do <i>not</i> attempt spawning your own. */
	public Class<? extends EntityLiving> getSpawnType();

	/** How many mobs are to be spawned by this point, equal to the original base cap minus how many the player has killed since the last reset. */
	public int getActiveSpawnCap();

	/** How many mobs are currently "in play" (ie spawned in world and active) from this spawn point. */
	public int getCurrentlySpawned();
}
