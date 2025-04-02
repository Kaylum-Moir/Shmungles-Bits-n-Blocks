package mod.bitsnblocks.api.item.multistate;

import mod.bitsnblocks.api.blockinformation.BlockInformation;
import mod.bitsnblocks.api.serialization.RawSerializable;

import java.util.Set;

/**
 * The statistics of a multi state itemstack.
 */
public interface IStatistics extends RawSerializable
{

    /**
     * The primary state of the mutli state itemstacks this statistics object
     * belongs to.
     *
     * @return The primary blockstate.
     */
    BlockInformation getPrimaryState();

    /**
     * Indicates if the multistate object is empty.
     *
     * @return {@code true} for an empty multi state object.
     */
    boolean isEmpty();

    /**
     * Returns all states (without count) that are contained in the object
     *
     * @return All states in the object.
     */
    Set<BlockInformation> getContainedStates();
}
