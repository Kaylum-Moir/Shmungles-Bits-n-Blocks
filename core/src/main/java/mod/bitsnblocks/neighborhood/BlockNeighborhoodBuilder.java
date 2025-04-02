package mod.bitsnblocks.neighborhood;

import mod.bitsnblocks.api.blockinformation.BlockInformation;
import mod.bitsnblocks.api.multistate.accessor.IAreaAccessor;
import mod.bitsnblocks.api.neighborhood.IBlockNeighborhood;
import mod.bitsnblocks.api.neighborhood.IBlockNeighborhoodBuilder;
import mod.bitsnblocks.api.profiling.IProfilerSection;
import mod.bitsnblocks.profiling.ProfilingManager;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.function.Function;

public final class BlockNeighborhoodBuilder implements IBlockNeighborhoodBuilder {
    private static final BlockNeighborhoodBuilder INSTANCE = new BlockNeighborhoodBuilder();

    private BlockNeighborhoodBuilder() {
    }

    public static BlockNeighborhoodBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    public @NotNull IBlockNeighborhood build(
            @Nullable Function<Direction, BlockInformation> neighborhoodBlockStateProvider,
            @Nullable Function<Direction, IAreaAccessor> neighborhoodAreaAccessorProvider
    ) {
        final EnumMap<Direction, BlockNeighborhoodEntry> neighborhoodMap = new EnumMap<>(Direction.class);

        try (IProfilerSection ignored2 = ProfilingManager.getInstance().withSection("Key building")) {
            for (final Direction value : Direction.values()) {
                final BlockInformation state = neighborhoodBlockStateProvider != null ? neighborhoodBlockStateProvider.apply(value) : BlockInformation.AIR;
                final IAreaAccessor accessor = neighborhoodAreaAccessorProvider != null ? neighborhoodAreaAccessorProvider.apply(value) : null;
                if (accessor == null) {
                    neighborhoodMap.put(value, new BlockNeighborhoodEntry(state));
                } else {
                    neighborhoodMap.put(value, new BlockNeighborhoodEntry(
                                    state,
                                    accessor
                            )
                    );
                }
            }

            return new BlockNeighborhood(neighborhoodMap);
        }
    }
}
