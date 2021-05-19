package net.minecraft.client.font;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.ResourceManager;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface FontLoader {
   @Nullable
   Font load(ResourceManager manager);
}
