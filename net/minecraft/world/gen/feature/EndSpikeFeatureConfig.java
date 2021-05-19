package net.minecraft.world.gen.feature;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class EndSpikeFeatureConfig implements FeatureConfig {
   public static final Codec<EndSpikeFeatureConfig> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.BOOL.fieldOf("crystal_invulnerable").orElse(false).forGetter((endSpikeFeatureConfig) -> {
         return endSpikeFeatureConfig.crystalInvulnerable;
      }), EndSpikeFeature.Spike.CODEC.listOf().fieldOf("spikes").forGetter((endSpikeFeatureConfig) -> {
         return endSpikeFeatureConfig.spikes;
      }), BlockPos.CODEC.optionalFieldOf("crystal_beam_target").forGetter((endSpikeFeatureConfig) -> {
         return Optional.ofNullable(endSpikeFeatureConfig.crystalBeamTarget);
      })).apply(instance, (Function3)(EndSpikeFeatureConfig::new));
   });
   private final boolean crystalInvulnerable;
   private final List<EndSpikeFeature.Spike> spikes;
   @Nullable
   private final BlockPos crystalBeamTarget;

   public EndSpikeFeatureConfig(boolean crystalInvulnerable, List<EndSpikeFeature.Spike> spikes, @Nullable BlockPos crystalBeamTarget) {
      this(crystalInvulnerable, spikes, Optional.ofNullable(crystalBeamTarget));
   }

   private EndSpikeFeatureConfig(boolean crystalInvulnerable, List<EndSpikeFeature.Spike> spikes, Optional<BlockPos> crystalBeamTarget) {
      this.crystalInvulnerable = crystalInvulnerable;
      this.spikes = spikes;
      this.crystalBeamTarget = (BlockPos)crystalBeamTarget.orElse((Object)null);
   }

   public boolean isCrystalInvulnerable() {
      return this.crystalInvulnerable;
   }

   public List<EndSpikeFeature.Spike> getSpikes() {
      return this.spikes;
   }

   @Nullable
   public BlockPos getPos() {
      return this.crystalBeamTarget;
   }
}
