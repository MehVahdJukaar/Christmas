package net.mehvahdjukaar.snowyspirit.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;

public class ModMessages {

    public static void init() {
        NetworkHelper.addNetworkRegistration(event -> {
                    event.registerServerBound(ServerBoundUpdateSledState.CODEC);
                    event.registerClientBound(ClientBoundSyncWreathMessage.CODEC);
                    event.registerClientBound(ClientBoundSyncAllWreaths.CODEC);
                },
                3);
    }

}