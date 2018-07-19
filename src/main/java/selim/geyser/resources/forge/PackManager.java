package selim.geyser.resources.forge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class PackManager {

	private static final File PACK_FOLDER;
	private static final Map<String, List<Byte[]>> PACKETS = new HashMap<>();
	private static final Map<String, Integer> REMAINING = new HashMap<>();

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

	public static void startDataList(String name, int numPackets) {
		PACKETS.put(name, new CopyOnWriteArrayList<>());
		REMAINING.put(name, numPackets);
	}

	public static void addDataPacket(String name, byte[] data) {
		addDataPacket(name, toWrapper(data));
	}

	public static void addDataPacket(String name, Byte[] data) {
		List<Byte[]> dataList = PACKETS.get(name);
		if (dataList == null)
			return;
		dataList.add(data);
		REMAINING.put(name, REMAINING.get(name) - 1);
		if (REMAINING.get(name) == 0)
			finalizePack(name);
	}

	public static void finalizePack(String name) {
		if (!PACKETS.containsKey(name))
			return;
		File packFile = new File(PACK_FOLDER, name + ".zip");
		try {
			OutputStream stream = new FileOutputStream(packFile);
			List<Byte[]> dataList = PACKETS.get(name);
			for (Byte[] data : dataList)
				for (Byte b : data)
					stream.write(b);
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		PACKETS.remove(name);
		REMAINING.remove(name);
	}

	private static Byte[] toWrapper(byte[] arr) {
		Byte[] wrapArr = new Byte[arr.length];
		for (int i = 0; i < arr.length; i++)
			wrapArr[i] = arr[i];
		return wrapArr;
	}

}
