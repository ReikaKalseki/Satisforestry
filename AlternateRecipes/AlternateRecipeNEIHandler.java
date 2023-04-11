/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.Satisforestry.AlternateRecipes;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import Reika.DragonAPI.Libraries.Rendering.ReikaGuiAPI;
import Reika.Satisforestry.API.AltRecipe.UncraftableAltRecipeWithNEI;
import Reika.Satisforestry.Config.AlternateRecipe;
import Reika.Satisforestry.Config.BiomeConfig;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class AlternateRecipeNEIHandler extends TemplateRecipeHandler {

	public class AltRecipe extends CachedRecipe {

		private final AlternateRecipe recipe;
		private final CachedRecipe neiDelegate;

		public AltRecipe(AlternateRecipe is) {
			recipe = is;
			neiDelegate = is.createNEIDelegate();
		}

		@Override
		public PositionedStack getResult() {
			if (neiDelegate != null)
				return neiDelegate.getResult();
			UncraftableAltRecipeWithNEI rec = recipe.getSpecialNEIDisplay();
			return new PositionedStack(rec == null ? recipe.getRecipeOutput() : rec.getRecipeOutput(), 119, 24);
		}

		@Override
		public List<PositionedStack> getIngredients() {
			ArrayList<PositionedStack> li = new ArrayList();
			if (neiDelegate != null) {
				li.addAll(neiDelegate.getIngredients());
			}
			else if (recipe.isUncraftableWithNEI()) {
				UncraftableAltRecipeWithNEI rec = recipe.getSpecialNEIDisplay();
				Object[] in = rec.getDisplayInputs();
				for (int i = 0; i < Math.min(in.length, 9); i++) {
					if (in[i] != null) {
						int x = (i%3)*18;
						int y = (i/3)*18;
						if (in[i] instanceof Block)
							in[i] = new ItemStack((Block)in[i]);
						if (in[i] instanceof Item)
							in[i] = new ItemStack((Item)in[i]);
						li.add(new PositionedStack(in[i], x+25, y+6));
					}
				}
			}
			return li;
		}
	}

	@Override
	public String getRecipeName() {
		return "Alternate Recipes";
	}

	@Override
	public String getGuiTexture() {
		return "textures/gui/container/crafting_table.png";
	}

	@Override
	public void drawBackground(int recipe)
	{
		GL11.glColor4f(1, 1, 1, 1);
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(this.getGuiTexture()));
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		ReikaGuiAPI.instance.drawTexturedModalRectWithDepth(0, 0, 5, 11, 166, 70, ReikaGuiAPI.NEI_DEPTH);
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
		transferRects.add(new RecipeTransferRect(new Rectangle(84, 23, 24, 18), "altrecipes", new Object[0]));
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results) {
		if (outputId != null && outputId.equals("altrecipes")) {
			try {
				for (AlternateRecipe dd : BiomeConfig.instance.getAlternateRecipes()) {
					if (dd.useNEIHandler())
						arecipes.add(new AltRecipe(dd));
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.loadCraftingRecipes(outputId, results);
	}

	@Override
	public void loadUsageRecipes(String inputId, Object... ingredients) {
		if (inputId != null && inputId.equals("altrecipes")) {
			try {
				for (AlternateRecipe dd : BiomeConfig.instance.getAlternateRecipes()) {
					if (dd.useNEIHandler())
						arecipes.add(new AltRecipe(dd));
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.loadUsageRecipes(inputId, ingredients);
	}

	@Override
	public void loadCraftingRecipes(ItemStack result) {
		super.loadCraftingRecipes(result);
		arecipes.addAll(this.getEntriesForItem(result));
	}

	private Collection<AltRecipe> getEntriesForItem(ItemStack is) {
		ArrayList<AltRecipe> li = new ArrayList();
		try {
			for (AlternateRecipe dd : BiomeConfig.instance.getAlternateRecipes()) {
				if (dd.useNEIHandler() && dd.crafts(is))
					li.add(new AltRecipe(dd));
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
		for (AlternateRecipe dd : BiomeConfig.instance.getAlternateRecipes()) {
			if (dd.useNEIHandler() && dd.usesItem(ingredient))
				arecipes.add(new AltRecipe(dd));
		}
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
	public void drawExtras(int recipe) {
		CachedRecipe a = arecipes.get(recipe);
		if (a instanceof AltRecipe) {
			AlternateRecipe r = ((AltRecipe)a).recipe;
			FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
			UncraftableAltRecipeWithNEI sp = r.getSpecialNEIDisplay();
			if (sp != null) {
				GL11.glScaled(0.5, 0.5, 0.5);
				ReikaGuiAPI.instance.drawCenteredStringNoShadow(fr, r.getDisplayName(), 166, 123, 0xFA9549);
				ReikaGuiAPI.instance.drawCenteredStringNoShadow(fr, sp.getDescription(), 166, 123+fr.FONT_HEIGHT+4, 0xFA9549);
				GL11.glScaled(2, 2, 2);
			}
			else {
				ReikaGuiAPI.instance.drawCenteredStringNoShadow(fr, r.getDisplayName(), 83, 63, 0xFA9549);
			}
			ReikaGuiAPI.instance.drawCenteredStringNoShadow(fr, "Requires unlock in a crash site", 83, 80, 0x646464);
			ReikaGuiAPI.instance.drawCenteredStringNoShadow(fr, "provided with the following:", 83, 92, 0x646464);
			if (r.unlockPower != null)
				ReikaGuiAPI.instance.drawCenteredStringNoShadow(fr, "Power: "+r.unlockPower.getDisplayString(), 83, 104, 0x646464);
			ItemStack is = r.getRequiredItem();
			if (is != null)
				ReikaGuiAPI.instance.drawCenteredStringNoShadow(fr, "Items: "+is.stackSize+" "+is.getDisplayName(), 83, 116, 0x646464);
			ReikaGuiAPI.instance.drawLine(4, 75, 162, 75, 0x646464);
		}
	}
}
