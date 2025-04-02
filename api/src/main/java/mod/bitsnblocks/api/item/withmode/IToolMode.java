package mod.bitsnblocks.api.item.withmode;

import mod.bitsnblocks.api.item.withmode.group.IToolModeGroup;
import mod.bitsnblocks.api.util.IWithDisplayName;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A mode of a given tool.
 */
public interface IToolMode<G extends IToolModeGroup> extends IWithDisplayName, IRenderableMode
{

    /**
     * An optional which indicates the group this tool mode
     * is part of.
     *
     * @return The optional tool mode group.
     */
    @NotNull
    Optional<G> getGroup();
}
