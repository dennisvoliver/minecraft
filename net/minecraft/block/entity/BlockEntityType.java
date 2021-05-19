package net.minecraft.block.entity;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.types.Type;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class BlockEntityType<T extends BlockEntity> {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final BlockEntityType<FurnaceBlockEntity> FURNACE;
   public static final BlockEntityType<ChestBlockEntity> CHEST;
   public static final BlockEntityType<TrappedChestBlockEntity> TRAPPED_CHEST;
   public static final BlockEntityType<EnderChestBlockEntity> ENDER_CHEST;
   public static final BlockEntityType<JukeboxBlockEntity> JUKEBOX;
   public static final BlockEntityType<DispenserBlockEntity> DISPENSER;
   public static final BlockEntityType<DropperBlockEntity> DROPPER;
   public static final BlockEntityType<SignBlockEntity> SIGN;
   public static final BlockEntityType<MobSpawnerBlockEntity> MOB_SPAWNER;
   public static final BlockEntityType<PistonBlockEntity> PISTON;
   public static final BlockEntityType<BrewingStandBlockEntity> BREWING_STAND;
   public static final BlockEntityType<EnchantingTableBlockEntity> ENCHANTING_TABLE;
   public static final BlockEntityType<EndPortalBlockEntity> END_PORTAL;
   public static final BlockEntityType<BeaconBlockEntity> BEACON;
   public static final BlockEntityType<SkullBlockEntity> SKULL;
   public static final BlockEntityType<DaylightDetectorBlockEntity> DAYLIGHT_DETECTOR;
   public static final BlockEntityType<HopperBlockEntity> HOPPER;
   public static final BlockEntityType<ComparatorBlockEntity> COMPARATOR;
   public static final BlockEntityType<BannerBlockEntity> BANNER;
   public static final BlockEntityType<StructureBlockBlockEntity> STRUCTURE_BLOCK;
   public static final BlockEntityType<EndGatewayBlockEntity> END_GATEWAY;
   public static final BlockEntityType<CommandBlockBlockEntity> COMMAND_BLOCK;
   public static final BlockEntityType<ShulkerBoxBlockEntity> SHULKER_BOX;
   public static final BlockEntityType<BedBlockEntity> BED;
   public static final BlockEntityType<ConduitBlockEntity> CONDUIT;
   public static final BlockEntityType<BarrelBlockEntity> BARREL;
   public static final BlockEntityType<SmokerBlockEntity> SMOKER;
   public static final BlockEntityType<BlastFurnaceBlockEntity> BLAST_FURNACE;
   public static final BlockEntityType<LecternBlockEntity> LECTERN;
   public static final BlockEntityType<BellBlockEntity> BELL;
   public static final BlockEntityType<JigsawBlockEntity> JIGSAW;
   public static final BlockEntityType<CampfireBlockEntity> CAMPFIRE;
   public static final BlockEntityType<BeehiveBlockEntity> BEEHIVE;
   private final Supplier<? extends T> supplier;
   private final Set<Block> blocks;
   private final Type<?> type;

   @Nullable
   public static Identifier getId(BlockEntityType<?> blockEntityType) {
      return Registry.BLOCK_ENTITY_TYPE.getId(blockEntityType);
   }

   private static <T extends BlockEntity> BlockEntityType<T> create(String string, BlockEntityType.Builder<T> builder) {
      if (builder.blocks.isEmpty()) {
         LOGGER.warn((String)"Block entity type {} requires at least one valid block to be defined!", (Object)string);
      }

      Type<?> type = Util.getChoiceType(TypeReferences.BLOCK_ENTITY, string);
      return (BlockEntityType)Registry.register(Registry.BLOCK_ENTITY_TYPE, (String)string, builder.build(type));
   }

   public BlockEntityType(Supplier<? extends T> supplier, Set<Block> blocks, Type<?> type) {
      this.supplier = supplier;
      this.blocks = blocks;
      this.type = type;
   }

   @Nullable
   public T instantiate() {
      return (BlockEntity)this.supplier.get();
   }

   public boolean supports(Block block) {
      return this.blocks.contains(block);
   }

   @Nullable
   public T get(BlockView world, BlockPos pos) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      return blockEntity != null && blockEntity.getType() == this ? blockEntity : null;
   }

   static {
      FURNACE = create("furnace", BlockEntityType.Builder.create(FurnaceBlockEntity::new, Blocks.FURNACE));
      CHEST = create("chest", BlockEntityType.Builder.create(ChestBlockEntity::new, Blocks.CHEST));
      TRAPPED_CHEST = create("trapped_chest", BlockEntityType.Builder.create(TrappedChestBlockEntity::new, Blocks.TRAPPED_CHEST));
      ENDER_CHEST = create("ender_chest", BlockEntityType.Builder.create(EnderChestBlockEntity::new, Blocks.ENDER_CHEST));
      JUKEBOX = create("jukebox", BlockEntityType.Builder.create(JukeboxBlockEntity::new, Blocks.JUKEBOX));
      DISPENSER = create("dispenser", BlockEntityType.Builder.create(DispenserBlockEntity::new, Blocks.DISPENSER));
      DROPPER = create("dropper", BlockEntityType.Builder.create(DropperBlockEntity::new, Blocks.DROPPER));
      SIGN = create("sign", BlockEntityType.Builder.create(SignBlockEntity::new, Blocks.OAK_SIGN, Blocks.SPRUCE_SIGN, Blocks.BIRCH_SIGN, Blocks.ACACIA_SIGN, Blocks.JUNGLE_SIGN, Blocks.DARK_OAK_SIGN, Blocks.OAK_WALL_SIGN, Blocks.SPRUCE_WALL_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.DARK_OAK_WALL_SIGN, Blocks.CRIMSON_SIGN, Blocks.CRIMSON_WALL_SIGN, Blocks.WARPED_SIGN, Blocks.WARPED_WALL_SIGN));
      MOB_SPAWNER = create("mob_spawner", BlockEntityType.Builder.create(MobSpawnerBlockEntity::new, Blocks.SPAWNER));
      PISTON = create("piston", BlockEntityType.Builder.create(PistonBlockEntity::new, Blocks.MOVING_PISTON));
      BREWING_STAND = create("brewing_stand", BlockEntityType.Builder.create(BrewingStandBlockEntity::new, Blocks.BREWING_STAND));
      ENCHANTING_TABLE = create("enchanting_table", BlockEntityType.Builder.create(EnchantingTableBlockEntity::new, Blocks.ENCHANTING_TABLE));
      END_PORTAL = create("end_portal", BlockEntityType.Builder.create(EndPortalBlockEntity::new, Blocks.END_PORTAL));
      BEACON = create("beacon", BlockEntityType.Builder.create(BeaconBlockEntity::new, Blocks.BEACON));
      SKULL = create("skull", BlockEntityType.Builder.create(SkullBlockEntity::new, Blocks.SKELETON_SKULL, Blocks.SKELETON_WALL_SKULL, Blocks.CREEPER_HEAD, Blocks.CREEPER_WALL_HEAD, Blocks.DRAGON_HEAD, Blocks.DRAGON_WALL_HEAD, Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD, Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, Blocks.PLAYER_HEAD, Blocks.PLAYER_WALL_HEAD));
      DAYLIGHT_DETECTOR = create("daylight_detector", BlockEntityType.Builder.create(DaylightDetectorBlockEntity::new, Blocks.DAYLIGHT_DETECTOR));
      HOPPER = create("hopper", BlockEntityType.Builder.create(HopperBlockEntity::new, Blocks.HOPPER));
      COMPARATOR = create("comparator", BlockEntityType.Builder.create(ComparatorBlockEntity::new, Blocks.COMPARATOR));
      BANNER = create("banner", BlockEntityType.Builder.create(BannerBlockEntity::new, Blocks.WHITE_BANNER, Blocks.ORANGE_BANNER, Blocks.MAGENTA_BANNER, Blocks.LIGHT_BLUE_BANNER, Blocks.YELLOW_BANNER, Blocks.LIME_BANNER, Blocks.PINK_BANNER, Blocks.GRAY_BANNER, Blocks.LIGHT_GRAY_BANNER, Blocks.CYAN_BANNER, Blocks.PURPLE_BANNER, Blocks.BLUE_BANNER, Blocks.BROWN_BANNER, Blocks.GREEN_BANNER, Blocks.RED_BANNER, Blocks.BLACK_BANNER, Blocks.WHITE_WALL_BANNER, Blocks.ORANGE_WALL_BANNER, Blocks.MAGENTA_WALL_BANNER, Blocks.LIGHT_BLUE_WALL_BANNER, Blocks.YELLOW_WALL_BANNER, Blocks.LIME_WALL_BANNER, Blocks.PINK_WALL_BANNER, Blocks.GRAY_WALL_BANNER, Blocks.LIGHT_GRAY_WALL_BANNER, Blocks.CYAN_WALL_BANNER, Blocks.PURPLE_WALL_BANNER, Blocks.BLUE_WALL_BANNER, Blocks.BROWN_WALL_BANNER, Blocks.GREEN_WALL_BANNER, Blocks.RED_WALL_BANNER, Blocks.BLACK_WALL_BANNER));
      STRUCTURE_BLOCK = create("structure_block", BlockEntityType.Builder.create(StructureBlockBlockEntity::new, Blocks.STRUCTURE_BLOCK));
      END_GATEWAY = create("end_gateway", BlockEntityType.Builder.create(EndGatewayBlockEntity::new, Blocks.END_GATEWAY));
      COMMAND_BLOCK = create("command_block", BlockEntityType.Builder.create(CommandBlockBlockEntity::new, Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK));
      SHULKER_BOX = create("shulker_box", BlockEntityType.Builder.create(ShulkerBoxBlockEntity::new, Blocks.SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX));
      BED = create("bed", BlockEntityType.Builder.create(BedBlockEntity::new, Blocks.RED_BED, Blocks.BLACK_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.CYAN_BED, Blocks.GRAY_BED, Blocks.GREEN_BED, Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_GRAY_BED, Blocks.LIME_BED, Blocks.MAGENTA_BED, Blocks.ORANGE_BED, Blocks.PINK_BED, Blocks.PURPLE_BED, Blocks.WHITE_BED, Blocks.YELLOW_BED));
      CONDUIT = create("conduit", BlockEntityType.Builder.create(ConduitBlockEntity::new, Blocks.CONDUIT));
      BARREL = create("barrel", BlockEntityType.Builder.create(BarrelBlockEntity::new, Blocks.BARREL));
      SMOKER = create("smoker", BlockEntityType.Builder.create(SmokerBlockEntity::new, Blocks.SMOKER));
      BLAST_FURNACE = create("blast_furnace", BlockEntityType.Builder.create(BlastFurnaceBlockEntity::new, Blocks.BLAST_FURNACE));
      LECTERN = create("lectern", BlockEntityType.Builder.create(LecternBlockEntity::new, Blocks.LECTERN));
      BELL = create("bell", BlockEntityType.Builder.create(BellBlockEntity::new, Blocks.BELL));
      JIGSAW = create("jigsaw", BlockEntityType.Builder.create(JigsawBlockEntity::new, Blocks.JIGSAW));
      CAMPFIRE = create("campfire", BlockEntityType.Builder.create(CampfireBlockEntity::new, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE));
      BEEHIVE = create("beehive", BlockEntityType.Builder.create(BeehiveBlockEntity::new, Blocks.BEE_NEST, Blocks.BEEHIVE));
   }

   public static final class Builder<T extends BlockEntity> {
      private final Supplier<? extends T> supplier;
      private final Set<Block> blocks;

      private Builder(Supplier<? extends T> supplier, Set<Block> blocks) {
         this.supplier = supplier;
         this.blocks = blocks;
      }

      public static <T extends BlockEntity> BlockEntityType.Builder<T> create(Supplier<? extends T> supplier, Block... blocks) {
         return new BlockEntityType.Builder(supplier, ImmutableSet.copyOf((Object[])blocks));
      }

      public BlockEntityType<T> build(Type<?> type) {
         return new BlockEntityType(this.supplier, this.blocks, type);
      }
   }
}
