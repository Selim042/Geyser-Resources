package selim.geyser.resources.bukkit.packets;

import io.netty.buffer.ByteBuf;
import selim.geyser.core.bukkit.BukkitByteBufUtils;
import selim.geyser.core.bukkit.network.GeyserPacket;

public class PacketPackHeader extends GeyserPacket {

	private String name;
	private int numPackets;

	public PacketPackHeader() {}

	public PacketPackHeader(String name, int numPackets) {
		this.name = name;
		this.numPackets = numPackets;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.name = BukkitByteBufUtils.readUTF8String(buf);
		this.numPackets = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		BukkitByteBufUtils.writeUTF8String(buf, this.name);
		buf.writeInt(this.numPackets);
	}

}
