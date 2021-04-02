package Reika.Satisforestry.API;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface PinkTreeType {

	public String name();

	public int ordinal();

	@SideOnly(Side.CLIENT)
	public int getBasicRenderColor();

}
