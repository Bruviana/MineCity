package br.com.gamemods.minecity.forge.accessors;

import br.com.gamemods.minecity.api.world.WorldDim;
import net.minecraft.world.WorldServer;

public interface IWorldServer
{
    WorldDim getMineCityWorld();
    void setMineCityWorld(WorldDim world);
    default WorldServer getForgeWorldServer()
    {
        return (WorldServer) this;
    }
}
