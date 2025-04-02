package mod.bitsnblocks.client.registrars;

import com.communi.suggestu.scena.core.client.rendering.IRenderingManager;
import mod.bitsnblocks.client.besr.BitStorageBESR;
import mod.bitsnblocks.registrars.ModBlockEntityTypes;

public final class BlockEntityRenderers
{

    private BlockEntityRenderers()
    {
        throw new IllegalStateException("Can not instantiate an instance of: BlockEntityRenderers. This is a utility class");
    }

    public static void onClientConstruction() {
        IRenderingManager.getInstance().registerBlockEntityRenderer(
                registrar -> {
                    registrar.registerBlockEntityRenderer(ModBlockEntityTypes.BIT_STORAGE.get(), context -> new BitStorageBESR());
                }
        );
    }
}
