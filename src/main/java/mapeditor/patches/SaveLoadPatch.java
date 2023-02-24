package mapeditor.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.saveAndContinue.SaveAndContinue;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import javassist.CtBehavior;
import mapeditor.MapEditor;

import java.io.IOException;
import java.util.HashMap;

public class SaveLoadPatch {
    @SpirePatch(
            clz = SaveAndContinue.class,
            method = "save",
            paramtypez = {SaveFile.class}
    )
    public static class SavePatch {
        @SpireInsertPatch(
                locator = SaveLocator.class
        )
        public static void Insert(SaveFile save) {
            try {
                MapEditor.mapSaver.save();
            } catch (IOException e) {
                MapEditor.logger.info("Error occured while saving map modifications!");
                e.printStackTrace();
            }
        }
    }

    private static class SaveLocator extends SpireInsertLocator {
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.MethodCallMatcher("com.google.gson.GsonBuilder", "create");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpirePatch(clz = AbstractDungeon.class, method = "generateMap")
    public static class LoadPatch {
        @SpirePostfixPatch
        public static void PostFix() {
            try {
                MapEditor.mapSaver.load();
            } catch (IOException e) {
                MapEditor.logger.info("Error occured while modding map post generation");
                e.printStackTrace();
            }
        }
    }
}
