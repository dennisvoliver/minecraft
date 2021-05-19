package net.minecraft.client.gui.screen.pack;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.ResourcePackCompatibility;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ResourcePackOrganizer {
   private final ResourcePackManager resourcePackManager;
   private final List<ResourcePackProfile> enabledPacks;
   private final List<ResourcePackProfile> disabledPacks;
   private final Function<ResourcePackProfile, Identifier> field_25785;
   private final Runnable updateCallback;
   private final Consumer<ResourcePackManager> applier;

   public ResourcePackOrganizer(Runnable updateCallback, Function<ResourcePackProfile, Identifier> function, ResourcePackManager resourcePackManager, Consumer<ResourcePackManager> consumer) {
      this.updateCallback = updateCallback;
      this.field_25785 = function;
      this.resourcePackManager = resourcePackManager;
      this.enabledPacks = Lists.newArrayList((Iterable)resourcePackManager.getEnabledProfiles());
      Collections.reverse(this.enabledPacks);
      this.disabledPacks = Lists.newArrayList((Iterable)resourcePackManager.getProfiles());
      this.disabledPacks.removeAll(this.enabledPacks);
      this.applier = consumer;
   }

   public Stream<ResourcePackOrganizer.Pack> getDisabledPacks() {
      return this.disabledPacks.stream().map((resourcePackProfile) -> {
         return new ResourcePackOrganizer.DisabledPack(resourcePackProfile);
      });
   }

   public Stream<ResourcePackOrganizer.Pack> getEnabledPacks() {
      return this.enabledPacks.stream().map((resourcePackProfile) -> {
         return new ResourcePackOrganizer.EnabledPack(resourcePackProfile);
      });
   }

   public void apply() {
      this.resourcePackManager.setEnabledProfiles((Collection)Lists.reverse(this.enabledPacks).stream().map(ResourcePackProfile::getName).collect(ImmutableList.toImmutableList()));
      this.applier.accept(this.resourcePackManager);
   }

   public void refresh() {
      this.resourcePackManager.scanPacks();
      this.enabledPacks.retainAll(this.resourcePackManager.getProfiles());
      this.disabledPacks.clear();
      this.disabledPacks.addAll(this.resourcePackManager.getProfiles());
      this.disabledPacks.removeAll(this.enabledPacks);
   }

   @Environment(EnvType.CLIENT)
   class DisabledPack extends ResourcePackOrganizer.AbstractPack {
      public DisabledPack(ResourcePackProfile resourcePackProfile) {
         super(resourcePackProfile);
      }

      protected List<ResourcePackProfile> getCurrentList() {
         return ResourcePackOrganizer.this.disabledPacks;
      }

      protected List<ResourcePackProfile> getOppositeList() {
         return ResourcePackOrganizer.this.enabledPacks;
      }

      public boolean isEnabled() {
         return false;
      }

      public void enable() {
         this.toggle();
      }

      public void disable() {
      }
   }

   @Environment(EnvType.CLIENT)
   class EnabledPack extends ResourcePackOrganizer.AbstractPack {
      public EnabledPack(ResourcePackProfile resourcePackProfile) {
         super(resourcePackProfile);
      }

      protected List<ResourcePackProfile> getCurrentList() {
         return ResourcePackOrganizer.this.enabledPacks;
      }

      protected List<ResourcePackProfile> getOppositeList() {
         return ResourcePackOrganizer.this.disabledPacks;
      }

      public boolean isEnabled() {
         return true;
      }

      public void enable() {
      }

      public void disable() {
         this.toggle();
      }
   }

   @Environment(EnvType.CLIENT)
   abstract class AbstractPack implements ResourcePackOrganizer.Pack {
      private final ResourcePackProfile profile;

      public AbstractPack(ResourcePackProfile profile) {
         this.profile = profile;
      }

      protected abstract List<ResourcePackProfile> getCurrentList();

      protected abstract List<ResourcePackProfile> getOppositeList();

      public Identifier method_30286() {
         return (Identifier)ResourcePackOrganizer.this.field_25785.apply(this.profile);
      }

      public ResourcePackCompatibility getCompatibility() {
         return this.profile.getCompatibility();
      }

      public Text getDisplayName() {
         return this.profile.getDisplayName();
      }

      public Text getDescription() {
         return this.profile.getDescription();
      }

      public ResourcePackSource getSource() {
         return this.profile.getSource();
      }

      public boolean isPinned() {
         return this.profile.isPinned();
      }

      public boolean isAlwaysEnabled() {
         return this.profile.isAlwaysEnabled();
      }

      protected void toggle() {
         this.getCurrentList().remove(this.profile);
         this.profile.getInitialPosition().insert(this.getOppositeList(), this.profile, Function.identity(), true);
         ResourcePackOrganizer.this.updateCallback.run();
      }

      protected void move(int offset) {
         List<ResourcePackProfile> list = this.getCurrentList();
         int i = list.indexOf(this.profile);
         list.remove(i);
         list.add(i + offset, this.profile);
         ResourcePackOrganizer.this.updateCallback.run();
      }

      public boolean canMoveTowardStart() {
         List<ResourcePackProfile> list = this.getCurrentList();
         int i = list.indexOf(this.profile);
         return i > 0 && !((ResourcePackProfile)list.get(i - 1)).isPinned();
      }

      public void moveTowardStart() {
         this.move(-1);
      }

      public boolean canMoveTowardEnd() {
         List<ResourcePackProfile> list = this.getCurrentList();
         int i = list.indexOf(this.profile);
         return i >= 0 && i < list.size() - 1 && !((ResourcePackProfile)list.get(i + 1)).isPinned();
      }

      public void moveTowardEnd() {
         this.move(1);
      }
   }

   @Environment(EnvType.CLIENT)
   public interface Pack {
      Identifier method_30286();

      ResourcePackCompatibility getCompatibility();

      Text getDisplayName();

      Text getDescription();

      ResourcePackSource getSource();

      default Text getDecoratedDescription() {
         return this.getSource().decorate(this.getDescription());
      }

      boolean isPinned();

      boolean isAlwaysEnabled();

      void enable();

      void disable();

      void moveTowardStart();

      void moveTowardEnd();

      boolean isEnabled();

      default boolean canBeEnabled() {
         return !this.isEnabled();
      }

      default boolean canBeDisabled() {
         return this.isEnabled() && !this.isAlwaysEnabled();
      }

      boolean canMoveTowardStart();

      boolean canMoveTowardEnd();
   }
}
