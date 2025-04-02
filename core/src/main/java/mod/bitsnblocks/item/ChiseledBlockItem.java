package mod.bitsnblocks.item;

import com.google.common.base.Suppliers;
import mod.bitsnblocks.api.util.VectorUtils;
import mod.bitsnblocks.api.blockinformation.BlockInformation;
import mod.bitsnblocks.api.change.IChangeTrackerManager;
import mod.bitsnblocks.api.config.IClientConfiguration;
import mod.bitsnblocks.api.exceptions.SpaceOccupiedException;
import mod.bitsnblocks.api.item.chiseled.IChiseledBlockItem;
import mod.bitsnblocks.api.item.multistate.IMultiStateItemStack;
import mod.bitsnblocks.api.modification.operation.IModificationOperation;
import mod.bitsnblocks.api.multistate.StateEntrySize;
import mod.bitsnblocks.api.multistate.accessor.IAreaAccessor;
import mod.bitsnblocks.api.multistate.mutator.IMutatorFactory;
import mod.bitsnblocks.api.util.IBatchMutation;
import mod.bitsnblocks.api.multistate.mutator.world.IWorldAreaMutator;
import mod.bitsnblocks.api.multistate.snapshot.IMultiStateSnapshot;
import mod.bitsnblocks.api.placement.PlacementResult;
import mod.bitsnblocks.api.util.BlockInformationUtils;
import mod.bitsnblocks.api.util.HelpTextUtils;
import mod.bitsnblocks.api.util.LocalStrings;
import mod.bitsnblocks.block.ChiseledBlock;
import mod.bitsnblocks.item.multistate.SingleBlockMultiStateItemStack;
import mod.bitsnblocks.multistate.snapshot.SimpleSnapshot;
import mod.bitsnblocks.registrars.ModDataComponentTypes;
import mod.bitsnblocks.registrars.ModModificationOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class ChiseledBlockItem extends BlockItem implements IChiseledBlockItem
{

    private static final Supplier<ItemStack> DEFAULT_INSTANCE = Suppliers.memoize(() -> {
        final RandomSource random = RandomSource.createNewThreadLocalInstance();

        final int blockStateCount = (StateEntrySize.current().getBitsPerBlockSide() / 4) *
                                      (StateEntrySize.current().getBitsPerBlockSide() / 4) *
                                      (StateEntrySize.current().getBitsPerBlockSide() / 4);
        final List<BlockInformation> blockInformation = new ArrayList<>(blockStateCount);
        for (int i = 0; i < blockStateCount; i++)
        {
            blockInformation.add(BlockInformationUtils.getRandomSupportedInformation(random));
        }

        final SimpleSnapshot results = new SimpleSnapshot(BlockInformation.AIR);

        results.mutableStream()
          .forEach(stateEntryInfo -> {
              final Vec3 pos = stateEntryInfo.getStartPoint()
                                 .multiply(StateEntrySize.current().getBitsPerBlockSideScalingVector());

              final Vec3 indexPos = pos.multiply(1 / 4d, 1/4d, 1/4d);
              final Vec3i index = VectorUtils.toInteger(indexPos.x(), indexPos.y(), indexPos.z());

              final int size = StateEntrySize.current().getBitsPerBlockSide() / 4;
              final int blockInformationIndex = index.getX() + (index.getY() * size) + (index.getZ() * size * size);
              final BlockInformation info = blockInformation.get(blockInformationIndex);
              stateEntryInfo.overrideState(info);
          });

        final ItemStack stack = results.toItemStack().toBlockStack();
        stack.set(ModDataComponentTypes.IS_DEFAULT_INSTANCE.get(), true);
        return stack;
    });

    public ChiseledBlockItem(final Block blockIn, final Properties builder)
    {
        super(blockIn, builder);
    }

    @Override
    public @NotNull Component getName(final @NotNull ItemStack stack)
    {
        if (stack.getOrDefault(ModDataComponentTypes.IS_DEFAULT_INSTANCE.get(), false)) {
            return LocalStrings.DefaultChiseledBlockItemName.getText();
        }

        final IMultiStateItemStack multiStateItemStack = this.createItemStack(stack);
        final Component primaryStateComponent = multiStateItemStack.getStatistics().getPrimaryState().blockState().getBlock().getName();
        return LocalStrings.ChiseledBlockItemName.getText(primaryStateComponent);
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
        return new SingleBlockMultiStateItemStack(stack);
    }

    @NotNull
    @Override
    public InteractionResult place(@NotNull final BlockPlaceContext context)
    {
        final Player player = context.getPlayer();
        if (player == null)
            return InteractionResult.FAIL;

        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();

        final IAreaAccessor source = this.createItemStack(context.getItemInHand());
        final IWorldAreaMutator areaMutator = player.isShiftKeyDown() ?
                                                IMutatorFactory.getInstance().covering(
                                                  level,
                                                  context.getClickLocation(),
                                                  context.getClickLocation().add(1d, 1d, 1d)
                                                )
                                                :
                                                  IMutatorFactory.getInstance().in(level, pos);
        final IMultiStateSnapshot attemptTarget = areaMutator.createSnapshot();

        final boolean noCollisions = source.stream().sequential()
          .allMatch(stateEntryInfo -> {
              try
              {
                  attemptTarget.setInAreaTarget(
                    stateEntryInfo.getBlockInformation(),
                    stateEntryInfo.getStartPoint());

                  return true;
              }
              catch (SpaceOccupiedException exception)
              {
                  return false;
              }
          });

        if (noCollisions)
        {
            try (IBatchMutation ignored = areaMutator.batch(IChangeTrackerManager.getInstance().getChangeTracker(context.getPlayer())))
            {
                source.stream().forEach(
                  stateEntryInfo -> {
                      try
                      {
                          areaMutator.setInAreaTarget(
                            stateEntryInfo.getBlockInformation(),
                            stateEntryInfo.getStartPoint());
                      }
                      catch (SpaceOccupiedException ignored1)
                      {
                      }
                  }
                );
            }

            if (getBlock() instanceof ChiseledBlock chiseledBlock)
            {
                level.gameEvent(player, GameEvent.BLOCK_PLACE, pos);
                final BlockState state = level.getBlockState(pos);
                final SoundType soundtype = chiseledBlock.getSoundType(state, level, pos, player);
                level.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            }

            if (context.getPlayer() == null || !context.getPlayer().isCreative()) {
                context.getItemInHand().shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        HelpTextUtils.build(LocalStrings.HelpBitBag, tooltip);
    }

    @Override
    public PlacementResult getPlacementResult(final ItemStack heldStack, final Player playerEntity, final BlockHitResult blockRayTraceResult)
    {
        final IAreaAccessor source = this.createItemStack(heldStack);
        final Vec3 target = getTargetedPosition(heldStack, playerEntity, blockRayTraceResult);
        final IWorldAreaMutator areaMutator = IMutatorFactory.getInstance().covering(
          playerEntity.level(),
          target,
          target.add(1,1,1));
        final IMultiStateSnapshot attemptTarget = areaMutator.createSnapshot();

        final boolean noSpaceOccupied = source.stream()
                .allMatch(stateEntryInfo -> {
                    try {
                        attemptTarget.setInAreaTarget(
                                stateEntryInfo.getBlockInformation(),
                                stateEntryInfo.getStartPoint());

                        return true;
                    } catch (SpaceOccupiedException exception) {
                        return false;
                    }
                });
        return noSpaceOccupied
                ? PlacementResult.success()
                : PlacementResult.failure(
                        IClientConfiguration::getNotFittingPatternPlacementColor,
                        LocalStrings.PatternPlacementNotAnAirBlock.getText());
    }

    @Override
    public @NotNull ItemStack getDefaultInstance()
    {
        return DEFAULT_INSTANCE.get();
    }

    @Override
    public @NotNull IModificationOperation getMode(final ItemStack stack)
    {
        return ModModificationOperation.ROTATE_AROUND_X.get();
    }

    @Override
    public void setMode(final ItemStack stack, final IModificationOperation mode)
    {
        final IMultiStateItemStack multiStateItemStack = this.createItemStack(stack);
        final IMultiStateSnapshot snapshot = multiStateItemStack.createSnapshot();
        mode.apply(snapshot);
        final IMultiStateItemStack resultStack = snapshot.toItemStack();
        resultStack.writeDataTo(stack);
    }

    @Override
    public @NotNull Collection<IModificationOperation> getPossibleModes()
    {
        return ModModificationOperation.REGISTRY_SUPPLIER.get().getValues();
    }

    @Override
    public boolean requiresUpdateOnClosure()
    {
        return false;
    }
}
