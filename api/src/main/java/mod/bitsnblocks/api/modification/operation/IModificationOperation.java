package mod.bitsnblocks.api.modification.operation;

import com.communi.suggestu.scena.core.registries.ICustomRegistry;
import com.communi.suggestu.scena.core.registries.ICustomRegistryEntry;
import mod.bitsnblocks.api.IChiselsAndBitsAPI;
import mod.bitsnblocks.api.item.withmode.IToolMode;
import mod.bitsnblocks.api.multistate.mutator.IGenerallyModifiableAreaMutator;
import mod.bitsnblocks.api.registries.IRegistryManager;

/**
 * A modification operation that can be performed in the modification table.
 */
public interface IModificationOperation extends ICustomRegistryEntry, IToolMode<IModificationOperationGroup>
{

    /**
     * The default modification operation.
     * @return The default operation.
     */
    static IModificationOperation getDefaultMode() {
        return IChiselsAndBitsAPI.getInstance().getDefaultModificationOperation();
    }

    /**
     * The underlying registry that contains the different modification modes that can be performed.
     * @return The underlying forge registry.
     */
    static ICustomRegistry<IModificationOperation> getRegistry() {
        return IRegistryManager.getInstance().getModificationOperationRegistry();
    }

    /**
     * Performs a modification on the snapshot.
     *
     * @param source The mutator to modify.
     */
    void apply(final IGenerallyModifiableAreaMutator source);
}
