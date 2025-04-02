package mod.bitsnblocks.api.item.bit;

import mod.bitsnblocks.api.blockinformation.BlockInformation;
import mod.bitsnblocks.api.chiseling.mode.IChiselMode;
import mod.bitsnblocks.api.item.change.IChangeTrackingItem;
import mod.bitsnblocks.api.item.click.IRightClickControllingItem;
import mod.bitsnblocks.api.item.withhighlight.IWithHighlightItem;
import mod.bitsnblocks.api.item.withmode.IWithModeItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an item which is a single bit.
 */
public interface IBitItem extends IRightClickControllingItem, IWithHighlightItem, IWithModeItem<IChiselMode>, IChangeTrackingItem
{

    /**
     * Returns the block information which is contained in a stack with the
     * given bit item.
     *
     * @param stack The stack which contains this bit item.
     *
     * @return The block information contained in this bit item.
     */
    @NotNull
    BlockInformation getBlockInformation(final ItemStack stack);

    /**
     * Invoked when a merge operation of a bit inside a bitbag is beginning during a shift-click interaction
     * in the bit bag UI.
     */
    void onMergeOperationWithBagBeginning();

    /**
     * Invoked when a merge operation of a bit inside a bitbag is ending during a shift-click interaction
     * in the bit bag UI.
     */
    void onMergeOperationWithBagEnding();
}
