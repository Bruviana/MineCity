package br.com.gamemods.minecity.forge.base.protection.immersiveengineering;

import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenReactor;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.ShapeBlockReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockWoodenDevices extends IBlockOpenReactor
{
    @Override
    default Reaction reactPrePlace(Permissible who, IItemStack stack, BlockPos pos)
    {
        IWorldServer world = pos.world.getInstance(IWorldServer.class);
        ITileEntity tile = world.getTileEntity(pos);
        if(tile instanceof Shaped)
            return new ShapeBlockReaction(pos.world, ((Shaped) tile).getShape(), PermissionFlag.MODIFY);

        return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
    }

    @Override
    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack,
                                     boolean offHand, Direction face)
    {
        ITileEntity tile = player.getIWorld().getTileEntity(pos);
        if(stack != null && stack.isTool("IE_HAMMER") && tile instanceof ImmersiveTileModifyOnHammer)
        {
            SingleBlockReaction react = new SingleBlockReaction(pos, PermissionFlag.MODIFY);
            react.addDenialListener((reaction, permissible, flag, pos1, message) ->
                    player.sendBlockAndTile(pos)
            );
            return react;
        }

        if(!player.isSneaking() && tile instanceof ImmersiveTileOpenOnClick)
            return new SingleBlockReaction(pos, PermissionFlag.OPEN);

        return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
    }
}
