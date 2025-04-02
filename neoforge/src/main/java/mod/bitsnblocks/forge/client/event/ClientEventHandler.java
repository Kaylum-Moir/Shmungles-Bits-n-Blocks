package mod.bitsnblocks.forge.client.event;

import mod.bitsnblocks.api.util.constants.Constants;
import mod.bitsnblocks.forge.client.block.ForgeClientChiseledBlockExtensions;
import mod.bitsnblocks.registrars.ModBlocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerBlock(new ForgeClientChiseledBlockExtensions(), ModBlocks.CHISELED_BLOCK.get());
    }
}
