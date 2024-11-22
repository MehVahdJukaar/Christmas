package net.mehvahdjukaar.snowyspirit.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;

public class ModNetworking {

    public static void init() {
        System.out.println("ModMessages init");
        NetworkHelper.addNetworkRegistration(event -> {
        System.out.println("Registration event");
                    event.registerServerBound(ServerBoundUpdateSledState.CODEC);
                    event.registerClientBound(ClientBoundSyncWreathMessage.CODEC);
                    event.registerClientBound(ClientBoundSyncAllWreaths.CODEC);
                },
                3);
    }

}