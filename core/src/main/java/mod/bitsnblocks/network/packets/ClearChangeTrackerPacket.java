package mod.bitsnblocks.network.packets;

import mod.bitsnblocks.api.change.IChangeTracker;
import mod.bitsnblocks.api.change.IChangeTrackerManager;
import mod.bitsnblocks.api.util.constants.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class ClearChangeTrackerPacket extends ModPacket
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "clear_change_tracker");
    public static final CustomPacketPayload.Type<ClearChangeTrackerPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public ClearChangeTrackerPacket(RegistryFriendlyByteBuf byteBuf)
    {
        this.readPayload(byteBuf);
    }

    public ClearChangeTrackerPacket()
    {
    }

    @Override
    public void writePayload(final RegistryFriendlyByteBuf buffer)
    {
    }

    @Override
    public void readPayload(final RegistryFriendlyByteBuf buffer)
    {
    }

    @Override
    public void server(final ServerPlayer playerEntity)
    {
        final IChangeTracker tracker = IChangeTrackerManager.getInstance().getChangeTracker(playerEntity);
        tracker.clear();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
