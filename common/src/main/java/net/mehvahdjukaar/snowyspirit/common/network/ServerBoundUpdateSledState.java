package net.mehvahdjukaar.snowyspirit.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.mehvahdjukaar.snowyspirit.common.entity.SledEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

public class ServerBoundUpdateSledState implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf,ServerBoundUpdateSledState> CODEC =
            Message.makeType(SnowySpirit.res("s2c_update_sled_movement"), ServerBoundUpdateSledState::new);

    public final float clientDx;
    public final float clientDy;
    public final float clientDz;

    public ServerBoundUpdateSledState(RegistryFriendlyByteBuf buffer) {
        this.clientDx = buffer.readFloat();
        this.clientDy = buffer.readFloat();
        this.clientDz = buffer.readFloat();
    }

    public ServerBoundUpdateSledState(Vec3 movement) {
        this.clientDx = (float) movement.x;
        this.clientDy = (float) movement.y;
        this.clientDz = (float) movement.z;

    }

    @Override
    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeFloat(this.clientDx);
        buffer.writeFloat(this.clientDy);
        buffer.writeFloat(this.clientDz);
    }

    @Override
    public void handle(Context context) {
        if (context.getPlayer().getVehicle() instanceof SledEntity sled) {
            sled.setSyncedMovement(this.clientDx, this.clientDy, this.clientDx);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}