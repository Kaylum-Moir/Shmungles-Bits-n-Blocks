package mod.bitsnblocks.block;

import com.communi.suggestu.saecularia.caudices.core.block.IBlockWithWorldlyProperties;
import com.communi.suggestu.scena.core.blockstate.ILevelBasedPropertyAccessor;
import com.communi.suggestu.scena.core.dist.Dist;
import com.communi.suggestu.scena.core.dist.DistExecutor;
import com.communi.suggestu.scena.core.entity.IPlayerInventoryManager;
import mod.bitsnblocks.BitsNBlocks;
import mod.bitsnblocks.api.axissize.CollisionType;
import mod.bitsnblocks.api.block.IMultiStateBlock;
import mod.bitsnblocks.api.block.entity.IMultiStateBlockEntity;
import mod.bitsnblocks.api.blockinformation.BlockInformation;
import mod.bitsnblocks.api.change.IChangeTrackerManager;
import mod.bitsnblocks.api.chiseling.eligibility.IEligibilityManager;
import mod.bitsnblocks.api.config.IClientConfiguration;
import mod.bitsnblocks.api.config.IServerConfiguration;
import mod.bitsnblocks.api.exceptions.SpaceOccupiedException;
import mod.bitsnblocks.api.item.multistate.IMultiStateItemFactory;
import mod.bitsnblocks.api.item.multistate.IMultiStateItemStack;
import mod.bitsnblocks.api.multistate.StateEntrySize;
import mod.bitsnblocks.api.multistate.mutator.IMutableStateEntryInfo;
import mod.bitsnblocks.api.multistate.snapshot.IMultiStateSnapshot;
import mod.bitsnblocks.api.util.*;
import mod.bitsnblocks.api.variant.state.IStateVariant;
import mod.bitsnblocks.api.variant.state.IStateVariantManager;
import mod.bitsnblocks.api.voxelshape.IVoxelShapeManager;
import mod.bitsnblocks.block.entities.ChiseledBlockEntity;
import mod.bitsnblocks.client.clipboard.CreativeClipboardUtils;
import mod.bitsnblocks.network.packets.NeighborBlockUpdatedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ChiseledBlock extends Block implements IMultiStateBlock, SimpleWaterloggedBlock, IBlockWithWorldlyProperties {
    public ChiseledBlock(Properties properties) {
        super(
                properties
                        .isViewBlocking(ChiseledBlock::isViewBlocking)
                        .noOcclusion()
        );
    }

    private static boolean isViewBlocking(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return false;
    }

    @Override
    public float getFriction(final BlockState state, final LevelReader levelReader, final BlockPos pos, @Nullable final Entity entity) {
        final float frictionValue = getBlockEntity(levelReader, pos)
                .map(multiStateBlockEntity -> multiStateBlockEntity.getStatistics().getSlipperiness())
                .orElse(0f);

        return Float.isNaN(frictionValue) || frictionValue <= 0.0001f ? 0.6f : frictionValue;
    }

    @Override
    public int getLightEmission(final BlockState state, final BlockGetter blockGetter, final BlockPos pos) {
        return getBlockEntity(blockGetter, pos)
                .map(multiStateBlockEntity -> blockGetter.getMaxLightLevel() * multiStateBlockEntity.getStatistics().getLightEmissionFactor())
                .map(inertValue -> inertValue * IServerConfiguration.getInstance().getLightFactorMultiplier().get())
                .map(consumedValue -> Math.max(consumedValue, 0))
                .map(consumedValue -> Math.min(consumedValue, blockGetter.getMaxLightLevel()))
                .orElse(0d).intValue();
    }

    @Override
    public boolean canHarvestBlock(final BlockState state, final BlockGetter blockGetter, final BlockPos pos, final Player player) {
        return getBlockEntity(blockGetter, pos)
                .map(e -> {
                    final BlockInformation primaryState = e.getStatistics().getPrimaryState();

                    return ILevelBasedPropertyAccessor.getInstance().canHarvestBlock(
                            new SingleBlockBlockReader(
                                    primaryState,
                                    pos,
                                    blockGetter
                            ),
                            pos,
                            player
                    );
                })
                .orElse(true);
    }

    @Override
    public ItemStack getCloneItemStack(final BlockState state, final HitResult target, final LevelReader blockGetter, final BlockPos pos, final Player player) {
        if (!(target instanceof final BlockHitResult blockRayTraceResult)) {
            return ItemStack.EMPTY;
        }

        if (
                (!IClientConfiguration.getInstance().getInvertPickBlockBehaviour().get() && player.isShiftKeyDown()) ||
                        (IClientConfiguration.getInstance().getInvertPickBlockBehaviour().get() && !player.isShiftKeyDown())
        ) {
            return getBlockEntity(blockGetter, pos)
                    .map(e -> {
                        final IMultiStateSnapshot snapshot = e.createSnapshot();
                        final IMultiStateItemStack multiStateItemStack = snapshot.toItemStack();
                        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CreativeClipboardUtils.addPickedBlock(multiStateItemStack, Minecraft.getInstance().level.registryAccess()));

                        return multiStateItemStack.toBlockStack();
                    })
                    .orElse(ItemStack.EMPTY);
        }

        return getBlockEntity(blockGetter, pos)
                .flatMap(e -> {
                    final Vec3 hitVec = blockRayTraceResult.getLocation();
                    final BlockPos blockPos = blockRayTraceResult.getBlockPos();
                    final Vec3 accuratePos = new Vec3(
                            blockPos.getX(),
                            blockPos.getY(),
                            blockPos.getZ()
                    );
                    final Vec3 faceOffset = new Vec3(
                            blockRayTraceResult.getDirection().getOpposite().getStepX() * StateEntrySize.current().getSizePerHalfBit(),
                            blockRayTraceResult.getDirection().getOpposite().getStepY() * StateEntrySize.current().getSizePerHalfBit(),
                            blockRayTraceResult.getDirection().getOpposite().getStepZ() * StateEntrySize.current().getSizePerHalfBit()
                    );
                    final Vec3 hitDelta = hitVec.subtract(accuratePos).add(faceOffset);

                    try {
                        return e.getInAreaTarget(hitDelta);
                    } catch (IllegalArgumentException exception) {
                        //Because people do stupid stuff.
                        return Optional.empty();
                    }
                })
                .map(targetedStateEntry -> IMultiStateItemFactory.getInstance().createBlockFrom(targetedStateEntry))
                .orElseGet(() -> getBlockEntity(blockGetter, pos)
                        .map(e -> {
                            final IMultiStateSnapshot snapshot = e.createSnapshot();
                            return snapshot.toItemStack().toBlockStack();
                        })
                        .orElse(ItemStack.EMPTY));
    }

    @Override
    public BlockState rotate(final BlockState state, final LevelAccessor levelAccessor, final BlockPos pos, final Rotation rotation) {
        if (rotation == Rotation.NONE)
            return state;

        getBlockEntity(levelAccessor, pos)
                .ifPresent(e -> e.rotate(Direction.Axis.Y, 4 - rotation.ordinal()));

        return state;
    }

    @Override
    public BlockState mirror(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, Mirror mirror) {
        if (mirror == Mirror.NONE) {
            return blockState;
        }

        getBlockEntity(levelAccessor, blockPos)
                .ifPresent(e -> e.mirror(switch (mirror) {
                    case NONE -> throw new IllegalArgumentException("Invalid mirror");
                    case LEFT_RIGHT -> Direction.Axis.Z;
                    case FRONT_BACK -> Direction.Axis.X;
                }));

        return blockState;
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState blockState, SignalGetter signalGetter, BlockPos blockPos, Direction direction) {
        return getBlockEntity(signalGetter, blockPos)
                .map(multiStateBlockEntity -> multiStateBlockEntity.getStatistics().shouldCheckWeakPower())
                .orElse(false);
    }

    @Override
    public boolean shouldDisplayFluidOverlay(final BlockState state, final BlockAndTintGetter blockAndTintGetter, final BlockPos pos, final FluidState fluidState) {
        return true;
    }

    @NotNull
    private static Optional<IMultiStateBlockEntity> getBlockEntity(final BlockGetter worldIn, final BlockPos pos) {
        final BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if (!(tileEntity instanceof IMultiStateBlockEntity)) {
            return Optional.empty();
        }

        return Optional.of((IMultiStateBlockEntity) tileEntity);
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull final BlockState state, @NotNull final BlockGetter reader, @NotNull final BlockPos pos) {
        return getBlockEntity(reader, pos)
                .map(multiStateBlockEntity -> multiStateBlockEntity.getStatistics().canPropagateSkylight())
                .orElse(false);
    }

    @Override
    public boolean useShapeForLightOcclusion(final @NotNull BlockState blockState) {
        return true;
    }

    @Override
    public void playerDestroy(
            @NotNull final Level worldIn,
            @NotNull final Player player,
            @NotNull final BlockPos pos,
            @NotNull final BlockState state,
            @Nullable final BlockEntity te,
            @NotNull final ItemStack stack) {
        if (te instanceof final IMultiStateBlockEntity multiStateBlockEntity) {

            final IMultiStateSnapshot snapshot = multiStateBlockEntity.createSnapshot();
            popResource(worldIn, pos, snapshot.toItemStack().toBlockStack());
        }
    }

    @Override
    public void setPlacedBy(
            @NotNull final Level worldIn,
            @NotNull final BlockPos pos,
            @NotNull final BlockState state,
            @Nullable final LivingEntity placer,
            @NotNull final ItemStack stack) {
        getBlockEntity(worldIn, pos)
                .ifPresent(multiStateBlockEntity -> {
                    final Direction placementDirection = placer == null ? Direction.NORTH : placer.getDirection().getOpposite();
                    final int horizontalIndex = placementDirection.get2DDataValue();

                    int rotationCount = horizontalIndex - 4;
                    if (rotationCount < 0) {
                        rotationCount += 4;
                    }

                    multiStateBlockEntity.rotate(Direction.Axis.Y, rotationCount);
                    super.setPlacedBy(worldIn, pos, state, placer, stack);
                });
    }

    @Override
    public boolean hasDynamicShape() {
        return true;
    }

    @NotNull
    @Override
    public BlockInformation getPrimaryState(@NotNull final BlockGetter world, @NotNull final BlockPos pos) {
        return getBlockEntity(world, pos)
                .map(e -> e.getStatistics().getPrimaryState())
                .orElse(BlockInformation.AIR);
    }

    @Override
    public void onRemove(final @NotNull BlockState state, final @NotNull Level worldIn, final @NotNull BlockPos pos, final BlockState newState, final boolean isMoving) {
        if (newState.getBlock() instanceof ChiseledBlock) {
            return;
        }

        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public boolean canBeReplaced(@NotNull final BlockState state, final BlockPlaceContext useContext) {
        return getBlockEntity(useContext.getLevel(), useContext.getClickedPos())
                .map(multiStateBlockEntity -> multiStateBlockEntity.getStatistics().isEmptyBlock())
                .orElse(true);
    }

    @Override
    public @NotNull VoxelShape getBlockSupportShape(final @NotNull BlockState state, final @NotNull BlockGetter reader, final @NotNull BlockPos pos) {
        final VoxelShape shape = getBlockEntity(reader, pos)
                .map(multiStateBlockEntity -> IVoxelShapeManager.getInstance().get(multiStateBlockEntity, CollisionType.COLLIDEABLE_ONLY))
                .orElse(Shapes.empty());

        return shape.isEmpty() ? Shapes.block() : shape;
    }

    @Override
    public float getShadeBrightness(@NotNull final BlockState state, @NotNull final BlockGetter worldIn, @NotNull final BlockPos pos) {
        return getBlockEntity(worldIn, pos)
                .map(b -> b.getStatistics().isFullBlock())
                .map(f -> f ? 0.2f : 1f)
                .orElse(1f);
    }

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull final BlockState state, @NotNull final BlockGetter worldIn, @NotNull final BlockPos pos, @NotNull final CollisionContext context) {
        return getBlockEntity(worldIn, pos)
                .map(blockEntity -> blockEntity.getShape(CollisionType.NONE_AIR))
                .orElse(Shapes.empty());
    }

    @NotNull
    @Override
    public VoxelShape getCollisionShape(@NotNull final BlockState state, @NotNull final BlockGetter worldIn, @NotNull final BlockPos pos, @NotNull final CollisionContext context) {
        return getBlockEntity(worldIn, pos)
                .map(blockEntity -> blockEntity.getShape(CollisionType.COLLIDEABLE_ONLY))
                .orElse(Shapes.empty());
    }

    @NotNull
    @Override
    public VoxelShape getVisualShape(@NotNull final BlockState state, @NotNull final BlockGetter reader, @NotNull final BlockPos pos, @NotNull final CollisionContext context) {
        return getShape(state, reader, pos, context);
    }

    @Override
    public float getDestroyProgress(
            @NotNull final BlockState state,
            @NotNull final Player player,
            @NotNull final BlockGetter worldIn,
            @NotNull final BlockPos pos) {
        return getBlockEntity(worldIn, pos)
                .map(multiStateBlockEntity -> multiStateBlockEntity.getStatistics().getRelativeBlockHardness(player))
                .orElse(1f);
    }

    @Override
    public boolean canPlaceLiquid(final Player player, final @NotNull BlockGetter worldIn, final @NotNull BlockPos pos, final @NotNull BlockState state, final Fluid fluidIn) {
        return IEligibilityManager.getInstance().canBeChiseled(new BlockInformation(fluidIn.defaultFluidState().createLegacyBlock(), IStateVariantManager.getInstance().getStateVariant(fluidIn.defaultFluidState()))) &&
                worldIn.getBlockEntity(pos) instanceof IMultiStateBlockEntity multiStateBlockEntity && multiStateBlockEntity.isCanBeFlooded();
    }

    @Override
    public boolean placeLiquid(final @NotNull LevelAccessor worldIn, final @NotNull BlockPos pos, final @NotNull BlockState state, final @NotNull FluidState fluidStateIn) {
        final Fluid still = fluidStateIn.getType() instanceof FlowingFluid ? ((FlowingFluid) fluidStateIn.getType()).getSource() : fluidStateIn.getType();

        if (Fluids.EMPTY.isSame(still)) {
            return false;
        }

        return getBlockEntity(worldIn, pos)
                .map(entity -> {
                    try (IBatchMutation ignored = entity.batch()) {
                        entity.mutableStream().forEach(
                                stateEntry -> {
                                    if (stateEntry.getBlockInformation().isAir()) {
                                        final BlockState blockState = still.defaultFluidState().createLegacyBlock();
                                        final Optional<IStateVariant> additionalStateInfo = IStateVariantManager.getInstance()
                                                .getStateVariant(
                                                        fluidStateIn
                                                );

                                        try {
                                            stateEntry.setBlockInformation(new BlockInformation(blockState, additionalStateInfo));
                                        } catch (SpaceOccupiedException e) {
                                            //Ignore
                                        }
                                    }
                                }
                        );
                    }

                    return true;
                })
                .orElse(false);
    }


    @Override
    public @NotNull ItemStack pickupBlock(final Player player, final @NotNull LevelAccessor p_154560_, final @NotNull BlockPos p_154561_, final @NotNull BlockState p_154562_) {
        return ItemStack.EMPTY;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final @NotNull BlockPos pos, final @NotNull BlockState state) {
        return new ChiseledBlockEntity(pos, state);
    }

    @Override
    public void neighborChanged(
            final @NotNull BlockState state,
            final Level level,
            final @NotNull BlockPos position,
            final @NotNull Block block,
            final @NotNull BlockPos otherPosition,
            final boolean update) {
        if (!(level.getBlockEntity(position) instanceof ChiseledBlockEntity chiseledBlockEntity))
            return;

        if (level.isClientSide())
            chiseledBlockEntity.updateModelData();
        else {
            BitsNBlocks.getInstance().getNetworkChannel().sendToTrackingChunk(
                    new NeighborBlockUpdatedPacket(position, otherPosition),
                    level.getChunkAt(position)
            );
        }
    }

    @Override
    public Integer getBeaconColorMultiplier(final BlockState state, final LevelReader levelReader, final BlockPos pos, final BlockPos beaconPos) {
        return getBlockEntity(levelReader, pos)
                .filter(e -> e.getStatistics().getStateCounts().keySet()
                        .stream()
                        .filter(entryState -> !entryState.isAir())
                        .allMatch(entryState -> IStateVariantManager.getInstance().getBeaconColorMultiplier(entryState, levelReader, pos, beaconPos).isPresent())
                )
                .flatMap(e -> e.getStatistics().getStateCounts().entrySet()
                        .stream()
                        .filter(entryState -> !entryState.getKey().isAir())
                        .map(entryState ->
                                IStateVariantManager.getInstance().getBeaconColorMultiplier(entryState.getKey(), levelReader, pos, beaconPos)
                                        .map(color -> ArrayUtils.multiply(
                                                ColorUtils.unpack(color),
                                                entryState.getValue())))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .reduce((floats, floats2) -> {
                            if (floats.length != floats2.length)
                                return new float[0];

                            final float[] result = new float[floats.length];
                            for (int i = 0; i < floats.length; i++) {
                                result[i] = floats[i] + floats2[i];
                            }
                            return result;
                        })
                        .filter(result -> result.length > 0)
                        .flatMap(summedResult -> getBlockEntity(levelReader, pos)
                                .map(entity -> ArrayUtils.multiply(summedResult, 1f / (entity.getStatistics().getFullnessFactor() * StateEntrySize.current().getBitsPerBlock())))
                        )
                )
                .map(ColorUtils::pack)
                .orElse(null);
    }

    @Override
    public SoundType getSoundType(final BlockState state, final LevelReader levelReader, final BlockPos pos, @Nullable final Entity entity) {
        return getBlockEntity(levelReader, pos)
                .map(blockEntity -> blockEntity.getStatistics().getPrimaryState())
                .map(blockState -> ILevelBasedPropertyAccessor.getInstance().getSoundType(
                        new SingleBlockLevelReader(blockState, pos, levelReader),
                        pos,
                        entity
                ))
                .orElse(SoundType.STONE);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter blockGetter, BlockPos position, Explosion explosion) {
        return (float) (double) (getBlockEntity(blockGetter, position)
                .map(e -> e.getStatistics().getStateCounts().entrySet()
                        .stream()
                        .filter(entryState -> !entryState.getKey().isAir())
                        .mapToDouble(entryState -> ILevelBasedPropertyAccessor.getInstance().
                                getExplosionResistance(
                                        new SingleBlockBlockReader(
                                                entryState.getKey(),
                                                position,
                                                blockGetter
                                        ),
                                        position,
                                        explosion
                                ) * entryState.getValue()
                        )
                        .sum() / (e.getStatistics().getFullnessFactor() * StateEntrySize.current().getBitsPerBlock())
                ).orElse(0d));
    }

    @Override
    public @NotNull BlockState playerWillDestroy(final @NotNull Level level, final @NotNull BlockPos blockPos, final @NotNull BlockState blockState, final @NotNull Player player) {
        final BlockState superResult = super.playerWillDestroy(level, blockPos, blockState, player);
        if (level.isClientSide())
            getBlockEntity(level, blockPos)
                    .map(IMultiStateBlockEntity::createSnapshot)
                    .map(IMultiStateSnapshot::toItemStack)
                    .ifPresent(stack -> CreativeClipboardUtils.addBrokenBlock(stack, level.registryAccess()));
        return superResult;
    }

    @Override
    public boolean canBeGrass(final LevelReader levelReader, final BlockState grassState, final BlockPos grassBlockPos, final BlockState targetState, final BlockPos targetPosition) {
        return getBlockEntity(levelReader, targetPosition)
                .map(blockEntity -> blockEntity.getStatistics().canSustainGrassBelow())
                .orElse(false);
    }

    public static EquipmentSlot getSlotForHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack itemStack, @NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult blockHitResult) {
        if (itemStack.is(Items.SPONGE)) {
            return getBlockEntity(level, blockPos)
                    .map(blockEntity -> {
                        try (IBatchMutation mutation = blockEntity.batch(IChangeTrackerManager.getInstance().getChangeTracker(player))) {
                            return blockEntity.mutableStream()
                                    .filter(entry -> !entry.getBlockInformation().blockState().getFluidState().isEmpty())
                                    .peek(IMutableStateEntryInfo::clear)
                                    .count();
                        }
                    })
                    .map(count -> {
                        if (count > 0) {
                            itemStack.shrink(1);
                            IPlayerInventoryManager.getInstance().giveToPlayer(
                                    player, new ItemStack(Items.WET_SPONGE)
                            );
                        }

                        return count > 0 ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                    })
                    .orElse(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
        }

        if (itemStack.is(Items.GLOWSTONE_DUST)) {
            return getBlockEntity(level, blockPos)
                    .map(blockEntity -> {
                        if (blockEntity.isEmitsLightBasedOnFullBlock()) {
                            blockEntity.setEmitsLightBasedOnFullBlock(false);
                            return ItemInteractionResult.CONSUME;
                        }

                        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                    })
                    .orElse(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
        }

        if (itemStack.is(Items.BLACK_DYE)) {
            return getBlockEntity(level, blockPos)
                    .map(blockEntity -> {
                        if (!blockEntity.isEmitsLightBasedOnFullBlock()) {
                            blockEntity.setEmitsLightBasedOnFullBlock(true);
                            return ItemInteractionResult.CONSUME;
                        }

                        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                    })
                    .orElse(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
        }

        if (itemStack.is(Items.HONEYCOMB)) {
            return getBlockEntity(level, blockPos)
                    .map(blockEntity -> {
                        if (blockEntity.isCanBeFlooded()) {
                            blockEntity.setCanBeFlooded(false);
                            return ItemInteractionResult.CONSUME;
                        }

                        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                    }).orElse(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
        }

        if (itemStack.is(Items.SHEARS)) {
            return getBlockEntity(level, blockPos)
                    .map(blockEntity -> {
                        if (!blockEntity.isCanBeFlooded()) {
                            blockEntity.setCanBeFlooded(true);
                            if (player instanceof ServerPlayer serverPlayer) {
                                itemStack.hurtAndBreak(1, serverPlayer, getSlotForHand(hand));
                            }
                            return ItemInteractionResult.SUCCESS;
                        }

                        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                    }).orElse(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
