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

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ReikaTextureHelper.bindTexture(Satisforestry.class, "/Reika/Satisforestry/Textures/minergui.png");
		this.drawTexturedModalRect(j, k, 0, 0, xSize, ySize);

		int f = Math.min(76, tile.getMineProgressScaled(76));
		this.drawTexturedModalRect(j + 50, k + 27, 0, 220, f, 6);

		f = (int)Math.min(64, tile.powerBar*64);
		this.drawTexturedModalRect(j + 56, k + 37, 0, 230, f, 6);

		int o = tile.getOverclockingStep();
		int dx = 0;
		for (int i = 0; i <= o; i++) {
			int w = i == 0 ? 43 : 21;
			this.drawTexturedModalRect(j + 28+dx, k + 121, dx, 242, w, 3);
			dx += w+4;
		}
	}

}
