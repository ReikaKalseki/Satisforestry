/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.Satisforestry.Registry;

import java.util.HashMap;
import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.OreDictionary;

import Reika.DragonAPI.Instantiable.MetadataItemBlock;
import Reika.DragonAPI.Interfaces.Registry.BlockEnum;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.Satisforestry.ItemPinkSapling;
import Reika.Satisforestry.Satisforestry;
import Reika.Satisforestry.Biome.Generator.PinkTreeGeneratorBase.PinkTreeTypes;
import Reika.Satisforestry.Blocks.BlockCaveShield;
import Reika.Satisforestry.Blocks.BlockCaveSpawner;
import Reika.Satisforestry.Blocks.BlockDecoration;
import Reika.Satisforestry.Blocks.BlockDecoration.DecorationType;
import Reika.Satisforestry.Blocks.BlockGasEmitter;
import Reika.Satisforestry.Blocks.BlockMinerMulti;
import Reika.Satisforestry.Blocks.BlockMinerMulti.MinerBlocks;
import Reika.Satisforestry.Blocks.BlockNodeHarvester;
import Reika.Satisforestry.Blocks.BlockPinkGrass;
import Reika.Satisforestry.Blocks.BlockPinkGrass.GrassTypes;
import Reika.Satisforestry.Blocks.BlockPinkLeaves;
import Reika.Satisforestry.Blocks.BlockPinkLog;
import Reika.Satisforestry.Blocks.BlockPinkSapling;
import Reika.Satisforestry.Blocks.BlockPowerSlug;
import Reika.Satisforestry.Blocks.BlockRedBamboo;
import Reika.Satisforestry.Blocks.BlockResourceNode;
import Reika.Satisforestry.Blocks.BlockTerrain;
import Reika.Satisforestry.Blocks.BlockTerrain.TerrainType;
import Reika.Satisforestry.Blocks.ItemBlockMinerMulti;
import Reika.Satisforestry.Blocks.ItemBlockNodeHarvester;
import Reika.Satisforestry.Blocks.ItemBlockPowerSlug;

public enum SFBlocks implements BlockEnum {

	LOG(BlockPinkLog.class, MetadataItemBlock.class),
	BAMBOO(BlockRedBamboo.class, null),
	LEAVES(BlockPinkLeaves.class, MetadataItemBlock.class),
	GRASS(BlockPinkGrass.class, MetadataItemBlock.class),
	CAVESHIELD(BlockCaveShield.class, null),
	SPAWNER(BlockCaveSpawner.class, null),
	GASEMITTER(BlockGasEmitter.class, null),
	RESOURCENODE(BlockResourceNode.class, null),
	TERRAIN(BlockTerrain.class, MetadataItemBlock.class),
	DECORATION(BlockDecoration.class, MetadataItemBlock.class),
	SAPLING(BlockPinkSapling.class, ItemPinkSapling.class),
	HARVESTER(BlockNodeHarvester.class, ItemBlockNodeHarvester.class),
	MINERMULTI(BlockMinerMulti.class, ItemBlockMinerMulti.class),
	SLUG(BlockPowerSlug.class, ItemBlockPowerSlug.class),
	;

	private final Class blockClass;
	private final Class itemBlock;

	public static final SFBlocks[] blockList = values();

	private static final HashMap<Block, SFBlocks> IDMap = new HashMap();

	private SFBlocks(Class <? extends Block> cl, Class<? extends ItemBlock> ib) {
		blockClass = cl;
		itemBlock = ib;
	}

	public Block getBlockInstance() {
		return Satisforestry.blocks[this.ordinal()];
	}

	public static SFBlocks getFromID(Item id) {
		return getFromID(Block.getBlockFromItem(id));
	}

	public static SFBlocks getFromID(Block id) {
		SFBlocks block = IDMap.get(id);
		if (block == null) {
			for (int i = 0; i < blockList.length; i++) {
				SFBlocks g = blockList[i];
				Block blockID = g.getBlockInstance();
				if (id == blockID) {
					IDMap.put(id, g);
					return g;
				}
			}
		}
		else {
			return block;
		}
		return null;
	}

	public Material getBlockMaterial() {
		switch(this) {
			case GRASS:
				return Material.plants;
			case LEAVES:
			case BAMBOO:
				return Material.leaves;
			case LOG:
				return Material.wood;
			case HARVESTER:
			case MINERMULTI:
				return Material.iron;
			case SLUG:
				return Satisforestry.slugMaterial;
			default:
				return Material.rock;
		}
	}

	@Override
	public Class[] getConstructorParamTypes() {
		switch(this) {
			case GRASS:
			case LEAVES:
			case SAPLING:
			case BAMBOO:
			case LOG:
				return new Class[0];
			default:
				return new Class[]{Material.class};
		}
	}

	@Override
	public Object[] getConstructorParams() {
		switch(this) {
			case GRASS:
			case LEAVES:
			case SAPLING:
			case BAMBOO:
			case LOG:
				return new Object[0];
			default:
				return new Object[]{this.getBlockMaterial()};
		}
	}

	@Override
	public String getUnlocalizedName() {
		return this.name().toLowerCase();
	}

	@Override
	public Class getObjectClass() {
		return blockClass;
	}

	@Override
	public String getBasicName() {
		return StatCollector.translateToLocal("sfblock."+this.getUnlocalizedName());
	}

	@Override
	public String getMultiValuedName(int meta) {
		switch(this) {
			case DECORATION:
				return StatCollector.translateToLocal("pinkforest.deco."+DecorationType.list[meta].name().toLowerCase(Locale.ENGLISH).replace("_", ""));
			case TERRAIN:
				return StatCollector.translateToLocal("pinkforest.terrain."+TerrainType.list[meta].name().toLowerCase(Locale.ENGLISH).replace("_", ""));
			case GRASS:
				String key = GrassTypes.list[meta].nameKey;
				String s = key != null ? key : GrassTypes.list[meta].name().toLowerCase(Locale.ENGLISH).replace("_", "");
				return StatCollector.translateToLocal("pinkforest.grass."+s);
			case LEAVES:
			case SAPLING:
			case LOG:
				String tree = StatCollector.translateToLocal("pinkforest.tree."+PinkTreeTypes.getLeafType(meta).name().toLowerCase(Locale.ENGLISH));
				String block = StatCollector.translateToLocal("pinkforest.tree."+this.name().toLowerCase(Locale.ENGLISH));
				return tree+" "+block;
			case HARVESTER:
				return StatCollector.translateToLocal("sfminer.type."+meta)+" "+this.getBasicName();
			case MINERMULTI:
				return StatCollector.translateToLocal("multiblock.sfminer."+MinerBlocks.list[meta&7].name().toLowerCase(Locale.ENGLISH));
			case SLUG:
				return StatCollector.translateToLocal("powerslug."+meta%3)+" "+this.getBasicName();
			default:
				return "";
		}
	}

	@Override
	public boolean hasMultiValuedName() {
		switch(this) {
			case TERRAIN:
			case DECORATION:
			case GRASS:
			case LEAVES:
			case SAPLING:
			case LOG:
			case HARVESTER:
			case MINERMULTI:
			case SLUG:
				return true;
			default:
				return false;
		}
	}

	@Override
	public int getNumberMetadatas() {
		switch(this) {
			case DECORATION:
				return DecorationType.list.length;
			case TERRAIN:
				return TerrainType.list.length;
			case GRASS:
				return GrassTypes.list.length;
			case HARVESTER:
				return 3;
			case MINERMULTI:
				return 8;
			case SLUG:
				return 6;
			default:
				return 1;
		}
	}

	@Override
	public Class<? extends ItemBlock> getItemBlock() {
		return itemBlock;
	}

	@Override
	public boolean hasItemBlock() {
		return itemBlock != null;
	}

	public boolean isDummiedOut() {
		return blockClass == null;
	}

	public Item getItem() {
		return Item.getItemFromBlock(this.getBlockInstance());
	}

	@Override
	public ItemStack getStackOfMetadata(int meta) {
		return new ItemStack(this.getBlockInstance(), 1, meta);
	}

	public boolean matchWith(ItemStack is) {
		return is != null && ReikaItemHelper.matchStackWithBlock(is, this.getBlockInstance());
	}

	public ItemStack getAnyMetaStack() {
		return this.getStackOfMetadata(OreDictionary.WILDCARD_VALUE);
	}
}
