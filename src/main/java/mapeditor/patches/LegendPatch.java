package mapeditor.patches;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.map.Legend;
import mapeditor.ui.EditorControlPanel;

import java.util.ArrayList;

public class LegendPatch {
    public static EditorControlPanel panel = new EditorControlPanel(Legend.X, Legend.Y - 400.0F * Settings.yScale);
    public static boolean showPanel = false;
    public static Hitbox hb = new Hitbox(Legend.X + 128, Legend.Y + 200, 64 * Settings.scale, 64 * Settings.scale);
    public static ArrayList<PowerTip> tips;
    static {
        tips = new ArrayList<>();
        tips.add(new PowerTip(EditorControlPanel.TEXT[9], EditorControlPanel.TEXT[10]));
    }

    @SpirePatch2(clz = Legend.class, method = "update", paramtypez = {float.class, boolean.class})
    public static class LegendUpdatePatch {
        @SpirePostfixPatch
        public static void PostFix(Legend __instance,float mapAlpha, boolean isMapScreen) {
            if (mapAlpha >= 0.8F && isMapScreen) {
                showPanel = true;
                panel.update();
                hb.update();

                if(hb.hovered) {
                    TipHelper.queuePowerTips(Legend.X - 128,Legend.Y - 200, tips);
                }
            } else {
                showPanel = false;
            }
        }
    }
    @SpirePatch2(clz = Legend.class, method = "render", paramtypez = {SpriteBatch.class})
    public static class LegendRenderPatch {
        @SpirePostfixPatch
        public static void PostFix(SpriteBatch sb) {
            hb.render(sb);
            if(showPanel) {
                panel.render(sb);
                Texture qMark = ImageMaster.RUN_HISTORY_MAP_ICON_EVENT;
                sb.draw(qMark, hb.x, hb.y, 16.0F, 16.0F, 64.0F, 64.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, qMark.getWidth(), qMark.getHeight(), false, false);
            }
        }
    }


}
