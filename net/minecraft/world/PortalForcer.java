package net.minecraft.world;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;

public class PortalForcer {
   private final ServerWorld world;

   public PortalForcer(ServerWorld world) {
      this.world = world;
   }

   public Optional<PortalUtil.Rectangle> method_30483(BlockPos blockPos, boolean bl) {
      PointOfInterestStorage pointOfInterestStorage = this.world.getPointOfInterestStorage();
      int i = bl ? 16 : 128;
      pointOfInterestStorage.preloadChunks(this.world, blockPos, i);
      Optional<PointOfInterest> optional = pointOfInterestStorage.getInSquare((pointOfInterestType) -> {
         return pointOfInterestType == PointOfInterestType.NETHER_PORTAL;
      }, blockPos, i, PointOfInterestStorage.OccupationStatus.ANY).sorted(Comparator.comparingDouble((pointOfInterest) -> {
         return pointOfInterest.getPos().getSquaredDistance(blockPos);
      }).thenComparingInt((pointOfInterest) -> {
         return pointOfInterest.getPos().getY();
      })).filter((pointOfInterest) -> {
         return this.world.getBlockState(pointOfInterest.getPos()).contains(Properties.HORIZONTAL_AXIS);
      }).findFirst();
      return optional.map((pointOfInterest) -> {
         BlockPos blockPos = pointOfInterest.getPos();
         this.world.getChunkManager().addTicket(ChunkTicketType.PORTAL, new ChunkPos(blockPos), 3, blockPos);
         BlockState blockState = this.world.getBlockState(blockPos);
         return PortalUtil.getLargestRectangle(blockPos, (Direction.Axis)blockState.get(Properties.HORIZONTAL_AXIS), 21, Direction.Axis.Y, 21, (blockPosx) -> {
            return this.world.getBlockState(blockPosx) == blockState;
         });
      });
   }

   public Optional<PortalUtil.Rectangle> method_30482(BlockPos blockPos, Direction.Axis axis) {
      Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, axis);
      double d = -1.0D;
      BlockPos blockPos2 = null;
      double e = -1.0D;
      BlockPos blockPos3 = null;
      WorldBorder worldBorder = this.world.getWorldBorder();
      int i = this.world.getDimensionHeight() - 1;
      BlockPos.Mutable mutable = blockPos.mutableCopy();
      Iterator var13 = BlockPos.method_30512(blockPos, 16, Direction.EAST, Direction.SOUTH).iterator();

      while(true) {
         BlockPos.Mutable mutable2;
         int p;
         do {
            do {
               if (!var13.hasNext()) {
                  if (d == -1.0D && e != -1.0D) {
                     blockPos2 = blockPos3;
                     d = e;
                  }

                  int o;
                  if (d == -1.0D) {
                     blockPos2 = (new BlockPos(blockPos.getX(), MathHelper.clamp(blockPos.getY(), 70, this.world.getDimensionHeight() - 10), blockPos.getZ())).toImmutable();
                     Direction direction2 = direction.rotateYClockwise();
                     if (!worldBorder.contains(blockPos2)) {
                        return Optional.empty();
                     }

                     for(o = -1; o < 2; ++o) {
                        for(p = 0; p < 2; ++p) {
                           for(int q = -1; q < 3; ++q) {
                              BlockState blockState = q < 0 ? Blocks.OBSIDIAN.getDefaultState() : Blocks.AIR.getDefaultState();
                              mutable.set((Vec3i)blockPos2, p * direction.getOffsetX() + o * direction2.getOffsetX(), q, p * direction.getOffsetZ() + o * direction2.getOffsetZ());
                              this.world.setBlockState(mutable, blockState);
                           }
                        }
                     }
                  }

                  for(int r = -1; r < 3; ++r) {
                     for(o = -1; o < 4; ++o) {
                        if (r == -1 || r == 2 || o == -1 || o == 3) {
                           mutable.set((Vec3i)blockPos2, r * direction.getOffsetX(), o, r * direction.getOffsetZ());
                           this.world.setBlockState(mutable, Blocks.OBSIDIAN.getDefaultState(), 3);
                        }
                     }
                  }

                  BlockState blockState2 = (BlockState)Blocks.NETHER_PORTAL.getDefaultState().with(NetherPortalBlock.AXIS, axis);

                  for(o = 0; o < 2; ++o) {
                     for(p = 0; p < 3; ++p) {
                        mutable.set((Vec3i)blockPos2, o * direction.getOffsetX(), p, o * direction.getOffsetZ());
                        this.world.setBlockState(mutable, blockState2, 18);
                     }
                  }

                  return Optional.of(new PortalUtil.Rectangle(blockPos2.toImmutable(), 2, 3));
               }

               mutable2 = (BlockPos.Mutable)var13.next();
               p = Math.min(i, this.world.getTopY(Heightmap.Type.MOTION_BLOCKING, mutable2.getX(), mutable2.getZ()));
               int k = true;
            } while(!worldBorder.contains((BlockPos)mutable2));
         } while(!worldBorder.contains((BlockPos)mutable2.move(direction, 1)));

         mutable2.move(direction.getOpposite(), 1);

         for(int l = p; l >= 0; --l) {
            mutable2.setY(l);
            if (this.world.isAir(mutable2)) {
               int m;
               for(m = l; l > 0 && this.world.isAir(mutable2.move(Direction.DOWN)); --l) {
               }

               if (l + 4 <= i) {
                  int n = m - l;
                  if (n <= 0 || n >= 3) {
                     mutable2.setY(l);
                     if (this.method_30481(mutable2, mutable, direction, 0)) {
                        double f = blockPos.getSquaredDistance(mutable2);
                        if (this.method_30481(mutable2, mutable, direction, -1) && this.method_30481(mutable2, mutable, direction, 1) && (d == -1.0D || d > f)) {
                           d = f;
                           blockPos2 = mutable2.toImmutable();
                        }

                        if (d == -1.0D && (e == -1.0D || e > f)) {
                           e = f;
                           blockPos3 = mutable2.toImmutable();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean method_30481(BlockPos blockPos, BlockPos.Mutable mutable, Direction direction, int i) {
      Direction direction2 = direction.rotateYClockwise();

      for(int j = -1; j < 3; ++j) {
         for(int k = -1; k < 4; ++k) {
            mutable.set((Vec3i)blockPos, direction.getOffsetX() * j + direction2.getOffsetX() * i, k, direction.getOffsetZ() * j + direction2.getOffsetZ() * i);
            if (k < 0 && !this.world.getBlockState(mutable).getMaterial().isSolid()) {
               return false;
            }

            if (k >= 0 && !this.world.isAir(mutable)) {
               return false;
            }
         }
      }

      return true;
   }
}
