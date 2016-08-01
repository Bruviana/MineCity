package br.com.gamemods.minecity.forge;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.MineCityConfig;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.ChunkProvider;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.api.world.WorldProvider;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.forge.accessors.IChunk;
import br.com.gamemods.minecity.forge.accessors.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.accessors.IWorldServer;
import br.com.gamemods.minecity.forge.command.ForgeCommandSender;
import br.com.gamemods.minecity.forge.command.ForgePlayer;
import br.com.gamemods.minecity.forge.command.RootCommand;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Inconsistency;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Stream;

@Mod(modid = "minecity", name = "MineCity", version = "1.0-SNAPSHOT", acceptableRemoteVersions = "*")
public class MineCityForgeMod implements Server, WorldProvider, ChunkProvider
{
    public MinecraftServer server;
    public MineCity mineCity;
    private MineCityConfig config;
    private Path worldContainer;
    private ExecutorService executors;
    private final ConcurrentLinkedQueue<FutureTask> syncTasks = new ConcurrentLinkedQueue<>();

    @EventHandler
    public void onPreInit(FMLPreInitializationEvent event)
    {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();

        this.config = new MineCityConfig();
        this.config.locale = Locale.forLanguageTag(config.get("general", "language", "en").getString());
        this.config.dbUrl = config.get("database", "url", this.config.dbUrl).getString();
        this.config.dbUser = Optional.of(config.get("database", "user", "").getString())
                .filter(u->!u.isEmpty()).orElse(null);

        this.config.dbPass = Optional.of(config.get("database", "pass", "").getString())
                .filter(p->!p.isEmpty()).map(String::getBytes).orElse(null);

        config.save();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Stream<String> getOnlinePlayerNames()
    {
        return ((List<EntityPlayer>)server.getConfigurationManager().playerEntityList).stream()
                .map(EntityPlayer::getCommandSenderName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Stream<PlayerID> getOnlinePlayers()
    {
        return ((List<EntityPlayer>)server.getConfigurationManager().playerEntityList).stream()
                .map(e-> this.player(e).getPlayerId());
    }

    @Override
    public void runAsynchronously(Runnable runnable)
    {
        executors.submit(runnable);
    }

    @Override
    public <R> Future<R> callSyncMethod(Callable<R> callable)
    {
        FutureTask<R> future = new FutureTask<>(callable);
        syncTasks.add(future);
        return future;
    }

    @EventHandler
    public void onServerStart(FMLServerAboutToStartEvent event) throws IOException, DataSourceException, SAXException
    {
        executors = Executors.newCachedThreadPool();
        server = event.getServer();
        worldContainer = Paths.get(server.getFolderName());

        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
        mineCity = new MineCity(this, config);
        mineCity.worldProvider = Optional.of(this);
        mineCity.commands.parseXml(MineCity.class.getResourceAsStream("/assets/minecity/commands.xml"));
        mineCity.messageTransformer.parseXML(MineCity.class.getResourceAsStream("/assets/minecity/messages.xml"));
        mineCity.dataSource.initDB();
    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event)
    {
        mineCity.commands.getRootCommands().stream()
                .map(name->mineCity.commands.get(name).get())
                .map(r->r.command).distinct()
                .forEach(i-> event.registerServerCommand(new RootCommand<>(this, i)));
    }

    @EventHandler
    public void onServerStop(FMLServerStoppedEvent event) throws DataSourceException
    {
        MinecraftForge.EVENT_BUS.unregister(this);
        executors.shutdown();
        boolean terminated = false;
        try
        {
            terminated = executors.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch(InterruptedException ignored)
        {}

        if(!terminated)
            executors.shutdownNow();

        mineCity.dataSource.close();
        mineCity = null;
        worldContainer = null;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChunkLoad(ChunkEvent.Load event) throws DataSourceException
    {
        if(event.world.isRemote)
            return;

        Chunk chunk = event.getChunk();
        ChunkPos pos = new ChunkPos(world(chunk.worldObj), chunk.xPosition, chunk.zPosition);
        pos.instance = chunk;
        if(chunk instanceof IChunk)
            ((IChunk) chunk).setMineCityClaim(new ClaimedChunk(Inconsistency.INSTANCE, pos));

        mineCity.loadChunk(pos);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChunkUnload(ChunkEvent.Unload event) throws DataSourceException
    {
        if(event.world.isRemote)
            return;

        mineCity.unloadChunk(chunk(event.getChunk()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onWorldLoad(WorldEvent.Load event) throws DataSourceException
    {
        if(event.world.isRemote)
            return;

        mineCity.loadNature(world(event.world));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onWorldUnload(WorldEvent.Unload event) throws DataSourceException
    {
        if(event.world.isRemote)
            return;

        mineCity.unloadNature(world(event.world));

        if(event.world instanceof IWorldServer)
            ((IWorldServer) event.world).setMineCityWorld(null);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END || event.side != Side.SERVER )
            return;

        player(event.player).tick();
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END || event.side != Side.SERVER)
            return;

        mineCity.reloadQueuedChunk();
        Iterator<FutureTask> iterator = syncTasks.iterator();
        while(iterator.hasNext())
        {
            FutureTask task = iterator.next();
            iterator.remove();
            task.run();
        }
    }

    public ChunkPos chunk(Chunk chunk)
    {
        if(chunk instanceof IChunk)
        {
            ChunkPos pos = ((IChunk) chunk).getMineCityChunk();
            if(pos != null)
                return pos;
        }

        ChunkPos pos = new ChunkPos(world(chunk.worldObj), chunk.xPosition, chunk.zPosition);
        pos.instance = chunk;
        return pos;
    }

    public ForgePlayer player(EntityPlayer player)
    {
        if(player instanceof IEntityPlayerMP)
        {
            IEntityPlayerMP cast = ((IEntityPlayerMP) player);
            ForgePlayer cache = cast.getMineCityPlayer();
            if(cache != null)
                return cache;

            cache = new ForgePlayer(this, player);
            cast.setMineCityPlayer(cache);
            return cache;
        }

        return new ForgePlayer(this, player);
    }

    @Nullable
    @Override
    public ChunkPos getChunk(@NotNull WorldDim dim, int x, int z)
    {
        WorldServer world = world(dim);
        if(world == null)
            return null;

        Chunk chunk = getLoadedChunk(world, x, z);
        if(!(chunk instanceof IChunk))
            return null;

        ChunkPos pos = ((IChunk) chunk).getMineCityChunk();
        if(pos != null)
            return pos;

        pos = new ChunkPos(dim, x, z);
        pos.instance = chunk;
        return pos;
    }

    @Override
    public boolean setClaim(@NotNull ClaimedChunk claim)
    {
        ChunkPos pos = claim.chunk;
        if(pos.instance instanceof IChunk)
        {
            IChunk chunk = (IChunk) pos.instance;
            if(chunk.isMineCityChunkValid())
            {
                chunk.setMineCityClaim(claim);
                return true;
            }
        }

        WorldServer worldServer = world(pos.world);
        if(!(worldServer instanceof IWorldServer))
            return false;

        Chunk forgeChunk = getLoadedChunk(worldServer, pos.x, pos.z);
        if(!(forgeChunk instanceof IChunk))
            return false;

        pos.instance = forgeChunk;
        ((IChunk) forgeChunk).setMineCityClaim(claim);
        return true;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public Stream<ClaimedChunk> loadedChunks()
    {
        WorldServer overWorld = DimensionManager.getWorld(0);
        if(!(overWorld instanceof IWorldServer))
            return Stream.empty();

        List overWorldChunks = overWorld.theChunkProviderServer.loadedChunks;
        if(overWorldChunks.isEmpty() || !(overWorldChunks.get(0) instanceof IChunk))
            return Stream.empty();

        Stream<IChunk> composite = overWorld.theChunkProviderServer.loadedChunks.stream();
        for(WorldServer worldServer : DimensionManager.getWorlds())
        {
            if(worldServer == overWorld) continue;
            composite = Stream.concat(composite, overWorld.theChunkProviderServer.loadedChunks.stream());
        }

        return composite.map(IChunk::getMineCityClaim);
    }

    @Nullable
    public Chunk getLoadedChunk(WorldServer world, int x, int z)
    {
        return (Chunk) world.theChunkProviderServer.loadedChunkHashMap.getValueByKey(
                ChunkCoordIntPair.chunkXZ2Int(x, z)
        );
    }

    @Nullable
    @Override
    public ClaimedChunk getClaim(@NotNull WorldDim dim, int x, int z)
    {
        WorldServer world = world(dim);
        if(world == null)
            return null;

        Chunk chunk = getLoadedChunk(world, x, z);
        if(!(chunk instanceof IChunk))
            return null;

        return ((IChunk) chunk).getMineCityClaim();
    }

    @Nullable
    @Override
    public ClaimedChunk getClaim(@NotNull ChunkPos pos)
    {
        if(pos.instance instanceof IChunk)
        {
            IChunk chunk = (IChunk) pos.instance;
            Chunk fChunk = (Chunk) chunk;
            if(fChunk.worldObj instanceof IWorldServer && ((IWorldServer) fChunk.worldObj).getMineCityWorld() != null)
            {
                ClaimedChunk claim = chunk.getMineCityClaim();
                if(claim != null)
                    return claim;
            }
        }

        WorldServer world = world(pos.world);
        if(world == null)
            return null;

        Chunk chunk = getLoadedChunk(world, pos.x, pos.z);
        if(!(chunk instanceof IChunk))
            return null;

        pos.instance = chunk;
        return  ((IChunk) chunk).getMineCityClaim();
    }

    public WorldDim world(World world)
    {
        boolean impl = world instanceof IWorldServer;
        if(impl)
        {
            WorldDim cached = ((IWorldServer) world).getMineCityWorld();
            if(cached != null)
                return cached;
        }

        Path worldPath = worldContainer.resolve(Optional.ofNullable(world.provider.getSaveFolder()).orElse(""));
        WorldDim worldDim = new WorldDim(world.provider.dimensionId, worldPath.toString());
        worldDim.instance = world;

        if(impl)
            ((IWorldServer) world).setMineCityWorld(worldDim);

        return worldDim;
    }

    @Nullable
    public WorldServer world(WorldDim world)
    {
        if(world.instance instanceof WorldServer)
        {
            if(((IWorldServer) world.instance).getMineCityWorld() != null)
                return (WorldServer) world.instance;
        }

        WorldServer worldServer = DimensionManager.getWorld(world.dim);
        if(worldServer == null || !world.equals(world(worldServer)))
            return null;

        world.instance = worldServer;
        return worldServer;
    }

    @Nullable
    @Override
    public WorldDim getWorld(int dim, @NotNull String dir)
    {
        WorldServer worldServer = DimensionManager.getWorld(dim);
        if(!(worldServer instanceof IWorldServer))
            return null;

        WorldDim worldDim = ((IWorldServer)worldServer).getMineCityWorld();
        if(worldDim == null)
            worldDim = world(worldServer);

        if(dir.equals(worldDim.dir))
            return worldDim;

        return null;
    }

    public CommandSender sender(ICommandSender sender)
    {
        if(sender instanceof EntityPlayer)
            return player((EntityPlayer) sender);
        return new ForgeCommandSender<>(this, sender);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<PlayerID> getPlayerId(String name)
    {
        for(EntityPlayer player : (List<EntityPlayer>) server.getConfigurationManager().playerEntityList)
        {
            String playerName = player.getCommandSenderName();
            if(name.equals(playerName))
                return Optional.of(new PlayerID(player.getUniqueID(), playerName));
        }

        return Optional.empty();
    }

    @Nullable
    @Override
    public Optional<ChunkProvider> getChunkProvider()
    {
        return Optional.of(this);
    }
}
