package mod.bitsnblocks.client.registrars;

import com.communi.suggestu.scena.core.client.rendering.IColorManager;
import mod.bitsnblocks.client.colors.ChiseledBlockBlockColor;
import mod.bitsnblocks.registrars.ModBlocks;

public final class BlockColors
{

    private BlockColors()
    {
        throw new IllegalStateException("Can not instantiate an instance of: BlockColors. This is a utility class");
    }

    public static void onClientConstruction()
    {
        IColorManager.getInstance().setupBlockColors(
          configuration -> {
              configuration.register(
                      new ChiseledBlockBlockColor(),
                      ModBlocks.CHISELED_BLOCK.get()
              );
          }
        );
    }
}
