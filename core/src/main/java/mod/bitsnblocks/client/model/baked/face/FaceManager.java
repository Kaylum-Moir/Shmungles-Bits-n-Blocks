package mod.bitsnblocks.client.model.baked.face;

import com.communi.suggestu.scena.core.client.fluid.IClientFluidManager;
import com.communi.suggestu.scena.core.client.models.baked.IDataAwareBakedModel;
import com.communi.suggestu.scena.core.client.rendering.IRenderingManager;
import com.communi.suggestu.scena.core.fluid.FluidInformation;
import com.communi.suggestu.scena.core.registries.IPlatformRegistryManager;
import com.google.common.collect.Lists;
import mod.bitsnblocks.api.blockinformation.BlockInformation;
import mod.bitsnblocks.api.client.model.baked.cache.IBakedModelCacheKey;
import mod.bitsnblocks.api.client.variant.state.IClientStateVariantManager;
import mod.bitsnblocks.api.config.IClientConfiguration;
import mod.bitsnblocks.client.model.baked.cache.BakedModelCacheKeyCalculatorRegistry;
import mod.bitsnblocks.client.model.baked.face.model.ModelQuadLayer;
import mod.bitsnblocks.client.model.baked.face.model.ModelVertexRange;
import mod.bitsnblocks.client.model.baked.simple.SimpleGeneratedModel;
import mod.bitsnblocks.utils.ItemStackUtils;
import mod.bitsnblocks.utils.LightUtil;
import mod.bitsnblocks.utils.SimpleMaxSizedCache;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class FaceManager {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final RandomSource RANDOM = Util.make(RandomSource.createNewThreadLocalInstance(), (random) -> random.setSeed(42L));

    private static final FaceManager INSTANCE = new FaceManager();

    private final SimpleMaxSizedCache<Key, Collection<ModelQuadLayer>> cache = new SimpleMaxSizedCache<>(
            IClientConfiguration.getInstance().getFaceLayerCacheSize()::get
    );
    private final SimpleMaxSizedCache<BlockInformation, Integer> colorCache = new SimpleMaxSizedCache<>(
            () -> {
                return IPlatformRegistryManager.getInstance().getBlockStateIdMap().size() == 0 ? 1000 : IPlatformRegistryManager.getInstance().getBlockStateIdMap().size();
            }
    );


    private FaceManager() {
    }

    public static FaceManager getInstance() {
        return INSTANCE;
    }

    private static Optional<ModelQuadLayer> createQuadLayer(
            final BakedQuad quad, BlockInformation blockInformation, final Direction cullDirection, final int stateColor) {
        if (quad.getDirection() != cullDirection)
            return Optional.empty();

        try {
            final TextureAtlasSprite sprite = findQuadTexture(quad);

            ModelQuadLayer.Builder layerBuilder = ModelQuadLayer.Builder.create(blockInformation);
            layerBuilder.setTexture(sprite);
            layerBuilder.withColor(stateColor);
            layerBuilder.withSourceQuad(quad);

            LightUtil.put(layerBuilder, quad);

            return Optional.of(layerBuilder.build());
        } catch (final Exception ex) {
            LOGGER.error("Failed to process quad: " + quad, ex);
            return Optional.empty();
        }
    }

    private static BakedModel solveModel(
            final BlockInformation state,
            final BakedModel originalModel,
            final long primaryStateRenderSeed,
            final RenderType renderType
    ) {
        boolean hasFaces;
        try {
            hasFaces = hasFaces(originalModel, state, null, primaryStateRenderSeed, renderType);

            for (final Direction f : Direction.values()) {
                hasFaces = hasFaces || hasFaces(originalModel, state, f, primaryStateRenderSeed, renderType);
            }
        } catch (final Exception e) {
            // an exception was thrown.. use the item model and hope...
            hasFaces = false;
        }

        if (!hasFaces) {
            // if the model is empty then lets grab an item and try that...
            final ItemStack is = ItemStackUtils.getItemStackFromBlockState(state);
            if (!is.isEmpty()) {
                final BakedModel itemModel =
                        Minecraft.getInstance().getItemRenderer().getModel(is, Minecraft.getInstance().level, Minecraft.getInstance().player, 0);

                try {
                    hasFaces = hasFaces(originalModel, state, null, primaryStateRenderSeed, renderType);

                    for (final Direction f : Direction.values()) {
                        hasFaces = hasFaces || hasFaces(originalModel, state, f, primaryStateRenderSeed, renderType);
                    }
                } catch (final Exception e) {
                    // an exception was thrown.. use the item model and hope...
                    hasFaces = false;
                }

                if (hasFaces) {
                    return itemModel;
                } else {
                    return new SimpleGeneratedModel(findTexture(state, originalModel, Direction.UP, primaryStateRenderSeed, renderType));
                }
            }
        }

        return originalModel;
    }

    private static boolean hasFaces(
            final BakedModel model,
            final BlockInformation state,
            final Direction f,
            final long primaryStateRenderSeed,
            final RenderType renderType) {
        final List<BakedQuad> quads = getModelQuads(model, state, f, primaryStateRenderSeed, renderType);
        if (quads == null || quads.isEmpty()) {
            return false;
        }

        TextureAtlasSprite texture = null;

        try {
            texture = findTexture(null, quads, f);
        } catch (final Exception ignored) {
        }

        final ModelVertexRange vertexRangeExtractor = new ModelVertexRange();

        for (final BakedQuad quad : quads) {
            LightUtil.put(vertexRangeExtractor, quad);
        }

        return vertexRangeExtractor.getLargestRange() > 0 && !isMissingTexture(texture);
    }

    public static TextureAtlasSprite findTexture(
            final BlockInformation state,
            final BakedModel model,
            final Direction myFace,
            final long primaryStateRenderSeed,
            final RenderType renderType) {
        TextureAtlasSprite texture = null;

        if (model != null) {
            try {
                texture = findTexture(null, getModelQuads(model, state, myFace, primaryStateRenderSeed, renderType), myFace);

                if (texture == null) {
                    for (final Direction side : Direction.values()) {
                        texture = findTexture(texture, getModelQuads(model, state, side, primaryStateRenderSeed, renderType), side);
                    }

                    texture = findTexture(texture, getModelQuads(model, state, null, primaryStateRenderSeed, renderType), null);
                }
            } catch (final Exception ignored) {
            }
        }

        // who knows if that worked.. now lets try to get a texture...
        if (isMissingTexture(texture)) {
            try {
                if (model != null) {
                    texture = model.getParticleIcon();
                }
            } catch (final Exception ignored) {
            }
        }

        if (isMissingTexture(texture)) {
            try {
                texture = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(state.blockState());
            } catch (final Exception ignored) {
            }
        }

        if (texture == null) {
            texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ResourceLocation.withDefaultNamespace("missingno"));
        }

        return texture;
    }

    private static TextureAtlasSprite findTexture(
            TextureAtlasSprite texture,
            final List<BakedQuad> faceQuads,
            final Direction myFace) throws IllegalArgumentException, NullPointerException {
        for (final BakedQuad q : faceQuads) {
            if (q.getDirection() == myFace) {
                texture = findQuadTexture(q);
            }
        }

        return texture;
    }

    @SuppressWarnings("ConstantConditions")
    private static TextureAtlasSprite findQuadTexture(
            final BakedQuad q
    ) throws IllegalArgumentException, NullPointerException {
        if (q.getSprite() == null)
            return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(MissingTextureAtlasSprite.getLocation());
        return q.getSprite();
    }

    private static boolean isMissingTexture(final TextureAtlasSprite sprite) {
        if (sprite == null)
            return true;

        return sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation());
    }

    private static List<BakedQuad> getModelQuads(
            final BakedModel model,
            final BlockInformation state,
            final Direction f,
            final long primaryStateRenderSeed,
            final RenderType renderType) {
        // try to get block model...
        try {
            RANDOM.setSeed(primaryStateRenderSeed);
            if (model instanceof IDataAwareBakedModel dataAwareBakedModel) {
                return dataAwareBakedModel.getQuads(state.blockState(), f, RANDOM, IClientStateVariantManager.getInstance().getBlockModelData(state), renderType);
            } else {
                return model.getQuads(state.blockState(), f, RANDOM);
            }
        } catch (final Throwable ignored) {
        }

        try {
            RANDOM.setSeed(primaryStateRenderSeed);
            // try to get item model?
            if (model instanceof IDataAwareBakedModel dataAwareBakedModel) {
                return dataAwareBakedModel.getQuads(null, f, RANDOM, IClientStateVariantManager.getInstance().getBlockModelData(state), renderType);
            } else {
                return model.getQuads(null, f, RANDOM);
            }
        } catch (final Throwable ignored) {
        }

        final ItemStack is = ItemStackUtils.getItemStackFromBlockState(state);
        if (!is.isEmpty()) {
            final BakedModel secondModel = getOverrides(model).resolve(model, is, Minecraft.getInstance().level, Minecraft.getInstance().player, 0);

            if (secondModel != null) {
                try {
                    RANDOM.setSeed(primaryStateRenderSeed);
                    if (secondModel instanceof IDataAwareBakedModel dataAwareBakedModel) {
                        return dataAwareBakedModel.getQuads(state.blockState(), f, RANDOM, IClientStateVariantManager.getInstance().getBlockModelData(state), renderType);
                    } else {
                        return secondModel.getQuads(state.blockState(), f, RANDOM);
                    }
                } catch (final Throwable ignored) {
                }
            }
        }

        // try to not crash...
        return Collections.emptyList();
    }

    private static ItemOverrides getOverrides(
            final BakedModel model) {
        if (model != null) {
            return model.getOverrides();
        }
        return ItemOverrides.EMPTY;
    }

    private static void injectFluidVertexDataForSide(final ModelQuadLayer.Builder builder, final float minU, final float maxU, final float minV, final float maxV, final Direction cullDirection) {
        if (cullDirection == null)
            return;

        switch (cullDirection) {
            case DOWN -> {
                builder.withVertexData(v -> {
                    v.withVertexIndex(0).withX(0).withY(0).withZ(1).withU(minU).withV(minV);
                });
                builder.withVertexData(v -> {
                    v.withVertexIndex(1).withX(0).withY(0).withZ(0).withU(minU).withV(maxV);
                });
                builder.withVertexData(v -> {
                    v.withVertexIndex(3).withX(1).withY(0).withZ(1).withU(maxU).withV(maxV);
                });
                builder.withVertexData(v -> {
                    v.withVertexIndex(2).withX(1).withY(0).withZ(0).withU(maxU).withV(minV);
                });
            }
            case UP -> {
                builder.withVertexData(v -> {
                    v.withVertexIndex(0).withX(0).withY(1).withZ(0).withU(minU).withV(minV);
                });
                builder.withVertexData(v -> {
                    v.withVertexIndex(1).withX(0).withY(1).withZ(1).withU(minU).withV(maxV);
                });
                builder.withVertexData(v -> {
                    v.withVertexIndex(2).withX(1).withY(1).withZ(1).withU(maxU).withV(maxV);
                });
                builder.withVertexData(v -> {
                    v.withVertexIndex(3).withX(1).withY(1).withZ(0).withU(maxU).withV(minV);
                });
            }
            case NORTH -> {
                builder.withVertexData(v -> {
                    v.withVertexIndex(0).withX(1).withY(1).withZ(0).withU(minU).withV(minV);
                });
                builder.withVertexData(v -> {
                    v.withVertexIndex(1).withX(1).withY(0).withZ(0).withU(minU).withV(maxV);
                });
                builder.withVertexData(v -> {
                    v.withVertexIndex(2).withX(0).withY(0).withZ(0).withU(maxU).withV(maxV);
                });
                builder.withVertexData(v -> {
                    v.withVertexIndex(3).withX(0).withY(1).withZ(0).withU(maxU).withV(minV);
                });
            }
            case SOUTH -> {
                builder.withVertexData(v -> {
                    v.withVertexIndex(0).withX(0).withY(1).withZ(1).withU(minU).withV(minV);
                });
                builder.withVertexData(v -> {
                    v.withVertexIndex(1).withX(0).withY(0).withZ(1).withU(minU).withV(maxV);
                });
                builder.withVertexData(v -> {
                    v.withVertexIndex(2).withX(1).withY(0).withZ(1).withU(maxU).withV(maxV);
                });
                builder.withVertexData(v -> {
                    v.withVertexIndex(3).withX(1).withY(1).withZ(1).withU(maxU).withV(minV);
                });
            }
            case WEST -> {
                builder.withVertexData(v -> {
                    v.withVertexIndex(0).withX(0).withY(1).withZ(0).withU(minU).withV(minV);
                });
                builder.withVertexData(v -> {
                    v.withVertexIndex(1).withX(0).withY(0).withZ(0).withU(minU).withV(maxV);
                });
                builder.withVertexData(v -> {
                    v.withVertexIndex(2).withX(0).withY(0).withZ(1).withU(maxU).withV(maxV);
                });
                builder.withVertexData(v -> {
                    v.withVertexIndex(3).withX(0).withY(1).withZ(1).withU(maxU).withV(minV);
                });
            }
            case EAST -> {
                builder.withVertexData(v -> {
                    v.withVertexIndex(0).withX(1).withY(1).withZ(1).withU(minU).withV(minV);
                });
                builder.withVertexData(v -> {
                    v.withVertexIndex(1).withX(1).withY(0).withZ(1).withU(minU).withV(maxV);
                });
                builder.withVertexData(v -> {
                    v.withVertexIndex(2).withX(1).withY(0).withZ(0).withU(maxU).withV(maxV);
                });
                builder.withVertexData(v -> {
                    v.withVertexIndex(3).withX(1).withY(1).withZ(0).withU(maxU).withV(minV);
                });
            }
        }
    }

    public void clearCache() {
        cache.clear();
        colorCache.clear();
    }

    public Collection<ModelQuadLayer> getCachedLayersFor(
            final BlockInformation state,
            final Direction face,
            final RenderType layer,
            long primaryStateRenderSeed,
            @NotNull RenderType renderType) {
        if (layer == null) {
            return null;
        }

        final BakedModel model = solveModel(state, Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(state.blockState()), primaryStateRenderSeed, renderType);
        final IBakedModelCacheKey modelCacheKey = BakedModelCacheKeyCalculatorRegistry.getInstance()
                .getCacheKey(model, primaryStateRenderSeed);

        final Key key = new Key(state, layer, face, modelCacheKey, renderType);

        return cache.get(key, () -> {
            try {
                return buildFaceQuadLayers(state, face, primaryStateRenderSeed, renderType, model);
            } finally {
            }
        });
    }

    private List<ModelQuadLayer> buildFaceQuadLayers(
            final BlockInformation blockInformation,
            final Direction cullDirection,
            final long primaryStateRenderSeed,
            @NotNull final RenderType renderType,
            @NotNull final BakedModel model) {
        final int lv = IClientConfiguration.getInstance().getUseGetLightValue().get() ? blockInformation.blockState().getLightEmission() : 0;

        final Fluid fluid = blockInformation.blockState().getFluidState().getType();
        if (fluid != Fluids.EMPTY) {
            final ModelQuadLayer.Builder builder = ModelQuadLayer.Builder.create(blockInformation);
            builder.setQuadOrientation(cullDirection);
            builder.withColor(IClientFluidManager.getInstance().getFluidColor(new FluidInformation(fluid)));
            builder.withLight(lv);

            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(IRenderingManager.getInstance().getFlowingFluidTexture(fluid));

            if (cullDirection.getAxis() == Direction.Axis.Y) {
                sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(IRenderingManager.getInstance().getStillFluidTexture(fluid));
            }

            float minV = sprite.getV0();
            float maxU = sprite.getU(16f / sprite.contents().width());
            float minU = sprite.getU0();
            float maxV = sprite.getV(16f / sprite.contents().height());

            builder.withSprite(sprite);
            injectFluidVertexDataForSide(builder, minU, maxU, minV, maxV, cullDirection);

            builder.setQuadTint(0xff);

            return Collections.singletonList(builder.build());
        }

        final List<ModelQuadLayer> layers = Lists.newArrayList();
        final int color = getColorFor(blockInformation);

        final List<BakedQuad> quads = getModelQuads(model, blockInformation, cullDirection, primaryStateRenderSeed, renderType);
        quads.forEach(quad -> createQuadLayer(quad, blockInformation, cullDirection, color).ifPresent(layers::add));

        return layers;
    }

    private int getColorFor(final BlockInformation state) {
        return colorCache.get(state, () -> {
            //TODO: Introduce a way for external systems to provide a color for a face based on the state variant.

            int out;
            final Fluid fluid = state.blockState().getFluidState().getType();
            if (fluid != Fluids.EMPTY) {
                out = IClientFluidManager.getInstance().getFluidColor(fluid);
            } else {
                final ItemStack target = ItemStackUtils.getItemStackFromBlockState(state);

                if (target.isEmpty()) {
                    out = 0xffffff;
                } else {
                    out = Minecraft.getInstance().itemColors.getColor(target, 0);
                }
            }

            return out;
        });
    }

    private record Key(BlockInformation blockState, RenderType renderType, Direction direction,
                       IBakedModelCacheKey modelKey, RenderType type) {
    }
}
