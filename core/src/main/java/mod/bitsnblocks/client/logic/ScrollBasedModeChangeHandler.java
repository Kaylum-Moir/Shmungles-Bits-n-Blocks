package mod.bitsnblocks.client.logic;

import com.google.common.collect.Lists;
import mod.bitsnblocks.BitsNBlocks;
import mod.bitsnblocks.api.config.IClientConfiguration;
import mod.bitsnblocks.api.item.withmode.IWithModeItem;
import mod.bitsnblocks.network.packets.HeldToolModeChangedPacket;
import mod.bitsnblocks.utils.ItemStackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ScrollBasedModeChangeHandler
{

    public static boolean onScroll(final double scrollDelta) {
        if (Minecraft.getInstance().player == null || !Minecraft.getInstance().player.isShiftKeyDown())
            return false;

        final ItemStack stack = ItemStackUtils.getModeItemStackFromPlayer(Minecraft.getInstance().player);
        if (stack.isEmpty())
            return false;

        if (!(stack.getItem() instanceof final IWithModeItem<?> toolModeItem))
            return false;

        if (!IClientConfiguration.getInstance().getShouldScrollInteractionsChangeMode().get())
            return false;

        final List<?> modes = Lists.newArrayList(toolModeItem.getPossibleModes());
        int workingIndex = modes.indexOf(toolModeItem.getMode(stack));

        if (scrollDelta < 0) {
            workingIndex++;
        }
        else
        {
            workingIndex--;
        }

        if (workingIndex < 0)
            workingIndex = modes.size() + workingIndex;
        else if (workingIndex >= modes.size())
            workingIndex -= modes.size();

        BitsNBlocks.getInstance().getNetworkChannel().sendToServer(new HeldToolModeChangedPacket(workingIndex));
        return true;
    }
}
