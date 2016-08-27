package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;

@Referenced
public class FrostRayTraceResultTransformer extends InsertInterfaceTransformer
{
    @Referenced(at = MineCityFrostCoreMod.class)
    public FrostRayTraceResultTransformer()
    {
        super(
                "net.minecraft.util.math.RayTraceResult",
                "br.com.gamemods.minecity.forge.mc_1_10_2.accessors.FrostRayTraceResult"
        );
    }
}
