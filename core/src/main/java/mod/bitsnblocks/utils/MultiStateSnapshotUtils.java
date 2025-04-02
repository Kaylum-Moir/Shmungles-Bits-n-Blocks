package mod.bitsnblocks.utils;

import mod.bitsnblocks.api.block.storage.StateEntryStorage;
import mod.bitsnblocks.api.multistate.snapshot.IMultiStateSnapshot;
import mod.bitsnblocks.multistate.snapshot.SimpleSnapshot;

public class MultiStateSnapshotUtils
{

    private MultiStateSnapshotUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: MultiStateSnapshotUtils. This is a utility class");
    }

    public static IMultiStateSnapshot createFromStorage(final StateEntryStorage storage) {
        return new SimpleSnapshot(storage.createSnapshot());
    }
}
