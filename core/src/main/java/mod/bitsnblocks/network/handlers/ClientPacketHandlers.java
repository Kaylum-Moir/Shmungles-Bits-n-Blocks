package mod.bitsnblocks.network.handlers;

import com.mojang.datafixers.util.Either;
import mod.bitsnblocks.BitsNBlocks;
import mod.bitsnblocks.api.block.entity.IMultiStateBlockEntity;
import mod.bitsnblocks.api.block.entity.INetworkUpdatableEntity;
import mod.bitsnblocks.api.change.IChangeTrackerManager;
import mod.bitsnblocks.api.change.changes.IChange;
import mod.bitsnblocks.api.client.screen.AbstractChiselsAndBitsScreen;
import mod.bitsnblocks.api.client.sharing.IPatternSharingManager;
import mod.bitsnblocks.api.client.sharing.PatternIOException;
import mod.bitsnblocks.api.item.multistate.IMultiStateItemStack;
import mod.bitsnblocks.api.profiling.IProfilerSection;
import mod.bitsnblocks.client.screens.widgets.ChangeTrackerOperationsWidget;
import mod.bitsnblocks.client.clipboard.CreativeClipboardUtils;
import mod.bitsnblocks.item.multistate.SingleBlockMultiStateItemStack;
import mod.bitsnblocks.network.packets.GivePlayerPatternCommandPacket;
import mod.bitsnblocks.profiling.ProfilingManager;
import mod.bitsnblocks.registrars.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Deque;

public final class ClientPacketHandlers
{

    private ClientPacketHandlers()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ClientPacketHandlers. This is a utility class");
    }

    public static void handleChiseledBlockUpdated(final BlockPos blockPos, final FriendlyByteBuf updateData) {
        try(IProfilerSection ignored = ProfilingManager.getInstance().withSection("Handling tile entity update packet")) {
            if (Minecraft.getInstance().level != null) {
                BlockEntity tileEntity = Minecraft.getInstance().level.getBlockEntity(blockPos);
                if (tileEntity == null) {
                    Minecraft.getInstance().level.setBlock(blockPos, ModBlocks.CHISELED_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
                    tileEntity = Minecraft.getInstance().level.getBlockEntity(blockPos);
                }

                if (tileEntity instanceof INetworkUpdatableEntity<?> networkUpdatableEntity) {
                    handleBlockEntityUpdate(networkUpdatableEntity, updateData);
                }
            }
        }
    }

    private static <T> void handleBlockEntityUpdate(INetworkUpdatableEntity<T> blockEntity, final FriendlyByteBuf updateData) {
        final RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(updateData, blockEntity.registryAccess());
        final StreamCodec<RegistryFriendlyByteBuf, T> codec = blockEntity.streamCodec();

        final T payload = codec.decode(buf);

        blockEntity.receivePayload(payload);
    }

    public static void handleChangeTrackerUpdated(final Deque<IChange> tag) {
        IChangeTrackerManager.getInstance().getChangeTracker(Minecraft.getInstance().player).setChanges(tag);
        if(Minecraft.getInstance().screen instanceof AbstractChiselsAndBitsScreen)
        {
            ((AbstractChiselsAndBitsScreen) Minecraft.getInstance().screen).getWidgets()
              .stream()
              .filter(ChangeTrackerOperationsWidget.class::isInstance)
              .map(ChangeTrackerOperationsWidget.class::cast)
              .forEach(ChangeTrackerOperationsWidget::updateState);
        }
    }

    public static void handleNeighborUpdated(final BlockPos toUpdate, final BlockPos from) {
        Minecraft.getInstance().level.getBlockState(toUpdate)
          .handleNeighborChanged(
            Minecraft.getInstance().level,
            toUpdate,
            Minecraft.getInstance().level.getBlockState(from).getBlock(),
            from,
            false
          );
    }

    public static void handleAddMultiStateToClipboard(final ItemStack stack) {
        final IMultiStateItemStack itemStack = new SingleBlockMultiStateItemStack(stack);
        CreativeClipboardUtils.addBrokenBlock(itemStack, Minecraft.getInstance().level.registryAccess());
    }

    public static void handleExportPatternCommandMessage(final BlockPos target, final String name) {
        final BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(target);
        if (!(blockEntity instanceof IMultiStateBlockEntity multiStateBlockEntity))
        {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("Failed to export pattern: " + name + " - Not a multistate block."));
            return;
        }

        final IMultiStateItemStack multiStateItemStack = multiStateBlockEntity.createSnapshot().toItemStack();
        IPatternSharingManager.getInstance().exportPattern(multiStateItemStack, name);
    }

    public static void handleImportPatternCommandMessage(final String name) {;
        final Either<IMultiStateItemStack, PatternIOException> importResult = IPatternSharingManager.getInstance().importPattern(name);
        importResult.ifLeft(stack -> {
            BitsNBlocks.getInstance().getNetworkChannel()
              .sendToServer(new GivePlayerPatternCommandPacket(stack.createSnapshot()));
        });
        importResult.ifRight(e -> {
            Minecraft.getInstance().player.sendSystemMessage(e.getErrorMessage());
        });
    }
}
