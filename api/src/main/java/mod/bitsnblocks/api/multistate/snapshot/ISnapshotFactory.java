package mod.bitsnblocks.api.multistate.snapshot;

import mod.bitsnblocks.api.IChiselsAndBitsAPI;
import mod.bitsnblocks.api.blockinformation.BlockInformation;

/**
 * A factory to create simple snapshots.
 */
public interface ISnapshotFactory
{

    static ISnapshotFactory getInstance() {
        return IChiselsAndBitsAPI.getInstance().getSnapshotFactory();
    }

    /**
     * Creates a new simple single block snapshot.
     *
     * @return The new snapshot.
     */
    IMultiStateSnapshot singleBlock();


    /**
     * Creates a new simple single block snapshot.
     *
     * @param blockInformation The block information which will fill up the entire snapshot once returned.
     * @return The new snapshot.
     */
    IMultiStateSnapshot singleBlock(BlockInformation blockInformation);
}
