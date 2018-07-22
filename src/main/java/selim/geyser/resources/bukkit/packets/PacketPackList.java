package selim.geyser.resources.bukkit.packets;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import io.netty.buffer.ByteBuf;
import selim.geyser.core.bukkit.BukkitByteBufUtils;
import selim.geyser.core.bukkit.network.GeyserPacket;
import selim.geyser.core.bukkit.network.GeyserPacketHandler;

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
			
			return null;
		}

	}

}
