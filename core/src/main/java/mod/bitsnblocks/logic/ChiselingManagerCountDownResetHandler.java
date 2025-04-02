package mod.bitsnblocks.logic;

import mod.bitsnblocks.chiseling.ChiselingManager;
import net.minecraft.world.entity.player.Player;

public class ChiselingManagerCountDownResetHandler
{
    public static void doResetFor(Player player)
    {
        ChiselingManager.getInstance().resetLastChiselCountdown(player);
    }
}
