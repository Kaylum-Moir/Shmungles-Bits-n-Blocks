package mod.bitsnblocks.client.registrars;

import mod.bitsnblocks.client.tooltip.ChiseledBlockTooltipHandler;

public class ClientTooltipComponents {

    public static void onClientConstruction() {
        ChiseledBlockTooltipHandler.configure();
    }
}
