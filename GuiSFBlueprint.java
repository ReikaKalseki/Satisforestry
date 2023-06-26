package Reika.Satisforestry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Exception.UnreachableCodeException;
import Reika.DragonAPI.Instantiable.Data.BlockStruct.FilledBlockArray;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Data.Maps.ItemHashMap;
import Reika.DragonAPI.Instantiable.GUI.CustomSoundGuiButton.CustomSoundGui;
import Reika.DragonAPI.Instantiable.GUI.CustomSoundGuiButton.CustomSoundImagedGuiButton;
import Reika.DragonAPI.Instantiable.Rendering.StructureRenderer;
import Reika.DragonAPI.Instantiable.Rendering.StructureRenderer.BlockChoiceHook;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.DragonAPI.Libraries.Java.ReikaGLHelper.BlendMode;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.Libraries.Rendering.ReikaColorAPI;
import Reika.DragonAPI.Libraries.Rendering.ReikaGuiAPI;
import Reika.DragonAPI.Libraries.Rendering.ReikaRenderHelper;
import Reika.Satisforestry.Blocks.BlockFrackingPressurizer.TileFrackingExtractor;
import Reika.Satisforestry.Miner.FrackerStructure;
import Reika.Satisforestry.Miner.MinerStructure;
import Reika.Satisforestry.Registry.SFBlocks;
import Reika.Satisforestry.Registry.SFSounds;

public class GuiSFBlueprint extends GuiScreen implements CustomSoundGui {

	protected final EntityPlayer player;

	private static final String TEXTURE = "/Reika/Satisforestry/Textures/bpgui.png";

	protected int xSize;
	protected int ySize;

	private int mode = 0;
	private int tick = 0;

	private final FilledBlockArray array;
	private final StructureRenderer render;
	private final String title;

	private int buttonCooldown = 0;

	private static boolean guiClosed = false;

	public static boolean renderTESR;

	private final class PowerTypeChoice implements BlockChoiceHook {

		private final Block block;

		private PowerTypeChoice(Block b) {
			block = b;
		}

		@Override
		public ItemStack getBlock(Coordinate pos, ItemStack orig) {
			if (orig.getItemDamage() > 2)
				return orig;
			return new ItemStack(block, 1, (tick/100)%3);
		}

	};

	private final BlockChoiceHook hideIfShift = new BlockChoiceHook() {

		@Override
		public ItemStack getBlock(Coordinate pos, ItemStack orig) {
			return mode == 2 ? null : orig;
		}

	};

	private final class ModeButton extends CustomSoundImagedGuiButton {

		private final int iconU;

		public ModeButton(int id, int x, int y, int w, int h, int iconU) {
			super(id, x, y, w, h, iconU, 220, TEXTURE, Satisforestry.class, GuiSFBlueprint.this);
			this.iconU = iconU;
		}

		@Override
		protected void renderButton() {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
			ReikaRenderHelper.prepareGeoDraw(false);
			int c = mode == id ? 0x666666 : ReikaColorAPI.mixColors(0x404040, 0x262626, this.getHoverFade());
			Tessellator v5 = Tessellator.instance;
			v5.startDrawingQuads();
			v5.setColorRGBA_I(c, 255);
			v5.addVertex(xPosition, yPosition+height, zLevel);
			v5.addVertex(xPosition+width, yPosition+height, zLevel);
			v5.addVertex(xPosition+width, yPosition, zLevel);
			v5.addVertex(xPosition, yPosition, zLevel);
			v5.draw();
			GL11.glPopAttrib();
			GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
			GL11.glEnable(GL11.GL_BLEND);
			BlendMode.DEFAULT.apply();
			ReikaTextureHelper.bindTexture(modClass, this.getButtonTexture());
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.drawTexturedModalRect(xPosition+width/2-16, yPosition+height/2-16, iconU, 220, 32, 32);
			GL11.glPopAttrib();
		}

	}

	public GuiSFBlueprint(EntityPlayer ep, int struct) {
		player = ep;

		guiClosed = false;

		xSize = 248;
		ySize = 220;

		switch(struct) {
			case 0:
				array = MinerStructure.getMinerStructure(ep.worldObj, 0, 0, 0, ForgeDirection.EAST);
				array.setBlock(0, 0, 0, SFBlocks.HARVESTER.getBlockInstance(), 0);
				title = SFBlocks.HARVESTER.getBasicName();
				break;
			case 1:
				array = FrackerStructure.getFrackerStructure(ep.worldObj, 0, 0, 0);
				array.setBlock(0, 0, 0, SFBlocks.FRACKER.getBlockInstance(), 0);
				title = SFBlocks.FRACKER.getBasicName();
				break;
			case 2:
				array = TileFrackingExtractor.getStructure(ep.worldObj, 0, 0, 0);
				array.setBlock(0, 0, 0, SFBlocks.FRACKER.getBlockInstance(), 3);
				title = SFBlocks.FRACKER.getMultiValuedName(3);
				break;
			default:
				throw new UnreachableCodeException();
		}

		render = new StructureRenderer(array);
		render.addBlockHook(SFBlocks.HARVESTER.getBlockInstance(), new PowerTypeChoice(SFBlocks.HARVESTER.getBlockInstance()));
		render.addBlockHook(SFBlocks.FRACKER.getBlockInstance(), new PowerTypeChoice(SFBlocks.FRACKER.getBlockInstance()));

		render.addBlockHook(SFBlocks.MINERMULTI.getBlockInstance(), hideIfShift);
		render.addBlockHook(SFBlocks.FRACKERMULTI.getBlockInstance(), hideIfShift);
		render.addBlockHook(Blocks.redstone_lamp, hideIfShift);
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		render.resetRotation();
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;

		int x0 = 5;
		int gap0 = 0;
		buttonList.add(new ModeButton(0, j+x0, k+15, 51, 33, 0));
		buttonList.add(new ModeButton(1, j+x0+gap0+51, k+15, 51, 33, 32));
		buttonList.add(new ModeButton(2, j+x0+gap0*2+102, k+15, 51, 33, 64));
		buttonList.add(new CustomSoundImagedGuiButton(5, j+219, k+0, 29, 15, 219, 0, TEXTURE, Satisforestry.class, this) {
			@Override
			protected void renderButton() {
				super.renderButton();
				int a = (int)(this.getHoverFade()*255);
				if (a > 0) {
					GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
					GL11.glEnable(GL11.GL_BLEND);
					BlendMode.DEFAULT.apply();
					this.drawTexturedModalRect(xPosition, yPosition, 209, 240, width, height, 0xffffff, a);
					GL11.glPopAttrib();
				}
			}
		});
		//buttonList.add(new CustomSoundImagedGuiButton(4, mode == 1 ? j+125 : j+165, k+8, 20, 20, "N#"));

		if (mode == 1) {
			//buttonList.add(new CustomSoundImagedGuiButton(2, j+165, k+8, 20, 20, "+"));
			//buttonList.add(new CustomSoundImagedGuiButton(3, j+145, k+8, 20, 20, "-"));
			int n = array.getSizeY();
			int gap = 2;
			int w = (xSize-gap*n)/n;
			int o = (xSize-n*(w+gap))/2;
			for (int i = 0; i < n; i++) {
				int dx = i*(w+gap)+o;
				boolean sel = render.getCurrentSlice() == i;
				final int layer = i;
				CustomSoundImagedGuiButton b = new CustomSoundImagedGuiButton(100+i, j+dx, k+48, w, 30, 212, 225, TEXTURE, Satisforestry.class, this) {
					@Override
					protected void renderButton() {
						GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
						GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
						ReikaRenderHelper.prepareGeoDraw(false);
						int c = sel ? 0xCBCBCB : ReikaColorAPI.mixColors(0x909090, 0x747474, this.getHoverFade());
						Tessellator v5 = Tessellator.instance;
						v5.startDrawingQuads();
						v5.setColorRGBA_I(c, 255);
						v5.addVertex(xPosition, yPosition+height, zLevel);
						v5.addVertex(xPosition+width, yPosition+height, zLevel);
						v5.addVertex(xPosition+width, yPosition, zLevel);
						v5.addVertex(xPosition, yPosition, zLevel);
						v5.draw();
						GL11.glPopAttrib();
						ReikaTextureHelper.bindFontTexture();
						c = sel ? 0xffffff : ReikaColorAPI.GStoHex(0x57*3/4); //why is there a *1.333 in the effective color in GuiAPI in this window?!
						ReikaGuiAPI.instance.drawCenteredStringNoShadow(renderer, String.format("%d", layer+1), this.getLabelX(), this.getLabelY(), c);
					}
				};
				buttonList.add(b);
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton b) {
		if (buttonCooldown > 0)
			return;

		super.actionPerformed(b);

		if (b.id >= 100) {
			render.setSlice(b.id-100);
			this.initGui();
		}
		else if (b.id == 0) {
			mode = 0;
			render.reset();
			this.initGui();
		}
		else if (b.id == 1) {
			mode = 1;
			this.initGui();
		}
		else if (b.id == 2) {
			mode = 2;
			this.initGui();
		}
		else if (b.id == 5) {
			guiClosed = true;
			player.closeScreen();
		}
		buttonCooldown = 10;
	}

	@Override
	public void onGuiClosed() {
		ReikaSoundHelper.playClientSound(SFSounds.GUICLICK, player, 0.67F, 1);
	}

	@Override
	public final void drawScreen(int mx, int my, float f) {
		if (buttonCooldown > 0)
			buttonCooldown--;

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ReikaTextureHelper.bindTexture(Satisforestry.class, TEXTURE);

		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;

		if (mode == 1) {
			this.drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
		}
		else {
			this.drawTexturedModalRect(j, k, 0, 0, xSize, 48);
			this.drawTexturedModalRect(j, k+48, 0, 87, xSize, 39);
			this.drawTexturedModalRect(j, k+87, 0, 87, xSize, ySize-87);
		}

		fontRendererObj.drawString(title+" ("+array.getSizeX()+"x"+array.getSizeY()+"x"+array.getSizeZ()+")", j+6, k+4, 0xffffff);

		tick++;

		//FilledBlockArray arr = page.getStructure().getStructureForDisplay();

		GL11.glPushMatrix();
		switch(mode) {
			case 0:
			case 2:
				this.draw3d(j, k, f);
				break;
			case 1:
				this.drawSlice(j, k);
				break;
		}
		GL11.glPopMatrix();
		this.drawTally(j, k);

		super.drawScreen(mx, my, f);
	}

	private void drawSlice(int j, int k) {
		boolean miner = title.equals(SFBlocks.HARVESTER.getBasicName());
		boolean fracker = title.equals(SFBlocks.FRACKER.getBasicName());
		render.drawSlice(miner ? j : (fracker ? j-3 : j-12), k+(miner ? 21 : (fracker ? 25 : 13)), fontRendererObj, miner ? 1 : (fracker ? 0.5 : 2));
	}

	private void drawTally(int j, int k) {
		ItemHashMap<Integer> map = array.tally(mode == 1 ? c -> c.yCoord-array.getMinY() == render.getCurrentSlice() : null);
		int i = 0;
		int n = 8;
		List<ItemStack> c = new ArrayList(map.keySet());
		Collections.sort(c, ReikaItemHelper.comparator);
		GL11.glPushMatrix();
		double s = 1.5;
		GL11.glScaled(s, s, s);
		int step = mode == 1 ? 30 : 24;
		for (ItemStack is : c) {
			int dx = (int)(j+xSize/2+(i-c.size()/2D)*step);
			int dy = k+186;
			ItemStack is2 = ReikaItemHelper.getSizedItemStack(is, map.get(is));
			int x0 = (int)(dx/s);
			int y0 = (int)(dy/s);
			if (mode == 1) {
				GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
				ReikaRenderHelper.prepareGeoDraw(false);
				Tessellator v5 = Tessellator.instance;
				v5.startDrawingQuads();
				v5.setColorRGBA_I(0xb0b0b0, 255);
				v5.addVertex(x0-1, y0+16, zLevel);
				v5.addVertex(x0+17, y0+16, zLevel);
				v5.addVertex(x0+17, y0, zLevel);
				v5.addVertex(x0-1, y0, zLevel);

				v5.addVertex(x0, y0+17, zLevel);
				v5.addVertex(x0+16, y0+17, zLevel);
				v5.addVertex(x0+16, y0-1, zLevel);
				v5.addVertex(x0, y0-1, zLevel);
				v5.draw();
				//ReikaGuiAPI.instance.drawRect(x0-1, y0, 18, 16, 0xffb0b0b0, false);
				//ReikaGuiAPI.instance.drawRect(x0, y0-1, 16, 18, 0xffb0b0b0, false);
				GL11.glPopAttrib();
			}
			ReikaGuiAPI.instance.drawItemStack(itemRender, fontRendererObj, is2, x0, y0, true);
			i++;
		}
		GL11.glPopMatrix();
	}

	private void draw3d(int j, int k, float ptick) {
		renderTESR = mode == 2;
		if (renderTESR) {
			RenderHelper.enableGUIStandardItemLighting();
			GL11.glColor4f(1, 1, 1, 1);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			int i1 = 240;
			int k1 = 240;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, i1 / 1.0F, k1 / 1.0F);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		}
		if (Mouse.isButtonDown(0) && tick > 2) {
			render.rotate(0.25*Mouse.getDY(), 0.25*Mouse.getDX(), 0);
		}
		else if (Mouse.isButtonDown(1)) {
			render.resetRotation();
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			render.rotate(0, 0.75, 0);
		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			render.rotate(0, -0.75, 0);
		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			render.rotate(-0.75, 0, 0);
		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			render.rotate(0.75, 0, 0);
		}

		boolean miner = title.equals(SFBlocks.HARVESTER.getBasicName());
		boolean fracker = title.equals(SFBlocks.FRACKER.getBasicName());
		render.draw3D(miner ? -12 : 0, miner || fracker ? -6 : 0, ptick, true, 0.75);
	}

	@Override
	public void playButtonSound(GuiButton b) {
		if (!guiClosed && b.id != 5)
			ReikaSoundHelper.playClientSound(SFSounds.GUICLICK, player, 0.67F, 1);
	}

	@Override
	public void playHoverSound(GuiButton b) {
		ReikaSoundHelper.playClientSound(SFSounds.GUISEL, player, 0.67F, 1);
	}

}
