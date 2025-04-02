package mod.bitsnblocks.client.chiseling.preview.render;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.bitsnblocks.api.chiseling.IChiselingContext;
import mod.bitsnblocks.api.client.render.preview.chiseling.IChiselContextPreviewRenderer;
import mod.bitsnblocks.api.util.constants.Constants;
import net.minecraft.resources.ResourceLocation;

public class NoopChiselContextPreviewRenderer implements IChiselContextPreviewRenderer
{
    static ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "noop");

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void renderExistingContextsBoundingBox(
      final PoseStack matrixStack, final IChiselingContext currentContextSnapshot)
    {
        //Some people do not want this, so we have this.
    }
}
