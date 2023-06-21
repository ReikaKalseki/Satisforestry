package Reika.Satisforestry.Render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Effects.EntityBlurFX;
import Reika.Satisforestry.Blocks.BlockResourceNode;
import Reika.Satisforestry.Blocks.BlockResourceNode.TileResourceNode;
import Reika.Satisforestry.Registry.SFBlocks;


public class EntityMinerFX extends EntityBlurFX {

	private IIcon iconToUse;
	private int uIndex;
	private int vIndex;

	public EntityMinerFX(World world, double x, double y, double z, double vx, double vy, double vz, TileResourceNode te) {
		super(world, x, y, z, vx, vy, vz, null);
		this.setBasicBlend().setRapidExpand();//.setColliding();
		boolean crys = rand.nextBoolean();
		iconToUse = crys ? BlockResourceNode.getCrystal() : SFBlocks.RESOURCENODE.getBlockInstance().blockIcon;
		uIndex = rand.nextInt(4)*4;
		vIndex = rand.nextInt(4)*4;
		if (crys) {
			this.setColor(te.getOverlayColor());
			particleAlpha = 0.6F+0.4F*rand.nextFloat();
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
	}

	@Override
	public void renderParticle(Tessellator v5, float par2, float par3, float par4, float par5, float par6, float par7) {
		float f10 = 0.1F * particleScale;

		double u = iconToUse.getInterpolatedU(uIndex);
		double v = iconToUse.getInterpolatedV(vIndex);
		double du = u+(iconToUse.getMaxU()-iconToUse.getMinU())*0.25;
		double dv = v+(iconToUse.getMaxV()-iconToUse.getMinV())*0.25;

		double f11 = prevPosX + (posX - prevPosX) * par2 - interpPosX;
		double f12 = prevPosY + (posY - prevPosY) * par2 - interpPosY;
		double f13 = prevPosZ + (posZ - prevPosZ) * par2 - interpPosZ;
		v5.setColorRGBA_F(particleRed, particleGreen, particleBlue, particleAlpha);
		v5.addVertexWithUV(f11 - par3 * f10 - par6 * f10, f12 - par4 * f10, f13 - par5 * f10 - par7 * f10, du, dv);
		v5.addVertexWithUV(f11 - par3 * f10 + par6 * f10, f12 + par4 * f10, f13 - par5 * f10 + par7 * f10, du, v);
		v5.addVertexWithUV(f11 + par3 * f10 + par6 * f10, f12 + par4 * f10, f13 + par5 * f10 + par7 * f10, u, v);
		v5.addVertexWithUV(f11 + par3 * f10 - par6 * f10, f12 - par4 * f10, f13 + par5 * f10 - par7 * f10, u, dv);
	}

}
