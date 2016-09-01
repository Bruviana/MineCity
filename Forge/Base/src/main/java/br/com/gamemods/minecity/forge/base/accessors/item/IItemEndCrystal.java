package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.Cuboid;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.ShapeBlockReaction;
import net.minecraft.init.Blocks;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IItemEndCrystal extends IItem
{
    @Override
    default Reaction reactRightClickBlock(IEntityPlayerMP player, IItemStack stack, boolean offHand,
                                          IState state, BlockPos pos, Direction face)
    {
        IBlock block = state.getIBlock();
        if(block != Blocks.OBSIDIAN && block != Blocks.BEDROCK)
            return NoReaction.INSTANCE;

        ShapeBlockReaction reaction = new ShapeBlockReaction(pos.world, new Cuboid(pos.add(-1,1,-1), pos.add(1,2,1)), PermissionFlag.MODIFY);
        reaction.onDenyUpdateInventory();
        return reaction;
    }
}
