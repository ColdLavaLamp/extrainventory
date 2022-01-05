package easton.extrainv.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends HandledScreen {

    @Shadow @Final private RecipeBookWidget recipeBook;
    @Shadow private boolean mouseDown;

    private static final Identifier BONUS_ROWS_TEXTURE = new Identifier("extrainv", "textures/gui/bonus_rows.png");

    @Inject(method = "<init>", at = @At("TAIL"))
    private void fixHeight(PlayerEntity player, CallbackInfo info) {
        this.backgroundHeight += 50;
    }

    @Inject(method = "isClickOutsideBounds", at = @At("RETURN"), cancellable = true)
    private void addRows(double mouseX, double mouseY, int left, int top, int button, CallbackInfoReturnable<Boolean> info) {
        info.setReturnValue( info.getReturnValue() && mouseY >= (double)(top + this.backgroundHeight + 36) );
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void drawBonus(MatrixStack matrices, float delta, int mouseX, int mouseY, CallbackInfo info) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, BONUS_ROWS_TEXTURE);
        int i = this.x;
        int j = this.y + 166;
        this.drawTexture(matrices, i, j, 0, 0, 176, 50);
    }

    public InventoryScreenMixin(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @ModifyArg(method = "init", index = 8, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TexturedButtonWidget;<init>(IIIIIIILnet/minecraft/util/Identifier;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)V"))
    private ButtonWidget.PressAction recipeBookPosResetter(ButtonWidget.PressAction action) {
        return (buttonWidget) -> {
            this.recipeBook.toggleOpen();
            this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);
            ((TexturedButtonWidget)buttonWidget).setPos(this.x + 104, this.height / 2 - 47);
            this.mouseDown = true;
        };
    }

    @ModifyConstant(method = "init", constant = @Constant(intValue = 22, ordinal = 0))
    private int recipeBookPosFix(int og) {
        return 47;
    }

}
