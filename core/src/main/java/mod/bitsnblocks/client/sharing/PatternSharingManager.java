package mod.bitsnblocks.client.sharing;

import com.communi.suggestu.scena.core.dist.Dist;
import com.communi.suggestu.scena.core.dist.DistExecutor;
import com.mojang.datafixers.util.Either;
import mod.bitsnblocks.api.client.sharing.IPatternSharingManager;
import mod.bitsnblocks.api.client.sharing.PatternIOException;
import mod.bitsnblocks.api.item.multistate.IMultiStateItemStack;
import mod.bitsnblocks.api.util.LocalStrings;
import net.minecraft.client.Minecraft;

public final class PatternSharingManager implements IPatternSharingManager {
    private static final PatternSharingManager INSTANCE = new PatternSharingManager();

    public static PatternSharingManager getInstance() {
        return INSTANCE;
    }

    private PatternSharingManager() {
    }

    @Override
    public void exportPattern(final IMultiStateItemStack multiStateItemStack, final String name) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PatternSharingExecutor.doSavePattern(multiStateItemStack, name, Minecraft.getInstance().level.registryAccess()));
    }

    @Override
    public Either<IMultiStateItemStack, PatternIOException> importPattern(final String name) {
        return DistExecutor.unsafeRunForDist(
                () -> () -> PatternSharingExecutor.doImportPattern(name, Minecraft.getInstance().level.registryAccess()),
                () -> () -> Either.right(new PatternIOException(
                        LocalStrings.PatternImportInvokedFromTheServer.getText(),
                        "Pattern invoked from the server side!"
                )));
    }
}
