package mod.bitsnblocks.item;

import mod.bitsnblocks.api.exceptions.SealingNotSupportedException;
import mod.bitsnblocks.api.item.multistate.IMultiStateItemStack;
import mod.bitsnblocks.api.item.pattern.IMultiUsePatternItem;
import mod.bitsnblocks.api.item.pattern.IPatternItem;
import mod.bitsnblocks.api.multistate.accessor.IAreaAccessor;
import mod.bitsnblocks.api.multistate.mutator.IMutatorFactory;
import mod.bitsnblocks.api.multistate.mutator.world.IWorldAreaMutator;
import mod.bitsnblocks.api.multistate.snapshot.IMultiStateSnapshot;
import mod.bitsnblocks.api.notifications.INotificationManager;
import mod.bitsnblocks.api.pattern.placement.IPatternPlacementType;
import mod.bitsnblocks.api.placement.PlacementResult;
import mod.bitsnblocks.api.util.HelpTextUtils;
import mod.bitsnblocks.api.util.LocalStrings;
import mod.bitsnblocks.item.multistate.SingleBlockMultiStateItemStack;
import mod.bitsnblocks.multistate.snapshot.EmptySnapshot;
import mod.bitsnblocks.registrars.ModDataComponentTypes;
import mod.bitsnblocks.registrars.ModItems;
import mod.bitsnblocks.registrars.ModPatternPlacementTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class SingleUsePatternItem extends Item implements IPatternItem
{

    public SingleUsePatternItem(final Properties builder)
    {
        super(builder);
    }

    @Override
    public @NotNull Component getName(final ItemStack item)
    {
        if (item.has(ModDataComponentTypes.HIGHLIGHT.get()))
        {
            final Component highlight = item.get(ModDataComponentTypes.HIGHLIGHT.get());
            return Objects.requireNonNull(highlight).copy().withStyle(ChatFormatting.RED);
        }

        return super.getName(item);
    }

    /**
     * Creates an itemstack aware context wrapper that gives access to the multistate information contained within the given itemstack.
     *
     * @param stack The stack to get an {@link IMultiStateItemStack} for.
     * @return The {@link IMultiStateItemStack} that represents the data in the given itemstack.
     */
    @NotNull
    @Override
    public IMultiStateItemStack createItemStack(final ItemStack stack)
    {
        //Take care of an empty pattern.
        //Generally the case when this is a stack from the creative menu.
        if (!SingleBlockMultiStateItemStack.hasData(stack)) {
            return EmptySnapshot.Stack.INSTANCE;
        }

        return new SingleBlockMultiStateItemStack(stack);
    }

    @Override
    @NotNull
    public InteractionResult useOn(@NotNull UseOnContext context) {
        final IMultiStateItemStack contents = createItemStack(context.getItemInHand());
        if (contents.getStatistics().isEmpty()) {
            if (context.getPlayer() == null)
                return InteractionResult.FAIL;

            if (!context.getPlayer().isCreative())
                return InteractionResult.FAIL;

            if (!context.getPlayer().isShiftKeyDown())
                return InteractionResult.FAIL;

            final IWorldAreaMutator areaMutator = IMutatorFactory.getInstance().in(context.getLevel(), context.getClickedPos());
            areaMutator.createSnapshot().toItemStack().writeDataTo(context.getItemInHand());
            return InteractionResult.SUCCESS;
        }


        return this.tryPlace(new BlockPlaceContext(context));
    }

    @NotNull
    public InteractionResult tryPlace(@NotNull final BlockPlaceContext context)
    {
        if (context.getPlayer() == null)
            return InteractionResult.FAIL;

        final IAreaAccessor source = this.createItemStack(context.getItemInHand());
        final IMultiStateSnapshot sourceSnapshot = source.createSnapshot();
        final IPatternPlacementType mode = getMode(context.getItemInHand());

        final PlacementResult resultType = mode
          .performPlacement(
            sourceSnapshot,
            context,
            false);

        if (!resultType.isSuccess() && context.getLevel().isClientSide()) {
            INotificationManager.getInstance().notify(
              mode.getIcon(),
              new Vec3(1, 0, 0),
              resultType.getFailureMessage()
            );
        }

        return resultType.isSuccess() ?
                 determineSuccessResult(context, context.getItemInHand()) :
                 InteractionResult.FAIL;
    }

    protected InteractionResult determineSuccessResult(final BlockPlaceContext context, final ItemStack stack) {
        if (context.getPlayer() != null && context.getPlayer().isCreative())
        {
            return InteractionResult.SUCCESS;
        }

        stack.shrink(1);
        return InteractionResult.CONSUME;
    }

    @Override
    public @NotNull ItemStack seal(@NotNull final ItemStack source) throws SealingNotSupportedException
    {
        if (source.getItem() == this)
        {
            if (!(source.getItem() instanceof IMultiUsePatternItem))
            {
                final ItemStack seal = new ItemStack(ModItems.MULTI_USE_PATTERN_ITEM.get());
                final IMultiStateItemStack stack = createItemStack(source);
                stack.writeDataTo(seal);
                return seal;
            }

            throw new SealingNotSupportedException();
        }

        return source;
    }

    @Override
    public void appendHoverText(
      final @NotNull ItemStack stack, @Nullable final TooltipContext worldIn, final @NotNull List<Component> tooltip, final @NotNull TooltipFlag flagIn)
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

            HelpTextUtils.build(LocalStrings.HelpSimplePattern, tooltip);
        }
    }

    @Override
    public @NotNull IPatternPlacementType getMode(final ItemStack stack)
    {
        return stack.getOrDefault(ModDataComponentTypes.PATTERN_PLACEMENT_TYPE.get(), ModPatternPlacementTypes.PLACEMENT.get());
    }

    @Override
    public void setMode(final ItemStack stack, final IPatternPlacementType mode)
    {
        stack.set(ModDataComponentTypes.PATTERN_PLACEMENT_TYPE.get(), mode);
    }

    @Override
    public @NotNull Collection<IPatternPlacementType> getPossibleModes()
    {
        return ModPatternPlacementTypes.REGISTRY_SUPPLIER.get().getValues();
    }
}
