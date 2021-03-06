package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.GameMode;

@Environment(EnvType.CLIENT)
public class OpenToLanScreen extends Screen {
   private static final Text ALLOW_COMMANDS_TEXT = new TranslatableText("selectWorld.allowCommands");
   private static final Text GAME_MODE_TEXT = new TranslatableText("selectWorld.gameMode");
   private static final Text OTHER_PLAYERS_TEXT = new TranslatableText("lanServer.otherPlayers");
   private final Screen parent;
   private ButtonWidget buttonAllowCommands;
   private ButtonWidget buttonGameMode;
   private String gameMode = "survival";
   private boolean allowCommands;

   public OpenToLanScreen(Screen parent) {
      super(new TranslatableText("lanServer.title"));
      this.parent = parent;
   }

   protected void init() {
      this.addButton(new ButtonWidget(this.width / 2 - 155, this.height - 28, 150, 20, new TranslatableText("lanServer.start"), (buttonWidget) -> {
         this.client.openScreen((Screen)null);
         int i = NetworkUtils.findLocalPort();
         TranslatableText text2;
         if (this.client.getServer().openToLan(GameMode.byName(this.gameMode), this.allowCommands, i)) {
            text2 = new TranslatableText("commands.publish.started", new Object[]{i});
         } else {
            text2 = new TranslatableText("commands.publish.failed");
         }

         this.client.inGameHud.getChatHud().addMessage(text2);
         this.client.updateWindowTitle();
      }));
      this.addButton(new ButtonWidget(this.width / 2 + 5, this.height - 28, 150, 20, ScreenTexts.CANCEL, (buttonWidget) -> {
         this.client.openScreen(this.parent);
      }));
      this.buttonGameMode = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 155, 100, 150, 20, LiteralText.EMPTY, (buttonWidget) -> {
         if ("spectator".equals(this.gameMode)) {
            this.gameMode = "creative";
         } else if ("creative".equals(this.gameMode)) {
            this.gameMode = "adventure";
         } else if ("adventure".equals(this.gameMode)) {
            this.gameMode = "survival";
         } else {
            this.gameMode = "spectator";
         }

         this.updateButtonText();
      }));
      this.buttonAllowCommands = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 + 5, 100, 150, 20, ALLOW_COMMANDS_TEXT, (buttonWidget) -> {
         this.allowCommands = !this.allowCommands;
         this.updateButtonText();
      }));
      this.updateButtonText();
   }

   private void updateButtonText() {
      this.buttonGameMode.setMessage(new TranslatableText("options.generic_value", new Object[]{GAME_MODE_TEXT, new TranslatableText("selectWorld.gameMode." + this.gameMode)}));
      this.buttonAllowCommands.setMessage(ScreenTexts.composeToggleText(ALLOW_COMMANDS_TEXT, this.allowCommands));
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 50, 16777215);
      drawCenteredText(matrices, this.textRenderer, OTHER_PLAYERS_TEXT, this.width / 2, 82, 16777215);
      super.render(matrices, mouseX, mouseY, delta);
   }
}
