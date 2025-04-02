package mod.bitsnblocks.chiseling.eligibility;

import com.communi.suggestu.saecularia.caudices.core.block.IBlockWithWorldlyProperties;
import mod.bitsnblocks.api.blockinformation.BlockInformation;
import mod.bitsnblocks.api.chiseling.eligibility.IEligibilityOptions;
import mod.bitsnblocks.api.config.IServerConfiguration;
import mod.bitsnblocks.api.util.SingleBlockBlockReader;
import mod.bitsnblocks.registrars.ModBlocks;
import mod.bitsnblocks.utils.ClassUtils;
import mod.bitsnblocks.utils.ReflectionHelperBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEligibilityAnalysisData
{
    private final boolean isCompatible;
    private final float   hardness;
    private final float   explosionResistance;

    private BlockEligibilityAnalysisData(
      final boolean isCompatible,
      final float hardness,
      final float explosionResistance )
    {
        this.isCompatible = isCompatible;
        this.hardness = hardness;
        this.explosionResistance = explosionResistance;
    }

    public boolean isCompatible()
    {
        return isCompatible;
    }

    public float getHardness()
    {
        return hardness;
    }

    public float getExplosionResistance()
    {
        return explosionResistance;
    }

    @SuppressWarnings("DataFlowIssue") //We need to pass null in as a level.
    public static BlockEligibilityAnalysisData createFromState(
      final BlockInformation state )
    {
        try
        {
            // require basic hardness behavior...
            final ReflectionHelperBlock reflectBlock = ModBlocks.REFLECTION_HELPER_BLOCK.get();
            final Block blk = state.blockState().getBlock();
            final Class<? extends Block> blkClass = blk.getClass();

            reflectBlock.getDestroyProgress( null, null, null, null );
            final Class<?> b_Class = ClassUtils.getDeclaringClass( blkClass, reflectBlock.getLastInvokedThreadLocalMethodName(), BlockState.class, Player.class, BlockGetter.class, BlockPos.class );
            final boolean test_b = b_Class == Block.class || b_Class == BlockBehaviour.class;

            reflectBlock.getExplosionResistance();
            Class<?> exploResistanceClz = ClassUtils.getDeclaringClass( blkClass, reflectBlock.getLastInvokedThreadLocalMethodName());
            final boolean test_c = exploResistanceClz == Block.class || exploResistanceClz == BlockBehaviour.class;

            reflectBlock.getExplosionResistance( null, null, null, null );
            exploResistanceClz = ClassUtils.getDeclaringClass( blkClass, reflectBlock.getLastInvokedThreadLocalMethodName(), BlockState.class, BlockGetter.class, BlockPos.class, Explosion.class );
            final boolean test_d = exploResistanceClz == Block.class || exploResistanceClz == BlockBehaviour.class || exploResistanceClz == null ||
                                     IEligibilityOptions.getInstance().isValidExplosionDefinitionClass(exploResistanceClz);

            final boolean isFluid = !state.blockState().getFluidState().isEmpty();

            // is it perfect?
            if ( test_b && test_c && test_d && !isFluid )
            {
                final float blockHardness = state.blockState().getDestroySpeed(new SingleBlockBlockReader(state, state.blockState().getBlock()), BlockPos.ZERO);
                float resistance = blk.getExplosionResistance();

                if (blk instanceof IBlockWithWorldlyProperties blockWithWorldlyProperties) {
                    resistance = blockWithWorldlyProperties.getExplosionResistance(state.blockState(), new SingleBlockBlockReader(state, state.blockState().getBlock()), BlockPos.ZERO,
                            new Explosion(null, null, 0, 1,0, 10, false, Explosion.BlockInteraction.KEEP));
                }

                return new BlockEligibilityAnalysisData( true, blockHardness, resistance );
            }
            else if (test_b && test_c && test_d) {
                //TODO Adapt this
                return new BlockEligibilityAnalysisData( true, 2f, 6f );
            }
            else
            {
                return new BlockEligibilityAnalysisData( IServerConfiguration.getInstance().getCompatabilityMode().get(), 2f, 6f );
            }
        }
        catch ( final Exception err )
        {
            return new BlockEligibilityAnalysisData( false, -1, -1 );
        }
    }
}
