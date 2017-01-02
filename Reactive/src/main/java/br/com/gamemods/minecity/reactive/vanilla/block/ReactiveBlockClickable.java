package br.com.gamemods.minecity.reactive.vanilla.block;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.reactive.game.block.Interaction;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockTrait;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockType;
import br.com.gamemods.minecity.reactive.game.block.data.BlockRole;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;
import org.jetbrains.annotations.NotNull;

/**
 * A block that has simple click reactions, like wooden doors and buttons.
 */
public interface ReactiveBlockClickable<T extends Comparable<T>> extends ReactiveBlockType, ReactiveBlockTrait<T>
{
    ReactiveBlockClickable<?> INSTANCE = new ReactiveBlockClickable(){};

    @NotNull
    @Override
    default BlockRole getBlockRole()
    {
        return BlockRole.CLICKABLE;
    }

    @Override
    default Reaction reactRightClick(Interaction event)
    {
        return new SingleBlockReaction(event.getBlock().getPosition(), PermissionFlag.CLICK);
    }
}
