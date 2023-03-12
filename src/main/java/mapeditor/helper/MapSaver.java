package mapeditor.helper;

import basemod.abstracts.CustomSavable;
import com.badlogic.gdx.Gdx;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import mapeditor.MapEditor;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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

        int pX;
        int pY;
        float pOffX;
        float pOffY;
        int cX;
        int cY;
        float cOffX;
        float cOffY;

        boolean isBoss = false;

        public MapEditAction(MapEditor.RoomType roomType, int x, int y, float pOffX, float pOffY) {
            this.roomType = roomType;
            this.pX = x;
            this.pY = y;
            this.pOffX = pOffX;
            this.pOffY = pOffY;
            this.action = ActionType.ADD;
        }

        public MapEditAction(MapRoomNode parent, MapRoomNode child) {
            this.action = ActionType.LINK;
            this.pX = parent.x;
            this.pY = parent.y;
            this.pOffX = parent.offsetX;
            this.pOffY = parent.offsetY;
            if(child == null) {
                this.isBoss = true;
            } else {
                this.cX = child.x;
                this.cY = child.y;
                this.cOffX = child.offsetX;
                this.cOffY = child.offsetY;
            }

        }

        public MapEditAction(MapRoomNode removingNode) {
            this.action = ActionType.REMOVE;
            this.pX = removingNode.x;
            this.pY = removingNode.y;
        }

        public void execute() {
            MapEditor.logger.info("Executing " + this.toString());
            switch (action) {
                case ADD:
                    MapManipulator.placeNode(this.roomType, pX,pY,pOffX,pOffY);
                    break;
                case LINK:
                    NodeLinker.link(this.pX,this.pY,pOffX,pOffY,cX,cY,cOffX,cOffY, this.isBoss);
                    break;
                case REMOVE:
                    MapManipulator.removeNode(this.pX,this.pY);
                    break;
            }
        }

        @Override
        public String toString() {
            return "[ Action: " + this.action + "; x = "+ this.pX + "; y = " + this.pY + "; ]";
        }
    }

    public static HashMap<String,ArrayList<MapEditAction>> edits = new HashMap<>();

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
        this.filePath = SpireConfig.makeFilePath("MapEditor","MapEdits","json");
        this.file = new File(this.filePath);
        this.file.createNewFile();
        this.mapEditType = new TypeToken<HashMap<String,ArrayList<MapEditAction>>>() { }.getType();
    }

    public void load() throws IOException {
        Reader reader = Files.newBufferedReader(Paths.get(this.filePath));
        HashMap<String,ArrayList<MapEditAction>> editActions = null;
        try {
            editActions = CustomSavable.saveFileGson.fromJson(reader, mapEditType);
        } catch (Exception e){
            e.printStackTrace();
            MapEditor.logger.info("Error occurred while loading saved edits, deleting map modifications");
            Gdx.files.local(this.filePath).delete();
        }

        if (editActions != null) {
            edits = editActions;
            ArrayList<MapEditAction> mapEditActions = editActions.get(AbstractDungeon.id);
            if(mapEditActions == null) {
                edits.put(AbstractDungeon.id, new ArrayList<MapEditAction>());
            } else {
                Iterator<MapEditAction> editIt = mapEditActions.iterator();
                while (editIt.hasNext()) {
                    editIt.next().execute();
                }
            }

        } else {
            edits = new HashMap<>();
        }

        reader.close();
    }

    public void save() throws IOException {
        FileWriter fileWriter = new FileWriter(this.filePath);
        CustomSavable.saveFileGson.toJson(edits, mapEditType, fileWriter);
        fileWriter.flush();
        fileWriter.close();
    }

    public static void addEdit(MapEditAction e) {
        ArrayList<MapEditAction> dungeonEdit = MapSaver.edits.get(AbstractDungeon.id);
        if(dungeonEdit == null) {
            dungeonEdit = new ArrayList<>();
            edits.put(AbstractDungeon.id, dungeonEdit);
        }
        dungeonEdit.add(e);
    }
}
