package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.permission.BasicFlagHolder;
import br.com.gamemods.minecity.api.world.WorldDim;
import org.jetbrains.annotations.NotNull;

public final class Nature extends BasicFlagHolder implements ChunkOwner
{
    @NotNull
    public final MineCity mineCity;
    @NotNull
    public final WorldDim world;
    private boolean valid = true;

    public Nature(@NotNull MineCity mineCity, @NotNull WorldDim world)
    {
        this.mineCity = mineCity;
        this.world = world;
        mineCity.defaultNatureFlags.forEach(this::deny);
    }

    public void invalidate()
    {
        valid = false;
    }

    public boolean isValid()
    {
        return valid;
    }
}
