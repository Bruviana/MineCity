package br.com.gamemods.minecity.sponge.listeners;

import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.block.BlockChange;
import br.com.gamemods.minecity.reactive.game.block.Modification;
import br.com.gamemods.minecity.reactive.game.block.PreModification;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlock;
import br.com.gamemods.minecity.reactive.game.block.data.BlockSnapshotData;
import br.com.gamemods.minecity.reactive.game.entity.data.EntityData;
import br.com.gamemods.minecity.reactive.game.entity.data.Hand;
import br.com.gamemods.minecity.reactive.game.item.ReactiveItemStack;
import br.com.gamemods.minecity.reactive.reaction.InteractReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.sponge.MineCitySponge;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import static java.util.stream.Collectors.toList;

public class ActionListener
{
    private final MineCitySponge sponge;

    @Nullable
    private HandInteractEvent lastEntityInteractEvent;

    public ActionListener(MineCitySponge sponge)
    {
        this.sponge = sponge;
    }

    @Listener(order = Order.POST)
    public void onInteract(HandInteractEvent event, @First Entity entity)
    {
        lastEntityInteractEvent = event;
    }

    @Nullable
    private ReactiveItemStack getStackFromEntity(Entity entity, HandType hand)
    {
        return entity instanceof ArmorEquipable
                ? sponge.reactiveStack(((ArmorEquipable)entity).getItemInHand(hand).orElse(null))
                : null;
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onInteractBlock(InteractBlockEvent.Secondary event, @First Entity subject)
    {
        Hand hand = Hand.from(event.getHandType());
        Direction side = sponge.direction(event.getTargetSide());
        PrecisePoint point = event.getInteractionPoint().map(sponge::precisePoint).orElse(null);

        EntityData entity = ReactiveLayer.getEntityData(subject).get();
        ReactiveBlock block = sponge.reactiveBlock(event.getTargetBlock(), subject.getWorld());
        ReactiveItemStack stack = getStackFromEntity(subject, event.getHandType());

        InteractReaction reaction = entity.onRightClick(hand, stack, block, side, point);

        Permissible sender = sponge.permissible(subject);
        AtomicBoolean notify = new AtomicBoolean(true);
        reaction.getAction().can(sponge.mineCity, sender).ifPresent(denial-> {
            event.setCancelled(true);
            if(notify.get())
            {
                sender.send(FlagHolder.wrapDeny(denial));
                notify.set(false);
            }
        });

        reaction.getUseItem().can(sponge.mineCity, sender).ifPresent(denial-> {
            event.setUseItemResult(Tristate.FALSE);
            if(notify.get())
            {
                sender.send(FlagHolder.wrapDeny(denial));
                notify.set(false);
            }
        });

        reaction.getUseBlock().can(sponge.mineCity, sender).ifPresent(denial-> {
            event.setUseBlockResult(Tristate.FALSE);
            if(notify.get())
            {
                sender.send(FlagHolder.wrapDeny(denial));
                notify.set(false);
            }
        });
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockPlace(ChangeBlockEvent.Place event, @First Entity subject)
    {
        onBlockChange(event, subject, EntityData::onBlockPlace);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockBreak(ChangeBlockEvent.Break event, @First Entity subject)
    {
        onBlockChange(event, subject, EntityData::onBlockBreak);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockReplace(ChangeBlockEvent.Modify event, @First Entity subject)
    {
        onBlockChange(event, subject, EntityData::onBlockReplace);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockGrow(ChangeBlockEvent.Grow event, @First Entity subject)
    {
        onBlockChange(event, subject, EntityData::onBlockGrow);
    }

    public void onBlockChangePre(ChangeBlockEvent.Pre event, @First Entity subject)
    {
        HandInteractEvent interact = this.lastEntityInteractEvent;
        if(interact != null && interact.getCause().first(Entity.class).orElse(null) != subject)
            interact = null;

        Hand hand = null;
        ReactiveItemStack stack = null;

        if(interact != null)
        {
            HandType handType = interact.getHandType();
            hand = Hand.from(handType);
            stack = getStackFromEntity(subject, handType);
        }

        EntityData entity = ReactiveLayer.getEntityData(subject).get();
        List<BlockSnapshotData> changeList = event.getLocations().stream()
                .map(Location::createSnapshot).map(ReactiveLayer::getBlockSnapshotData)
                .map(Optional::get).collect(toList());

        PreModification modification = new PreModification(changeList, entity, stack, hand);
        Reaction reaction = entity.onBlockChangePre(modification);

        Permissible sender = sponge.permissible(subject);
        reaction.can(sponge.mineCity, sender).ifPresent(reason-> {
            event.setCancelled(true);
            sender.send(FlagHolder.wrapDeny(reason));
        });
    }

    private void onBlockChange(ChangeBlockEvent event, Entity subject, BiFunction<EntityData, Modification, Reaction> operation)
    {
        HandInteractEvent interact = this.lastEntityInteractEvent;
        if(interact != null && interact.getCause().first(Entity.class).orElse(null) != subject)
            interact = null;

        Hand hand = null;
        ReactiveItemStack stack = null;

        if(interact != null)
        {
            HandType handType = interact.getHandType();
            hand = Hand.from(handType);
            stack = getStackFromEntity(subject, handType);
        }

        EntityData entity = ReactiveLayer.getEntityData(subject).get();
        List<BlockChange> changeList = event.getTransactions().stream()
                .map(tran -> new BlockChange(
                        ReactiveLayer.getBlockSnapshotData(tran.getOriginal()).get(),
                        ReactiveLayer.getBlockSnapshotData(tran.getFinal()).get()
                )).collect(toList());

        Modification modification = new Modification(changeList, entity, stack, hand);
        Reaction reaction = operation.apply(entity, modification);

        Permissible sender = sponge.permissible(subject);
        reaction.can(sponge.mineCity, sender).ifPresent(reason-> {
            event.setCancelled(true);
            sender.send(FlagHolder.wrapDeny(reason));
        });
    }
}
