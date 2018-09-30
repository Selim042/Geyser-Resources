package selim.geyser.resources.forge.packets;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import selim.geyser.core.shared.SharedByteBufUtils;
import selim.geyser.resources.forge.PackManager;

public class PacketPackData implements IMessage {

	private byte[] data;

	@Override
	public void fromBytes(ByteBuf buf) {
		this.data = SharedByteBufUtils.readByteArray(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		SharedByteBufUtils.writeByteArray(buf, this.data);
	}

	public static class Handler implements IMessageHandler<PacketPackData, IMessage> {

		@Override
		public IMessage onMessage(PacketPackData message, MessageContext ctx) {
			PackManager.addDataPacket(message.data);
			return null;
		}

	}

}
