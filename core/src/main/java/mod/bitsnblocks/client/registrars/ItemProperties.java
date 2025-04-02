package mod.bitsnblocks.client.registrars;

import com.communi.suggestu.scena.core.client.models.IModelManager;
import mod.bitsnblocks.api.util.constants.Constants;
import mod.bitsnblocks.registrars.ModItems;
import net.minecraft.resources.ResourceLocation;

public final class ItemProperties {

    private ItemProperties() {
        throw new IllegalStateException("Can not instantiate an instance of: ItemProperties. This is a utility class");
    }

    public static void onClientConstruction() {
        IModelManager.getInstance().registerItemModelProperty(registrar -> {
            registrar.registerItemModelProperty(ModItems.MEASURING_TAPE.get(), ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "is_measuring"), (stack, clientWorld, livingEntity, value) -> {
                if (stack.getItem() != ModItems.MEASURING_TAPE.get())
                    return 0;

                return ModItems.MEASURING_TAPE.get().getStart(stack).isPresent() ? 1 : 0;
            });
        });
    }
}
