package br.com.gamemods.minecity.commands;

import br.com.gamemods.minecity.api.Async;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.StringUtil;
import br.com.gamemods.minecity.api.command.*;
import br.com.gamemods.minecity.api.shape.Shape;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Island;
import br.com.gamemods.minecity.structure.Plot;
import br.com.gamemods.minecity.structure.Selection;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlotCommand
{
    @Slow
    @Async
    @Command(value = "plot.create", console = false, args = @Arg(name = "plot name", sticky = true))
    public static CommandResult<?> create(CommandEvent cmd) throws DataSourceException
    {
        Selection selection = cmd.sender.getSelection(cmd.position.world);
        if(selection.isIncomplete())
        {
            cmd.sender.getServer().callSyncMethod(cmd.sender::giveSelectionTool);
            return new CommandResult<>(new Message("cmd.plot.create.no-selection",
                    "Select the plot area and then execute this command again."
            ), true);
        }

        String name = String.join(" ", cmd.args);
        String identityName = StringUtil.identity(name);
        if(identityName.length() < 3)
            return new CommandResult<>(new Message("cmd.plot.create.name-too-short",
                    "Please type a bigger name"
            ));

        Shape shape = selection.toShape();
        Island island = cmd.getChunk().getIsland().orElse(null);
        if(island == null)
            return new CommandResult<>(new Message("cmd.plot.create.not-claimed", "You are not inside a city"));

        if(!cmd.sender.getPlayerId().equals(island.getCity().getOwner()))
            return new CommandResult<>(new Message("cmd.plot.create.no-permission",
                    "You don't have permission to create plots inside ${city}",
                    new Object[]{"city", island.getCity().getName()}
            ));

        if(!shape.contains(cmd.position.getBlock()))
            return new CommandResult<>(new Message("cmd.plot.create.outside",
                    "Stand inside the plot and execute this command again."
            ));

        Optional<Plot> conflict = island.getCity().getPlot(name);
        if(conflict.isPresent())
            return new CommandResult<>(new Message("cmd.plot.create.conflict",
                    "The name ${name} conflicts with ${conflict}",
                    new Object[][]{
                            {"name", name}, {"conflict", conflict.get().getName()}
                    }
            ));

        Optional<ChunkPos> unclaimed = shape.chunks(selection.world)
                .filter(c ->
                        !cmd.mineCity.getOrFetchChunkUnchecked(c)
                                .flatMap(ClaimedChunk::getIsland)
                                .filter(island::equals)
                                .isPresent()
                ).findAny();

        if(unclaimed.isPresent())
            return new CommandResult<>(new Message("cmd.plot.create.overlaps.unclaimed",
                    "The selected area overlaps an unclaimed chunk located at: X: ${x} and Z: ${z}",
                    new Object[][]{
                            {"x", unclaimed.get().x}, {"z", unclaimed.get().z}
                    }
            ));

        List<Plot> overlaps = island.getPlots().stream().filter(p -> p.getShape().overlaps(shape))
                .collect(Collectors.toList());

        if(!overlaps.isEmpty())
        {
            if(overlaps.size() > 1)
                return new CommandResult<>(new Message("cmd.plot.create.overlaps.plots",
                        "The selected area overlaps ${count} plots: ${plots}",
                        new Object[][]{
                                {"count", overlaps.size()},
                                {"plots", Message.list(
                                        overlaps.stream().map(Plot::getName).sorted()
                                                .map(Message::new).toArray(Message[]::new)
                                )}
                        }
                ));
            else
                return new CommandResult<>(new Message("cmd.plot.create.overlaps.plot",
                        "The selected area overlaps the plot ${name}",
                        new Object[]{"name", overlaps.get(0).getName()}
                ));
        }

        Plot plot = island.createPlot(name, null, cmd.position.getBlock(), shape);
        return new CommandResult<>(new Message("cmd.plot.create.success",
                "The plot ${name} was created successfully",
                new Object[]{"name", plot.getName()}
        ), plot);
    }

    @Slow
    @Async
    @Command(value = "plot.rename", console = false, args = @Arg(name = "new name", sticky = true))
    public static CommandResult<?> rename(CommandEvent cmd) throws DataSourceException
    {
        Plot plot = cmd.mineCity.getPlot(cmd.position.getBlock()).orElse(null);
        if(plot == null)
            return new CommandResult<>(new Message("cmd.plot.rename.not-claimed", "You are not inside a plot"));

        if(!cmd.sender.getPlayerId().equals(plot.owner()))
            return new CommandResult<>(new Message("cmd.plot.rename.no-permission",
                    "You don't have permission to rename the plot ${plot}",
                    new Object[]{"plot", plot.getName()}
            ));

        String name = String.join(" ", cmd.args);
        String identity = StringUtil.identity(name);
        if(identity.length() < 3)
            return new CommandResult<>(new Message("cmd.plot.rename.name-too-short", "Please type a bigger name"));

        if(name.equals(plot.getName()))
            return new CommandResult<>(new Message("cmd.plot.rename.already-named",
                    "The plot is already named ${name}",
                    new Object[]{"name", name}
            ));

        Optional<Plot> conflict = plot.getCity().getPlot(identity);
        if(conflict.isPresent())
            return new CommandResult<>(new Message("cmd.plot.rename.conflict",
                    "The name ${name} conflicts with ${conflict}",
                    new Object[][]{
                            {"name", name}, {"conflict", conflict.get().getName()}
                    }
            ));

        String old = plot.getName();
        plot.setName(name);
        return new CommandResult<>(new Message("cmd.plot.rename.success",
                "The plot ${old} is now named ${name}",
                new Object[][]{
                        {"old",old}, {"new",name}
                }
        ), true);
    }

    @Command(value = "plot.return", console = false)
    public static CommandResult<?> returnPlot(CommandEvent cmd)
    {
        Plot plot = cmd.mineCity.getPlot(cmd.position.getBlock()).orElse(null);
        if(plot == null)
            return new CommandResult<>(new Message("cmd.plot.return.not-claimed", "You are not inside a plot"));

        if(!plot.getOwner().isPresent())
            return new CommandResult<>(new Message("cmd.plot.return.already", "The plot ${plot} is already owned by the mayor of the city ${city}",
                    new Object[][]{
                            {"plot", plot.getName()}, {"city", plot.getCity().getName()}
                    }));

        if(!cmd.sender.getPlayerId().equals(plot.owner()))
            return new CommandResult<>(new Message("cmd.plot.return.no-permission",
                    "You don't have permission to return the ownership of ${plot} to the city ${city}",
                    new Object[][]{
                            {"plot", plot.getName()},
                            {"city", plot.getCity().getName()}
                    }));

        String code = cmd.sender.confirm(sender -> {
            plot.setOwner(null);
            return new CommandResult<>(new Message("cmd.plot.return.success",
                    "The plot ${plot} has returned to the city ${city}",
                    new Object[][]{
                            {"plot", plot.getName()},
                            {"city", plot.getCity().getName()}
                    }), true);
        });

        return new CommandResult<>(new Message("cmd.plot.return.confirm",
                "<msg>You are about to return the plot ${plot} and everything that is in it to the city ${city}, all permissions will be reset " +
                "and you'll no longer be able to control the plot unless you have the appropriate city city's permission or the mayor grants them later, " +
                "<b>you'll not be refunded by this action</b>. If you are sure about this, type /plot confirm ${code}</msg>",
                new Object[][]{
                        {"plot", plot.getName()},
                        {"city", plot.getCity().getName()},
                        {"code", code}
                }), code);
    }

    @Slow
    @Async
    @Command(value = "plot.transfer", console = false, args = @Arg(name = "player name", type = Arg.Type.PLAYER))
    public static CommandResult<?> transfer(CommandEvent cmd) throws DataSourceException
    {
        Plot plot = cmd.mineCity.getPlot(cmd.position.getBlock()).orElse(null);
        if(plot == null)
            return new CommandResult<>(new Message("cmd.plot.transfer.not-claimed", "You are not inside a plot"));

        PlayerID senderId = cmd.sender.getPlayerId();
        if(!senderId.equals(plot.owner()))
            return new CommandResult<>(new Message("cmd.plot.transfer.no-permission",
                    "You don't have permission to transfer the plot ${plot}",
                    new Object[]{"plot", plot.getName()}
            ));

        if(cmd.args.isEmpty())
            return new CommandResult<>(new Message("cmd.plot.transfer.player.empty",
                    "Type a player name"));

        if(cmd.args.size() > 1)
            return new CommandResult<>(new Message("cmd.plot.transfer.too-many-args",
                    "Player names does not have spaces"
        ));

        String playerName = cmd.args.get(0);
        PlayerID target = cmd.mineCity.findPlayer(playerName).orElse(null);
        if(target == null)
            return new CommandResult<>(new Message("cmd.plot.transfer.player.not-found",
                    "No player was found with name ${name}",
                    new Object[]{"name",playerName}
            ));

        if(target.equals(senderId))
        {
            if(plot.getOwner().isPresent())
                return new CommandResult<>(new Message("cmd.plot.transfer.self.already",
                        "You already own the plot ${plot}",
                        new Object[]{"plot", plot.getName()}
                ));

            plot.setOwner(senderId);
            return new CommandResult<>(new Message("cmd.plot.transfer.self.success",
                    "The plot ${plot} is now your personal plot",
                    new Object[]{"plot", plot.getName()}
            ), senderId);
        }

        if(target.equals(plot.getCity().getOwner()))
        {
            String code = cmd.sender.confirm(sender -> {
                plot.setOwner(target);
                return new CommandResult<>(new Message("cmd.plot.transfer.mayor.success",
                        "The plot ${plot} is now a ${target}'s personal plot.",
                        new Object[][]{
                                {"plot", plot.getName()},
                                {"target", target.getName()}
                        }
                ), true);
            });

            return new CommandResult<>(new Message("cmd.plot.transfer.mayor.confirm",
                    "<msg>You are about to transfer the plot ${plot} and everything that is in it to ${target}, the plot " +
                    "<b>will not be returned to the city</b> " +
                    "and will become a ${target}'s personal plot. You'll not be refunded and you'll not be able to " +
                    "undo this action. If you are sure about it type /plot confirm ${code}</msg>",
                    new Object[][]{
                            {"plot", plot.getName()},
                            {"target", target.getName()},
                            {"code", code}
                    }
            ), code);
        }
        else
        {
            String code = cmd.sender.confirm(sender -> {
                plot.setOwner(target);
                return new CommandResult<>(new Message("cmd.plot.transfer.player.success",
                        "The plot ${plot} is now owned by ${target}",
                        new Object[][]{
                                {"plot", plot.getName()},
                                {"target", target.getName()}
                        }
                ), true);
            });

            return new CommandResult<>(new Message("cmd.plot.transfer.player.confirm",
                    "<msg>You are about to transfer the plot ${plot} and everything that is in it to ${target}. " +
                    "You'll not be refunded and <b>you'll not be able to  undo this action</b>. " +
                    "If you are sure about it type /plot confirm ${code}</msg>",
                    new Object[][]{
                            {"plot", plot.getName()},
                            {"target", target.getName()},
                            {"code", code}
                    }
            ), code);
        }
    }
}
