package mod.chiselsandbits.api.chiseling;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public interface IChiselingManager
{

    static IChiselingManager getInstance() {
        return IChiselsAndBitsAPI.getInstance().getChiselingManager();
    }

    /**
     * Gets or creates a new chiseling context for the given player.
     *
     * A new context is created when either one of the following conditions is met:
     *  - No context has been created before.
     *  - The world of the player and the world of the existing context are not equal
     *  - The new chisel mode and the chisel mode of the existing context are not equal.
     *
     * @param playerEntity The player for which the context is looked up.
     * @param mode The mode which the player wants to chisel in.
     * @param modeOfOperandus The mode of operation for the current context.
     * @param simulation Indicates if the context should be created as a simulation context if it does not exist. If the context does not exist, a simulation context is not stored in the manager. A simulation context will also be a snapshot of the current context if one already exists.
     * @param causingItemStack The stack that triggered the chiseling operation.
     * @return The context.
     */
    default IChiselingContext getOrCreateContext(
      final Player playerEntity,
      final IChiselMode mode,
      final ChiselingOperation modeOfOperandus,
      final boolean simulation,
      final ItemStack causingItemStack) {
        final Optional<IChiselingContext> optionalWithCurrent = get(playerEntity, mode, modeOfOperandus);

        if (optionalWithCurrent.isPresent())
        {
            final IChiselingContext current = optionalWithCurrent.get();
            return current.createSnapshot();
        }

        return create(playerEntity, mode, modeOfOperandus, simulation, causingItemStack);
    }

    /**
     * Gives access to the chiseling context of the player, if it exists.
     *
     * @param playerEntity The player for which the context is looked up.
     * @param mode The mode which the player wants to chisel in
     *
     * @return An optional potentially containing the current context of the player.
     */
    Optional<IChiselingContext> get(
      final Player playerEntity,
      final IChiselMode mode
    );

    /**
     * Gives access to the chiseling context of the player, if it exists.
     *
     * @param playerEntity The player for which the context is looked up.
     * @param mode The mode which the player wants to chisel in.
     * @param modeOfOperandus The mode of operation for the current context.
     *
     * @return An optional potentially containing the current context of the player.
     */
    Optional<IChiselingContext> get(
      final Player playerEntity,
      final IChiselMode mode,
      final ChiselingOperation modeOfOperandus
    );

    /**
     * Creates a new context for a given player.
     * If {@code simulate} is false and an a context for the player already exists, then that context is overriden.
     *
     * @param playerEntity The player for which the context is created.
     * @param mode The mode which the player wants to chisel in.
     * @param modeOfOperandus The mode of operation for the current context.
     * @param simulation Indicates if the context is used in contextual simulation. {@code true}, prevents overriding of the current context and also does not save the context in the manager.
     * @param causingItemStack The stack that triggered the chiseling operation.
     *
     * @return The newly created context.
     */
    IChiselingContext create(
      final Player playerEntity,
      final IChiselMode mode,
      final ChiselingOperation modeOfOperandus,
      final boolean simulation,
      final ItemStack causingItemStack
    );
}
