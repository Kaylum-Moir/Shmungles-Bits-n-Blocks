package mod.bitsnblocks.client.model.baked.chiseled;

import mod.bitsnblocks.api.blockinformation.BlockInformation;
import mod.bitsnblocks.api.config.IClientConfiguration;
import mod.bitsnblocks.api.item.multistate.IMultiStateItemStack;
import mod.bitsnblocks.api.multistate.accessor.IAreaAccessor;
import mod.bitsnblocks.api.multistate.accessor.identifier.IAreaShapeIdentifier;
import mod.bitsnblocks.api.neighborhood.IBlockNeighborhood;
import mod.bitsnblocks.api.neighborhood.IBlockNeighborhoodBuilder;
import mod.bitsnblocks.api.profiling.IProfilerSection;
import mod.bitsnblocks.profiling.ProfilingManager;
import mod.bitsnblocks.utils.SimpleMaxSizedCache;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class ChiseledBlockBakedModelManager {
    private static final ChiseledBlockBakedModelManager INSTANCE = new ChiseledBlockBakedModelManager();

    private final SimpleMaxSizedCache<Key, ChiseledBlockBakedModel> cache = new SimpleMaxSizedCache<>(
            () -> IClientConfiguration.getInstance().getModelCacheSize().get() * RenderType.chunkBufferLayers().size()
    );

    private ChiseledBlockBakedModelManager() {
    }

    public static ChiseledBlockBakedModelManager getInstance() {
        return INSTANCE;
    }

    public void clearCache() {
        cache.clear();
    }

    public ChiseledBlockBakedModel get(
            @NotNull final IMultiStateItemStack multiStateItemStack,
            @NotNull final ChiselRenderType chiselRenderType,
            @NotNull final RenderType renderType
    ) {
        try (IProfilerSection ignored = ProfilingManager.getInstance().withSection("Item based chiseled block model")) {
            return get(
                multiStateItemStack,
                multiStateItemStack.getStatistics().getPrimaryState(),
                chiselRenderType,
                null,
                null,
                BlockPos.ZERO,
                renderType
            );
        }
    }

    public ChiseledBlockBakedModel get(
            @NotNull final IAreaAccessor accessor,
            @NotNull final BlockInformation primaryState,
            @NotNull final ChiselRenderType chiselRenderType,
            @NotNull final RenderType renderType
    ) {
        return this.get(accessor, primaryState, chiselRenderType, null, null, BlockPos.ZERO, renderType);
    }

    public ChiseledBlockBakedModel get(
            final IAreaAccessor accessor,
            final BlockInformation primaryState,
            final ChiselRenderType chiselRenderType,
            @Nullable final Function<Direction, BlockInformation> neighborhoodBlockInformationProvider,
            @Nullable final Function<Direction, IAreaAccessor> neighborhoodAreaAccessorProvider,
            @NotNull final BlockPos position,
            @NotNull final RenderType renderType
    ) {
        try (IProfilerSection ignored1 = ProfilingManager.getInstance().withSection("Block based chiseled block model")) {
            return get(accessor, primaryState, chiselRenderType, IBlockNeighborhoodBuilder.getInstance().build(
                    neighborhoodBlockInformationProvider,
                    neighborhoodAreaAccessorProvider
            ), position, renderType);
        }
    }

    public ChiseledBlockBakedModel get(
            final IAreaAccessor accessor,
            final BlockInformation primaryState,
            final ChiselRenderType chiselRenderType,
            @NotNull IBlockNeighborhood blockNeighborhood,
            @NotNull BlockPos position,
            @NotNull final RenderType renderType
    ) {
        try (IProfilerSection ignored1 = ProfilingManager.getInstance().withSection("Block based chiseled block model")) {
            final long primaryStateRenderSeed = primaryState.blockState().getSeed(position);
            final Key key = new Key(
                    accessor.createNewShapeIdentifier(),
                    primaryState,
                    chiselRenderType,
                    blockNeighborhood,
                    primaryStateRenderSeed,
                    renderType);
            return cache.get(key,
                    () -> {
                        try (IProfilerSection ignored3 = ProfilingManager.getInstance().withSection("Cache mis")) {
                            return new ChiseledBlockBakedModel(
                                    primaryState,
                                    chiselRenderType,
                                    accessor,
                                    blockNeighborhood,
                                    primaryStateRenderSeed
                            );
                        }
                    });
        }
    }

    private record Key(IAreaShapeIdentifier identifier, BlockInformation primaryState,
                       ChiselRenderType chiselRenderType, IBlockNeighborhood neighborhood, long renderSeed,
                       RenderType renderType) {
        }
}
