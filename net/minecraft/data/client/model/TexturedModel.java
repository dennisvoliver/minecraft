package net.minecraft.data.client.model;

import com.google.gson.JsonElement;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;

/**
 * Represents a model with texture variables defined.
 */
public class TexturedModel {
   public static final TexturedModel.Factory CUBE_ALL;
   public static final TexturedModel.Factory CUBE_MIRRORED_ALL;
   public static final TexturedModel.Factory CUBE_COLUMN;
   public static final TexturedModel.Factory CUBE_COLUMN_HORIZONTAL;
   public static final TexturedModel.Factory CUBE_BOTTOM_TOP;
   public static final TexturedModel.Factory CUBE_TOP;
   public static final TexturedModel.Factory ORIENTABLE;
   public static final TexturedModel.Factory ORIENTABLE_WITH_BOTTOM;
   public static final TexturedModel.Factory CARPET;
   public static final TexturedModel.Factory TEMPLATE_GLAZED_TERRACOTTA;
   public static final TexturedModel.Factory CORAL_FAN;
   public static final TexturedModel.Factory PARTICLE;
   public static final TexturedModel.Factory TEMPLATE_ANVIL;
   public static final TexturedModel.Factory LEAVES;
   public static final TexturedModel.Factory TEMPLATE_LANTERN;
   public static final TexturedModel.Factory TEMPLATE_HANGING_LANTERN;
   public static final TexturedModel.Factory TEMPLATE_SEAGRASS;
   public static final TexturedModel.Factory END_FOR_TOP_CUBE_COLUMN;
   public static final TexturedModel.Factory END_FOR_TOP_CUBE_COLUMN_HORIZONTAL;
   public static final TexturedModel.Factory WALL_CUBE_BOTTOM_TOP;
   public static final TexturedModel.Factory field_23959;
   private final Texture texture;
   private final Model model;

   private TexturedModel(Texture texture, Model model) {
      this.texture = texture;
      this.model = model;
   }

   public Model getModel() {
      return this.model;
   }

   public Texture getTexture() {
      return this.texture;
   }

   public TexturedModel texture(Consumer<Texture> textureConsumer) {
      textureConsumer.accept(this.texture);
      return this;
   }

   public Identifier upload(Block block, BiConsumer<Identifier, Supplier<JsonElement>> writer) {
      return this.model.upload(block, this.texture, writer);
   }

   public Identifier upload(Block block, String suffix, BiConsumer<Identifier, Supplier<JsonElement>> writer) {
      return this.model.upload(block, suffix, this.texture, writer);
   }

   private static TexturedModel.Factory makeFactory(Function<Block, Texture> textureGetter, Model model) {
      return (block) -> {
         return new TexturedModel((Texture)textureGetter.apply(block), model);
      };
   }

   public static TexturedModel getCubeAll(Identifier id) {
      return new TexturedModel(Texture.all(id), Models.CUBE_ALL);
   }

   static {
      CUBE_ALL = makeFactory(Texture::all, Models.CUBE_ALL);
      CUBE_MIRRORED_ALL = makeFactory(Texture::all, Models.CUBE_MIRRORED_ALL);
      CUBE_COLUMN = makeFactory(Texture::sideEnd, Models.CUBE_COLUMN);
      CUBE_COLUMN_HORIZONTAL = makeFactory(Texture::sideEnd, Models.CUBE_COLUMN_HORIZONTAL);
      CUBE_BOTTOM_TOP = makeFactory(Texture::sideTopBottom, Models.CUBE_BOTTOM_TOP);
      CUBE_TOP = makeFactory(Texture::sideAndTop, Models.CUBE_TOP);
      ORIENTABLE = makeFactory(Texture::sideFrontTop, Models.ORIENTABLE);
      ORIENTABLE_WITH_BOTTOM = makeFactory(Texture::sideFrontTopBottom, Models.ORIENTABLE_WITH_BOTTOM);
      CARPET = makeFactory(Texture::wool, Models.CARPET);
      TEMPLATE_GLAZED_TERRACOTTA = makeFactory(Texture::pattern, Models.TEMPLATE_GLAZED_TERRACOTTA);
      CORAL_FAN = makeFactory(Texture::fan, Models.CORAL_FAN);
      PARTICLE = makeFactory(Texture::particle, Models.PARTICLE);
      TEMPLATE_ANVIL = makeFactory(Texture::top, Models.TEMPLATE_ANVIL);
      LEAVES = makeFactory(Texture::all, Models.LEAVES);
      TEMPLATE_LANTERN = makeFactory(Texture::lantern, Models.TEMPLATE_LANTERN);
      TEMPLATE_HANGING_LANTERN = makeFactory(Texture::lantern, Models.TEMPLATE_HANGING_LANTERN);
      TEMPLATE_SEAGRASS = makeFactory(Texture::texture, Models.TEMPLATE_SEAGRASS);
      END_FOR_TOP_CUBE_COLUMN = makeFactory(Texture::sideAndEndForTop, Models.CUBE_COLUMN);
      END_FOR_TOP_CUBE_COLUMN_HORIZONTAL = makeFactory(Texture::sideAndEndForTop, Models.CUBE_COLUMN_HORIZONTAL);
      WALL_CUBE_BOTTOM_TOP = makeFactory(Texture::wallSideTopBottom, Models.CUBE_BOTTOM_TOP);
      field_23959 = makeFactory(Texture::method_27168, Models.CUBE_COLUMN);
   }

   @FunctionalInterface
   public interface Factory {
      TexturedModel get(Block block);

      default Identifier upload(Block block, BiConsumer<Identifier, Supplier<JsonElement>> writer) {
         return this.get(block).upload(block, writer);
      }

      default Identifier upload(Block block, String suffix, BiConsumer<Identifier, Supplier<JsonElement>> writer) {
         return this.get(block).upload(block, suffix, writer);
      }

      default TexturedModel.Factory withTexture(Consumer<Texture> textureConsumer) {
         return (block) -> {
            return this.get(block).texture(textureConsumer);
         };
      }
   }
}
