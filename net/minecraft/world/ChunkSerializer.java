package net.minecraft.world;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.SimpleTickScheduler;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ChunkSerializer {
   private static final Logger LOGGER = LogManager.getLogger();

   public static ProtoChunk deserialize(ServerWorld world, StructureManager structureManager, PointOfInterestStorage poiStorage, ChunkPos pos, CompoundTag tag) {
      ChunkGenerator chunkGenerator = world.getChunkManager().getChunkGenerator();
      BiomeSource biomeSource = chunkGenerator.getBiomeSource();
      CompoundTag compoundTag = tag.getCompound("Level");
      ChunkPos chunkPos = new ChunkPos(compoundTag.getInt("xPos"), compoundTag.getInt("zPos"));
      if (!Objects.equals(pos, chunkPos)) {
         LOGGER.error((String)"Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", (Object)pos, pos, chunkPos);
      }

      BiomeArray biomeArray = new BiomeArray(world.getRegistryManager().get(Registry.BIOME_KEY), pos, biomeSource, compoundTag.contains("Biomes", 11) ? compoundTag.getIntArray("Biomes") : null);
      UpgradeData upgradeData = compoundTag.contains("UpgradeData", 10) ? new UpgradeData(compoundTag.getCompound("UpgradeData")) : UpgradeData.NO_UPGRADE_DATA;
      ChunkTickScheduler<Block> chunkTickScheduler = new ChunkTickScheduler((block) -> {
         return block == null || block.getDefaultState().isAir();
      }, pos, compoundTag.getList("ToBeTicked", 9));
      ChunkTickScheduler<Fluid> chunkTickScheduler2 = new ChunkTickScheduler((fluid) -> {
         return fluid == null || fluid == Fluids.EMPTY;
      }, pos, compoundTag.getList("LiquidsToBeTicked", 9));
      boolean bl = compoundTag.getBoolean("isLightOn");
      ListTag listTag = compoundTag.getList("Sections", 10);
      int i = true;
      ChunkSection[] chunkSections = new ChunkSection[16];
      boolean bl2 = world.getDimension().hasSkyLight();
      ChunkManager chunkManager = world.getChunkManager();
      LightingProvider lightingProvider = chunkManager.getLightingProvider();
      if (bl) {
         lightingProvider.setRetainData(pos, true);
      }

      for(int j = 0; j < listTag.size(); ++j) {
         CompoundTag compoundTag2 = listTag.getCompound(j);
         int k = compoundTag2.getByte("Y");
         if (compoundTag2.contains("Palette", 9) && compoundTag2.contains("BlockStates", 12)) {
            ChunkSection chunkSection = new ChunkSection(k << 4);
            chunkSection.getContainer().read(compoundTag2.getList("Palette", 10), compoundTag2.getLongArray("BlockStates"));
            chunkSection.calculateCounts();
            if (!chunkSection.isEmpty()) {
               chunkSections[k] = chunkSection;
            }

            poiStorage.initForPalette(pos, chunkSection);
         }

         if (bl) {
            if (compoundTag2.contains("BlockLight", 7)) {
               lightingProvider.enqueueSectionData(LightType.BLOCK, ChunkSectionPos.from(pos, k), new ChunkNibbleArray(compoundTag2.getByteArray("BlockLight")), true);
            }

            if (bl2 && compoundTag2.contains("SkyLight", 7)) {
               lightingProvider.enqueueSectionData(LightType.SKY, ChunkSectionPos.from(pos, k), new ChunkNibbleArray(compoundTag2.getByteArray("SkyLight")), true);
            }
         }
      }

      long l = compoundTag.getLong("InhabitedTime");
      ChunkStatus.ChunkType chunkType = getChunkType(tag);
      Object chunk2;
      if (chunkType == ChunkStatus.ChunkType.field_12807) {
         ListTag var10000;
         Function var10001;
         DefaultedRegistry var10002;
         Object tickScheduler2;
         if (compoundTag.contains("TileTicks", 9)) {
            var10000 = compoundTag.getList("TileTicks", 10);
            var10001 = Registry.BLOCK::getId;
            var10002 = Registry.BLOCK;
            var10002.getClass();
            tickScheduler2 = SimpleTickScheduler.fromNbt(var10000, var10001, var10002::get);
         } else {
            tickScheduler2 = chunkTickScheduler;
         }

         Object tickScheduler4;
         if (compoundTag.contains("LiquidTicks", 9)) {
            var10000 = compoundTag.getList("LiquidTicks", 10);
            var10001 = Registry.FLUID::getId;
            var10002 = Registry.FLUID;
            var10002.getClass();
            tickScheduler4 = SimpleTickScheduler.fromNbt(var10000, var10001, var10002::get);
         } else {
            tickScheduler4 = chunkTickScheduler2;
         }

         chunk2 = new WorldChunk(world.toServerWorld(), pos, biomeArray, upgradeData, (TickScheduler)tickScheduler2, (TickScheduler)tickScheduler4, l, chunkSections, (chunk) -> {
            writeEntities(compoundTag, chunk);
         });
      } else {
         ProtoChunk protoChunk = new ProtoChunk(pos, upgradeData, chunkSections, chunkTickScheduler, chunkTickScheduler2);
         protoChunk.setBiomes(biomeArray);
         chunk2 = protoChunk;
         protoChunk.setInhabitedTime(l);
         protoChunk.setStatus(ChunkStatus.byId(compoundTag.getString("Status")));
         if (protoChunk.getStatus().isAtLeast(ChunkStatus.FEATURES)) {
            protoChunk.setLightingProvider(lightingProvider);
         }

         if (!bl && protoChunk.getStatus().isAtLeast(ChunkStatus.LIGHT)) {
            Iterator var41 = BlockPos.iterate(pos.getStartX(), 0, pos.getStartZ(), pos.getEndX(), 255, pos.getEndZ()).iterator();

            while(var41.hasNext()) {
               BlockPos blockPos = (BlockPos)var41.next();
               if (((Chunk)chunk2).getBlockState(blockPos).getLuminance() != 0) {
                  protoChunk.addLightSource(blockPos);
               }
            }
         }
      }

      ((Chunk)chunk2).setLightOn(bl);
      CompoundTag compoundTag3 = compoundTag.getCompound("Heightmaps");
      EnumSet<Heightmap.Type> enumSet = EnumSet.noneOf(Heightmap.Type.class);
      Iterator var43 = ((Chunk)chunk2).getStatus().getHeightmapTypes().iterator();

      while(var43.hasNext()) {
         Heightmap.Type type = (Heightmap.Type)var43.next();
         String string = type.getName();
         if (compoundTag3.contains(string, 12)) {
            ((Chunk)chunk2).setHeightmap(type, compoundTag3.getLongArray(string));
         } else {
            enumSet.add(type);
         }
      }

      Heightmap.populateHeightmaps((Chunk)chunk2, enumSet);
      CompoundTag compoundTag4 = compoundTag.getCompound("Structures");
      ((Chunk)chunk2).setStructureStarts(readStructureStarts(structureManager, compoundTag4, world.getSeed()));
      ((Chunk)chunk2).setStructureReferences(readStructureReferences(pos, compoundTag4));
      if (compoundTag.getBoolean("shouldSave")) {
         ((Chunk)chunk2).setShouldSave(true);
      }

      ListTag listTag2 = compoundTag.getList("PostProcessing", 9);

      ListTag listTag4;
      int o;
      for(int m = 0; m < listTag2.size(); ++m) {
         listTag4 = listTag2.getList(m);

         for(o = 0; o < listTag4.size(); ++o) {
            ((Chunk)chunk2).markBlockForPostProcessing(listTag4.getShort(o), m);
         }
      }

      if (chunkType == ChunkStatus.ChunkType.field_12807) {
         return new ReadOnlyChunk((WorldChunk)chunk2);
      } else {
         ProtoChunk protoChunk2 = (ProtoChunk)chunk2;
         listTag4 = compoundTag.getList("Entities", 10);

         for(o = 0; o < listTag4.size(); ++o) {
            protoChunk2.addEntity(listTag4.getCompound(o));
         }

         ListTag listTag5 = compoundTag.getList("TileEntities", 10);

         CompoundTag compoundTag6;
         for(int p = 0; p < listTag5.size(); ++p) {
            compoundTag6 = listTag5.getCompound(p);
            ((Chunk)chunk2).addPendingBlockEntityTag(compoundTag6);
         }

         ListTag listTag6 = compoundTag.getList("Lights", 9);

         for(int q = 0; q < listTag6.size(); ++q) {
            ListTag listTag7 = listTag6.getList(q);

            for(int r = 0; r < listTag7.size(); ++r) {
               protoChunk2.addLightSource(listTag7.getShort(r), q);
            }
         }

         compoundTag6 = compoundTag.getCompound("CarvingMasks");
         Iterator var51 = compoundTag6.getKeys().iterator();

         while(var51.hasNext()) {
            String string2 = (String)var51.next();
            GenerationStep.Carver carver = GenerationStep.Carver.valueOf(string2);
            protoChunk2.setCarvingMask(carver, BitSet.valueOf(compoundTag6.getByteArray(string2)));
         }

         return protoChunk2;
      }
   }

   public static CompoundTag serialize(ServerWorld world, Chunk chunk) {
      ChunkPos chunkPos = chunk.getPos();
      CompoundTag compoundTag = new CompoundTag();
      CompoundTag compoundTag2 = new CompoundTag();
      compoundTag.putInt("DataVersion", SharedConstants.getGameVersion().getWorldVersion());
      compoundTag.put("Level", compoundTag2);
      compoundTag2.putInt("xPos", chunkPos.x);
      compoundTag2.putInt("zPos", chunkPos.z);
      compoundTag2.putLong("LastUpdate", world.getTime());
      compoundTag2.putLong("InhabitedTime", chunk.getInhabitedTime());
      compoundTag2.putString("Status", chunk.getStatus().getId());
      UpgradeData upgradeData = chunk.getUpgradeData();
      if (!upgradeData.isDone()) {
         compoundTag2.put("UpgradeData", upgradeData.toTag());
      }

      ChunkSection[] chunkSections = chunk.getSectionArray();
      ListTag listTag = new ListTag();
      LightingProvider lightingProvider = world.getChunkManager().getLightingProvider();
      boolean bl = chunk.isLightOn();

      CompoundTag compoundTag7;
      for(int i = -1; i < 17; ++i) {
         ChunkSection chunkSection = (ChunkSection)Arrays.stream(chunkSections).filter((chunkSectionx) -> {
            return chunkSectionx != null && chunkSectionx.getYOffset() >> 4 == i;
         }).findFirst().orElse(WorldChunk.EMPTY_SECTION);
         ChunkNibbleArray chunkNibbleArray = lightingProvider.get(LightType.BLOCK).getLightSection(ChunkSectionPos.from(chunkPos, i));
         ChunkNibbleArray chunkNibbleArray2 = lightingProvider.get(LightType.SKY).getLightSection(ChunkSectionPos.from(chunkPos, i));
         if (chunkSection != WorldChunk.EMPTY_SECTION || chunkNibbleArray != null || chunkNibbleArray2 != null) {
            compoundTag7 = new CompoundTag();
            compoundTag7.putByte("Y", (byte)(i & 255));
            if (chunkSection != WorldChunk.EMPTY_SECTION) {
               chunkSection.getContainer().write(compoundTag7, "Palette", "BlockStates");
            }

            if (chunkNibbleArray != null && !chunkNibbleArray.isUninitialized()) {
               compoundTag7.putByteArray("BlockLight", chunkNibbleArray.asByteArray());
            }

            if (chunkNibbleArray2 != null && !chunkNibbleArray2.isUninitialized()) {
               compoundTag7.putByteArray("SkyLight", chunkNibbleArray2.asByteArray());
            }

            listTag.add(compoundTag7);
         }
      }

      compoundTag2.put("Sections", listTag);
      if (bl) {
         compoundTag2.putBoolean("isLightOn", true);
      }

      BiomeArray biomeArray = chunk.getBiomeArray();
      if (biomeArray != null) {
         compoundTag2.putIntArray("Biomes", biomeArray.toIntArray());
      }

      ListTag listTag2 = new ListTag();
      Iterator var21 = chunk.getBlockEntityPositions().iterator();

      CompoundTag compoundTag6;
      while(var21.hasNext()) {
         BlockPos blockPos = (BlockPos)var21.next();
         compoundTag6 = chunk.getPackedBlockEntityTag(blockPos);
         if (compoundTag6 != null) {
            listTag2.add(compoundTag6);
         }
      }

      compoundTag2.put("TileEntities", listTag2);
      ListTag listTag3 = new ListTag();
      if (chunk.getStatus().getChunkType() == ChunkStatus.ChunkType.field_12807) {
         WorldChunk worldChunk = (WorldChunk)chunk;
         worldChunk.setUnsaved(false);

         for(int k = 0; k < worldChunk.getEntitySectionArray().length; ++k) {
            Iterator var29 = worldChunk.getEntitySectionArray()[k].iterator();

            while(var29.hasNext()) {
               Entity entity = (Entity)var29.next();
               CompoundTag compoundTag5 = new CompoundTag();
               if (entity.saveToTag(compoundTag5)) {
                  worldChunk.setUnsaved(true);
                  listTag3.add(compoundTag5);
               }
            }
         }
      } else {
         ProtoChunk protoChunk = (ProtoChunk)chunk;
         listTag3.addAll(protoChunk.getEntities());
         compoundTag2.put("Lights", toNbt(protoChunk.getLightSourcesBySection()));
         compoundTag6 = new CompoundTag();
         GenerationStep.Carver[] var30 = GenerationStep.Carver.values();
         int var32 = var30.length;

         for(int var34 = 0; var34 < var32; ++var34) {
            GenerationStep.Carver carver = var30[var34];
            BitSet bitSet = protoChunk.getCarvingMask(carver);
            if (bitSet != null) {
               compoundTag6.putByteArray(carver.toString(), bitSet.toByteArray());
            }
         }

         compoundTag2.put("CarvingMasks", compoundTag6);
      }

      compoundTag2.put("Entities", listTag3);
      TickScheduler<Block> tickScheduler = chunk.getBlockTickScheduler();
      if (tickScheduler instanceof ChunkTickScheduler) {
         compoundTag2.put("ToBeTicked", ((ChunkTickScheduler)tickScheduler).toNbt());
      } else if (tickScheduler instanceof SimpleTickScheduler) {
         compoundTag2.put("TileTicks", ((SimpleTickScheduler)tickScheduler).toNbt());
      } else {
         compoundTag2.put("TileTicks", world.getBlockTickScheduler().toTag(chunkPos));
      }

      TickScheduler<Fluid> tickScheduler2 = chunk.getFluidTickScheduler();
      if (tickScheduler2 instanceof ChunkTickScheduler) {
         compoundTag2.put("LiquidsToBeTicked", ((ChunkTickScheduler)tickScheduler2).toNbt());
      } else if (tickScheduler2 instanceof SimpleTickScheduler) {
         compoundTag2.put("LiquidTicks", ((SimpleTickScheduler)tickScheduler2).toNbt());
      } else {
         compoundTag2.put("LiquidTicks", world.getFluidTickScheduler().toTag(chunkPos));
      }

      compoundTag2.put("PostProcessing", toNbt(chunk.getPostProcessingLists()));
      compoundTag7 = new CompoundTag();
      Iterator var33 = chunk.getHeightmaps().iterator();

      while(var33.hasNext()) {
         Entry<Heightmap.Type, Heightmap> entry = (Entry)var33.next();
         if (chunk.getStatus().getHeightmapTypes().contains(entry.getKey())) {
            compoundTag7.put(((Heightmap.Type)entry.getKey()).getName(), new LongArrayTag(((Heightmap)entry.getValue()).asLongArray()));
         }
      }

      compoundTag2.put("Heightmaps", compoundTag7);
      compoundTag2.put("Structures", writeStructures(chunkPos, chunk.getStructureStarts(), chunk.getStructureReferences()));
      return compoundTag;
   }

   public static ChunkStatus.ChunkType getChunkType(@Nullable CompoundTag tag) {
      if (tag != null) {
         ChunkStatus chunkStatus = ChunkStatus.byId(tag.getCompound("Level").getString("Status"));
         if (chunkStatus != null) {
            return chunkStatus.getChunkType();
         }
      }

      return ChunkStatus.ChunkType.field_12808;
   }

   private static void writeEntities(CompoundTag tag, WorldChunk chunk) {
      ListTag listTag = tag.getList("Entities", 10);
      World world = chunk.getWorld();

      for(int i = 0; i < listTag.size(); ++i) {
         CompoundTag compoundTag = listTag.getCompound(i);
         EntityType.loadEntityWithPassengers(compoundTag, world, (entity) -> {
            chunk.addEntity(entity);
            return entity;
         });
         chunk.setUnsaved(true);
      }

      ListTag listTag2 = tag.getList("TileEntities", 10);

      for(int j = 0; j < listTag2.size(); ++j) {
         CompoundTag compoundTag2 = listTag2.getCompound(j);
         boolean bl = compoundTag2.getBoolean("keepPacked");
         if (bl) {
            chunk.addPendingBlockEntityTag(compoundTag2);
         } else {
            BlockPos blockPos = new BlockPos(compoundTag2.getInt("x"), compoundTag2.getInt("y"), compoundTag2.getInt("z"));
            BlockEntity blockEntity = BlockEntity.createFromTag(chunk.getBlockState(blockPos), compoundTag2);
            if (blockEntity != null) {
               chunk.addBlockEntity(blockEntity);
            }
         }
      }

   }

   private static CompoundTag writeStructures(ChunkPos pos, Map<StructureFeature<?>, StructureStart<?>> structureStarts, Map<StructureFeature<?>, LongSet> structureReferences) {
      CompoundTag compoundTag = new CompoundTag();
      CompoundTag compoundTag2 = new CompoundTag();
      Iterator var5 = structureStarts.entrySet().iterator();

      while(var5.hasNext()) {
         Entry<StructureFeature<?>, StructureStart<?>> entry = (Entry)var5.next();
         compoundTag2.put(((StructureFeature)entry.getKey()).getName(), ((StructureStart)entry.getValue()).toTag(pos.x, pos.z));
      }

      compoundTag.put("Starts", compoundTag2);
      CompoundTag compoundTag3 = new CompoundTag();
      Iterator var9 = structureReferences.entrySet().iterator();

      while(var9.hasNext()) {
         Entry<StructureFeature<?>, LongSet> entry2 = (Entry)var9.next();
         compoundTag3.put(((StructureFeature)entry2.getKey()).getName(), new LongArrayTag((LongSet)entry2.getValue()));
      }

      compoundTag.put("References", compoundTag3);
      return compoundTag;
   }

   private static Map<StructureFeature<?>, StructureStart<?>> readStructureStarts(StructureManager structureManager, CompoundTag tag, long worldSeed) {
      Map<StructureFeature<?>, StructureStart<?>> map = Maps.newHashMap();
      CompoundTag compoundTag = tag.getCompound("Starts");
      Iterator var6 = compoundTag.getKeys().iterator();

      while(var6.hasNext()) {
         String string = (String)var6.next();
         String string2 = string.toLowerCase(Locale.ROOT);
         StructureFeature<?> structureFeature = (StructureFeature)StructureFeature.STRUCTURES.get(string2);
         if (structureFeature == null) {
            LOGGER.error((String)"Unknown structure start: {}", (Object)string2);
         } else {
            StructureStart<?> structureStart = StructureFeature.readStructureStart(structureManager, compoundTag.getCompound(string), worldSeed);
            if (structureStart != null) {
               map.put(structureFeature, structureStart);
            }
         }
      }

      return map;
   }

   private static Map<StructureFeature<?>, LongSet> readStructureReferences(ChunkPos pos, CompoundTag tag) {
      Map<StructureFeature<?>, LongSet> map = Maps.newHashMap();
      CompoundTag compoundTag = tag.getCompound("References");
      Iterator var4 = compoundTag.getKeys().iterator();

      while(var4.hasNext()) {
         String string = (String)var4.next();
         map.put(StructureFeature.STRUCTURES.get(string.toLowerCase(Locale.ROOT)), new LongOpenHashSet(Arrays.stream(compoundTag.getLongArray(string)).filter((l) -> {
            ChunkPos chunkPos2 = new ChunkPos(l);
            if (chunkPos2.method_24022(pos) > 8) {
               LOGGER.warn((String)"Found invalid structure reference [ {} @ {} ] for chunk {}.", (Object)string, chunkPos2, pos);
               return false;
            } else {
               return true;
            }
         }).toArray()));
      }

      return map;
   }

   public static ListTag toNbt(ShortList[] lists) {
      ListTag listTag = new ListTag();
      ShortList[] var2 = lists;
      int var3 = lists.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         ShortList shortList = var2[var4];
         ListTag listTag2 = new ListTag();
         if (shortList != null) {
            ShortListIterator var7 = shortList.iterator();

            while(var7.hasNext()) {
               Short short_ = (Short)var7.next();
               listTag2.add(ShortTag.of(short_));
            }
         }

         listTag.add(listTag2);
      }

      return listTag;
   }
}
