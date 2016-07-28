package br.com.gamemods.minecity.datasource.api;

import br.com.gamemods.minecity.api.permission.Group;
import br.com.gamemods.minecity.structure.Island;

import java.util.Collection;

public final class CityCreationResult
{
    public final ICityStorage storage;
    public final Island island;
    public final Collection<Group> groups;

    public CityCreationResult(ICityStorage storage, Island island, Collection<Group> groups)
    {
        this.storage = storage;
        this.island = island;
        this.groups = groups;
    }
}
