package mod.bitsnblocks.chiseling.modes.connected.material;

import com.communi.suggestu.scena.core.registries.AbstractCustomRegistryEntry;
import com.google.common.collect.Maps;
import mod.bitsnblocks.api.axissize.CollisionType;
import mod.bitsnblocks.api.blockinformation.BlockInformation;
import mod.bitsnblocks.api.change.IChangeTrackerManager;
import mod.bitsnblocks.api.chiseling.ChiselingOperation;
import mod.bitsnblocks.api.chiseling.IChiselingContext;
import mod.bitsnblocks.api.chiseling.mode.IChiselMode;
import mod.bitsnblocks.api.inventory.bit.IBitInventory;
import mod.bitsnblocks.api.inventory.management.IBitInventoryManager;
import mod.bitsnblocks.api.item.click.ClickProcessingState;
import mod.bitsnblocks.api.item.withmode.group.IToolModeGroup;
import mod.bitsnblocks.api.multistate.StateEntrySize;
import mod.bitsnblocks.api.multistate.accessor.IAreaAccessor;
import mod.bitsnblocks.api.multistate.accessor.IStateEntryInfo;
import mod.bitsnblocks.api.multistate.mutator.IMutatorFactory;
import mod.bitsnblocks.api.util.IBatchMutation;
import mod.bitsnblocks.api.util.IQuadFunction;
import mod.bitsnblocks.api.util.LocalStrings;
import mod.bitsnblocks.api.util.RayTracingUtils;
import mod.bitsnblocks.api.util.VectorUtils;
import mod.bitsnblocks.registrars.ModChiselModeGroups;
import mod.bitsnblocks.registrars.ModMetadataKeys;
import mod.bitsnblocks.utils.BitInventoryUtils;
import mod.bitsnblocks.utils.ItemStackUtils;
import mod.bitsnblocks.voxelshape.VoxelShapeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
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

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class ConnectedMaterialChiselingMode extends AbstractCustomRegistryEntry implements IChiselMode
{
    private final int              depth;
    private final MutableComponent displayName;
    private final MutableComponent multiLineDisplayName;
    private final ResourceLocation iconName;

    public ConnectedMaterialChiselingMode(
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
      final Player Player, final IChiselingContext context)
    {
        final Optional<ClickProcessingState> rayTraceHandle = this.processRayTraceIntoContext(
          Player,
          context,
          Direction::getOpposite,
          face -> Vec3.atLowerCornerOf(face.getOpposite().getNormal()),
          IQuadFunction.fourthIdentity(),
          position -> IMutatorFactory.getInstance().in(
            context.getWorld(),
            VectorUtils.toBlockPos(position)
          ),
          direction -> Vec3i.ZERO
        );

        if (context.isSimulation())
        {
            return ClickProcessingState.DEFAULT;
        }

        return rayTraceHandle.orElseGet(() -> context.getMutator().map(mutator -> {
              try (IBatchMutation ignored =
                     mutator.batch(IChangeTrackerManager.getInstance().getChangeTracker(Player)))
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
                    Player,
                    blockState,
                    count
                  ));
              }

              return ClickProcessingState.ALLOW;
          }).orElse(ClickProcessingState.DEFAULT)
        );
    }

    @Override
    public void onStoppedLeftClicking(final Player Player, final IChiselingContext context)
    {
        //NOOP
    }

    @Override
    public ClickProcessingState onRightClickBy(final Player player, final IChiselingContext context)
    {
        final Optional<ClickProcessingState> rayTraceHandle = this.processRayTraceIntoContext(
          player,
          context,
          UnaryOperator.identity(),
          face -> Vec3.atLowerCornerOf(face.getNormal()),
          (hitPos, inBlockTargetedPosition, hitFace, candidatePosition) -> {
              if (inBlockTargetedPosition.equals(candidatePosition))
              {
                  return Vec3.atLowerCornerOf(hitPos)
                    .add(inBlockTargetedPosition)
                    .add(Vec3.atLowerCornerOf(
                          hitFace.getOpposite().getNormal()
                        )
                        .multiply(StateEntrySize.current().getSizePerBitScalingVector())
                    );
              }

              final Vec3 relevantAxisCandidate = candidatePosition.multiply(
                Math.abs(hitFace.getNormal().getX()),
                Math.abs(hitFace.getNormal().getY()),
                Math.abs(hitFace.getNormal().getZ())
              );
              final Vec3 noneRelevantAxisCandidates = candidatePosition.subtract(relevantAxisCandidate);
              final Vec3 relevantAxisTarget = inBlockTargetedPosition.multiply(
                Math.abs(hitFace.getNormal().getX()),
                Math.abs(hitFace.getNormal().getY()),
                Math.abs(hitFace.getNormal().getZ())
              );

              final Vec3 offset = candidatePosition.subtract(inBlockTargetedPosition);
              final Vec3 relevantAxisOffset = offset.multiply(
                Math.abs(hitFace.getNormal().getX()),
                Math.abs(hitFace.getNormal().getY()),
                Math.abs(hitFace.getNormal().getZ())
              );

              return noneRelevantAxisCandidates.add(relevantAxisTarget).subtract(relevantAxisOffset)
                .add(hitPos.getX(), hitPos.getY(), hitPos.getZ())
                .add(Vec3.atLowerCornerOf(
                      hitFace.getOpposite().getNormal()
                    )
                    .multiply(StateEntrySize.current().getSizePerBitScalingVector())
                );
          },
          position -> IMutatorFactory.getInstance().covering(
            context.getWorld(),
            VectorUtils.toBlockPos(position.subtract(1, 1, 1)),
            VectorUtils.toBlockPos(position.add(1, 1, 1))
          ),
          direction -> new Vec3i(
            direction.getNormal().getX() * depth,
            direction.getNormal().getY() * depth,
            direction.getNormal().getZ() * depth
          )
        );

        if (context.isSimulation())
        {
            return ClickProcessingState.DEFAULT;
        }

        return rayTraceHandle.orElseGet(() -> context.getMutator().map(mutator -> {
              final BlockInformation heldBlockState = ItemStackUtils.getHeldBitBlockInformationFromPlayer(player);
              if (heldBlockState.isAir())
              {
                  context.setError(LocalStrings.ChiselAttemptFailedNoPlaceableBitHeld.getText());
                  return ClickProcessingState.DEFAULT;
              }

              final Predicate<IStateEntryInfo> filter = context.getStateFilter()
                .map(builder -> builder.apply(mutator))
                .orElse((state) -> true);

              final int missingBitCount = (int) mutator.stream()
                .filter(state -> state.getBlockInformation().isAir() && filter.test(state))
                .count();

              final IBitInventory playerBitInventory = IBitInventoryManager.getInstance().create(player);

              context.setComplete();
              if (playerBitInventory.canExtract(heldBlockState, missingBitCount) || player.isCreative())
              {
                  if (!player.isCreative())
                  {
                      playerBitInventory.extract(heldBlockState, missingBitCount);
                  }

                  try (IBatchMutation ignored =
                         mutator.batch(IChangeTrackerManager.getInstance().getChangeTracker(player)))
                  {
                      mutator.inWorldMutableStream()
                        .filter(state -> state.getBlockInformation().isAir() && filter.test(state))
                        .forEach(state -> state.overrideState(heldBlockState)); //We can use override state here to prevent the try-catch block.
                  }
              }
              else
              {
                  context.setError(LocalStrings.ChiselAttemptFailedNotEnoughBits.getText(heldBlockState.blockState().getBlock().getName()));
              }

              if (missingBitCount == 0)
              {
                  final BlockPos heightPos = mutator.getInWorldEndBlockPoint();
                  if (heightPos.getY() >= context.getWorld().getMaxBuildHeight())
                  {
                      context.setError(LocalStrings.ChiselAttemptFailedAttemptTooHigh.getText());
                  }
                  else if (heightPos.getY() <= context.getWorld().getMinBuildHeight()) {
                      context.setError(LocalStrings.ChiselAttemptFailedAttemptTooLow.getText());
                  }
              }

              return ClickProcessingState.ALLOW;
          }).orElse(ClickProcessingState.DEFAULT)
        );
    }

    @Override
    public void onStoppedRightClicking(final Player Player, final IChiselingContext context)
    {
        //NOOP
    }

    @Override
    public Optional<IAreaAccessor> getCurrentAccessor(final IChiselingContext context)
    {
        return context.getMutator().map(mutator -> mutator);
    }

    @Override
    public boolean isStillValid(final Player Player, final IChiselingContext context, final ChiselingOperation modeOfOperation)
    {
        final Optional<Set<Vec3i>> validPositions = context.getMetadata(ModMetadataKeys.VALID_POSITIONS.get());
        final Optional<Direction> targetedSide = context.getMetadata(ModMetadataKeys.TARGETED_SIDE.get());
        final Optional<BlockPos> targetedBlockPos = context.getMetadata(ModMetadataKeys.TARGETED_BLOCK.get());

        if (validPositions.isEmpty() || targetedSide.isEmpty() || targetedBlockPos.isEmpty())
        {
            return false;
        }

        final HitResult hitResult = RayTracingUtils.rayTracePlayer(Player);
        if (hitResult.getType() != HitResult.Type.BLOCK || !(hitResult instanceof final BlockHitResult blockHitResult))
        {
            return false;
        }

        if (blockHitResult.getDirection() != targetedSide.get())
        {
            return false;
        }

        final Function<Direction, Vec3> placementFacingAdapter = modeOfOperation == ChiselingOperation.CHISELING ?
                                                                   face -> Vec3.atLowerCornerOf(face.getOpposite().getNormal()) :
                                                                                                                                  face -> Vec3.atLowerCornerOf(face.getNormal());

        final Vec3 hitVector = blockHitResult.getLocation().add(
          placementFacingAdapter.apply(blockHitResult.getDirection())
            .multiply(StateEntrySize.current().getSizePerHalfBit(), StateEntrySize.current().getSizePerHalfBit(), StateEntrySize.current().getSizePerHalfBit())
        );

        final BlockPos hitPos = VectorUtils.toBlockPos(hitVector);
        final Vec3 hitBlockPosVector = Vec3.atLowerCornerOf(hitPos);
        final Vec3 inBlockHitVector = hitVector.subtract(hitBlockPosVector);
        final Vec3i selectedPosition = VectorUtils.toInteger(
          inBlockHitVector.x() * StateEntrySize.current().getBitsPerBlockSide(),
          inBlockHitVector.y() * StateEntrySize.current().getBitsPerBlockSide(),
          inBlockHitVector.z() * StateEntrySize.current().getBitsPerBlockSide()
        );

        return validPositions.get().contains(selectedPosition) && hitPos.equals(targetedBlockPos.get());
    }

    private Optional<ClickProcessingState> processRayTraceIntoContext(
      final Player Player,
      final IChiselingContext context,
      final UnaryOperator<Direction> searchDirectionAdapter,
      final Function<Direction, Vec3> placementFacingAdapter,
      final IQuadFunction<BlockPos, Vec3, Direction, Vec3, Vec3> stateExtractionAdapter,
      final Function<Vec3, IAreaAccessor> areaAccessorBuilder,
      final Function<Direction, Vec3i> filterOffsetProducer
    )
    {
        final HitResult hitResult = RayTracingUtils.rayTracePlayer(Player);
        if (hitResult.getType() != HitResult.Type.BLOCK || !(hitResult instanceof final BlockHitResult blockHitResult))
        {
            context.setError(LocalStrings.ChiselAttemptFailedNoBlock.getText());
            return Optional.of(ClickProcessingState.DEFAULT);
        }

        final Vec3 hitVector = blockHitResult.getLocation().add(
          placementFacingAdapter.apply(blockHitResult.getDirection())
            .multiply(StateEntrySize.current().getSizePerHalfBit(), StateEntrySize.current().getSizePerHalfBit(), StateEntrySize.current().getSizePerHalfBit())
        );

        final BlockPos hitPos = VectorUtils.toBlockPos(hitVector);
        final Vec3 hitBlockPosVector = Vec3.atLowerCornerOf(hitPos);
        final Vec3 inBlockHitVector = hitVector.subtract(hitBlockPosVector);

        final Deque<Vec3i> toProcess = new LinkedList<>();
        final IAreaAccessor worldAccessor = areaAccessorBuilder.apply(hitVector);

        final Set<Vec3i> processed = new HashSet<>();
        final Set<Vec3i> validPositions = new HashSet<>();

        final Set<Vec3i> offsets = new HashSet<>();
        offsets.add(searchDirectionAdapter.apply(blockHitResult.getDirection()).getNormal());
        Arrays.stream(Direction.values())
          .filter(direction -> direction.getAxis() != blockHitResult.getDirection().getAxis())
          .map(Direction::getNormal)
          .forEach(offsets::add);

        final Vec3i selectedPosition = VectorUtils.toInteger(
          inBlockHitVector.x() * StateEntrySize.current().getBitsPerBlockSide(),
          inBlockHitVector.y() * StateEntrySize.current().getBitsPerBlockSide(),
          inBlockHitVector.z() * StateEntrySize.current().getBitsPerBlockSide()
        );
        toProcess.addLast(selectedPosition);

        final Vec3i relevantSelectedAxisVector =
          new Vec3i(
            selectedPosition.getX() * Math.abs(blockHitResult.getDirection().getNormal().getX()),
            selectedPosition.getY() * Math.abs(blockHitResult.getDirection().getNormal().getY()),
            selectedPosition.getZ() * Math.abs(blockHitResult.getDirection().getNormal().getZ())
          );

        final Vec3 selectedInBlockPosition = Vec3.atLowerCornerOf(
          selectedPosition
        ).multiply(
          StateEntrySize.current().getSizePerBitScalingVector()
        );

        final Optional<IStateEntryInfo> targetedInfo = worldAccessor.getInAreaTarget(
          stateExtractionAdapter.apply(hitPos, selectedInBlockPosition, blockHitResult.getDirection(), selectedInBlockPosition)
        );

        if (targetedInfo.isEmpty())
        {
            context.setError(LocalStrings.ChiselAttemptFailedTargetedBlockNotChiselable.getText());
            return Optional.of(ClickProcessingState.DEFAULT);
        }

        while (!toProcess.isEmpty())
        {
            final Vec3i targetedPosition = toProcess.removeFirst();
            final Vec3 targetedInBlockPosition = Vec3.atLowerCornerOf(
              targetedPosition
            ).multiply(
              StateEntrySize.current().getSizePerBitScalingVector()
            );
            final Optional<IStateEntryInfo> targetCandidate = worldAccessor.getInAreaTarget(
              stateExtractionAdapter.apply(hitPos, selectedInBlockPosition, blockHitResult.getDirection(), targetedInBlockPosition)
            );

            processed.add(targetedPosition);

            if (targetCandidate.isEmpty())
            {
                continue;
            }

            IStateEntryInfo target = targetCandidate.get();

            if (target.getBlockInformation().equals(targetedInfo.get().getBlockInformation()) || target.getBlockInformation().isAir())
            {
                if (target.getBlockInformation().equals(targetedInfo.get().getBlockInformation()))
                {
                    //We are somehow connected to the targeted bit and of the same state.
                    validPositions.add(targetedPosition);
                }

                offsets.forEach(offset -> {
                    final Vec3i newTarget = new Vec3i(
                      targetedPosition.getX() + offset.getX(),
                      targetedPosition.getY() + offset.getY(),
                      targetedPosition.getZ() + offset.getZ()
                    );
                    if (newTarget.getX() >= 0 && newTarget.getX() < StateEntrySize.current().getBitsPerBlockSide() &&
                          newTarget.getY() >= 0 && newTarget.getY() < StateEntrySize.current().getBitsPerBlockSide() &&
                          newTarget.getZ() >= 0 && newTarget.getZ() < StateEntrySize.current().getBitsPerBlockSide())
                    {

                        final Vec3i relevantNewTargetAxisVector =
                          new Vec3i(
                            newTarget.getX() * Math.abs(blockHitResult.getDirection().getNormal().getX()),
                            newTarget.getY() * Math.abs(blockHitResult.getDirection().getNormal().getY()),
                            newTarget.getZ() * Math.abs(blockHitResult.getDirection().getNormal().getZ())
                          );

                        final int targetedDepth = Math.abs(
                          relevantSelectedAxisVector.get(blockHitResult.getDirection().getAxis()) -
                            relevantNewTargetAxisVector.get(blockHitResult.getDirection().getAxis())
                        );

                        if (targetedDepth <= depth - 1)
                        {
                            //Valid offset found
                            if (!processed.contains(newTarget) && !toProcess.contains(newTarget))
                            {
                                toProcess.addLast(newTarget);
                            }
                        }
                    }
                    else
                    {
                        processed.add(newTarget);
                    }
                });
            }
        }

        context.include(hitPos, Vec3.ZERO);
        context.include(hitPos, new Vec3(0.9999, 0.9999, 0.9999));
        context.setStateFilter(accessor -> new SelectedBitStateFilter(filterOffsetProducer.apply(blockHitResult.getDirection()), validPositions));

        context.setMetadata(ModMetadataKeys.VALID_POSITIONS.get(), validPositions);
        context.setMetadata(ModMetadataKeys.TARGETED_SIDE.get(), blockHitResult.getDirection());
        context.setMetadata(ModMetadataKeys.TARGETED_BLOCK.get(), hitPos);

        return Optional.empty();
    }

    @Override
    public @NotNull ResourceLocation getIcon()
    {
        return iconName;
    }

    @Override
    public @NotNull Optional<IToolModeGroup> getGroup()
    {
        return Optional.of(ModChiselModeGroups.CONNECTED_MATERIAL);
    }

    @Override
    public Component getDisplayName()
    {
        return displayName;
    }

    @Override
    public Component getMultiLineDisplayName()
    {
        return multiLineDisplayName;
    }

    @Override
    public VoxelShape getShape(final IChiselingContext context)
    {
        if (context.getMutator().isEmpty())
            return Shapes.empty();

        return VoxelShapeManager.getInstance().get(context.getMutator().get(), CollisionType.ALL);
    }

    private static final class SelectedBitStateFilter implements Predicate<IStateEntryInfo>
    {

        private final Vec3i      offset;
        private final Set<Vec3i> validPositions;

        public SelectedBitStateFilter(final Vec3i offset, final Set<Vec3i> validPositions)
        {
            this.offset = offset;
            this.validPositions = validPositions;
        }

        @Override
        public boolean test(final IStateEntryInfo iStateEntryInfo)
        {
            final Vec3i position = VectorUtils.toInteger(
              iStateEntryInfo.getStartPoint().x() * StateEntrySize.current().getBitsPerBlockSide(),
              iStateEntryInfo.getStartPoint().y() * StateEntrySize.current().getBitsPerBlockSide(),
              iStateEntryInfo.getStartPoint().z() * StateEntrySize.current().getBitsPerBlockSide()
            );

            return validPositions.contains(
              position
            );
        }

        @Override
        public int hashCode()
        {
            int result = offset.hashCode();
            result = 31 * result + validPositions.hashCode();
            return result;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof final SelectedBitStateFilter that))
            {
                return false;
            }

            if (!offset.equals(that.offset))
            {
                return false;
            }
            return validPositions.equals(that.validPositions);
        }

        @Override
        public String toString()
        {
            return "SelectedBitStateFilter{" +
                     "offset=" + offset +
                     ", validPositions=" + validPositions +
                     '}';
        }
    }
}
