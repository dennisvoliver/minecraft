package net.minecraft.entity.attribute;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class AttributeContainer {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Map<EntityAttribute, EntityAttributeInstance> custom = Maps.newHashMap();
   private final Set<EntityAttributeInstance> tracked = Sets.newHashSet();
   private final DefaultAttributeContainer fallback;

   public AttributeContainer(DefaultAttributeContainer defaultAttributes) {
      this.fallback = defaultAttributes;
   }

   private void updateTrackedStatus(EntityAttributeInstance instance) {
      if (instance.getAttribute().isTracked()) {
         this.tracked.add(instance);
      }

   }

   public Set<EntityAttributeInstance> getTracked() {
      return this.tracked;
   }

   public Collection<EntityAttributeInstance> getAttributesToSend() {
      return (Collection)this.custom.values().stream().filter((attribute) -> {
         return attribute.getAttribute().isTracked();
      }).collect(Collectors.toList());
   }

   @Nullable
   public EntityAttributeInstance getCustomInstance(EntityAttribute attribute) {
      return (EntityAttributeInstance)this.custom.computeIfAbsent(attribute, (attributex) -> {
         return this.fallback.createOverride(this::updateTrackedStatus, attributex);
      });
   }

   public boolean hasAttribute(EntityAttribute attribute) {
      return this.custom.get(attribute) != null || this.fallback.has(attribute);
   }

   public boolean hasModifierForAttribute(EntityAttribute attribute, UUID uuid) {
      EntityAttributeInstance entityAttributeInstance = (EntityAttributeInstance)this.custom.get(attribute);
      return entityAttributeInstance != null ? entityAttributeInstance.getModifier(uuid) != null : this.fallback.hasModifier(attribute, uuid);
   }

   public double getValue(EntityAttribute attribute) {
      EntityAttributeInstance entityAttributeInstance = (EntityAttributeInstance)this.custom.get(attribute);
      return entityAttributeInstance != null ? entityAttributeInstance.getValue() : this.fallback.getValue(attribute);
   }

   public double getBaseValue(EntityAttribute attribute) {
      EntityAttributeInstance entityAttributeInstance = (EntityAttributeInstance)this.custom.get(attribute);
      return entityAttributeInstance != null ? entityAttributeInstance.getBaseValue() : this.fallback.getBaseValue(attribute);
   }

   public double getModifierValue(EntityAttribute attribute, UUID uuid) {
      EntityAttributeInstance entityAttributeInstance = (EntityAttributeInstance)this.custom.get(attribute);
      return entityAttributeInstance != null ? entityAttributeInstance.getModifier(uuid).getValue() : this.fallback.getModifierValue(attribute, uuid);
   }

   public void removeModifiers(Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers) {
      attributeModifiers.asMap().forEach((entityAttribute, collection) -> {
         EntityAttributeInstance entityAttributeInstance = (EntityAttributeInstance)this.custom.get(entityAttribute);
         if (entityAttributeInstance != null) {
            collection.forEach(entityAttributeInstance::removeModifier);
         }

      });
   }

   public void addTemporaryModifiers(Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers) {
      attributeModifiers.forEach((entityAttribute, entityAttributeModifier) -> {
         EntityAttributeInstance entityAttributeInstance = this.getCustomInstance(entityAttribute);
         if (entityAttributeInstance != null) {
            entityAttributeInstance.removeModifier(entityAttributeModifier);
            entityAttributeInstance.addTemporaryModifier(entityAttributeModifier);
         }

      });
   }

   @Environment(EnvType.CLIENT)
   public void setFrom(AttributeContainer other) {
      other.custom.values().forEach((entityAttributeInstance) -> {
         EntityAttributeInstance entityAttributeInstance2 = this.getCustomInstance(entityAttributeInstance.getAttribute());
         if (entityAttributeInstance2 != null) {
            entityAttributeInstance2.setFrom(entityAttributeInstance);
         }

      });
   }

   public ListTag toTag() {
      ListTag listTag = new ListTag();
      Iterator var2 = this.custom.values().iterator();

      while(var2.hasNext()) {
         EntityAttributeInstance entityAttributeInstance = (EntityAttributeInstance)var2.next();
         listTag.add(entityAttributeInstance.toTag());
      }

      return listTag;
   }

   public void fromTag(ListTag tag) {
      for(int i = 0; i < tag.size(); ++i) {
         CompoundTag compoundTag = tag.getCompound(i);
         String string = compoundTag.getString("Name");
         Util.ifPresentOrElse(Registry.ATTRIBUTE.getOrEmpty(Identifier.tryParse(string)), (entityAttribute) -> {
            EntityAttributeInstance entityAttributeInstance = this.getCustomInstance(entityAttribute);
            if (entityAttributeInstance != null) {
               entityAttributeInstance.fromTag(compoundTag);
            }

         }, () -> {
            LOGGER.warn((String)"Ignoring unknown attribute '{}'", (Object)string);
         });
      }

   }
}
