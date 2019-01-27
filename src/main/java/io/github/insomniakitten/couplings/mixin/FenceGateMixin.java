/*
 * Copyright (C) 2018 InsomniaKitten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.insomniakitten.couplings.mixin;

import io.github.insomniakitten.couplings.hook.FenceGateHooks;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FenceGateBlock.class)
final class FenceGateMixin {
  private static final String ACTIVATE =
    "Lnet/minecraft/block/FenceGateBlock;" +
      "activate(" +
        "Lnet/minecraft/block/BlockState;" +
        "Lnet/minecraft/world/World;" +
        "Lnet/minecraft/util/math/BlockPos;" +
        "Lnet/minecraft/entity/player/PlayerEntity;" +
        "Lnet/minecraft/util/Hand;" +
        "Lnet/minecraft/util/hit/BlockHitResult;" +
      ")Z";

  private FenceGateMixin() {}

  @Inject(
    method = FenceGateMixin.ACTIVATE,
    at = @At("RETURN")
  )
  private void coupled$use(
    final BlockState state,
    final World world,
    final BlockPos pos,
    final PlayerEntity player,
    final Hand hand,
    final BlockHitResult hit,
    final CallbackInfoReturnable<Boolean> cir
  ) {
    FenceGateHooks.usageCallback(state, world, pos, player, hand, hit, cir.getReturnValueZ());
  }
}
