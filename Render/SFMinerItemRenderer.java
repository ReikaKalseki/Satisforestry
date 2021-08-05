package Reika.Satisforestry.Render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;

import Reika.Satisforestry.Registry.SFBlocks;


public class SFMinerItemRenderer implements IItemRenderer {

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		TileEntity te = SFBlocks.HARVESTER.getBlockInstance().createTileEntity(Minecraft.getMinecraft().theWorld, item.getItemDamage());
		TileEntityRendererDispatcher.instance.renderTileEntityAt(te, 0, 0, 0, 0.0F);
	}

}
