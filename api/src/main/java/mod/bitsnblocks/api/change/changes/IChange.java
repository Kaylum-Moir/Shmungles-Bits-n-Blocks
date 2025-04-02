package mod.bitsnblocks.api.change.changes;

import com.mojang.serialization.Codec;
import mod.bitsnblocks.api.registries.IRegistryManager;
import mod.bitsnblocks.api.serialization.CBStreamCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Represents a single change that has been created with bits.
 */
public interface IChange extends IChangeHandler {

    Codec<IChange> CODEC = Codec.lazyInitialized(() -> IRegistryManager.getInstance().getChangeTypeRegistry().byNameCodec()
            .dispatch(IChange::getType, IChangeType::codec));

    StreamCodec<RegistryFriendlyByteBuf, IChange> STREAM_CODEC = CBStreamCodecs.lazyInitialized(() -> CBStreamCodecs.dispatch(
            IRegistryManager.getInstance().getChangeTypeRegistry().byNameStreamCodec(),
            IChangeType::streamCodec,
            IChange::getType
    ));

    /**
     * {@return The type of the change.}
     */
    IChangeType getType();
}
