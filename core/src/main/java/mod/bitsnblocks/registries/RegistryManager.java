package mod.bitsnblocks.registries;

import com.communi.suggestu.scena.core.registries.ICustomRegistry;
import mod.bitsnblocks.api.change.changes.IChangeType;
import mod.bitsnblocks.api.chiseling.mode.IChiselMode;
import mod.bitsnblocks.api.cutting.operation.ICuttingOperation;
import mod.bitsnblocks.api.glueing.operation.IGlueingOperation;
import mod.bitsnblocks.api.modification.operation.IModificationOperation;
import mod.bitsnblocks.api.multistate.snapshot.IMultiStateSnapshotType;
import mod.bitsnblocks.api.pattern.placement.IPatternPlacementType;
import mod.bitsnblocks.api.registries.IRegistryManager;
import mod.bitsnblocks.registrars.*;
import org.jetbrains.annotations.NotNull;

public class RegistryManager implements IRegistryManager
{
    private static final RegistryManager INSTANCE = new RegistryManager();

    private RegistryManager()
    {
    }

    public static RegistryManager getInstance()
    {
        return INSTANCE;
    }

    /**
     * The registry which controls all available chiseling modes.
     *
     * @return The registry.
     */
    @Override
    public ICustomRegistry<IChiselMode> getChiselModeRegistry()
    {
        return ModChiselModes.REGISTRY.get();
    }

    @Override
    public @NotNull ICustomRegistry<IModificationOperation> getModificationOperationRegistry()
    {
        return ModModificationOperation.REGISTRY_SUPPLIER.get();
    }

    @Override
    public @NotNull ICustomRegistry<ICuttingOperation> getCuttingOperationRegistry()
    {
        return ModCuttingOperation.REGISTRY_SUPPLIER.get();
    }

    @Override
    public @NotNull ICustomRegistry<IGlueingOperation> getGlueingOperationRegistry()
    {
        return ModGlueingOperation.REGISTRY_SUPPLIER.get();
    }

    @Override
    public @NotNull ICustomRegistry<IChangeType> getChangeTypeRegistry() {
        return ModChangeTypes.REGISTRY_SUPPLIER.get();
    }

    @Override
    public @NotNull ICustomRegistry<IMultiStateSnapshotType> getMultiStateSnapshotTypeRegistry() {
        return ModMultiStateSnapshotTypes.REGISTRY_SUPPLIER.get();
    }

    @Override
    public @NotNull ICustomRegistry<IPatternPlacementType> getPatternPlacementTypeRegistry() {
        return ModPatternPlacementTypes.REGISTRY_SUPPLIER.get();
    }
}
