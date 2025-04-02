package mod.bitsnblocks.logic;

import com.mojang.brigadier.CommandDispatcher;
import mod.bitsnblocks.command.CommandManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

public class CommandRegistrationHandler
{

    public static void registerCommandsTo(final CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext pContext) {
        CommandManager.getInstance().register(dispatcher, pContext);
    }
}
