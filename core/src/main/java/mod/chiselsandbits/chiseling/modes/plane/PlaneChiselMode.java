package mod.chiselsandbits.chiseling.modes.plane;

import com.communi.suggestu.scena.core.registries.AbstractCustomRegistryEntry;
import com.google.common.collect.Maps;
import mod.chiselsandbits.api.axissize.CollisionType;
import mod.chiselsandbits.api.blockinformation.BlockInformation;
import mod.chiselsandbits.api.change.IChangeTrackerManager;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.item.click.ClickProcessingState;
import mod.chiselsandbits.api.item.withmode.group.IToolModeGroup;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.util.IBatchMutation;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.api.util.RayTracingUtils;
import mod.chiselsandbits.api.util.VectorUtils;
import mod.chiselsandbits.registrars.ModChiselModeGroups;
import mod.chiselsandbits.utils.BitInventoryUtils;
import mod.chiselsandbits.utils.ItemStackUtils;
import mod.chiselsandbits.voxelshape.VoxelShapeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static mod.chiselsandbits.block.entities.ChiseledBlockEntity.ONE_THOUSANDS;

public class PlaneChiselMode extends AbstractCustomRegistryEntry implements IChiselMode
{
    private final int                       depth;
    private final MutableComponent displayName;
    private final MutableComponent multiLineDisplayName;
    private final ResourceLocation          iconName;

    PlaneChiselMode(
      final int depth,
      final MutableComponent displayName,
      final MutableComponent multiLineDisplayName,
      final ResourceLocation iconName)
    {
        this.depth = depth;
        this.displayName = displayName;
        this.multiLineDisplayName = multiLineDisplayName;
        this.iconName = iconName;
    }
    @Override
    public ClickProcessingState onLeftClickBy(
      final Player playerEntity, final IChiselingContext context)
    {
        final Optional<ClickProcessingState> rayTraceHandle = this.processRayTraceIntoContext(
          playerEntity,
          context,
          face -> Vec3.atLowerCornerOf(face.getOpposite().getNormal()),
          Direction::getOpposite
        );

        if (context.isSimulation())
        {
            return ClickProcessingState.DEFAULT;
        }

        return rayTraceHandle.orElseGet(() -> context.getMutator().map(mutator -> {
              try (IBatchMutation ignored =
                     mutator.batch(IChangeTrackerManager.getInstance().getChangeTracker(playerEntity)))
              {
                  context.setComplete();

                  final Map<BlockInformation, Integer> resultingBitCount = Maps.newHashMap();

                  final Predicate<IStateEntryInfo> filter = context.getStateFilter()
                                                                     .map(builder -> builder.apply(mutator))
                                                                     .orElse((state) -> true);

                  final int totalModifiedStates = mutator.inWorldMutableStream()
                    .filter(filter)
                    .mapToInt(state -> {
                        final BlockInformation currentState = state.getBlockInformation();

                        return context.tryDamageItemAndDoOrSetBrokenError(
                          () -> {
                              resultingBitCount.putIfAbsent(currentState, 0);
                              resultingBitCount.computeIfPresent(currentState, (s, currentCount) -> currentCount + 1);

                              state.clear();
                          });
                    })
                    .sum();

                  if (totalModifiedStates == 0) {
                      context.setError(LocalStrings.ChiselAttemptFailedNoValidStateFound.getText());
                  }

                  resultingBitCount.forEach((blockState, count) -> BitInventoryUtils.insertIntoOrSpawn(
                    playerEntity,
                    blockState,
                    count
                  ));
              }

              return ClickProcessingState.ALLOW;
          }).orElse(ClickProcessingState.DEFAULT)
        );
    }

    @Override
    public void onStoppedLeftClicking(final Player playerEntity, final IChiselingContext context)
    {

    }

    @SuppressWarnings("deprecation")
    @Override
    public ClickProcessingState onRightClickBy(final Player playerEntity, final IChiselingContext context)
    {
        final Optional<ClickProcessingState> rayTraceHandle = this.processRayTraceIntoContext(
          playerEntity,
          context,
          face -> Vec3.atLowerCornerOf(face.getNormal()),
          Function.identity()
        );

        if (context.isSimulation())
        {
            return ClickProcessingState.DEFAULT;
        }

        return rayTraceHandle.orElseGet(() -> context.getMutator().map(mutator -> {
              final BlockInformation heldBlockState = ItemStackUtils.getHeldBitBlockInformationFromPlayer(playerEntity);
              if (heldBlockState.isAir())
              {
                  return ClickProcessingState.DEFAULT;
              }

              final int missingBitCount = (int) mutator.stream()
                                                  .filter(state -> state.getBlockInformation().isAir())
                                                  .count();

              final IBitInventory playerBitInventory = IBitInventoryManager.getInstance().create(playerEntity);

              context.setComplete();
              if (playerBitInventory.canExtract(heldBlockState, missingBitCount) || playerEntity.isCreative())
              {
                  if (!playerEntity.isCreative())
                  {
                      playerBitInventory.extract(heldBlockState, missingBitCount);
                  }

                  try (IBatchMutation ignored =
                         mutator.batch(IChangeTrackerManager.getInstance().getChangeTracker(playerEntity)))
                  {
                      mutator.inWorldMutableStream()
                        .filter(state -> state.getBlockInformation().isAir())
                        .forEach(state -> state.overrideState(heldBlockState)); //We can use override state here to prevent the try-catch block.
                  }
              }
              else
              {
                  context.setError(LocalStrings.ChiselAttemptFailedNotEnoughBits.getText(heldBlockState.blockState().getBlock().getName()));
              }

              if (missingBitCount == 0) {
                  final BlockPos heightPos = mutator.getInWorldEndBlockPoint();
                  if (heightPos.getY() >= context.getWorld().getMaxBuildHeight()) {
                      Component component = (Component.translatable("build.tooHigh", context.getWorld().getMaxBuildHeight() - 1)).withStyle(ChatFormatting.RED);
                      playerEntity.sendSystemMessage(component);
                  }
              }

              return ClickProcessingState.ALLOW;
          }).orElse(ClickProcessingState.DEFAULT)
        );
    }

    @Override
    public void onStoppedRightClicking(final Player playerEntity, final IChiselingContext context)
    {

    }

    @Override
    public Optional<IAreaAccessor> getCurrentAccessor(final IChiselingContext context)
    {
        return context.getMutator().map(mutator -> mutator);
    }

    private Optional<ClickProcessingState> processRayTraceIntoContext(
      final Player playerEntity,
      final IChiselingContext context,
      final Function<Direction, Vec3> placementFacingAdapter,
      final Function<Direction, Direction> iterationAdaptor
    )
    {
        final HitResult rayTraceResult = RayTracingUtils.rayTracePlayer(playerEntity);
        if (rayTraceResult.getType() != HitResult.Type.BLOCK || !(rayTraceResult instanceof final BlockHitResult blockRayTraceResult))
        {
            context.setError(LocalStrings.ChiselAttemptFailedNoBlock.getText());
            return Optional.of(ClickProcessingState.DEFAULT);
        }

        final Vec3 hitVector = blockRayTraceResult.getLocation().add(
          placementFacingAdapter.apply(blockRayTraceResult.getDirection())
            .multiply(StateEntrySize.current().getSizePerHalfBit(), StateEntrySize.current().getSizePerHalfBit(), StateEntrySize.current().getSizePerHalfBit())
        );

        final Vec3 hitBlockPosVector = Vec3.atLowerCornerOf(VectorUtils.toBlockPos(hitVector));
        final Vec3 inBlockHitVector = hitVector.subtract(hitBlockPosVector);
        final Vec3 inBlockBitVector = inBlockHitVector.multiply(StateEntrySize.current().getBitsPerBlockSide(), StateEntrySize.current().getBitsPerBlockSide(), StateEntrySize.current().getBitsPerBlockSide());

        final Direction iterationDirection = iterationAdaptor.apply(blockRayTraceResult.getDirection());
        switch (iterationDirection)
        {
            case DOWN -> includeDownAxis(context, hitBlockPosVector, inBlockBitVector);
            case UP -> includeUpAxis(context, hitBlockPosVector, inBlockBitVector);
            case NORTH -> includeNorthAxis(context, hitBlockPosVector, inBlockBitVector);
            case SOUTH -> includeSouthAxis(context, hitBlockPosVector, inBlockBitVector);
            case WEST -> includeWestAxis(context, hitBlockPosVector, inBlockBitVector);
            case EAST -> includeEastAxis(context, hitBlockPosVector, inBlockBitVector);
        }

        return Optional.empty();
    }

    private void includeDownAxis(final IChiselingContext context, final Vec3 hitBlockPosVector, final Vec3 inBlockBitVector)
    {
        final BlockPos position = VectorUtils.toBlockPos(hitBlockPosVector);
        context.include(position, clampVectorToBlock(new Vec3(0, inBlockBitVector.y() - depth, 0).add(Vec3.atLowerCornerOf(Direction.UP.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vec3(15.5, inBlockBitVector.y() - depth, 15.5).add(Vec3.atLowerCornerOf(Direction.UP.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));

        context.include(position, clampVectorToBlock(new Vec3(0, inBlockBitVector.y() - 0.5f, 0).add(Vec3.atLowerCornerOf(Direction.UP.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vec3(15.5, inBlockBitVector.y() - 0.5f , 15.5).add(Vec3.atLowerCornerOf(Direction.UP.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
    }

    private void includeUpAxis(final IChiselingContext context, final Vec3 hitBlockPosVector, final Vec3 inBlockBitVector)
    {
        final BlockPos position = VectorUtils.toBlockPos(hitBlockPosVector);
        context.include(position, clampVectorToBlock(new Vec3(0, inBlockBitVector.y() + depth, 0).add(Vec3.atLowerCornerOf(Direction.DOWN.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vec3(15.5, inBlockBitVector.y() + depth, 15.5).add(Vec3.atLowerCornerOf(Direction.DOWN.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));

        context.include(position, clampVectorToBlock(new Vec3(0, inBlockBitVector.y() + 0.5f, 0).add(Vec3.atLowerCornerOf(Direction.DOWN.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vec3(15.5, inBlockBitVector.y() + 0.5f , 15.5).add(Vec3.atLowerCornerOf(Direction.DOWN.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
    }

    private void includeNorthAxis(final IChiselingContext context, final Vec3 hitBlockPosVector, final Vec3 inBlockBitVector)
    {
        final BlockPos position = VectorUtils.toBlockPos(hitBlockPosVector);
        context.include(position, clampVectorToBlock(new Vec3(0, 0, inBlockBitVector.z() - depth).add(Vec3.atLowerCornerOf(Direction.SOUTH.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vec3(15.5, 15.5, inBlockBitVector.z() - depth).add(Vec3.atLowerCornerOf(Direction.SOUTH.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));

        context.include(position, clampVectorToBlock(new Vec3(0, 0, inBlockBitVector.z() - 0.5f).add(Vec3.atLowerCornerOf(Direction.SOUTH.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vec3(15.5, 15.5, inBlockBitVector.z() - 0.5f).add(Vec3.atLowerCornerOf(Direction.SOUTH.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
    }

    private void includeSouthAxis(final IChiselingContext context, final Vec3 hitBlockPosVector, final Vec3 inBlockBitVector)
    {
        final BlockPos position = VectorUtils.toBlockPos(hitBlockPosVector);
        context.include(position, clampVectorToBlock(new Vec3(0, 0, inBlockBitVector.z() + depth).add(Vec3.atLowerCornerOf(Direction.NORTH.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vec3(15.5, 15.5, inBlockBitVector.z() + depth).add(Vec3.atLowerCornerOf(Direction.NORTH.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));

        context.include(position, clampVectorToBlock(new Vec3(0, 0, inBlockBitVector.z() + 0.5f).add(Vec3.atLowerCornerOf(Direction.NORTH.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vec3(15.5, 15.5, inBlockBitVector.z() + 0.5f).add(Vec3.atLowerCornerOf(Direction.NORTH.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
    }

    private void includeWestAxis(final IChiselingContext context, final Vec3 hitBlockPosVector, final Vec3 inBlockBitVector)
    {
        final BlockPos position = VectorUtils.toBlockPos(hitBlockPosVector);
        context.include(position, clampVectorToBlock(new Vec3(inBlockBitVector.x() - depth, 0, 0).add(Vec3.atLowerCornerOf(Direction.EAST.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vec3(inBlockBitVector.x() - depth, 15.5, 15.5).add(Vec3.atLowerCornerOf(Direction.EAST.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));

        context.include(position, clampVectorToBlock(new Vec3(inBlockBitVector.x() - 0.5f, 0, 0).add(Vec3.atLowerCornerOf(Direction.EAST.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vec3(inBlockBitVector.x() - 0.5f , 15.5, 15.5).add(Vec3.atLowerCornerOf(Direction.EAST.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
    }

    private void includeEastAxis(final IChiselingContext context, final Vec3 hitBlockPosVector, final Vec3 inBlockBitVector)
    {
        final BlockPos position = VectorUtils.toBlockPos(hitBlockPosVector);
        context.include(position, clampVectorToBlock(new Vec3(inBlockBitVector.x() + depth, 0, 0).add(Vec3.atLowerCornerOf(Direction.WEST.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vec3(inBlockBitVector.x() + depth, 15.5, 15.5).add(Vec3.atLowerCornerOf(Direction.WEST.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));

        context.include(position, clampVectorToBlock(new Vec3(inBlockBitVector.x() + 0.5f, 0, 0).add(Vec3.atLowerCornerOf(Direction.WEST.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vec3(inBlockBitVector.x() + 0.5f , 15.5, 15.5).add(Vec3.atLowerCornerOf(Direction.WEST.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
    }

    private Vec3 clampVectorToBlock(final Vec3 v)
    {
        return new Vec3(
          v.x() < 0 ? 0 : (v.x() >= 1 ? 1 - ONE_THOUSANDS : v.x()),
          v.y() < 0 ? 0 : (v.y() >= 1 ? 1 - ONE_THOUSANDS : v.y()),
          v.z() < 0 ? 0 : (v.z() >= 1 ? 1 - ONE_THOUSANDS : v.z())
        );
    }

    @Override
    public VoxelShape getShape(final IChiselingContext context)
    {
        if (context.getMutator().isEmpty())
            return Shapes.empty();

        return VoxelShapeManager.getInstance().get(context.getMutator().get(), CollisionType.ALL);
    }

    @Override
    public @NotNull ResourceLocation getIcon()
    {
        return iconName;
    }

    @Override
    public Component getDisplayName()
    {
        return this.displayName;
    }

    @Override
    public Component getMultiLineDisplayName()
    {
        return this.multiLineDisplayName;
    }

    @NotNull
    @Override
    public Optional<IToolModeGroup> getGroup()
    {
        return Optional.of(ModChiselModeGroups.PLANE);
    }
}
