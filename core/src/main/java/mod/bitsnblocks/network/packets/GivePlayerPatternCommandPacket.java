package mod.bitsnblocks.network.packets;

import com.communi.suggestu.scena.core.entity.IPlayerInventoryManager;
import mod.bitsnblocks.api.item.multistate.IMultiStateItemStack;
import mod.bitsnblocks.api.multistate.snapshot.IMultiStateSnapshot;
import mod.bitsnblocks.api.util.constants.Constants;
import mod.bitsnblocks.registrars.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class GivePlayerPatternCommandPacket extends ModPacket
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "give_player_pattern_command");
    public static final CustomPacketPayload.Type<GivePlayerPatternCommandPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    private IMultiStateSnapshot snapshot;

    public GivePlayerPatternCommandPacket(IMultiStateSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public GivePlayerPatternCommandPacket(final RegistryFriendlyByteBuf buffer)
    {
        readPayload(buffer);
    }

    @Override
    public void writePayload(final RegistryFriendlyByteBuf buffer)
    {
        IMultiStateSnapshot.STREAM_CODEC.encode(buffer, snapshot);
    }

    @Override
    public void readPayload(final RegistryFriendlyByteBuf buffer)
    {
        snapshot = IMultiStateSnapshot.STREAM_CODEC.decode(buffer);
    }

    @Override
    public void server(final ServerPlayer playerEntity)
    {
        final Item item = ModItems.SINGLE_USE_PATTERN_ITEM.get();
        final IMultiStateItemStack patternStack = this.snapshot.toItemStack();

        IPlayerInventoryManager.getInstance().giveToPlayer(playerEntity, patternStack.toPatternStack());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
