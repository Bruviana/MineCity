package br.com.gamemods.minecity.forge.base.protection.opencomputers;

import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.MultiBlockReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

import java.util.ArrayList;
import java.util.List;

@Referenced(at = ModInterfacesTransformer.class)
public interface IScreen extends ISimpleBlock
{
    @Override
    default Reaction reactPrePlace(Permissible who, IItemStack stack, BlockPos base)
    {
        List<BlockPos> list = new ArrayList<>(7);
        list.add(base);
        IWorldServer world = base.world.getInstance(IWorldServer.class);
        Direction.block.stream().map(base::add).filter(pos-> world.getIBlock(pos) == this).forEach(list::add);
        return new MultiBlockReaction(PermissionFlag.MODIFY, list);
    }

    @Override
    default Reaction reactRightClickActivation(BlockPos pos, IState state, IEntityPlayerMP player,
                                               IItemStack stack, boolean offHand, Direction face)
    {
        SingleBlockReaction reaction = new SingleBlockReaction(pos, PermissionFlag.CLICK);
        reaction.onDenyCloseScreen(player);
        return reaction;
    }
}
