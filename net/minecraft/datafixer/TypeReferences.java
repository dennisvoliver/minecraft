package net.minecraft.datafixer;

import com.mojang.datafixers.DSL.TypeReference;

/**
 * Represents all the type references Minecraft's datafixer can fix.
 */
public class TypeReferences {
   public static final TypeReference LEVEL = () -> {
      return "level";
   };
   /**
    * A type reference which refers to a player.
    */
   public static final TypeReference PLAYER = () -> {
      return "player";
   };
   /**
    * A type reference which refers to a chunk.
    */
   public static final TypeReference CHUNK = () -> {
      return "chunk";
   };
   /**
    * A type reference which refers to the saved creative hotbars.
    * 
    * <p>This type reference is only used on the client.
    */
   public static final TypeReference HOTBAR = () -> {
      return "hotbar";
   };
   /**
    * A type reference which refers to client game options.
    */
   public static final TypeReference OPTIONS = () -> {
      return "options";
   };
   public static final TypeReference STRUCTURE = () -> {
      return "structure";
   };
   public static final TypeReference STATS = () -> {
      return "stats";
   };
   public static final TypeReference SAVED_DATA = () -> {
      return "saved_data";
   };
   public static final TypeReference ADVANCEMENTS = () -> {
      return "advancements";
   };
   /**
    * A type reference which refers to the point of interest data in a chunk.
    */
   public static final TypeReference POI_CHUNK = () -> {
      return "poi_chunk";
   };
   /**
    * A type reference which refers to a block entity.
    */
   public static final TypeReference BLOCK_ENTITY = () -> {
      return "block_entity";
   };
   /**
    * A type reference which refers to an item stack.
    */
   public static final TypeReference ITEM_STACK = () -> {
      return "item_stack";
   };
   /**
    * A type reference which refers to a block state.
    */
   public static final TypeReference BLOCK_STATE = () -> {
      return "block_state";
   };
   /**
    * A type reference which refers to an entity's identifier.
    */
   public static final TypeReference ENTITY_NAME = () -> {
      return "entity_name";
   };
   /**
    * A type reference which refers to an entity tree.
    * 
    * <p>An entity tree contains the passengers of an entity and their passengers.
    */
   public static final TypeReference ENTITY_TREE = () -> {
      return "entity_tree";
   };
   /**
    * A type reference which refers to a type of entity.
    */
   public static final TypeReference ENTITY = () -> {
      return "entity";
   };
   /**
    * A type reference which refers to a block's identifier.
    */
   public static final TypeReference BLOCK_NAME = () -> {
      return "block_name";
   };
   /**
    * A type reference which refers to an item's identifier.
    */
   public static final TypeReference ITEM_NAME = () -> {
      return "item_name";
   };
   public static final TypeReference UNTAGGED_SPAWNER = () -> {
      return "untagged_spawner";
   };
   public static final TypeReference STRUCTURE_FEATURE = () -> {
      return "structure_feature";
   };
   public static final TypeReference OBJECTIVE = () -> {
      return "objective";
   };
   public static final TypeReference TEAM = () -> {
      return "team";
   };
   public static final TypeReference RECIPE = () -> {
      return "recipe";
   };
   /**
    * A type reference which refers to a biome.
    */
   public static final TypeReference BIOME = () -> {
      return "biome";
   };
   /**
    * A type reference which refers to chunk generator settings.
    */
   public static final TypeReference CHUNK_GENERATOR_SETTINGS = () -> {
      return "world_gen_settings";
   };
}
