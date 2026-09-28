package ca.teamdman.sfm.common.net;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public final class SFMNetwork {

    public static final SimpleNetworkWrapper WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel("sfm");

    public static void init() {
        int id = 0;
        WRAPPER.registerMessage(PacketSaveProgram.Handler.class, PacketSaveProgram.class, id++, Side.SERVER);
        WRAPPER.registerMessage(PacketSetLabel.Handler.class, PacketSetLabel.class, id++, Side.SERVER);
        WRAPPER.registerMessage(PacketLabelGunAction.Handler.class, PacketLabelGunAction.class, id++, Side.SERVER);
        WRAPPER
            .registerMessage(PacketManagerSetLogLevel.Handler.class, PacketManagerSetLogLevel.class, id++, Side.SERVER);
        WRAPPER.registerMessage(PacketManagerClearLogs.Handler.class, PacketManagerClearLogs.class, id++, Side.SERVER);
        WRAPPER
            .registerMessage(PacketManagerRequestLogs.Handler.class, PacketManagerRequestLogs.class, id++, Side.SERVER);
        WRAPPER.registerMessage(PacketManagerLogs.Handler.class, PacketManagerLogs.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(PacketManagerAction.Handler.class, PacketManagerAction.class, id++, Side.SERVER);
    }
}
