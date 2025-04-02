package mod.bitsnblocks.forge;

import com.communi.suggestu.scena.core.dist.Dist;
import com.communi.suggestu.scena.core.dist.DistExecutor;
import com.communi.suggestu.scena.core.init.PlatformInitializationHandler;
import com.mojang.logging.LogUtils;
import mod.bitsnblocks.BitsNBlocks;
import mod.bitsnblocks.api.util.constants.Constants;
import mod.bitsnblocks.client.ChiselsAndBitsClient;
import mod.bitsnblocks.forge.platform.ForgeAdaptingBitInventoryManager;
import mod.bitsnblocks.forge.platform.ForgePluginDiscoverer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.slf4j.Logger;

import java.util.function.Consumer;

@Mod(Constants.MOD_ID)
public class Forge
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private BitsNBlocks bitsNBlocks;

    private void setChiselsAndBits(final BitsNBlocks bitsNBlocks)
    {
        this.bitsNBlocks = bitsNBlocks;
    }

    public Forge(IEventBus modBus)
	{
        LOGGER.info("Initialized Chisels&Bits - Forge");
        //We need to use the platform initialization manager to handle the init in the constructor since this runs in parallel with scena itself.
        PlatformInitializationHandler.getInstance().onInit(platform -> {
            setChiselsAndBits(new BitsNBlocks(
                    block -> block == IBlockExtension.class,
                    ForgeAdaptingBitInventoryManager.getInstance(),
                    ForgePluginDiscoverer.getInstance()
            ));

            DistExecutor.runWhenOn(Dist.CLIENT, () -> Client::init);
        });
        
        modBus.addListener((Consumer<FMLCommonSetupEvent>) event -> bitsNBlocks.onInitialize());
	}

    public static final class Client {

        private static ChiselsAndBitsClient chiselsAndBitsClient;

        public static void setChiselsAndBitsClient(final ChiselsAndBitsClient chiselsAndBitsClient)
        {
            Client.chiselsAndBitsClient = chiselsAndBitsClient;
        }

        public static void init() {
            LOGGER.info("Initialized Chisels&Bits-Forge client");
            //We need to use the platform initialization manager to handle the init in the constructor since this runs in parallel with scena itself.
            PlatformInitializationHandler.getInstance().onInit(platform -> {
                setChiselsAndBitsClient(new ChiselsAndBitsClient());
            });
        }
    }
}
