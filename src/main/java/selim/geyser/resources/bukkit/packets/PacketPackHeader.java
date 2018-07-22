package selim.geyser.resources.bukkit.packets;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import selim.geyser.core.bukkit.network.GeyserPacket;

public class PacketPackHeader extends GeyserPacket {

	private String name;
	private int numPackets;

	@Override
	public void fromBytes(ByteBuf buf) {
		this.name = ByteBufUtils.readUTF8String(buf);
		this.numPackets = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.name);
		buf.writeInt(this.numPackets);
	}

}
