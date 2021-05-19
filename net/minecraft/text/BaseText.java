package net.minecraft.text;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;

public abstract class BaseText implements MutableText {
   protected final List<Text> siblings = Lists.newArrayList();
   private OrderedText orderedText;
   @Nullable
   @Environment(EnvType.CLIENT)
   private Language previousLanguage;
   private Style style;

   public BaseText() {
      this.orderedText = OrderedText.EMPTY;
      this.style = Style.EMPTY;
   }

   public MutableText append(Text text) {
      this.siblings.add(text);
      return this;
   }

   public String asString() {
      return "";
   }

   public List<Text> getSiblings() {
      return this.siblings;
   }

   public MutableText setStyle(Style style) {
      this.style = style;
      return this;
   }

   public Style getStyle() {
      return this.style;
   }

   public abstract BaseText copy();

   public final MutableText shallowCopy() {
      BaseText baseText = this.copy();
      baseText.siblings.addAll(this.siblings);
      baseText.setStyle(this.style);
      return baseText;
   }

   @Environment(EnvType.CLIENT)
   public OrderedText asOrderedText() {
      Language language = Language.getInstance();
      if (this.previousLanguage != language) {
         this.orderedText = language.reorder((StringVisitable)this);
         this.previousLanguage = language;
      }

      return this.orderedText;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof BaseText)) {
         return false;
      } else {
         BaseText baseText = (BaseText)obj;
         return this.siblings.equals(baseText.siblings) && Objects.equals(this.getStyle(), baseText.getStyle());
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.getStyle(), this.siblings});
   }

   public String toString() {
      return "BaseComponent{style=" + this.style + ", siblings=" + this.siblings + '}';
   }
}
