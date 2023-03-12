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

    @SpirePatch2(clz = Legend.class, method = "update")
    public static class LegendUpdatePatch {
        @SpirePostfixPatch
        public static void PostFix() {
            panel.update();
        }
    }
    @SpirePatch2(clz = Legend.class, method = "render", paramtypez = {SpriteBatch.class})
    public static class LegendRenderPatch {
        @SpirePostfixPatch
        public static void PostFix(SpriteBatch sb) {
            panel.render(sb);
        }
    }


}
