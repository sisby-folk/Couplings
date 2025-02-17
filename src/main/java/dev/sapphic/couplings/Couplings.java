/*
 * Copyright 2022 Chloe Dawn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.sapphic.couplings;

import com.google.common.base.Preconditions;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;

public final class Couplings implements ModInitializer {
  public static final int COUPLING_DISTANCE = 64;
  public static final int COUPLING_SIGNAL = 8;

  static final ResourceLocation CLIENT_CONFIG = new ResourceLocation("couplings", "client_config");
  static final ResourceLocation SERVER_CONFIG = new ResourceLocation("couplings", "server_config");

  public static final CouplingsConfig CONFIG = CouplingsConfig.createToml(FabricLoader.getInstance().getConfigDir(), "", "couplings", CouplingsConfig.class);

  public static boolean ignoresSneaking(final Player player) {
    if (player instanceof CouplingsPlayer) {
      return CouplingsPlayer.ignoresSneaking(player);
    }

    return CONFIG.ignoreSneaking.value();
  }

  public static boolean couplesDoors(final Level level) {
    return level.isClientSide() ? CouplingsClient.serverCouplesDoors() : CONFIG.coupleDoors.value();
  }

  public static boolean couplesFenceGates(final Level level) {
    return level.isClientSide() ? CouplingsClient.serverCouplesFenceGates() : CONFIG.coupleFenceGates.value();
  }

  public static boolean couplesTrapdoors(final Level level) {
    return level.isClientSide() ? CouplingsClient.serverCouplesTrapdoors() : CONFIG.coupleTrapdoors.value();
  }

  @Override
  public void onInitialize() {
    if (!CONFIG.coupleDoors.value() || !CONFIG.coupleFenceGates.value() || !CONFIG.coupleTrapdoors.value()) {
      LogManager.getLogger().warn("No features are enabled, this could be a bug!");
    }
    ServerPlayNetworking.registerGlobalReceiver(
        CLIENT_CONFIG,
        (server, player, listener, buf, sender) -> {
          Preconditions.checkArgument(buf.readableBytes() == Byte.BYTES, buf);

          final var clientConfig = buf.readByte();

          Preconditions.checkArgument(clientConfig <= 1, buf);

          server.execute(() -> CouplingsPlayer.ignoresSneaking(player, clientConfig != 0));
        });

    ServerPlayConnectionEvents.JOIN.register(
        (listener, sender, server) -> {
          var couplings = 0b000;

          couplings |= (CONFIG.coupleDoors.value() ? 1 : 0) << 2;
          couplings |= (CONFIG.coupleFenceGates.value() ? 1 : 0) << 1;
          couplings |= CONFIG.coupleTrapdoors.value() ? 1 : 0;

          final var buffer =
              Unpooled.buffer(Byte.BYTES, Byte.BYTES).writeByte(couplings).asReadOnly();

          ServerPlayNetworking.send(listener.player, SERVER_CONFIG, new FriendlyByteBuf(buffer));
        });
  }
}
