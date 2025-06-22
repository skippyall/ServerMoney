package io.github.skippyall.servermoney.paybutton;

import io.github.skippyall.servermoney.MoneyBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class PayButtonBlockEntity extends BlockEntity {
    private UUID owner = null;
    private double amount = 0;

    public PayButtonBlockEntity(BlockPos pos, BlockState state) {
        super(MoneyBlocks.PAY_BUTTON.blockEntityType(), pos, state);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        markDirty();
    }

    public void setAmount(double amount) {
        this.amount = amount;
        markDirty();
    }

    public UUID getOwner() {
        return owner;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        owner = nbt.get("owner", Uuids.CODEC).orElse(null);
        amount = nbt.getDouble("amount").orElse(0.0);
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        nbt.put("owner", Uuids.CODEC, owner);
        nbt.putDouble("amount", amount);
    }
}
