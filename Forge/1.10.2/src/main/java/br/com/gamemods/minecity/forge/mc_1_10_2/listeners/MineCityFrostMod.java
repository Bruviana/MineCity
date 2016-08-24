package br.com.gamemods.minecity.forge.mc_1_10_2.listeners;

import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.LegacyFormat;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.forge.base.ModConstants;
import br.com.gamemods.minecity.forge.base.command.RootCommand;
import br.com.gamemods.minecity.forge.mc_1_10_2.MineCityFrost;
import br.com.gamemods.minecity.forge.mc_1_10_2.command.FrostTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.protection.vanilla.FrostBlockProtections;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import org.xml.sax.SAXException;

import java.io.IOException;

@Mod(modid = ModConstants.MOD_ID, name = ModConstants.MOD_ID, version = ModConstants.MOD_VERSION, acceptableRemoteVersions = "*")
public class MineCityFrostMod
{
    private MineCityFrost forge;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) throws IOException, SAXException
    {
        forge = new MineCityFrost();

        LegacyFormat.BLACK.server = TextFormatting.BLACK;
        LegacyFormat.DARK_BLUE.server = TextFormatting.DARK_BLUE;
        LegacyFormat.DARK_GREEN.server = TextFormatting.DARK_GREEN;
        LegacyFormat.DARK_AQUA.server = TextFormatting.DARK_AQUA;
        LegacyFormat.DARK_RED.server = TextFormatting.DARK_RED;
        LegacyFormat.DARK_PURPLE.server = TextFormatting.DARK_PURPLE;
        LegacyFormat.GOLD.server = TextFormatting.GOLD;
        LegacyFormat.GRAY.server = TextFormatting.GRAY;
        LegacyFormat.DARK_GRAY.server = TextFormatting.DARK_GRAY;
        LegacyFormat.BLUE.server = TextFormatting.BLUE;
        LegacyFormat.GREEN.server = TextFormatting.GREEN;
        LegacyFormat.AQUA.server = TextFormatting.AQUA;
        LegacyFormat.RED.server = TextFormatting.RED;
        LegacyFormat.LIGHT_PURPLE.server = TextFormatting.LIGHT_PURPLE;
        LegacyFormat.YELLOW.server = TextFormatting.YELLOW;
        LegacyFormat.WHITE.server = TextFormatting.WHITE;
        LegacyFormat.RESET.server = TextFormatting.RESET;
        LegacyFormat.MAGIC.server = TextFormatting.OBFUSCATED;
        LegacyFormat.BOLD.server = TextFormatting.BOLD;
        LegacyFormat.STRIKE.server = TextFormatting.STRIKETHROUGH;
        LegacyFormat.UNDERLINE.server = TextFormatting.UNDERLINE;
        LegacyFormat.ITALIC.server = TextFormatting.ITALIC;

        forge.onPreInit(new Configuration(event.getSuggestedConfigurationFile()), event.getModLog(), new FrostTransformer());
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new FrostTickListener(forge));
        MinecraftForge.EVENT_BUS.register(new FrostToolListener(forge));
        MinecraftForge.EVENT_BUS.register(new FrostWorldListener(forge));
        MinecraftForge.EVENT_BUS.register(new FrostBlockProtections(forge));
    }

    @Slow
    @Mod.EventHandler
    public void onServerStart(FMLServerAboutToStartEvent event) throws IOException, DataSourceException, SAXException
    {
        forge.onServerAboutToStart(event.getServer());
    }

    @Mod.EventHandler
    public void onServerStart(FMLServerStartingEvent event)
    {
        forge.mineCity.commands.getRootCommands().stream()
                .map(name->forge.mineCity.commands.get(name).get())
                .map(r->r.command).distinct()
                .forEach(i-> event.registerServerCommand(new RootCommand<>(forge, i)));
    }

    @Slow
    @Mod.EventHandler
    public void onServerStop(FMLServerStoppedEvent event) throws DataSourceException
    {
        forge.onServerStop();
    }
}
