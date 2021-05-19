package net.minecraft.data.server;

import java.nio.file.Path;
import net.minecraft.data.DataGenerator;
import net.minecraft.entity.EntityType;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class EntityTypeTagsProvider extends AbstractTagProvider<EntityType<?>> {
   public EntityTypeTagsProvider(DataGenerator dataGenerator) {
      super(dataGenerator, Registry.ENTITY_TYPE);
   }

   protected void configure() {
      this.getOrCreateTagBuilder(EntityTypeTags.SKELETONS).add((Object[])(EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON));
      this.getOrCreateTagBuilder(EntityTypeTags.RAIDERS).add((Object[])(EntityType.EVOKER, EntityType.PILLAGER, EntityType.RAVAGER, EntityType.VINDICATOR, EntityType.ILLUSIONER, EntityType.WITCH));
      this.getOrCreateTagBuilder(EntityTypeTags.BEEHIVE_INHABITORS).add((Object)EntityType.BEE);
      this.getOrCreateTagBuilder(EntityTypeTags.ARROWS).add((Object[])(EntityType.ARROW, EntityType.SPECTRAL_ARROW));
      this.getOrCreateTagBuilder(EntityTypeTags.IMPACT_PROJECTILES).addTag(EntityTypeTags.ARROWS).add((Object[])(EntityType.SNOWBALL, EntityType.FIREBALL, EntityType.SMALL_FIREBALL, EntityType.EGG, EntityType.TRIDENT, EntityType.DRAGON_FIREBALL, EntityType.WITHER_SKULL));
   }

   protected Path getOutput(Identifier identifier) {
      return this.root.getOutput().resolve("data/" + identifier.getNamespace() + "/tags/entity_types/" + identifier.getPath() + ".json");
   }

   public String getName() {
      return "Entity Type Tags";
   }
}
