package br.com.gamemods.minecity.bukkit;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.MineCityConfig;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.LegacyFormat;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.command.MessageTransformer;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;

public class MineCityPlugin extends JavaPlugin
{
    private MineCityBukkit instance;
    private BukkitTask reloadTask;

    @Slow
    @Override
    public void onEnable()
    {
        LegacyFormat.BLACK.server = ChatColor.BLACK;
        LegacyFormat.DARK_BLUE.server = ChatColor.DARK_BLUE;
        LegacyFormat.DARK_GREEN.server = ChatColor.DARK_GREEN;
        LegacyFormat.DARK_AQUA.server = ChatColor.DARK_AQUA;
        LegacyFormat.DARK_RED.server = ChatColor.DARK_RED;
        LegacyFormat.DARK_PURPLE.server = ChatColor.DARK_PURPLE;
        LegacyFormat.GOLD.server = ChatColor.GOLD;
        LegacyFormat.GRAY.server = ChatColor.GRAY;
        LegacyFormat.DARK_GRAY.server = ChatColor.DARK_GRAY;
        LegacyFormat.BLUE.server = ChatColor.BLUE;
        LegacyFormat.GREEN.server = ChatColor.GREEN;
        LegacyFormat.AQUA.server = ChatColor.AQUA;
        LegacyFormat.RED.server = ChatColor.RED;
        LegacyFormat.LIGHT_PURPLE.server = ChatColor.LIGHT_PURPLE;
        LegacyFormat.YELLOW.server = ChatColor.YELLOW;
        LegacyFormat.WHITE.server = ChatColor.WHITE;
        LegacyFormat.RESET.server = ChatColor.RESET;
        LegacyFormat.MAGIC.server = ChatColor.MAGIC;
        LegacyFormat.BOLD.server = ChatColor.BOLD;
        LegacyFormat.STRIKE.server = ChatColor.STRIKETHROUGH;
        LegacyFormat.UNDERLINE.server = ChatColor.UNDERLINE;
        LegacyFormat.ITALIC.server = ChatColor.ITALIC;

        try
        {
            saveDefaultConfig();
            reloadConfig();

            @SuppressWarnings("SpellCheckingInspection")
            FileConfiguration yaml = getConfig();
            MineCityConfig config = new MineCityConfig();
            config.dbUrl = yaml.getString("database.url", config.dbUrl);
            config.dbUser = Optional.ofNullable(yaml.getString("database.user")).filter(u-> !u.isEmpty()).orElse(null);
            config.dbPass = Optional.ofNullable(yaml.getString("database.pass")).filter(p-> !p.isEmpty()).map(String::getBytes).orElse(null);
            config.locale = Locale.forLanguageTag(Optional.ofNullable(yaml.getString("language")).filter(l->!l.isEmpty()).orElse("en"));

            for(PermissionFlag flag: PermissionFlag.values())
            {
                boolean allow = yaml.getBoolean("permissions.default.nature."+flag+".allow", flag.defaultNature);
                String msg = yaml.getString("permissions.default.nature."+flag+".allow", "");
                if(!allow)
                {
                    if(msg.isEmpty())
                        config.defaultNatureFlags.deny(flag);
                    else
                        config.defaultNatureFlags.deny(flag, new Message("", msg));
                }

                allow = yaml.getBoolean("permissions.default.city."+flag+".allow", flag.defaultNature);
                msg = yaml.getString("permissions.default.city."+flag+".allow", "");
                if(!allow)
                {
                    if(msg.isEmpty())
                        config.defaultCityFlags.deny(flag);
                    else
                        config.defaultCityFlags.deny(flag, new Message("", msg));
                }

                allow = yaml.getBoolean("permissions.default.plot."+flag+".allow", flag.defaultNature);
                msg = yaml.getString("permissions.default.plot."+flag+".allow", "");
                if(!allow)
                {
                    if(msg.isEmpty())
                        config.defaultPlotFlags.deny(flag);
                    else
                        config.defaultPlotFlags.deny(flag, new Message("", msg));
                }
            }

            MessageTransformer transformer = new MessageTransformer();
            transformer.parseXML(MineCity.class.getResourceAsStream("/assets/minecity/messages-en.xml"));
            instance = new MineCityBukkit(this, config, transformer);
            instance.mineCity.dataSource.initDB();
            instance.mineCity.commands.parseXml(MineCity.class.getResourceAsStream("/assets/minecity/commands-en.xml"));

            reloadTask = getScheduler().runTaskTimer(this, instance.mineCity::reloadQueuedChunk, 1, 1);
        }
        catch(Exception e)
        {
            getLogger().log(Level.SEVERE, "Failed to load MineCity, shutting down the server", e);
            Bukkit.shutdown();
        }
    }

    @Slow
    @Override
    public void onDisable()
    {
        try
        {
            instance.mineCity.dataSource.close();
        }
        catch(DataSourceException e)
        {
            e.printStackTrace();
        }
        reloadTask.cancel();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        return instance.onCommand(sender, label, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        String[] path = new String[args.length+1];
        path[0] = label;
        System.arraycopy(args, 0, path, 1, args.length);
        return instance.mineCity.commands.complete(path);
    }

    public BukkitScheduler getScheduler()
    {
        return getServer().getScheduler();
    }

    public PluginManager getPluginManager()
    {
        return getServer().getPluginManager();
    }
}
