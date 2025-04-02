package mod.bitsnblocks.client.registrars;

import com.communi.suggestu.scena.core.client.rendering.IRenderingManager;
import mod.bitsnblocks.client.ister.BitStorageISTER;
import mod.bitsnblocks.client.ister.InteractionISTER;
import mod.bitsnblocks.registrars.ModItems;

public final class BlockEntityWithoutLevelRenderers
{

    private BlockEntityWithoutLevelRenderers()
    {
        throw new IllegalStateException("Can not instantiate an instance of: BlockEntityWithoutLevelRenderers. This is a utility class");
    }

    public static void onClientConstruction() {
        IRenderingManager.getInstance().registerBlockEntityWithoutLevelRenderer(
                registrar -> {
                    registrar.registerBlockEntityWithoutLevelRenderer(ModItems.ITEM_BIT_STORAGE.get(), new BitStorageISTER());
                    registrar.registerBlockEntityWithoutLevelRenderer(ModItems.UNSEAL_ITEM.get(), new InteractionISTER());
                    registrar.registerBlockEntityWithoutLevelRenderer(ModItems.QUILL.get(), new InteractionISTER());
                    registrar.registerBlockEntityWithoutLevelRenderer(ModItems.SEALANT_ITEM.get(), new InteractionISTER());
                }
        );
    }
}
