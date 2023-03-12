package mapeditor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;

import java.util.ArrayList;
import java.util.function.Consumer;

public class Button {
    public interface OnClickActions {
        void onClick();
    }

    public Hitbox hb;
    public float x;
    public float y;
    public String text;
    public float textWidth;
    public Texture texture;
    public boolean flipY;
    public ArrayList<PowerTip> tips;
    public OnClickActions clickActions;

    public Button(String text, float cx, float cy, Texture texture, ArrayList<PowerTip> tips, OnClickActions actions, boolean flipY) {
        this.text = text;
        this.textWidth = FontHelper.getSmartWidth(FontHelper.topPanelInfoFont, text, Float.MAX_VALUE, 0.0F);
        this.hb = new Hitbox(textWidth, 64.0F * Settings.scale);
        this.hb.move(cx, cy);
        this.x = cx;
        this.y = cy;
        this.tips = tips;
        this.texture = texture;
        this.clickActions = actions;
        this.flipY = flipY;
    }

    public void update() {
        this.hb.move(x, y);
        this.hb.update();
        if (this.hb.justHovered) {
            CardCrawlGame.sound.playA("UI_HOVER", -0.3F);
        }
        if(this.hb.hovered) {
            TipHelper.queuePowerTips(this.x - 128.0F * Settings.scale, this.y,tips);
        }

        if (this.hb.hovered && InputHelper.justClickedLeft) {
            this.hb.clickStarted = true;
        }

        if (this.hb.clicked || this.hb.hovered && CInputActionSet.select.isJustPressed()) {
            this.hb.clicked = false;

            CardCrawlGame.sound.playA("UI_CLICK_1", -0.2F);
            this.clickActions.onClick();
        }

    }

    public void render(SpriteBatch sb) {
        hb.render(sb);
        Color color = !this.hb.hovered ? Settings.CREAM_COLOR : Settings.GOLD_COLOR;
        FontHelper.renderFontCentered(sb, FontHelper.topPanelInfoFont, this.text, this.x, this.y - 32.0f, color);
        sb.setColor(color);

        sb.draw(this.texture, this.x - 32.0f, this.y, 16.0F, 16.0F, 64.0F, 64.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, this.texture.getWidth(), this.texture.getHeight(), false, flipY);

    }
}
