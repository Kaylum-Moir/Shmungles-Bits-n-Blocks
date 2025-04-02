package mod.bitsnblocks.inventory.management;

import mod.bitsnblocks.api.inventory.bit.IAdaptingBitInventoryManager;
import mod.bitsnblocks.api.inventory.bit.IBitInventory;
import mod.bitsnblocks.api.inventory.bit.IBitInventoryItem;
import mod.bitsnblocks.api.inventory.bit.IBitInventoryItemStack;
import mod.bitsnblocks.api.inventory.management.IBitInventoryManager;
import mod.bitsnblocks.inventory.bit.IInventoryBitInventory;
import mod.bitsnblocks.inventory.player.PlayerMainAndOffhandInventoryWrapper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BitInventoryManager implements IBitInventoryManager
{
    private static final BitInventoryManager INSTANCE = new BitInventoryManager();

    private BitInventoryManager()
    {
    }

    public static BitInventoryManager getInstance()
    {
        return INSTANCE;
    }

    @Override
    public IBitInventory create(final Player playerEntity)
    {
        return this.create(new PlayerMainAndOffhandInventoryWrapper(playerEntity.getInventory()));
    }

    @Override
    public IBitInventory create(final Object target)
    {
        return IAdaptingBitInventoryManager.getInstance()
                 .create(target)
                 .filter(IBitInventory.class::isInstance)
                 .map(IBitInventory.class::cast)
                 .orElseThrow(() -> new IllegalArgumentException("The given target object is not supported on the current platform!"));
    }

    @Override
    public IBitInventory create(final Container inventory)
    {
        return new IInventoryBitInventory(inventory);
    }

    @Override
    public IBitInventoryItemStack create(final ItemStack stack)
    {
        if (stack.getItem() instanceof IBitInventoryItem bitInventoryItem) {
            return bitInventoryItem.create(stack);
        }

        throw new IllegalArgumentException("The given ItemStack is not supported on the current platform!");
    }
}
