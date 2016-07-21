package br.com.gamemods.minecity.bukkit.command;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BukkitLocatableSender<S extends CommandSender> extends BukkitCommandSender<S>
{
    public BukkitLocatableSender(MineCityBukkit plugin, S sender)
    {
        super(plugin, sender);
    }

    @Override
    public BlockPos getPosition()
    {
        if(sender instanceof BlockCommandSender)
            return plugin.blockPos(((BlockCommandSender) sender).getBlock().getLocation());
        if(sender instanceof Entity)
            return plugin.blockPos(((Entity) sender).getLocation());
        throw new UnsupportedOperationException("Sender: "+sender);
    }

    @Nullable
    @Override
    public Message teleport(@NotNull BlockPos pos)
    {
        if(sender instanceof Entity)
        {
            Entity entity = (Entity) sender;
            Location current = entity.getLocation();

            Location location = plugin.location(pos).orElse(null);
            if(location == null)
                return new Message("action.teleport.world-not-found",
                        "The destiny world ${name} was not found or is not loaded",
                        new Object[]{"name",pos.world.name()}
                );

            location.setPitch(current.getPitch());
            location.setYaw(current.getYaw());

            if(entity.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND))
                return null;

            return new Message("action.teleport.cancelled", "The teleport were cancelled");
        }

        return super.teleport(pos);
    }

    @Override
    public void send(Message message)
    {
        sender.sendMessage(plugin.mineCity.messageTransformer.toLegacy(message));
    }
}
