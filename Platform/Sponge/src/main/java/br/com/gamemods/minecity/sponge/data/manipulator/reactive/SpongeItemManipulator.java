package br.com.gamemods.minecity.sponge.data.manipulator.reactive;

import br.com.gamemods.minecity.reactive.game.item.ReactiveItem;
import br.com.gamemods.minecity.reactive.game.item.ReactiveItemState;
import br.com.gamemods.minecity.reactive.game.item.ReactiveItemTrait;
import br.com.gamemods.minecity.reactive.game.item.data.*;
import br.com.gamemods.minecity.reactive.game.item.data.supplier.SupplierItemData;
import br.com.gamemods.minecity.reactive.reactor.ItemReactor;
import br.com.gamemods.minecity.sponge.data.value.SpongeItemData;
import br.com.gamemods.minecity.sponge.data.value.SpongeItemStackData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static br.com.gamemods.minecity.sponge.data.manipulator.reactive.SpongeManipulator.handleSupplier;

public class SpongeItemManipulator implements ItemManipulator, ItemReactor
{
    private final SpongeManipulator manipulator;
    private final ThreadLocal<ItemType> handlingItemType = new ThreadLocal<>();

    public SpongeItemManipulator(SpongeManipulator manipulator)
    {
        this.manipulator = manipulator;
    }

    @Override
    public Collection<ItemData> findItemTypes(Class<?> clazz)
    {
        return Sponge.getGame().getRegistry().getAllOf(ItemType.class).stream().filter(clazz::isInstance)
                .map(this::getItemData).collect(Collectors.toList());
    }

    @NotNull
    @Override
    public Optional<ItemData> getItemData(@NotNull Object item)
    {
        if(item instanceof CharSequence)
        {
            item = Sponge.getGame().getRegistry().getType(ItemType.class, item.toString()).orElse(null);
            if(item instanceof SupplierItemData)
                return Optional.of(((SupplierItemData) item).getItemData());
        }

        if(!(item instanceof ItemType))
            return Optional.empty();

        return Optional.of(getItemData((ItemType) item));
    }

    public ItemData getItemData(@NotNull ItemType itemType)
    {
        return handleSupplier(handlingItemType, itemType, SupplierItemData.class,
                SupplierItemData::getItemData,
                ()-> new SpongeItemData(manipulator, itemType)
        );
    }

    @NotNull
    @Override
    public Optional<ReactiveItemState> getReactiveItemState(ItemStateData ItemState)
    {
        return Optional.empty();
    }

    @NotNull
    @Override
    public <T> Optional<ReactiveItemTrait<T>> getReactiveItemTrait(ItemTraitData<T> itemTrait)
    {
        return Optional.empty();
    }

    @NotNull
    @Override
    public Optional<ReactiveItem> getReactiveItem(Object item)
    {
        return Optional.empty();
    }

    @NotNull
    @Override
    public Optional<ItemStackData> getItemStackData(@NotNull Object stack)
    {
        if(!(stack instanceof ItemStack))
            return Optional.empty();

        return Optional.of(getItemStackData((ItemStack) stack));
    }

    public SpongeItemStackData getItemStackData(ItemStack stack)
    {
        return new SpongeItemStackData(manipulator, stack);
    }

    @NotNull
    @Override
    public Optional<ItemStateData> getItemStateData(@NotNull Object state)
    {
        if(!(state instanceof ItemStack))
            return Optional.empty();

        return Optional.of(getItemStackData((ItemStack) state));
    }

    @NotNull
    @Override
    public Optional<ItemTraitData<?>> getItemTraitData(@NotNull Object trait)
    {
        return Optional.empty();
    }

    @Override
    public String toString()
    {
        return "SpongeItemManipulator{"+
                "manipulator="+manipulator+
                ", handlingItemType="+handlingItemType+
                '}';
    }
}
