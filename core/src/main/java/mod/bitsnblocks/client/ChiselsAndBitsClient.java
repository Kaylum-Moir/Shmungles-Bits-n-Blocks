package mod.bitsnblocks.client;

import mod.bitsnblocks.api.plugin.IChiselsAndBitsPlugin;
import mod.bitsnblocks.api.plugin.IPluginManager;
import mod.bitsnblocks.client.registrars.*;
import mod.bitsnblocks.keys.KeyBindingManager;

public class ChiselsAndBitsClient {

    public ChiselsAndBitsClient() {
        BlockEntityRenderers.onClientConstruction();
        BlockEntityWithoutLevelRenderers.onClientConstruction();
        ItemColors.onClientConstruction();
        BlockColors.onClientConstruction();
        ModelLoaders.onClientConstruction();
        ItemBlockRenderTypes.onClientConstruction();
        EventHandlers.onClientConstruction();
        KeyBindingManager.getInstance().onModInitialization();
        Screens.onClientConstruction();
        ItemProperties.onClientConstruction();
        GPUResources.onClientConstruction();
        ClientTooltipComponents.onClientConstruction();

        IPluginManager.getInstance().run(IChiselsAndBitsPlugin::onClientConstruction);
    }
}
