package mod.bitsnblocks.item;

import mod.bitsnblocks.api.exceptions.SealingNotSupportedException;
import mod.bitsnblocks.api.item.multistate.IMultiStateItemStack;
import mod.bitsnblocks.api.item.pattern.IMultiUsePatternItem;
import mod.bitsnblocks.api.pattern.placement.IPatternPlacementType;
import mod.bitsnblocks.api.sealing.ISupportsUnsealing;
import mod.bitsnblocks.api.util.HelpTextUtils;
import mod.bitsnblocks.api.util.LocalStrings;
import mod.bitsnblocks.registrars.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MultiUsePatternItem extends SingleUsePatternItem implements IMultiUsePatternItem
{
    public MultiUsePatternItem(final Properties builder)
    {
        super(builder);
    }

    @Override
    protected InteractionResult determineSuccessResult(final BlockPlaceContext context, final ItemStack resultingStack)
    {
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull ItemStack seal(final @NotNull ItemStack source) throws SealingNotSupportedException
    {
        throw new SealingNotSupportedException();
    }

    @Override
    public void appendHoverText(
      final @NotNull ItemStack stack, final @Nullable TooltipContext context, final @NotNull List<Component> tooltip, final @NotNull TooltipFlag flagIn)
    {
        final IPatternPlacementType mode = getMode(stack);
        if (mode.getGroup().isPresent())
        {
            tooltip.add(LocalStrings.PatternItemTooltipModeGrouped.getText(mode.getGroup().get().getDisplayName(), mode.getDisplayName()));
        }
        else
        {
            tooltip.add(LocalStrings.PatternItemTooltipModeSimple.getText(mode.getDisplayName()));
        }

        if ((Minecraft.getInstance().getWindow() != null && Screen.hasShiftDown())) {
            tooltip.add(Component.literal("        "));
            tooltip.add(Component.literal("        "));

            HelpTextUtils.build(
              LocalStrings.HelpSealedPattern, tooltip
            );
        }
    }

    @Override
    public @NotNull ItemStack unseal(@NotNull final ItemStack source) throws SealingNotSupportedException
    {
        if (source.getItem() instanceof ISupportsUnsealing)
        {
            final ItemStack seal = new ItemStack(ModItems.SINGLE_USE_PATTERN_ITEM.get());
            final IMultiStateItemStack stack = createItemStack(source);
            stack.writeDataTo(seal);
            return seal;
        }

        return source;
    }
}
