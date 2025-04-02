package mod.bitsnblocks.forge.data.model;

import mod.bitsnblocks.api.util.constants.Constants;
import mod.bitsnblocks.registrars.ModItems;
import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class UnsealItemModelGenerator extends AbstractInteractableItemModelGenerator
{

    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(true, new UnsealItemModelGenerator(event.getGenerator(), event.getExistingFileHelper()));
    }

    public UnsealItemModelGenerator(final DataGenerator generator, final ExistingFileHelper existingFileHelper)
    {
        super(generator, existingFileHelper, ModItems.UNSEAL_ITEM);
    }

    @Override
    public @NotNull String getName()
    {
        return "Unseal item model generator";
    }
}
