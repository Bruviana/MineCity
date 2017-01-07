package br.com.gamemods.minecity.sponge.data.value;

import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockType;
import br.com.gamemods.minecity.reactive.game.block.data.BlockStateData;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTraitData;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTypeData;
import br.com.gamemods.minecity.reactive.game.item.data.ItemData;
import br.com.gamemods.minecity.sponge.data.manipulator.reactive.SpongeManipulator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.block.BlockType;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpongeBlockTypeData implements BlockTypeData
{
    private final SpongeManipulator manipulator;
    private final BlockType blockType;
    private ReactiveBlockType reactive = ReactiveBlockType.DECORATIVE;

    public SpongeBlockTypeData(SpongeManipulator manipulator, BlockType blockType)
    {
        this.manipulator = manipulator;
        this.blockType = blockType;
    }

    @NotNull
    @Override
    public Optional<ReactiveBlockType> getReactiveBlockType()
    {
        return Optional.of(reactive);
    }

    @Override
    public void setReactive(@Nullable ReactiveBlockType reactiveBlock)
    {
        reactive = reactiveBlock == null? ReactiveBlockType.DECORATIVE : reactiveBlock;
    }

    @Override
    public BlockType getBlockType()
    {
        return blockType;
    }

    @Override
    public BlockStateData getDefaultBlockStateData()
    {
        return manipulator.block.getBlockStateData(blockType.getDefaultState());
    }

    @Override
    public Optional<ItemData> getItemData()
    {
        return blockType.getItem().map(manipulator.item::getItemData);
    }

    @Override
    public Optional<BlockTraitData<?>> getTraitData(String traitId)
    {
        return blockType.getTrait(traitId).map(manipulator.block::getBlockTraitData);
    }

    @Override
    public Collection<BlockTraitData<?>> getTraits()
    {
        return blockType.getTraits().stream().map(manipulator.block::getBlockTraitData).collect(Collectors.toList());
    }

    @NotNull
    @Override
    public Optional<String> getBlockIdName()
    {
        return Optional.of(blockType.getName());
    }

    @NotNull
    @Override
    public Optional<Integer> getBlockId()
    {
        return Optional.empty();
    }

    @Override
    public String toString()
    {
        return "SpongeBlockTypeData{"+
                "blockType="+blockType+
                ", reactive="+reactive+
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        SpongeBlockTypeData that = (SpongeBlockTypeData) o;

        return blockType.equals(that.blockType);
    }

    @Override
    public int hashCode()
    {
        return blockType.hashCode();
    }
}
