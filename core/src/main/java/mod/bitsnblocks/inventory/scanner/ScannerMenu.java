package mod.bitsnblocks.inventory.scanner;

import com.communi.suggestu.scena.core.inventory.ScenaContainerMenu;
import mod.bitsnblocks.registrars.ModBlocks;
import mod.bitsnblocks.registrars.ModContainerTypes;
import mod.bitsnblocks.utils.container.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import org.jetbrains.annotations.NotNull;

public class ScannerMenu extends ScenaContainerMenu
{

    private final ContainerLevelAccess containerLevelAccess;
    private final SimpleContainer      scannerSlotContainer;

    public ScannerMenu(int screenIndex, Inventory inventory) {
        this(screenIndex, inventory, ContainerLevelAccess.NULL);
    }

    public ScannerMenu(final int screenIndex, final Inventory inventory, final ContainerLevelAccess containerLevelAccess)
    {
        super(ModContainerTypes.PATTERN_SCANNER_CONTAINER.get(), screenIndex);
        this.containerLevelAccess = containerLevelAccess;
        this.scannerSlotContainer = new SimpleContainer(0);
    }

    @Override
    public boolean stillValid(final @NotNull Player player)
    {
        return stillValid(containerLevelAccess, player, ModBlocks.PATTERN_SCANNER.get());
    }

    @Override
    protected void registerQuickMoveRules() {
        //Noop for now.
    }
}
