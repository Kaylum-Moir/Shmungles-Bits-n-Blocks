package mod.bitsnblocks.client.logic;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.bitsnblocks.client.render.MeasurementRenderer;
import net.minecraft.client.Minecraft;

public class MeasurementsRenderHandler
{

    public static void renderMeasurements(final PoseStack poseStack)
    {
        if (Minecraft.getInstance().player != null && !Minecraft.getInstance().player.isSpectator())
            MeasurementRenderer.getInstance().renderMeasurements(poseStack);
    }

}
