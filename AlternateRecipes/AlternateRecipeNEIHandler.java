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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.Libraries.Rendering.ReikaGuiAPI;
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
			return neiDelegate == null ? new PositionedStack(recipe.getRecipeOutput(), 119, 24) : neiDelegate.getResult();
		}

		@Override
		public List<PositionedStack> getIngredients() {
			ArrayList<PositionedStack> li = new ArrayList();
			if (neiDelegate != null)
				li.addAll(neiDelegate.getIngredients());
			return li;
		}
	}

	@Override
	public String getRecipeName() {
		return "Alternate Recipes";
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
		transferRects.add(new RecipeTransferRect(new Rectangle(84, 23, 24, 18), "altrecipes", new Object[0]));
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results) {
		if (outputId != null && outputId.equals("altrecipes")) {
			try {
				for (AlternateRecipe dd : BiomeConfig.instance.getAlternateRecipes()) {
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
				if (ReikaItemHelper.matchStacks(dd.getRecipeOutput(), is))
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
			if (dd.usesItem(ingredient))
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
			FontRenderer fontRendererObj = Minecraft.getMinecraft().fontRenderer;
			ReikaGuiAPI.instance.drawCenteredStringNoShadow(fontRendererObj, r.getDisplayName(), 53, 23, 0xFA9549);
			if (r.unlockPower != null)
				ReikaGuiAPI.instance.drawCenteredStringNoShadow(fontRendererObj, "Requires "+r.unlockPower.getDisplayString(), 53, 32, 0x646464);
			ItemStack is = r.getRequiredItem();
			if (is != null)
				ReikaGuiAPI.instance.drawCenteredStringNoShadow(fontRendererObj, "Requires "+is.stackSize+" "+is.getDisplayName(), 53, 38, 0x646464);
		}
	}
}
