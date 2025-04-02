package mod.bitsnblocks.client.clipboard;

import mod.bitsnblocks.api.client.clipboard.ICreativeClipboardManager;
import mod.bitsnblocks.api.config.IClientConfiguration;
import mod.bitsnblocks.api.item.multistate.IMultiStateItemStack;
import mod.bitsnblocks.registrars.ModCreativeTabs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.HolderLookup;

public final class CreativeClipboardUtils
{

    private CreativeClipboardUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: CreativeClipboardUtils. This is a utility class");
    }

    public static void addPickedBlock(final IMultiStateItemStack multiStateItemStack, HolderLookup.Provider provider) {
        if (IClientConfiguration.getInstance().getShouldPickedBlocksBeAddedToClipboard().get()) {
            ICreativeClipboardManager.getInstance().addEntry(multiStateItemStack, provider);
        }
    }

    public static void addBrokenBlock(final IMultiStateItemStack multiStateItemStack, HolderLookup.Provider provider) {
        if (IClientConfiguration.getInstance().getShouldBrokenBlocksBeAddedToClipboard().get()) {
            ICreativeClipboardManager.getInstance().addEntry(multiStateItemStack, provider);
        }
    }

    public static void deleteHoveredClipboardEntry(final Minecraft minecraft, final CreativeModeInventoryScreen screen) {
        if (CreativeModeInventoryScreen.selectedTab != ModCreativeTabs.CLIPBOARD.get())
            return;

        if (screen.hoveredSlot == null)
            return;

        if (minecraft.level == null)
            return;

        if (screen.isCreativeSlot(screen.hoveredSlot)) {
            ICreativeClipboardManager.getInstance().removeEntry(screen.hoveredSlot.index, minecraft.level.registryAccess());
        }
    }
}
