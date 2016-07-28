package br.com.gamemods.minecity.api.world;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.permission.EntityID;
import br.com.gamemods.minecity.api.permission.Identifiable;
import br.com.gamemods.minecity.api.permission.Identity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface MinecraftEntity extends Identifiable
{
    @NotNull
    @Override
    UUID getUniqueId();

    @NotNull
    Type getType();

    @Nullable
    CommandSender getCommandSender();

    @NotNull
    @Override
    default Identity<UUID> getIdentity()
    {
        Type type = getType();
        if(type == Type.PLAYER)
            return new PlayerID(getUniqueId(), getName());

        return new EntityID(type, getUniqueId(), getName());
    }

    enum Type
    {
        /**
         * A player...
         */
        PLAYER,

        /**
         * A dropped item
         */
        ITEM,

        /**
         * A decorative entity that doesn't hold items, like paintings and banners
         */
        STRUCTURE,

        /**
         * A structural entity that holds items, like frames and armor stands
         */
        STORAGE,

        /**
         * Any vehicle that is not alive, like minecarts and boats
         */
        VEHICLE,

        /**
         * Arrows, snowballs, eggs..
         */
        PROJECTILE,

        /**
         * Pacific animals like chicken and cow
         */
        ANIMAL,

        /**
         * Shh BOOM
         */
        MONSTER,

        /**
         * Something very different...
         */
        UNCLASSIFIED
    }
}
