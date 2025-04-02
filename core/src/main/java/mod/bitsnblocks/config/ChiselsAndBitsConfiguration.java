package mod.bitsnblocks.config;

import com.communi.suggestu.scena.core.util.LanguageHandler;
import mod.bitsnblocks.api.config.IChiselsAndBitsConfiguration;
import mod.bitsnblocks.api.config.IClientConfiguration;
import mod.bitsnblocks.api.config.ICommonConfiguration;
import mod.bitsnblocks.api.config.IServerConfiguration;
import mod.bitsnblocks.api.util.constants.Constants;

public class ChiselsAndBitsConfiguration implements IChiselsAndBitsConfiguration
{

    static {
        //Load the language file.
        final String fileLoc = "assets/" + Constants.MOD_ID + "/lang/%s.json";
        LanguageHandler.loadLangPath(fileLoc);
    }

    private final IClientConfiguration client = new ClientConfiguration();
    private final ICommonConfiguration common = new CommonConfiguration();
    private final IServerConfiguration server = new ServerConfiguration();

    @Override
    public IClientConfiguration getClient()
    {
        return client;
    }

    @Override
    public ICommonConfiguration getCommon()
    {
        return common;
    }

    @Override
    public IServerConfiguration getServer()
    {
        return server;
    }
}
