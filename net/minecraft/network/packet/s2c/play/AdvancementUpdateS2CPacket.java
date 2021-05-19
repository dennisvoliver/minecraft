package net.minecraft.network.packet.s2c.play;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.util.Identifier;

public class AdvancementUpdateS2CPacket implements Packet<ClientPlayPacketListener> {
   private boolean clearCurrent;
   private Map<Identifier, Advancement.Task> toEarn;
   private Set<Identifier> toRemove;
   private Map<Identifier, AdvancementProgress> toSetProgress;

   public AdvancementUpdateS2CPacket() {
   }

   public AdvancementUpdateS2CPacket(boolean clearCurrent, Collection<Advancement> toEarn, Set<Identifier> toRemove, Map<Identifier, AdvancementProgress> toSetProgress) {
      this.clearCurrent = clearCurrent;
      this.toEarn = Maps.newHashMap();
      Iterator var5 = toEarn.iterator();

      while(var5.hasNext()) {
         Advancement advancement = (Advancement)var5.next();
         this.toEarn.put(advancement.getId(), advancement.createTask());
      }

      this.toRemove = toRemove;
      this.toSetProgress = Maps.newHashMap(toSetProgress);
   }

   public void apply(ClientPlayPacketListener clientPlayPacketListener) {
      clientPlayPacketListener.onAdvancements(this);
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.clearCurrent = buf.readBoolean();
      this.toEarn = Maps.newHashMap();
      this.toRemove = Sets.newLinkedHashSet();
      this.toSetProgress = Maps.newHashMap();
      int i = buf.readVarInt();

      int l;
      Identifier identifier3;
      for(l = 0; l < i; ++l) {
         identifier3 = buf.readIdentifier();
         Advancement.Task task = Advancement.Task.fromPacket(buf);
         this.toEarn.put(identifier3, task);
      }

      i = buf.readVarInt();

      for(l = 0; l < i; ++l) {
         identifier3 = buf.readIdentifier();
         this.toRemove.add(identifier3);
      }

      i = buf.readVarInt();

      for(l = 0; l < i; ++l) {
         identifier3 = buf.readIdentifier();
         this.toSetProgress.put(identifier3, AdvancementProgress.fromPacket(buf));
      }

   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeBoolean(this.clearCurrent);
      buf.writeVarInt(this.toEarn.size());
      Iterator var2 = this.toEarn.entrySet().iterator();

      Entry entry2;
      while(var2.hasNext()) {
         entry2 = (Entry)var2.next();
         Identifier identifier = (Identifier)entry2.getKey();
         Advancement.Task task = (Advancement.Task)entry2.getValue();
         buf.writeIdentifier(identifier);
         task.toPacket(buf);
      }

      buf.writeVarInt(this.toRemove.size());
      var2 = this.toRemove.iterator();

      while(var2.hasNext()) {
         Identifier identifier2 = (Identifier)var2.next();
         buf.writeIdentifier(identifier2);
      }

      buf.writeVarInt(this.toSetProgress.size());
      var2 = this.toSetProgress.entrySet().iterator();

      while(var2.hasNext()) {
         entry2 = (Entry)var2.next();
         buf.writeIdentifier((Identifier)entry2.getKey());
         ((AdvancementProgress)entry2.getValue()).toPacket(buf);
      }

   }

   @Environment(EnvType.CLIENT)
   public Map<Identifier, Advancement.Task> getAdvancementsToEarn() {
      return this.toEarn;
   }

   @Environment(EnvType.CLIENT)
   public Set<Identifier> getAdvancementIdsToRemove() {
      return this.toRemove;
   }

   @Environment(EnvType.CLIENT)
   public Map<Identifier, AdvancementProgress> getAdvancementsToProgress() {
      return this.toSetProgress;
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldClearCurrent() {
      return this.clearCurrent;
   }
}
