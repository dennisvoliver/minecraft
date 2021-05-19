package net.minecraft.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MaterialColor;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

public class FilledMapItem extends NetworkSyncedItem {
   public FilledMapItem(Item.Settings settings) {
      super(settings);
   }

   public static ItemStack createMap(World world, int x, int z, byte scale, boolean showIcons, boolean unlimitedTracking) {
      ItemStack itemStack = new ItemStack(Items.FILLED_MAP);
      createMapState(itemStack, world, x, z, scale, showIcons, unlimitedTracking, world.getRegistryKey());
      return itemStack;
   }

   @Nullable
   public static MapState getMapState(ItemStack stack, World world) {
      return world.getMapState(getMapName(getMapId(stack)));
   }

   @Nullable
   public static MapState getOrCreateMapState(ItemStack map, World world) {
      MapState mapState = getMapState(map, world);
      if (mapState == null && world instanceof ServerWorld) {
         mapState = createMapState(map, world, world.getLevelProperties().getSpawnX(), world.getLevelProperties().getSpawnZ(), 3, false, false, world.getRegistryKey());
      }

      return mapState;
   }

   public static int getMapId(ItemStack stack) {
      CompoundTag compoundTag = stack.getTag();
      return compoundTag != null && compoundTag.contains("map", 99) ? compoundTag.getInt("map") : 0;
   }

   private static MapState createMapState(ItemStack stack, World world, int x, int z, int scale, boolean showIcons, boolean unlimitedTracking, RegistryKey<World> dimension) {
      int i = world.getNextMapId();
      MapState mapState = new MapState(getMapName(i));
      mapState.init(x, z, scale, showIcons, unlimitedTracking, dimension);
      world.putMapState(mapState);
      stack.getOrCreateTag().putInt("map", i);
      return mapState;
   }

   public static String getMapName(int mapId) {
      return "map_" + mapId;
   }

   public void updateColors(World world, Entity entity, MapState state) {
      if (world.getRegistryKey() == state.dimension && entity instanceof PlayerEntity) {
         int i = 1 << state.scale;
         int j = state.xCenter;
         int k = state.zCenter;
         int l = MathHelper.floor(entity.getX() - (double)j) / i + 64;
         int m = MathHelper.floor(entity.getZ() - (double)k) / i + 64;
         int n = 128 / i;
         if (world.getDimension().hasCeiling()) {
            n /= 2;
         }

         MapState.PlayerUpdateTracker playerUpdateTracker = state.getPlayerSyncData((PlayerEntity)entity);
         ++playerUpdateTracker.field_131;
         boolean bl = false;

         for(int o = l - n + 1; o < l + n; ++o) {
            if ((o & 15) == (playerUpdateTracker.field_131 & 15) || bl) {
               bl = false;
               double d = 0.0D;

               for(int p = m - n - 1; p < m + n; ++p) {
                  if (o >= 0 && p >= -1 && o < 128 && p < 128) {
                     int q = o - l;
                     int r = p - m;
                     boolean bl2 = q * q + r * r > (n - 2) * (n - 2);
                     int s = (j / i + o - 64) * i;
                     int t = (k / i + p - 64) * i;
                     Multiset<MaterialColor> multiset = LinkedHashMultiset.create();
                     WorldChunk worldChunk = world.getWorldChunk(new BlockPos(s, 0, t));
                     if (!worldChunk.isEmpty()) {
                        ChunkPos chunkPos = worldChunk.getPos();
                        int u = s & 15;
                        int v = t & 15;
                        int w = 0;
                        double e = 0.0D;
                        if (world.getDimension().hasCeiling()) {
                           int x = s + t * 231871;
                           x = x * x * 31287121 + x * 11;
                           if ((x >> 20 & 1) == 0) {
                              multiset.add(Blocks.DIRT.getDefaultState().getTopMaterialColor(world, BlockPos.ORIGIN), 10);
                           } else {
                              multiset.add(Blocks.STONE.getDefaultState().getTopMaterialColor(world, BlockPos.ORIGIN), 100);
                           }

                           e = 100.0D;
                        } else {
                           BlockPos.Mutable mutable = new BlockPos.Mutable();
                           BlockPos.Mutable mutable2 = new BlockPos.Mutable();

                           for(int y = 0; y < i; ++y) {
                              for(int z = 0; z < i; ++z) {
                                 int aa = worldChunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, y + u, z + v) + 1;
                                 BlockState blockState;
                                 if (aa <= 1) {
                                    blockState = Blocks.BEDROCK.getDefaultState();
                                 } else {
                                    do {
                                       --aa;
                                       mutable.set(chunkPos.getStartX() + y + u, aa, chunkPos.getStartZ() + z + v);
                                       blockState = worldChunk.getBlockState(mutable);
                                    } while(blockState.getTopMaterialColor(world, mutable) == MaterialColor.CLEAR && aa > 0);

                                    if (aa > 0 && !blockState.getFluidState().isEmpty()) {
                                       int ab = aa - 1;
                                       mutable2.set(mutable);

                                       BlockState blockState2;
                                       do {
                                          mutable2.setY(ab--);
                                          blockState2 = worldChunk.getBlockState(mutable2);
                                          ++w;
                                       } while(ab > 0 && !blockState2.getFluidState().isEmpty());

                                       blockState = this.getFluidStateIfVisible(world, blockState, mutable);
                                    }
                                 }

                                 state.removeBanner(world, chunkPos.getStartX() + y + u, chunkPos.getStartZ() + z + v);
                                 e += (double)aa / (double)(i * i);
                                 multiset.add(blockState.getTopMaterialColor(world, mutable));
                              }
                           }
                        }

                        w /= i * i;
                        double f = (e - d) * 4.0D / (double)(i + 4) + ((double)(o + p & 1) - 0.5D) * 0.4D;
                        int ac = 1;
                        if (f > 0.6D) {
                           ac = 2;
                        }

                        if (f < -0.6D) {
                           ac = 0;
                        }

                        MaterialColor materialColor = (MaterialColor)Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MaterialColor.CLEAR);
                        if (materialColor == MaterialColor.WATER) {
                           f = (double)w * 0.1D + (double)(o + p & 1) * 0.2D;
                           ac = 1;
                           if (f < 0.5D) {
                              ac = 2;
                           }

                           if (f > 0.9D) {
                              ac = 0;
                           }
                        }

                        d = e;
                        if (p >= 0 && q * q + r * r < n * n && (!bl2 || (o + p & 1) != 0)) {
                           byte b = state.colors[o + p * 128];
                           byte c = (byte)(materialColor.id * 4 + ac);
                           if (b != c) {
                              state.colors[o + p * 128] = c;
                              state.markDirty(o, p);
                              bl = true;
                           }
                        }
                     }
                  }
               }
            }
         }

      }
   }

   private BlockState getFluidStateIfVisible(World world, BlockState state, BlockPos pos) {
      FluidState fluidState = state.getFluidState();
      return !fluidState.isEmpty() && !state.isSideSolidFullSquare(world, pos, Direction.UP) ? fluidState.getBlockState() : state;
   }

   private static boolean hasPositiveDepth(Biome[] biomes, int scale, int x, int z) {
      return biomes[x * scale + z * scale * 128 * scale].getDepth() >= 0.0F;
   }

   public static void fillExplorationMap(ServerWorld serverWorld, ItemStack map) {
      MapState mapState = getOrCreateMapState(map, serverWorld);
      if (mapState != null) {
         if (serverWorld.getRegistryKey() == mapState.dimension) {
            int i = 1 << mapState.scale;
            int j = mapState.xCenter;
            int k = mapState.zCenter;
            Biome[] biomes = new Biome[128 * i * 128 * i];

            int n;
            int o;
            for(n = 0; n < 128 * i; ++n) {
               for(o = 0; o < 128 * i; ++o) {
                  biomes[n * 128 * i + o] = serverWorld.getBiome(new BlockPos((j / i - 64) * i + o, 0, (k / i - 64) * i + n));
               }
            }

            for(n = 0; n < 128; ++n) {
               for(o = 0; o < 128; ++o) {
                  if (n > 0 && o > 0 && n < 127 && o < 127) {
                     Biome biome = biomes[n * i + o * i * 128 * i];
                     int p = 8;
                     if (hasPositiveDepth(biomes, i, n - 1, o - 1)) {
                        --p;
                     }

                     if (hasPositiveDepth(biomes, i, n - 1, o + 1)) {
                        --p;
                     }

                     if (hasPositiveDepth(biomes, i, n - 1, o)) {
                        --p;
                     }

                     if (hasPositiveDepth(biomes, i, n + 1, o - 1)) {
                        --p;
                     }

                     if (hasPositiveDepth(biomes, i, n + 1, o + 1)) {
                        --p;
                     }

                     if (hasPositiveDepth(biomes, i, n + 1, o)) {
                        --p;
                     }

                     if (hasPositiveDepth(biomes, i, n, o - 1)) {
                        --p;
                     }

                     if (hasPositiveDepth(biomes, i, n, o + 1)) {
                        --p;
                     }

                     int q = 3;
                     MaterialColor materialColor = MaterialColor.CLEAR;
                     if (biome.getDepth() < 0.0F) {
                        materialColor = MaterialColor.ORANGE;
                        if (p > 7 && o % 2 == 0) {
                           q = (n + (int)(MathHelper.sin((float)o + 0.0F) * 7.0F)) / 8 % 5;
                           if (q == 3) {
                              q = 1;
                           } else if (q == 4) {
                              q = 0;
                           }
                        } else if (p > 7) {
                           materialColor = MaterialColor.CLEAR;
                        } else if (p > 5) {
                           q = 1;
                        } else if (p > 3) {
                           q = 0;
                        } else if (p > 1) {
                           q = 0;
                        }
                     } else if (p > 0) {
                        materialColor = MaterialColor.BROWN;
                        if (p > 3) {
                           q = 1;
                        } else {
                           q = 3;
                        }
                     }

                     if (materialColor != MaterialColor.CLEAR) {
                        mapState.colors[n + o * 128] = (byte)(materialColor.id * 4 + q);
                        mapState.markDirty(n, o);
                     }
                  }
               }
            }

         }
      }
   }

   public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
      if (!world.isClient) {
         MapState mapState = getOrCreateMapState(stack, world);
         if (mapState != null) {
            if (entity instanceof PlayerEntity) {
               PlayerEntity playerEntity = (PlayerEntity)entity;
               mapState.update(playerEntity, stack);
            }

            if (!mapState.locked && (selected || entity instanceof PlayerEntity && ((PlayerEntity)entity).getOffHandStack() == stack)) {
               this.updateColors(world, entity, mapState);
            }

         }
      }
   }

   @Nullable
   public Packet<?> createSyncPacket(ItemStack stack, World world, PlayerEntity player) {
      return getOrCreateMapState(stack, world).getPlayerMarkerPacket(stack, world, player);
   }

   public void onCraft(ItemStack stack, World world, PlayerEntity player) {
      CompoundTag compoundTag = stack.getTag();
      if (compoundTag != null && compoundTag.contains("map_scale_direction", 99)) {
         scale(stack, world, compoundTag.getInt("map_scale_direction"));
         compoundTag.remove("map_scale_direction");
      } else if (compoundTag != null && compoundTag.contains("map_to_lock", 1) && compoundTag.getBoolean("map_to_lock")) {
         copyMap(world, stack);
         compoundTag.remove("map_to_lock");
      }

   }

   protected static void scale(ItemStack map, World world, int amount) {
      MapState mapState = getOrCreateMapState(map, world);
      if (mapState != null) {
         createMapState(map, world, mapState.xCenter, mapState.zCenter, MathHelper.clamp(mapState.scale + amount, 0, 4), mapState.showIcons, mapState.unlimitedTracking, mapState.dimension);
      }

   }

   public static void copyMap(World world, ItemStack stack) {
      MapState mapState = getOrCreateMapState(stack, world);
      if (mapState != null) {
         MapState mapState2 = createMapState(stack, world, 0, 0, mapState.scale, mapState.showIcons, mapState.unlimitedTracking, mapState.dimension);
         mapState2.copyFrom(mapState);
      }

   }

   @Environment(EnvType.CLIENT)
   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      MapState mapState = world == null ? null : getOrCreateMapState(stack, world);
      if (mapState != null && mapState.locked) {
         tooltip.add((new TranslatableText("filled_map.locked", new Object[]{getMapId(stack)})).formatted(Formatting.GRAY));
      }

      if (context.isAdvanced()) {
         if (mapState != null) {
            tooltip.add((new TranslatableText("filled_map.id", new Object[]{getMapId(stack)})).formatted(Formatting.GRAY));
            tooltip.add((new TranslatableText("filled_map.scale", new Object[]{1 << mapState.scale})).formatted(Formatting.GRAY));
            tooltip.add((new TranslatableText("filled_map.level", new Object[]{mapState.scale, 4})).formatted(Formatting.GRAY));
         } else {
            tooltip.add((new TranslatableText("filled_map.unknown")).formatted(Formatting.GRAY));
         }
      }

   }

   @Environment(EnvType.CLIENT)
   public static int getMapColor(ItemStack stack) {
      CompoundTag compoundTag = stack.getSubTag("display");
      if (compoundTag != null && compoundTag.contains("MapColor", 99)) {
         int i = compoundTag.getInt("MapColor");
         return -16777216 | i & 16777215;
      } else {
         return -12173266;
      }
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      BlockState blockState = context.getWorld().getBlockState(context.getBlockPos());
      if (blockState.isIn(BlockTags.BANNERS)) {
         if (!context.getWorld().isClient) {
            MapState mapState = getOrCreateMapState(context.getStack(), context.getWorld());
            mapState.addBanner(context.getWorld(), context.getBlockPos());
         }

         return ActionResult.success(context.getWorld().isClient);
      } else {
         return super.useOnBlock(context);
      }
   }
}
