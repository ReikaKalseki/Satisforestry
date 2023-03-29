package Reika.Satisforestry.AlternateRecipes;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import Reika.DragonAPI.Instantiable.GUI.CustomSoundGuiButton.CustomSoundGui;
import Reika.DragonAPI.Instantiable.GUI.CustomSoundGuiButton.CustomSoundImagedGuiButton;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.DragonAPI.Libraries.Rendering.ReikaGuiAPI;
import Reika.Satisforestry.SFPacketHandler.SFPackets;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Blocks.BlockCrashSite.TileCrashSite;
import Reika.Satisforestry.Config.AlternateRecipe;
import Reika.Satisforestry.Registry.SFBlocks;
import Reika.Satisforestry.Registry.SFSounds;

public class GuiCrashSite extends GuiContainer implements CustomSoundGui {

	protected final TileCrashSite tile;
	protected final EntityPlayer player;

	private static final String TEXTURE = "/Reika/Satisforestry/Textures/crashgui.png";

	public GuiCrashSite(EntityPlayer ep, TileCrashSite te) {
		super(new ContainerCrashSite(ep, te));
		tile = te;
		player = ep;

		xSize = 176;
		ySize = 186;
	}

	@Override
	public void initGui() {
		super.initGui();

		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		buttonList.add(new CrashSiteButton(0, j+139, k+47, 16, 16, 182, 220));
	}

	@Override
	protected void actionPerformed(GuiButton b) {
		super.actionPerformed(b);

		ReikaPacketHelper.sendPacketToServer(Satisforestry.packetChannel, SFPackets.CRASHUNLOCK.ordinal(), tile);
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
		ReikaTextureHelper.bindTexture(Satisforestry.class, TEXTURE);
		this.drawTexturedModalRect(j, k, 0, 0, xSize, ySize);

		AlternateRecipe r = tile.getRecipe();

		if (r != null) {
			if (r.unlockPower != null) {
				Tessellator v5 = Tessellator.instance;
				double ox = j+69.5;
				double oy = k+81.5;

				double u = 130.5;
				double v = 230.5;
				GL11.glDisable(GL11.GL_CULL_FACE);
				v5.startDrawing(GL11.GL_TRIANGLE_FAN);
				v5.setColorOpaque_I(0xffffff);
				v5.addVertexWithUV(ox, oy, 0, u/256D, v/256D);
				double ma = 360*tile.progressFactor;
				double da = 0.25;
				double s = 11;
				for (double a = 0; a < ma; a += da) {
					double dx = Math.sin(Math.toRadians(-a+90));
					double dy = Math.cos(Math.toRadians(-a+90));
					double x = ox+s*dx;
					double y = oy+s*dy;
					double du = u+dx*s;
					double dv = v+dy*s;
					//ReikaJavaLibrary.pConsole(a+">"+x+","+y+" @ "+du+","+dv+" from "+u+","+v);
					v5.addVertexWithUV(x, y, 0, du/256D, dv/256D);
				}
				v5.draw();
				GL11.glEnable(GL11.GL_CULL_FACE);
			}
			else {
				this.drawTexturedModalRect(j+59, k+71, 96, 220, 22, 22);
			}
			if (r.getRequiredItem() == null) {
				this.drawTexturedModalRect(j+23, k+71, 96, 220, 22, 22);
			}
		}

		ReikaTextureHelper.bindFontTexture();

		fontRendererObj.drawString(SFBlocks.getFromID(tile.getBlockType()).getBasicName(), j+6, k+5, 0xffffff);

		if (r != null) {
			ReikaGuiAPI.instance.drawCenteredStringNoShadow(fontRendererObj, r.getDisplayName(), j+53, k+23, 0xFA9549);
			GL11.glPushMatrix();
			GL11.glScaled(0.5, 0.5, 1);
			if (r.unlockPower != null)
				ReikaGuiAPI.instance.drawCenteredStringNoShadow(fontRendererObj, "Requires "+r.unlockPower.getDisplayString(), (j+53)*2, (k+32)*2, 0x646464);
			ItemStack is = r.getRequiredItem();
			if (is != null)
				ReikaGuiAPI.instance.drawCenteredStringNoShadow(fontRendererObj, "Requires "+is.stackSize+" "+is.getDisplayName(), (j+53)*2, (k+38)*2, 0x646464);
			boolean met = tile.isRequirementMet();
			String s = met ? "Press Button To Unlock" : "Requirements Not Met";
			((CrashSiteButton)buttonList.get(0)).enabled = met;
			ReikaGuiAPI.instance.drawCenteredStringNoShadow(fontRendererObj, s, (j+53)*2, (k+53)*2, 0x646464);
			GL11.glPopMatrix();
		}
		else {
			((CrashSiteButton)buttonList.get(0)).enabled = false;
		}
	}

	private class CrashSiteButton extends CustomSoundImagedGuiButton {

		public CrashSiteButton(int par1, int par2, int par3, int par4, int par5, int par7, int par8) {
			super(par1, par2, par3, par4, par5, par7, par8, TEXTURE, Satisforestry.class, GuiCrashSite.this);
		}

		@Override
		protected void getHoveredTextureCoordinates() {
			v += 17;
		}

		@Override
		protected void modifyTextureUV() {
			if (this.isClicked())
				u += 19;
		}

		@Override
		protected void onFailedClick() {
			ReikaSoundHelper.playClientSound(SFSounds.CRASHFAIL, player, 1, 1);
		}
	}

	@Override
	public void playButtonSound(GuiButton b) {
		//silence
	}

	@Override
	public void playHoverSound(GuiButton b) {

	}

}
