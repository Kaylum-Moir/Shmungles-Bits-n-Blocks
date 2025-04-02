package mod.bitsnblocks.logic;

import mod.bitsnblocks.chiseling.ChiselingManager;

public class ServerStartHandler
{

    public static void onServerStart() {
        ChiselingManager.getInstance().onServerStarting();
    }
}
