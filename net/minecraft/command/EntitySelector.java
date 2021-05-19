package net.minecraft.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.predicate.NumberRange;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Texts;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class EntitySelector {
   private final int limit;
   private final boolean includesNonPlayers;
   private final boolean localWorldOnly;
   private final Predicate<Entity> basePredicate;
   private final NumberRange.FloatRange distance;
   private final Function<Vec3d, Vec3d> positionOffset;
   @Nullable
   private final Box box;
   private final BiConsumer<Vec3d, List<? extends Entity>> sorter;
   private final boolean senderOnly;
   @Nullable
   private final String playerName;
   @Nullable
   private final UUID uuid;
   @Nullable
   private final EntityType<?> type;
   private final boolean usesAt;

   public EntitySelector(int count, boolean includesNonPlayers, boolean localWorldOnly, Predicate<Entity> basePredicate, NumberRange.FloatRange distance, Function<Vec3d, Vec3d> positionOffset, @Nullable Box box, BiConsumer<Vec3d, List<? extends Entity>> sorter, boolean senderOnly, @Nullable String playerName, @Nullable UUID uuid, @Nullable EntityType<?> type, boolean usesAt) {
      this.limit = count;
      this.includesNonPlayers = includesNonPlayers;
      this.localWorldOnly = localWorldOnly;
      this.basePredicate = basePredicate;
      this.distance = distance;
      this.positionOffset = positionOffset;
      this.box = box;
      this.sorter = sorter;
      this.senderOnly = senderOnly;
      this.playerName = playerName;
      this.uuid = uuid;
      this.type = type;
      this.usesAt = usesAt;
   }

   public int getLimit() {
      return this.limit;
   }

   public boolean includesNonPlayers() {
      return this.includesNonPlayers;
   }

   public boolean isSenderOnly() {
      return this.senderOnly;
   }

   public boolean isLocalWorldOnly() {
      return this.localWorldOnly;
   }

   private void checkSourcePermission(ServerCommandSource serverCommandSource) throws CommandSyntaxException {
      if (this.usesAt && !serverCommandSource.hasPermissionLevel(2)) {
         throw EntityArgumentType.NOT_ALLOWED_EXCEPTION.create();
      }
   }

   public Entity getEntity(ServerCommandSource serverCommandSource) throws CommandSyntaxException {
      this.checkSourcePermission(serverCommandSource);
      List<? extends Entity> list = this.getEntities(serverCommandSource);
      if (list.isEmpty()) {
         throw EntityArgumentType.ENTITY_NOT_FOUND_EXCEPTION.create();
      } else if (list.size() > 1) {
         throw EntityArgumentType.TOO_MANY_ENTITIES_EXCEPTION.create();
      } else {
         return (Entity)list.get(0);
      }
   }

   public List<? extends Entity> getEntities(ServerCommandSource serverCommandSource) throws CommandSyntaxException {
      this.checkSourcePermission(serverCommandSource);
      if (!this.includesNonPlayers) {
         return this.getPlayers(serverCommandSource);
      } else if (this.playerName != null) {
         ServerPlayerEntity serverPlayerEntity = serverCommandSource.getMinecraftServer().getPlayerManager().getPlayer(this.playerName);
         return (List)(serverPlayerEntity == null ? Collections.emptyList() : Lists.newArrayList((Object[])(serverPlayerEntity)));
      } else if (this.uuid != null) {
         Iterator var7 = serverCommandSource.getMinecraftServer().getWorlds().iterator();

         Entity entity;
         do {
            if (!var7.hasNext()) {
               return Collections.emptyList();
            }

            ServerWorld serverWorld = (ServerWorld)var7.next();
            entity = serverWorld.getEntity(this.uuid);
         } while(entity == null);

         return Lists.newArrayList((Object[])(entity));
      } else {
         Vec3d vec3d = (Vec3d)this.positionOffset.apply(serverCommandSource.getPosition());
         Predicate<Entity> predicate = this.getPositionPredicate(vec3d);
         if (this.senderOnly) {
            return (List)(serverCommandSource.getEntity() != null && predicate.test(serverCommandSource.getEntity()) ? Lists.newArrayList((Object[])(serverCommandSource.getEntity())) : Collections.emptyList());
         } else {
            List<Entity> list = Lists.newArrayList();
            if (this.isLocalWorldOnly()) {
               this.appendEntitiesFromWorld(list, serverCommandSource.getWorld(), vec3d, predicate);
            } else {
               Iterator var5 = serverCommandSource.getMinecraftServer().getWorlds().iterator();

               while(var5.hasNext()) {
                  ServerWorld serverWorld2 = (ServerWorld)var5.next();
                  this.appendEntitiesFromWorld(list, serverWorld2, vec3d, predicate);
               }
            }

            return this.getEntities(vec3d, list);
         }
      }
   }

   private void appendEntitiesFromWorld(List<Entity> list, ServerWorld serverWorld, Vec3d vec3d, Predicate<Entity> predicate) {
      if (this.box != null) {
         list.addAll(serverWorld.getEntitiesByType(this.type, this.box.offset(vec3d), predicate));
      } else {
         list.addAll(serverWorld.getEntitiesByType(this.type, predicate));
      }

   }

   public ServerPlayerEntity getPlayer(ServerCommandSource serverCommandSource) throws CommandSyntaxException {
      this.checkSourcePermission(serverCommandSource);
      List<ServerPlayerEntity> list = this.getPlayers(serverCommandSource);
      if (list.size() != 1) {
         throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
      } else {
         return (ServerPlayerEntity)list.get(0);
      }
   }

   public List<ServerPlayerEntity> getPlayers(ServerCommandSource serverCommandSource) throws CommandSyntaxException {
      this.checkSourcePermission(serverCommandSource);
      ServerPlayerEntity serverPlayerEntity2;
      if (this.playerName != null) {
         serverPlayerEntity2 = serverCommandSource.getMinecraftServer().getPlayerManager().getPlayer(this.playerName);
         return (List)(serverPlayerEntity2 == null ? Collections.emptyList() : Lists.newArrayList((Object[])(serverPlayerEntity2)));
      } else if (this.uuid != null) {
         serverPlayerEntity2 = serverCommandSource.getMinecraftServer().getPlayerManager().getPlayer(this.uuid);
         return (List)(serverPlayerEntity2 == null ? Collections.emptyList() : Lists.newArrayList((Object[])(serverPlayerEntity2)));
      } else {
         Vec3d vec3d = (Vec3d)this.positionOffset.apply(serverCommandSource.getPosition());
         Predicate<Entity> predicate = this.getPositionPredicate(vec3d);
         if (this.senderOnly) {
            if (serverCommandSource.getEntity() instanceof ServerPlayerEntity) {
               ServerPlayerEntity serverPlayerEntity3 = (ServerPlayerEntity)serverCommandSource.getEntity();
               if (predicate.test(serverPlayerEntity3)) {
                  return Lists.newArrayList((Object[])(serverPlayerEntity3));
               }
            }

            return Collections.emptyList();
         } else {
            Object list2;
            if (this.isLocalWorldOnly()) {
               ServerWorld var10000 = serverCommandSource.getWorld();
               predicate.getClass();
               list2 = var10000.getPlayers(predicate::test);
            } else {
               list2 = Lists.newArrayList();
               Iterator var5 = serverCommandSource.getMinecraftServer().getPlayerManager().getPlayerList().iterator();

               while(var5.hasNext()) {
                  ServerPlayerEntity serverPlayerEntity4 = (ServerPlayerEntity)var5.next();
                  if (predicate.test(serverPlayerEntity4)) {
                     ((List)list2).add(serverPlayerEntity4);
                  }
               }
            }

            return this.getEntities(vec3d, (List)list2);
         }
      }
   }

   private Predicate<Entity> getPositionPredicate(Vec3d vec3d) {
      Predicate<Entity> predicate = this.basePredicate;
      if (this.box != null) {
         Box box = this.box.offset(vec3d);
         predicate = predicate.and((entity) -> {
            return box.intersects(entity.getBoundingBox());
         });
      }

      if (!this.distance.isDummy()) {
         predicate = predicate.and((entity) -> {
            return this.distance.testSqrt(entity.squaredDistanceTo(vec3d));
         });
      }

      return predicate;
   }

   private <T extends Entity> List<T> getEntities(Vec3d vec3d, List<T> list) {
      if (list.size() > 1) {
         this.sorter.accept(vec3d, list);
      }

      return list.subList(0, Math.min(this.limit, list.size()));
   }

   public static MutableText getNames(List<? extends Entity> list) {
      return Texts.join(list, Entity::getDisplayName);
   }
}
