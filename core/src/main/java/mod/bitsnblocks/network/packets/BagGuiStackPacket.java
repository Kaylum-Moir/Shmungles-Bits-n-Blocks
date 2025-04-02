package mod.bitsnblocks.network.packets;

import mod.bitsnblocks.api.util.constants.Constants;
import mod.bitsnblocks.container.BagContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class BagGuiStackPacket extends ModPacket
{

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "bag_gui_stack");
    public static final CustomPacketPayload.Type<BagGuiStackPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    private int       index = -1;
    private ItemStack stack;

    public BagGuiStackPacket(final RegistryFriendlyByteBuf buffer)
    {
        readPayload(buffer);
    }

    public BagGuiStackPacket(final int index, @NotNull final ItemStack stack)
    {
        this.index = index;
        this.stack = stack;
    }

    @Override
    public void client()
    {
        if (Minecraft.getInstance().player == null)
        {
            return;
        }

        if (Minecraft.getInstance().player.containerMenu == null)
        {
            return;
        }

        final AbstractContainerMenu cc = Minecraft.getInstance().player.containerMenu;
        if (cc instanceof BagContainer)
        {
            ((BagContainer) cc).bitSlots.get(index).set(stack);
        }
    }

    @Override
    public void readPayload(
      final RegistryFriendlyByteBuf buffer)
    {
        index = buffer.readInt();
        stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        stack.setCount(buffer.readVarInt());
    }

    @Override
    public void writePayload(final RegistryFriendlyByteBuf buffer)
    {
        buffer.writeInt(index);

        final ItemStack networkStack = stack.copy();
        networkStack.setCount(1);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, networkStack);

        buffer.writeVarInt(stack.getCount());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
