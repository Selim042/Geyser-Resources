package selim.geyser.resources.bukkit.packets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import io.netty.buffer.ByteBuf;
import selim.geyser.core.bukkit.BukkitByteBufUtils;
import selim.geyser.core.bukkit.network.GeyserPacket;
import selim.geyser.core.bukkit.network.GeyserPacketHandler;
import selim.geyser.resources.bukkit.GeyserResourcesSpigot;

public class PacketPackList extends GeyserPacket {

	private List<String> zips;

	public PacketPackList() {
		this.zips = new ArrayList<>();
	}

	public PacketPackList(List<String> files) {
		this.zips = new ArrayList<String>(files);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.zips.size());
		for (String zip : zips)
			BukkitByteBufUtils.writeUTF8String(buf, zip);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int size = buf.readInt();
		for (int i = 0; i < size; i++)
			zips.add(BukkitByteBufUtils.readUTF8String(buf));
	}

	public static class Handler extends GeyserPacketHandler<PacketPackList, GeyserPacket> {

		@Override
		public GeyserPacket handle(Player player, PacketPackList packet) {
			for (String pack : packet.zips)
				Bukkit.getScheduler().scheduleSyncDelayedTask(GeyserResourcesSpigot.INSTANCE,
						new TransferRunnable(player, pack));
			return null;
		}

		private static class TransferRunnable implements Runnable {

			private final Player player;
			private final String pack;

			public TransferRunnable(Player player, String pack) {
				this.player = player;
				this.pack = pack;
			}

			@Override
			public void run() {
				try {
					File file = GeyserResourcesSpigot.getPack(pack);
					if (!file.exists()) {
						GeyserResourcesSpigot.LOGGER.info("Could not find requested asset pack " + pack);
						GeyserResourcesSpigot.NETWORK.sendPacket(player, new PacketPackHeader(pack, 0));
						return;
					}
					FileInputStream inputStream = new FileInputStream(file);
					int numPackets = (inputStream.available() / GeyserResourcesSpigot.DATA_PACKET_SIZE)
							+ 1;
					GeyserResourcesSpigot.NETWORK.sendPacket(player,
							new PacketPackHeader(pack, numPackets));
					for (int i = 0; i < numPackets; i++) {
						byte[] bytes = new byte[GeyserResourcesSpigot.DATA_PACKET_SIZE];
						inputStream.read(bytes, i * GeyserResourcesSpigot.DATA_PACKET_SIZE,
								GeyserResourcesSpigot.DATA_PACKET_SIZE);
						GeyserResourcesSpigot.NETWORK.sendPacket(player, new PacketPackData(bytes));
					}
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
