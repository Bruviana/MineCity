package br.com.gamemods.minecity.sponge.data.value;

import br.com.gamemods.minecity.reactive.game.block.data.BlockStateData;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTraitData;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTypeData;
import br.com.gamemods.minecity.sponge.data.manipulator.reactive.SpongeManipulator;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.data.property.block.ReplaceableProperty;

import java.util.Optional;
import java.util.stream.Stream;

public class SpongeBlockStateData implements BlockStateData
{
    private final SpongeManipulator manipulator;
    private final BlockState blockState;

    public SpongeBlockStateData(SpongeManipulator manipulator, BlockState blockState)
    {
        this.manipulator = manipulator;
        this.blockState = blockState;
    }

    @Override
    public BlockState getBlockState()
    {
        return blockState;
    }

    @NotNull
    @Override
    public <V extends Comparable<V>> Optional<V> getTrait(BlockTraitData<V> traitData)
    {
        BlockTrait<V> trait;
        if(traitData instanceof SpongeBlockTraitData)
            trait = ((SpongeBlockTraitData<V>) traitData).trait;
        else
            throw new UnsupportedOperationException("Unsupported traitData: "+traitData);

        return blockState.getTraitValue(trait);
    }

    @NotNull
    @Override
    public BlockTypeData getBlockTypeData()
    {
        return manipulator.block.getBlockTypeData(blockState.getType());
    }

    @Override
    public Stream<BlockTraitData<?>> blockTraitStream()
    {
        return blockState.getTraits().stream().map(manipulator.block::getBlockTraitData);
    }

    @Override
    public boolean isReplaceable()
    {
        return blockState.getProperty(ReplaceableProperty.class).map(ReplaceableProperty::getValue).orElse(false);
    }
}
