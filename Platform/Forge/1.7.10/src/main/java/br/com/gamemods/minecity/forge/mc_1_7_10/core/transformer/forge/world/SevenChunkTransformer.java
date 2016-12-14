package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.world;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.world.ChunkTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;

@Referenced
public class SevenChunkTransformer extends ChunkTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenChunkTransformer()
    {
        super("br.com.gamemods.minecity.forge.mc_1_7_10.accessors.world.SevenChunk");
    }
}
