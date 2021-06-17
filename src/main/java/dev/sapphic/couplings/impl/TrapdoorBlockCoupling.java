/*
 * Copyright (C) 2020 Chloe Dawn
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

package dev.sapphic.couplings.impl;

import dev.sapphic.couplings.Couplings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public final class TrapdoorBlockCoupling {
  private TrapdoorBlockCoupling() {
  }

  public static void used(final BlockState state, final Level level, final BlockPos pos, final Player player) {
    if (!player.isCrouching() || Couplings.IGNORE_SNEAKING) {
      tryOpenCloseEach(state, level, pos, player, state.getValue(TrapDoorBlock.OPEN));
    }
  }

  public static void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final boolean powered) {
    if (powered != (powered || isSufficientlyPowered(state, level, pos))) {
      level.setBlock(pos, state.setValue(TrapDoorBlock.POWERED, false).setValue(TrapDoorBlock.OPEN, true), 2);
    } else if (!powered || (level.getBestNeighborSignal(pos) >= Couplings.COUPLING_SIGNAL)) {
      tryOpenCloseEach(state, level, pos, null, powered);
    }
  }

  private static void tryOpenCloseEach(final BlockState state, final Level level, final BlockPos pos, final @Nullable Player player, final boolean open) {
    final Half half = state.getValue(TrapDoorBlock.HALF);
    final Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
    final boolean traverseZ = facing.getAxis() == Axis.X;
    final int offset = (facing.getAxisDirection() == AxisDirection.POSITIVE) ? 1 : -1;
    final int distance = Couplings.COUPLING_DISTANCE;
    boolean continuePos = true;
    boolean continueNeg = true;

    for (int step = 0; (step <= distance) && (continuePos || continueNeg); ++step) {
      if (continuePos) {
        BlockPos relative = pos;

        if (step != 0) {
          relative = pos.offset(traverseZ ? 0 : step, 0, traverseZ ? step : 0);

          continuePos = ((player == null) || level.mayInteract(player, relative))
            && tryOpenClose(state, relative, level, facing, half, open);
        }

        if (continuePos) {
          relative = relative.offset(traverseZ ? offset : 0, 0, traverseZ ? 0 : offset);

          if ((player == null) || level.mayInteract(player, relative)) {
            tryOpenClose(state, relative, level, facing.getOpposite(), half, open);
          }
        }
      }

      if (continueNeg && (step != 0)) {
        BlockPos relative = pos.offset(traverseZ ? 0 : -step, 0, traverseZ ? -step : 0);

        continueNeg = ((player == null) || level.mayInteract(player, relative))
          && tryOpenClose(state, relative, level, facing, half, open);

        if (continueNeg) {
          relative = relative.offset(traverseZ ? offset : 0, 0, traverseZ ? 0 : offset);
          if ((player == null) || level.mayInteract(player, relative)) {
            tryOpenClose(state, relative, level, facing.getOpposite(), half, open);
          }
        }
      }
    }
  }

  private static boolean tryOpenClose(final BlockState state, final BlockPos pos, final Level level, final Direction facing, final Half half, final boolean open) {
    final BlockState other = level.getBlockState(pos);

    if ((state.getBlock() == other.getBlock()) && (facing == other.getValue(HorizontalDirectionalBlock.FACING))) {
      if ((half == other.getValue(TrapDoorBlock.HALF)) && (open != other.getValue(TrapDoorBlock.OPEN))) {
        level.setBlock(pos, other.setValue(TrapDoorBlock.OPEN, open), 2);
        if (other.getValue(TrapDoorBlock.WATERLOGGED)) {
          level.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return true;
      }
    }

    return false;
  }

  private static boolean isSufficientlyPowered(final BlockState state, final Level level, final BlockPos pos) {
    final Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
    final boolean traverseZ = facing.getAxis() == Axis.X;
    final int offset = (facing.getAxisDirection() == AxisDirection.POSITIVE) ? 1 : -1;
    final int distance = Couplings.COUPLING_DISTANCE;
    boolean continuePos = true;
    boolean continueNeg = true;

    for (int step = 0; (step <= distance) && (continuePos || continueNeg); ++step) {
      if (continuePos) {
        BlockPos relative = pos;

        if (step != 0) {
          relative = pos.offset(traverseZ ? 0 : step, 0, traverseZ ? step : 0);
          
          final BlockState other = level.getBlockState(relative);

          if ((state.getBlock() == other.getBlock()) && (facing == other.getValue(HorizontalDirectionalBlock.FACING))) {
            if (level.getBestNeighborSignal(relative) >= Couplings.COUPLING_SIGNAL) {
              return true;
            }
          } else {
            continuePos = false;
          }
        }

        if (continuePos) {
          relative = relative.offset(traverseZ ? offset : 0, 0, traverseZ ? 0 : offset);

          final BlockState other = level.getBlockState(relative);

          if (state.getBlock() == other.getBlock()) {
            if (facing == other.getValue(HorizontalDirectionalBlock.FACING)) {
              if (level.getBestNeighborSignal(relative) >= Couplings.COUPLING_SIGNAL) {
                return true;
              }
            }
          }
        }
      }

      if (continueNeg && (step != 0)) {
        BlockPos relative = pos.offset(traverseZ ? 0 : -step, 0, traverseZ ? -step : 0);

        final BlockState other = level.getBlockState(relative);

        if ((state.getBlock() == other.getBlock()) && (facing == other.getValue(HorizontalDirectionalBlock.FACING))) {
          if (level.getBestNeighborSignal(relative) >= Couplings.COUPLING_SIGNAL) {
            return true;
          }
        } else {
          continueNeg = false;
        }

        if (continueNeg) {
          relative = relative.offset(traverseZ ? offset : 0, 0, traverseZ ? 0 : offset);

          final BlockState oppositeOther = level.getBlockState(relative);

          if (state.getBlock() == oppositeOther.getBlock()) {
            if (facing.getOpposite() == oppositeOther.getValue(HorizontalDirectionalBlock.FACING)) {
              if (level.getBestNeighborSignal(relative) >= Couplings.COUPLING_SIGNAL) {
                return true;
              }
            }
          }
        }
      }
    }
    
    return false;
  }
}
