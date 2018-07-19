package selim.geyser.resources.forge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import selim.geyser.core.shared.EnumComponent;
import selim.geyser.core.shared.GeyserCoreInfo;
import selim.geyser.resources.forge.packets.PacketPackData;
import selim.geyser.resources.forge.packets.PacketPackHeader;
import selim.geyser.resources.shared.GeyserResourcesInfo;

@Mod(modid = GeyserResourcesInfo.ID, name = GeyserResourcesInfo.NAME,
		version = GeyserResourcesInfo.VERSION, clientSideOnly = true)
public class GeyserResourcesForge {

	@Mod.Instance(value = GeyserResourcesInfo.ID)
	public static GeyserResourcesForge instance;
	public static final Logger LOGGER = LogManager.getLogger(GeyserCoreInfo.ID);
	public static SimpleNetworkWrapper network;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		network = NetworkRegistry.INSTANCE.newSimpleChannel(GeyserCoreInfo.CHANNEL);
		network.registerMessage(PacketPackHeader.Handler.class, PacketPackHeader.class,
				GeyserResourcesInfo.PacketDiscrimators.PACK_HEADER, Side.CLIENT);
		network.registerMessage(PacketPackData.Handler.class, PacketPackData.class,
				GeyserResourcesInfo.PacketDiscrimators.PACK_DATA, Side.CLIENT);

		FMLInterModComms.sendMessage(GeyserCoreInfo.ID, GeyserCoreInfo.IMC_SEND_KEY,
				EnumComponent.CORE.toString());
	}

}
