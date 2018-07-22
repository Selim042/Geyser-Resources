package selim.geyser.resources.bukkit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import selim.geyser.core.bukkit.network.NetworkHandler;
import selim.geyser.core.shared.EnumComponent;
import selim.geyser.core.shared.IGeyserCorePlugin;
import selim.geyser.core.shared.IGeyserPlugin;
import selim.geyser.resources.bukkit.packets.PacketPackData;
import selim.geyser.resources.bukkit.packets.PacketPackHeader;
import selim.geyser.resources.bukkit.packets.PacketPackList;
import selim.geyser.resources.shared.GeyserResourcesInfo;

public class GeyserResourcesSpigot extends JavaPlugin
		implements Listener, IGeyserCorePlugin, IGeyserPlugin {

	public static final int DATA_PACKET_SIZE = 1000;

	public static Logger LOGGER;
	public static GeyserResourcesSpigot INSTANCE;
	public static NetworkHandler NETWORK;
	public static File ZIP_FOLDER;

	@Override
	public void onEnable() {
		LOGGER = this.getLogger();
		INSTANCE = this;

		NETWORK = NetworkHandler.registerChannel(this, GeyserResourcesInfo.CHANNEL);
		NETWORK.registerPacket(GeyserResourcesInfo.PacketDiscrimators.PACK_HEADER,
				PacketPackHeader.class);
		NETWORK.registerPacket(GeyserResourcesInfo.PacketDiscrimators.PACK_DATA, PacketPackData.class);
		NETWORK.registerPacket(GeyserResourcesInfo.PacketDiscrimators.PACK_LIST, PacketPackList.class,
				PacketPackList.Handler.class);

		ZIP_FOLDER = new File(this.getDataFolder(), "packs");
		if (!ZIP_FOLDER.exists())
			ZIP_FOLDER.mkdirs();

		PluginManager manager = this.getServer().getPluginManager();
		manager.registerEvents(this, this);
	}

	private static final Map<String, File> USED_ZIPS = new HashMap<>();
	private static final List<String> USED_ZIP_NAMES = new CopyOnWriteArrayList<>();
	// private static final List<File> USED_ZIPS = new CopyOnWriteArrayList<>();

	public static File getPack(String name) {
		return USED_ZIPS.get(name);
	}

	@EventHandler
	public void onPluginEnable(PluginEnableEvent event) {
		this.getFile();
		if (!(event.getPlugin() instanceof JavaPlugin))
			return;
		JavaPlugin plugin = (JavaPlugin) event.getPlugin();
		if (plugin instanceof IGeyserPlugin) {
			IGeyserPlugin geyserPlugin = (IGeyserPlugin) plugin;
			if (arrContains(EnumComponent.RESOURCES, geyserPlugin.requiredComponents())) {
				String pluginName = plugin.getName();
				String pluginVersion = plugin.getDescription().getVersion();
				String zipName = getZipName(pluginName, pluginVersion);
				File zipFile = new File(ZIP_FOLDER, zipName);
				if (shouldCreateZip(pluginName, pluginVersion)) {
					LOGGER.info("Creating asset pack for " + pluginName + " version " + pluginVersion
							+ "." + zipFile);
					try {
						File file = new File(plugin.getClass().getProtectionDomain().getCodeSource()
								.getLocation().getPath());
						JarFile jar = new JarFile(file);
						if (!zipFile.exists())
							zipFile.createNewFile();
						ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile));
						transferFiles(pluginName, zip, jar);
					} catch (IllegalArgumentException | SecurityException | IOException e) {
						e.printStackTrace();
					}
				}
				USED_ZIPS.put(zipName, zipFile);
				// USED_ZIPS.add(new File(ZIP_FOLDER, zipName));
				USED_ZIP_NAMES.add(zipName);
			}
		}
	}

	private static void transferFiles(String pluginName, ZipOutputStream zip, JarFile jar) {
		String pathPrefix = "resources/assets/" + pluginName.toLowerCase() + "/";
		Enumeration<JarEntry> entries = jar.entries();
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			if (entry != null && entry.getName().startsWith(pathPrefix)) {
				String fileName = entry.getName()
						.substring(entry.getName().indexOf(pathPrefix) + pathPrefix.length());
				try {
					zip.putNextEntry(new ZipEntry(fileName));
					InputStream inputStream = jar.getInputStream(entry);
					byte[] bytes = new byte[inputStream.available()];
					inputStream.read(bytes);
					zip.write(bytes);
					inputStream.close();
					zip.closeEntry();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			zip.close();
			jar.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean shouldCreateZip(String pluginName, String version) {
		if (ZIP_FOLDER.listFiles() == null || ZIP_FOLDER.list().length == 0)
			return true;
		return arrContains(getZipName(pluginName, version), ZIP_FOLDER.list());
	}

	private static String getZipName(String pluginName, String version) {
		return pluginName + "-" + version + ".zip";
	}

	private int getPing(Player player) {
		try {
			Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
			return (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | NoSuchFieldException e) {
			this.getLogger().log(Level.INFO, "Unable to get ping for " + player.getDisplayName()
					+ ", encountered a " + e.getClass().getName());
			e.printStackTrace();
			return -1;
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		int ping = getPing(player);
		if (ping <= 0)
			ping = 40;
		else
			ping = ping / 25;
		this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

			@Override
			public void run() {
				if (player.getListeningPluginChannels().contains(GeyserResourcesInfo.CHANNEL)) {
					NETWORK.sendPacket(player, new PacketPackList(USED_ZIP_NAMES));
				}
			}
		}, ping);
	}

	@Override
	public EnumComponent[] providedComponents() {
		return new EnumComponent[] { EnumComponent.RESOURCES };
	}

	@SafeVarargs
	private static <T> boolean arrContains(T t, T... ttt) {
		if (ttt == null)
			return false;
		for (T tt : ttt)
			if (tt != null && tt.equals(t))
				return true;
		return false;
	}

	@Override
	public EnumComponent[] requiredComponents() {
		return new EnumComponent[] { EnumComponent.RESOURCES };
	}

	@Override
	public boolean requiredOnClient(EnumComponent component) {
		return false;
	}

}
