package mod.bitsnblocks.logic;

import mod.bitsnblocks.api.chiseling.eligibility.IEligibilityAnalysisResult;
import mod.bitsnblocks.api.chiseling.eligibility.IEligibilityManager;
import mod.bitsnblocks.api.config.ICommonConfiguration;
import mod.bitsnblocks.item.MagnifyingGlassItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MagnifyingGlassTooltipHandler
{

    public static void onItemTooltip(final ItemStack itemStack, final List<Component> toolTips)
    {
        if (Minecraft.getInstance().player != null && ICommonConfiguration.getInstance().getEnableHelp().get())
            if (Minecraft.getInstance().player.getMainHandItem().getItem() instanceof MagnifyingGlassItem
                  || Minecraft.getInstance().player.getOffhandItem().getItem() instanceof MagnifyingGlassItem)
                if (itemStack.getItem() instanceof BlockItem) {
                    final IEligibilityAnalysisResult result = IEligibilityManager.getInstance().analyse(itemStack);

                    toolTips.add(
                        result.canBeChiseled() || result.isAlreadyChiseled() ?
                          result.getReason().withStyle(ChatFormatting.GREEN) :
                          result.getReason().withStyle(ChatFormatting.RED)
                    );
                }
    }
}
