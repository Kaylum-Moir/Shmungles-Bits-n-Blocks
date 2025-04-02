package mod.bitsnblocks.client.logic;

import com.communi.suggestu.scena.core.dist.Dist;
import com.communi.suggestu.scena.core.dist.DistExecutor;
import mod.bitsnblocks.BitsNBlocks;
import mod.bitsnblocks.item.MeasuringTapeItem;
import mod.bitsnblocks.keys.KeyBindingManager;
import mod.bitsnblocks.network.packets.MeasurementsResetPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public class MeasurementTapeItemResetHandler {

    public static void checkAndDoReset() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            if (KeyBindingManager.getInstance().isResetMeasuringTapeKeyPressed()) {
                ItemStack stack = ItemStack.EMPTY;
                if (Minecraft.getInstance().player.getMainHandItem().getItem() instanceof MeasuringTapeItem) {
                    stack = Minecraft.getInstance().player.getMainHandItem();
                }
                else if (Minecraft.getInstance().player.getOffhandItem().getItem() instanceof MeasuringTapeItem) {
                    stack = Minecraft.getInstance().player.getOffhandItem();
                }

                if (!stack.isEmpty() && stack.getItem() instanceof MeasuringTapeItem measuringTapeItem) {
                    measuringTapeItem.clear(stack);
                    BitsNBlocks.getInstance().getNetworkChannel().sendToServer(new MeasurementsResetPacket());
                }
            }
        });
    }
}