package Reika.Satisforestry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.HashBiMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.Auxiliary.PopupWriter;
import Reika.DragonAPI.Exception.InstallationException;
import Reika.DragonAPI.IO.DirectResourceManager;
import Reika.DragonAPI.Instantiable.Event.Client.PlayMusicEvent;
import Reika.DragonAPI.Instantiable.IO.CustomMusic;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.Java.ReikaObfuscationHelper;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.StreamThread;

@SideOnly(Side.CLIENT)
public class SFMusic {

	public static final SFMusic instance = new SFMusic();

	private static final String potentialSupportMsg = "This file type can be supported with the use of the NotEnoughCodecs Mod. ";
	private static final String officialSiteMsg = "The mod can be downloaded from its official CurseForge page: https://www.curseforge.com/minecraft/mc-mods/notenoughcodecs/files";
	private static final String backportMsg = "To support this file type, you will need a backported version of a modern version for new filetype support: https://gregtech.overminddl1.com/openmods/codecs/NotEnoughCodecs/1.7.10-0.6/NotEnoughCodecs-1.7.10-0.6.jar";

	private int potentiallySupportedFiles = 0;
	private boolean requiresBackportedNEC = false;

	private final HashBiMap<String, SFMusicEntry> musicCache = HashBiMap.create();

	private SFMusicEntry currentMusic;

	private SFMusic() {

	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void playCustomMusic(ClientTickEvent evt) {
		World world = Minecraft.getMinecraft().theWorld;
		EntityPlayer ep = Minecraft.getMinecraft().thePlayer;
		if (world != null && ep != null && Satisforestry.isPinkForest(world, MathHelper.floor_double(ep.posX), MathHelper.floor_double(ep.posZ)))
			this.tickMusicEngine(world);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void overrideMusic(PlayMusicEvent evt) {
		World world = Minecraft.getMinecraft().theWorld;
		EntityPlayer ep = Minecraft.getMinecraft().thePlayer;
		if (world != null && ep != null && Satisforestry.isPinkForest(world, MathHelper.floor_double(ep.posX), MathHelper.floor_double(ep.posZ)))
			evt.setCanceled(true);
	}

	/*
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void addMusicCodecs(SoundSetupEvent evt) {
		loadCodecs();
	}

	private void loadCodecs() {
		try {
			SoundSystemConfig.setCodec("mp3", CodecJLayerMP3.class);
		}
		catch (SoundSystemException e) {
			throw new RegistrationException(Satisforestry.instance, "Could not register music codecs!", e);
		}
	}*/

	public void loadMusic(String path) {
		File folder = new File(path);
		if (folder.exists() && folder.isDirectory()) {
			Satisforestry.logger.log("Loading SF OST from "+path);
			int amt = 0;
			for (DayQuadrant d : DayQuadrant.list) {
				File f = new File(folder, d.name().toLowerCase(Locale.ENGLISH));
				if (f.exists() && f.isDirectory()) {
					amt += this.loadTracks(f, d);
				}
			}
			File f = new File(folder, "all");
			if (f.exists() && f.isDirectory()) {
				amt += this.loadTracks(f, DayQuadrant.list);
			}
			if (amt == 0) {
				Satisforestry.logger.logError("Failed to find any tracks!");
			}
			else {
				Satisforestry.logger.log("Finished loading "+amt+" tracks.");
			}

			if (potentiallySupportedFiles > 0) {
				String msg = potentiallySupportedFiles+" SF OST files were not a supported type, but can be loaded with the NotEnoughCodecs Mod.";
				if (requiresBackportedNEC) {
					msg = msg+" To support their file type, you will need a backported copy of a modern version.";
				}
				msg = msg+" See the log file for more information.";
				PopupWriter.instance.addMessage(msg);
			}
		}
		else {
			throw new InstallationException(Satisforestry.instance, "Specified OST folder does not exist @ "+path);
		}
	}

	private int loadTracks(File f0, DayQuadrant... set) {
		int ret = 0;
		for (File f : f0.listFiles()) {
			if (f.isDirectory()) {
				ret += this.loadTracks(f, set);
				continue;
			}
			MusicSupport type = this.isMusicFile(f);
			if (type == MusicSupport.SUPPORTED) {
				try {
					String path = f.getCanonicalPath();
					ret++;
					for (DayQuadrant d : set) {
						d.addTrack(path);
						Satisforestry.logger.log("Loaded track "+f+" to time "+d);
					}
				}
				catch (IOException e) {
					Satisforestry.logger.logError("Failed to load track "+f+": ");
					e.printStackTrace();
				}
			}
			else {
				Satisforestry.logger.logError("Track "+f+" is not a recognized audio file!");
				if (type == MusicSupport.MP3 || type == MusicSupport.FLAC) {
					potentiallySupportedFiles++;
					boolean official = type == MusicSupport.MP3 || !MinecraftForge.MC_VERSION.startsWith("1.7");
					if (!official)
						requiresBackportedNEC = true;
					String msg = potentialSupportMsg;
					if (official) {
						msg = msg+" "+officialSiteMsg;
					}
					else {
						msg = msg+" "+backportMsg;
					}
					Satisforestry.logger.log(msg);
				}
			}
		}
		return ret;
	}

	private MusicSupport isMusicFile(File f) {
		String n = f.getName();
		String ext = n.substring(n.lastIndexOf('.')+1).toLowerCase(Locale.ENGLISH);
		if (SoundSystemConfig.getCodec(f.getName()) != null)
			return MusicSupport.SUPPORTED;
		else if (ext.equals("flac"))
			return MusicSupport.FLAC;
		else if (ext.equals("mp3"))
			return MusicSupport.MP3;
		else
			return MusicSupport.UNSUPPORTED;
	}

	public void tickMusicEngine(World world) {
		if (Minecraft.getMinecraft().gameSettings.getSoundLevel(SFClient.sfCategory) <= 0)
			return;
		SoundHandler sh = Minecraft.getMinecraft().getSoundHandler();
		StreamThread th = ReikaSoundHelper.getStreamingThread(sh);
		if (th == null || !th.isAlive()) {
			sh.stopSounds();
			ReikaSoundHelper.restartStreamingSystem(sh);
		}
		//ReikaJavaLibrary.pConsole(s.path+":"+sh.isSoundPlaying(s));
		if (currentMusic != null && ReikaObfuscationHelper.isDeObfEnvironment() && Keyboard.isKeyDown(Keyboard.KEY_END)) {
			sh.stopSound(currentMusic);
			return;
		}
		if (currentMusic != null && sh.isSoundPlaying(currentMusic)) {
			return;
		}
		currentMusic = this.selectTrack(world);
		//ReikaJavaLibrary.pConsole("Selected at time "+world.getWorldTime()%24000+" = "+DayQuadrant.getByTime(world.getWorldTime())+" : "+currentMusic);
		if (currentMusic != null)
			currentMusic.play(sh);
	}

	private SFMusicEntry selectTrack(World world) {
		DayQuadrant dq = DayQuadrant.getByTime(world.getWorldTime());
		return dq != null ? ReikaJavaLibrary.getRandomListEntry(DragonAPICore.rand, dq.tracks) : null;
	}

	private SFMusicEntry getOrCreateMusic(String path) {
		SFMusicEntry ret = musicCache.get(path);
		if (ret == null) {
			ret = new SFMusicEntry(path);
			musicCache.put(path, ret);
		}
		return ret;
	}

	private static enum MusicSupport {
		SUPPORTED,
		MP3,
		FLAC,
		UNSUPPORTED;
	}

	public static enum DayQuadrant {
		MORNING(0),
		DAY(6000, 4000),
		EVENING(12000),
		NIGHT(18000, 4000);

		private static final DayQuadrant[] list = values();

		private static final long DEFAULT_WINDOW = 1500;

		private final long baseTime;
		private final long timeWindow;

		private final ArrayList<SFMusicEntry> tracks = new ArrayList();

		private DayQuadrant(long time) {
			this(time, DayQuadrant.DEFAULT_WINDOW);
		}

		private DayQuadrant(long time, long window) {
			baseTime = time;
			timeWindow = window;
		}

		public static DayQuadrant getByTime(long time) {
			time = time%24000;
			/*
			if (time < 3000 || time >= 21000) {
				return MORNING;
			}
			else if (time < 9000) {
				return DAY;
			}
			else if (time < 15000) {
				return EVENING;
			}
			else {
				return NIGHT;
			}*/
			for (DayQuadrant dq : list) {
				long difference = time%24000-dq.baseTime;
				while (difference < -12000)
					difference += 24000;
				while (difference > 12000)
					difference -= 24000;
				difference = Math.abs(difference);
				//ReikaJavaLibrary.pConsole(time%24000+", "+dq.baseTime+", "+difference);
				if (difference <= dq.timeWindow)
					return dq;
			}
			return null;
		}

		public void addTrack(String path) throws IOException {
			SFMusicEntry mus = instance.getOrCreateMusic(path);
			DirectResourceManager.getInstance().registerCustomPath(mus.path, SFClient.sfCategory, true);
			tracks.add(mus);
		}
	}

	private static class SFMusicEntry extends CustomMusic {

		public SFMusicEntry(String path) {
			super(path);
		}

	}

}
