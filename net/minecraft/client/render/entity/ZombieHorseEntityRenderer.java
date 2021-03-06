package net.minecraft.client.render.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.HorseEntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ZombieHorseEntityRenderer extends HorseBaseEntityRenderer<HorseBaseEntity, HorseEntityModel<HorseBaseEntity>> {
   private static final Map<EntityType<?>, Identifier> TEXTURES;

   public ZombieHorseEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
      super(entityRenderDispatcher, new HorseEntityModel(0.0F), 1.0F);
   }

   public Identifier getTexture(HorseBaseEntity horseBaseEntity) {
      return (Identifier)TEXTURES.get(horseBaseEntity.getType());
   }

   static {
      TEXTURES = Maps.newHashMap(ImmutableMap.of(EntityType.ZOMBIE_HORSE, new Identifier("textures/entity/horse/horse_zombie.png"), EntityType.SKELETON_HORSE, new Identifier("textures/entity/horse/horse_skeleton.png")));
   }
}
