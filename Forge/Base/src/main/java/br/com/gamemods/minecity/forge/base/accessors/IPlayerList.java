package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.Teleporter;

import java.util.List;

public interface IPlayerList
{
    List<EntityPlayerMP> getPlayerEntities();
    List<IEntityPlayerMP> getIPlayers();

    void transferToDimension(IEntityPlayerMP player, int dimension, Teleporter teleporter);

    void transferToDimension(IEntity entity, int dimension, Teleporter teleporter);

    boolean isOp(GameProfile profile);
}
