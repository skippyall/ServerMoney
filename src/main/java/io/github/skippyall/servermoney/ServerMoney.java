package io.github.skippyall.servermoney;

import eu.midnightdust.lib.config.MidnightConfig;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import io.github.skippyall.servermoney.coin.CoinItem;
import io.github.skippyall.servermoney.commands.MoneyCommand;
import io.github.skippyall.servermoney.commands.ShopCommand;
import io.github.skippyall.servermoney.config.ServerMoneyConfig;
import io.github.skippyall.servermoney.input.InputAttachment;
import io.github.skippyall.servermoney.money.MoneyDistributor;
import io.github.skippyall.servermoney.money.MoneyStorage;
import io.github.skippyall.servermoney.shop.BreakShopEvent;
import io.github.skippyall.servermoney.shop.ShopResendCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.item.Item;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.timer.TimerCallbackSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMoney implements ModInitializer {
    public static final String MOD_ID = "servermoney";
    public static final Logger LOGGER = LoggerFactory.getLogger("Server Money");
    public static final CustomPayload.Id<?> PACKET_ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "check_presence"));

    public static final Identifier COIN_ID = Identifier.of(MOD_ID, "coin");
    public static final CoinItem COIN_ITEM = Registry.register(Registries.ITEM, COIN_ID, new CoinItem(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, COIN_ID))));

    @Override
    public void onInitialize() {
        PolymerNetworking.registerS2CVersioned(PACKET_ID, PacketCodec.unit(null));

        MoneyBlocks.register();
        CommandRegistrationCallback.EVENT.register(new MoneyCommand());
        CommandRegistrationCallback.EVENT.register(new ShopCommand());

        PlayerBlockBreakEvents.BEFORE.register(new BreakShopEvent());
        TimerCallbackSerializer.INSTANCE.registerSerializer(Identifier.of(MOD_ID, "shop_resend"), ShopResendCallback.CODEC);
        InputAttachment.register();

        ServerLifecycleEvents.SERVER_STARTED.register(MoneyStorage::init);
        ServerTickEvents.END_SERVER_TICK.register(MoneyDistributor::tick);

        MidnightConfig.init(MOD_ID, ServerMoneyConfig.class);
    }
}
