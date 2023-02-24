package mapeditor.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.map.DungeonMap;
import com.megacrit.cardcrawl.map.MapRoomNode;
import javassist.CtBehavior;
import mapeditor.helper.MapSaver;
import mapeditor.helper.NodeLinker;

import java.util.ArrayList;

public class BossNodePatch {
    @SpirePatch(clz = DungeonMap.class, method = "update")
    public static class NodeLinkPatch {
        @SpireInsertPatch(locator = BossUpdateLocator.class)
        public static void Insert(DungeonMap __instance) {
            if(__instance.bossHb.hovered && InputHelper.justClickedRight) {
                if (NodeLinker.node1 != null) {
                    NodeLinker.linkBoss(NodeLinker.node1);
                    MapSaver.edits.add(new MapSaver.MapEditAction(NodeLinker.node1, null));
                    NodeLinker.node1 = null;
                }
            }
        }
    }

    private static class BossUpdateLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            Matcher.MethodCallMatcher methodCallMatcher = new Matcher.MethodCallMatcher(DungeonMap.class, "updateReticle");
            return LineFinder.findInOrder(ctBehavior,methodCallMatcher);
        }
    }
}
