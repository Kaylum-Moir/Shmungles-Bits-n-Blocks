package mod.bitsnblocks.client.render;

import com.communi.suggestu.scena.core.util.TransformationUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import mod.bitsnblocks.api.measuring.IMeasurement;
import mod.bitsnblocks.api.measuring.MeasuringMode;
import mod.bitsnblocks.api.measuring.MeasuringType;
import mod.bitsnblocks.api.util.VectorUtils;
import mod.bitsnblocks.api.util.constants.Constants;
import mod.bitsnblocks.measures.MeasuringManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.PlayerTeam;
import org.joml.Vector3f;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.UUID;

public final class MeasurementRenderer
{
    private static final MeasurementRenderer INSTANCE = new MeasurementRenderer();

    private MeasurementRenderer()
    {
    }

    public static MeasurementRenderer getInstance()
    {
        return INSTANCE;
    }

    public void renderMeasurements(final PoseStack poseStack)
    {
        if (Minecraft.getInstance().level == null)
        {
            return;
        }

        final Collection<? extends IMeasurement> measurements = MeasuringManager.getInstance().getInWorld(Minecraft.getInstance().level);


        Vec3 vector3d = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double xView = vector3d.x();
        double yView = vector3d.y();
        double zView = vector3d.z();

        measurements.forEach(measurement -> {
            final Vec3 startPos = measurement.getFrom();

            final AABB measurementBB = new AABB(
              Vec3.ZERO, measurement.getSize().add(0.0001d, 0.0001d, 0.0001d)
            );
            final VoxelShape boundingShape = Shapes.create(measurementBB);

            if (measurement.getMode().getGroup().map(g -> g != MeasuringType.DISTANCE).orElse(false))
            {
                LevelRenderer.renderShape(
                  poseStack,
                  Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(ModRenderTypes.MEASUREMENT_LINES.get()),
                  boundingShape,
                  startPos.x() - xView,
                  startPos.y() - yView,
                  startPos.z() - zView,
                  (float) measurement.getMode().getColorVector().x(),
                  (float) measurement.getMode().getColorVector().y(),
                  (float) measurement.getMode().getColorVector().z(),
                  (float) measurement.getMode().getAlphaChannel()
                );

                final Vec3 lengths = VectorUtils.absolute(measurement.getTo().subtract(measurement.getFrom()));
                final Vec3 centerPos = measurement.getFrom().add(measurement.getTo()).multiply(0.5, 0.5, 0.5);

                if (lengths.y() > 1/16d)
                    renderMeasurementSize(poseStack, measurement, lengths.y(), new Vec3(measurement.getFrom().x(), centerPos.y(), measurement.getFrom().z()));
                if (lengths.x() > 1/16d)
                    renderMeasurementSize(poseStack, measurement, lengths.x(), new Vec3(centerPos.x(), measurement.getFrom().y(), measurement.getFrom().z()));
                if (lengths.z() > 1/16d)
                    renderMeasurementSize(poseStack, measurement, lengths.z(), new Vec3(measurement.getFrom().x(), measurement.getFrom().y(), centerPos.z()));
            }
            else if (measurement.getMode().getGroup().map(g -> g == MeasuringType.DISTANCE).orElse(false))
            {
                final VertexConsumer bufferIn = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(ModRenderTypes.MEASUREMENT_LINES.get());
                bufferIn.addVertex(poseStack.last().pose(),
                  (float) (measurement.getFrom().x() - xView),
                  (float) (measurement.getFrom().y() - yView),
                  (float) (measurement.getFrom().z() - zView))
                  .setColor(
                    (float) measurement.getMode().getColorVector().x(),
                    (float) measurement.getMode().getColorVector().y(),
                    (float) measurement.getMode().getColorVector().z(),
                    (float) measurement.getMode().getAlphaChannel()
                  )
                  .setNormal(poseStack.last(), 0, 1, 0);

                bufferIn.addVertex(poseStack.last().pose(),
                  (float) (measurement.getTo().x() - xView),
                  (float) (measurement.getTo().y() - yView),
                  (float) (measurement.getTo().z() - zView))
                  .setColor(
                    (float) measurement.getMode().getColorVector().x(),
                    (float) measurement.getMode().getColorVector().y(),
                    (float) measurement.getMode().getColorVector().z(),
                    (float) measurement.getMode().getAlphaChannel()
                  )
                  .setNormal(poseStack.last(), 0, 1, 0);

                final Vec3 lengths = VectorUtils.absolute(measurement.getTo().subtract(measurement.getFrom()));
                final double totalLength = lengths.length();
                final Vec3 centerPos = measurement.getFrom().add(measurement.getTo()).multiply(0.5, 0.5, 0.5);

                if (totalLength > 1/16d)
                    renderMeasurementSize(poseStack, measurement, totalLength, centerPos);
            }

            Minecraft.getInstance().renderBuffers().bufferSource().endBatch(ModRenderTypes.MEASUREMENT_LINES.get());

        });
    }

    private void renderMeasurementSize(
      final PoseStack matrixStack,
      final IMeasurement measurement,
      final double length,
      final Vec3 position
    )
    {
        final double letterSize = 5.0;
        final double zScale = 0.001;

        final Font fontRenderer = Minecraft.getInstance().font;
        final Component size = formatLength(measurement.getMode(), length);
        final Component owner = getOwnerName(measurement.getOwner());

        final float scale = getScale(length);

        Vec3 vector3d = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double xView = vector3d.x();
        double yView = vector3d.y();
        double zView = vector3d.z();

        matrixStack.pushPose();
        matrixStack.translate(position.x() - xView, position.y() + scale * letterSize - yView, position.z() - zView);
        performBillboardRotations(matrixStack);
        matrixStack.scale(scale, -scale, (float) zScale);
        matrixStack.translate(-fontRenderer.width(size) * 0.5, 0, 0);
        RenderSystem.disableDepthTest();
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        fontRenderer.drawInBatch(size.getString(), 0, 0, measurement.getMode().getColor().getTextColor(), false, matrixStack.last().pose(), buffer, Font.DisplayMode.SEE_THROUGH, 0, 15728880);
        matrixStack.translate(-fontRenderer.width(owner) * 0.5, -fontRenderer.lineHeight, 0);
        fontRenderer.drawInBatch(owner.getString(), 0, 0, measurement.getMode().getColor().getTextColor(), false, matrixStack.last().pose(), buffer, Font.DisplayMode.SEE_THROUGH, 0, 15728880);
        buffer.endBatch();
        RenderSystem.enableDepthTest();
        matrixStack.popPose();
    }

    private Component formatLength(
      final MeasuringMode mode,
      final double length
    )
    {
        final MeasuringType type = mode.getType();

        if (type == MeasuringType.DISTANCE)
        {
            return Component.literal(new DecimalFormat("#.0#").format(length));
        }
        else if (type == MeasuringType.BLOCK)
        {
            return Component.translatable(Constants.MOD_ID + ".measurements.lengths.block", new DecimalFormat("#").format(Math.floor(length + 1)));
        }

        return Component.translatable(Constants.MOD_ID + ".measurements.lengths.bit", new DecimalFormat("#").format(Math.floor((length) * 16)));
    }

    private float getScale(
      final double maxLen)
    {
        final double maxFontSize = 0.04;
        final double minFontSize = 0.004;

        final double delta = Math.min(1.0, maxLen / 4.0);
        double scale = maxFontSize * delta + minFontSize * (1.0 - delta);

        if (maxLen < 0.25)
        {
            scale = minFontSize;
        }

        return (float) Math.min(maxFontSize, scale);
    }

    private void performBillboardRotations(
      final PoseStack matrixStack)
    {
        final Entity view = Minecraft.getInstance().cameraEntity != null ? Minecraft.getInstance().cameraEntity : Minecraft.getInstance().player;
        if (view != null)
        {
            final float yaw = view.yRotO + (view.getYRot() - view.yRotO) * Minecraft.getInstance().gameRenderer.getMainCamera().getPartialTickTime();
            matrixStack.mulPose(TransformationUtils.quatFromXYZ(new Vector3f(0, 180-yaw, 0), true));

            final float pitch = view.xRotO + (view.getXRot() - view.xRotO) * Minecraft.getInstance().gameRenderer.getMainCamera().getPartialTickTime();
            matrixStack.mulPose(TransformationUtils.quatFromXYZ(new Vector3f(-pitch, 0, 0), true));
        }
    }

    private Component getOwnerName(final UUID id) {
        if (id == (Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : UUID.randomUUID())) {
            return Component.translatable(Constants.MOD_ID + ".measurements.owners.you");
        }

        final PlayerInfo playerInfo = Minecraft.getInstance().getConnection() != null ? Minecraft.getInstance().getConnection().getPlayerInfo(id) : null;
        if (playerInfo == null)
            return Component.translatable(Constants.MOD_ID + ".measurements.owners.unknown");

        return Component.translatable(Constants.MOD_ID + ".measurements.owners.by", playerInfo.getTabListDisplayName() != null ? this.formatPlayerDisplayName(playerInfo, playerInfo.getTabListDisplayName().copy()) : this.formatPlayerDisplayName(playerInfo, PlayerTeam.formatNameForTeam(playerInfo.getTeam(), Component.literal(playerInfo.getProfile().getName()))));
    }

    private Component formatPlayerDisplayName(PlayerInfo p_238524_1_, MutableComponent p_238524_2_) {
        return p_238524_1_.getGameMode() == GameType.SPECTATOR ? p_238524_2_.withStyle(ChatFormatting.ITALIC) : p_238524_2_;
    }
}
