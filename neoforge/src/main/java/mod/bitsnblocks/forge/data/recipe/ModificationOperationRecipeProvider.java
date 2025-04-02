package mod.bitsnblocks.forge.data.recipe;

import mod.bitsnblocks.api.util.constants.Constants;
import mod.bitsnblocks.recipe.modificationtable.ModificationTableRecipe;
import mod.bitsnblocks.registrars.ModModificationOperation;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModificationOperationRecipeProvider extends RecipeProvider
{

    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(true, new ModificationOperationRecipeProvider(
                event.getGenerator().getPackOutput(),
                event.getLookupProvider()
        ));
    }

    private ModificationOperationRecipeProvider(final PackOutput generatorIn, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(generatorIn, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput consumer) {
        ModModificationOperation.REGISTRY_SUPPLIER.get().forEach(
                operation -> {
                    consumer.accept(
                            operation.getRegistryName(),
                            new ModificationTableRecipe(operation),
                            null
                    );
                }
        );
    }

    @Override
    public @NotNull String getName()
    {
        return "Modification operation recipes";
    }
}
