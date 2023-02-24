package mapeditor.helper;

import basemod.CustomEventRoom;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.*;
import mapeditor.MapEditor;

import java.util.ArrayList;

public class MapManipulator {

    public static void placeNode(MapEditor.RoomType roomType, int x, int y) {
        ArrayList<ArrayList<MapRoomNode>> map = AbstractDungeon.map;
        ArrayList<MapRoomNode> row = map.get(y);
        //shift other rooms
        for (int i = x; i < row.size(); i ++) {
            row.get(i).x ++;
        }
        MapRoomNode roomToAdd = new MapRoomNode(x,y);
        roomToAdd.room = getRoom(roomType);
        row.add(roomToAdd);
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
}
