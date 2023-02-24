package mapeditor.helper;

import basemod.CustomEventRoom;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.*;
import com.megacrit.cardcrawl.screens.DungeonMapScreen;
import mapeditor.MapEditor;
import mapeditor.patches.MapNodePatches;

import java.util.ArrayList;

public class MapManipulator {

    public static void placeNode(MapEditor.RoomType roomType, int x, int y) {
        x -=  MapRoomNode.OFFSET_X;
        y -= DungeonMapScreen.offsetY + 180.0F * Settings.scale;

        ArrayList<ArrayList<MapRoomNode>> map = AbstractDungeon.map;
        int yNum = Math.min(toMapY(y), map.size()-1);
        yNum = Math.max(yNum, 0);
        ArrayList<MapRoomNode> row = map.get(yNum);
        //shift other rooms
//        for (int i = x; i < row.size(); i ++) {
//            row.get(i).x ++;
//        }
        int xNum = row.size();
        MapRoomNode roomToAdd = new MapRoomNode(xNum,yNum);
        roomToAdd.room = getRoom(roomType);
        MapNodePatches.setNodeCustom(roomToAdd, true);

        roomToAdd.offsetX = x - xNum * 128.0f;
        roomToAdd.offsetY = y - yNum * Settings.MAP_DST_Y;
        MapEditor.logger.info("offsetX: " + roomToAdd.offsetX + " offsetY: " + roomToAdd.offsetY + " at (" + x + ", " + y + ")");
        MapEditor.logger.info("Placing node of Type: " + roomType + " at (" + xNum + ", " + yNum + ")");
        row.add(roomToAdd);

        AbstractDungeon.dungeonMapScreen.updateImage();
    }

    public static AbstractRoom getRoom(MapEditor.RoomType roomType) {
        switch (roomType) {
            default:
            case EVENT:
                return new CustomEventRoom();
            case ELITE:
                return new MonsterRoomElite();
            case TREASURE:
                return new TreasureRoom();
            case SHOP:
                return new ShopRoom();
            case MONSTER:
                return new MonsterRoom();
            case CAMPFIRE:
                return new RestRoom();
        }
    }

    public static int toMapY(int screenY) {
        return (int) (screenY / Settings.MAP_DST_Y);
    }

    public static void removeNode(MapRoomNode node) {

    }
}
