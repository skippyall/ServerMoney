package io.github.skippyall.servermoney.input;

import com.mojang.authlib.GameProfile;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import io.github.skippyall.servermoney.MoneyBlocks;
import io.github.skippyall.servermoney.config.ServerMoneyConfig;
import io.github.skippyall.servermoney.money.MoneyStorage;
import io.github.skippyall.servermoney.paybutton.PayButtonBlockEntity;
import io.github.skippyall.servermoney.shop.block.ShopBlockEntity;
import io.github.skippyall.servermoney.shop.modification.ShopModification;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Input {
    public static CompletableFuture<String> selectString(ServerPlayerEntity player, Text title, String defaultInput) {
        CompletableFuture<String> future = new CompletableFuture<>();

        AnvilInputGui gui = new AnvilInputGui(player, false);
        gui.setSlot(AnvilScreenHandler.OUTPUT_ID, new ItemStack(Items.EMERALD_BLOCK), (index, type, action) -> {
            String out = gui.getInput();
            future.complete(out);
        });
        gui.setTitle(title);
        gui.setDefaultInputValue(defaultInput);
        gui.open();
        return future;
    }

    public static void selectPrice(ServerPlayerEntity player, ShopBlockEntity shop) {
        //player.sendMessage(Text.translatable("servermoney.input.price").setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/shop modify price "))));
        selectString(player, Text.translatable("servermoney.shop.owner.price.input"), String.valueOf(shop.getPrice())).thenAccept(string -> {
            try {
                double outDouble = Double.parseDouble(string);
                InputAttachment.hasInputType(player, InputType.PRICE);
                InputAttachment.getCompletableFuture(player, InputType.PRICE).complete(outDouble);
            } catch (NumberFormatException e) {
                player.sendMessage(Text.translatable("servermoney.shop.owner.number_parse_error"));
            }
        });
        scheduleInput(InputType.PRICE, player).thenAccept(shop::setPrice);
    }

    public static void selectAmount(ServerPlayerEntity player, ShopBlockEntity shop) {
        //player.sendMessage(Text.translatable("servermoney.input.amount").setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/shop modify amount "))));
        selectString(player, Text.translatable("servermoney.shop.owner.amount.input"), String.valueOf(shop.getCount())).thenAccept(string -> {
            try {
                int outInt = Integer.parseInt(string);
                if(InputAttachment.hasInputType(player, InputType.AMOUNT)) {
                    InputAttachment.getCompletableFuture(player, InputType.AMOUNT).complete(outInt);
                }
            } catch (NumberFormatException e) {
                player.sendMessage(Text.translatable("servermoney.shop.owner.number_parse_error"));
            }
        });
        scheduleInput(InputType.AMOUNT, player).thenAccept(shop::setCount);
    }

    public static void selectItem(PlayerEntity player, ShopBlockEntity shop) {
        player.sendMessage(Text.translatable("servermoney.input.item").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.SuggestCommand("/shop modify item"))), false);
        scheduleInput(InputType.ITEM, player).thenAccept(shop::setItem);
    }

    /**
     * Lets the player select a shop. The modification will be applied to the shop that the player selects.
     * @param player The player that should select the shop
     * @param modification The ShopModification that should be applied to the selected shop
     */
    public static void selectShop(PlayerEntity player, ShopModification modification){
        scheduleInput(InputType.SHOP, player).thenAccept(modification::apply);
    }

    /*public static void closeShop(PlayerEntity player, ShopBlockEntity be) {
        player.sendMessage(Text.literal("Do you really want to close the shop? If you want, click ").append(Text.literal("here.").setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/shop close")).withUnderline(true))));
        long start = System.currentTimeMillis();
        scheduleInput(InputType.CLOSE, player).thenAccept((v) -> {
            if (System.currentTimeMillis() < start + 10000) {
                ShopModification.delShopModification().apply(be);
                player.sendMessage(Text.literal("Shop sucessful closed"));
            }
        });
    }*/

    public static void confirmPayButton(PlayerEntity sender, UUID receiver, double amount, BlockPos pos, World world) {
        Optional<GameProfile> profile = sender.getServer().getUserCache().getByUuid(receiver);
        String name = "unknown";
        if(profile.isPresent()) {
            name = profile.get().getName();
        }
        sender.sendMessage(Text.translatable("servermoney.input.paybutton.confirm", name, amount, ServerMoneyConfig.moneySymbol).setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/money confirm"))), false);
        long start = System.currentTimeMillis();
        scheduleInput(InputType.CONFIRM_PAY, sender).thenAccept((v) -> {
            if (System.currentTimeMillis() < start + 10000 && world.getBlockEntity(pos) instanceof PayButtonBlockEntity pbbe) {
                if(pbbe.getOwner().equals(receiver) && MoneyStorage.tryPay(sender.getUuid(), receiver, amount)) {
                    MoneyBlocks.PAY_BUTTON.block().onPay(world.getBlockState(pos), world, pos, sender);
                }
            }
        });
    }

    /**
     * Marks the player as selecting a value
     * @param type The type of the input
     * @param player The player that should select the value
     * @return A CompletableFuture that completes when player has selected a value
     * @param <T> The type of the input
     */
    public static <T> CompletableFuture<T> scheduleInput(InputType<T> type, PlayerEntity player){
        CompletableFuture<T> future = new CompletableFuture<>();
        InputAttachment.setScheduledInput(player, future, type);
        return future;
    }

    public static class InputType<T> {
        public static final InputType<Double> PRICE = new InputType<>();
        public static final InputType<Integer> AMOUNT = new InputType<>();
        public static final InputType<ItemVariant> ITEM = new InputType<>();
        public static final InputType<ShopBlockEntity> SHOP = new InputType<>();
        public static final InputType<Void> CLOSE = new InputType<>();
        public static final InputType<Void> CONFIRM_PAY = new InputType<>();
        private InputType() {

        }
    }
}
