package net.mehvahdjukaar.snowyspirit.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;


public class ClientBoundSyncWreathMessage implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf,ClientBoundSyncWreathMessage> CODEC =
            Message.makeType(SnowySpirit.res("sync_wreath_msg"), ClientBoundSyncWreathMessage::new);

    public final BlockPos pos;
    public final boolean hasWreath;

    public ClientBoundSyncWreathMessage(RegistryFriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.hasWreath = buffer.readBoolean();
    }

    public ClientBoundSyncWreathMessage(BlockPos pos, boolean hasWreath) {
        this.pos = pos;
        this.hasWreath = hasWreath;
    }

    @Override
    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeBoolean(this.hasWreath);
    }

    @Override
    public void handle(Context context) {
        ClientReceivers.handleSyncWreathPacket(this);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}