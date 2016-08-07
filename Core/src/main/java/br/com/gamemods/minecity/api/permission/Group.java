package br.com.gamemods.minecity.api.permission;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.StringUtil;
import br.com.gamemods.minecity.api.world.EntityUpdate;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.api.ICityStorage;
import br.com.gamemods.minecity.structure.City;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Group implements Identifiable<Integer>
{
    public final int id;
    @NotNull
    private final ICityStorage storage;
    @NotNull
    public final City home;
    @NotNull
    private String name;
    @NotNull
    private String identityName;
    @NotNull
    private final Set<Identity<?>> members;
    private final Set<PlayerID> managers;
    @NotNull
    private final GroupID identity;
    private boolean invalid;

    public Group(@NotNull ICityStorage storage, int id, @NotNull City home, @NotNull String identityName,
                 @NotNull String name, @NotNull Collection<Identity<?>> members, Collection<PlayerID> managers)
    {
        this.id = id;
        this.home = home;
        this.name = name;
        this.identityName = identityName;
        this.members = Collections.newSetFromMap(new ConcurrentHashMap<>(members.size()));
        this.members.addAll(members);
        this.managers = Collections.newSetFromMap(new ConcurrentHashMap<>(managers.size()));
        this.managers.addAll(managers);
        this.storage = storage;
        identity = new GroupID(id, name, home.getName(), home.getId());
    }

    @Slow
    public synchronized void addMember(@NotNull Identity<?> member) throws DataSourceException, IllegalStateException
    {
        if(invalid)
            throw new IllegalStateException();

        if(members.contains(member))
            return;

        if(member.getType() == Identity.Type.GROUP)
            throw new UnsupportedOperationException("Add a group to an other group");

        storage.addMember(this, member);
        members.add(member);
        home.mineCity.entityUpdates.add(new EntityUpdate(member, EntityUpdate.Type.GROUP_ADDED, identity));
    }

    @Slow
    public synchronized void addManager(@NotNull PlayerID manager) throws DataSourceException, IllegalStateException
    {
        if(invalid)
            throw new IllegalStateException();

        if(managers.contains(manager))
            return;

        storage.addManager(this, manager);
        managers.add(manager);
    }

    @Slow
    public synchronized void removeMember(@NotNull Identity<?> member) throws DataSourceException, IllegalStateException
    {
        if(invalid)
            throw new IllegalStateException();

        if(!members.contains(member))
            return;

        storage.removeMember(this, member);
        members.remove(member);
        home.mineCity.entityUpdates.add(new EntityUpdate(member, EntityUpdate.Type.GROUP_REMOVED, identity));
    }

    @Slow
    public synchronized void removeManager(@NotNull PlayerID manager) throws DataSourceException, IllegalStateException
    {
        if(invalid)
            throw new IllegalStateException();

        if(!managers.contains(manager))
            return;

        storage.removeManager(this, manager);
        managers.remove(manager);
    }

    public boolean isMember(@NotNull Identity<?> member)
    {
        return !invalid && members.contains(member);
    }

    public boolean isManager(@NotNull PlayerID id)
    {
        return managers.contains(id);
    }

    @NotNull
    public Set<Identity<?>> getMembers()
    {
        if(invalid)
            return Collections.emptySet();

        return Collections.unmodifiableSet(members);
    }

    public Set<PlayerID> getManagers()
    {
        if(invalid)
            return Collections.emptySet();

        return Collections.unmodifiableSet(managers);
    }

    @Slow
    public synchronized void setName(@NotNull String name) throws DataSourceException
    {
        if(invalid)
            throw new IllegalStateException();

        Group group = home.getGroup(name);
        if(group != null)
            throw new IllegalArgumentException("'"+name+"' conflicts with '"+group.name+"'");

        String id = StringUtil.identity(name);

        storage.setName(this, id, name);

        String oldName = this.identityName;
        identityName = id;
        identity.name = name;
        this.name = name;
        home.updateGroupName(this, oldName);
    }

    @Slow
    public void remove() throws DataSourceException
    {
        if(invalid)
            return;

        storage.deleteGroup(this);
        invalidate();
    }


    public void checkCityValidity()
    {
        if(invalid)
            return;

        if(home.isInvalid())
            invalidate();
    }

    private void invalidate()
    {
        invalid = true;
        members.forEach(m-> home.mineCity.entityUpdates.add(new EntityUpdate(m, EntityUpdate.Type.GROUP_REMOVED, identity)));
        members.clear();
        home.removeInvalidGroups();
    }

    @NotNull
    public String getIdentityName()
    {
        return identityName;
    }

    public boolean isValid()
    {
        return !invalid;
    }

    public boolean isInvalid()
    {
        return invalid;
    }

    public void updateCityName()
    {
        identity.home = home.getName();
    }

    @NotNull
    @Override
    public Integer getUniqueId()
    {
        return id;
    }

    @Override
    @NotNull
    public String getName()
    {
        return name;
    }

    @NotNull
    @Override
    public GroupID getIdentity()
    {
        return identity;
    }
}
