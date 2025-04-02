package mod.bitsnblocks.logic;

import mod.bitsnblocks.measures.MeasuringManager;
import net.minecraft.server.level.ServerPlayer;

public class MeasuringSynchronisationHandler
{

    public static void syncToAll() {
        MeasuringManager.getInstance().syncToAll();
    }

    public static void syncTo(ServerPlayer player) {
        MeasuringManager.getInstance().syncTo(player);
    }
}
