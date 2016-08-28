package br.com.gamemods.minecity.forge.base.accessors.entity;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import net.minecraft.entity.projectile.EntityFishHook;

public interface IEntityFishHook extends EntityProjectile
{
    @Override
    default EntityFishHook getForgeEntity()
    {
        return (EntityFishHook) this;
    }

    default IEntityPlayerMP getAnger()
    {
        return (IEntityPlayerMP) ((EntityFishHook) this).angler;
    }

    @Override
    default void detectShooter(MineCityForge mod)
    {
        IEntityPlayerMP anger = getAnger();
        if(anger == null)
            setShooter(new ProjectileShooter(getEntityPos(mod)));
        else
            setShooter(new ProjectileShooter(getEntityPos(mod), anger));
    }
}
