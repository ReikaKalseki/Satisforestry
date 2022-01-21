package Reika.Satisforestry.Entity.AI;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.ModInteract.Bees.ReikaBeeHelper;
import Reika.Satisforestry.SFBees;
import Reika.Satisforestry.Biome.DecoratorPinkForest;
import Reika.Satisforestry.Registry.SFOptions;

import forestry.api.genetics.IGenome;
import forestry.api.genetics.IIndividual;
import forestry.api.lepidopterology.IEntityButterfly;

public abstract class EntityAIButterflyPaleberry extends EntityAIBase {

	public final IEntityButterfly butterfly;
	protected final World world;

	protected Coordinate target;

	public EntityAIButterflyPaleberry(IEntityButterfly b) {
		butterfly = b;
		world = b.getEntity().worldObj;
		this.setMutexBits(63);
	}

	@Override
	public final boolean shouldExecute() {
		if (SFOptions.PALEBERRYPOLLEN.getState() && this.needsPollen() && !this.checkPollen()) {
			return false;
		}
		target = this.findTarget(world);
		//ReikaJavaLibrary.pConsole(target+" from "+butterfly);
		return target != null && this.isValidToStart();
	}

	protected abstract boolean isValidToStart();

	protected abstract boolean needsPollen();

	protected final boolean checkPollen() {
		IIndividual p = butterfly.getPollen();
		if (p == null)
			return false;
		IGenome gen = p.getGenome();
		if (gen == null)
			return false;
		return SFBees.getPinkTree().equals(gen.getPrimary());
	}

	protected final Coordinate findTarget(World world) {
		if (target != null && this.isValidTarget(target.xCoord, target.yCoord, target.zCoord))
			return target;
		int x = MathHelper.floor_double(butterfly.getEntity().posX);
		//int y = MathHelper.floor_double(this.butterfly.getEntity().posY);
		int z = MathHelper.floor_double(butterfly.getEntity().posZ);
		for (int dx = x-12; dx <= x+13; dx++) {
			for (int dz = z-12; dz <= z+13; dz++) {
				int y = DecoratorPinkForest.getTrueTopAt(world, dx, dz);
				int by = y+2;
				if (this.isValidTarget(dx, by, dz))
					return new Coordinate(dx, by, dz);
			}
		}
		return null;
	}

	protected abstract boolean isValidTarget(int x, int y, int z);

	@Override
	public final void startExecuting() {
		//butterfly.getEntity().getNavigator().clearPathEntity();
		//if (butterfly.getEntity().getNavigator().tryMoveToXYZ(target.xCoord, target.yCoord+0.25, target.zCoord, butterfly.getEntity().getAIMoveSpeed()*1.5F))
		//ReikaJavaLibrary.pConsole(world.getTotalWorldTime()+": Pathing to "+target+" @ "+butterfly);
		ReikaBeeHelper.setButterflyTarget(butterfly, Vec3.createVectorHelper(target.xCoord+0.5, target.yCoord+1, target.zCoord+0.5));
		//else
		//	ReikaJavaLibrary.pConsole(world.getTotalWorldTime()+": Failed to path");
	}

	@Override
	public void updateTask() {
		super.updateTask();
		EntityCreature e = butterfly.getEntity();
		double dx = (target.xCoord+0.5-e.posX);
		double dy = (target.yCoord+1.125-e.posY);
		double dz = (target.zCoord+0.5-e.posZ);
		double dd = ReikaMathLibrary.py3d(dx, dy, dz);
		double vxz = 0.2;
		double vy = 0.4;
		e.motionX = dx/dd*vxz;
		e.motionZ = dz/dd*vxz;
		e.motionY = dy/dd*vy;
		e.velocityChanged = true;
		e.rotationYaw = e.rotationYawHead = (float)Math.toDegrees(Math.atan2(e.motionZ, e.motionX));
	}

	@Override
	public final void resetTask() {
		super.resetTask();
		//ReikaJavaLibrary.pConsole(world.getTotalWorldTime()+": Resetting from "+target+" @ "+butterfly);
		ReikaBeeHelper.setButterflyTarget(butterfly, null);
	}

}
