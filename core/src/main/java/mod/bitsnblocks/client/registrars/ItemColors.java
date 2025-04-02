package mod.bitsnblocks.client.registrars;

import com.communi.suggestu.scena.core.client.rendering.IColorManager;
import com.communi.suggestu.scena.core.registries.deferred.IRegistryObject;
import mod.bitsnblocks.client.colors.BitBagItemColor;
import mod.bitsnblocks.client.colors.BitItemItemColor;
import mod.bitsnblocks.client.colors.ChiseledBlockItemItemColor;
import mod.bitsnblocks.compact.legacy.MateriallyChiseledConversionItem;
import mod.bitsnblocks.registrars.ModItems;

public final class ItemColors
{

    private ItemColors()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ItemColors. This is a utility class");
    }

    public static void onClientConstruction()
    {
        IColorManager.getInstance().setupItemColors(
          configuration -> {
              configuration
                .register(new ChiseledBlockItemItemColor(), ModItems.LEGACY_MATERIAL_CHISELED_BLOCKS.stream().map(IRegistryObject::get).toArray(MateriallyChiseledConversionItem[]::new));
              configuration
                .register(new ChiseledBlockItemItemColor(), ModItems.CHISELED_BLOCK.get());
              configuration
                .register(new BitItemItemColor(), ModItems.ITEM_BLOCK_BIT.get());
              configuration
                .register(new BitBagItemColor(), ModItems.ITEM_BIT_BAG_DEFAULT.get(), ModItems.ITEM_BIT_BAG_DYED.get());
          }
        );
    }
}
