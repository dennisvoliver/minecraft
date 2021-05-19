package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface ParticleFactory<T extends ParticleEffect> {
   @Nullable
   Particle createParticle(T parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ);
}
