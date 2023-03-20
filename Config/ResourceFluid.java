package Reika.Satisforestry.Config;

import java.util.HashMap;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import Reika.DragonAPI.DragonAPICore;

public class ResourceFluid extends NodeResource<Fluid> {

	public final int maxNodes;
	public final boolean glowAtNight;
	public final Fluid requiredInput;
	public final int requiredInputAmount;

	public ResourceFluid(String s, String n, int w, int c, int nodes, boolean glow, FluidStack fin, HashMap<String, Object> map) {
		super(s, n, w, c, map);
		maxNodes = nodes;
		glowAtNight = glow;
		requiredInput = fin == null ? null : fin.getFluid();
		requiredInputAmount = fin == null ? 0 : fin.amount;
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

	public FluidStack generateRandomFluid(Purity p, boolean peaceful, float pressureFactor) {
		NodeItem f = this.getRandomItem(Integer.MAX_VALUE, p, false);
		int amt = f.getAmount(p, Integer.MAX_VALUE, false, peaceful, DragonAPICore.rand);
		return new FluidStack(this.getItem(f), (int)(amt*pressureFactor));
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
