package mapeditor.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.screens.options.ConfirmPopup;
import mapeditor.MapEditor;
import mapeditor.helper.MapSaver;

@SpirePatch2(clz = ConfirmPopup.class, method = "abandonRunFromMainMenu")
public class AbandonMainMenuPatch {
    @SpirePostfixPatch
    public static void PostFix() {
        MapSaver.clear();
    }
}
