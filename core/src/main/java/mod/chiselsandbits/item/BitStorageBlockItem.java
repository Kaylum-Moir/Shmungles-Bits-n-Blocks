package mod.chiselsandbits.item;

import mod.chiselsandbits.api.blockinformation.BlockInformation;
import mod.chiselsandbits.api.util.HelpTextUtils;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import mod.chiselsandbits.block.BitStorageBlock;
import mod.chiselsandbits.block.entities.BitStorageBlockEntity;
import mod.chiselsandbits.registrars.ModDataComponentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BitStorageBlockItem extends BlockItem
{

    public BitStorageBlockItem(
      final Block block , Properties builder)
    {
        super( block, builder );
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        if (stack.has(ModDataComponentTypes.BLOCK_INFORMATION.get()) && stack.has(ModDataComponentTypes.COUNT.get())) {
            final BlockInformation containedState = stack.get(ModDataComponentTypes.BLOCK_INFORMATION.get());
            final int count = stack.get(ModDataComponentTypes.COUNT.get());

            HelpTextUtils.build(LocalStrings.HelpBitStorageFilled, tooltip, containedState.blockState().getBlock().getName(), count);
        }
        else
        {
            HelpTextUtils.build(LocalStrings.HelpBitStorageEmpty, tooltip);
        }
    }

    @Override
    protected boolean updateCustomBlockEntityTag(
      final @NotNull BlockPos pos, final @NotNull Level worldIn, @Nullable final Player player, final @NotNull ItemStack stack, final @NotNull BlockState state)
    {
        super.updateCustomBlockEntityTag(pos, worldIn, player, stack, state);
        if (worldIn.isClientSide)
            return false;

        final BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        if (!(blockEntity instanceof final BitStorageBlockEntity bitStorage))
            return false;

        BitStorageBlock.updateEntityFromStack(stack, bitStorage);
        return true;
    }
}