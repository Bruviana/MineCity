package br.com.gamemods.minecity.forge.base.accessors.entity;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import net.minecraft.entity.projectile.EntityFireball;

public interface IEntityFireball extends EntityProjectile
{
    @Override
    default EntityFireball getForgeEntity()
    {
        return (EntityFireball) this;
    }

    default IEntityLivingBase getIShooter()
    {
        return (IEntityLivingBase) ((EntityFireball) this).shootingEntity;
    }

    @Override
    default void detectShooter(MineCityForge mod)
    {
        IEntityLivingBase shooter = getIShooter();
        if(shooter == null)
            setShooter(new ProjectileShooter(getEntityPos(mod)));
        else
            setShooter(new ProjectileShooter(getEntityPos(mod), shooter));
    }
}
