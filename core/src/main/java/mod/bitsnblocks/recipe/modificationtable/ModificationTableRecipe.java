package mod.bitsnblocks.recipe.modificationtable;

import mod.bitsnblocks.api.item.multistate.IMultiStateItem;
import mod.bitsnblocks.api.item.multistate.IMultiStateItemStack;
import mod.bitsnblocks.api.item.pattern.IMultiUsePatternItem;
import mod.bitsnblocks.api.item.pattern.IPatternItem;
import mod.bitsnblocks.api.modification.operation.IModificationOperation;
import mod.bitsnblocks.api.multistate.snapshot.IMultiStateSnapshot;
import mod.bitsnblocks.multistate.snapshot.EmptySnapshot;
import mod.bitsnblocks.registrars.ModRecipeSerializers;
import mod.bitsnblocks.registrars.ModRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ModificationTableRecipe implements Recipe<CraftingInput>
{
    private final IModificationOperation operation;

    public ModificationTableRecipe(final IModificationOperation operation) {this.operation = operation;}

    public IModificationOperation getOperation()
    {
        return operation;
    }

    @Override
    public boolean matches(final CraftingInput inv, final @NotNull Level worldIn)
    {
        return inv.getItem(0).getItem() instanceof IPatternItem && !(inv.getItem(0).getItem() instanceof IMultiUsePatternItem);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return getAppliedSnapshot(input).toItemStack().toPatternStack();
    }

    public @NotNull ItemStack getCraftingBlockResult(final CraftingInput inv)
    {
        return getAppliedSnapshot(inv).toItemStack().toBlockStack();
    }

    public @NotNull IMultiStateSnapshot getAppliedSnapshot(final CraftingInput inv)
    {
        final ItemStack multiStateStack = inv.getItem(0);
        if (multiStateStack.isEmpty())
            return EmptySnapshot.INSTANCE;

        if (!(multiStateStack.getItem() instanceof final IMultiStateItem item))
            return EmptySnapshot.INSTANCE;

        final IMultiStateItemStack multiStateItemStack = item.createItemStack(multiStateStack);
        final IMultiStateSnapshot snapshot = multiStateItemStack.createSnapshot().clone();

        getOperation().apply(snapshot);

        return snapshot;
    }

    public Component getDisplayName() {
        return Component.translatable(
          Objects.requireNonNull(this.getOperation().getRegistryName()).getNamespace() + ".recipes.chisel.pattern.modification." + this.getOperation().getRegistryName().getPath());
    }

    @Override
    public boolean canCraftInDimensions(final int width, final int height)
    {
        return width * height > 0;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer()
    {
        return ModRecipeSerializers.MODIFICATION_TABLE.get();
    }

    @Override
    public @NotNull RecipeType<?> getType()
    {
        return ModRecipeTypes.MODIFICATION_TABLE.get();
    }
}
