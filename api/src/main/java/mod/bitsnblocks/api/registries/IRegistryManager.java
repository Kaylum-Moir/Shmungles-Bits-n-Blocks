package mod.bitsnblocks.api.registries;

import com.communi.suggestu.scena.core.registries.ICustomRegistry;
import mod.bitsnblocks.api.IChiselsAndBitsAPI;
import mod.bitsnblocks.api.change.changes.IChangeType;
import mod.bitsnblocks.api.chiseling.mode.IChiselMode;
import mod.bitsnblocks.api.cutting.operation.ICuttingOperation;
import mod.bitsnblocks.api.glueing.operation.IGlueingOperation;
import mod.bitsnblocks.api.modification.operation.IModificationOperation;
import mod.bitsnblocks.api.multistate.snapshot.IMultiStateSnapshotType;
import mod.bitsnblocks.api.pattern.placement.IPatternPlacementType;
import org.jetbrains.annotations.NotNull;

/**
 * Manages all registries which are used by Chisels and Bits.
 */
public interface IRegistryManager
{

    static IRegistryManager getInstance() {
        return IChiselsAndBitsAPI.getInstance().getRegistryManager();
    }

    /**
     * The registry which controls all available chiseling modes.
     *
     * @return The registry.
     */
    ICustomRegistry<IChiselMode> getChiselModeRegistry();

    /**
     * The registry used for modifications of single use patterns.
     *
     * @return The modification operation registry.
     */
    @NotNull
    ICustomRegistry<IModificationOperation> getModificationOperationRegistry();

    /**
     * The registry used for cutting of single use patterns.
     *
     * @return The cutting operation registry.
     */
    @NotNull
    ICustomRegistry<ICuttingOperation> getCuttingOperationRegistry();

    /**
     * The registry used for glueing of single use patterns.
     *
     * @return The glueing operation registry.
     */
    @NotNull
    ICustomRegistry<IGlueingOperation> getGlueingOperationRegistry();

    /**
     * The registry used for change types.
     *
     * @return The change type registry.
     */
    @NotNull
    ICustomRegistry<IChangeType> getChangeTypeRegistry();

    /**
     * The registry used for multi state snapshot types.
     *
     * @return The multi state snapshot type registry.
     */
    @NotNull
    ICustomRegistry<IMultiStateSnapshotType> getMultiStateSnapshotTypeRegistry();

    /**
     * The registry used for pattern placement types.
     *
     * @return The pattern placement type registry.
     */
    @NotNull
    ICustomRegistry<IPatternPlacementType> getPatternPlacementTypeRegistry();
}
