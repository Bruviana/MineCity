package br.com.gamemods.minecity.forge.base.accessors.entity;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityShulkerBullet;

import java.lang.reflect.Field;

public interface IEntityShulkerBullet extends EntityProjectile
{
    @Override
    default void detectShooter(MineCityForge mod)
    {
        IEntityLivingBase owner = null;
        for(Field field: EntityShulkerBullet.class.getDeclaredFields())
        {
            if(field.getType() == EntityLivingBase.class)
            {
                field.setAccessible(true);
                try
                {
                    owner = (IEntityLivingBase) field.get(this);
                }
                catch(Exception e)
                {
                    System.err.println("[MineCity] Failed to detect the owner of "+this);
                    e.printStackTrace();
                }
                break;
            }
        }

        if(owner == null)
            setShooter(new ProjectileShooter(getEntityPos(mod)));
        else
            setShooter(new ProjectileShooter(getEntityPos(mod), owner));
    }
}
