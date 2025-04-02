package mod.bitsnblocks.network.packets;

import mod.bitsnblocks.api.item.withmode.IWithModeItem;
import mod.bitsnblocks.api.util.constants.Constants;
import mod.bitsnblocks.utils.ItemStackUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class HeldToolModeChangedPacket extends ModPacket
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "held_tool_mode_changed");
    public static final CustomPacketPayload.Type<HeldToolModeChangedPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    private int modeIndex;

    public HeldToolModeChangedPacket(final RegistryFriendlyByteBuf buffer)
    {
        readPayload(buffer);
    }

    public HeldToolModeChangedPacket(final int modeIndex)
    {
        this.modeIndex = modeIndex;
    }

    @Override
    public void writePayload(final RegistryFriendlyByteBuf buffer)
    {
        buffer.writeVarInt(this.modeIndex);
    }

    @Override
    public void readPayload(final RegistryFriendlyByteBuf buffer)
    {
        this.modeIndex = buffer.readVarInt();
    }

    @Override
    public void server(final ServerPlayer playerEntity)
    {
        final ItemStack stack = ItemStackUtils.getModeItemStackFromPlayer(playerEntity);
        if (stack.getItem() instanceof IWithModeItem) {
            final IWithModeItem<?> modeItem = (IWithModeItem<?>) stack.getItem();
            modeItem.setMode(stack, modeIndex);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
