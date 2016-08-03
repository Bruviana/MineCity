package br.com.gamemods.minecity.api.permission;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * A flag holder that can provide different denial messages per flag
 */
public class SimpleFlagHolder implements FlagHolder
{
    protected Message defaultMessage = DEFAULT_DENIAL_MESSAGE;
    protected final Map<PermissionFlag, Message> generalPermissions;

    public SimpleFlagHolder()
    {
        generalPermissions = new EnumMap<>(PermissionFlag.class);
    }

    protected SimpleFlagHolder(Map<PermissionFlag, Message> map)
    {
        this.generalPermissions = map;
    }

    public Message getDefaultMessage()
    {
        return defaultMessage;
    }

    protected void setDefaultMessage(Message message)
    {
        Message old = this.defaultMessage;
        defaultMessage = message;

        generalPermissions.entrySet().stream().filter(e-> e.getValue().equals(old)).map(Map.Entry::getKey)
                .forEach(f-> generalPermissions.put(f, message));
    }

    @NotNull
    @Override
    public Optional<Message> can(@NotNull MinecraftEntity entity, @NotNull PermissionFlag action)
    {
        return Optional.ofNullable(generalPermissions.get(action));
    }

    @NotNull
    @Override
    public Optional<Message> can(@NotNull Identity<?> identity, @NotNull PermissionFlag action)
    {
        return Optional.ofNullable(generalPermissions.get(action));
    }

    /**
     * Changes the default permission for a flag, will not affect direct permissions and restrictions
     * @param flag The flag that will be allowed
     */
    public void allow(PermissionFlag flag)
    {
        generalPermissions.remove(flag);
    }

    /**
     * Changes the default permission for a flag, will not affect direct permissions and restrictions.
     * The current default denial message will be used, if the default message changes later it will be updated.
     * @param flag The flag that will be denied
     */
    public void deny(PermissionFlag flag)
    {
        generalPermissions.put(flag, defaultMessage);
    }

    /**
     * Changes the default permission for a flag, will not affect direct permissions and restrictions.
     * @param flag The flag that will be denied
     * @param message The message denial message that will be displayed.
     */
    public void deny(PermissionFlag flag, Message message)
    {
        generalPermissions.put(flag, message);
    }

    /**
     * Changes the default permission for all flags in the map, will not affect direct permissions and restrictions.
     * @param flags The flags that will be denied, the map may not contains {@code null} keys or values
     */
    public void denyAll(Map<PermissionFlag, Message> flags)
    {
        generalPermissions.putAll(flags);
    }

    /**
     * Changes the default permission for all flags in the other flag holder, will not affect direct permissions and restrictions.
     * @param holder The flags that will be denied
     */
    public void denyAll(SimpleFlagHolder holder)
    {
        denyAll(holder.generalPermissions);
    }
}
