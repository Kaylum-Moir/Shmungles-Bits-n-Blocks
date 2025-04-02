package mod.bitsnblocks.logic;

import mod.bitsnblocks.change.ChangeTrackerSyncManager;

public class ServerTickHandler {

    public static void onPostServerTick() {
        ChangeTrackerSyncManager.getInstance().sync();
    }
}
