package mod.bitsnblocks.multistate.snapshot;

import mod.bitsnblocks.api.block.storage.StateEntryStorage;
import mod.bitsnblocks.api.blockinformation.BlockInformation;
import mod.bitsnblocks.api.multistate.snapshot.IMultiStateSnapshot;
import mod.bitsnblocks.api.multistate.snapshot.ISnapshotFactory;

public final class SnapshotFactory implements ISnapshotFactory
{
    private static final SnapshotFactory INSTANCE = new SnapshotFactory();

    public static SnapshotFactory getInstance()
    {
        return INSTANCE;
    }

    private SnapshotFactory()
    {
    }

    @Override
    public IMultiStateSnapshot singleBlock()
    {
        return new SimpleSnapshot(
                new StateEntryStorage()
        );
    }

    @Override
    public IMultiStateSnapshot singleBlock(final BlockInformation blockInformation)
    {
        return new SimpleSnapshot(blockInformation);
    }
}
