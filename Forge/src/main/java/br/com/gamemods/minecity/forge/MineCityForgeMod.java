package br.com.gamemods.minecity.forge;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.MineCityConfig;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.api.world.WorldProvider;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.forge.command.ForgeCommandSender;
import br.com.gamemods.minecity.forge.command.ForgePlayer;
import br.com.gamemods.minecity.forge.command.RootCommand;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
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
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Mod(modid = "minecity", name = "MineCity", version = "1.0-SNAPSHOT", acceptableRemoteVersions = "*")
public class MineCityForgeMod implements Server, WorldProvider
{
    public MinecraftServer server;
    public MineCity mineCity;
    private MineCityConfig config;
    private Path worldContainer;

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

    @EventHandler
    public void onServerStart(FMLServerAboutToStartEvent event) throws IOException, DataSourceException, SAXException
    {
        server = event.getServer();
        worldContainer = Paths.get(server.getFolderName());

        MinecraftForge.EVENT_BUS.register(this);
        mineCity = new MineCity(this, config);
        mineCity.worldProvider = this;
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
        mineCity.loadChunk(new ChunkPos(world(event.world), chunk.xPosition, chunk.zPosition));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChunkUnload(ChunkEvent.Unload event) throws DataSourceException
    {
        if(event.world.isRemote)
            return;

        Chunk chunk = event.getChunk();
        mineCity.unloadChunk(new ChunkPos(world(event.world), chunk.xPosition, chunk.zPosition));
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

    public ChunkPos chunk(Chunk chunk)
    {
        return new ChunkPos(world(chunk.worldObj), chunk.xPosition, chunk.zPosition);
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

        WorldServer worldServer = server.worldServerForDimension(world.dim);
        if(worldServer == null || !world.equals(world(worldServer)))
            return null;

        world.instance = worldServer;
        return worldServer;
    }

    @Nullable
    @Override
    public WorldDim getWorld(int dim, @NotNull String dir)
    {
        WorldServer worldServer = server.worldServerForDimension(dim);
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
            return new ForgePlayer(this, (EntityPlayer) sender);
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
}
