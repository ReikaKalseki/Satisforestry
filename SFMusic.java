package Reika.Satisforestry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.world.World;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.Exception.InstallationException;
import Reika.DragonAPI.IO.DirectResourceManager;
import Reika.DragonAPI.Instantiable.IO.CustomMusic;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.Java.ReikaObfuscationHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.StreamThread;

@SideOnly(Side.CLIENT)
public class SFMusic {

	public static final SFMusic instance = new SFMusic();

	private SFMusicEntry currentMusic;

	private SFMusic() {

	}

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
			}
			else if (this.isMusicFile(f)) {
				ret++;
				for (DayQuadrant d : set) {
					try {
						d.addTrack(f);
						Satisforestry.logger.log("Loaded track "+f+" to time "+d);
					}
					catch (IOException e) {
						Satisforestry.logger.logError("Failed to load track "+f+": ");
						e.printStackTrace();
						ret--;
					}
				}
			}
			else {
				Satisforestry.logger.logError("Track "+f+" is not a MC audio file!");
			}
		}
		return ret;
	}

	private boolean isMusicFile(File f) {
		String n = f.getName();
		String ext = n.substring(n.lastIndexOf('.')+1);
		return SoundSystemConfig.getCodec(f.getName()) != null;//ext.equals("ogg")?;// || ext.equals("mp3") || ext.equals("wav");
	}

	public void tickMusicEngine(World world) {
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

		public void addTrack(File f) throws IOException {
			SFMusicEntry mus = new SFMusicEntry(f.getCanonicalPath());
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
