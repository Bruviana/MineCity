package br.com.gamemods.minecity.bukkit.protection;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.bukkit.BukkitUtil19;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Island;
import br.com.gamemods.minecity.structure.Plot;
import com.google.common.collect.MapMaker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static br.com.gamemods.minecity.api.permission.PermissionFlag.*;
import static br.com.gamemods.minecity.bukkit.BukkitUtil.optional;

public class EntityProtections extends AbstractProtection
{
    private Set<Egg> eggs = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());
    private Set<EnderPearl> pearls = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());

    public EntityProtections(@NotNull MineCityBukkit plugin)
    {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event)
    {
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        switch(reason)
        {
            case ENDER_PEARL:
            case EGG:
            {
                Location location = event.getLocation();
                Set<? extends Projectile> set = reason == CreatureSpawnEvent.SpawnReason.EGG? eggs : pearls;
                Projectile entity = getNearest(set, location);
                if(entity == null)
                {
                    event.setCancelled(true);
                    return;
                }

                ProjectileSource shooter = entity.getShooter();
                if(shooter instanceof Player)
                {
                    if(check(location, (Player) shooter, PermissionFlag.MODIFY))
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            break;
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onProjectHit(ProjectileHitEvent event)
    {
        Projectile entity = event.getEntity();
        switch(entity.getType())
        {
            case EGG:
            {
                Egg egg = (Egg) entity;
                eggs.add(egg);
                plugin.plugin.getScheduler().runTaskLater(plugin.plugin, () -> eggs.remove(egg), 5);
            }
            break;
            case ENDER_PEARL:
            {
                EnderPearl pearl = (EnderPearl) entity;
                Location location = pearl.getLocation();

                ProjectileSource shooter = pearl.getShooter();
                if(shooter instanceof Player)
                {
                    if(check(location, (Player) shooter, PermissionFlag.ENTER))
                    {
                        pearl.remove();
                        return;
                    }
                }

                pearls.add(pearl);
                plugin.plugin.getScheduler().runTaskLater(plugin.plugin, () -> pearls.remove(pearl), 5);
            }
            break;
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onVehicleDamage(VehicleDamageEvent event)
    {
        Entity attacker = event.getAttacker();
        Vehicle vehicle = event.getVehicle();

        if(attacker instanceof Projectile)
        {
            ProjectileSource shooter = ((Projectile)attacker).getShooter();

            if(shooter instanceof Entity)
                attacker = (Entity) shooter;
            else if(shooter instanceof BlockProjectileSource)
            {
                Block block = ((BlockProjectileSource) shooter).getBlock();
                Location loc = block.getLocation();
                ChunkPos chunkPos = plugin.chunk(loc);
                ClaimedChunk chunk = plugin.mineCity.provideChunk(chunkPos);
                PlayerID owner = chunk.getPlotAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).map(Plot::owner)
                        .orElseGet(()-> chunk.getIsland().map(Island::getCity).map(City::owner).orElse(null));

                if(owner == null)
                    return;

                BlockPos blockPos = plugin.blockPos(vehicle.getLocation());
                FlagHolder holder;
                if(blockPos.getChunk().equals(chunkPos))
                    holder = chunk.getFlagHolder(blockPos);
                else
                    holder = plugin.mineCity.provideChunk(blockPos.getChunk()).getFlagHolder(blockPos);

                Optional<Message> denial = holder.can(owner, PermissionFlag.MODIFY);
                if(denial.isPresent())
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if(attacker instanceof Player)
        {
            if(check(vehicle.getLocation(), (Player)attacker, PermissionFlag.MODIFY))
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
        Entity attacker = event.getDamager();
        switch(attacker.getType())
        {
            case EGG:
                Egg egg = (Egg) attacker;
                eggs.add(egg);
                plugin.plugin.getScheduler().runTaskLater(plugin.plugin, () -> eggs.remove(egg), 5);
                break;

            case ENDER_PEARL:
                EnderPearl pearl = (EnderPearl) pearls;
                pearls.add(pearl);
                plugin.plugin.getScheduler().runTaskLater(plugin.plugin, () -> pearls.remove(pearl), 5);
                break;
        }

        if(event.isCancelled())
            return;

        if(attacker instanceof Projectile)
        {
            ProjectileSource shooter = ((Projectile)attacker).getShooter();

            if(shooter instanceof Entity)
                attacker = (Entity) shooter;
            else if(shooter instanceof BlockProjectileSource)
            {
                Block block = ((BlockProjectileSource) shooter).getBlock();
                Location loc = block.getLocation();
                ChunkPos chunkPos = plugin.chunk(loc);
                ClaimedChunk chunk = plugin.mineCity.provideChunk(chunkPos);
                PlayerID owner = chunk.getPlotAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).map(Plot::owner)
                        .orElseGet(()-> chunk.getIsland().map(Island::getCity).map(City::owner).orElse(null));

                if(owner == null)
                    return;

                Entity victim = event.getEntity();
                BlockPos blockPos = plugin.blockPos(victim.getLocation());
                FlagHolder holder;
                if(blockPos.getChunk().equals(chunkPos))
                    holder = chunk.getFlagHolder(blockPos);
                else
                    holder = plugin.mineCity.provideChunk(blockPos.getChunk()).getFlagHolder(blockPos);

                Optional<Message> denial = holder.can(owner,
                        victim instanceof Player? PVP : victim instanceof Monster? PVM : PVC
                );
                if(denial.isPresent())
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if(attacker instanceof Tameable)
        {
            AnimalTamer tamer = ((Tameable) attacker).getOwner();
            if(tamer instanceof Entity)
                attacker = (Entity) tamer;
            else if(tamer instanceof OfflinePlayer)
            {
                OfflinePlayer offlinePlayer = (OfflinePlayer) tamer;
                PlayerID owner = new PlayerID(offlinePlayer.getUniqueId(), offlinePlayer.getName());

                Entity victim = event.getEntity();
                BlockPos blockPos = plugin.blockPos(victim.getLocation());
                FlagHolder holder = plugin.mineCity.provideChunk(blockPos.getChunk()).getFlagHolder(blockPos);

                Optional<Message> denial = holder.can(owner,
                        victim instanceof Player? PVP : victim instanceof Monster? PVM : PVC
                );
                if(denial.isPresent())
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if(attacker instanceof Player)
        {
            Entity victim = event.getEntity();
            if(check(victim.getLocation(), (Player)attacker, victim instanceof Player? PVP : victim instanceof Monster? PVM : PVC))
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event)
    {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();
        if(entity instanceof ArmorStand)
        {
            if(check(entity.getLocation(), player, PermissionFlag.OPEN))
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();
        Optional<ItemStack> hand = BukkitUtil19.getItemInSlot(player.getInventory(), event.getHand());

        if(entity instanceof Villager)
        {
            Villager villager = (Villager) entity;
            if(villager.isAdult() && check(villager.getLocation(), player, PermissionFlag.HARVEST))
                event.setCancelled(true);
        }
        else if(entity instanceof Horse)
        {
            Horse horse = (Horse) entity;
            if(horse.isAdult())
            {
                Material m = hand.map(ItemStack::getType).orElse(Material.AIR);
                boolean modifying;
                switch(m)
                {
                    case SADDLE:
                        modifying = !optional(horse.getInventory().getSaddle()).isPresent();
                        break;

                    case DIAMOND_BARDING:
                    case GOLD_BARDING:
                    case IRON_BARDING:
                        modifying = !optional(horse.getInventory().getArmor()).isPresent();
                        break;

                    default:
                        modifying = false;
                }

                if(modifying)
                {
                    if(!player.equals(horse.getOwner()))
                    {
                        if(check(horse.getLocation(), player, PermissionFlag.MODIFY))
                            event.setCancelled(true);
                    }
                }
                else
                {
                    if(check(entity.getLocation(), player, PermissionFlag.ENTER, PermissionFlag.RIDE))
                    {
                        event.setCancelled(true);

                        if(player.equals(horse.getOwner()))
                            entity.teleport(player);
                    }
                }
            }
        }
        else if(entity instanceof Minecart)
        {
            if(entity instanceof RideableMinecart)
            {
                if(check(entity.getLocation(), player, PermissionFlag.ENTER, PermissionFlag.RIDE))
                    event.setCancelled(true);
            }
            else if(entity instanceof InventoryHolder)
            {
                if(check(entity.getLocation(), player, PermissionFlag.OPEN))
                    event.setCancelled(true);
            }
            else if(entity instanceof PoweredMinecart)
            {
                if(check(entity.getLocation(), player, PermissionFlag.CLICK))
                    event.setCancelled(true);
            }
            else if(entity instanceof ExplosiveMinecart)
            {
                if(hand.map(ItemStack::getType).filter(Material.FLINT_AND_STEEL::equals).isPresent())
                {
                    if(check(entity.getLocation(), player, PermissionFlag.MODIFY))
                        event.setCancelled(true);
                }
            }
        }
        else if(entity instanceof Cow)
        {
            if(hand.isPresent())
            {
                Material item = hand.get().getType();
                EntityType type = entity.getType();
                if(item == Material.BUCKET && type == EntityType.COW || item == Material.BOWL && type == EntityType.MUSHROOM_COW)
                {
                    if(check(entity.getLocation(), player, PermissionFlag.HARVEST))
                        event.setCancelled(true);
                }
            }
        }
        else if(entity instanceof Pig)
        {
            Pig pig = (Pig) entity;
            if(pig.isAdult())
            {
                if(!pig.hasSaddle())
                {
                    if(hand.map(ItemStack::getType).filter(Material.SADDLE::equals).isPresent())
                    {
                        if(check(pig.getLocation(), player, PermissionFlag.MODIFY))
                            event.setCancelled(true);
                    }
                }
                else
                {
                    if(check(entity.getLocation(), player, PermissionFlag.ENTER, PermissionFlag.RIDE))
                        event.setCancelled(true);
                }
            }
        }
        else if(entity instanceof Vehicle)
        {
            if(check(entity.getLocation(), player, PermissionFlag.ENTER, PermissionFlag.RIDE))
                event.setCancelled(true);
        }
        else if(entity instanceof ItemFrame)
        {
            if(check(entity.getLocation(), player, PermissionFlag.OPEN))
                event.setCancelled(true);
        }
        else if(entity instanceof LeashHitch)
        {
            if(check(entity.getLocation(), player, PermissionFlag.MODIFY))
                event.setCancelled(true);
        }
        else if(entity.getType() == EntityType.ZOMBIE)
        {
            Zombie zombie = (Zombie) entity;
            if(hand.map(ItemStack::getType).filter(Material.GOLDEN_APPLE::equals).isPresent() && zombie.hasPotionEffect(PotionEffectType.WEAKNESS))
            {
                if(check(zombie.getLocation(), player, PermissionFlag.MODIFY))
                    event.setCancelled(true);
            }
        }
        else if(entity instanceof Tameable)
        {
            Tameable tameable = (Tameable) entity;
            if(!tameable.isTamed())
            {
                if(check(entity.getLocation(), player, PermissionFlag.MODIFY))
                    event.setCancelled(true);
            }
        }
        else if(entity instanceof EnderCrystal)
        {
            if(check(entity.getLocation(), player, PermissionFlag.MODIFY))
                event.setCancelled(true);
        }
        else if(entity instanceof LingeringPotion)
        {
            if(hand.map(ItemStack::getType).filter(Material.GLASS_BOTTLE::equals).isPresent())
            {
                if(check(entity.getLocation(), player, PermissionFlag.CLICK))
                    event.setCancelled(true);
            }
        }
    }
}
