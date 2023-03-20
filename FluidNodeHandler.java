/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.Satisforestry;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.Instantiable.Data.BlockStruct.FilledBlockArray;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Rendering.StructureRenderer;
import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.DragonAPI.Libraries.Java.ReikaGLHelper.BlendMode;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.Libraries.Rendering.ReikaGuiAPI;
import Reika.DragonAPI.Libraries.Rendering.ReikaRenderHelper;
import Reika.Satisforestry.Blocks.BlockFrackingAux.TileFrackingAux;
import Reika.Satisforestry.Blocks.BlockFrackingNode.TileFrackingNode;
import Reika.Satisforestry.Config.BiomeConfig;
import Reika.Satisforestry.Config.NodeResource.Purity;
import Reika.Satisforestry.Config.NodeResource.ResourceItemView;
import Reika.Satisforestry.Config.ResourceFluid;
import Reika.Satisforestry.Registry.SFBlocks;
import Reika.Satisforestry.Render.FrackingNodeAuxRenderer;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class FluidNodeHandler extends TemplateRecipeHandler {

	private final TileFrackingNode tileDelegate = new TileFrackingNode();

	private final Comparator<CachedRecipe> displaySorter = new Comparator<CachedRecipe>() {

		@Override
		public int compare(CachedRecipe o1, CachedRecipe o2) {
			if (o1 instanceof ResourceEntry && o2 instanceof ResourceEntry) {
				ResourceEntry l1 = (ResourceEntry)o1;
				ResourceEntry l2 = (ResourceEntry)o2;
				return Integer.compare(l2.item.spawnWeight, l1.item.spawnWeight);
			}
			else if (o1 instanceof ResourceEntry) {
				return Integer.MAX_VALUE;
			}
			else if (o2 instanceof ResourceEntry) {
				return Integer.MIN_VALUE;
			}
			else
				return 0;
		}

	};

	public class ResourceEntry extends CachedRecipe {

		private final ResourceFluid item;
		private final Purity purity;

		private final Comparator<PositionedStack> sorter = new Comparator<PositionedStack>() {

			@Override
			public int compare(PositionedStack o1, PositionedStack o2) {
				return ReikaItemHelper.comparator.compare(o1.item, o2.item);
			}

		};

		public ResourceEntry(ResourceFluid is, Purity p) {
			item = is;
			purity = p;
		}

		@Override
		public PositionedStack getResult() {
			return null;//new PositionedStack(item.getItem(), 5, 15);
		}

		@Override
		public PositionedStack getIngredient()
		{
			return null;
		}

		@Override
		public List<PositionedStack> getOtherStacks() {
			ArrayList<PositionedStack> stacks = new ArrayList();/*
			for (ItemStack is : item.getItemSet(purity).keySet()) {
				stacks.add(new PositionedStack(is, 2, 36+stacks.size()*20));
			}
			Collections.sort(stacks, sorter);*/
			return stacks;
		}
	}

	private StructureRenderer renderer;

	@Override
	public String getRecipeName() {
		return "Fracking Node Fluids";
	}

	@Override
	public String getGuiTexture() {
		return "unknown.png";
	}

	@Override
	public void drawBackground(int recipe)
	{
		GL11.glColor4f(1, 1, 1, 1);
		//ReikaTextureHelper.bindTexture(RotaryCraft.class, this.getGuiTexture());
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		ReikaGuiAPI.instance.drawTexturedModalRectWithDepth(0, 1, 5, 11, 166, 70, ReikaGuiAPI.NEI_DEPTH);
	}

	@Override
	public void drawForeground(int recipe)
	{
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glDisable(GL11.GL_LIGHTING);
		//ReikaTextureHelper.bindTexture(RotaryCraft.class, this.getGuiTexture());
		this.drawExtras(recipe);
	}

	@Override
	public void loadTransferRects() {
		transferRects.add(new RecipeTransferRect(new Rectangle(0, 3, 165, 52), "ResourceFluids"));
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results) {
		if (outputId != null && outputId.equals("ResourceFluids")) {
			try {
				for (ResourceFluid dd : BiomeConfig.instance.getFluidDrops()) {
					for (Purity p : Purity.list)
						arecipes.add(new ResourceEntry(dd, p));
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		Collections.sort(arecipes, displaySorter);
		super.loadCraftingRecipes(outputId, results);
	}

	@Override
	public void loadUsageRecipes(String inputId, Object... ingredients) {
		super.loadUsageRecipes(inputId, ingredients);
	}

	@Override
	public void loadCraftingRecipes(ItemStack result) {
		super.loadCraftingRecipes(result);
		arecipes.addAll(this.getEntriesForItem(result));
		Collections.sort(arecipes, displaySorter);
	}

	private Collection<ResourceEntry> getEntriesForItem(ItemStack is) {
		FluidStack fs = FluidContainerRegistry.getFluidForFilledItem(is);
		ArrayList<ResourceEntry> li = new ArrayList();
		if (fs == null)
			return li;
		try {
			for (ResourceFluid dd : BiomeConfig.instance.getFluidDrops()) {
				for (Purity p : Purity.list) {
					if (dd.producesAt(fs.getFluid(), p))
						li.add(new ResourceEntry(dd, p));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return li;
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient) {
		super.loadUsageRecipes(ingredient);
	}

	@Override
	public Class<? extends GuiContainer> getGuiClass() {
		return null;
	}

	@Override
	public int recipiesPerPage() {
		return 1;
	}

	@Override
	public void drawExtras(int recipe)
	{
		CachedRecipe r = arecipes.get(recipe);
		if (r instanceof ResourceEntry) {
			ResourceEntry re = (ResourceEntry)r;
			ResourceFluid wc = re.item;
			Minecraft mc = Minecraft.getMinecraft();
			if (GuiScreen.isCtrlKeyDown())
				renderer = null;
			if (renderer == null) {
				FilledBlockArray arr = new FilledBlockArray(mc.theWorld);
				for (int i = -9; i <= 9; i++) {
					for (int k = -9; k <= 9; k++) {
						if (Math.abs(i)+Math.abs(k) < 12 && Math.abs(i) < 9 && Math.abs(k) < 9)
							arr.setBlock(i, 0, k, SFBlocks.CAVESHIELD.getBlockInstance());
					}
				}
				NBTTagCompound tag = new NBTTagCompound();
				tileDelegate.writeToNBT(tag);
				tag.setString("resource", wc.id);
				tag.setInteger("purity", Purity.PURE.ordinal());
				tileDelegate.readFromNBT(tag);
				arr.setTile(0, 0, 0, SFBlocks.FRACKNODE.getBlockInstance(), 0, tileDelegate);

				for (int i = -6; i <= 6; i += 4) {
					int k = Math.abs(i) == 6 ? 2 : 6;
					if (DragonAPICore.rand.nextInt(4) > 0) {
						TileFrackingAux te2 = new TileFrackingAux();
						te2.linkTo(new Coordinate(tileDelegate));
						arr.setTile(i, 0, k, SFBlocks.FRACKNODEAUX.getBlockInstance(), 0, te2);
					}
					if (DragonAPICore.rand.nextInt(4) > 0) {
						TileFrackingAux te2 = new TileFrackingAux();
						te2.linkTo(new Coordinate(tileDelegate));
						arr.setTile(i, 0, -k, SFBlocks.FRACKNODEAUX.getBlockInstance(), 0, te2);
					}
				}
				renderer = new StructureRenderer(arr);
			}
			renderer.rotate(0, -0.75, 0);
			if (!GuiScreen.isShiftKeyDown()) {
				GuiContainer gc = (GuiContainer)mc.currentScreen;
				int gsc = ReikaRenderHelper.getGUIScale();
				GL11.glPushMatrix();
				double sc = 0.75;
				//GL11.glTranslated(-23, 55, -100);
				//GL11.glTranslated(-gc.guiLeft, -gc.guiTop, 0);
				GL11.glTranslated(-80D, 5D, 0);
				GL11.glScaled(sc, sc, sc);
				FrackingNodeAuxRenderer.renderDelegateMaster = tileDelegate;
				SFClient.fracking.setRenderPass(0);
				SFClient.frackingAux.setRenderPass(0);
				renderer.draw3D(0, 0, ReikaRenderHelper.getPartialTickTime(), true);
				SFClient.fracking.setRenderPass(1);
				SFClient.frackingAux.setRenderPass(1);
				renderer.draw3D(0, 0, ReikaRenderHelper.getPartialTickTime(), true);
				FrackingNodeAuxRenderer.renderDelegateMaster = null;
				GL11.glPopMatrix();
			}
			BlendMode.DEFAULT.apply();
			ReikaGuiAPI api = ReikaGuiAPI.instance;
			api.drawLine(0, 3, 165, 3, 0xff707070);
			api.drawLine(0, 25, 165, 25, 0xffaaaaaa);
			FontRenderer fr = mc.fontRenderer;
			api.drawCenteredStringNoShadow(fr, wc.displayName, 82, 5, 0x000000);
			api.drawCenteredStringNoShadow(fr, re.purity.getDisplayName()+" Nodes", 82, 15, 0x000000);
			//fr.drawString("Spawn Weight: "+wc.spawnWeight, 26, 28, 0x000000);
			int dy = 0;
			ResourceItemView ri = wc.getAllItems(re.purity).iterator().next();
			Fluid is = wc.getItem(ri);
			IIcon ico = is.getIcon();
			ReikaTextureHelper.bindTerrainTexture();
			GL11.glColor4f(1, 1, 1, 1);
			api.drawTexturedModelRectFromIcon(5, 28+dy, ico, 16, 16);
			String n = is.getLocalizedName(new FluidStack(is, 100));
			fr.drawString(n, 26, 28+dy, 0x000000);
			String counts = ri.minAmount == ri.maxAmount ? ri.minAmount+"mB" : ri.minAmount+"mB - "+ri.maxAmount+"mB";
			fr.drawString(counts, 26, 38+dy, 0x000000);
			dy -= fr.FONT_HEIGHT+2;
			api.drawLine(0, 58+dy, 165, 58+dy, 0xffaaaaaa);
			dy += (fr.FONT_HEIGHT+2)*3;
			api.drawLine(0, 58+dy-(fr.FONT_HEIGHT+2)*3, 165, 58+dy-(fr.FONT_HEIGHT+2)*3, 0xff707070);
			if (GuiScreen.isShiftKeyDown()) {
				if (wc.requiredInput != null) {
					n = wc.requiredInput.getLocalizedName(new FluidStack(wc.requiredInput, wc.requiredInputAmount));
					fr.drawString("Required input: "+n+" ("+wc.requiredInputAmount+" mB)", 5, 28+dy, 0x000000);
					dy += (fr.FONT_HEIGHT+2)*3;
				}
			}
			else {
				api.drawCenteredStringNoShadow(fr, "Hold LSHIFT for detailed info", 82, dy+28, 0x000000);
			}
		}
	}
}
