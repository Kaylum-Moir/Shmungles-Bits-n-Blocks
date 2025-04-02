package mod.bitsnblocks.item;

import com.communi.suggestu.scena.core.dist.Dist;
import com.communi.suggestu.scena.core.dist.DistExecutor;
import com.google.common.collect.Lists;
import mod.bitsnblocks.BitsNBlocks;
import mod.bitsnblocks.api.item.click.ClickProcessingState;
import mod.bitsnblocks.api.item.measuring.IMeasuringTapeItem;
import mod.bitsnblocks.api.measuring.MeasuringMode;
import mod.bitsnblocks.api.util.BlockHitResultUtils;
import mod.bitsnblocks.api.util.HelpTextUtils;
import mod.bitsnblocks.api.util.LocalStrings;
import mod.bitsnblocks.api.util.RayTracingUtils;
import mod.bitsnblocks.keys.KeyBindingManager;
import mod.bitsnblocks.measures.MeasuringManager;
import mod.bitsnblocks.network.packets.MeasurementsResetPacket;
import mod.bitsnblocks.registrars.ModDataComponentTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class MeasuringTapeItem extends Item implements IMeasuringTapeItem
{
    public MeasuringTapeItem(final Properties properties)
    {
        super(properties);
    }

    @NotNull
    @Override
    public MeasuringMode getMode(final ItemStack stack)
    {
        return stack.getOrDefault(ModDataComponentTypes.MEASURING_MODE.get(), MeasuringMode.WHITE_BIT);
    }

    @Override
    public void setMode(final ItemStack stack, final MeasuringMode mode)
    {
        stack.set(ModDataComponentTypes.MEASURING_MODE.get(), mode);
    }

    @Override
    public @NotNull Collection<MeasuringMode> getPossibleModes()
    {
        return Lists.newArrayList(MeasuringMode.values());
    }

    @Override
    public ClickProcessingState handleRightClickProcessing(
      final Player playerEntity, final InteractionHand hand, final BlockPos position, final Direction face, final ClickProcessingState currentState)
    {
        ClickProcessingState result = ClickProcessingState.DEFAULT;
        final ItemStack stack = playerEntity.getItemInHand(hand);
        if (stack.getItem() == this)
        {
            //We only check for this on the client not on the server side, the client sends the packet to the server and then performs the code.
            if (DistExecutor.unsafeRunForDist(
              () -> () -> { return KeyBindingManager.getInstance().isResetMeasuringTapeKeyPressed(); },
              () -> () -> { return false; })
            )
            {
                clear(stack);
                BitsNBlocks.getInstance().getNetworkChannel().sendToServer(new MeasurementsResetPacket());
            }
            else
            {
                final HitResult rayTraceResult = RayTracingUtils.rayTracePlayer(playerEntity);
                if (rayTraceResult.getType() == HitResult.Type.BLOCK && rayTraceResult instanceof final BlockHitResult blockRayTraceResult)
                {
                    final Optional<Vec3> startPointHandler = getStart(stack);
                    if (startPointHandler.isEmpty())
                    {
                        setStart(stack, getMode(stack).getType().adaptClickedPosition(blockRayTraceResult));
                    }
                    else
                    {
                        final Vec3 startPoint = startPointHandler.get();
                        final Vec3 hitVector = BlockHitResultUtils.getCenterOfHitObject(blockRayTraceResult, getMode(stack).getType().getResolution() );
                        MeasuringManager.getInstance().createAndSend(
                          startPoint,
                          getMode(stack).getType().adaptClickedPosition(blockRayTraceResult),
                          blockRayTraceResult.getDirection(),
                          getMode(stack)
                        );
                        clear(stack);
                    }
                    result = ClickProcessingState.ALLOW;
                }
            }
        }

        return result;
    }

    @Override
    public void inventoryTick(final @NotNull ItemStack stack, final @NotNull Level worldIn, final @NotNull Entity entityIn, final int itemSlot, final boolean isSelected)
    {
        if (!worldIn.isClientSide())
            return;

        if (!(entityIn instanceof final Player playerEntity))
            return;

        if (stack.getItem() != this)
            return;

        final Optional<Vec3> startPointHandler = getStart(stack);
        if (startPointHandler.isEmpty()) {
            return;
        }

        if (KeyBindingManager.getInstance().isResetMeasuringTapeKeyPressed()) {
            clear(stack);
            BitsNBlocks.getInstance().getNetworkChannel().sendToServer(new MeasurementsResetPacket());
            return;
        }

        final HitResult rayTraceResult = RayTracingUtils.rayTracePlayer(playerEntity);
        if (rayTraceResult.getType() != HitResult.Type.BLOCK || !(rayTraceResult instanceof final BlockHitResult blockRayTraceResult))
        {
            return;
        }
        final Vec3 hitVector = blockRayTraceResult.getLocation();

        final Vec3 startPoint = startPointHandler.get();

        MeasuringManager.getInstance().createAndSend(
          startPoint,
          getMode(stack).getType().adaptClickedPosition(blockRayTraceResult),
          blockRayTraceResult.getDirection(),
          getMode(stack)
        );
    }

    @Override
    public @NotNull Optional<Vec3> getStart(final @NotNull ItemStack stack)
    {
        return Optional.ofNullable(stack.get(ModDataComponentTypes.START.get()));
    }

    @Override
    public void setStart(final @NotNull ItemStack stack, final @NotNull Vec3 start)
    {
        stack.set(ModDataComponentTypes.START.get(), start);
    }

    @Override
    public void clear(final @NotNull ItemStack stack)
    {
        stack.remove(ModDataComponentTypes.START.get());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            if (KeyBindingManager.getInstance().hasBeenInitialized()) {
                HelpTextUtils.build(
                  LocalStrings.HelpTapeMeasure, tooltip,
                  Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage(),
                  Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage(),
                  KeyBindingManager.getInstance().getResetMeasuringTapeKeyBinding().getTranslatedKeyMessage(),
                  KeyBindingManager.getInstance().getOpenToolMenuKeybinding().getTranslatedKeyMessage()
                );
            }
          });
    }
}
