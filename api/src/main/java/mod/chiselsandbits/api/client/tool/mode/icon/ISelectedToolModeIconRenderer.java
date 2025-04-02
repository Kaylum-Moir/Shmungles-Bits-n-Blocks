package mod.chiselsandbits.api.client.tool.mode.icon;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Used to render the icons on the hot bar for the selected tool mode.
 */
public interface ISelectedToolModeIconRenderer
{
    /**
     * The id of the renderer.
     * Used to give the player a selection option for the preview renderer.
     *
     * @return The id of the preview renderer.
     */
    ResourceLocation getId();

    /**
     * Invoked to render the stacks tool mode icon in the given itemstack.
     * This is invoked already translated to the top left pixel of the slot in question.
     *
     * @param guiGraphics The matrix stack.
     * @param stack       The stack.
     */
    void render(GuiGraphics guiGraphics, ItemStack stack);
}
