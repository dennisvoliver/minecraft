package net.minecraft.scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class ServerScoreboard extends Scoreboard {
   private final MinecraftServer server;
   private final Set<ScoreboardObjective> objectives = Sets.newHashSet();
   private Runnable[] updateListeners = new Runnable[0];

   public ServerScoreboard(MinecraftServer server) {
      this.server = server;
   }

   public void updateScore(ScoreboardPlayerScore score) {
      super.updateScore(score);
      if (this.objectives.contains(score.getObjective())) {
         this.server.getPlayerManager().sendToAll(new ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.CHANGE, score.getObjective().getName(), score.getPlayerName(), score.getScore()));
      }

      this.runUpdateListeners();
   }

   public void updatePlayerScore(String playerName) {
      super.updatePlayerScore(playerName);
      this.server.getPlayerManager().sendToAll(new ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.REMOVE, (String)null, playerName, 0));
      this.runUpdateListeners();
   }

   public void updatePlayerScore(String playerName, ScoreboardObjective objective) {
      super.updatePlayerScore(playerName, objective);
      if (this.objectives.contains(objective)) {
         this.server.getPlayerManager().sendToAll(new ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.REMOVE, objective.getName(), playerName, 0));
      }

      this.runUpdateListeners();
   }

   public void setObjectiveSlot(int slot, @Nullable ScoreboardObjective objective) {
      ScoreboardObjective scoreboardObjective = this.getObjectiveForSlot(slot);
      super.setObjectiveSlot(slot, objective);
      if (scoreboardObjective != objective && scoreboardObjective != null) {
         if (this.getSlot(scoreboardObjective) > 0) {
            this.server.getPlayerManager().sendToAll(new ScoreboardDisplayS2CPacket(slot, objective));
         } else {
            this.removeScoreboardObjective(scoreboardObjective);
         }
      }

      if (objective != null) {
         if (this.objectives.contains(objective)) {
            this.server.getPlayerManager().sendToAll(new ScoreboardDisplayS2CPacket(slot, objective));
         } else {
            this.addScoreboardObjective(objective);
         }
      }

      this.runUpdateListeners();
   }

   public boolean addPlayerToTeam(String playerName, Team team) {
      if (super.addPlayerToTeam(playerName, team)) {
         this.server.getPlayerManager().sendToAll(new TeamS2CPacket(team, Arrays.asList(playerName), 3));
         this.runUpdateListeners();
         return true;
      } else {
         return false;
      }
   }

   public void removePlayerFromTeam(String playerName, Team team) {
      super.removePlayerFromTeam(playerName, team);
      this.server.getPlayerManager().sendToAll(new TeamS2CPacket(team, Arrays.asList(playerName), 4));
      this.runUpdateListeners();
   }

   public void updateObjective(ScoreboardObjective objective) {
      super.updateObjective(objective);
      this.runUpdateListeners();
   }

   public void updateExistingObjective(ScoreboardObjective objective) {
      super.updateExistingObjective(objective);
      if (this.objectives.contains(objective)) {
         this.server.getPlayerManager().sendToAll(new ScoreboardObjectiveUpdateS2CPacket(objective, 2));
      }

      this.runUpdateListeners();
   }

   public void updateRemovedObjective(ScoreboardObjective objective) {
      super.updateRemovedObjective(objective);
      if (this.objectives.contains(objective)) {
         this.removeScoreboardObjective(objective);
      }

      this.runUpdateListeners();
   }

   public void updateScoreboardTeamAndPlayers(Team team) {
      super.updateScoreboardTeamAndPlayers(team);
      this.server.getPlayerManager().sendToAll(new TeamS2CPacket(team, 0));
      this.runUpdateListeners();
   }

   public void updateScoreboardTeam(Team team) {
      super.updateScoreboardTeam(team);
      this.server.getPlayerManager().sendToAll(new TeamS2CPacket(team, 2));
      this.runUpdateListeners();
   }

   public void updateRemovedTeam(Team team) {
      super.updateRemovedTeam(team);
      this.server.getPlayerManager().sendToAll(new TeamS2CPacket(team, 1));
      this.runUpdateListeners();
   }

   public void addUpdateListener(Runnable listener) {
      this.updateListeners = (Runnable[])Arrays.copyOf(this.updateListeners, this.updateListeners.length + 1);
      this.updateListeners[this.updateListeners.length - 1] = listener;
   }

   protected void runUpdateListeners() {
      Runnable[] var1 = this.updateListeners;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         Runnable runnable = var1[var3];
         runnable.run();
      }

   }

   public List<Packet<?>> createChangePackets(ScoreboardObjective objective) {
      List<Packet<?>> list = Lists.newArrayList();
      list.add(new ScoreboardObjectiveUpdateS2CPacket(objective, 0));

      for(int i = 0; i < 19; ++i) {
         if (this.getObjectiveForSlot(i) == objective) {
            list.add(new ScoreboardDisplayS2CPacket(i, objective));
         }
      }

      Iterator var5 = this.getAllPlayerScores(objective).iterator();

      while(var5.hasNext()) {
         ScoreboardPlayerScore scoreboardPlayerScore = (ScoreboardPlayerScore)var5.next();
         list.add(new ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.CHANGE, scoreboardPlayerScore.getObjective().getName(), scoreboardPlayerScore.getPlayerName(), scoreboardPlayerScore.getScore()));
      }

      return list;
   }

   public void addScoreboardObjective(ScoreboardObjective objective) {
      List<Packet<?>> list = this.createChangePackets(objective);
      Iterator var3 = this.server.getPlayerManager().getPlayerList().iterator();

      while(var3.hasNext()) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var3.next();
         Iterator var5 = list.iterator();

         while(var5.hasNext()) {
            Packet<?> packet = (Packet)var5.next();
            serverPlayerEntity.networkHandler.sendPacket(packet);
         }
      }

      this.objectives.add(objective);
   }

   public List<Packet<?>> createRemovePackets(ScoreboardObjective objective) {
      List<Packet<?>> list = Lists.newArrayList();
      list.add(new ScoreboardObjectiveUpdateS2CPacket(objective, 1));

      for(int i = 0; i < 19; ++i) {
         if (this.getObjectiveForSlot(i) == objective) {
            list.add(new ScoreboardDisplayS2CPacket(i, objective));
         }
      }

      return list;
   }

   public void removeScoreboardObjective(ScoreboardObjective objective) {
      List<Packet<?>> list = this.createRemovePackets(objective);
      Iterator var3 = this.server.getPlayerManager().getPlayerList().iterator();

      while(var3.hasNext()) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var3.next();
         Iterator var5 = list.iterator();

         while(var5.hasNext()) {
            Packet<?> packet = (Packet)var5.next();
            serverPlayerEntity.networkHandler.sendPacket(packet);
         }
      }

      this.objectives.remove(objective);
   }

   public int getSlot(ScoreboardObjective objective) {
      int i = 0;

      for(int j = 0; j < 19; ++j) {
         if (this.getObjectiveForSlot(j) == objective) {
            ++i;
         }
      }

      return i;
   }

   public static enum UpdateMode {
      CHANGE,
      REMOVE;
   }
}
