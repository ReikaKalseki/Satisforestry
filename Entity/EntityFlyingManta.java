package Reika.Satisforestry.Entity;

import java.util.List;

import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.Immutable.DecimalPosition;
import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.DragonAPI.Libraries.MathSci.ReikaPhysicsHelper;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.Biomewide.BiomewideFeatureGenerator;
import Reika.Satisforestry.Biome.Biomewide.MantaGenerator.MantaPath;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityFlyingManta extends EntityFlying {

	public static final float MAX_WING_DEFLECTION = 1.5F;

	private WorldLocation pathRoot;
	private List<DecimalPosition> pathSpline;

	private int splineIndex;

	public EntityFlyingManta(World w) {
		super(w);
		noClip = true;
		this.setSize(7, 2);
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
	protected final void entityInit() {
		super.entityInit();
		//dataWatcher.addObject(28, 0F);
		//dataWatcher.addObject(29, 0F);
		dataWatcher.addObject(30, 0F);
	}

	@SideOnly(Side.CLIENT)
	public float getWingDeflection() {
		return dataWatcher.getWatchableObjectFloat(30);
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

		double dx = posNext.xCoord-pos.xCoord;
		double dy = posNext.yCoord-pos.yCoord;
		double dz = posNext.zCoord-pos.zCoord;

		dataWatcher.updateObject(30, MAX_WING_DEFLECTION);

		double[] angs = ReikaPhysicsHelper.cartesianToPolar(dx, dy, dz);
		rotationYaw = (float)angs[2];
		rotationPitch = (float)angs[1];
		//ReikaJavaLibrary.pConsole(this);
	}

	@Override
	public final boolean shouldRiderFaceForward(EntityPlayer player) {
		return true;
	}

	@Override
	public final double getMountedYOffset() {
		return 0.55;
	}

	@Override
	protected final boolean interact(EntityPlayer ep) {
		if (!worldObj.isRemote) {
			if (riddenByEntity != null && riddenByEntity.equals(ep)) {
				ep.dismountEntity(this);
			}
			else {
				ep.mountEntity(this);
			}
		}
		return true;
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
