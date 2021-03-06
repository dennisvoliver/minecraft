package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.util.ChatUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.DynamicSerializableUuid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public final class NbtHelper {
   private static final Logger LOGGER = LogManager.getLogger();

   @Nullable
   public static GameProfile toGameProfile(CompoundTag tag) {
      String string = null;
      UUID uUID = null;
      if (tag.contains("Name", 8)) {
         string = tag.getString("Name");
      }

      if (tag.containsUuid("Id")) {
         uUID = tag.getUuid("Id");
      }

      try {
         GameProfile gameProfile = new GameProfile(uUID, string);
         if (tag.contains("Properties", 10)) {
            CompoundTag compoundTag = tag.getCompound("Properties");
            Iterator var5 = compoundTag.getKeys().iterator();

            while(var5.hasNext()) {
               String string2 = (String)var5.next();
               ListTag listTag = compoundTag.getList(string2, 10);

               for(int i = 0; i < listTag.size(); ++i) {
                  CompoundTag compoundTag2 = listTag.getCompound(i);
                  String string3 = compoundTag2.getString("Value");
                  if (compoundTag2.contains("Signature", 8)) {
                     gameProfile.getProperties().put(string2, new Property(string2, string3, compoundTag2.getString("Signature")));
                  } else {
                     gameProfile.getProperties().put(string2, new Property(string2, string3));
                  }
               }
            }
         }

         return gameProfile;
      } catch (Throwable var11) {
         return null;
      }
   }

   public static CompoundTag fromGameProfile(CompoundTag tag, GameProfile profile) {
      if (!ChatUtil.isEmpty(profile.getName())) {
         tag.putString("Name", profile.getName());
      }

      if (profile.getId() != null) {
         tag.putUuid("Id", profile.getId());
      }

      if (!profile.getProperties().isEmpty()) {
         CompoundTag compoundTag = new CompoundTag();
         Iterator var3 = profile.getProperties().keySet().iterator();

         while(var3.hasNext()) {
            String string = (String)var3.next();
            ListTag listTag = new ListTag();

            CompoundTag compoundTag2;
            for(Iterator var6 = profile.getProperties().get(string).iterator(); var6.hasNext(); listTag.add(compoundTag2)) {
               Property property = (Property)var6.next();
               compoundTag2 = new CompoundTag();
               compoundTag2.putString("Value", property.getValue());
               if (property.hasSignature()) {
                  compoundTag2.putString("Signature", property.getSignature());
               }
            }

            compoundTag.put(string, listTag);
         }

         tag.put("Properties", compoundTag);
      }

      return tag;
   }

   @VisibleForTesting
   public static boolean matches(@Nullable Tag standard, @Nullable Tag subject, boolean equalValue) {
      if (standard == subject) {
         return true;
      } else if (standard == null) {
         return true;
      } else if (subject == null) {
         return false;
      } else if (!standard.getClass().equals(subject.getClass())) {
         return false;
      } else if (standard instanceof CompoundTag) {
         CompoundTag compoundTag = (CompoundTag)standard;
         CompoundTag compoundTag2 = (CompoundTag)subject;
         Iterator var11 = compoundTag.getKeys().iterator();

         String string;
         Tag tag;
         do {
            if (!var11.hasNext()) {
               return true;
            }

            string = (String)var11.next();
            tag = compoundTag.get(string);
         } while(matches(tag, compoundTag2.get(string), equalValue));

         return false;
      } else if (standard instanceof ListTag && equalValue) {
         ListTag listTag = (ListTag)standard;
         ListTag listTag2 = (ListTag)subject;
         if (listTag.isEmpty()) {
            return listTag2.isEmpty();
         } else {
            for(int i = 0; i < listTag.size(); ++i) {
               Tag tag2 = listTag.get(i);
               boolean bl = false;

               for(int j = 0; j < listTag2.size(); ++j) {
                  if (matches(tag2, listTag2.get(j), equalValue)) {
                     bl = true;
                     break;
                  }
               }

               if (!bl) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return standard.equals(subject);
      }
   }

   /**
    * Serializes a {@link UUID} into its equivalent NBT representation.
    * 
    * @since 20w10a
    */
   public static IntArrayTag fromUuid(UUID uuid) {
      return new IntArrayTag(DynamicSerializableUuid.toIntArray(uuid));
   }

   /**
    * Deserializes a tag into a {@link UUID}.
    * The tag's data must have the same structure as the output of {@link #fromUuid}.
    * 
    * @throws IllegalArgumentException if {@code tag} is not a valid representation of a UUID
    * @since 20w10a
    */
   public static UUID toUuid(Tag tag) {
      if (tag.getReader() != IntArrayTag.READER) {
         throw new IllegalArgumentException("Expected UUID-Tag to be of type " + IntArrayTag.READER.getCrashReportName() + ", but found " + tag.getReader().getCrashReportName() + ".");
      } else {
         int[] is = ((IntArrayTag)tag).getIntArray();
         if (is.length != 4) {
            throw new IllegalArgumentException("Expected UUID-Array to be of length 4, but found " + is.length + ".");
         } else {
            return DynamicSerializableUuid.toUuid(is);
         }
      }
   }

   public static BlockPos toBlockPos(CompoundTag tag) {
      return new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
   }

   public static CompoundTag fromBlockPos(BlockPos pos) {
      CompoundTag compoundTag = new CompoundTag();
      compoundTag.putInt("X", pos.getX());
      compoundTag.putInt("Y", pos.getY());
      compoundTag.putInt("Z", pos.getZ());
      return compoundTag;
   }

   public static BlockState toBlockState(CompoundTag tag) {
      if (!tag.contains("Name", 8)) {
         return Blocks.AIR.getDefaultState();
      } else {
         Block block = (Block)Registry.BLOCK.get(new Identifier(tag.getString("Name")));
         BlockState blockState = block.getDefaultState();
         if (tag.contains("Properties", 10)) {
            CompoundTag compoundTag = tag.getCompound("Properties");
            StateManager<Block, BlockState> stateManager = block.getStateManager();
            Iterator var5 = compoundTag.getKeys().iterator();

            while(var5.hasNext()) {
               String string = (String)var5.next();
               net.minecraft.state.property.Property<?> property = stateManager.getProperty(string);
               if (property != null) {
                  blockState = (BlockState)withProperty(blockState, property, string, compoundTag, tag);
               }
            }
         }

         return blockState;
      }
   }

   private static <S extends State<?, S>, T extends Comparable<T>> S withProperty(S state, net.minecraft.state.property.Property<T> property, String key, CompoundTag propertiesTag, CompoundTag mainTag) {
      Optional<T> optional = property.parse(propertiesTag.getString(key));
      if (optional.isPresent()) {
         return (State)state.with(property, (Comparable)optional.get());
      } else {
         LOGGER.warn((String)"Unable to read property: {} with value: {} for blockstate: {}", (Object)key, propertiesTag.getString(key), mainTag.toString());
         return state;
      }
   }

   public static CompoundTag fromBlockState(BlockState state) {
      CompoundTag compoundTag = new CompoundTag();
      compoundTag.putString("Name", Registry.BLOCK.getId(state.getBlock()).toString());
      ImmutableMap<net.minecraft.state.property.Property<?>, Comparable<?>> immutableMap = state.getEntries();
      if (!immutableMap.isEmpty()) {
         CompoundTag compoundTag2 = new CompoundTag();
         UnmodifiableIterator var4 = immutableMap.entrySet().iterator();

         while(var4.hasNext()) {
            Entry<net.minecraft.state.property.Property<?>, Comparable<?>> entry = (Entry)var4.next();
            net.minecraft.state.property.Property<?> property = (net.minecraft.state.property.Property)entry.getKey();
            compoundTag2.putString(property.getName(), nameValue(property, (Comparable)entry.getValue()));
         }

         compoundTag.put("Properties", compoundTag2);
      }

      return compoundTag;
   }

   private static <T extends Comparable<T>> String nameValue(net.minecraft.state.property.Property<T> property, Comparable<?> value) {
      return property.name(value);
   }

   /**
    * Uses the data fixer to update a tag to the latest data version.
    * 
    * @param fixer the data fixer
    * @param fixTypes the fix types
    * @param tag the tag to fix
    * @param oldVersion the data version of the compound tag
    */
   public static CompoundTag update(DataFixer fixer, DataFixTypes fixTypes, CompoundTag tag, int oldVersion) {
      return update(fixer, fixTypes, tag, oldVersion, SharedConstants.getGameVersion().getWorldVersion());
   }

   /**
    * Uses the data fixer to update a tag.
    * 
    * @param fixer the data fixer
    * @param fixTypes the fix types
    * @param tag the tag to fix
    * @param oldVersion the data version of the compound tag
    * @param targetVersion the data version to update the tag to
    */
   public static CompoundTag update(DataFixer fixer, DataFixTypes fixTypes, CompoundTag tag, int oldVersion, int targetVersion) {
      return (CompoundTag)fixer.update(fixTypes.getTypeReference(), new Dynamic(NbtOps.INSTANCE, tag), oldVersion, targetVersion).getValue();
   }
}
