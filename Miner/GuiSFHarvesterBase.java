package Reika.Satisforestry.Miner;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;

import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.DragonAPI.Libraries.Rendering.ReikaGuiAPI;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockResourceNode.ResourceNode;
import Reika.Satisforestry.Config.NodeResource;
import Reika.Satisforestry.Registry.SFBlocks;

public abstract class GuiSFHarvesterBase<T extends TileResourceHarvesterBase> extends GuiContainer {

	protected final T tile;
	protected final EntityPlayer player;

	public GuiSFHarvesterBase(EntityPlayer ep, T te) {
		super(new ContainerSFMiner(ep, te));
		tile = te;
		player = ep;

		xSize = 176;
		ySize = 219;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int a, int b) {
		//int j = (width - xSize) / 2;
		//int k = (height - ySize) / 2;

		super.drawGuiContainerForegroundLayer(a, b);
		/*
		fontRendererObj.drawString("Lubricant", 5, 11, 4210752);

		if (api.isMouseInBox(j+23, j+32, k+20, k+76)) {
			int mx = api.getMouseRealX();
			int my = api.getMouseRealY();
			api.drawTooltipAt(fontRendererObj, String.format("%d/%d", grin.getLevel(), grin.MAXLUBE), mx-j, my-k);
		}*/
	}

	protected abstract String getTextureName();

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ReikaTextureHelper.bindTexture(Satisforestry.class, "/Reika/Satisforestry/Textures/"+this.getTextureName()+".png");
		this.drawTexturedModalRect(j, k, 0, 0, xSize, ySize);

		int f = Math.min(74, tile.getMineProgressScaled(74));
		this.drawTexturedModalRect(j + 56, k + 21, 0, 220, f, 6);

		f = (int)Math.min(74, tile.powerBar*74);
		this.drawTexturedModalRect(j + 56, k + 38, 0, 230, f, 6);

		int o = tile.getOverclockingStep(true);
		int dx = 0;
		for (int i = 0; i <= o; i++) {
			int w = i == 0 ? 43 : 21;
			this.drawTexturedModalRect(j+28+dx, k+122, dx, 243, w, 2);
			dx += w+4;
			if (i > 0 && tile.getUpgradeSlot(i) == null) {
				this.drawTexturedModalRect(j+80+25*(i-1), k+104, 79, 220, 16, 16);
			}
		}

		ResourceNode te = tile.getResourceNode();
		this.drawExtraGuiElements(te, j, k);

		ReikaTextureHelper.bindFontTexture();

		fontRendererObj.drawString(SFBlocks.getFromID(tile.getBlockType()).getBasicName(), j+6, k+5, 0xffffff);

		GL11.glPushMatrix();
		GL11.glScaled(0.5, 0.5, 0.5);
		ReikaGuiAPI.instance.drawCenteredStringNoShadow(fontRendererObj, tile.getOperationPowerCost(true), (j+90)*2, (k+39)*2, 0xFA9549);
		GL11.glPopMatrix();

		if (te != null) {
			NodeResource ri = te.getResource();
			int baseyield = (int)(20*60*this.getNodeBaseYieldPerTick(ri, te));
			float sp = tile.getNetSpeedFactor(true);

			ReikaGuiAPI.instance.drawCenteredStringNoShadow(fontRendererObj, ri.getDisplayName(), j+90, k+76, 0x646464);
			ReikaGuiAPI.instance.drawCenteredStringNoShadow(fontRendererObj, baseyield+"/min Base", j+90, k+76+fontRendererObj.FONT_HEIGHT, 0x646464);

			fontRendererObj.drawString(String.format("%.0f%s", tile.getOverclockingLevel(true)*100, "%"), j+27, k+103, 0xFA9549);
			String s = String.format("%.1f", baseyield*sp);
			fontRendererObj.drawString(s, j+27, k+114, 0xFA9549);
			fontRendererObj.drawString("/min", j+27+fontRendererObj.getStringWidth(s), k+114, 0x646464);
		}
	}

	protected void drawExtraGuiElements(ResourceNode te, int j, int k) {

	}

	/** Includes purity */
	protected abstract float getNodeBaseYieldPerTick(NodeResource ri, ResourceNode te);

}
