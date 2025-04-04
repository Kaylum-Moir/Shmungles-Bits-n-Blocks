package mod.bitsnblocks.client.model.baked.base;

import com.communi.suggestu.scena.core.client.models.baked.IDataAwareBakedModel;
import com.communi.suggestu.scena.core.client.models.data.IBlockModelData;
import com.communi.suggestu.scena.core.client.rendering.type.IRenderTypeManager;
import mod.bitsnblocks.client.model.baked.simple.NullBakedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@SuppressWarnings("deprecation")
public abstract class BaseSmartModel implements BakedModel, IDataAwareBakedModel {

    private final ItemOverrides overrides;

    public BaseSmartModel() {
        overrides = new OverrideHelper(this);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @NotNull
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.STONE.defaultBlockState());
    }

    @NotNull
    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    @NotNull
    public List<BakedQuad> getQuads(
            @Nullable final BlockState state, @Nullable final Direction side, @NotNull final RandomSource rand, @NotNull final IBlockModelData extraData, @Nullable RenderType renderType) {
        final BakedModel model = handleBlockState(state, rand, extraData, renderType);
        if (model instanceof IDataAwareBakedModel dataAwareBakedModel)
            return dataAwareBakedModel.getQuads(state, side, rand, extraData, renderType);

        return model.getQuads(state, side, rand);
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, @NotNull final RandomSource rand) {
        final BakedModel model = handleBlockState(state, rand);
        return model.getQuads(state, side, rand);
    }

    public BakedModel handleBlockState(
            final BlockState state,
            final RandomSource rand) {
        return NullBakedModel.instance;
    }

    public BakedModel handleBlockState(
            final BlockState state,
            final RandomSource random,
            final IBlockModelData modelData) {
        return handleBlockState(state, random);
    }

    public BakedModel handleBlockState(
            final BlockState state,
            final RandomSource random,
            final IBlockModelData modelData,
            @Nullable RenderType renderType) {
        return handleBlockState(state, random, modelData);
    }

    public BakedModel handleItemStack(
            final ItemStack stack
    ) {
        return NullBakedModel.instance;
    }

    @NotNull
    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    public BakedModel resolve(
            final BakedModel originalModel,
            final ItemStack stack,
            final Level world,
            final LivingEntity entity) {
        return handleItemStack(stack);
    }

    @Override
    public @NotNull Collection<RenderType> getSupportedRenderTypes(BlockState blockState, RandomSource randomSource, IBlockModelData iBlockModelData) {
        final BakedModel model = handleBlockState(blockState, randomSource, iBlockModelData);
        if (model instanceof IDataAwareBakedModel dataAwareBakedModel) {
            return dataAwareBakedModel.getSupportedRenderTypes(blockState, randomSource, iBlockModelData);
        }

        return IRenderTypeManager.getInstance().getRenderTypesFor(
                model,
                blockState,
                randomSource,
                iBlockModelData
        );
    }

	@Override
	public @NotNull Collection<RenderType> getSupportedRenderTypes(ItemStack itemStack, boolean b) {
		final BakedModel model = handleItemStack(itemStack);
		if (model instanceof IDataAwareBakedModel dataAwareBakedModel) {
			return dataAwareBakedModel.getSupportedRenderTypes(itemStack, b);
		}

		return IRenderTypeManager.getInstance().getRenderTypesFor(
				model,
				itemStack,
				b
		);
	}

	private static class OverrideHelper extends ItemOverrides {
        final BaseSmartModel parent;

        public OverrideHelper(
                final BaseSmartModel p) {
            super();
            parent = p;
        }

        @Nullable
        @Override
        public BakedModel resolve(
                final @NotNull BakedModel p_173465_,
                final @NotNull ItemStack p_173466_,
                @Nullable final ClientLevel p_173467_,
                @Nullable final LivingEntity p_173468_,
                final int p_173469_) {
            return parent.resolve(p_173465_, p_173466_, p_173467_, p_173468_);
        }
    }
}
