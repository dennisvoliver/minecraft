package net.minecraft.state;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.state.property.Property;
import org.jetbrains.annotations.Nullable;

public class StateManager<O, S extends State<O, S>> {
   private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
   private final O owner;
   private final ImmutableSortedMap<String, Property<?>> properties;
   private final ImmutableList<S> states;

   protected StateManager(Function<O, S> function, O object, StateManager.Factory<O, S> factory, Map<String, Property<?>> propertiesMap) {
      this.owner = object;
      this.properties = ImmutableSortedMap.copyOf(propertiesMap);
      Supplier<S> supplier = () -> {
         return (State)function.apply(object);
      };
      MapCodec<S> mapCodec = MapCodec.of(Encoder.empty(), Decoder.unit(supplier));

      Entry entry;
      for(UnmodifiableIterator var7 = this.properties.entrySet().iterator(); var7.hasNext(); mapCodec = method_30040(mapCodec, supplier, (String)entry.getKey(), (Property)entry.getValue())) {
         entry = (Entry)var7.next();
      }

      Map<Map<Property<?>, Comparable<?>>, S> map = Maps.newLinkedHashMap();
      List<S> list = Lists.newArrayList();
      Stream<List<Pair<Property<?>, Comparable<?>>>> stream = Stream.of(Collections.emptyList());

      Property property;
      for(UnmodifiableIterator var11 = this.properties.values().iterator(); var11.hasNext(); stream = stream.flatMap((listx) -> {
         return property.getValues().stream().map((comparable) -> {
            List<Pair<Property<?>, Comparable<?>>> list2 = Lists.newArrayList((Iterable)listx);
            list2.add(Pair.of(property, comparable));
            return list2;
         });
      })) {
         property = (Property)var11.next();
      }

      stream.forEach((list2) -> {
         ImmutableMap<Property<?>, Comparable<?>> immutableMap = (ImmutableMap)list2.stream().collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));
         S state = (State)factory.create(object, immutableMap, mapCodec);
         map.put(immutableMap, state);
         list.add(state);
      });
      Iterator var13 = list.iterator();

      while(var13.hasNext()) {
         S state = (State)var13.next();
         state.createWithTable(map);
      }

      this.states = ImmutableList.copyOf((Collection)list);
   }

   private static <S extends State<?, S>, T extends Comparable<T>> MapCodec<S> method_30040(MapCodec<S> mapCodec, Supplier<S> supplier, String string, Property<T> property) {
      return Codec.mapPair(mapCodec, property.getValueCodec().fieldOf(string).setPartial(() -> {
         return property.createValue((State)supplier.get());
      })).xmap((pair) -> {
         return (State)((State)pair.getFirst()).with(property, ((Property.Value)pair.getSecond()).getValue());
      }, (state) -> {
         return Pair.of(state, property.createValue(state));
      });
   }

   public ImmutableList<S> getStates() {
      return this.states;
   }

   public S getDefaultState() {
      return (State)this.states.get(0);
   }

   public O getOwner() {
      return this.owner;
   }

   public Collection<Property<?>> getProperties() {
      return this.properties.values();
   }

   public String toString() {
      return MoreObjects.toStringHelper((Object)this).add("block", this.owner).add("properties", this.properties.values().stream().map(Property::getName).collect(Collectors.toList())).toString();
   }

   @Nullable
   public Property<?> getProperty(String name) {
      return (Property)this.properties.get(name);
   }

   public static class Builder<O, S extends State<O, S>> {
      private final O owner;
      private final Map<String, Property<?>> namedProperties = Maps.newHashMap();

      public Builder(O owner) {
         this.owner = owner;
      }

      public StateManager.Builder<O, S> add(Property<?>... properties) {
         Property[] var2 = properties;
         int var3 = properties.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            Property<?> property = var2[var4];
            this.validate(property);
            this.namedProperties.put(property.getName(), property);
         }

         return this;
      }

      private <T extends Comparable<T>> void validate(Property<T> property) {
         String string = property.getName();
         if (!StateManager.VALID_NAME_PATTERN.matcher(string).matches()) {
            throw new IllegalArgumentException(this.owner + " has invalidly named property: " + string);
         } else {
            Collection<T> collection = property.getValues();
            if (collection.size() <= 1) {
               throw new IllegalArgumentException(this.owner + " attempted use property " + string + " with <= 1 possible values");
            } else {
               Iterator var4 = collection.iterator();

               String string2;
               do {
                  if (!var4.hasNext()) {
                     if (this.namedProperties.containsKey(string)) {
                        throw new IllegalArgumentException(this.owner + " has duplicate property: " + string);
                     }

                     return;
                  }

                  T comparable = (Comparable)var4.next();
                  string2 = property.name(comparable);
               } while(StateManager.VALID_NAME_PATTERN.matcher(string2).matches());

               throw new IllegalArgumentException(this.owner + " has property: " + string + " with invalidly named value: " + string2);
            }
         }
      }

      public StateManager<O, S> build(Function<O, S> ownerToStateFunction, StateManager.Factory<O, S> factory) {
         return new StateManager(ownerToStateFunction, this.owner, factory, this.namedProperties);
      }
   }

   public interface Factory<O, S> {
      S create(O owner, ImmutableMap<Property<?>, Comparable<?>> entries, MapCodec<S> mapCodec);
   }
}
