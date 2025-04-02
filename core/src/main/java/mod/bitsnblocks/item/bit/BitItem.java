package mod.bitsnblocks.item.bit;

import com.communi.suggestu.scena.core.dist.Dist;
import com.communi.suggestu.scena.core.dist.DistExecutor;
import com.communi.suggestu.scena.core.fluid.IFluidManager;
import com.communi.suggestu.scena.core.registries.IPlatformRegistryManager;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import mod.bitsnblocks.api.blockinformation.BlockInformation;
import mod.bitsnblocks.api.chiseling.ChiselingOperation;
import mod.bitsnblocks.api.chiseling.IChiselingContext;
import mod.bitsnblocks.api.chiseling.IChiselingManager;
import mod.bitsnblocks.api.chiseling.ILocalChiselingContextCache;
import mod.bitsnblocks.api.chiseling.mode.IChiselMode;
import mod.bitsnblocks.api.client.render.preview.chiseling.IChiselContextPreviewRendererRegistry;
import mod.bitsnblocks.api.client.variant.state.IClientStateVariantManager;
import mod.bitsnblocks.api.config.IClientConfiguration;
import mod.bitsnblocks.api.config.IServerConfiguration;
import mod.bitsnblocks.api.item.bit.IBitItem;
import mod.bitsnblocks.api.item.chisel.IChiselItem;
import mod.bitsnblocks.api.item.chisel.IChiselingItem;
import mod.bitsnblocks.api.item.click.ClickProcessingState;
import mod.bitsnblocks.api.item.documentation.IDocumentableItem;
import mod.bitsnblocks.api.notifications.INotificationManager;
import mod.bitsnblocks.api.util.LocalStrings;
import mod.bitsnblocks.api.util.constants.Constants;
import mod.bitsnblocks.api.variant.state.IStateVariantManager;
import mod.bitsnblocks.chiseling.ChiselingManager;
import mod.bitsnblocks.client.render.ModRenderTypes;
import mod.bitsnblocks.compact.legacy.UpgradeUtils;
import mod.bitsnblocks.registrars.ModDataComponentTypes;
import mod.bitsnblocks.registrars.ModCreativeTabs;
import mod.bitsnblocks.utils.ItemStackUtils;
import mod.bitsnblocks.utils.TranslationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BitItem extends Item implements IChiselingItem, IBitItem, IDocumentableItem
{
    private static final Logger LOGGER = LogManager.getLogger();

    private final List<ItemStack> availableBitStacks = Lists.newLinkedList();

    private final ThreadLocal<Boolean> threadLocalBitMergeOperationInProgress = ThreadLocal.withInitial(() -> false);

    public BitItem(final Properties properties)
    {
        super(properties);
    }

    @Override
    public ClickProcessingState handleLeftClickProcessing(
      final Player playerEntity, final InteractionHand hand, final BlockPos position, final Direction face, final ClickProcessingState currentState)
    {
        return handleClickProcessing(
          playerEntity, hand, currentState, ChiselingOperation.CHISELING, IChiselMode::onLeftClickBy
        );
    }
    
    @Override
    public void onLeftClickProcessingEnd(final Player player, final ItemStack stack)
    {
        final IChiselMode chiselMode = getMode(stack);
        Optional<IChiselingContext> context = IChiselingManager.getInstance().get(
          player,
          chiselMode,
          ChiselingOperation.CHISELING);

        if (context.isEmpty()) {
            context = ILocalChiselingContextCache.getInstance().get(ChiselingOperation.CHISELING);
        }

        context.ifPresent(c -> {
            chiselMode.onStoppedLeftClicking(player, c);
            if (c.isComplete()) {
                player.getCooldowns().addCooldown(this, Constants.TICKS_BETWEEN_CHISEL_USAGE);
                ILocalChiselingContextCache.getInstance().clear(ChiselingOperation.CHISELING);
            }
        });
    }

    @Override
    public boolean canUse(final Player playerEntity, final ItemStack stack)
    {
        final boolean isAllowedToUse = ChiselingManager.getInstance().canChisel(playerEntity) && !playerEntity.getCooldowns().isOnCooldown(stack.getItem());
        if (getMode(stack).isSingleClickUse() && !isAllowedToUse && playerEntity.level().isClientSide() && IClientConfiguration.getInstance().getShowCoolDownError().get()) {
            INotificationManager.getInstance().notify(
                    getMode(stack).getIcon(),
                    new Vec3(1, 0, 0),
                    LocalStrings.ChiselAttemptFailedWaitForCoolDown.getText()
            );
        }

        return isAllowedToUse;
    }

    @NotNull
    @Override
    public IChiselMode getMode(final ItemStack stack)
    {
        UpgradeUtils.upgradeBitItem(stack);
        return stack.getOrDefault(ModDataComponentTypes.CHISEL_MODE.get(), IChiselMode.getDefaultMode());
    }

    @NotNull
    @Override
    public Component getName(@NotNull final ItemStack stack)
    {
        final BlockInformation containedStack = getBlockInformation(stack);
        final Block block = containedStack.blockState().getBlock();

        Component stateName = block.asItem().getName(new ItemStack(block));
        if (block instanceof final LiquidBlock flowingFluidBlock) {
            stateName = IFluidManager.getInstance().getDisplayName(flowingFluidBlock.fluid);
        }

        if (containedStack.variant().isPresent()) {
            stateName = IStateVariantManager.getInstance().getName(containedStack).orElse(stateName);
        }

        return Component.translatable(this.getDescriptionId(stack), stateName);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        final IChiselMode mode = getMode(stack);
        if (mode.getGroup().isPresent()) {
            tooltip.add(TranslationUtils.build("chiselmode.mode_grouped", mode.getGroup().get().getDisplayName(), mode.getDisplayName()));
        }
        else {
            tooltip.add(TranslationUtils.build("chiselmode.mode", mode.getDisplayName()));
        }

        final BlockInformation blockInformation = getBlockInformation(stack);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            IClientStateVariantManager.getInstance().appendHoverText(blockInformation, context, tooltip, flagIn);
        });


        super.appendHoverText(stack, context, tooltip, flagIn);
    }

    @Override
    public void setMode(final ItemStack stack, final IChiselMode mode)
    {
        if (mode == null)
            return;

        stack.set(ModDataComponentTypes.CHISEL_MODE.get(), mode);
    }

    @NotNull
    @Override
    public Collection<IChiselMode> getPossibleModes()
    {
        return IChiselMode.getRegistry().getValues().stream().sorted(Comparator.comparing(IChiselMode::getRegistryName)).collect(Collectors.toList());
    }

    @Override
    public ClickProcessingState handleRightClickProcessing(
      final Player playerEntity, final InteractionHand hand, final BlockPos position, final Direction face, final ClickProcessingState currentState)
    {
        return handleClickProcessing(
          playerEntity, hand, currentState, ChiselingOperation.PLACING, IChiselMode::onRightClickBy
        );
    }

    @Override
    public void onRightClickProcessingEnd(final Player player, final ItemStack stack)
    {
        final IChiselMode chiselMode = getMode(stack);
        Optional<IChiselingContext> context = IChiselingManager.getInstance().get(
          player,
          chiselMode,
          ChiselingOperation.PLACING);

        if (context.isEmpty()) {
            context = ILocalChiselingContextCache.getInstance().get(ChiselingOperation.PLACING);
        }

        context.ifPresent(c -> {
            chiselMode.onStoppedRightClicking(player, c);
            if (c.isComplete()) {
                player.getCooldowns().addCooldown(this, Constants.TICKS_BETWEEN_CHISEL_USAGE);
                ILocalChiselingContextCache.getInstance().clear(ChiselingOperation.PLACING);
            }
        });
    }

    private ClickProcessingState handleClickProcessing(
      final Player playerEntity,
      final InteractionHand hand,
      final ClickProcessingState currentState,
      final ChiselingOperation modeOfOperation,
      final ChiselModeInteractionCallback callback)
    {
        final ItemStack itemStack = playerEntity.getItemInHand(hand);
        if (itemStack.isEmpty() || itemStack.getItem() != this)
            return currentState;

        if (modeOfOperation.isChiseling() && IServerConfiguration.getInstance().getRequireChiselInOffHandForBitBreaking().get()) {
            final ItemStack offHandStack = playerEntity.getItemInHand(InteractionHand.OFF_HAND);
            if (offHandStack.isEmpty() || !(offHandStack.getItem() instanceof IChiselItem)) {
                playerEntity.getCooldowns().addCooldown(this, Constants.TICKS_BETWEEN_CHISEL_ERRORS);
                INotificationManager.getInstance().notify(
                        getMode(itemStack).getIcon(),
                        new Vec3(1, 0, 0),
                        LocalStrings.ChiselAttemptMissingChiselInOffhand.getText()
                );
                return currentState;
            }
        }

        final IChiselingItem chiselingItem = (IChiselingItem) itemStack.getItem();
        final IChiselMode chiselMode = chiselingItem.getMode(itemStack);

        final IChiselingContext context = IChiselingManager.getInstance().getOrCreateContext(
          playerEntity,
          chiselMode,
          modeOfOperation,
          false,
          itemStack);

        final ClickProcessingState resultState = callback.run(chiselMode, playerEntity, context);

        if (context.isComplete()) {
            playerEntity.getCooldowns().addCooldown(this, Constants.TICKS_BETWEEN_CHISEL_USAGE);
            ILocalChiselingContextCache.getInstance().clear(modeOfOperation);
        }

        if (context.getError().isPresent() && context.getWorld().isClientSide()) {
            INotificationManager.getInstance().notify(
              context.getMode().getIcon(),
              new Vec3(1, 0, 0),
              context.getError().get()
            );
        }

        return resultState;
    }

    @Override
    public @NotNull BlockInformation getBlockInformation(final ItemStack stack)
    {
        UpgradeUtils.upgradeBitItem(stack);
        return stack.getOrDefault(ModDataComponentTypes.BLOCK_INFORMATION.get(), BlockInformation.AIR);
    }

    @Override
    public void onMergeOperationWithBagBeginning()
    {
        this.threadLocalBitMergeOperationInProgress.set(true);
    }

    @Override
    public void onMergeOperationWithBagEnding()
    {
        this.threadLocalBitMergeOperationInProgress.set(false);
    }

    @Override
    public boolean shouldDrawDefaultHighlight(@NotNull final Player playerEntity)
    {
        final ItemStack itemStack = ItemStackUtils.getHighlightItemStackFromPlayer(playerEntity);
        if (itemStack.isEmpty() || itemStack.getItem() != this)
        {
            return true;
        }

        final IChiselingItem chiselingItem = (IChiselingItem) itemStack.getItem();
        final IChiselMode chiselMode = chiselingItem.getMode(itemStack);

        final Optional<IChiselingContext> potentiallyExistingContext =
          IChiselingManager.getInstance().get(playerEntity, chiselMode, ChiselingOperation.CHISELING);
        if (potentiallyExistingContext.isPresent())
        {
            final IChiselingContext context = potentiallyExistingContext.get();

            if (context.getMutator().isPresent())
            {
                return false;
            }

            final IChiselingContext currentContextSnapshot = context.createSnapshot();

            if (currentContextSnapshot.getModeOfOperandus() == ChiselingOperation.CHISELING)
            {
                chiselMode.onLeftClickBy(
                  playerEntity,
                  currentContextSnapshot
                );
            }
            else
            {
                chiselMode.onRightClickBy(
                  playerEntity,
                  currentContextSnapshot
                );
            }

            return currentContextSnapshot.getMutator().isEmpty();
        }

        final Optional<IChiselingContext> localCachedContext = ILocalChiselingContextCache
                                                                 .getInstance()
                                                                 .get(ChiselingOperation.CHISELING);

        if (localCachedContext.isPresent())
        {
            final IChiselingContext context = localCachedContext.get();

            if (
              context.getMode() == chiselMode
            )

                if (context.getMutator().isPresent())
                {
                    return false;
                }

            return context.getMutator().isEmpty();
        }

        final IChiselingContext context = IChiselingManager.getInstance().create(
          playerEntity,
          chiselMode,
          ChiselingOperation.CHISELING,
          true,
          itemStack);

        //We try a left click render primary.
        chiselMode.onLeftClickBy(
          playerEntity,
          context
        );

        if (context.getMutator().isPresent())
            return false;

        chiselMode.onRightClickBy(
          playerEntity,
          context
        );

        return context.getMutator().isEmpty();
    }

    @Override
    public void renderHighlight(
      final Player playerEntity,
      final LevelRenderer worldRenderer,
      final PoseStack matrixStack,
      final float partialTicks)
    {
        final ItemStack itemStack = ItemStackUtils.getHighlightItemStackFromPlayer(playerEntity);
        if (itemStack.isEmpty() || itemStack.getItem() != this)
            return;

        final IChiselingItem chiselingItem = (IChiselingItem) itemStack.getItem();
        final IChiselMode chiselMode = chiselingItem.getMode(itemStack);

        final Optional<IChiselingContext> potentiallyExistingContext =
          IChiselingManager.getInstance().get(playerEntity, chiselMode);


        final Optional<IChiselingContext> potentialChiselingContext = ILocalChiselingContextCache.getInstance()
                                                                       .get(ChiselingOperation.CHISELING);

        final Optional<IChiselingContext> potentialPlacingContext = ILocalChiselingContextCache.getInstance()
          .get(ChiselingOperation.PLACING);

        if (potentiallyExistingContext.isPresent()) {
            final IChiselingContext currentContextSnapshot = potentiallyExistingContext.get().createSnapshot();

            if (currentContextSnapshot.getModeOfOperandus() == ChiselingOperation.CHISELING) {
                chiselMode.onLeftClickBy(
                  playerEntity,
                  currentContextSnapshot
                );
            }
            else
            {
                chiselMode.onRightClickBy(
                  playerEntity,
                  currentContextSnapshot
                );
            }

            IChiselContextPreviewRendererRegistry.getInstance().getCurrent()
                                                                 .renderExistingContextsBoundingBox(matrixStack, currentContextSnapshot);

            return;
        } else if (potentialChiselingContext.isPresent()) {
            final IChiselingContext chiselingContext = potentialChiselingContext.get();
            if (potentialChiselingContext.get().getMode() == chiselMode
                && chiselingContext.getMode().isStillValid(playerEntity, chiselingContext, ChiselingOperation.CHISELING))
            {
                IChiselContextPreviewRendererRegistry.getInstance().getCurrent()
                  .renderExistingContextsBoundingBox(matrixStack, chiselingContext);
            }
            else
            {
                ILocalChiselingContextCache.getInstance().clear(ChiselingOperation.CHISELING);
            }

            if (potentialPlacingContext.isPresent()) {
                final IChiselingContext placingContext = potentialPlacingContext.get();
                if (placingContext.getMode() == chiselMode &&
                      potentialPlacingContext.get().getMode().isStillValid(playerEntity, potentialPlacingContext.get(), ChiselingOperation.PLACING))
                {
                    IChiselContextPreviewRendererRegistry.getInstance().getCurrent()
                      .renderExistingContextsBoundingBox(matrixStack, placingContext);
                }
                else
                {
                    ILocalChiselingContextCache.getInstance().clear(ChiselingOperation.PLACING);
                }
            }

            return;
        }
        else if (potentialPlacingContext.isPresent()
                   && potentialPlacingContext.get().getMode() == chiselMode
                   && chiselMode.isStillValid(playerEntity, potentialPlacingContext.get(), ChiselingOperation.PLACING)) {

            final IChiselingContext context = potentialPlacingContext.get();

            IChiselContextPreviewRendererRegistry.getInstance().getCurrent()
              .renderExistingContextsBoundingBox(matrixStack, context);

            return;
        }

        final IChiselingContext chiselingContext = IChiselingManager.getInstance().create(
          playerEntity,
          chiselMode,
          ChiselingOperation.CHISELING,
          true,
          itemStack
        );
        final IChiselingContext placingContext = IChiselingManager.getInstance().create(
          playerEntity,
          chiselMode,
          ChiselingOperation.PLACING,
          true,
          itemStack
        );

        chiselMode.onLeftClickBy(
          playerEntity,
          chiselingContext
        );
        chiselMode.onRightClickBy(
          playerEntity,
          placingContext
        );

        if (chiselingContext.getMutator().isPresent() && chiselingContext.getError().isEmpty()) {
            IChiselContextPreviewRendererRegistry.getInstance().getCurrent()
              .renderExistingContextsBoundingBox(matrixStack, chiselingContext);
            ILocalChiselingContextCache.getInstance().set(ChiselingOperation.CHISELING, chiselingContext);
        }
        if (placingContext.getMutator().isPresent() && placingContext.getError().isEmpty()) {
            IChiselContextPreviewRendererRegistry.getInstance().getCurrent()
              .renderExistingContextsBoundingBox(matrixStack, placingContext);
            ILocalChiselingContextCache.getInstance().set(ChiselingOperation.PLACING, placingContext);
        }
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(ModRenderTypes.MEASUREMENT_LINES.get());
    }

    @Override
    public boolean isDamageableDuringChiseling()
    {
        return false;
    }

    @FunctionalInterface
    private interface ChiselModeInteractionCallback {
        ClickProcessingState run(final IChiselMode chiselMode, final Player playerEntity, final IChiselingContext context);
    }

    @Override
    public Map<String, ItemStack> getDocumentableInstances(final Item item)
    {
        ensureAvailableBitStacksAreLoaded();

        return this.availableBitStacks
          .stream()
          .filter(stack -> !stack.isEmpty())
          .collect(Collectors.toMap(
            stack -> "bit_" + IPlatformRegistryManager.getInstance().getBlockRegistry().getKey(this.getBlockInformation(stack).blockState().getBlock()).toString().replace(":", "_"),
            Function.identity()
          ));
    }


    private void ensureAvailableBitStacksAreLoaded()
    {
        if (availableBitStacks.isEmpty()) {
            ModCreativeTabs.BITS.get().buildContents(new CreativeModeTab.ItemDisplayParameters(FeatureFlagSet.of(), false, null));
            availableBitStacks.addAll(
                    ModCreativeTabs.BITS.get().getDisplayItems()
            );

            availableBitStacks.sort(Comparator.comparing(stack -> {
                if (!(stack.getItem() instanceof IBitItem))
                    throw new IllegalStateException("Stack did not contain a bit item.");

                return ((IBitItem) stack.getItem()).getBlockInformation(stack);
            }));
        }
    }
}
