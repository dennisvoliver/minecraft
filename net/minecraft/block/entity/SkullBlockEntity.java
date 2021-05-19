package net.minecraft.block.entity;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.ChatUtil;
import net.minecraft.util.Tickable;
import net.minecraft.util.UserCache;
import org.jetbrains.annotations.Nullable;

public class SkullBlockEntity extends BlockEntity implements Tickable {
   @Nullable
   private static UserCache userCache;
   @Nullable
   private static MinecraftSessionService sessionService;
   @Nullable
   private GameProfile owner;
   private int ticksPowered;
   private boolean powered;

   public SkullBlockEntity() {
      super(BlockEntityType.SKULL);
   }

   public static void setUserCache(UserCache value) {
      userCache = value;
   }

   public static void setSessionService(MinecraftSessionService value) {
      sessionService = value;
   }

   public CompoundTag toTag(CompoundTag tag) {
      super.toTag(tag);
      if (this.owner != null) {
         CompoundTag compoundTag = new CompoundTag();
         NbtHelper.fromGameProfile(compoundTag, this.owner);
         tag.put("SkullOwner", compoundTag);
      }

      return tag;
   }

   public void fromTag(BlockState state, CompoundTag tag) {
      super.fromTag(state, tag);
      if (tag.contains("SkullOwner", 10)) {
         this.setOwnerAndType(NbtHelper.toGameProfile(tag.getCompound("SkullOwner")));
      } else if (tag.contains("ExtraType", 8)) {
         String string = tag.getString("ExtraType");
         if (!ChatUtil.isEmpty(string)) {
            this.setOwnerAndType(new GameProfile((UUID)null, string));
         }
      }

   }

   public void tick() {
      BlockState blockState = this.getCachedState();
      if (blockState.isOf(Blocks.DRAGON_HEAD) || blockState.isOf(Blocks.DRAGON_WALL_HEAD)) {
         if (this.world.isReceivingRedstonePower(this.pos)) {
            this.powered = true;
            ++this.ticksPowered;
         } else {
            this.powered = false;
         }
      }

   }

   @Environment(EnvType.CLIENT)
   public float getTicksPowered(float tickDelta) {
      return this.powered ? (float)this.ticksPowered + tickDelta : (float)this.ticksPowered;
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   public GameProfile getOwner() {
      return this.owner;
   }

   @Nullable
   public BlockEntityUpdateS2CPacket toUpdatePacket() {
      return new BlockEntityUpdateS2CPacket(this.pos, 4, this.toInitialChunkDataTag());
   }

   public CompoundTag toInitialChunkDataTag() {
      return this.toTag(new CompoundTag());
   }

   public void setOwnerAndType(@Nullable GameProfile gameProfile) {
      this.owner = gameProfile;
      this.loadOwnerProperties();
   }

   private void loadOwnerProperties() {
      this.owner = loadProperties(this.owner);
      this.markDirty();
   }

   @Nullable
   public static GameProfile loadProperties(@Nullable GameProfile profile) {
      if (profile != null && !ChatUtil.isEmpty(profile.getName())) {
         if (profile.isComplete() && profile.getProperties().containsKey("textures")) {
            return profile;
         } else if (userCache != null && sessionService != null) {
            GameProfile gameProfile = userCache.findByName(profile.getName());
            if (gameProfile == null) {
               return profile;
            } else {
               Property property = (Property)Iterables.getFirst(gameProfile.getProperties().get("textures"), (Object)null);
               if (property == null) {
                  gameProfile = sessionService.fillProfileProperties(gameProfile, true);
               }

               return gameProfile;
            }
         } else {
            return profile;
         }
      } else {
         return profile;
      }
   }
}
