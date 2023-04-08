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
import Reika.DragonAPI.Instantiable.Math.Spline.SplineType;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.Satisforestry.Blocks.BlockFrackingAux.TileFrackingAux;
import Reika.Satisforestry.Blocks.BlockFrackingNode;
import Reika.Satisforestry.Blocks.BlockFrackingNode.TileFrackingNode;
import Reika.Satisforestry.Config.ResourceFluid;


public class FrackingNodeAuxRenderer extends FrackingNodeRenderer {

	public static TileFrackingNode renderDelegateMaster;

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
			root = renderDelegateMaster;
		if (root == null)
			return flag;
		ResourceFluid ri = root.getResource();
		int c = ri == null ? 0xffffff : ri.color;
		if (renderPass == 0)
			v5.setColorOpaque_I(0xffffff);
		else
			v5.setColorOpaque_I(c);
		if (renderPass == 1 && ri != null && ri.glowAtNight)
			v5.setBrightness(240);
		else
			v5.setBrightness(block.getMixedBrightnessForBlock(world, x, y+1, z));

		rand.setSeed(this.calcSeed(x, y, z));
		rand.nextBoolean();

		double x1 = x+0.5;
		double z1 = z+0.5;
		double x2 = root.xCoord+0.5;
		double z2 = root.zCoord+0.5;

		int n = 5;
		LightningBolt b = new LightningBolt(new DecimalPosition(root), new DecimalPosition(x+0.5, y+0.5, z+0.5), n);
		b.setRandom(rand).setVariance(1).maximize();

		List<DecimalPosition> li = b.spline(SplineType.CENTRIPETAL, (int)(ReikaMathLibrary.py3d(root.xCoord-x, 0, root.zCoord-z)*1));
		for (DecimalPosition d : li) {
			if (d.getDistanceTo(root.xCoord+0.5, d.yCoord, root.zCoord+0.5) <= 2.5)
				continue;
			if (d.getDistanceTo(x+0.5, d.yCoord, z+0.5) <= 1.0)
				continue;
			double dx = ReikaRandomHelper.getRandomPlusMinus(d.xCoord-0.5, 0.125, rand);
			double dz = ReikaRandomHelper.getRandomPlusMinus(d.zCoord-0.5, 0.125, rand);
			double ds = ReikaRandomHelper.getRandomBetween(0.9, 1.2, rand);
			int idx = renderPass == 0 ? 0 : (int)((Double.doubleToRawLongBits(dx)^Double.doubleToRawLongBits(dz))%5);
			IIcon ico = renderPass == 0 ? block.blockIcon : BlockFrackingNode.getOverlay(((idx%5+5)%5)+5);
			this.renderWedgePie(dx, y+renderPass*0.005, dz, 6, 0.03125, 0.5*ds, 0, 0.25*ds, 0, 1, v5, ico);
		}

		return true;
	}

	@Override
	protected double getRadiusScale() {
		return 0.67;
	}

}
