package mod.bitsnblocks;

import mod.bitsnblocks.apiipml.ChiselsAndBitsAPI;
import mod.bitsnblocks.api.IChiselsAndBitsAPI;
import mod.bitsnblocks.api.chiseling.eligibility.IEligibilityOptions;
import mod.bitsnblocks.api.config.IChiselsAndBitsConfiguration;
import mod.bitsnblocks.api.inventory.bit.IAdaptingBitInventoryManager;
import mod.bitsnblocks.api.plugin.IChiselsAndBitsPlugin;
import mod.bitsnblocks.api.plugin.IPluginDiscoverer;
import mod.bitsnblocks.api.util.constants.Constants;
import mod.bitsnblocks.config.ChiselsAndBitsConfiguration;
import mod.bitsnblocks.network.NetworkChannel;
import mod.bitsnblocks.plugin.PluginManger;
import mod.bitsnblocks.registrars.*;

public class BitsNBlocks
{
	private static BitsNBlocks instance;
	private final  NetworkChannel     networkChannel = new NetworkChannel(Constants.MOD_ID);

	private final IChiselsAndBitsConfiguration configuration;

	public BitsNBlocks(
            IEligibilityOptions eligibilityOptions,
            IAdaptingBitInventoryManager adaptingBitInventoryManager,
            IPluginDiscoverer pluginDiscoverer)
	{
	    instance = this;

        this.configuration = new ChiselsAndBitsConfiguration();
        IChiselsAndBitsAPI.Holder.setInstance(new ChiselsAndBitsAPI(
                eligibilityOptions,
                adaptingBitInventoryManager,
                pluginDiscoverer
        ));

        ModBlockEntityTypes.onModConstruction();
        ModBlocks.onModConstruction();
        ModChiselModeGroups.onModConstruction();
        ModChiselModes.onModConstruction();
        ModContainerTypes.onModConstruction();
        ModCreativeTabs.onModConstruction();
        ModItems.onModConstruction();
        ModMetadataKeys.onModConstruction();
        ModModelProperties.onModConstruction();
        ModModificationOperation.onModConstruction();
        ModModificationOperationGroups.onModConstruction();
        ModPatternPlacementTypes.onModConstruction();
        ModRecipeSerializers.onModConstruction();
        ModTags.onModConstruction();
        ModRecipeTypes.onModConstruction();
        ModChangeTypes.onModConstruction();
        ModMultiStateSnapshotTypes.onModConstruction();
        ModDataComponentTypes.onModConstruction();

        ModEventHandler.onModConstruction();

        networkChannel.registerCommonMessages();

        PluginManger.getInstance().detect();
        PluginManger.getInstance().run(IChiselsAndBitsPlugin::onConstruction);
	}

	public static BitsNBlocks getInstance()
	{
		return instance;
	}

    public IChiselsAndBitsConfiguration getConfiguration()
    {
        return configuration;
    }

    public NetworkChannel getNetworkChannel() {
	    return networkChannel;
    }

    public void onInitialize() {
        PluginManger.getInstance().run(IChiselsAndBitsPlugin::onInitialize);
    }
}
