package mod.bitsnblocks.client.model;

import com.communi.suggestu.scena.core.client.models.loaders.IModelSpecification;
import com.communi.suggestu.scena.core.client.models.loaders.context.IModelBakingContext;
import mod.bitsnblocks.client.model.baked.chiseled.DataAwareChiseledBlockBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;

import java.util.function.Function;

public class ChiseledBlockModel implements IModelSpecification<ChiseledBlockModel>
{
    @Override
    public BakedModel bake(IModelBakingContext iModelBakingContext, ModelBaker modelBaker, Function<Material, TextureAtlasSprite> function, ModelState modelState) {
        return new DataAwareChiseledBlockBakedModel();
    }
}
