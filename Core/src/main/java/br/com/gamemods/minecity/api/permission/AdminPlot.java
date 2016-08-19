package br.com.gamemods.minecity.api.permission;

import br.com.gamemods.minecity.structure.Plot;
import org.jetbrains.annotations.NotNull;

public class AdminPlot extends ServerAdmins<Plot>
{
    public AdminPlot(Plot plot)
    {
        super(plot, ServerAdmins.INSTANCE.getName());
    }

    @NotNull
    @Override
    public String getName()
    {
        return ServerAdmins.INSTANCE.getName();
    }

    @Override
    public void setName(@NotNull String name)
    {
        ServerAdmins.INSTANCE.setName(name);
    }
}
