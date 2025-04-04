package mod.bitsnblocks.client.logic;

import com.communi.suggestu.scena.core.dist.DistExecutor;
import mod.bitsnblocks.item.MonocleItem;
import mod.bitsnblocks.keys.KeyBindingManager;
import mod.bitsnblocks.utils.ItemStackUtils;
import net.minecraft.client.Minecraft;

public class IsScopingHandler
{

    private IsScopingHandler()
    {
        throw new IllegalStateException("Can not instantiate an instance of: IsScopingHandler. This is a utility class");
    }

    public static boolean isScoping()
    {
        return DistExecutor.unsafeRunForDist(
          () -> () -> !ItemStackUtils.getHighlightItemStackFromPlayer(Minecraft.getInstance().player).isEmpty() &&
                        Minecraft.getInstance().player.getInventory().getArmor(3).getItem() instanceof MonocleItem &&
                        KeyBindingManager.getInstance().isScopingKeyPressed(),
          () -> () -> false
        );
    }
}
