package selim.geyser.resources.forge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import scala.actors.threadpool.Arrays;

@Mod.EventBusSubscriber
public class PackManager {

	public static final File PACK_FOLDER;
	private static final List<File> USED_PACKS = new CopyOnWriteArrayList<>();
	private static int NUM_PACKS;

	private static String PACK_NAME;
	private static int TOTAL = -1;
	private static int REMAINING = -1;
	private static byte[] MD5;
	private static final List<Byte[]> PACKETS = new CopyOnWriteArrayList<>();

	private static final List<IResourcePack> LOADED_PACKS = new CopyOnWriteArrayList<>();

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
		if (numPacks == 0)
			applyPacks();
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

	private static void unloadOldPacks() {
		ResourcePackRepository repo = Minecraft.getMinecraft().getResourcePackRepository();
		List<ResourcePackRepository.Entry> list = ReflectionHelper
				.getPrivateValue(ResourcePackRepository.class, repo, "repositoryEntries");
		for (ResourcePackRepository.Entry e : Collections.unmodifiableList(list)) {
			if (e != null && LOADED_PACKS.contains(e.getResourcePack())) {
				list.remove(e);
				LOADED_PACKS.remove(e.getResourcePack());
			}
		}
	}

	private static void applyPacks() {
		unloadOldPacks();
		ResourcePackRepository repo = Minecraft.getMinecraft().getResourcePackRepository();
		List<ResourcePackRepository.Entry> list = ReflectionHelper
				.getPrivateValue(ResourcePackRepository.class, repo, "repositoryEntries");
		try {
			Constructor<ResourcePackRepository.Entry> contructor = ResourcePackRepository.Entry.class
					.getDeclaredConstructor(ResourcePackRepository.class, File.class);
			Constructor.setAccessible(new AccessibleObject[] { contructor }, true);
			for (File pack : USED_PACKS) {
				ResourcePackRepository.Entry entry = contructor.newInstance(repo, pack);
				list.add(entry);
				LOADED_PACKS.add(entry.getResourcePack());
			}
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | InstantiationException | NoSuchMethodException e) {
			GeyserResourcesForge.LOGGER.error("Error encountered when loading Geyser asset packs");
			e.printStackTrace();
		}
		Minecraft.getMinecraft().scheduleResourcesRefresh();
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

	@SubscribeEvent
	public static void onDisconnect(ClientDisconnectionFromServerEvent event) {
		unloadOldPacks();
		Minecraft.getMinecraft().scheduleResourcesRefresh();
	}

}
