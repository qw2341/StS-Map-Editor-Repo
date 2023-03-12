package mapeditor.patches;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.map.Legend;
import mapeditor.ui.EditorControlPanel;

public class LegendPatch {
    public static EditorControlPanel panel = new EditorControlPanel(Legend.X, Legend.Y - 400.0F * Settings.yScale);
    public static boolean showPanel = false;

    @SpirePatch2(clz = Legend.class, method = "update", paramtypez = {float.class, boolean.class})
    public static class LegendUpdatePatch {
        @SpirePostfixPatch
        public static void PostFix(float mapAlpha, boolean isMapScreen) {
            if (mapAlpha >= 0.8F && isMapScreen) {
                showPanel = true;
                panel.update();
            } else {
                showPanel = false;
            }
        }
    }
    @SpirePatch2(clz = Legend.class, method = "render", paramtypez = {SpriteBatch.class})
    public static class LegendRenderPatch {
        @SpirePostfixPatch
        public static void PostFix(SpriteBatch sb) {
            if(showPanel) panel.render(sb);
        }
    }


}
