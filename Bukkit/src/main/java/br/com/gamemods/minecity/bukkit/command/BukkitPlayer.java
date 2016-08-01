package br.com.gamemods.minecity.bukkit.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import org.bukkit.entity.Player;

public class BukkitPlayer extends BukkitLocatableSender<Player>
{
    public final PlayerID playerId;

    public BukkitPlayer(MineCityBukkit plugin, Player player)
    {
        super(plugin, player);
        this.playerId = new PlayerID(player.getUniqueId(), player.getName());
    }

    @Override
    public EntityPos getPosition()
    {
        return plugin.entityPos(sender.getLocation());
    }

    @Override
    public boolean isPlayer()
    {
        return true;
    }

    @Override
    public PlayerID getPlayerId()
    {
        return playerId;
    }
}
