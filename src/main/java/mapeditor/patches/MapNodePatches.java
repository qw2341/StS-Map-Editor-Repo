package mapeditor.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.map.MapEdge;
import com.megacrit.cardcrawl.map.MapRoomNode;
import javassist.CtBehavior;
import mapeditor.MapEditor;
import mapeditor.helper.MapManipulator;
import mapeditor.helper.MapSaver;
import mapeditor.helper.NodeLinker;

public class MapNodePatches {
    @SpirePatch(clz = MapRoomNode.class, method = SpirePatch.CLASS)
    public static class CustomNodePatch {
        public static SpireField<Boolean> isCustomNode = new SpireField<>(() -> Boolean.FALSE);
    }

    public static void setNodeCustom(MapRoomNode roomNode, boolean isCustom) {
        CustomNodePatch.isCustomNode.set(roomNode, isCustom);
    }

    public static boolean getNodeCustom(MapRoomNode roomNode) {
        return CustomNodePatch.isCustomNode.get(roomNode);
    }

    @SpirePatch(clz = MapRoomNode.class, method = SpirePatch.CLASS)
    public static class RemoveNodePatch {
        public static SpireField<Boolean> shouldRender = new SpireField<Boolean>(() -> Boolean.TRUE);
    }

    public static void setShouldRender(MapRoomNode node, boolean shouldRender) {
        RemoveNodePatch.shouldRender.set(node, shouldRender);
    }

    public static boolean getShouldRender(MapRoomNode node) {
        return RemoveNodePatch.shouldRender.get(node);
    }

    @SpirePatch(clz = MapRoomNode.class, method = "update")
    public static class RightClickPatch {
        @SpireInsertPatch(locator = UpdateLocator.class)
        public static void Insert(MapRoomNode __instance) {
            if(__instance.hb.hovered && InputHelper.justClickedRight) {
                if(MapEditor.INSTANCE.shiftKey.isPressed()) {
                    MapManipulator.removeNode(__instance);
                    MapSaver.edits.add(new MapSaver.MapEditAction(__instance));
                } else {
//                    MapEditor.logger.info("Edges for node: (" + __instance.x + ", " + __instance.y + ") are : ");
//                    for(MapEdge e : __instance.getEdges()) MapEditor.logger.info("[" + e.srcX+ ", " +e.srcY + " -> "+ e.dstX+ ", " + e.dstY +"]");
                    if (NodeLinker.node1 == null) {
                        NodeLinker.node1 = __instance;
                    } else {
                        if(__instance != NodeLinker.node1) {
                            NodeLinker.link(NodeLinker.node1, __instance);
                            MapSaver.edits.add(new MapSaver.MapEditAction(NodeLinker.node1, __instance));
                            NodeLinker.node1 = __instance;
                        }
                        if(!MapEditor.INSTANCE.ctrlKey.isPressed()) NodeLinker.node1 = null;
                    }
                }

            }
        }
    }
    private static class UpdateLocator extends SpireInsertLocator {

        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            Matcher.FieldAccessMatcher fieldAccessMatcher = new Matcher.FieldAccessMatcher(MapRoomNode.class, "edges");
            return LineFinder.findInOrder(ctBehavior, fieldAccessMatcher);
        }
    }
}
