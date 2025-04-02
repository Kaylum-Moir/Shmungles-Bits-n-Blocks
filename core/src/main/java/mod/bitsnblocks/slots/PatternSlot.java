package mod.bitsnblocks.slots;

import mod.bitsnblocks.api.item.pattern.IPatternItem;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PatternSlot extends Slot
{
    public PatternSlot(
      final Container inventoryIn,
      final int index,
      final int xPosition,
      final int yPosition)
    {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(final ItemStack itemStack)
    {
        return itemStack.getItem() instanceof IPatternItem;
    }
}
