package mod.bitsnblocks.api.item.chiseled;

import mod.bitsnblocks.api.axissize.CollisionType;
import mod.bitsnblocks.api.item.multistate.IMultiStateItem;
import mod.bitsnblocks.api.placement.IPlacementPreviewProvidingItem;
import mod.bitsnblocks.api.item.withmode.IWithModeItem;
import mod.bitsnblocks.api.modification.operation.IModificationOperation;
import mod.bitsnblocks.api.voxelshape.IVoxelShapeManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector4f;

/**
 * Represents items which represent a broken chiseled block.
 */
public interface IChiseledBlockItem extends IMultiStateItem, IPlacementPreviewProvidingItem, IWithModeItem<IModificationOperation>
{
    @Override
    default VoxelShape getWireFrame(
      final ItemStack stack, final Player player, final BlockHitResult rayTraceResult)
    {
        return IVoxelShapeManager.getInstance().get(
          createItemStack(stack),
          CollisionType.NONE_AIR
        );
    }

    @Override
    default Vector4f getWireFrameColor(ItemStack heldStack, Player playerEntity, BlockHitResult blockRayTraceResult)
    {
        return getPlacementResult(heldStack, playerEntity, blockRayTraceResult).getColor();
    }

    @Override
    default Vec3 getTargetedPosition(ItemStack heldStack, Player playerEntity, BlockHitResult blockRayTraceResult)
    {
        return !playerEntity.isShiftKeyDown() ?
                 Vec3.atLowerCornerOf(blockRayTraceResult.getBlockPos().offset(blockRayTraceResult.getDirection().getNormal()))
                 :
                   blockRayTraceResult.getLocation();
    }

    @Override
    default boolean overridesOccupiedBits(ItemStack heldStack)
    {
        return false;
    }
}
