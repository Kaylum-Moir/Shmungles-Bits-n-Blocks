package mod.bitsnblocks.inventory.bit;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mod.bitsnblocks.api.blockinformation.BlockInformation;
import mod.bitsnblocks.api.inventory.bit.watchable.IWatch;
import mod.bitsnblocks.api.inventory.bit.watchable.IWatchableBitInventory;
import mod.bitsnblocks.api.item.bit.IBitItem;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SlottedBitInventory extends AbstractBitInventory implements IWatchableBitInventory
{
    protected final int size;
    protected final Int2ObjectMap<ItemStack> slotMap = new Int2ObjectArrayMap<>();

    private final Map<UUID, Runnable> onChangeCallbacks = Maps.newConcurrentMap();

    public SlottedBitInventory(
      final int size
    ) {
        this.size = size;
    }

    @Override
    protected ItemStack getItem(final int index)
    {
        return slotMap.getOrDefault(index, ItemStack.EMPTY);
    }

    @Override
    protected int getInventorySize()
    {
        return size;
    }

    @Override
    protected void setSlotContents(final int index, final ItemStack stack)
    {
        if (stack.isEmpty())
        {
            slotMap.remove(index);
            onChange();
            return;
        }

        if (!(stack.getItem() instanceof IBitItem))
            throw new IllegalArgumentException("Can not insert a none bit item into the inventory.");

        slotMap.put(index, stack);

        onChange();
    }

    @Override
    public IWatch startWatching(final Runnable onChangeCallback)
    {
        final UUID id = UUID.randomUUID();
        this.onChangeCallbacks.put(id, onChangeCallback);
        return () -> this.onChangeCallbacks.remove(id);
    }

    protected Stream<BitSlot> getContents() {
        return this.slotMap.values().stream()
                .filter(Predicate.not(ItemStack::isEmpty))
                .map(
                stack -> new BitSlot(
                        ((IBitItem) stack.getItem()).getBlockInformation(stack),
                        stack.getCount()
                )
        );
    }

    protected void onChange() {
        this.onChangeCallbacks.values().forEach(Runnable::run);
    }

    @Override
    public boolean isEmpty()
    {
        return this.slotMap.isEmpty() || this.slotMap.values().stream().allMatch(slot -> slot.getCount() == 0);
    }

    protected static final class BitSlot {

        private BlockInformation blockInformation = BlockInformation.AIR;
        private int count;

        public BitSlot()
        {
        }

        public BitSlot(final BlockInformation blockInformation, final int count)
        {
            this.blockInformation = blockInformation;
            this.count = count;
        }

        public BlockInformation getBlockInformation()
        {
            return blockInformation;
        }

        public int getCount()
        {
            return count;
        }

        public void setBlockInformation(final BlockInformation blockInformation)
        {
            this.blockInformation = blockInformation;
        }

        public void setCount(final int count)
        {
            this.count = count;
        }
    }
}
