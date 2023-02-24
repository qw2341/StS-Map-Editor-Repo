package mapeditor.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.screens.DungeonMapScreen;
import javassist.CtBehavior;
import mapeditor.helper.MapManipulator;

import java.util.ArrayList;



public class RemovedMapNodeRenderPatch {
    @SpirePatch(clz = DungeonMapScreen.class, method = "updateImage")
    public static class RemoveNodes {
        @SpirePostfixPatch
        public static void PostFix(DungeonMapScreen __instance, ArrayList<MapRoomNode> ___visibleMapNodes) {
            ___visibleMapNodes.removeIf(roomNode -> !MapNodePatches.getShouldRender(roomNode));
        }
    }
    @SpirePatch(clz = DungeonMapScreen.class, method = "update")
    public static class UpdatePatch {
        @SpireInsertPatch(locator = Locator.class)
        public static void Insert(DungeonMapScreen __instance) {
           if(MapManipulator.removingNode) {
               __instance.updateImage();
               MapManipulator.removingNode = false;
           }
        }
    }



    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            Matcher.FieldAccessMatcher fieldAccessMatcher = new Matcher.FieldAccessMatcher(AbstractDungeon.class, "isScreenUp");
            return LineFinder.findInOrder(ctBehavior, fieldAccessMatcher);
        }
    }
}
