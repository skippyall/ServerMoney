package io.github.skippyall.servermoney.money;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.skippyall.servermoney.ServerMoney;
import io.github.skippyall.servermoney.compat.minions.MinionsCompat;
import io.github.skippyall.servermoney.config.ServerMoneyConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MoneyStorage extends PersistentState {
    private static final String ID_KEY = "id";
    private static final String MONEY_KEY = "money";

    public static final Codec<MoneyStorage> CODEC = RecordCodecBuilder.<Map.Entry<UUID, Double>>create(instance ->
        instance.group(
            Uuids.CODEC.fieldOf(ID_KEY).forGetter(Map.Entry::getKey),
            Codec.DOUBLE.fieldOf(MONEY_KEY).forGetter(Map.Entry::getValue)
        ).apply(instance, AbstractMap.SimpleEntry::new)
    ).listOf().xmap(MoneyStorage::new, MoneyStorage::getEntries);

    public static final PersistentStateType<MoneyStorage> TYPE = new PersistentStateType<>(ServerMoney.MOD_ID, MoneyStorage::new, CODEC, null);
    private static HashMap<UUID, Double> moneymap = new HashMap<>();

    private static MoneyStorage INSTANCE;

    public MoneyStorage(List<Map.Entry<UUID, Double>> entries) {
        entries.forEach(entry -> moneymap.put(entry.getKey(), entry.getValue()));
    }

    public MoneyStorage() {
        moneymap = new HashMap<>();
    }

    public List<Map.Entry<UUID, Double>> getEntries() {
        return List.copyOf(moneymap.entrySet());
    }

    public static double getMoney(PlayerEntity player){
        return getMoney(player.getGameProfile().getId());
    }

    public static double getMoney(UUID id){
        return moneymap.containsKey(id) ? moneymap.get(id) : getInitialMoney(id);
    }

    public static double getInitialMoney(UUID id) {
        return MinionsCompat.isMinion(id) ? 0 : ServerMoneyConfig.initialMoney;
    }

    public static void setMoney(PlayerEntity player, double money){
        setMoney(player.getGameProfile().getId(), money);
    }

    public static void setMoney(@NotNull UUID id, double money){
        moneymap.put(id, money);
    }

    public static double addMoney(PlayerEntity player, double money){
        return addMoney(player.getGameProfile().getId(), money);
    }

    public static double addMoney(@NotNull UUID id, double money){
        double newMoney = getMoney(id) + money;
        setMoney(id, newMoney);
        return newMoney;
    }

    public static boolean tryRemoveMoney(PlayerEntity player, double money){
        return tryRemoveMoney(player.getGameProfile().getId(), money);
    }

    public static boolean tryRemoveMoney(@NotNull UUID id, double money){
        if(canPay(id, money)) {
            addMoney(id, -money);
            return true;
        } else {
            return false;
        }
    }

    public static boolean tryPay(UUID sender, UUID receiver, double money) {
        if(canPay(sender, money)) {
            double senderMoney = getMoney(sender);
            setMoney(sender, senderMoney - money);

            double receiverMoney = getMoney(receiver);
            setMoney(receiver, receiverMoney + money);
            return true;
        }
        return false;
    }

    public static boolean canPay(PlayerEntity player, double money) {
        return canPay(player.getGameProfile().getId(), money);
    }

    public static boolean canPay(UUID player, double money) {
        return getMoney(player) >= money;
    }

    public static void init(MinecraftServer server){
        PersistentStateManager manager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        INSTANCE = manager.getOrCreate(TYPE);
        INSTANCE.markDirty();
    }
}
