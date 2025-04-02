package mod.bitsnblocks.api.multistate.snapshot;

import com.mojang.serialization.Codec;
import mod.bitsnblocks.api.item.multistate.IMultiStateItemStack;
import mod.bitsnblocks.api.multistate.mutator.IGenerallyModifiableAreaMutator;
import mod.bitsnblocks.api.multistate.statistics.IMultiStateObjectStatistics;
import mod.bitsnblocks.api.registries.IRegistryManager;
import mod.bitsnblocks.api.serialization.CBStreamCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface IMultiStateSnapshot extends Cloneable, IGenerallyModifiableAreaMutator
{
    Codec<IMultiStateSnapshot> CODEC = Codec.lazyInitialized(() -> IRegistryManager.getInstance().getMultiStateSnapshotTypeRegistry()
            .byNameCodec().dispatch(IMultiStateSnapshot::getType, IMultiStateSnapshotType::codec));

    StreamCodec<RegistryFriendlyByteBuf, IMultiStateSnapshot> STREAM_CODEC = CBStreamCodecs.lazyInitialized(() -> CBStreamCodecs.dispatch(
            IRegistryManager.getInstance().getMultiStateSnapshotTypeRegistry().byNameStreamCodec(),
            IMultiStateSnapshotType::streamCodec,
            IMultiStateSnapshot::getType
    ));

    /**
     * Gets the type of the snapshot.
     *
     * @return The type of the snapshot.
     */
    IMultiStateSnapshotType getType();

    /**
     * Converts the current snapshot to a variant which is itemstack capable.
     *
     * @return The multistate itemstack which is the itemstack nbt representation of the current snapshot.
     */
    IMultiStateItemStack toItemStack();

    /**
     * Returns the statistics of the current snapshot.
     *
     * @return The statistics
     */
    IMultiStateObjectStatistics getStatics();

    /**
     * Creates a clone of the snapshot.
     *
     * @return The clone.
     */
    IMultiStateSnapshot clone();
}
