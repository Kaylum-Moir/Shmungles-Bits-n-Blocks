package mod.bitsnblocks.client.model.baked.chiseled;

import mod.bitsnblocks.api.blockinformation.BlockInformation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public enum VoxelType
{
    SOLID(s -> s.getFluidState().isEmpty()),
    FLUID(s -> !s.getFluidState().isEmpty()),
    UNKNOWN(s -> true);

    private final Predicate<BlockState> isValidBlockStateCallback;

    VoxelType(final Predicate<BlockState> isValidBlockStateCallback) {this.isValidBlockStateCallback = isValidBlockStateCallback;}

    public boolean isValidBlockState(final BlockInformation blockState) {
        return this.isValidBlockStateCallback.test(blockState.blockState());
    }

    public boolean isFluid() {
        return this == FLUID;
    }
}
