package Reika.Satisforestry.Entity;

import java.util.Collection;
import java.util.List;

import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;

import Reika.DragonAPI.Auxiliary.ChunkManager;
import Reika.DragonAPI.Instantiable.Data.Immutable.DecimalPosition;
import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.DragonAPI.Interfaces.Entity.ChunkLoadingEntity;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaPhysicsHelper;
import Reika.Satisforestry.SFSounds;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.Biomewide.BiomewideFeatureGenerator;
import Reika.Satisforestry.Biome.Biomewide.MantaGenerator.MantaPath;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityFlyingManta extends EntityFlying implements ChunkLoadingEntity {

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

		for (DecimalPosition p : pathSpline) {
			p.setBlock(worldObj, Blocks.diamond_block);
		}

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

			ChunkManager.instance.loadChunks(this);

			if (pathSpline == null) {
				MantaPath path = BiomewideFeatureGenerator.instance.getPathAround(worldObj, pathRoot);
				if (pathRoot != null && path == null)
					Satisforestry.logger.logError("Could not reload manta path for "+this);
				this.setPath(path);
				return;
			}

			splineIndex = (splineIndex+1)%pathSpline.size();
			this.setPathPosition();
		}

		if (worldObj.isRemote) {
			this.doFX();
		}
	}

	@SideOnly(Side.CLIENT)
	private void doFX() {
		if (rand.nextInt(ridingEntity != null ? 200 : 800) == 0) {
			SFSounds s = SFSounds.MANTA1;
			switch(rand.nextInt(4)) {
				case 1:
					s = SFSounds.MANTA2;
					break;
				case 2:
					s = SFSounds.MANTA3;
					break;
				case 3:
					s = SFSounds.MANTA4;
					break;
			}
			float p = 0.8F+rand.nextFloat()*0.4F;
			ReikaSoundHelper.playClientSound(s, posX, posY, posZ, 2, p, false);
			ReikaSoundHelper.playClientSound(s, posX, posY, posZ, 2, p, false);
			ReikaSoundHelper.playClientSound(s, posX, posY, posZ, 2, p, false);
		}
		/*
		Vec3 left = this.getLookVec();
		Vec3 look = ReikaVectorHelper.rotateVector(left, -90, 0, 0);
		double dl = 3.5;
		EntityBlurFX fx = new EntityBlurFX(worldObj, posX+look.xCoord*dl, posY-2, posZ+look.zCoord*dl, IconPrefabs.FADE.getIcon());
		int c1 = 0x52B6FF;
		int c2 = 0xA8DAFF;
		int c = ReikaColorAPI.mixColors(c1, c2, rand.nextFloat());
		int l = ReikaRandomHelper.getRandomBetween(20, 90);
		float s = (float)ReikaRandomHelper.getRandomBetween(2.5, 5);
		fx.setColor(c).setRapidExpand().setLife(l).setScale(s);
		Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		 */
	}

	private void setPathPosition() {
		DecimalPosition pos = pathSpline.get(splineIndex);
		this.setPosition(pos.xCoord, pos.yCoord, pos.zCoord);

		DecimalPosition posNext = pathSpline.get((splineIndex+1)%pathSpline.size());

		double dx = posNext.xCoord-pos.xCoord;
		double dy = posNext.yCoord-pos.yCoord;
		double dz = posNext.zCoord-pos.zCoord;

		float f = (float)Math.max(0, MAX_WING_DEFLECTION*Math.min(1, 1+dy*7.5));
		dataWatcher.updateObject(30, f);

		double[] angs = ReikaPhysicsHelper.cartesianToPolar(dx, dy, dz);
		rotationYaw = 90-(float)angs[2]-45;
		rotationPitch = -(float)angs[1]+90;
		//ReikaJavaLibrary.pConsole(this);
	}

	@Override
	public void setDead() {
		super.setDead();
		this.onDestroy();
	}

	@Override
	protected void despawnEntity() {

	}

	public void onDestroy() {
		ChunkManager.instance.unloadChunks(this);
	}

	public Collection<ChunkCoordIntPair> getChunksToLoad() {
		return ChunkManager.instance.getChunkSquare(MathHelper.floor_double(posX), MathHelper.floor_double(posZ), 2);
	}

	@Override
	public boolean isInRangeToRenderDist(double distsq) {
		return true;
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
				return false;
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
