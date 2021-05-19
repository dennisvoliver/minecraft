package net.minecraft.entity.projectile;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class ProjectileUtil {
   public static HitResult getCollision(Entity entity, Predicate<Entity> predicate) {
      Vec3d vec3d = entity.getVelocity();
      World world = entity.world;
      Vec3d vec3d2 = entity.getPos();
      Vec3d vec3d3 = vec3d2.add(vec3d);
      HitResult hitResult = world.raycast(new RaycastContext(vec3d2, vec3d3, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity));
      if (((HitResult)hitResult).getType() != HitResult.Type.MISS) {
         vec3d3 = ((HitResult)hitResult).getPos();
      }

      HitResult hitResult2 = getEntityCollision(world, entity, vec3d2, vec3d3, entity.getBoundingBox().stretch(entity.getVelocity()).expand(1.0D), predicate);
      if (hitResult2 != null) {
         hitResult = hitResult2;
      }

      return (HitResult)hitResult;
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   public static EntityHitResult raycast(Entity entity, Vec3d vec3d, Vec3d vec3d2, Box box, Predicate<Entity> predicate, double d) {
      World world = entity.world;
      double e = d;
      Entity entity2 = null;
      Vec3d vec3d3 = null;
      Iterator var12 = world.getOtherEntities(entity, box, predicate).iterator();

      while(true) {
         while(var12.hasNext()) {
            Entity entity3 = (Entity)var12.next();
            Box box2 = entity3.getBoundingBox().expand((double)entity3.getTargetingMargin());
            Optional<Vec3d> optional = box2.raycast(vec3d, vec3d2);
            if (box2.contains(vec3d)) {
               if (e >= 0.0D) {
                  entity2 = entity3;
                  vec3d3 = (Vec3d)optional.orElse(vec3d);
                  e = 0.0D;
               }
            } else if (optional.isPresent()) {
               Vec3d vec3d4 = (Vec3d)optional.get();
               double f = vec3d.squaredDistanceTo(vec3d4);
               if (f < e || e == 0.0D) {
                  if (entity3.getRootVehicle() == entity.getRootVehicle()) {
                     if (e == 0.0D) {
                        entity2 = entity3;
                        vec3d3 = vec3d4;
                     }
                  } else {
                     entity2 = entity3;
                     vec3d3 = vec3d4;
                     e = f;
                  }
               }
            }
         }

         if (entity2 == null) {
            return null;
         }

         return new EntityHitResult(entity2, vec3d3);
      }
   }

   @Nullable
   public static EntityHitResult getEntityCollision(World world, Entity entity, Vec3d vec3d, Vec3d vec3d2, Box box, Predicate<Entity> predicate) {
      double d = Double.MAX_VALUE;
      Entity entity2 = null;
      Iterator var9 = world.getOtherEntities(entity, box, predicate).iterator();

      while(var9.hasNext()) {
         Entity entity3 = (Entity)var9.next();
         Box box2 = entity3.getBoundingBox().expand(0.30000001192092896D);
         Optional<Vec3d> optional = box2.raycast(vec3d, vec3d2);
         if (optional.isPresent()) {
            double e = vec3d.squaredDistanceTo((Vec3d)optional.get());
            if (e < d) {
               entity2 = entity3;
               d = e;
            }
         }
      }

      if (entity2 == null) {
         return null;
      } else {
         return new EntityHitResult(entity2);
      }
   }

   public static final void method_7484(Entity entity, float f) {
      Vec3d vec3d = entity.getVelocity();
      if (vec3d.lengthSquared() != 0.0D) {
         float g = MathHelper.sqrt(Entity.squaredHorizontalLength(vec3d));
         entity.yaw = (float)(MathHelper.atan2(vec3d.z, vec3d.x) * 57.2957763671875D) + 90.0F;

         for(entity.pitch = (float)(MathHelper.atan2((double)g, vec3d.y) * 57.2957763671875D) - 90.0F; entity.pitch - entity.prevPitch < -180.0F; entity.prevPitch -= 360.0F) {
         }

         while(entity.pitch - entity.prevPitch >= 180.0F) {
            entity.prevPitch += 360.0F;
         }

         while(entity.yaw - entity.prevYaw < -180.0F) {
            entity.prevYaw -= 360.0F;
         }

         while(entity.yaw - entity.prevYaw >= 180.0F) {
            entity.prevYaw += 360.0F;
         }

         entity.pitch = MathHelper.lerp(f, entity.prevPitch, entity.pitch);
         entity.yaw = MathHelper.lerp(f, entity.prevYaw, entity.yaw);
      }
   }

   public static Hand getHandPossiblyHolding(LivingEntity entity, Item item) {
      return entity.getMainHandStack().getItem() == item ? Hand.MAIN_HAND : Hand.OFF_HAND;
   }

   public static PersistentProjectileEntity createArrowProjectile(LivingEntity entity, ItemStack stack, float damageModifier) {
      ArrowItem arrowItem = (ArrowItem)((ArrowItem)(stack.getItem() instanceof ArrowItem ? stack.getItem() : Items.ARROW));
      PersistentProjectileEntity persistentProjectileEntity = arrowItem.createArrow(entity.world, stack, entity);
      persistentProjectileEntity.applyEnchantmentEffects(entity, damageModifier);
      if (stack.getItem() == Items.TIPPED_ARROW && persistentProjectileEntity instanceof ArrowEntity) {
         ((ArrowEntity)persistentProjectileEntity).initFromStack(stack);
      }

      return persistentProjectileEntity;
   }
}
