package net.minecraft.client.realms.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.dto.WorldTemplate;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class RealmsScreenWithCallback extends RealmsScreen {
   protected abstract void callback(@Nullable WorldTemplate template);
}
