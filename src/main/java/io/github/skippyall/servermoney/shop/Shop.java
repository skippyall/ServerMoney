package io.github.skippyall.servermoney.shop;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Uuids;

import java.util.Optional;
import java.util.UUID;

public class Shop {
    private UUID shopOwner = FakePlayer.DEFAULT_UUID;
    private ItemVariant item = ItemVariant.blank();
    private int count = 0;
    private double price = 0;

    public UUID getShopOwner() {
        return shopOwner;
    }

    public ItemVariant getItem() {
        return item;
    }

    public int getCount(){
        return count;
    }

    public double getPrice() {
        return price;
    }

    public void setShopOwner(UUID shopOwner) {
        this.shopOwner = shopOwner;
    }

    public void setItem(ItemVariant item) {
        this.item = item;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public NbtCompound encode(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound shop = new NbtCompound();
        shop.put("owner", Uuids.CODEC, getShopOwner());
        shop.putDouble("price", getPrice());
        shop.put("item", ItemVariant.CODEC, getItem());
        shop.putInt("count", getCount());
        return shop;
    }

    public void decode(Optional<NbtCompound> optional, RegistryWrapper.WrapperLookup registryLookup) {
        if(optional.isPresent()) {
            NbtCompound shop = optional.get();
            setShopOwner(shop.get("owner", Uuids.CODEC).orElse(FakePlayer.DEFAULT_UUID));
            setPrice(shop.getDouble("price").orElse(0.0));
            setItem(shop.get("item", ItemVariant.CODEC).orElse(ItemVariant.blank()));
            setCount(shop.getInt("count").orElse(0));
        }
    }
}
