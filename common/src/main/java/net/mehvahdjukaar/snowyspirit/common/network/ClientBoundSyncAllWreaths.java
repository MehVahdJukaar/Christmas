package net.mehvahdjukaar.snowyspirit.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashSet;
import java.util.Set;


public class ClientBoundSyncAllWreaths implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf,ClientBoundSyncAllWreaths> CODEC =
            Message.makeType(SnowySpirit.res("sync_all_wreaths"), ClientBoundSyncAllWreaths::new);

    public final Set<BlockPos> pos;

    public ClientBoundSyncAllWreaths(RegistryFriendlyByteBuf buffer) {
        int count = buffer.readInt();
        this.pos = new HashSet<>();
        for (int i = 0; i < count; i++) {
            this.pos.add(buffer.readBlockPos());
        }
    }

    public ClientBoundSyncAllWreaths(Set<BlockPos> pos) {
        this.pos = pos;
    }

    @Override
    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(this.pos.size());
        for (BlockPos p : this.pos) {
            buffer.writeBlockPos(p);
        }
    }

    @Override
    public void handle(Context context) {
        ClientReceivers.handleSyncAlWreathsPacket(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}