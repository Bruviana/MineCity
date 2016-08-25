package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.command.IForgePlayer;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.EntityPlayerMPTransformer;
import net.minecraft.entity.player.EntityPlayerMP;

@Referenced(at = EntityPlayerMPTransformer.class)
public interface IEntityPlayerMP extends IEntity
{
    void setMineCityPlayer(IForgePlayer player);
    IForgePlayer getMineCityPlayer();

    default EntityPlayerMP getEntityPlayerMP()
    {
        return (EntityPlayerMP) this;
    }

    default PlayerID getIdentity()
    {
        IForgePlayer player = getMineCityPlayer();
        if(player != null)
            return player.getPlayerId();

        return new PlayerID(getUniqueId(), getName());
    }
}
