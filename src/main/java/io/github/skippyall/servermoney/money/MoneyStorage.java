package io.github.skippyall.servermoney.money;

import io.github.skippyall.servermoney.ServerMoney;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MoneyStorage extends PersistentState {
    public static final Type<MoneyStorage> TYPE = new Type<>(MoneyStorage::new, MoneyStorage::createFromNbt, null);
    private static HashMap<UUID, Long> moneymap = new HashMap<>();

    private static MoneyStorage INSTANCE;

    public static long getMoney(PlayerEntity player){
        return getMoney(player.getGameProfile().getId());
    }

    public static long getMoney(UUID id){
        return moneymap.containsKey(id)?moneymap.get(id):0;
    }

    public static void setMoney(PlayerEntity player, long money){
        setMoney(player.getGameProfile().getId(), money);
    }

    public static void setMoney(UUID id, long money){
        moneymap.put(id, money);
    }

    private static final String ID_KEY = "id";
    private static final String MONEY_KEY = "money";

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList money = new NbtList();
        for(Map.Entry<UUID, Long> entry : moneymap.entrySet()){
            NbtCompound compound = new NbtCompound();
            compound.putUuid(ID_KEY, entry.getKey());
            compound.putLong(MONEY_KEY, entry.getValue());
            money.add(compound);
        }
        nbt.put(MONEY_KEY, money);
        return nbt;
    }

    public static MoneyStorage createFromNbt(NbtCompound nbt) {
        NbtList money = (NbtList) nbt.get("money");
        if(money!=null) {
            for (NbtElement element : money) {
                NbtCompound compound = (NbtCompound) element;
                moneymap.put(compound.getUuid(ID_KEY), compound.getLong(MONEY_KEY));
            }
        }
        return new MoneyStorage();
    }

    public static void init(MinecraftServer server){
        PersistentStateManager manager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        INSTANCE = manager.getOrCreate(TYPE, ServerMoney.MOD_ID);
        INSTANCE.markDirty();
    }
}
