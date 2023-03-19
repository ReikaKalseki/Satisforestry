/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.Satisforestry.Render;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import Reika.DragonAPI.Instantiable.Data.Immutable.DecimalPosition;
import Reika.DragonAPI.Instantiable.Effects.LightningBolt;
import Reika.DragonAPI.Instantiable.Math.Spline;
import Reika.DragonAPI.Instantiable.Math.Spline.BasicSplinePoint;
import Reika.DragonAPI.Instantiable.Math.Spline.SplineType;
import Reika.DragonAPI.Instantiable.Rendering.StructureRenderer;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.Satisforestry.Blocks.BlockFrackingAux.TileFrackingAux;
import Reika.Satisforestry.Blocks.BlockFrackingNode;
import Reika.Satisforestry.Blocks.BlockFrackingNode.TileFrackingNode;
import Reika.Satisforestry.Config.ResourceFluid;


public class FrackingNodeAuxRenderer extends FrackingNodeRenderer {

	public FrackingNodeAuxRenderer(int id) {
		super(id);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		boolean flag = super.renderWorldBlock(world, x, y, z, block, modelId, renderer);
		Tessellator v5 = Tessellator.instance;
		TileFrackingAux te = (TileFrackingAux)world.getTileEntity(x, y, z);
		TileFrackingNode root = te.getMaster();
		if (root == null)
			return flag;
		ResourceFluid ri = root.getResource();
		int c = ri == null ? 0xffffff : ri.color;
		if (renderPass == 0 || StructureRenderer.isRenderingTiles())
			v5.setColorOpaque_I(0xffffff);
		else
			v5.setColorOpaque_I(c);
		v5.setBrightness(block.getMixedBrightnessForBlock(world, x, y+1, z));

		rand.setSeed(this.calcSeed(x, y, z));
		rand.nextBoolean();

		double x1 = x+0.5;
		double z1 = z+0.5;
		double x2 = root.xCoord+0.5;
		double z2 = root.zCoord+0.5;

		int n = 5;
		LightningBolt b = new LightningBolt(new DecimalPosition(root), new DecimalPosition(x+0.5, y+0.5, z+0.5), n);

		b.setRandom(rand);
		b.setVariance(1);
		b.maximize();

		Spline path = new Spline(SplineType.CENTRIPETAL);
		for (int i = 0; i <= b.nsteps; i++) {
			path.addPoint(new BasicSplinePoint(b.getPosition(i)));
		}
		List<DecimalPosition> li = path.get((int)(ReikaMathLibrary.py3d(root.xCoord-x, 0, root.zCoord-z)*1), false);
		for (DecimalPosition d : li) {
			if (d.getDistanceTo(root.xCoord+0.5, d.yCoord, root.zCoord+0.5) <= 2.5)
				continue;
			double dx = ReikaRandomHelper.getRandomPlusMinus(d.xCoord-0.5, 0.125, rand);
			double dz = ReikaRandomHelper.getRandomPlusMinus(d.zCoord-0.5, 0.125, rand);
			double ds = ReikaRandomHelper.getRandomBetween(0.9, 1.2, rand);
			int idx = renderPass == 0 ? 0 : (int)((Double.doubleToRawLongBits(dx)^Double.doubleToRawLongBits(dz))%5);
			IIcon ico = renderPass == 0 ? block.blockIcon : BlockFrackingNode.getOverlay(idx+5);
			this.renderWedgePie(dx, y, dz, 6, 0.03125, 0.5*ds, 0, 0.25*ds, 0, 1, v5, ico);
		}

		return true;
	}

	@Override
	protected double getRadiusScale() {
		return 0.67;
	}

}
