package Reika.Satisforestry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.Exception.InstallationException;
import Reika.DragonAPI.IO.DirectResourceManager;
import Reika.DragonAPI.Instantiable.IO.CustomMusic;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.Java.ReikaObfuscationHelper;

import paulscode.sound.StreamThread;

public class SFMusic {

	public static final SFMusic instance = new SFMusic();

	private SFMusicEntry currentMusic;

	private SFMusic() {

	}

	public void loadMusic(String path) {
		File folder = new File(path);
		if (folder.exists() && folder.isDirectory()) {
			Satisforestry.logger.log("Loading SF OST from "+path);
			for (DayQuadrant d : DayQuadrant.list) {
				File f = new File(folder, d.name().toLowerCase(Locale.ENGLISH));
				if (f.exists() && f.isDirectory()) {
					this.loadTracks(f, d);
				}
			}
			File f = new File(folder, "all");
			if (f.exists() && f.isDirectory()) {
				this.loadTracks(f, DayQuadrant.list);
			}
		}
		else {
			throw new InstallationException(Satisforestry.instance, "Specified OST folder does not exist @ "+path);
		}
	}

	private void loadTracks(File f0, DayQuadrant... set) {
		for (File f : f0.listFiles()) {
			if (f.isDirectory()) {
				this.loadTracks(f, set);
			}
			else if (this.isMusicFile(f)) {
				for (DayQuadrant d : set) {
					try {
						d.addTrack(f);
						Satisforestry.logger.log("Loaded track "+f+" to time "+d);
					}
					catch (IOException e) {
						Satisforestry.logger.logError("Failed to load track "+f+": ");
						e.printStackTrace();
					}
				}
			}
			else {
				Satisforestry.logger.logError("Track "+f+" is not a MC audio file!");
			}
		}
	}

	private boolean isMusicFile(File f) {
		String n = f.getName();
		String ext = n.substring(n.lastIndexOf('.'));
		return ext.equals("ogg");// || ext.equals("mp3") || ext.equals("wav");
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
		}
		if (currentMusic != null && sh.isSoundPlaying(currentMusic)) {
			return;
		}
		SFMusicEntry s = this.selectTrack(world);
		if (s != null)
			s.play(sh);
	}

	private SFMusicEntry selectTrack(World world) {
		DayQuadrant dq = DayQuadrant.getByTime(world.getWorldTime());
		return ReikaJavaLibrary.getRandomListEntry(DragonAPICore.rand, dq.tracks);
	}

	public static enum DayQuadrant {
		MORNING(0),
		DAY(6000, 4000),
		EVENING(12000),
		NIGHT(18000, 4000);

		private static final DayQuadrant[] list = values();

		private static final int DEFAULT_WINDOW = 1000;

		private final int baseTime;
		private final int timeWindow;

		private final ArrayList<SFMusicEntry> tracks = new ArrayList();

		private DayQuadrant(int time) {
			this(time, DayQuadrant.DEFAULT_WINDOW);
		}

		private DayQuadrant(int time, int window) {
			baseTime = time;
			timeWindow = window;
		}

		public static DayQuadrant getByTime(long time) {
			time = time%24000;
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
			}
		}

		public void addTrack(File f) throws IOException {
			ResourceLocation loc = DirectResourceManager.getResource(f.getCanonicalPath());

		}
	}

	private static class SFMusicEntry extends CustomMusic {

		public SFMusicEntry(String path) {
			super(path);
		}

	}

}
