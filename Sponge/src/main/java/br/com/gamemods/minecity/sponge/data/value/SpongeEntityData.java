package br.com.gamemods.minecity.sponge.data.value;

import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.reactive.game.entity.data.EntityData;
import br.com.gamemods.minecity.reactive.game.server.data.ChunkData;
import br.com.gamemods.minecity.sponge.data.manipulator.reactive.SpongeManipulator;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.Chunk;

import java.util.Optional;

public class SpongeEntityData implements EntityData
{
    private final Entity entity;
    private final SpongeManipulator manipulator;

    public SpongeEntityData(SpongeManipulator manipulator, Entity entity)
    {
        this.entity = entity;
        this.manipulator = manipulator;
    }

    @Override
    public Entity getEntity()
    {
        return entity;
    }

    @Override
    public EntityPos getEntityPosition()
    {
        return manipulator.sponge.entityPos(entity);
    }

    @NotNull
    @Override
    public Optional<ChunkData> getChunkData()
    {
        Optional<Chunk> chunk = entity.getWorld().getChunk(entity.getLocation().getChunkPosition());
        if(!chunk.isPresent())
            return Optional.empty();

        return Optional.of(manipulator.server.getChunkData(chunk.get()));
    }
}
