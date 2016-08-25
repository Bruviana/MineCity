package br.com.gamemods.minecity.forge.base.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertSetterGetterTransformer;

/**
 * Makes {@link net.minecraft.entity.player.EntityPlayerMP EntityPlayerMP}
 * implements {@link br.com.gamemods.minecity.forge.base.accessors.IEntityPlayerMP IEntityPlayerMP}
 * <pre><code>
 *     public class EntityPlayerMP extends EntityPlayer
 *         implements IEntityPlayerMP // <- Added
 *     {
 *         // ... original fields and methods
 *         public ForgePlayer mineCity;
 *         public ForgePlayer getMineCityPlayer(){ return this.mineCity; }
 *         public void setMineCityPlayer(ForgePlayer player){ this.mineCity = player; }
 *     }
 * </code></pre>
 */
@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCityForge7CoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
public class EntityPlayerMPTransformer extends InsertSetterGetterTransformer
{
    public EntityPlayerMPTransformer()
    {
        super(
                "net.minecraft.entity.player.EntityPlayerMP",
                "br/com/gamemods/minecity/forge/base/command/IForgePlayer", "mineCity",
                "br/com/gamemods/minecity/forge/base/accessors/IEntityPlayerMP",
                "setMineCityPlayer", "getMineCityPlayer"
        );
    }
}
