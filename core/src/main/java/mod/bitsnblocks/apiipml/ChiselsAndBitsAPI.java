package mod.bitsnblocks.apiipml;

import com.communi.suggestu.scena.core.dist.DistExecutor;
import mod.bitsnblocks.BitsNBlocks;
import mod.bitsnblocks.api.IChiselsAndBitsAPI;
import mod.bitsnblocks.api.block.state.id.IBlockStateIdManager;
import mod.bitsnblocks.api.change.IChangeTrackerManager;
import mod.bitsnblocks.api.chiseling.IChiselingManager;
import mod.bitsnblocks.api.chiseling.ILocalChiselingContextCache;
import mod.bitsnblocks.api.chiseling.conversion.IConversionManager;
import mod.bitsnblocks.api.chiseling.eligibility.IEligibilityManager;
import mod.bitsnblocks.api.chiseling.eligibility.IEligibilityOptions;
import mod.bitsnblocks.api.chiseling.mode.IChiselMode;
import mod.bitsnblocks.api.client.model.baked.cache.IBakedModelCacheKeyCalculatorRegistry;
import mod.bitsnblocks.api.client.render.preview.chiseling.IChiselContextPreviewRendererRegistry;
import mod.bitsnblocks.api.client.sharing.IPatternSharingManager;
import mod.bitsnblocks.api.client.tool.mode.icon.ISelectedToolModeIconRendererRegistry;
import mod.bitsnblocks.api.client.clipboard.ICreativeClipboardManager;
import mod.bitsnblocks.api.client.variant.state.IClientStateVariantManager;
import mod.bitsnblocks.api.config.IChiselsAndBitsConfiguration;
import mod.bitsnblocks.api.cutting.operation.ICuttingOperation;
import mod.bitsnblocks.api.glueing.operation.IGlueingOperation;
import mod.bitsnblocks.api.inventory.bit.IAdaptingBitInventoryManager;
import mod.bitsnblocks.api.inventory.management.IBitInventoryManager;
import mod.bitsnblocks.api.item.bit.IBitItemManager;
import mod.bitsnblocks.api.item.multistate.IMultiStateItemFactory;
import mod.bitsnblocks.api.launch.ILaunchPropertyManager;
import mod.bitsnblocks.api.measuring.IMeasuringManager;
import mod.bitsnblocks.api.modification.operation.IModificationOperation;
import mod.bitsnblocks.api.multistate.accessor.IAccessorFactory;
import mod.bitsnblocks.api.multistate.mutator.IMutatorFactory;
import mod.bitsnblocks.api.multistate.snapshot.ISnapshotFactory;
import mod.bitsnblocks.api.neighborhood.IBlockNeighborhoodBuilder;
import mod.bitsnblocks.api.notifications.INotificationManager;
import mod.bitsnblocks.api.permissions.IPermissionHandler;
import mod.bitsnblocks.api.plugin.IPluginDiscoverer;
import mod.bitsnblocks.api.plugin.IPluginManager;
import mod.bitsnblocks.api.profiling.IProfilingManager;
import mod.bitsnblocks.api.registries.IRegistryManager;
import mod.bitsnblocks.api.variant.state.IStateVariantManager;
import mod.bitsnblocks.api.voxelshape.IVoxelShapeManager;
import mod.bitsnblocks.change.ChangeTrackerManger;
import mod.bitsnblocks.chiseling.LocalChiselingContextCache;
import mod.bitsnblocks.chiseling.conversion.ConversionManager;
import mod.bitsnblocks.chiseling.eligibility.EligibilityManager;
import mod.bitsnblocks.client.chiseling.preview.render.ChiselContextPreviewRendererRegistry;
import mod.bitsnblocks.client.sharing.PatternSharingManager;
import mod.bitsnblocks.client.tool.mode.icon.SelectedToolModeRendererRegistry;
import mod.bitsnblocks.client.variant.state.ClientStateVariantManager;
import mod.bitsnblocks.client.clipboard.CreativeClipboardManager;
import mod.bitsnblocks.inventory.management.BitInventoryManager;
import mod.bitsnblocks.item.bit.BitItemManager;
import mod.bitsnblocks.item.multistate.MultiStateItemFactory;
import mod.bitsnblocks.launch.LaunchPropertyManager;
import mod.bitsnblocks.measures.MeasuringManager;
import mod.bitsnblocks.multistate.mutator.MutatorFactory;
import mod.bitsnblocks.multistate.snapshot.SnapshotFactory;
import mod.bitsnblocks.neighborhood.BlockNeighborhoodBuilder;
import mod.bitsnblocks.notifications.NotificationManager;
import mod.bitsnblocks.permissions.PermissionHandler;
import mod.bitsnblocks.plugin.PluginManger;
import mod.bitsnblocks.profiling.ProfilingManager;
import mod.bitsnblocks.registrars.ModChiselModes;
import mod.bitsnblocks.registrars.ModModificationOperation;
import mod.bitsnblocks.registrars.ModTags;
import mod.bitsnblocks.registries.RegistryManager;
import mod.bitsnblocks.stateinfo.additional.StateVariantManager;
import mod.bitsnblocks.voxelshape.VoxelShapeManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class ChiselsAndBitsAPI implements IChiselsAndBitsAPI
{
    private final IEligibilityOptions eligibilityOptions;
    private final IAdaptingBitInventoryManager adaptingBitInventoryManager;
    private final IPluginDiscoverer pluginDiscoverer;

    public ChiselsAndBitsAPI(IEligibilityOptions eligibilityOptions, IAdaptingBitInventoryManager adaptingBitInventoryManager, IPluginDiscoverer pluginDiscoverer)
    {
        this.eligibilityOptions = eligibilityOptions;
        this.adaptingBitInventoryManager = adaptingBitInventoryManager;
        this.pluginDiscoverer = pluginDiscoverer;
    }

    /**
     * Gives access to the factory that can produce different accessors.
     *
     * @return The factory used to create new accessors.
     */
    @Override
    public @NotNull IAccessorFactory getAccessorFactory()
    {
        return MutatorFactory.getInstance();
    }

    /**
     * Gives access to the factory that can produce different mutators.
     *
     * @return The factory used to create new mutators.
     */
    @NotNull
    @Override
    public IMutatorFactory getMutatorFactory()
    {
        return MutatorFactory.getInstance();
    }

    /**
     * Manager which deals with chiseling eligibility.
     *
     * @return The manager.
     */
    @NotNull
    @Override
    public IEligibilityManager getEligibilityManager()
    {
        return EligibilityManager.getInstance();
    }

    /**
     * Manager which deals with converting eligible blocks, blockstates and IItemProviders into their chiseled variants.
     *
     * @return The conversion manager.
     */
    @NotNull
    @Override
    public IConversionManager getConversionManager()
    {
        return ConversionManager.getInstance();
    }

    /**
     * Manager which deals with calculating, and optionally caching, the voxel shapes, which can be constructed from a given area.
     *
     * @return The voxel shape manager.
     */
    @NotNull
    @Override
    public IVoxelShapeManager getVoxelShapeManager()
    {
        return VoxelShapeManager.getInstance();
    }

    /**
     * A factory which can produce a multistate item from a given source.
     *
     * @return The factory.
     */
    @NotNull
    @Override
    public IMultiStateItemFactory getMultiStateItemFactory()
    {
        return MultiStateItemFactory.getInstance();
    }

    /**
     * Represents the default mode for the chiseling system.
     *
     * @return The default mode.
     */
    @NotNull
    @Override
    public IChiselMode getDefaultChiselMode()
    {
        return ModChiselModes.SINGLE_BIT.get();
    }

    /**
     * Gives access to all registries which are used by chisels and bits.
     *
     * @return The manager for registries used by chisels and bits.
     */
    @NotNull
    @Override
    public IRegistryManager getRegistryManager()
    {
        return RegistryManager.getInstance();
    }

    /**
     * Gives access to the manager which controls chiseling operations.
     *
     * @return The current chiseling manager.
     */
    @NotNull
    @Override
    public IChiselingManager getChiselingManager()
    {
        return mod.bitsnblocks.chiseling.ChiselingManager.getInstance();
    }

    /**
     * The configuration on top of which chisels and bits is running.
     *
     * @return The current configuration.
     */
    @Override
    public IChiselsAndBitsConfiguration getConfiguration()
    {
        return BitsNBlocks.getInstance().getConfiguration();
    }

    /**
     * The manager which deals with calculating the given blockstate ids in the current running session.
     *
     * @return The blockstate id manager.
     */
    @NotNull
    @Override
    public IBlockStateIdManager getBlockStateIdManager()
    {
        return new IBlockStateIdManager() {};
    }

    @NotNull
    @Override
    public IBitInventoryManager getBitInventoryManager()
    {
        return BitInventoryManager.getInstance();
    }

    @NotNull
    @Override
    public IBitItemManager getBitItemManager()
    {
        return BitItemManager.getInstance();
    }

    @Override
    public @NotNull IMeasuringManager getMeasuringManager()
    {
        return MeasuringManager.getInstance();
    }

    @Override
    public @NotNull IProfilingManager getProfilingManager()
    {
        return ProfilingManager.getInstance();
    }

    @Override
    public @NotNull ILocalChiselingContextCache getLocalChiselingContextCache()
    {
        return LocalChiselingContextCache.getInstance();
    }

    @Override
    public @NotNull IChangeTrackerManager getChangeTrackerManager()
    {
        return ChangeTrackerManger.getInstance();
    }

    @Override
    public @NotNull IBlockNeighborhoodBuilder getBlockNeighborhoodBuilder()
    {
        return BlockNeighborhoodBuilder.getInstance();
    }

    @Override
    public @NotNull IModificationOperation getDefaultModificationOperation()
    {
        return ModModificationOperation.ROTATE_AROUND_X.get();
    }

    @Override
    public @NotNull IPluginManager getPluginManager()
    {
        return PluginManger.getInstance();
    }

    @Override
    public @NotNull IChiselContextPreviewRendererRegistry getChiselContextPreviewRendererRegistry()
    {
        return ChiselContextPreviewRendererRegistry.getInstance();
    }

    @Override
    public @NotNull ISelectedToolModeIconRendererRegistry getSelectedToolModeIconRenderer()
    {
        return SelectedToolModeRendererRegistry.getInstance();
    }

    @Override
    public @NotNull TagKey<Block> getForcedTag()
    {
        return ModTags.Blocks.FORCED_CHISELABLE;
    }

    @Override
    public @NotNull TagKey<Block> getBlockedTag()
    {
        return ModTags.Blocks.BLOCKED_CHISELABLE;
    }

    @Override
    public @NotNull IPermissionHandler getPermissionHandler()
    {
        return PermissionHandler.getInstance();
    }

    @Override
    public @NotNull ICreativeClipboardManager getCreativeClipboardManager()
    {
        return CreativeClipboardManager.getInstance();
    }

    @Override
    public @NotNull IPatternSharingManager getPatternSharingManager()
    {
        return PatternSharingManager.getInstance();
    }

    @Override
    public @NotNull INotificationManager getNotificationManager()
    {
        return NotificationManager.getInstance();
    }

    @Override
    public @NotNull IStateVariantManager getStateVariantManager()
    {
        return StateVariantManager.getInstance();
    }

    @Override
    public @NotNull ICuttingOperation getDefaultCuttingOperation()
    {
        return null;
    }

    @Override
    public @NotNull IGlueingOperation getDefaultGlueingOperation()
    {
        return null;
    }

    @Override
    public @NotNull ISnapshotFactory getSnapshotFactory()
    {
        return SnapshotFactory.getInstance();
    }

    @Override
    public @NotNull IEligibilityOptions getEligibilityOptions() {
        return eligibilityOptions;
    }

    @Override
    public @NotNull IAdaptingBitInventoryManager getAdaptingBitInventoryManager() {
        return adaptingBitInventoryManager;
    }

    @Override
    public @NotNull IPluginDiscoverer getPluginDiscoverer() {
        return pluginDiscoverer;
    }

    @Override
    public @NotNull IClientStateVariantManager getClientStateVariantManager() {
        return DistExecutor.unsafeRunForDist(
                () -> ClientStateVariantManager::getInstance,
                () -> () -> null
        );
    }

    @Override
    public @NotNull ILaunchPropertyManager getLaunchPropertyManager() {
        return LaunchPropertyManager.getInstance();
    }

    @Override
    public @NotNull IBakedModelCacheKeyCalculatorRegistry getBakedModelCacheKeyCalculatorRegistry() {
        return null;
    }
}
