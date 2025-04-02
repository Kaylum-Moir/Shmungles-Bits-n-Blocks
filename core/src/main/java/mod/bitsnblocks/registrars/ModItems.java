package mod.bitsnblocks.registrars;

import com.communi.suggestu.scena.core.registries.deferred.IRegistrar;
import com.communi.suggestu.scena.core.registries.deferred.IRegistryObject;
import com.google.common.collect.Lists;
import mod.bitsnblocks.api.util.constants.Constants;
import mod.bitsnblocks.compact.legacy.LegacyMaterialManager;
import mod.bitsnblocks.compact.legacy.MateriallyChiseledConversionItem;
import mod.bitsnblocks.item.BitBagItem;
import mod.bitsnblocks.item.BitStorageBlockItem;
import mod.bitsnblocks.item.ChiseledBlockItem;
import mod.bitsnblocks.item.MagnifyingGlassItem;
import mod.bitsnblocks.item.MeasuringTapeItem;
import mod.bitsnblocks.item.MonocleItem;
import mod.bitsnblocks.item.MultiUsePatternItem;
import mod.bitsnblocks.item.QuillItem;
import mod.bitsnblocks.item.SealantItem;
import mod.bitsnblocks.item.SingleUsePatternItem;
import mod.bitsnblocks.item.UnsealItem;
import mod.bitsnblocks.item.WrenchItem;
import mod.bitsnblocks.item.bit.BitItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public final class ModItems {
    private static final Logger LOGGER = LogManager.getLogger();
    private final static IRegistrar<Item> ITEM_REGISTRAR = IRegistrar.create(Registries.ITEM, Constants.MOD_ID);
        public static final IRegistryObject<BitItem> ITEM_BLOCK_BIT =
            ITEM_REGISTRAR.register("block_bit", () -> new BitItem(new Item.Properties()));
    public static final IRegistryObject<MagnifyingGlassItem> MAGNIFYING_GLASS =
            ITEM_REGISTRAR.register("magnifying_glass", () -> new MagnifyingGlassItem(new Item.Properties()));
    public static final IRegistryObject<BitBagItem> ITEM_BIT_BAG_DEFAULT =
            ITEM_REGISTRAR.register("bit_bag", () -> new BitBagItem(new Item.Properties()));
    public static final IRegistryObject<BitBagItem> ITEM_BIT_BAG_DYED =
            ITEM_REGISTRAR.register("bit_bag_dyed", () -> new BitBagItem(new Item.Properties()));
    public static final IRegistryObject<BitStorageBlockItem>
            ITEM_BIT_STORAGE =
            ITEM_REGISTRAR.register("bit_storage", () -> new BitStorageBlockItem(ModBlocks.BIT_STORAGE.get(), new Item.Properties()
            ));
    public static final IRegistryObject<BlockItem>
            ITEM_MODIFICATION_TABLE =
            ITEM_REGISTRAR.register("modification_table", () -> new BlockItem(ModBlocks.MODIFICATION_TABLE.get(), new Item.Properties()
            ));
    public static final IRegistryObject<MeasuringTapeItem> MEASURING_TAPE =
            ITEM_REGISTRAR.register("measuring_tape", () -> new MeasuringTapeItem(new Item.Properties()));
    public static final IRegistryObject<SingleUsePatternItem> SINGLE_USE_PATTERN_ITEM =
            ITEM_REGISTRAR.register("pattern_single_use", () -> new SingleUsePatternItem(new Item.Properties()));
    public static final IRegistryObject<MultiUsePatternItem> MULTI_USE_PATTERN_ITEM =
            ITEM_REGISTRAR.register("pattern_multi_use", () -> new MultiUsePatternItem(new Item.Properties()));
    public static final IRegistryObject<QuillItem> QUILL =
            ITEM_REGISTRAR.register("quill", () -> new QuillItem(new Item.Properties()));
    public static final IRegistryObject<SealantItem> SEALANT_ITEM =
            ITEM_REGISTRAR.register("sealant", () -> new SealantItem(new Item.Properties()));
    public static final IRegistryObject<BlockItem> CHISELED_PRINTER =
            ITEM_REGISTRAR.register("chiseled_printer", () -> new BlockItem(ModBlocks.CHISELED_PRINTER.get(), new Item.Properties()));
    public static final IRegistryObject<BlockItem> PATTERN_SCANNER =
            ITEM_REGISTRAR.register("pattern_scanner", () -> new BlockItem(ModBlocks.PATTERN_SCANNER.get(), new Item.Properties()));
    public static final IRegistryObject<WrenchItem> WRENCH =
            ITEM_REGISTRAR.register("wrench", () -> new WrenchItem(new Item.Properties()));
    public static final IRegistryObject<UnsealItem> UNSEAL_ITEM =
            ITEM_REGISTRAR.register("unseal", () -> new UnsealItem(new Item.Properties()));
    public static final IRegistryObject<MonocleItem> MONOCLE_ITEM =
            ITEM_REGISTRAR.register("monocle", () -> new MonocleItem(new Item.Properties()));

    public static final IRegistryObject<ChiseledBlockItem> CHISELED_BLOCK =
            ITEM_REGISTRAR.register("chiseled_block", () -> new ChiseledBlockItem(ModBlocks.CHISELED_BLOCK.get(), new Item.Properties()));

    @Deprecated
    public static final List<IRegistryObject<MateriallyChiseledConversionItem>> LEGACY_MATERIAL_CHISELED_BLOCKS = Lists.newArrayList();

    private ModItems() {
        throw new IllegalStateException("Tried to initialize: ModItems but this is a Utility class.");
    }

    public static void onModConstruction() {
        LegacyMaterialManager.getInstance().getMaterialNames()
                .forEach(materialName -> {
                    LEGACY_MATERIAL_CHISELED_BLOCKS.add(
                            ITEM_REGISTRAR.register(
                                    "chiseled" + materialName,
                                    () -> new MateriallyChiseledConversionItem(
                                            new Item.Properties()
                                    )
                            )
                    );
                });

        LOGGER.info("Loaded item configuration.");
    }
}
