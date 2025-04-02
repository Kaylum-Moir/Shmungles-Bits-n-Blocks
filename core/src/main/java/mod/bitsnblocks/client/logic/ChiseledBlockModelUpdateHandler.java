package mod.bitsnblocks.client.logic;

import mod.bitsnblocks.block.entities.ChiseledBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

public class ChiseledBlockModelUpdateHandler
{

    public static void updateAllModelDataInChunk(LevelChunk chunk)
    {
        chunk.getBlockEntities()
                .values()
                .forEach(blockEntity ->
                {
                    if (blockEntity instanceof ChiseledBlockEntity chiseledBlockEntity)
                        chiseledBlockEntity.updateModelData();
                });
    }

}
