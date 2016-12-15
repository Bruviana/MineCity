package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.ForgeSingleBlockReaction;
import br.com.gamemods.minecity.reactive.reaction.NoReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IItemSnow extends IItemBlock
{
    @Override
    default Reaction reactRightClickBlock(IEntityPlayerMP player, IItemStack stack, boolean offHand, IState state, BlockPos pos,
                                          Direction face)
    {
        IBlock block = state.getIBlock();
        IBlock snow = this.getIBlock();

        if((face != Direction.UP || block != snow) && !block.isReplaceable(pos))
        {
            pos = pos.add(face);
            assert pos.world.instance != null;
            state = ((IWorldServer)pos.world.instance).getIState(pos);
            block = state.getIBlock();
        }

        if(block == snow)
        {
            int i = state.getIntValueOrMeta("layers");

            if(i <= 7)
            {
                ForgeSingleBlockReaction reaction = new ForgeSingleBlockReaction(pos, PermissionFlag.MODIFY);
                reaction.onDenyUpdateInventory();
                return reaction;
            }
        }

        return NoReaction.INSTANCE;
    }
}
