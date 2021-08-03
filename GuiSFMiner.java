package Reika.Satisforestry;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;

import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.Satisforestry.Blocks.TileNodeHarvester;

public class GuiSFMiner extends GuiContainer {

	private final TileNodeHarvester tile;
	private final EntityPlayer player;

	public GuiSFMiner(EntityPlayer ep, TileNodeHarvester te) {
		super(new ContainerSFMiner(ep, te));
		tile = te;
		player = ep;
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

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;

		String i = "/Reika/Satisforestry/Textures/minergui.png";
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ReikaTextureHelper.bindTexture(Satisforestry.class, i);
		this.drawTexturedModalRect(j, k, 0, 0, xSize, ySize);

		int i1 = Math.min(48, tile.getMineProgressScaled(48));
		this.drawTexturedModalRect(j + 99, k + 34, 176, 14, 1*(i1)+1, 16);
	}

}
