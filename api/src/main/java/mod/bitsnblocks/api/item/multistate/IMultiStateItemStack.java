package mod.bitsnblocks.api.item.multistate;

import mod.bitsnblocks.api.multistate.accessor.IAreaAccessor;
import mod.bitsnblocks.api.multistate.mutator.IMirrorAndRotateble;
import net.minecraft.world.item.ItemStack;

/**
 * The itemstack sensitive version of the multistate item.
 */
public interface IMultiStateItemStack extends IAreaAccessor, IMirrorAndRotateble {
    /**
     * The statistics of the itemstack.
     *
     * @return The statistics.
     */
    IStatistics getStatistics();

    /**
     * Converts this multistate itemstack data to an actual use able itemstack.
     *
     * @return The itemstack with the data of this multistate itemstack.
     */
    ItemStack toBlockStack();

    /**
     * Converts this multistate itemstack data into a pattern that can be reused.
     * By default converts this into a single use pattern.
     *
     * @return The single use patter from this multi state itemstack.
     */
    ItemStack toPatternStack();

    /**
     * Writes the state data to the itemstack.
     */
    void writeDataTo(ItemStack stack);
}
