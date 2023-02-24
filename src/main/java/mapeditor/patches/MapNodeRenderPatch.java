package mapeditor.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.screens.DungeonMapScreen;
import javassist.CtBehavior;

import java.util.ArrayList;

@SpirePatch(clz = DungeonMapScreen.class, method = "updateImage")
public class MapNodeRenderPatch {
    @SpireInsertPatch(locator = Locator.class, localvars = {"node","visibleMapNodes"})
    public static void Insert(DungeonMapScreen __instance, MapRoomNode node, ArrayList<MapRoomNode> visibleMapNodes) {
        if(!node.hasEdges() && MapNodePatches.getNodeCustom(node)) {
            visibleMapNodes.add(node);
        }
    }
    private static class Locator extends SpireInsertLocator {

        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            Matcher.MethodCallMatcher methodCallMatcher = new Matcher.MethodCallMatcher(MapRoomNode.class, "hasEdges");
            return LineFinder.findInOrder(ctBehavior, methodCallMatcher);
        }
    }
}
