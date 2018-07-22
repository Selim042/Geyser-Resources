package selim.geyser.resources.bukkit.packets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

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
				sendPack(player, pack);
			return null;
		}

		private void sendPack(Player player, String pack) {
			try {
				File file = GeyserResourcesSpigot.getPack(pack);
				if (!file.exists()) {
					GeyserResourcesSpigot.LOGGER.info("Could not find requested asset pack " + pack);
					GeyserResourcesSpigot.NETWORK.sendPacket(player, new PacketPackHeader(pack, 0));
					return;
				}
				try {
					MessageDigest md = MessageDigest.getInstance("MD5");
					DigestInputStream inputStream = new DigestInputStream(new FileInputStream(file), md);
					int numPackets = (inputStream.available() / GeyserResourcesSpigot.DATA_PACKET_SIZE)
							+ 1;
					GeyserResourcesSpigot.NETWORK.sendPacket(player, new PacketPackHeader(pack,
							numPackets, inputStream.getMessageDigest().digest()));
					byte[] data = new byte[inputStream.available()];
					System.out.println("avail: " + inputStream.available());
					inputStream.read(data);
					inputStream.close();
					for (int i = 0; i < numPackets; i++) {
						int length = GeyserResourcesSpigot.DATA_PACKET_SIZE;
						if ((i + 1) * GeyserResourcesSpigot.DATA_PACKET_SIZE > data.length)
							length = data.length - (i * GeyserResourcesSpigot.DATA_PACKET_SIZE);
						System.out.println("length: " + length);
						byte[] bytes = new byte[length];
						for (int i2 = 0; i2 < length; i2++)
							bytes[i2] = data[(i * GeyserResourcesSpigot.DATA_PACKET_SIZE) + i2];
						GeyserResourcesSpigot.NETWORK.sendPacket(player, new PacketPackData(bytes));
					}
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
