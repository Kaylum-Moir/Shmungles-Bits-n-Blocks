package mod.bitsnblocks.api.item.named;

import net.minecraft.world.item.ItemStack;


public interface IDynamicallyHighlightedNameItem extends IPermanentlyHighlightedNameItem
{
    ItemStack adaptItemStack(final ItemStack currentToolStack);
}
