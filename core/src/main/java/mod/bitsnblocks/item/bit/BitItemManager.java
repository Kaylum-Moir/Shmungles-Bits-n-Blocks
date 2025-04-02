package mod.bitsnblocks.item.bit;

import mod.bitsnblocks.api.blockinformation.BlockInformation;
import mod.bitsnblocks.api.chiseling.eligibility.IEligibilityManager;
import mod.bitsnblocks.api.item.bit.IBitItemManager;
import mod.bitsnblocks.registrars.ModDataComponentTypes;
import mod.bitsnblocks.registrars.ModItems;
import net.minecraft.world.item.ItemStack;

public class BitItemManager implements IBitItemManager
{
    private static final BitItemManager INSTANCE = new BitItemManager();

    private BitItemManager()
    {
    }

    public static BitItemManager getInstance()
    {
        return INSTANCE;
    }

    @Override
    public ItemStack create(final BlockInformation blockInformation, final int count)
    {
        if (blockInformation == null || blockInformation.isAir())
        {
            return ItemStack.EMPTY;
        }

        if (!IEligibilityManager.getInstance().canBeChiseled(blockInformation))
        {
            return ItemStack.EMPTY;
        }

        if (count <= 0 && count > ModItems.ITEM_BLOCK_BIT.get().getDefaultMaxStackSize())
        {
            return ItemStack.EMPTY;
        }

        final ItemStack resultStack = new ItemStack(ModItems.ITEM_BLOCK_BIT.get());
        resultStack.set(ModDataComponentTypes.BLOCK_INFORMATION.get(), blockInformation);
        resultStack.setCount(count);

        return resultStack;
    }
}
