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
import java.util.List;

import org.lwjgl.opengl.GL11;

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

		public AltRecipe(AlternateRecipe is) {
			recipe = is;
		}

		@Override
		public PositionedStack getResult() {
			return new PositionedStack(recipe.getOutput(), 5, 10);
		}

		@Override
		public List<PositionedStack> getIngredients() {
			ArrayList<PositionedStack> li = new ArrayList();
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
		transferRects.add(new RecipeTransferRect(new Rectangle(0, 3, 165, 52), "altrecipes"));
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
				if (ReikaItemHelper.matchStacks(dd.getOutput(), is))
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
		if (r instanceof AltRecipe) {

		}
	}
}
