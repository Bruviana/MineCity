package br.com.gamemods.minecity.bukkit;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.MineCityConfig;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.command.LegacyFormat;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.command.MessageTransformer;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.world.*;
import br.com.gamemods.minecity.bukkit.command.BukkitCommandSender;
import br.com.gamemods.minecity.bukkit.command.BukkitLocatableSender;
import br.com.gamemods.minecity.bukkit.command.BukkitPlayer;
import br.com.gamemods.minecity.bukkit.protection.BlockProtections;
import br.com.gamemods.minecity.bukkit.protection.EntityProtections;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class MineCityBukkit implements Server, Listener
{
    public final ExecutorService loadingTasks;
    public final MineCity mineCity;
    public final BukkitScheduler scheduler;
    public final MineCityPlugin plugin;
    public final Logger logger;
    public final Map<Player, BukkitPlayer> playerMap = new HashMap<>();
    public final Map<PlayerID, BukkitPlayer> playerIdMap = new HashMap<>();
    public final Map<World, WorldDim> worldMap = new HashMap<>();
    public final EntityProtections entityProtections;
    private String selectionToolTitle;
    private List<String> selectionToolLore;

    public MineCityBukkit(MineCityPlugin plugin, MineCityConfig config, MessageTransformer transformer)
    {
        this.plugin = plugin;
        logger = plugin.getLogger();
        scheduler = plugin.getScheduler();

        loadingTasks = Executors.newSingleThreadExecutor();
        mineCity = new MineCity(this, config, transformer);
        PluginManager pluginManager = plugin.getPluginManager();
        pluginManager.registerEvents(new WorldListener(this), plugin);
        pluginManager.registerEvents(entityProtections = new EntityProtections(this), plugin);
        pluginManager.registerEvents(new BlockProtections(this), plugin);
        pluginManager.registerEvents(this, plugin);
        selectionToolTitle = transformer.toLegacy(new Message("tool.selection.title", LegacyFormat.AQUA+"Selection Tool"));
        selectionToolLore = Arrays.asList(transformer.toMultilineLegacy(
                new Message("","<white>${lore}</white>", new Object[]
                        {"lore", new Message("tool.selection.lore","Selects an area in the world")}
                ))
        );
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        player(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        BukkitPlayer removed = playerMap.remove(event.getPlayer());
        if(removed != null)
            playerIdMap.remove(removed.getPlayerId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        Action action = event.getAction();
        if(action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK)
            return;

        ItemStack item = event.getItem();
        if(item == null || item.getType() != Material.WOOD_HOE)
            return;

        ItemMeta meta = item.getItemMeta();
        if(!meta.hasDisplayName() || !meta.hasLore() || !meta.getDisplayName().equals(selectionToolTitle) || !meta.getLore().equals(selectionToolLore))
            return;

        event.setCancelled(true);
        BlockPos block = blockPos(event.getClickedBlock());
        Player player = event.getPlayer();
        BukkitPlayer.BukkitSelection selection = player(player).getSelection(block.world);

        if(player.isSneaking())
        {
            if(action == Action.LEFT_CLICK_BLOCK)
                selection.b = block;
            else
                selection.a = block;

            selection.normalize();
            runAsynchronously(selection::updateDisplay);
        }
        else
            runAsynchronously(()->selection.select(block));
    }

    public void updateGroups()
    {
        Queue<EntityUpdate> entityUpdates = mineCity.entityUpdates;
        EntityUpdate update = entityUpdates.peek();
        if(update == null)
            return;

        update.ticks--;
        if(update.ticks <= 0)
        {
            entityUpdates.poll();
            return;
        }

        //noinspection SuspiciousMethodCalls
        BukkitPlayer bukkitPlayer = playerIdMap.get(update.identity);
        if(bukkitPlayer == null)
            return;

        if(bukkitPlayer.updateGroups(update))
            entityUpdates.poll();
    }

    @Override
    public MineCity getMineCity()
    {
        return mineCity;
    }

    public boolean onCommand(CommandSender sender, String label, String[] args)
    {
        List<String> path = new ArrayList<>(args.length + 1);
        path.add(label);
        path.addAll(Arrays.asList(args));
        mineCity.commands.invoke(sender(sender), path);
        return true;
    }

    @NotNull
    public BukkitPlayer player(@NotNull Player player)
    {
        return playerMap.computeIfAbsent(player, p -> {
            BukkitPlayer bukkitPlayer = new BukkitPlayer(this, p);
            playerIdMap.put(bukkitPlayer.getPlayerId(), bukkitPlayer);
            return bukkitPlayer;
        });
    }

    @NotNull
    private br.com.gamemods.minecity.api.command.CommandSender sender(@NotNull CommandSender sender)
    {
        if(sender instanceof Player)
            return player((Player) sender);
        if(sender instanceof Entity || sender instanceof BlockCommandSender)
            return new BukkitLocatableSender<>(this, sender);
        return new BukkitCommandSender<>(this, sender);
    }

    @NotNull
    public WorldDim world(@NotNull World world)
    {
        //noinspection deprecation
        return worldMap.computeIfAbsent(world, w-> new WorldDim(w, w.getEnvironment().getId(), w.getName()));
    }

    @NotNull
    public Optional<World> world(@NotNull WorldDim world)
    {
        World other = plugin.getServer().getWorld(world.dir);
        world.instance = other;
        return Optional.ofNullable(other);
    }

    @NotNull
    public ChunkPos chunk(@NotNull Location loc)
    {
        return new ChunkPos(world(loc.getWorld()), loc.getBlockX()>>4, loc.getBlockZ()>>4);
    }

    @NotNull
    public ChunkPos chunk(@NotNull Block loc)
    {
        return new ChunkPos(world(loc.getWorld()), loc.getX()>>4, loc.getZ()>>4);
    }

    @NotNull
    public ChunkPos chunk(@NotNull Chunk chunk)
    {
        ChunkPos pos = new ChunkPos(world(chunk.getWorld()), chunk.getX(), chunk.getZ());
        pos.instance = chunk;
        return pos;
    }

    @NotNull
    public BlockPos blockPos(@NotNull Block block)
    {
        return new BlockPos(world(block.getWorld()), block.getX(), block.getY(), block.getZ());
    }

    @NotNull
    public BlockPos blockPos(@NotNull BlockState block)
    {
        return new BlockPos(world(block.getWorld()), block.getX(), block.getY(), block.getZ());
    }

    @NotNull
    public BlockPos blockPos(@NotNull BlockPos base, @NotNull BlockState block)
    {
        return block.getWorld().equals(base.world.instance)?
                new BlockPos(base, block.getX(), block.getY(), block.getZ())
                : blockPos(block)
                ;
    }

    @NotNull
    public BlockPos blockPos(@NotNull BlockPos base, @NotNull Block block)
    {
        return block.getWorld().equals(base.world.instance)?
                new BlockPos(base, block.getX(), block.getY(), block.getZ())
                : blockPos(block)
                ;
    }

    @NotNull
    public BlockPos blockPos(@NotNull BlockPos base, @NotNull Location loc)
    {
        return loc.getWorld().equals(base.world.instance)?
                new BlockPos(base, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())
                : blockPos(loc)
                ;
    }

    @NotNull
    public BlockPos blockPos(@NotNull Location location)
    {
        return new BlockPos(world(location.getWorld()), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @NotNull
    public EntityPos entityPos(@NotNull Location loc)
    {
        return new EntityPos(world(loc.getWorld()), loc.getX(), loc.getY(), loc.getZ(), loc.getPitch(), loc.getYaw());
    }

    @NotNull
    public Optional<Location> location(@NotNull BlockPos pos)
    {
        return world(pos.world).map(world-> new Location(world, pos.x+0.5, pos.y+0.5, pos.z+0.5));
    }

    @NotNull
    public Optional<Location> location(@NotNull EntityPos pos)
    {
        return world(pos.world).map(world -> new Location(world, pos.x, pos.y, pos.z, pos.yaw, pos.pitch));
    }

    @NotNull
    public FlagHolder getFlagHolder(@NotNull Location loc)
    {
        return mineCity.provideChunk(chunk(loc)).getFlagHolder(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    @Override
    public Optional<PlayerID> getPlayerId(@NotNull String name)
    {
        Player player = plugin.getServer().getPlayer(name);
        if(player == null)
            return Optional.empty();
        return Optional.of(new PlayerID(player.getUniqueId(), player.getName()));
    }

    @Override
    public Stream<String> getOnlinePlayerNames()
    {
        return plugin.getServer().getOnlinePlayers().stream().map(Player::getName);
    }

    @Override
    public Stream<PlayerID> getOnlinePlayers()
    {
        return plugin.getServer().getOnlinePlayers().stream().map(p-> new PlayerID(p.getUniqueId(), p.getName()));
    }

    @Override
    public <R> Future<R> callSyncMethod(@NotNull Callable<R> callable)
    {
        return scheduler.callSyncMethod(plugin, callable);
    }

    @Override
    public void runAsynchronously(@NotNull Runnable runnable)
    {
        scheduler.runTaskAsynchronously(plugin, runnable);
    }
}
