package br.com.gamemods.minecity.bukkit.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BukkitCommandSender<S extends CommandSender> implements br.com.gamemods.minecity.api.command.CommandSender
{
    public final MineCityBukkit plugin;
    public final S sender;

    public BukkitCommandSender(MineCityBukkit plugin, S sender)
    {
        this.plugin = plugin;
        this.sender = sender;
    }

    @Override
    public BlockPos getPosition()
    {
        return null;
    }

    @Override
    public boolean isPlayer()
    {
        return false;
    }

    @Override
    public PlayerID getPlayerId()
    {
        return null;
    }

    @Nullable
    @Override
    public Message teleport(@NotNull BlockPos pos)
    {
        return new Message("action.teleport.unsupported", "Unsupported operation");
    }

    @Override
    public void send(Message message)
    {
        sender.sendMessage(plugin.mineCity.messageTransformer.toSimpleText(message));
    }
}
