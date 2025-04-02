package mod.bitsnblocks.client.chiseling.preview.render;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.bitsnblocks.api.axissize.CollisionType;
import mod.bitsnblocks.api.chiseling.ChiselingOperation;
import mod.bitsnblocks.api.chiseling.IChiselingContext;
import mod.bitsnblocks.api.client.render.preview.chiseling.IChiselContextPreviewRenderer;
import mod.bitsnblocks.api.config.IClientConfiguration;
import mod.bitsnblocks.api.util.constants.Constants;
import mod.bitsnblocks.client.render.ModRenderTypes;
import mod.bitsnblocks.voxelshape.VoxelShapeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class ConfigurableColoredVoxelShapeChiselContextPreviewRenderer implements IChiselContextPreviewRenderer
{
    static ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "default");

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void renderExistingContextsBoundingBox(
      final PoseStack poseStack, final IChiselingContext currentContextSnapshot)
    {
        if (currentContextSnapshot.getMutator().isEmpty())
            return;

        Vec3 Vec3 = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double xView = Vec3.x();
        double yView = Vec3.y();
        double zView = Vec3.z();

        final BlockPos inWorldStartPos = currentContextSnapshot.getMutator().get().getInWorldStartBlockPoint();
        final VoxelShape boundingShape = VoxelShapeManager.getInstance()
          .get(currentContextSnapshot.getMutator().get(),
            currentContextSnapshot.getModeOfOperandus().isChiseling() ? CollisionType.NONE_AIR : CollisionType.ALL, //TODO: Handle the sphere shape adapter somehow...
            false);

        final VoxelShape modeShape = currentContextSnapshot.getMode().getShape(currentContextSnapshot);
        final VoxelShape renderedShape = modeShape; // Shapes.joinUnoptimized(boundingShape, modeShape, BooleanOp.AND);

        final List<? extends Float> color = currentContextSnapshot.getModeOfOperandus() == ChiselingOperation.CHISELING ?
                                 IClientConfiguration.getInstance().getPreviewChiselingColor().get() :
                                 IClientConfiguration.getInstance().getPreviewPlacementColor().get();

        final List<? extends Float> mutatorColor = currentContextSnapshot.getModeOfOperandus() == ChiselingOperation.CHISELING ?
                                              IClientConfiguration.getInstance().getMutatorPreviewChiselingColor().get() :
                                              IClientConfiguration.getInstance().getMutatorPreviewPlacementColor().get();

        LevelRenderer.renderShape(
          poseStack,
          Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(ModRenderTypes.CHISEL_PREVIEW_INSIDE_BLOCKS.get()),
          renderedShape,
          inWorldStartPos.getX() - xView, inWorldStartPos.getY() - yView, inWorldStartPos.getZ() - zView,
          getColorValue(color, 0, 0f) * 0.3f,
          getColorValue(color, 1, 0f) * 0.3f,
          getColorValue(color, 2, 0f) * 0.3f,
          getColorValue(color, 3, 1f) * 0.3f
        );
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(ModRenderTypes.CHISEL_PREVIEW_INSIDE_BLOCKS.get());

        LevelRenderer.renderShape(
                poseStack,
                Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(ModRenderTypes.CHISEL_PREVIEW_OUTSIDE_BLOCKS.get()),
                renderedShape,
                inWorldStartPos.getX() - xView, inWorldStartPos.getY() - yView, inWorldStartPos.getZ() - zView,
                getColorValue(color, 0, 0f),
                getColorValue(color, 1, 0f),
                getColorValue(color, 2, 0f),
                getColorValue(color, 3, 1f)
        );
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(ModRenderTypes.CHISEL_PREVIEW_OUTSIDE_BLOCKS.get());

        if (IClientConfiguration.getInstance().getMutatorPreviewDebug().get()) {
            LevelRenderer.renderShape(
              poseStack,
              Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(ModRenderTypes.MEASUREMENT_LINES.get()),
              boundingShape,
              inWorldStartPos.getX() - xView, inWorldStartPos.getY() - yView, inWorldStartPos.getZ() - zView,
              getColorValue(mutatorColor, 0, 0f),
              getColorValue(mutatorColor, 1, 0f),
              getColorValue(mutatorColor, 2, 0f),
              getColorValue(mutatorColor, 3, 1f)
            );
            Minecraft.getInstance().renderBuffers().bufferSource().endBatch(ModRenderTypes.MEASUREMENT_LINES.get());
        }
    }

    private static float getColorValue(final List<? extends Float> values, final int index, final float defaultValue) {
        if (values.size() <= index || index < 0)
            return defaultValue;

        final Number value = values.get(index);
        if (0 <= value.floatValue() && value.floatValue() <= 1f)
            return value.floatValue();

        return defaultValue;
    }
}
