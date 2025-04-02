package mod.bitsnblocks.api.glueing.operation;

import com.communi.suggestu.scena.core.registries.ICustomRegistry;
import com.communi.suggestu.scena.core.registries.ICustomRegistryEntry;
import mod.bitsnblocks.api.IChiselsAndBitsAPI;
import mod.bitsnblocks.api.item.withmode.IToolMode;
import mod.bitsnblocks.api.multistate.accessor.IAreaAccessor;
import mod.bitsnblocks.api.registries.IRegistryManager;

import java.util.Collection;

/**
 * A Glueing operation that can be performed in the Glueing table.
 */
public interface IGlueingOperation extends ICustomRegistryEntry, IToolMode<IGlueingOperationGroup>
{
    /**
     * The default Glueing operation.
     * @return The default operation.
     */
    static IGlueingOperation getDefaultMode() {
        return IChiselsAndBitsAPI.getInstance().getDefaultGlueingOperation();
    }

    /**
     * The underlying registry that contains the different Glueing modes that can be performed.
     * @return The underlying forge registry.
     */
    static ICustomRegistry<IGlueingOperation> getRegistry() {
        return IRegistryManager.getInstance().getGlueingOperationRegistry();
    }

    /**
     * Performs a glueing on the given snapshots.
     * The given collection contains the individual pieces that are needed to be glued together.
     * <p>
     * In total a maximum 64 pieces can be given.
     *
     * @param sources The accessors to glue.
     * @return An accessor which was created by the operation.
     */
    IAreaAccessor apply(final Collection<IAreaAccessor> sources);
}
