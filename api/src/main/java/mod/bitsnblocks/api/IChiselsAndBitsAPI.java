package mod.bitsnblocks.api;

import mod.bitsnblocks.api.block.state.id.IBlockStateIdManager;
import mod.bitsnblocks.api.change.IChangeTrackerManager;
import mod.bitsnblocks.api.chiseling.IChiselingManager;
import mod.bitsnblocks.api.chiseling.ILocalChiselingContextCache;
import mod.bitsnblocks.api.chiseling.conversion.IConversionManager;
import mod.bitsnblocks.api.chiseling.eligibility.IEligibilityManager;
import mod.bitsnblocks.api.chiseling.eligibility.IEligibilityOptions;
import mod.bitsnblocks.api.chiseling.mode.IChiselMode;
import mod.bitsnblocks.api.client.clipboard.ICreativeClipboardManager;
import mod.bitsnblocks.api.client.model.baked.cache.IBakedModelCacheKeyCalculatorRegistry;
import mod.bitsnblocks.api.client.render.preview.chiseling.IChiselContextPreviewRendererRegistry;
import mod.bitsnblocks.api.client.sharing.IPatternSharingManager;
import mod.bitsnblocks.api.client.tool.mode.icon.ISelectedToolModeIconRendererRegistry;
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
import mod.bitsnblocks.api.multistate.StateEntrySize;
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
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Do not implement it can be accessed via its {@link #getInstance()}-method.
 */
public interface IChiselsAndBitsAPI
{
    /**
     * Gives access to the api instance.
     *
     * @return The api.
     */
    static IChiselsAndBitsAPI getInstance()
    {
        return Holder.getInstance();
    }

    /**
     * Gives access to the factory that can produce different accessors.
     *
     * @return The factory used to create new accessors.
     */
    @NotNull
    IAccessorFactory getAccessorFactory();
    
    /**
     * Gives access to the factory that can produce different mutators.
     *
     * @return The factory used to create new mutators.
     */
    @NotNull
    IMutatorFactory getMutatorFactory();

    /**
     * Manager which deals with chiseling eligibility.
     *
     * @return The manager.
     */
    @NotNull
    IEligibilityManager getEligibilityManager();

    /**
     * Manager which deals with converting eligible blocks, blockstates and IItemProviders into their chiseled
     * variants.
     *
     * @return The conversion manager.
     */
    @NotNull
    IConversionManager getConversionManager();

    /**
     * Manager which deals with calculating, and optionally caching, the voxel shapes, which
     * can be constructed from a given area.
     *
     * @return The voxel shape manager.
     */
    @NotNull
    IVoxelShapeManager getVoxelShapeManager();

    /**
     * A factory which can produce a multistate item from a given source.
     *
     * @return The factory.
     */
    @NotNull
    IMultiStateItemFactory getMultiStateItemFactory();

    /**
     * Represents the default mode for the chiseling system.
     *
     * @return The default mode.
     */
    @NotNull
    IChiselMode getDefaultChiselMode();

    /**
     * Gives access to all registries which are used by chisels and bits.
     *
     * @return The manager for registries used by chisels and bits.
     */
    @NotNull
    IRegistryManager getRegistryManager();

    /**
     * Gives access to the manager which controls chiseling operations.
     *
     * @return The current chiseling manager.
     */
    @NotNull
    IChiselingManager getChiselingManager();

    /**
     * The configuration on top of which chisels and bits is running.
     *
     * @return The current configuration.
     */
    IChiselsAndBitsConfiguration getConfiguration();

    /**
     * The manager which deals with calculating the given blockstate ids in the current running session.
     *
     * @return The blockstate id manager.
     */
    @NotNull
    IBlockStateIdManager getBlockStateIdManager();

    /**
     * Gives access to the bits inventory manager, which allows the conversion of normal inventory systems to bit inventories.
     * These special bit inventories respect the core interfaces that make up an object that can hold or is a bit.
     *
     * @return The manager for dealing with bits.
     */
    @NotNull
    IBitInventoryManager getBitInventoryManager();

    /**
     * The bit item manager.
     * Allows for the creation of bit based itemstacks.
     *
     * @return The bit item manager.
     */
    @NotNull
    IBitItemManager getBitItemManager();

    /**
     * The measuring manager.
     * Gives access to measurements created by a given player.
     *
     * @return The measuring manager.
     */
    @NotNull
    IMeasuringManager getMeasuringManager();

    /**
     * Represents the size of the bits in the current instance.
     *
     * @return The size of the state entries in the current instance.
     */
    @NotNull
    default StateEntrySize getStateEntrySize() {
        try {
            return getConfiguration().getServer().getBitSize().get();
        }
        catch (Exception ignored) {
            return StateEntrySize.ONE_HALF;
        }
    }

    /**
     * The profiling manager, allows for the profiling of operations related Chisels and Bits.
     *
     * @return The profiling manager.
     */
    @NotNull
    IProfilingManager getProfilingManager();

    /**
     * This method gives access to the client side local chiseling context cache.
     * Although this method also exists on the server side, it should be considered a cross tick cache for the latest chiseling context in use by the current player,
     * without it becoming the active context for that player.
     *
     * @return The {@link ILocalChiselingContextCache}.
     */
    @NotNull
    ILocalChiselingContextCache getLocalChiselingContextCache();

    /**
     * The change tracker manager.
     * Gives access to each players change tracker.
     *
     * @return The change tracker manager
     */
    @NotNull
    IChangeTrackerManager getChangeTrackerManager();

    /**
     * Gives access to the block neighborhood builder.
     * Allows for building block specific cache keys when the block environment is required.
     *
     * @return The block neighborhood builder.
     */
    @NotNull
    IBlockNeighborhoodBuilder getBlockNeighborhoodBuilder();

    /**
     * The default mode for performing modification operations if no other is supplied.
     * @return The default modification operation.
     */
    @NotNull
    IModificationOperation getDefaultModificationOperation();

    /**
     * Gives access to the plugin manager that is used to process chisels and bits plugins
     * @return The plugin manager
     */
    @NotNull
    IPluginManager getPluginManager();

    /**
     * Gives access to the chisel context preview renderer registry.
     * @return The registry.
     */
    @NotNull
    IChiselContextPreviewRendererRegistry getChiselContextPreviewRendererRegistry();

    /**
     * Gives access to the selected tool mode icon renderer registry.
     * @return The registry.
     */
    @NotNull
    ISelectedToolModeIconRendererRegistry getSelectedToolModeIconRenderer();

    /**
     * Returns the tag used in the eligibility system to force compatibility.
     * @return The forced compatibility tag.
     */
    @NotNull
    TagKey<Block> getForcedTag();

    /**
     * Returns the tag used in the eligibility system to block compatibility.
     * @return The blocked compatibility tag.
     */
    @NotNull
    TagKey<Block> getBlockedTag();

    /**
     * Returns the permission handler which is used to check if a particular area
     * is chiselable or not.
     *
     * @return The permission handler.
     */
    @NotNull
    IPermissionHandler getPermissionHandler();

    /**
     * Returns the clipboard manager for the creative clipboard.
     *
     * @return The clipboard manager.
     */
    @NotNull
    ICreativeClipboardManager getCreativeClipboardManager();

    /**
     * The pattern sharing manager.
     * This manager only works on the client side, and will do nothing on the server side.
     *
     * @return The pattern sharing manager.
     */
    @NotNull
    IPatternSharingManager getPatternSharingManager();

    /**
     * Handles showing notifications to the player.
     *
     * @return The notifications manager.
     */
    @NotNull
    INotificationManager getNotificationManager();

    @NotNull
    IStateVariantManager getStateVariantManager();

    @NotNull
    ICuttingOperation getDefaultCuttingOperation();

    @NotNull
    IGlueingOperation getDefaultGlueingOperation();

    @NotNull
    ISnapshotFactory getSnapshotFactory();

    @NotNull
    IEligibilityOptions getEligibilityOptions();

    @NotNull
    IAdaptingBitInventoryManager getAdaptingBitInventoryManager();

    @NotNull
    IPluginDiscoverer getPluginDiscoverer();

    @NotNull
    IClientStateVariantManager getClientStateVariantManager();

    @NotNull
    ILaunchPropertyManager getLaunchPropertyManager();

    @NotNull
    IBakedModelCacheKeyCalculatorRegistry getBakedModelCacheKeyCalculatorRegistry();

    class Holder {
        private static IChiselsAndBitsAPI apiInstance;

        public static IChiselsAndBitsAPI getInstance()
        {
            return apiInstance;
        }

        public static void setInstance(final IChiselsAndBitsAPI instance)
        {
            if (apiInstance != null)
                throw new IllegalStateException("Can not setup API twice!");

            apiInstance = instance;
        }
    }
}
