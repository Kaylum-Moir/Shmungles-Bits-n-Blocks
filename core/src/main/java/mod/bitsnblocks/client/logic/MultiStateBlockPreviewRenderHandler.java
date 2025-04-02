package mod.bitsnblocks.client.logic;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.bitsnblocks.api.client.render.preview.placement.PlacementPreviewRenderMode;
import mod.bitsnblocks.api.config.IClientConfiguration;
import mod.bitsnblocks.api.item.multistate.IMultiStateItemStack;
import mod.bitsnblocks.api.item.pattern.IPatternItem;
import mod.bitsnblocks.api.item.wireframe.IWireframeProvidingItem;
import mod.bitsnblocks.api.multistate.StateEntrySize;
import mod.bitsnblocks.api.placement.IPlacementPreviewProvidingItem;
import mod.bitsnblocks.api.placement.PlacementResult;
import mod.bitsnblocks.client.render.ChiseledBlockGhostRenderer;
import mod.bitsnblocks.client.render.ChiseledBlockWireframeRenderer;
import mod.bitsnblocks.utils.ItemStackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector4f;

import static mod.bitsnblocks.block.entities.ChiseledBlockEntity.ONE_THOUSANDS;

public class MultiStateBlockPreviewRenderHandler
{

    public static void renderMultiStateBlockPreview(final PoseStack poseStack)
    {
        final HitResult rayTraceResult = Minecraft.getInstance().hitResult;
        if (!(rayTraceResult instanceof final BlockHitResult blockRayTraceResult) || blockRayTraceResult.getType() == HitResult.Type.MISS)
            return;

        final Player playerEntity = Minecraft.getInstance().player;
        if (playerEntity == null || playerEntity.isSpectator())
            return;

        final ItemStack heldStack = ItemStackUtils.getMultiStateItemStackFromPlayer(playerEntity);
        if (!(heldStack.getItem() instanceof IWireframeProvidingItem wireframeItem))
            return;

        Vec3 targetedRenderPos = wireframeItem.getTargetedPosition(heldStack, playerEntity, blockRayTraceResult).add(ONE_THOUSANDS, ONE_THOUSANDS, ONE_THOUSANDS);
        // Snap to bit grid
        final float bitSize = StateEntrySize.current().getSizePerBit();
        targetedRenderPos = targetedRenderPos.subtract(
                targetedRenderPos.x % bitSize + (targetedRenderPos.x < 0 ? bitSize : 0),
                targetedRenderPos.y % bitSize + (targetedRenderPos.y < 0 ? bitSize : 0),
                targetedRenderPos.z % bitSize + (targetedRenderPos.z < 0 ? bitSize : 0)
        );

        final PlacementResult placementResult;
        final boolean ignoreDepth, forceWireframe;
        if (heldStack.getItem() instanceof IPlacementPreviewProvidingItem placementPreviewItem)
        {
            placementResult = placementPreviewItem.getPlacementResult(heldStack, playerEntity, blockRayTraceResult);
            ignoreDepth = placementPreviewItem.ignoreDepthForPlacement(heldStack, placementResult);
            forceWireframe = false;
        }
        else
        {
            placementResult = PlacementResult.failure(wireframeItem.getWireFrameColor(heldStack, playerEntity, blockRayTraceResult));
            ignoreDepth = wireframeItem.ignoreDepth(heldStack);
            forceWireframe = true;
        }

        final IClientConfiguration clientConfig = IClientConfiguration.getInstance();
        final PlacementPreviewRenderMode success = clientConfig.getSuccessfulPlacementRenderMode().get();
        final PlacementPreviewRenderMode failure = clientConfig.getFailedPlacementRenderMode().get();
        if (forceWireframe
                || (placementResult.isSuccess() && success.isWireframe())
                || (!placementResult.isSuccess() && failure.isWireframe())
                || !renderGhost(poseStack, heldStack, targetedRenderPos, placementResult, success, failure, ignoreDepth))
            renderWireFrame(poseStack, playerEntity, heldStack, wireframeItem, blockRayTraceResult, targetedRenderPos, placementResult.getColor(), ignoreDepth);
    }

    private static void renderWireFrame(
            final PoseStack poseStack,
            final Player playerEntity,
            final ItemStack heldStack,
            final IWireframeProvidingItem wireframeItem,
            final BlockHitResult blockRayTraceResult,
            final Vec3 targetedRenderPos,
            final Vector4f color,
            final boolean ignoreDepth)
    {
        final VoxelShape wireFrame = wireframeItem.getWireFrame(heldStack, playerEntity, blockRayTraceResult);

        ChiseledBlockWireframeRenderer.getInstance().renderShape(
                poseStack,
                wireFrame,
                targetedRenderPos,
                color,
                ignoreDepth
        );
    }

    private static boolean renderGhost(
            final PoseStack poseStack,
            final ItemStack heldStack,
            final Vec3 targetedRenderPos,
            final PlacementResult placementResult,
            final PlacementPreviewRenderMode success,
            final PlacementPreviewRenderMode failure,
            final boolean ignoreDepth)
    {
        final ItemStack renderStack;
        if (heldStack.getItem() instanceof final IPatternItem patternItem)
        {
            final IMultiStateItemStack multiSate = patternItem.createItemStack(heldStack);
            renderStack = multiSate.toBlockStack();
            if (renderStack.isEmpty())
                return false;
        }
        else
            renderStack = heldStack;

        ChiseledBlockGhostRenderer.getInstance().renderGhost(
          poseStack,
          renderStack,
          targetedRenderPos,
          placementResult,
          success,
          failure,
          ignoreDepth
        );
        return true;
    }
}
