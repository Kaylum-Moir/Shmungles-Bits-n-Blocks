package mod.bitsnblocks.chiseling.conversion;

import mod.bitsnblocks.api.blockinformation.BlockInformation;
import mod.bitsnblocks.api.chiseling.conversion.IConversionManager;
import mod.bitsnblocks.api.chiseling.eligibility.IEligibilityManager;
import mod.bitsnblocks.registrars.ModBlocks;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public class ConversionManager implements IConversionManager
{
    private static final ConversionManager INSTANCE = new ConversionManager();

    public static ConversionManager getInstance()
    {
        return INSTANCE;
    }

    @Override
    public Optional<Block> getChiseledVariantOf(final Block block)
    {
        if (!IEligibilityManager.getInstance().canBeChiseled(new BlockInformation(block.defaultBlockState(), Optional.empty())))
            return Optional.empty();

        return Optional.of(ModBlocks.CHISELED_BLOCK.get());
    }

    private ConversionManager()
    {
    }
}
