package io.github.skippyall.servermoney.shop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.timer.Timer;
import net.minecraft.world.timer.TimerCallback;

public record ShopResendCallback(BlockState state, BlockPos pos, RegistryKey<World> world) implements TimerCallback<MinecraftServer> {
    public static MapCodec<? extends TimerCallback<MinecraftServer>> CODEC = Codec.unit(new TimerCallback<MinecraftServer>() {
        @Override
        public void call(MinecraftServer server, Timer<MinecraftServer> events, long time) {}

        @Override
        public MapCodec<? extends TimerCallback<MinecraftServer>> getCodec() {
            return CODEC;
        }
    }).fieldOf("shopresend");

    public static long counter = 0;
    @Override
    public void call(MinecraftServer server, Timer<MinecraftServer> events, long time) {
        server.getPlayerManager().sendToDimension(new BlockUpdateS2CPacket(pos, state), world);
    }

    @Override
    public MapCodec<? extends TimerCallback<MinecraftServer>> getCodec() {
        return CODEC;
    }
}
