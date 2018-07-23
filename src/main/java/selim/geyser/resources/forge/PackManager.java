package selim.geyser.resources.forge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import scala.actors.threadpool.Arrays;

public class PackManager {

	public static final File PACK_FOLDER;
	private static List<File> USED_PACKS;
	private static int NUM_PACKS;

	private static String PACK_NAME;
	private static int TOTAL = -1;
	private static int REMAINING = -1;
	private static byte[] MD5;
	private static final List<Byte[]> PACKETS = new CopyOnWriteArrayList<>();

	static {
		String home = System.getProperty("user.home");
		String os = System.getProperty("os.name");
		if (os.startsWith("Windows"))
			home += "\\AppData\\Roaming\\.minecraft\\geyser\\resources";
		else if (os.startsWith("Mac"))
			home += "/Library/Application Support/minecraft/geyser/resources";
		else
			home += "/.minecraft/geyser/resources";
		PACK_FOLDER = new File(home);
		if (!PACK_FOLDER.exists())
			PACK_FOLDER.mkdirs();
	}

	public static void setNumPacks(int numPacks) {
		NUM_PACKS = numPacks;
	}

	public static int getNumPacks() {
		return NUM_PACKS;
	}

	public static String getDownloadingPack() {
		if (PACK_NAME == null)
			return "MISSINGNO";
		return PACK_NAME;
	}

	public static int getDownloadingProgress() {
		return (100 * (TOTAL - REMAINING)) / TOTAL;
	}

	public static void addUsedPack(String pack) {
		USED_PACKS.add(new File(PACK_FOLDER, pack));
	}

	public static void startDataList(String name, int numPackets, byte[] md5) {
		GeyserResourcesForge.LOGGER
				.info("Downloading asset pack " + name + " in " + numPackets + " parts.");
		if (numPackets <= 0)
			return;
		PACK_NAME = name;
		TOTAL = numPackets;
		REMAINING = numPackets;
		MD5 = md5;
		PACKETS.clear();
	}

	public static void addDataPacket(byte[] data) {
		addDataPacket(toWrapper(data));
	}

	public static void addDataPacket(Byte[] data) {
		if (PACKETS == null)
			return;
		PACKETS.add(data);
		REMAINING--;
		if (REMAINING == 0)
			finalizePack();
	}

	private static void finalizePack() {
		if (PACKETS.isEmpty() || REMAINING > 0 || PACK_NAME == null)
			return;
		List<Byte[]> packets = new ArrayList<>(PACKETS);
		PACKETS.clear();
		String packName = new String(PACK_NAME);
		PACK_NAME = null;
		TOTAL = -1;
		REMAINING = -1;
		NUM_PACKS--;
		// Minecraft.getMinecraft().addScheduledTask(new Runnable() {
		//
		// @Override
		// public void run() {
		File packFile = new File(PACK_FOLDER, packName);
		try {
			OutputStream stream = new FileOutputStream(packFile);
			for (Byte[] data : packets)
				for (Byte b : data)
					stream.write(b);
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			DigestInputStream digest = new DigestInputStream(new FileInputStream(packFile), md);
			byte[] md5 = digest.getMessageDigest().digest();
			digest.close();
			if (!Arrays.equals(md5, MD5)) {
				GeyserResourcesForge.LOGGER
						.error(packName + " failed checksum validation.  Deleting pack.");
				packFile.delete();
			}
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
		// }
		// });
		// PACKETS.clear();
		// PACK_NAME = null;
		if (NUM_PACKS == 0)
			applyPacks();
	}

	}

	private static Byte[] toWrapper(byte[] arr) {
		Byte[] wrapArr = new Byte[arr.length];
		for (int i = 0; i < arr.length; i++)
			wrapArr[i] = arr[i];
		return wrapArr;
	}

	public static boolean hasPack(String name) {
		return arrContains(name, PACK_FOLDER.list());
	}

	@SafeVarargs
	private static <T> boolean arrContains(T t, T... ttt) {
		if (ttt == null)
			return t == null;
		for (T tt : ttt)
			if (tt != null && tt.equals(t))
				return true;
		return false;
	}

}
