package mod.bitsnblocks.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import org.joml.Vector3f;

public final class TransformationUtils {

    private TransformationUtils() {
        throw new IllegalStateException("Tried to initialize TransformationUtils. But this is a utility class!");
    }

    public static void push(PoseStack stack, final Transformation transformation, final boolean requiresStackPush)
    {
        if (requiresStackPush)
        {
            stack.pushPose();
        }

        Vector3f trans = transformation.getTranslation();
        stack.translate(trans.x(), trans.y(), trans.z());

        stack.mulPose(transformation.getLeftRotation());

        Vector3f scale = transformation.getScale();
        stack.scale(scale.x(), scale.y(), scale.z());

        stack.mulPose(transformation.getRightRotation());
    }

}
