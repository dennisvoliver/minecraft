package net.minecraft.world.gen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.block.BlockState;
import net.minecraft.structure.RuinedPortalStructurePiece;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class RuinedPortalFeature extends StructureFeature<RuinedPortalFeatureConfig> {
   private static final String[] COMMON_PORTAL_STRUCTURE_IDS = new String[]{"ruined_portal/portal_1", "ruined_portal/portal_2", "ruined_portal/portal_3", "ruined_portal/portal_4", "ruined_portal/portal_5", "ruined_portal/portal_6", "ruined_portal/portal_7", "ruined_portal/portal_8", "ruined_portal/portal_9", "ruined_portal/portal_10"};
   private static final String[] RARE_PORTAL_STRUCTURE_IDS = new String[]{"ruined_portal/giant_portal_1", "ruined_portal/giant_portal_2", "ruined_portal/giant_portal_3"};

   public RuinedPortalFeature(Codec<RuinedPortalFeatureConfig> codec) {
      super(codec);
   }

   public StructureFeature.StructureStartFactory<RuinedPortalFeatureConfig> getStructureStartFactory() {
      return RuinedPortalFeature.Start::new;
   }

   private static boolean method_27209(BlockPos blockPos, Biome biome) {
      return biome.getTemperature(blockPos) < 0.15F;
   }

   private static int method_27211(Random random, ChunkGenerator chunkGenerator, RuinedPortalStructurePiece.VerticalPlacement verticalPlacement, boolean bl, int i, int j, BlockBox blockBox) {
      int s;
      if (verticalPlacement == RuinedPortalStructurePiece.VerticalPlacement.IN_NETHER) {
         if (bl) {
            s = choose(random, 32, 100);
         } else if (random.nextFloat() < 0.5F) {
            s = choose(random, 27, 29);
         } else {
            s = choose(random, 29, 100);
         }
      } else {
         int p;
         if (verticalPlacement == RuinedPortalStructurePiece.VerticalPlacement.IN_MOUNTAIN) {
            p = i - j;
            s = choosePlacementHeight(random, 70, p);
         } else if (verticalPlacement == RuinedPortalStructurePiece.VerticalPlacement.UNDERGROUND) {
            p = i - j;
            s = choosePlacementHeight(random, 15, p);
         } else if (verticalPlacement == RuinedPortalStructurePiece.VerticalPlacement.PARTLY_BURIED) {
            s = i - j + choose(random, 2, 8);
         } else {
            s = i;
         }
      }

      List<BlockPos> list = ImmutableList.of(new BlockPos(blockBox.minX, 0, blockBox.minZ), new BlockPos(blockBox.maxX, 0, blockBox.minZ), new BlockPos(blockBox.minX, 0, blockBox.maxZ), new BlockPos(blockBox.maxX, 0, blockBox.maxZ));
      List<BlockView> list2 = (List)list.stream().map((blockPos) -> {
         return chunkGenerator.getColumnSample(blockPos.getX(), blockPos.getZ());
      }).collect(Collectors.toList());
      Heightmap.Type type = verticalPlacement == RuinedPortalStructurePiece.VerticalPlacement.ON_OCEAN_FLOOR ? Heightmap.Type.OCEAN_FLOOR_WG : Heightmap.Type.WORLD_SURFACE_WG;
      BlockPos.Mutable mutable = new BlockPos.Mutable();

      int t;
      for(t = s; t > 15; --t) {
         int u = 0;
         mutable.set(0, t, 0);
         Iterator var14 = list2.iterator();

         while(var14.hasNext()) {
            BlockView blockView = (BlockView)var14.next();
            BlockState blockState = blockView.getBlockState(mutable);
            if (blockState != null && type.getBlockPredicate().test(blockState)) {
               ++u;
               if (u == 3) {
                  return t;
               }
            }
         }
      }

      return t;
   }

   private static int choose(Random random, int min, int max) {
      return random.nextInt(max - min + 1) + min;
   }

   private static int choosePlacementHeight(Random random, int min, int max) {
      return min < max ? choose(random, min, max) : max;
   }

   public static enum Type implements StringIdentifiable {
      STANDARD("standard"),
      DESERT("desert"),
      JUNGLE("jungle"),
      SWAMP("swamp"),
      MOUNTAIN("mountain"),
      OCEAN("ocean"),
      NETHER("nether");

      public static final Codec<RuinedPortalFeature.Type> CODEC = StringIdentifiable.createCodec(RuinedPortalFeature.Type::values, RuinedPortalFeature.Type::byName);
      private static final Map<String, RuinedPortalFeature.Type> BY_NAME = (Map)Arrays.stream(values()).collect(Collectors.toMap(RuinedPortalFeature.Type::getName, (type) -> {
         return type;
      }));
      private final String name;

      private Type(String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      public static RuinedPortalFeature.Type byName(String name) {
         return (RuinedPortalFeature.Type)BY_NAME.get(name);
      }

      public String asString() {
         return this.name;
      }
   }

   public static class Start extends StructureStart<RuinedPortalFeatureConfig> {
      protected Start(StructureFeature<RuinedPortalFeatureConfig> structureFeature, int i, int j, BlockBox blockBox, int k, long l) {
         super(structureFeature, i, j, blockBox, k, l);
      }

      public void init(DynamicRegistryManager dynamicRegistryManager, ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int j, Biome biome, RuinedPortalFeatureConfig ruinedPortalFeatureConfig) {
         RuinedPortalStructurePiece.Properties properties = new RuinedPortalStructurePiece.Properties();
         RuinedPortalStructurePiece.VerticalPlacement verticalPlacement6;
         if (ruinedPortalFeatureConfig.portalType == RuinedPortalFeature.Type.DESERT) {
            verticalPlacement6 = RuinedPortalStructurePiece.VerticalPlacement.PARTLY_BURIED;
            properties.airPocket = false;
            properties.mossiness = 0.0F;
         } else if (ruinedPortalFeatureConfig.portalType == RuinedPortalFeature.Type.JUNGLE) {
            verticalPlacement6 = RuinedPortalStructurePiece.VerticalPlacement.ON_LAND_SURFACE;
            properties.airPocket = this.random.nextFloat() < 0.5F;
            properties.mossiness = 0.8F;
            properties.overgrown = true;
            properties.vines = true;
         } else if (ruinedPortalFeatureConfig.portalType == RuinedPortalFeature.Type.SWAMP) {
            verticalPlacement6 = RuinedPortalStructurePiece.VerticalPlacement.ON_OCEAN_FLOOR;
            properties.airPocket = false;
            properties.mossiness = 0.5F;
            properties.vines = true;
         } else {
            boolean bl2;
            if (ruinedPortalFeatureConfig.portalType == RuinedPortalFeature.Type.MOUNTAIN) {
               bl2 = this.random.nextFloat() < 0.5F;
               verticalPlacement6 = bl2 ? RuinedPortalStructurePiece.VerticalPlacement.IN_MOUNTAIN : RuinedPortalStructurePiece.VerticalPlacement.ON_LAND_SURFACE;
               properties.airPocket = bl2 || this.random.nextFloat() < 0.5F;
            } else if (ruinedPortalFeatureConfig.portalType == RuinedPortalFeature.Type.OCEAN) {
               verticalPlacement6 = RuinedPortalStructurePiece.VerticalPlacement.ON_OCEAN_FLOOR;
               properties.airPocket = false;
               properties.mossiness = 0.8F;
            } else if (ruinedPortalFeatureConfig.portalType == RuinedPortalFeature.Type.NETHER) {
               verticalPlacement6 = RuinedPortalStructurePiece.VerticalPlacement.IN_NETHER;
               properties.airPocket = this.random.nextFloat() < 0.5F;
               properties.mossiness = 0.0F;
               properties.replaceWithBlackstone = true;
            } else {
               bl2 = this.random.nextFloat() < 0.5F;
               verticalPlacement6 = bl2 ? RuinedPortalStructurePiece.VerticalPlacement.UNDERGROUND : RuinedPortalStructurePiece.VerticalPlacement.ON_LAND_SURFACE;
               properties.airPocket = bl2 || this.random.nextFloat() < 0.5F;
            }
         }

         Identifier identifier2;
         if (this.random.nextFloat() < 0.05F) {
            identifier2 = new Identifier(RuinedPortalFeature.RARE_PORTAL_STRUCTURE_IDS[this.random.nextInt(RuinedPortalFeature.RARE_PORTAL_STRUCTURE_IDS.length)]);
         } else {
            identifier2 = new Identifier(RuinedPortalFeature.COMMON_PORTAL_STRUCTURE_IDS[this.random.nextInt(RuinedPortalFeature.COMMON_PORTAL_STRUCTURE_IDS.length)]);
         }

         Structure structure = structureManager.getStructureOrBlank(identifier2);
         BlockRotation blockRotation = (BlockRotation)Util.getRandom((Object[])BlockRotation.values(), this.random);
         BlockMirror blockMirror = this.random.nextFloat() < 0.5F ? BlockMirror.NONE : BlockMirror.FRONT_BACK;
         BlockPos blockPos = new BlockPos(structure.getSize().getX() / 2, 0, structure.getSize().getZ() / 2);
         BlockPos blockPos2 = (new ChunkPos(i, j)).getStartPos();
         BlockBox blockBox = structure.method_27267(blockPos2, blockRotation, blockPos, blockMirror);
         Vec3i vec3i = blockBox.getCenter();
         int k = vec3i.getX();
         int l = vec3i.getZ();
         int m = chunkGenerator.getHeight(k, l, RuinedPortalStructurePiece.getHeightmapType(verticalPlacement6)) - 1;
         int n = RuinedPortalFeature.method_27211(this.random, chunkGenerator, verticalPlacement6, properties.airPocket, m, blockBox.getBlockCountY(), blockBox);
         BlockPos blockPos3 = new BlockPos(blockPos2.getX(), n, blockPos2.getZ());
         if (ruinedPortalFeatureConfig.portalType == RuinedPortalFeature.Type.MOUNTAIN || ruinedPortalFeatureConfig.portalType == RuinedPortalFeature.Type.OCEAN || ruinedPortalFeatureConfig.portalType == RuinedPortalFeature.Type.STANDARD) {
            properties.cold = RuinedPortalFeature.method_27209(blockPos3, biome);
         }

         this.children.add(new RuinedPortalStructurePiece(blockPos3, verticalPlacement6, properties, identifier2, structure, blockRotation, blockMirror, blockPos));
         this.setBoundingBoxFromChildren();
      }
   }
}
