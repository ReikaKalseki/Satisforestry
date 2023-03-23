package Reika.Satisforestry.Config;

import java.util.HashMap;

import net.minecraft.util.MathHelper;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;

public class ResourceFluid extends NodeResource<Fluid> {

	public final int maxNodes;
	public final boolean glowAtNight;
	public final Fluid requiredInput;
	public final int requiredInputAmount;
	public final int roundBase;

	public ResourceFluid(String s, int w, int c, int nodes, boolean glow, FluidStack fin, int round, HashMap<String, Object> map) {
		super(s, null, w, c, map);
		maxNodes = nodes;
		glowAtNight = glow;
		requiredInput = fin == null ? null : fin.getFluid();
		requiredInputAmount = fin == null ? 0 : fin.amount;
		roundBase = MathHelper.clamp_int(round, 1, 1000);
	}

	@Override
	public String getDisplayName() {
		Fluid f = this.getFluid();
		return f.getLocalizedName(new FluidStack(f, 1000));
	}

	public Fluid getFluid() {
		return this.getItem(this.getRandomItem(Integer.MAX_VALUE, Purity.NORMAL, false));
	}

	public int[] getBaseMinMax(Purity p, boolean peaceful) {
		NodeItem f = this.getRandomItem(Integer.MAX_VALUE, p, false);
		return f.getAmountRange(p, Integer.MAX_VALUE, false, peaceful);
	}

	@Override
	public int maxItemsPerType() {
		return Purity.list.length;
	}

	@Override
	public Fluid getItem(NodeItem obj) {
		return obj.item;
	}

	@Override
	public boolean matchItem(NodeItem obj, Fluid is) {
		return is == obj.item;
	}

	public FluidStack generateRandomFluid(Purity p, boolean peaceful, float overclock) {
		NodeItem f = this.getRandomItem(Integer.MAX_VALUE, p, false);
		int amt = f.getAmount(p, Integer.MAX_VALUE, false, peaceful, DragonAPICore.rand);
		amt *= 1+overclock;
		if (roundBase > 1)
			amt = ReikaMathLibrary.roundToNearestX(roundBase, amt);
		return new FluidStack(this.getFluid(), amt);
	}

	@Override
	protected ResourceItemView getView(NodeItem ni, Purity p) {
		return new FluidResourceView(ni, p);
	}

	public class FluidResourceView extends ResourceItemView {


		protected FluidResourceView(NodeItem ni, Purity p) {
			super(ni, p);
		}

	}

}
