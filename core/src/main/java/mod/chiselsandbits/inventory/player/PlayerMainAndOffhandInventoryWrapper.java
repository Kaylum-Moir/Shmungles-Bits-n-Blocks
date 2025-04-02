package mod.chiselsandbits.inventory.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PlayerMainAndOffhandInventoryWrapper implements Container
{

    private final Inventory playerInventory;

    public PlayerMainAndOffhandInventoryWrapper(final Inventory playerInventory) {this.playerInventory = playerInventory;}

    @Override
    public int getContainerSize()
    {
        return 9*4+1;
    }

    @Override
    public boolean isEmpty()
    {
        return playerInventory.isEmpty();
    }

    @NotNull
    @Override
    public ItemStack getItem(int index)
    {
        if (index == 36)
            index += 4;

        return playerInventory.getItem(index);
    }

    @NotNull
    @Override
    public ItemStack removeItem(int index, final int count)
    {
        if (index == 36)
            index += 4;

        return playerInventory.removeItem(index, count);
    }

    @NotNull
    @Override
    public ItemStack removeItemNoUpdate(int index)
    {
        if (index == 36)
            index += 4;

        return playerInventory.removeItemNoUpdate(index);
    }

    @Override
    public void setItem(int index, @NotNull final ItemStack stack)
    {
        if (index == 36)
            index += 4;

        playerInventory.setItem(index, stack);
    }

    @Override
    public void setChanged()
    {
        playerInventory.setChanged();
    }

    @Override
    public boolean stillValid(@NotNull final Player player)
    {
        return playerInventory.stillValid(player);
    }

    @Override
    public void clearContent()
    {
        playerInventory.clearContent();
    }
}
