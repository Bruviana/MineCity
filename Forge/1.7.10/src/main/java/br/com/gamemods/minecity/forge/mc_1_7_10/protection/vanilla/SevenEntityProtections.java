package br.com.gamemods.minecity.forge.mc_1_7_10.protection.vanilla;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.*;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.protection.vanilla.EntityProtections;
import br.com.gamemods.minecity.forge.mc_1_7_10.event.FishingHookHitEntityEvent;
import br.com.gamemods.minecity.forge.mc_1_7_10.event.PotionApplyEvent;
import br.com.gamemods.minecity.forge.mc_1_7_10.event.VehicleDamageEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;

public class SevenEntityProtections extends EntityProtections
{
    public SevenEntityProtections(MineCityForge mod)
    {
        super(mod);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(EntityInteractEvent event)
    {
        if(event.entity.worldObj.isRemote)
            return;

        if(onPlayerInteractEntity(
                (IEntityPlayerMP) event.entityPlayer,
                (IEntity) event.target,
                (IItemStack) (Object) event.entityPlayer.getHeldItem(),
                false
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityConstruct(EntityEvent.EntityConstructing event)
    {
        if(event.entity.worldObj.isRemote)
            return;

        mod.callSpawnListeners((IEntity) event.entity);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityEnterChunk(EntityEvent.EnteringChunk event)
    {
        if(event.entity.worldObj.isRemote)
            return;

        onEntityEnterChunk(
                event.entity,
                event.oldChunkX,
                event.oldChunkZ,
                event.newChunkX,
                event.newChunkZ
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onFishingHookHitEntity(FishingHookHitEntityEvent event)
    {
        if(event.hook.worldObj.isRemote)
            return;

        if(onFishingHookHitEntity(
                (IEntity) event.entity,
                (EntityProjectile) event.hook
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPotionApply(PotionApplyEvent event)
    {
        if(event.entity.worldObj.isRemote)
            return;

        if(onPotionApply(
                (IEntityLivingBase) event.entity,
                (IPotionEffect) event.effect,
                (IEntity) event.potion
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingAttack(LivingAttackEvent event)
    {
        if(event.entity.worldObj.isRemote)
            return;

        if(onEntityDamage(
                (IEntityLivingBase) event.entity,
                event.source,
                event.ammount
        ))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onVehicleDamage(VehicleDamageEvent event)
    {
        if(event.entity.worldObj.isRemote)
            return;

        if(onEntityDamage(
                (IEntity) event.entity,
                event.source,
                event.amount
        ))
        {
            event.setCanceled(true);
        }
    }
}
