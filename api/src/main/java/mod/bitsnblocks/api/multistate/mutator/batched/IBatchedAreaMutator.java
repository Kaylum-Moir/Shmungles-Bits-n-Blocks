package mod.bitsnblocks.api.multistate.mutator.batched;

import mod.bitsnblocks.api.change.IChangeTracker;
import mod.bitsnblocks.api.multistate.mutator.IAreaMutator;
import mod.bitsnblocks.api.util.IBatchMutation;
import mod.bitsnblocks.api.util.IWithBatchableMutationSupport;

/**
 * A mutator which supports making mutations in batches.
 */
public interface IBatchedAreaMutator extends IAreaMutator, IWithBatchableMutationSupport {

    /**
     * Triggers a batch mutation start for block placement.
     * Enables tracking of the changes.
     *
     * @param changeTracker The change tracker to apply the changes to.
     * @return The batch mutation, which will record the changes automatically.
     */
    IBatchMutation batch(final IChangeTracker changeTracker);
}
