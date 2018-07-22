package selim.geyser.resources.forge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PackManager {

	private static final File PACK_FOLDER;
	private static String PACK_NAME;
	private static int REMAINING = -1;
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

	public static void startDataList(String name, int numPackets) {
		PACK_NAME = name;
		REMAINING = numPackets;
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
		File packFile = new File(PACK_FOLDER, PACK_NAME + ".zip");
		try {
			OutputStream stream = new FileOutputStream(packFile);
			for (Byte[] data : PACKETS)
				for (Byte b : data)
					stream.write(b);
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		PACKETS.clear();
		PACK_NAME = null;
		REMAINING = -1;
	}

	private static Byte[] toWrapper(byte[] arr) {
		Byte[] wrapArr = new Byte[arr.length];
		for (int i = 0; i < arr.length; i++)
			wrapArr[i] = arr[i];
		return wrapArr;
	}

}
