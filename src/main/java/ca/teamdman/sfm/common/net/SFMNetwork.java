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
        WRAPPER.registerMessage(PacketProgramEditReply.Handler.class, PacketProgramEditReply.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(
            PacketDebugTerminalRequest.Handler.class,
            PacketDebugTerminalRequest.class,
            id++,
            Side.SERVER);
        WRAPPER
            .registerMessage(PacketDebugTerminalReply.Handler.class, PacketDebugTerminalReply.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(PacketStationRequest.Handler.class, PacketStationRequest.class, id++, Side.SERVER);
        WRAPPER.registerMessage(PacketStationReply.Handler.class, PacketStationReply.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(PacketLabelGunSession.Handler.class, PacketLabelGunSession.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(
            PacketStationDebugRequest.Handler.class,
            PacketStationDebugRequest.class,
            id++,
            Side.SERVER);
        WRAPPER
            .registerMessage(PacketStationDebugReply.Handler.class, PacketStationDebugReply.class, id++, Side.CLIENT);
    }
}
