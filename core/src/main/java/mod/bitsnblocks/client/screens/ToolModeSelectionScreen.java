package mod.bitsnblocks.client.screens;

import mod.bitsnblocks.api.client.screen.AbstractChiselsAndBitsScreen;
import mod.bitsnblocks.api.item.withmode.IToolMode;
import mod.bitsnblocks.api.item.withmode.IWithModeItem;
import mod.bitsnblocks.api.item.withmode.group.IToolModeGroup;
import mod.bitsnblocks.api.util.LocalStrings;
import mod.bitsnblocks.client.screens.widgets.ChangeTrackerOperationsWidget;
import mod.bitsnblocks.client.screens.widgets.ToolModeSelectionWidget;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ToolModeSelectionScreen<M extends IToolMode<G>, G extends IToolModeGroup> extends AbstractChiselsAndBitsScreen
{
    @SuppressWarnings("unchecked")
    public static <M extends IToolMode<G>, G extends IToolModeGroup> ToolModeSelectionScreen<M, G> create(
      final ItemStack sourceStack
    )
    {
        final Item item = sourceStack.getItem();
        if (!(item instanceof IWithModeItem))
            throw new IllegalArgumentException("Can not open ToolMode UI for none ToolMode item!");

        final IWithModeItem<M> withModeItem = (IWithModeItem<M>) item;

        return new ToolModeSelectionScreen<>(withModeItem, sourceStack);
    }

    private final IWithModeItem<M> toolModeItem;
    private final ItemStack sourceStack;

    private ToolModeSelectionScreen(
      final IWithModeItem<M> toolModeItem,
      final ItemStack sourceStack
    )
    {
        super(LocalStrings.ToolMenuScreenName.getText(sourceStack.getDisplayName()));

        this.toolModeItem = toolModeItem;
        this.sourceStack = sourceStack;
    }

    @Override
    protected void init()
    {
        super.init();
        this.addRenderableWidget(
          new ToolModeSelectionWidget<>(
            this,
            toolModeItem,
            sourceStack
          )
        );

        this.addRenderableWidget(
          new ChangeTrackerOperationsWidget(
            this.width - ChangeTrackerOperationsWidget.WIDTH - 6,
            (int) ((this.height / 2f) - ChangeTrackerOperationsWidget.HEIGHT / 2f),
            this
          )
        );
    }
}
