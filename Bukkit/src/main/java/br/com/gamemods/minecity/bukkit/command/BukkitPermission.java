package br.com.gamemods.minecity.bukkit.command;

import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.permission.PermissionProvider;
import br.com.gamemods.minecity.permission.PermissionProxy;
import org.bukkit.permissions.Permissible;

public class BukkitPermission implements PermissionProxy
{
    public static final PermissionProvider PROVIDER = mineCity -> new BukkitPermission();

    @Override
    public boolean hasPermission(CommandSender sender, String perm)
    {
        try
        {
            return ((Permissible) sender.getHandler()).hasPermission(perm);
        }
        catch(ClassCastException e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
