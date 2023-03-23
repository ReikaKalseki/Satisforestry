package Reika.Satisforestry.Miner;

import java.util.ArrayList;
import java.util.Collections;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.Satisforestry.Blocks.BlockFrackingAux.TileFrackingAux;
import Reika.Satisforestry.Blocks.BlockFrackingNode.TileFrackingNode;
import Reika.Satisforestry.Blocks.BlockResourceNode.ResourceNode;
import Reika.Satisforestry.Config.NodeResource;
import Reika.Satisforestry.Config.ResourceFluid;

public class GuiSFFracker extends GuiSFHarvesterBase<TileFrackingPressurizer> {

	private final ArrayList<TileFrackingAux> satellites = new ArrayList();
	public GuiSFFracker(EntityPlayer ep, TileFrackingPressurizer te) {
		super(ep, te);

		TileFrackingNode n = te.getResourceNode();
		if (n != null) {
			for (TileFrackingAux c : n.getSatelliteNodes()) {
				satellites.add(c);
			}
			Collections.sort(satellites, (n1, n2) -> n1.getPurity().compareTo(n2.getPurity()));
		}
	}

	@Override
	protected void drawExtraGuiElements(ResourceNode te, int j, int k) {
		if (te != null) {
			ResourceFluid rf = (ResourceFluid)te.getResource();
			//ReikaGuiAPI.instance.drawCenteredStringNoShadow(fontRendererObj, min"x", j+104, k+55, 0x646464);
			//ReikaGuiAPI.instance.drawCenteredStringNoShadow(fontRendererObj, max"x", j+104, k+75, 0x646464);

			ArrayList<TileFrackingAux> li = new ArrayList(satellites);
			for (int i = 0; i < 8; i++) {
				TileFrackingAux n = li.isEmpty() ? null : li.remove(0);
				int u = 96;
				int v = 220;
				if (n == null) {
					v = 228;
				}
				else {
					u = 104+n.getPurity().ordinal()*8;
					v = n.hasExtractor() ? 228 : 220;
				}
				this.drawTexturedModalRect(j+58+i*9, k+29, u, v, 7, 7);
			}

			ReikaTextureHelper.bindTerrainTexture();
			GL11.glColor4f(1, 1, 1, 1);
			Fluid fl = rf.getFluid();
			this.drawTexturedModelRectFromIcon(j+80, k+55, fl.getIcon(new FluidStack(fl, 1000)), 16, 16);

		}
	}

	@Override
	protected String getTextureName() {
		return "frackergui";
	}

	@Override
	protected float getNodeBaseYieldPerTick(NodeResource ri, ResourceNode te) {
		int[] minMax = ((ResourceFluid)ri).getBaseMinMax(te.getPurity(), te.worldObj.difficultySetting == EnumDifficulty.PEACEFUL);
		return (minMax[0]+minMax[1])/2000F;
	}

}
