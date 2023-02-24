package mapeditor.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.helpers.EventHelper;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.map.LegendItem;
import javassist.CtBehavior;
import mapeditor.MapEditor;

public class LegendItemPatch {
    @SpirePatch(clz = LegendItem.class, method = "update")
    public static class ItemHover{
        @SpireInsertPatch(locator = Locator.class)
        public static void Insert(LegendItem __instance) {
            if(InputHelper.justClickedLeft) {
                int idx = ReflectionHacks.getPrivate(__instance, LegendItem.class, "index");
                MapEditor.selectedRoomType = indexToRoomType(idx);
            }

        }

        public static MapEditor.RoomType indexToRoomType(int index) {
            switch (index) {
                case 5:
                    return MapEditor.RoomType.ELITE;
                case 4:
                    return MapEditor.RoomType.MONSTER;
                case 3:
                    return MapEditor.RoomType.CAMPFIRE;
                case 2:
                    return MapEditor.RoomType.TREASURE;
                case 1:
                    return MapEditor.RoomType.SHOP;
                case 0:
                default:
                    return MapEditor.RoomType.EVENT;
            }
        }
    }

    private static class Locator extends SpireInsertLocator {

        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            Matcher.MethodCallMatcher methodCallMatcher = new Matcher.MethodCallMatcher(TipHelper.class, "renderGenericTip");
            return LineFinder.findInOrder(ctBehavior, methodCallMatcher);
        }
    }
}
