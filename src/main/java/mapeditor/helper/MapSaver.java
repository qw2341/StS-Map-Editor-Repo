package mapeditor.helper;

import basemod.abstracts.CustomSavable;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import mapeditor.MapEditor;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;

public class MapSaver{
    private final Type mapEditType;
    private String filePath;
    private File file;

    public enum ActionType {
        ADD, LINK, REMOVE
    }
    public static class MapEditAction implements Serializable {
        ActionType action;

        MapEditor.RoomType roomType;
        int x;
        int y;

        MapRoomNode parent;
        MapRoomNode child;

        public MapEditAction(MapEditor.RoomType roomType, int x, int y) {
            this.roomType = roomType;
            this.x = x;
            this.y = y;
            this.action = ActionType.ADD;
        }

        public MapEditAction(MapRoomNode parent, MapRoomNode child) {
            this.action = ActionType.LINK;
            this.parent = parent;
            this.child = child;
        }

        public MapEditAction(MapRoomNode removingNode) {
            this.action = ActionType.REMOVE;
            this.parent = removingNode;
        }

        public void execute() {
            switch (action) {
                case ADD:
                    MapManipulator.placeNode(this.roomType, this.x, this.y);
                    break;
                case LINK:
                    if(this.child != null) NodeLinker.link(this.parent, this.child);
                    else NodeLinker.linkBoss(this.parent);
                    break;
                case REMOVE:
                    MapManipulator.removeNode(this.parent);
                    break;
            }
        }

    }

    public static Queue<MapEditAction> edits = new PriorityQueue<>();

//    @Override
//    public Queue<MapEditAction> onSave() {
//        return edits;
//    }
//
//    @Override
//    public void onLoad(Queue<MapEditAction> mapEditActions) {
//        edits = mapEditActions;
//        Iterator<MapEditAction> editIt = mapEditActions.iterator();
//        while (editIt.hasNext()) {
//            editIt.next().execute();
//        }
//    }

    public MapSaver() throws IOException {
        this.filePath = SpireConfig.makeFilePath("loadoutMod","CardModifications","json");
        this.file = new File(this.filePath);
        this.file.createNewFile();
        this.mapEditType = new TypeToken<Queue<MapEditAction>>() { }.getType();
    }

    public void load() throws IOException {
        Reader reader = Files.newBufferedReader(Paths.get(this.filePath));
        Queue<MapEditAction> editActions = CustomSavable.saveFileGson.fromJson(reader, mapEditType);
        if (editActions != null) {
            edits = editActions;
            Iterator<MapEditAction> editIt = editActions.iterator();
            while (editIt.hasNext()) {
                editIt.next().execute();
            }
        }

        reader.close();
    }

    public void save() throws IOException {
        FileWriter fileWriter = new FileWriter(this.filePath);
        CustomSavable.saveFileGson.toJson(edits, mapEditType, fileWriter);
        fileWriter.flush();
        fileWriter.close();
    }
}
