package Reika.Satisforestry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Exception.RegistrationException;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.ModInteract.ItemStackRepository;
import Reika.DragonAPI.ModInteract.ItemHandlers.BCPipeHandler;
import Reika.DragonAPI.ModInteract.ItemHandlers.BCPipeHandler.Pipes;
import Reika.DragonAPI.ModInteract.ItemHandlers.IC2Handler;
import Reika.DragonAPI.ModInteract.ItemHandlers.IC2Handler.IC2Stacks;
import Reika.DragonAPI.ModRegistry.PowerTypes;
import Reika.RotaryCraft.API.RecipeInterface;
import Reika.Satisforestry.Blocks.BlockFrackerMulti.FrackerBlocks;
import Reika.Satisforestry.Blocks.BlockMinerMulti.MinerBlocks;
import Reika.Satisforestry.Registry.SFBlocks;

import cpw.mods.fml.common.registry.GameRegistry;

public class SFMachineRecipes {

	public static final SFMachineRecipes instance = new SFMachineRecipes();

	private final Object steelIngot = this.findItem("repo_ROTARYCRAFT_steelingot", "ore_ingotSteel", Items.iron_ingot);
	private final Object gear = this.findItem("repo_ROTARYCRAFT_gearunit4", "ore_gearIron", Blocks.piston);
	private final Object impeller = this.findItem("repo_ROTARYCRAFT_impeller", Blocks.piston);
	private final Object shaft = this.findItem("repo_ROTARYCRAFT_shaftitem", Items.iron_ingot);
	private final Object silver = this.findItem("ore_ingotSilver", Items.iron_ingot);
	private final Object drill = this.findItem("repo_ROTARYCRAFT_drill", Items.diamond);
	//private final Object rfcoil = this.findItem(GameRegistry.findItemStack(ModList.THERMALEXPANSION.modLabel, "powerCoilElectrum", 1), Items.redstone);
	private final Object rfcoil = this.findItem(GameRegistry.findItemStack(ModList.THERMALEXPANSION.modLabel, "powerCoilGold", 1), Items.redstone);
	private final Object redblock = this.findItem("ThermalExpansion:Frame:7", Blocks.redstone_block);
	private final Object energium = this.findItem(IC2Handler.IC2Stacks.ENERGIUM.getItem(), Items.redstone);
	private final ItemStack alloy = (ItemStack)this.findItem(IC2Handler.IC2Stacks.ADVANCEDALLOY.getItem(), new ItemStack(Items.iron_ingot));
	private final Object alloy2 = this.findItem("repo_ROTARYCRAFT_springtungsten", ModList.ENDERIO.modLabel+":itemAlloy:6", "ore_ingotSteel", Items.iron_ingot);
	private final Object orange = this.findItem("ore_ingotBronze", ReikaItemHelper.orangeDye);

	private SFMachineRecipes() {

	}

	public void addRecipes() {
		ItemStack core = SFBlocks.MINERMULTI.getStackOfMetadata(MinerBlocks.GRAY.ordinal());
		this.addRecipe(core, "psp", "sCs", "ppp", 'p', this.getPlate(true), 's', steelIngot, 'C', this.getFrame(true));
		this.addRecipe(SFBlocks.MINERMULTI.getStackOfMetadata(MinerBlocks.ORANGE.ordinal()), " o ", "obo", " o ", 'b', core, 'o', orange);
		this.addRecipe(SFBlocks.MINERMULTI.getStackOfMetadata(MinerBlocks.SILVER.ordinal()), " o ", "obo", " o ", 'b', core, 'o', this.getPlate(false));
		this.addRecipe(SFBlocks.MINERMULTI.getStackOfMetadata(MinerBlocks.DARK.ordinal()), "sss", "pCp", "sss", 'C', core, 'p', this.getPlate(true), 's', steelIngot);
		this.addRecipe(SFBlocks.MINERMULTI.getStackOfMetadata(MinerBlocks.HUB.ordinal()), "sGs", "pCp", "sGs", 'G', gear, 'C', core, 'p', this.getPlate(false), 's', steelIngot);
		this.addRecipe(SFBlocks.MINERMULTI.getStackOfMetadata(MinerBlocks.POWER.ordinal()), "sPs", "sCs", "sRs", 'C', core, 'P', rfcoil, 'R', redblock, 's', steelIngot);

		ItemStack drillbit = SFBlocks.MINERMULTI.getStackOfMetadata(MinerBlocks.DRILL.ordinal());
		this.addRecipe(drillbit, "aDa", "aBa", "dRd", 'a', alloy2, 'R', drill, 'D', core, 'B', Blocks.obsidian, 'd', Items.diamond, 'i', steelIngot);
		this.addRecipe(SFBlocks.MINERMULTI.getStackOfMetadata(MinerBlocks.CONVEYOR.ordinal()), "ihi", "pcp", "ihi", 'h', Blocks.hopper, 'c', Blocks.chest, 'p', this.getPlate(false), 'i', steelIngot);

		core = SFBlocks.FRACKERMULTI.getStackOfMetadata(FrackerBlocks.GRAY.ordinal());
		this.addRecipe(core, "sps", "pCp", "sps", 'p', this.getPlate(true), 's', steelIngot, 'C', this.getFrame(false));
		ItemStack tube = SFBlocks.FRACKERMULTI.getStackOfMetadata(FrackerBlocks.TUBE.ordinal());
		ItemStack housing = SFBlocks.FRACKERMULTI.getStackOfMetadata(FrackerBlocks.ORANGE.ordinal());
		this.addRecipe(SFBlocks.FRACKERMULTI.getStackOfMetadata(FrackerBlocks.ORANGE.ordinal(), 2), "obo", "ofo", "obo", 'b', core, 'o', orange, 'f', this.getFrame(false));
		this.addRecipe(SFBlocks.FRACKERMULTI.getStackOfMetadata(FrackerBlocks.SILVER.ordinal()), "sos", "obo", "sos", 'b', core, 'o', this.getPlate(false), 's', silver);
		this.addRecipe(SFBlocks.FRACKERMULTI.getStackOfMetadata(FrackerBlocks.DARK.ordinal()), "sss", "pCp", "sss", 'C', core, 'p', this.getPlate(true), 's', steelIngot);
		this.addRecipe(SFBlocks.FRACKERMULTI.getStackOfMetadata(FrackerBlocks.HUB.ordinal()), "sGs", "pCp", "sGs", 'G', gear, 'C', core, 'p', this.getPlate(false), 's', steelIngot);
		this.addRecipe(SFBlocks.FRACKERMULTI.getStackOfMetadata(FrackerBlocks.POWER.ordinal()), "sPs", "sCs", "sRs", 'C', core, 'P', rfcoil, 'R', redblock, 's', steelIngot);
		this.addRecipe(SFBlocks.FRACKERMULTI.getStackOfMetadata(FrackerBlocks.FLUIDIN.ordinal()), "apa", "hch", "apa", 'h', this.getPipe(false), 'p', this.getPlate(false), 'a', housing);
		this.addRecipe(tube, "sps", "apa", "sps", 'a', alloy2, 'p', this.getPipe(false), 's', this.getPlate(true));

		if (PowerTypes.RF.isLoaded())
			this.addWRRecipe(SFBlocks.HARVESTER.getStackOfMetadata(0), "scs", "iri", "odo", 'c', rfcoil, 'r', redblock, 's', steelIngot, 'o', Blocks.obsidian, 'i', alloy2, 'd', drillbit);
		if (PowerTypes.EU.isLoaded())
			this.addWRRecipe(SFBlocks.HARVESTER.getStackOfMetadata(1), "scs", "iri", "odo", 'r', energium, 'c', ReikaItemHelper.getAnyMetaStack(IC2Stacks.LAPOTRON.getItem()), 's', steelIngot, 'o', Blocks.obsidian, 'i', alloy2, 'd', drillbit);
		if (PowerTypes.ROTARYCRAFT.isLoaded())
			this.addWRRecipe(SFBlocks.HARVESTER.getStackOfMetadata(2), "scs", "iri", "odo", 'c', shaft, 'r', ItemStackRepository.instance.getItem(ModList.ROTARYCRAFT, "gearunit16"), 's', steelIngot, 'o', Blocks.obsidian, 'i', alloy2, 'd', drillbit);

		if (PowerTypes.RF.isLoaded())
			this.addWRRecipe(SFBlocks.FRACKER.getStackOfMetadata(0), "ici", "iri", "ipi", 'c', rfcoil, 'r', redblock, 'i', this.getPlate(true), 'p', this.getPipe(false));
		if (PowerTypes.EU.isLoaded())
			this.addWRRecipe(SFBlocks.FRACKER.getStackOfMetadata(1), "iei", "aca", "ipi", 'a', alloy, 'e', energium, 'c', ReikaItemHelper.getAnyMetaStack(IC2Stacks.LAPOTRON.getItem()), 'i', this.getPlate(true), 'p', this.getPipe(false));
		if (PowerTypes.ROTARYCRAFT.isLoaded())
			this.addWRRecipe(SFBlocks.FRACKER.getStackOfMetadata(2), "bGb", "bgb", "bpb", 's', steelIngot, 'b', tube, 'G', ItemStackRepository.instance.getItem(ModList.ROTARYCRAFT, "gearunit16"), 'g', impeller, 'p', this.getPipe(true));
		this.addWRRecipe(SFBlocks.FRACKER.getStackOfMetadata(3), "pgp", "PgP", "hgh", 'g', tube, 'P', this.getPlate(false), 'h', housing, 'p', this.getPipe(true));
	}

	private Object getPlate(boolean preferSteel) {
		List<Object> li = new ArrayList(Arrays.asList(
				"repo_ROTARYCRAFT_basepanel",
				"ore_plateSteel",
				"ore_plateIron",
				Items.iron_ingot
				));
		if (!preferSteel) {
			li.remove(1);
			li.remove(0);
		}
		return this.findItem(li.toArray(new Object[li.size()]));
	}

	private Object getPipe(boolean preferRC) {
		List<Object> li = new ArrayList(Arrays.asList(
				ReikaItemHelper.lookupItem("ImmersiveEngineering:metalDevice2:5"),
				BCPipeHandler.getInstance().getPipe(Pipes.pipeFluidsEmerald),
				ItemStackRepository.instance.getItem(ModList.ROTARYCRAFT, "pipe"),
				Items.bucket
				));
		if (preferRC) {
			li.add(0, li.remove(2));
		}
		return this.findItem(li.toArray(new Object[li.size()]));
	}

	public Object getFrame(boolean highTier) {
		List<Object> li = new ArrayList(Arrays.asList(
				GameRegistry.findItemStack(ModList.THERMALEXPANSION.modLabel, highTier ? "frameMachineHardened" : "frameMachineBasic", 1),
				ReikaItemHelper.lookupItem(ModList.IMMERSIVEENG.modLabel+":metalDecoration:"+(highTier ? "5" : "7")),
				ReikaItemHelper.lookupItem(ModList.ENDERIO.modLabel+":itemMachinePart:0"), //machine chassis
				Blocks.iron_bars
				));
		return this.findItem(li.toArray(new Object[li.size()]));
	}

	private void addWRRecipe(ItemStack out, Object... in) {
		if (ModList.ROTARYCRAFT.isLoaded()) {
			RecipeInterface.worktable.addAPIRecipe(new ShapedOreRecipe(out, in));
		}
		else {
			this.addRecipe(out, in);
		}
	}

	private void addRecipe(ItemStack out, Object... in) {
		GameRegistry.addRecipe(new ShapedOreRecipe(out, in));
	}
	/*
	private Object getItemWithFallback(Object item, Block back) {
		return this.getItemWithFallback(item, new ItemStack(back));
	}

	private Object getItemWithFallback(Object item, Item back) {
		return this.getItemWithFallback(item, new ItemStack(back));
	}

	private Object getItemWithFallback(Object item, ItemStack back) {
		return item != null ? item : back;
	}
	 */

	private Object findItem(Object... chain) {
		for (Object o : chain) {
			Object item = this.getItem(o);
			if (item != null)
				return item;
		}
		throw new RegistrationException(Satisforestry.instance, "No valid items found of "+Arrays.toString(chain));
	}

	private Object getItem(Object o) {
		try {
			if (o instanceof ItemStack) {
				return o;
			}
			else if (o instanceof Block) {
				return new ItemStack((Block)o);
			}
			else if (o instanceof Item) {
				return new ItemStack((Item)o);
			}
			else if (o instanceof String) {
				String s = (String)o;
				if (s.startsWith("ore_")) {
					s = s.substring(4);
					return ReikaItemHelper.oreItemExists(s) ? s : null;
				}
				else if (s.startsWith("repo_")) {
					String[] parts = s.split("_");
					return ItemStackRepository.instance.getItem(ModList.valueOf(parts[1].toUpperCase(Locale.ENGLISH)), parts[2]);
				}
				else {
					return ReikaItemHelper.lookupItem(s);
				}
			}
		}
		catch (Exception e) {
			Satisforestry.logger.logError("Could not parse item fetch: "+(o == null ? "null" : o.getClass().getName()+"="+o));
		}
		return null;
	}
}
