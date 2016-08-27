package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.world;

import br.com.gamemods.minecity.api.shape.PreciseCuboid;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.mc_1_7_10.SevenUtil;
import br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block.SevenBlock;
import br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block.SevenMetadataProperty;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.world.SevenWorldServerTransformer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Referenced(at = SevenWorldServerTransformer.class)
public interface SevenWorldServer extends IWorldServer
{
    @Override
    default SevenBlock getIBlock(int x, int y, int z)
    {
        return (SevenBlock) ((WorldServer) this).getBlock(x, y, z);
    }

    @Override
    default SevenBlock getIState(int x, int y, int z)
    {
        return getIBlock(x, y, z);
    }

    @Override
    default boolean isSideSolid(int x, int y, int z, Direction direction)
    {
        return ((WorldServer) this).isSideSolid(x, y, z, SevenUtil.toForgeDirection(direction));
    }

    default boolean isTopSolid(int x, int y, int z)
    {
        return ((WorldServer) this).isSideSolid(x, y, z, ForgeDirection.UP);
    }

    @Override
    default int getDimensionId()
    {
        return ((WorldServer) this).provider.dimensionId;
    }

    @Override
    default SevenChunk getLoadedChunk(int x, int z)
    {
        return (SevenChunk) ((WorldServer) this).theChunkProviderServer.loadedChunkHashMap.getValueByKey(
                ChunkCoordIntPair.chunkXZ2Int(x, z)
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    default Collection<Chunk> getLoadedChunks()
    {
        return ((WorldServer) this).theChunkProviderServer.loadedChunks;
    }

    @Override
    default boolean setBlock(int x, int y, int z, IState state)
    {
        return ((WorldServer) this).setBlock(x, y, z, state.getForgeBlock(), state.getValue(SevenMetadataProperty.INSTANCE), 3);
    }

    @Override
    default IRayTraceResult rayTraceBlocks(PrecisePoint start, PrecisePoint end, boolean stopOnLiquid)
    {
        return (IRayTraceResult) ((WorldServer) this).rayTraceBlocks(
                Vec3.createVectorHelper(start.x, start.y, start.z),
                Vec3.createVectorHelper(end.x, end.y, end.z),
                stopOnLiquid
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    default List<PreciseCuboid> getCollisionBoxes(PreciseCuboid cuboid)
    {
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
                cuboid.min.x,
                cuboid.min.y,
                cuboid.min.z,
                cuboid.max.x,
                cuboid.max.y,
                cuboid.max.z
        );

        List<AxisAlignedBB> list = ((WorldServer) this).func_147461_a(box);
        return list.stream().map(bb -> new PreciseCuboid(
                new PrecisePoint(bb.minX, bb.minY, bb.minZ),
                new PrecisePoint(bb.maxX, bb.maxY, bb.maxZ)
        )).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    default List<IEntity> getEntities(PreciseCuboid cuboid)
    {
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
                cuboid.min.x,
                cuboid.min.y,
                cuboid.min.z,
                cuboid.max.x,
                cuboid.max.y,
                cuboid.max.z
        );

        //noinspection ConstantConditions
        return ((WorldServer) this).getEntitiesWithinAABBExcludingEntity(null, box);
    }
}
