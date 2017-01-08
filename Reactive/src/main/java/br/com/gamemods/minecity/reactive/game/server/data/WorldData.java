package br.com.gamemods.minecity.reactive.game.server.data;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.reactive.game.server.data.supplier.SupplierWorldData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Information about a world
 */
public interface WorldData extends SupplierWorldData
{
    Object getWorld();

    /**
     * The server where this world is stored
     */
    @NotNull
    @Override
    ServerData getServerData();

    /**
     * Returns itself
     */
    @NotNull
    @Override
    default WorldData getWorldData()
    {
        return this;
    }

    Optional<BlockPos> getBlockPos(Object pos);
}
