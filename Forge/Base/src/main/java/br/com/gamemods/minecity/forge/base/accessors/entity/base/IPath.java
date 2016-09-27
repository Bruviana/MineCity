package br.com.gamemods.minecity.forge.base.accessors.entity.base;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IPath
{
    boolean isFinished();

    IPathPoint getFinalPoint();
}
