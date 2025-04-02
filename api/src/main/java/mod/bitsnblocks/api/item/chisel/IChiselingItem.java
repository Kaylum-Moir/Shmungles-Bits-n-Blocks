package mod.bitsnblocks.api.item.chisel;

import mod.bitsnblocks.api.chiseling.mode.IChiselMode;
import mod.bitsnblocks.api.item.change.IChangeTrackingItem;
import mod.bitsnblocks.api.item.click.ILeftClickControllingItem;
import mod.bitsnblocks.api.item.withhighlight.IWithHighlightItem;
import mod.bitsnblocks.api.item.withmode.IWithModeItem;

public interface IChiselingItem extends ILeftClickControllingItem, IWithModeItem<IChiselMode>, IWithHighlightItem, IChangeTrackingItem
{

    boolean isDamageableDuringChiseling();
}
