package br.com.gamemods.minecity.forge.base.protection.wrcbe;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.forgemultipart.ITMultiPart;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface DevicePart extends ITMultiPart
{
    @Override
    default Reaction reactPlayerActivate(IEntityPlayerMP player, IItemStack stack)
    {
        SingleBlockReaction reaction = new SingleBlockReaction(
                tileI().getBlockPos(player.getServer()),
                PermissionFlag.MODIFY
        );
        reaction.onDenyCloseScreen(player);
        return reaction;
    }
}
