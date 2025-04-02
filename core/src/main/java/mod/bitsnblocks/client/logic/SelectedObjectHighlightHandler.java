package mod.bitsnblocks.client.logic;

import mod.bitsnblocks.api.item.withhighlight.IWithHighlightItem;
import mod.bitsnblocks.utils.ItemStackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SelectedObjectHighlightHandler
{

    public static boolean onDrawHighlight()
    {
        final Player playerEntity = Minecraft.getInstance().player;
        if (playerEntity == null)
            return false;

        final ItemStack heldStack = ItemStackUtils.getHighlightItemStackFromPlayer(playerEntity);
        if (heldStack.isEmpty())
            return false;

        final Item stackItem = heldStack.getItem();
        if (!(stackItem instanceof IWithHighlightItem))
            return false;

        return !((IWithHighlightItem) stackItem).shouldDrawDefaultHighlight(playerEntity);
    }
}
