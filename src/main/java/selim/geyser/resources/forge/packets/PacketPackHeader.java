package selim.geyser.resources.forge.packets;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import selim.geyser.resources.forge.PackManager;

public class PacketPackHeader implements IMessage {

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

	public static class Handler implements IMessageHandler<PacketPackHeader, IMessage> {

		@Override
		public IMessage onMessage(PacketPackHeader message, MessageContext ctx) {
			PackManager.startDataList(message.name, message.numPackets);
			return null;
		}

	}

}
