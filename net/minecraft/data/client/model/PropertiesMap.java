package net.minecraft.data.client.model;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.state.property.Property;

/**
 * Represents a set of property to value pairs, used as conditions for model
 * application.
 * 
 * <p>This object is immutable.
 */
public final class PropertiesMap {
   private static final PropertiesMap EMPTY = new PropertiesMap(ImmutableList.of());
   private static final Comparator<Property.Value<?>> COMPARATOR = Comparator.comparing((value) -> {
      return value.getProperty().getName();
   });
   private final List<Property.Value<?>> propertyValues;

   public PropertiesMap method_25819(Property.Value<?> value) {
      return new PropertiesMap(ImmutableList.builder().addAll((Iterable)this.propertyValues).add((Object)value).build());
   }

   public PropertiesMap with(PropertiesMap propertiesMap) {
      return new PropertiesMap(ImmutableList.builder().addAll((Iterable)this.propertyValues).addAll((Iterable)propertiesMap.propertyValues).build());
   }

   private PropertiesMap(List<Property.Value<?>> list) {
      this.propertyValues = list;
   }

   public static PropertiesMap empty() {
      return EMPTY;
   }

   public static PropertiesMap method_25821(Property.Value<?>... values) {
      return new PropertiesMap(ImmutableList.copyOf((Object[])values));
   }

   public boolean equals(Object object) {
      return this == object || object instanceof PropertiesMap && this.propertyValues.equals(((PropertiesMap)object).propertyValues);
   }

   public int hashCode() {
      return this.propertyValues.hashCode();
   }

   public String asString() {
      return (String)this.propertyValues.stream().sorted(COMPARATOR).map(Property.Value::toString).collect(Collectors.joining(","));
   }

   public String toString() {
      return this.asString();
   }
}
