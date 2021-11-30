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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import Reika.DragonAPI.Libraries.ReikaEntityHelper;
import Reika.DragonAPI.Libraries.Java.ReikaGLHelper.BlendMode;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.Libraries.Rendering.ReikaGuiAPI;
import Reika.Satisforestry.Config.BiomeConfig;
import Reika.Satisforestry.Config.DoggoDrop;
import Reika.Satisforestry.Config.DoggoDrop.Condition;
import Reika.Satisforestry.Entity.EntityLizardDoggo;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class DoggoDropHandler extends TemplateRecipeHandler {

	private final Comparator<CachedRecipe> displaySorter = new Comparator<CachedRecipe>() {

		@Override
		public int compare(CachedRecipe o1, CachedRecipe o2) {
			if (o1 instanceof DoggoEntry && o2 instanceof DoggoEntry) {
				DoggoEntry l1 = (DoggoEntry)o1;
				DoggoEntry l2 = (DoggoEntry)o2;
				return Integer.compare(l2.item.baseWeight, l1.item.baseWeight);
			}
			else if (o1 instanceof DoggoEntry) {
				return Integer.MAX_VALUE;
			}
			else if (o2 instanceof DoggoEntry) {
				return Integer.MIN_VALUE;
			}
			else
				return 0;
		}

	};

	public class DoggoEntry extends CachedRecipe {

		private final DoggoDrop item;

		public DoggoEntry(DoggoDrop is) {
			item = is;
		}

		@Override
		public PositionedStack getResult() {
			return new PositionedStack(item.getItem(), 5, 10);
		}

		@Override
		public PositionedStack getIngredient()
		{
			return null;
		}
	}

	private EntityLizardDoggo renderEntity;

	@Override
	public String getRecipeName() {
		return "Lizard Doggo Drops";
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
		transferRects.add(new RecipeTransferRect(new Rectangle(0, 3, 165, 52), "doggodrops"));
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results) {
		if (outputId != null && outputId.equals("doggodrops")) {
			try {
				for (DoggoDrop dd : BiomeConfig.instance.getDoggoDrops()) {
					arecipes.add(new DoggoEntry(dd));
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

	private Collection<DoggoEntry> getEntriesForItem(ItemStack is) {
		ArrayList<DoggoEntry> li = new ArrayList();
		try {
			for (DoggoDrop dd : BiomeConfig.instance.getDoggoDrops()) {
				if (ReikaItemHelper.matchStacks(dd.getItem(), is))
					li.add(new DoggoEntry(dd));
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
		if (r instanceof DoggoEntry) {
			if (renderEntity == null) {
				renderEntity = new EntityLizardDoggo(Minecraft.getMinecraft().theWorld);
			}
			GL11.glPushMatrix();
			double sc = 48;
			GL11.glScaled(sc, sc, sc);
			GL11.glTranslated(2, 2, 0);
			GL11.glRotated(180, 0, 1, 1);
			GL11.glRotated(135, 1, 0, 0);
			GL11.glRotated(45, 0, 1, 0);
			GL11.glScaled(1, -1, 1);
			EntityPlayer ep = Minecraft.getMinecraft().thePlayer;
			renderEntity.setLocationAndAngles(ep.posX, ep.posY, ep.posZ, 0, 0);
			Render rd = ReikaEntityHelper.getEntityRenderer(EntityLizardDoggo.class);
			//rd.doRender(renderEntity, 0, 0, 0, 0, 0);
			GL11.glPopMatrix();
			BlendMode.DEFAULT.apply();
			ReikaGuiAPI api = ReikaGuiAPI.instance;
			api.drawLine(0, 3, 165, 3, 0xff707070);
			FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
			DoggoDrop wc = ((DoggoEntry)r).item;
			String n = wc.getItem().getDisplayName();
			fr.drawString(n, 26, 5, 0x000000);
			fr.drawString("Base Weight: "+wc.baseWeight, 26, 15, 0x000000);
			String counts = wc.minCount == wc.maxCount ? wc.minCount+"x" : wc.minCount+"x - "+wc.maxCount+"x";
			fr.drawString("Stack Size: "+counts, 26, 25, 0x000000);
			int dy = 0;

			Collection<Condition> li = wc.getRequirements();
			if (!li.isEmpty()) {
				api.drawLine(0, 37, 165, 37, 0xff707070);
				fr.drawString("Requires:", 26, 40, 0x000000);
				for (Condition c : li) {
					String s = c.getDisplayString();
					fr.drawSplitString(s, 32, 50+dy, 135, 0x000000);
					dy += (fr.FONT_HEIGHT+2)*fr.listFormattedStringToWidth(s, 135).size();
				}
				dy += fr.FONT_HEIGHT+2;
			}
			Map<Condition, Double> map = wc.getModifiers();
			map = new HashMap(map);
			if (!map.isEmpty()) {
				api.drawLine(0, 37+dy, 165, 37+dy, 0xff707070);
				fr.drawString("Weight Modifiers:", 26, 40+dy, 0x000000);
				for (Entry<Condition, Double> e : map.entrySet()) {
					String s = e.getKey().getDisplayString()+": "+e.getValue()+"x";
					fr.drawSplitString(s, 32, 50+dy, 135, 0x000000);
					dy += (fr.FONT_HEIGHT+2)*fr.listFormattedStringToWidth(s, 135).size();
				}
				dy += fr.FONT_HEIGHT+2;
			}
			api.drawLine(0, 39+dy, 165, 39+dy, 0xff707070);
		}
	}
}
