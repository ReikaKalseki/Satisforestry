package Reika.Satisforestry;

import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.terraingen.ChunkProviderEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.FluidRegistry;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.DependentMethodStripper.ClassDependent;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Auxiliary.Trackers.SpecialDayTracker;
import Reika.DragonAPI.Instantiable.Event.BlockStopsPrecipitationEvent;
import Reika.DragonAPI.Instantiable.Event.BlockTickEvent;
import Reika.DragonAPI.Instantiable.Event.GenLayerBeachEvent;
import Reika.DragonAPI.Instantiable.Event.GenLayerRiverEvent;
import Reika.DragonAPI.Instantiable.Event.GetYToSpawnMobEvent;
import Reika.DragonAPI.Instantiable.Event.IceFreezeEvent;
import Reika.DragonAPI.Instantiable.Event.LightLevelForSpawnEvent;
import Reika.DragonAPI.Instantiable.Event.SlotEvent.AddToSlotEvent;
import Reika.DragonAPI.Instantiable.Event.SlotEvent.RemoveFromSlotEvent;
import Reika.DragonAPI.Instantiable.Event.SnowOrIceOnGenEvent;
import Reika.DragonAPI.Instantiable.Event.Client.GrassIconEvent;
import Reika.DragonAPI.Instantiable.Event.Client.LiquidBlockIconEvent;
import Reika.DragonAPI.Instantiable.Event.Client.SinglePlayerLogoutEvent;
import Reika.DragonAPI.Instantiable.Event.Client.WaterColorEvent;
import Reika.DragonAPI.Libraries.ReikaAABBHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.Libraries.Rendering.ReikaColorAPI;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.DragonAPI.ModInteract.ItemHandlers.IC2Handler;
import Reika.Satisforestry.Biome.BiomePinkForest;
import Reika.Satisforestry.Biome.Biomewide.BiomewideFeatureGenerator;
import Reika.Satisforestry.Biome.Biomewide.UraniumCave;
import Reika.Satisforestry.Biome.Generator.WorldGenPinkRiver;
import Reika.Satisforestry.Biome.Generator.WorldGenUraniumCave;
import Reika.Satisforestry.Blocks.ItemBlockPowerSlug;
import Reika.Satisforestry.Entity.EntityEliteStinger;
import Reika.Satisforestry.Miner.GuiSFMiner;
import Reika.Satisforestry.Registry.SFBlocks;
import Reika.Satisforestry.Render.SpitterFireParticle;

import WayofTime.alchemicalWizardry.api.event.TeleposeEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SFEvents {

	public static final SFEvents instance = new SFEvents();

	private IIcon biomeGrassIcon;
	private IIcon biomeGrassIconSide;
	private IIcon biomeWaterIcon;
	private IIcon biomeWaterIconFlow;

	private IIcon flameParticle;

	private SFEvents() {

	}
	/*
	@SubscribeEvent
	public void tagPlayerMotions(LivingUpdateEvent evt) {
		if (evt.entityLiving instanceof EntityPlayer && !evt.entityLiving.worldObj.isRemote) {
			EntityPlayer ep = (EntityPlayer)evt.entityLiving;
			boolean fwd = KeyWatcher.instance.isKeyDown(ep, Key.FORWARD);
			boolean back = KeyWatcher.instance.isKeyDown(ep, Key.BACK);
			boolean left = KeyWatcher.instance.isKeyDown(ep, Key.LEFT);
			boolean right = KeyWatcher.instance.isKeyDown(ep, Key.RIGHT);
			double v = ep.isSneaking() ? 0.3 : 1;
			double fwdSpeed = fwd == back ? 0 : (fwd ? v : -v);
			double sideSpeed = left == right ? 0 : (left ? v : -v); //+ve to left
			float f4 = MathHelper.sin(ep.rotationYaw * (float)Math.PI / 180.0F);
			float f5 = MathHelper.cos(ep.rotationYaw * (float)Math.PI / 180.0F);
			double motionX = sideSpeed * f5 - fwdSpeed * f4;
			double motionZ = fwdSpeed * f5 + sideSpeed * f4;
			ReikaJavaLibrary.pConsole(fwdSpeed+" / "+sideSpeed+" @ "+ep.rotationYaw+" : "+motionX+", "+motionZ);
		}
	}
	 */
	@SubscribeEvent
	public void boostSlugMining(net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed evt) {
		ItemStack is = evt.entityPlayer.getCurrentArmor(3); //helmet
		if (is != null && SFBlocks.SLUG.matchWith(is)) {
			float lim = 24;
			if (evt.newSpeed < lim) {
				float f = 1+0.667F*(1+is.getItemDamage()%3)/(float)Math.pow(evt.originalSpeed, 0.4);
				//ReikaJavaLibrary.pConsole(evt.originalSpeed+" % "+evt.newSpeed+" & "+f);
				evt.newSpeed = Math.min(lim, evt.newSpeed*f);
			}
		}
	}

	@SubscribeEvent
	public void onAddArmor(AddToSlotEvent evt) {
		int id = evt.slotID;
		if (evt.inventory instanceof InventoryPlayer && evt.slotID == 39) { //head armor
			ItemStack is = evt.getItem();
			if (is == null || SFBlocks.SLUG.matchWith(is)) {
				ItemBlockPowerSlug.removeBonuses(((InventoryPlayer)evt.inventory).player);
			}
		}
	}

	@SubscribeEvent
	public void onRemoveArmor(RemoveFromSlotEvent evt) {
		int id = evt.slotID;
		if (evt.slotID == 39) { //head armor
			ItemStack is = evt.getItem();
			if (is != null && SFBlocks.SLUG.matchWith(is)) {
				ItemBlockPowerSlug.removeBonuses(((InventoryPlayer)evt.inventory).player);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority=EventPriority.LOWEST)
	@ModDependent(ModList.IC2)
	public void removeIncorrectIC2OverclockingValues(ItemTooltipEvent event) {
		if (Minecraft.getMinecraft().currentScreen instanceof GuiSFMiner && ReikaItemHelper.matchStacks(event.itemStack, IC2Handler.IC2Stacks.OVERCLOCK.getItem())) {
			Iterator<String> it = event.toolTip.iterator();
			while (it.hasNext()) {
				String s = it.next();
				if (s.contains("Decrease process time") || s.contains("Increase power to"))
					it.remove();
			}
		}
	}

	@SubscribeEvent
	public void meltSnowIce(BlockTickEvent evt) {
		if (!evt.world.isRaining() && evt.world.isDaytime() && !SpecialDayTracker.instance.isWinterEnabled() && Satisforestry.isPinkForest(evt.world, evt.xCoord, evt.zCoord) && evt.world.canBlockSeeTheSky(evt.xCoord, evt.yCoord+1, evt.zCoord)) {
			if (evt.block == Blocks.snow_layer)
				evt.world.setBlockToAir(evt.xCoord, evt.yCoord, evt.zCoord);
			else if (evt.block == Blocks.ice)
				evt.world.setBlock(evt.xCoord, evt.yCoord, evt.zCoord, Blocks.water);
		}
	}

	@SubscribeEvent
	public void preventNewIce(IceFreezeEvent evt) {
		if (Satisforestry.isPinkForest(evt.world, evt.xCoord, evt.zCoord)) {
			evt.setResult(Result.DENY);
		}
	}

	@SubscribeEvent
	public void preventSnowGen(SnowOrIceOnGenEvent evt) {
		if (Satisforestry.isPinkForest(evt.world, evt.xCoord, evt.zCoord)) {
			evt.setResult(Result.DENY);
		}
	}

	@SubscribeEvent
	public void shapePinkForest(ChunkProviderEvent.ReplaceBiomeBlocks evt) {
		if (evt.world != null && evt.blockArray != null) {
			Satisforestry.pinkforest.shapeTerrain(evt.world, evt.chunkX, evt.chunkZ, evt.blockArray, evt.metaArray);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void retextureGrass(GrassIconEvent evt) {
		if (Satisforestry.isPinkForest(evt.getBiome())) {
			evt.icon = evt.isTop ? biomeGrassIcon : biomeGrassIconSide;
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void retextureWater(LiquidBlockIconEvent evt) {
		if (Satisforestry.isPinkForest(evt.getBiome())) {
			if (evt.originalIcon == FluidRegistry.WATER.getFlowingIcon())
				evt.icon = biomeWaterIconFlow;
			else if (evt.originalIcon == FluidRegistry.WATER.getStillIcon())
				evt.icon = biomeWaterIcon;
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() == 0) {
			biomeGrassIcon = event.map.registerIcon("Satisforestry:terrain/grass_top");
			biomeGrassIconSide = event.map.registerIcon("Satisforestry:terrain/grass_side_overlay");
			biomeWaterIconFlow = event.map.registerIcon("Satisforestry:terrain/water/water_flow");
			biomeWaterIcon = event.map.registerIcon("Satisforestry:terrain/water/water_still");

			flameParticle = event.map.registerIcon("Satisforestry:spitterfire");
			SpitterFireParticle.setFireIcon(flameParticle);
		}
	}

	@SubscribeEvent
	public void changePinkRivers(GenLayerRiverEvent evt) {
		if (evt.originalBiomeID == Satisforestry.pinkforest.biomeID) {
			//evt.riverBiomeID = pinkriver.biomeID;
			evt.setResult(Result.DENY);
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void clearBiomeRiver(SinglePlayerLogoutEvent evt) {
		WorldGenPinkRiver.clearLakeCache();
		WorldGenUraniumCave.clearCaveCache();
		BiomewideFeatureGenerator.instance.clearOnUnload();
	}

	//@SubscribeEvent(priority = EventPriority.LOWEST)
	public void clearBiomeRiver(ClientDisconnectionFromServerEvent evt) {
		WorldGenPinkRiver.clearLakeCache();
		WorldGenUraniumCave.clearCaveCache();
		BiomewideFeatureGenerator.instance.clearOnUnload();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void forestWaterColor(WaterColorEvent evt) {
		if (Satisforestry.isPinkForest(evt.getBiome())) {
			evt.color = Satisforestry.pinkforest.getWaterColor(evt.access, evt.xCoord, evt.yCoord, evt.zCoord, evt.getLightLevel());
		}
	}

	@SubscribeEvent
	public void keepSurfaceMobsSparse(LivingSpawnEvent.CheckSpawn evt) {
		int x = MathHelper.floor_double(evt.x);
		int z = MathHelper.floor_double(evt.z);
		if (Satisforestry.isPinkForest(evt.world, x, z)) {
			if (evt.entityLiving instanceof EntitySpider && evt.y >= 90) {
				AxisAlignedBB box = ReikaAABBHelper.getPointAABB(evt.x, evt.y, evt.z, 18, 8);
				List<Entity> li = evt.world.getEntitiesWithinAABBExcludingEntity(evt.entityLiving, box, new IEntitySelector() {
					@Override
					public boolean isEntityApplicable(Entity e) {
						return e instanceof EntitySpider;
					}
				});
				if (li.size() > 1+(evt.world.skylightSubtracted+1)/3)
					evt.setResult(Result.DENY);
			}
		}
	}

	@SubscribeEvent
	public void dynamicSpawns(WorldEvent.PotentialSpawns evt) {
		if (evt.type == EnumCreatureType.monster && Satisforestry.isPinkForest(evt.world, evt.x, evt.z)) {
			if (BiomewideFeatureGenerator.instance.isInCave(evt.world, evt.x, evt.y, evt.z)) {
				evt.list.clear();
				evt.list.add(UraniumCave.instance.getRandomSpawn(evt.world.rand));
				//ReikaJavaLibrary.pConsole(evt.list.get(0).entityClass+" @ "+evt.world.getTotalWorldTime());
			}
			else {
				int wt = evt.world.skylightSubtracted*10/11; //gives range of 0 in day to 10 (compared to spider 30) in night
				evt.list.add(new BiomeGenBase.SpawnListEntry(EntityEliteStinger.class, wt, 1, 1));
			}
		}
	}

	@SubscribeEvent
	/** The javadoc on this event is WRONG - this cancels the onSpawnWith egg, to prevent spider jockeys and potions */
	public void cleanSpiders(LivingSpawnEvent.SpecialSpawn evt) {
		if (evt.entityLiving instanceof EntitySpider && Satisforestry.isPinkForest(evt.world, MathHelper.floor_double(evt.x), MathHelper.floor_double(evt.z))) {
			evt.setCanceled(true);
		}
	}

	/*
	@SubscribeEvent
	public void tagCavePlayers(LivingUpdateEvent evt) {
		if (evt.entityLiving instanceof EntityPlayer && !evt.entityLiving.worldObj.isRemote) {
			long time = evt.entityLiving.worldObj.getTotalWorldTime();
			if (time%10 == 0 && BiomewideFeatureGenerator.instance.isInCave(evt.entityLiving.worldObj, evt.entityLiving.posX, evt.entityLiving.posY, evt.entityLiving.posZ)) {
				evt.entityLiving.getEntityData().setLong("biomecavetick", time);
			}
		}
	}
	 */
	/*
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void brighterDarkness(LightVisualBrightnessEvent evt) {
		if (Satisforestry.isPinkForest(evt.getBiome())) {
			;//evt.brightness = evt.getBrightnessFor(Math.max(1, evt.lightLevel));
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void brighterDarkness(LightMixedBrightnessEvent evt) {
		if (Satisforestry.isPinkForest(evt.getBiome())) {
			;//evt.value = evt.getBrightnessFor(evt.blockLight, Math.max(1, evt.skyLight));
		}
	}
	 */
	/*
	@SubscribeEvent
	public void spidersAtAllBrightness(CheckSpawn evt) {
		if (evt.entity instanceof EntitySpider && this.isPinkForest(evt.entity.worldObj, MathHelper.floor_float(evt.x), MathHelper.floor_float(evt.z))) {
			evt.setResult(Result.ALLOW);
		}
	}
	 */
	@SubscribeEvent
	public void fallproofSpiders(LivingHurtEvent evt) {
		if (evt.entity instanceof EntitySpider && evt.source == DamageSource.fall && Satisforestry.isPinkForest(evt.entity.worldObj, MathHelper.floor_double(evt.entity.posX), MathHelper.floor_double(evt.entity.posZ))) {
			evt.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void spidersAtAllBrightness(LightLevelForSpawnEvent evt) {
		if (evt.mob instanceof EntitySpider && Satisforestry.isPinkForest(evt.entity.worldObj, evt.entityX, evt.entityZ)) {
			if (DragonAPICore.rand.nextInt(3) > 0)
				evt.setResult(Result.ALLOW);
		}
	}

	@SubscribeEvent
	public void mobSpawnY(GetYToSpawnMobEvent evt) {
		if (Satisforestry.isPinkForest(evt.world, evt.xCoord, evt.zCoord)) {
			int dy = evt.yToTry-1;
			Block at = evt.world.getBlock(evt.xCoord, dy, evt.zCoord);
			while (evt.yToTry > 1 && (at == Blocks.air || at == Satisforestry.log || at == Satisforestry.leaves || at.isWood(evt.world, evt.xCoord, dy, evt.zCoord) || at.isLeaves(evt.world, evt.xCoord, dy, evt.zCoord) || ReikaWorldHelper.softBlocks(evt.world, evt.xCoord, dy, evt.zCoord))) {
				evt.yToTry--;
				dy--;
				at = evt.world.getBlock(evt.xCoord, dy, evt.zCoord);
			}
			//ReikaJavaLibrary.pConsole(evt.yToTry+" from "+evt.yCoord+" > "+evt.world.getBlock(evt.xCoord, evt.yCoord, evt.zCoord)+" to "+evt.world.getBlock(evt.xCoord, evt.yToTry, evt.zCoord), evt.yCoord != evt.yToTry);
			/*
			int top = DecoratorPinkForest.getTrueTopAt(evt.world, evt.xCoord, evt.zCoord)+1;
			evt.yToTry = Math.min(evt.yToTry, top);
			if (false && evt.world.rand.nextInt(2) == 0) {
				evt.yToTry = top;
			}*/
		}
	}

	@SubscribeEvent
	public void snowThroughPinkLeaves(BlockStopsPrecipitationEvent evt) {
		if (evt.block == Satisforestry.leaves) {
			evt.setResult(Result.DENY);
		}
		else if (evt.block == Satisforestry.log && evt.getMetadata()%4 == 1) {
			evt.setResult(Result.DENY);
		}
	}

	@SubscribeEvent
	public void noPumpkins(Decorate evt) {
		if (evt.type == Decorate.EventType.PUMPKIN && Satisforestry.isPinkForest(evt.world, evt.chunkX+8, evt.chunkZ+8)) {
			evt.setResult(Result.DENY);
		}
	}

	@SubscribeEvent
	public void preventCliffBeaches(GenLayerBeachEvent evt) {
		if (Satisforestry.isPinkForest(evt.originalBiomeID)) {
			evt.beachIDToPlace = BiomeGenBase.beach.biomeID;
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void updateRendering(ClientTickEvent evt) {
		BiomePinkForest.updateRenderFactor(Minecraft.getMinecraft().thePlayer);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void setBiomeHorizonColor(FogColors evt) {
		if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null)
			return;
		if (Minecraft.getMinecraft().thePlayer.isInsideOfMaterial(Material.lava) || Minecraft.getMinecraft().thePlayer.isInsideOfMaterial(Material.water))
			return;
		float f = BiomePinkForest.renderFactor*(float)ReikaMathLibrary.normalizeToBounds(ReikaWorldHelper.getSunIntensity(Minecraft.getMinecraft().theWorld, false, (float)evt.renderPartialTicks), 0, 1, 0.2, 0.8);
		if (f > 0) {
			int color = 0xFFABF6;
			evt.red = evt.red*(1-f)+f*ReikaColorAPI.getRed(color)/255F;
			evt.green = evt.green*(1-f)+f*ReikaColorAPI.getGreen(color)/255F;
			evt.blue = evt.blue*(1-f)+f*ReikaColorAPI.getBlue(color)/255F;
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	@ModDependent(ModList.BLOODMAGIC)
	@ClassDependent("WayofTime.alchemicalWizardry.api.event.TeleposeEvent")
	public void noTelepose(TeleposeEvent evt) {
		if (!this.isMovable(evt.initialBlock) || !this.isMovable(evt.finalBlock))
			evt.setCanceled(true);
	}

	private boolean isMovable(Block b) {
		return b != SFBlocks.HARVESTER.getBlockInstance() && !UraniumCave.instance.isSpecialCaveBlock(b);
	}

}
