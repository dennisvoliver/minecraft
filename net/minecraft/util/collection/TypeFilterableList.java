package net.minecraft.util.collection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TypeFilterableList<T> extends AbstractCollection<T> {
   private final Map<Class<?>, List<T>> elementsByType = Maps.newHashMap();
   private final Class<T> elementType;
   private final List<T> allElements = Lists.newArrayList();

   public TypeFilterableList(Class<T> elementType) {
      this.elementType = elementType;
      this.elementsByType.put(elementType, this.allElements);
   }

   public boolean add(T e) {
      boolean bl = false;
      Iterator var3 = this.elementsByType.entrySet().iterator();

      while(var3.hasNext()) {
         Entry<Class<?>, List<T>> entry = (Entry)var3.next();
         if (((Class)entry.getKey()).isInstance(e)) {
            bl |= ((List)entry.getValue()).add(e);
         }
      }

      return bl;
   }

   public boolean remove(Object o) {
      boolean bl = false;
      Iterator var3 = this.elementsByType.entrySet().iterator();

      while(var3.hasNext()) {
         Entry<Class<?>, List<T>> entry = (Entry)var3.next();
         if (((Class)entry.getKey()).isInstance(o)) {
            List<T> list = (List)entry.getValue();
            bl |= list.remove(o);
         }
      }

      return bl;
   }

   public boolean contains(Object o) {
      return this.getAllOfType(o.getClass()).contains(o);
   }

   public <S> Collection<S> getAllOfType(Class<S> type) {
      if (!this.elementType.isAssignableFrom(type)) {
         throw new IllegalArgumentException("Don't know how to search for " + type);
      } else {
         List<T> list = (List)this.elementsByType.computeIfAbsent(type, (class_) -> {
            Stream var10000 = this.allElements.stream();
            class_.getClass();
            return (List)var10000.filter(class_::isInstance).collect(Collectors.toList());
         });
         return Collections.unmodifiableCollection(list);
      }
   }

   public Iterator<T> iterator() {
      return (Iterator)(this.allElements.isEmpty() ? Collections.emptyIterator() : Iterators.unmodifiableIterator(this.allElements.iterator()));
   }

   public List<T> method_29903() {
      return ImmutableList.copyOf((Collection)this.allElements);
   }

   public int size() {
      return this.allElements.size();
   }
}
