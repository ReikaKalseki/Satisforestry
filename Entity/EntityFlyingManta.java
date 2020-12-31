package Reika.Satisforestry.Entity;

import java.util.List;

import net.minecraft.entity.EntityFlying;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.Immutable.DecimalPosition;
import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.DragonAPI.Libraries.MathSci.ReikaPhysicsHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.Biomewide.BiomewideFeatureGenerator;
import Reika.Satisforestry.Biome.Biomewide.MantaGenerator.MantaPath;

public class EntityFlyingManta extends EntityFlying {

	private WorldLocation pathRoot;
	private List<DecimalPosition> pathSpline;

	private int splineIndex;

	public EntityFlyingManta(World w) {
		super(w);
		noClip = true;
	}

	public void setPath(MantaPath path) {
		if (path == null) {
			pathRoot = null;
			this.setDead();
			Satisforestry.logger.logError("Manta "+this+" had no path, removing");
			return;
		}
		pathRoot = path.biomeCenter;
		pathSpline = path.getSpline();
		splineIndex = 0;
		this.setPathPosition();
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (!worldObj.isRemote) {
			if (pathRoot == null) {
				this.setPath(null);
				return;
			}

			if (pathSpline == null) {
				MantaPath path = BiomewideFeatureGenerator.instance.getPathAround(pathRoot);
				if (pathRoot != null && path == null)
					Satisforestry.logger.logError("Could not reload manta path for "+this);
				this.setPath(path);
				return;
			}

			splineIndex = (splineIndex+1)%pathSpline.size();
			this.setPathPosition();
		}
	}

	private void setPathPosition() {
		DecimalPosition pos = pathSpline.get(splineIndex);
		this.setPosition(pos.xCoord, pos.yCoord, pos.zCoord);

		DecimalPosition posNext = pathSpline.get((splineIndex+1)%pathSpline.size());

		double[] angs = ReikaPhysicsHelper.cartesianToPolar(posNext.xCoord-pos.xCoord, posNext.yCoord-pos.yCoord, posNext.zCoord-pos.zCoord);
		rotationYaw = (float)angs[2];
		rotationPitch = (float)angs[1];
		//ReikaJavaLibrary.pConsole(this);
	}

	@Override
	public boolean attackEntityFrom(DamageSource src, float amt) {
		return false;
	}

	@Override
	public boolean isEntityInvulnerable() {
		return true;
	}

	@Override
	public void playLivingSound() {

	}

	@Override
	public int getTalkInterval() {
		return 120;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound NBT) {
		super.readEntityFromNBT(NBT);

		pathRoot = WorldLocation.readFromNBT("path", NBT);
		splineIndex = NBT.getInteger("pathPos");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound NBT) {
		super.writeEntityToNBT(NBT);

		pathRoot.writeToNBT("path", NBT);
		NBT.setInteger("pathPos", splineIndex);
	}

}
