package net.minecraft.client.gui.screen.ingame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.CommandBlockExecutor;

@Environment(EnvType.CLIENT)
public class CommandBlockScreen extends AbstractCommandBlockScreen {
   private final CommandBlockBlockEntity blockEntity;
   private ButtonWidget modeButton;
   private ButtonWidget conditionalModeButton;
   private ButtonWidget redstoneTriggerButton;
   private CommandBlockBlockEntity.Type mode;
   private boolean conditional;
   private boolean autoActivate;

   public CommandBlockScreen(CommandBlockBlockEntity blockEntity) {
      this.mode = CommandBlockBlockEntity.Type.REDSTONE;
      this.blockEntity = blockEntity;
   }

   CommandBlockExecutor getCommandExecutor() {
      return this.blockEntity.getCommandExecutor();
   }

   int getTrackOutputButtonHeight() {
      return 135;
   }

   protected void init() {
      super.init();
      this.modeButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 50 - 100 - 4, 165, 100, 20, new TranslatableText("advMode.mode.sequence"), (buttonWidget) -> {
         this.cycleType();
         this.updateMode();
      }));
      this.conditionalModeButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 50, 165, 100, 20, new TranslatableText("advMode.mode.unconditional"), (buttonWidget) -> {
         this.conditional = !this.conditional;
         this.updateConditionalMode();
      }));
      this.redstoneTriggerButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 + 50 + 4, 165, 100, 20, new TranslatableText("advMode.mode.redstoneTriggered"), (buttonWidget) -> {
         this.autoActivate = !this.autoActivate;
         this.updateActivationMode();
      }));
      this.doneButton.active = false;
      this.toggleTrackingOutputButton.active = false;
      this.modeButton.active = false;
      this.conditionalModeButton.active = false;
      this.redstoneTriggerButton.active = false;
   }

   public void updateCommandBlock() {
      CommandBlockExecutor commandBlockExecutor = this.blockEntity.getCommandExecutor();
      this.consoleCommandTextField.setText(commandBlockExecutor.getCommand());
      this.trackingOutput = commandBlockExecutor.isTrackingOutput();
      this.mode = this.blockEntity.getCommandBlockType();
      this.conditional = this.blockEntity.isConditionalCommandBlock();
      this.autoActivate = this.blockEntity.isAuto();
      this.updateTrackedOutput();
      this.updateMode();
      this.updateConditionalMode();
      this.updateActivationMode();
      this.doneButton.active = true;
      this.toggleTrackingOutputButton.active = true;
      this.modeButton.active = true;
      this.conditionalModeButton.active = true;
      this.redstoneTriggerButton.active = true;
   }

   public void resize(MinecraftClient client, int width, int height) {
      super.resize(client, width, height);
      this.updateTrackedOutput();
      this.updateMode();
      this.updateConditionalMode();
      this.updateActivationMode();
      this.doneButton.active = true;
      this.toggleTrackingOutputButton.active = true;
      this.modeButton.active = true;
      this.conditionalModeButton.active = true;
      this.redstoneTriggerButton.active = true;
   }

   protected void syncSettingsToServer(CommandBlockExecutor commandExecutor) {
      this.client.getNetworkHandler().sendPacket(new UpdateCommandBlockC2SPacket(new BlockPos(commandExecutor.getPos()), this.consoleCommandTextField.getText(), this.mode, commandExecutor.isTrackingOutput(), this.conditional, this.autoActivate));
   }

   private void updateMode() {
      switch(this.mode) {
      case SEQUENCE:
         this.modeButton.setMessage(new TranslatableText("advMode.mode.sequence"));
         break;
      case AUTO:
         this.modeButton.setMessage(new TranslatableText("advMode.mode.auto"));
         break;
      case REDSTONE:
         this.modeButton.setMessage(new TranslatableText("advMode.mode.redstone"));
      }

   }

   private void cycleType() {
      switch(this.mode) {
      case SEQUENCE:
         this.mode = CommandBlockBlockEntity.Type.AUTO;
         break;
      case AUTO:
         this.mode = CommandBlockBlockEntity.Type.REDSTONE;
         break;
      case REDSTONE:
         this.mode = CommandBlockBlockEntity.Type.SEQUENCE;
      }

   }

   private void updateConditionalMode() {
      if (this.conditional) {
         this.conditionalModeButton.setMessage(new TranslatableText("advMode.mode.conditional"));
      } else {
         this.conditionalModeButton.setMessage(new TranslatableText("advMode.mode.unconditional"));
      }

   }

   private void updateActivationMode() {
      if (this.autoActivate) {
         this.redstoneTriggerButton.setMessage(new TranslatableText("advMode.mode.autoexec.bat"));
      } else {
         this.redstoneTriggerButton.setMessage(new TranslatableText("advMode.mode.redstoneTriggered"));
      }

   }
}
