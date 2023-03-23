package Reika.Satisforestry.Miner;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.EntityPlayer;

import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.Satisforestry.Blocks.BlockResourceNode.ResourceNode;
import Reika.Satisforestry.Blocks.BlockResourceNode.TileResourceNode;
import Reika.Satisforestry.Config.NodeResource;
import Reika.Satisforestry.Config.ResourceItem;

public class GuiSFMiner extends GuiSFHarvesterBase<TileNodeHarvester> {

	public GuiSFMiner(EntityPlayer ep, TileNodeHarvester te) {
		super(ep, te);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
		super.drawGuiContainerBackgroundLayer(par1, par2, par3);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;

		TileResourceNode te = tile.getResourceNode();
		if (te != null) {
			ReikaTextureHelper.bindFontTexture();
			GL11.glColor4f(1, 1, 1, 1);
			ResourceItem ri = te.getResource();
			//ReikaGuiAPI.instance.drawCenteredStringNoShadow(fontRendererObj, min"x", j+104, k+55, 0x646464);
			//ReikaGuiAPI.instance.drawCenteredStringNoShadow(fontRendererObj, max"x", j+104, k+75, 0x646464);
			float time = te.getHarvestInterval()/tile.getNetSpeedFactor(true);
			fontRendererObj.drawString(String.format("%.2fs", time/20F), j+83, k+29, 0xFA9549);
		}
	}

	@Override
	protected String getTextureName() {
		return "minergui";
	}

	@Override
	protected float getNodeBaseYieldPerTick(NodeResource r, ResourceNode te) {
		return 1F/te.getHarvestInterval();
	}

}
