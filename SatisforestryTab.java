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

import java.util.Comparator;

import net.minecraft.item.ItemStack;

import Reika.DragonAPI.Instantiable.GUI.SortedCreativeTab;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SatisforestryTab extends SortedCreativeTab {

	public SatisforestryTab(String tabID) {
		super(tabID);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getIconItemStack() {
		return new ItemStack(Satisforestry.log);
	}

	@Override
	protected Comparator<ItemStack> getComparator() {
		return null;
	}

}
