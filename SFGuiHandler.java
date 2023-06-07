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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import Reika.Satisforestry.AlternateRecipes.ContainerCrashSite;
import Reika.Satisforestry.AlternateRecipes.GuiCrashSite;
import Reika.Satisforestry.Blocks.BlockCrashSite.TileCrashSite;
import Reika.Satisforestry.Miner.ContainerSFMiner;
import Reika.Satisforestry.Miner.GuiSFFracker;
import Reika.Satisforestry.Miner.GuiSFMiner;
import Reika.Satisforestry.Miner.TileFrackingPressurizer;
import Reika.Satisforestry.Miner.TileNodeHarvester;
import Reika.Satisforestry.Miner.TileResourceHarvesterBase;

import cpw.mods.fml.common.network.IGuiHandler;

public class SFGuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof TileResourceHarvesterBase) {
			return new ContainerSFMiner(player, (TileResourceHarvesterBase)te);
		}
		else if (te instanceof TileCrashSite) {
			return new ContainerCrashSite(player, (TileCrashSite)te);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == 1)
			return new GuiSFBlueprint(player, x);
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof TileNodeHarvester) {
			return new GuiSFMiner(player, (TileNodeHarvester)te);
		}
		else if (te instanceof TileFrackingPressurizer) {
			return new GuiSFFracker(player, (TileFrackingPressurizer)te);
		}
		else if (te instanceof TileCrashSite) {
			return new GuiCrashSite(player, (TileCrashSite)te);
		}
		return null;
	}

}
