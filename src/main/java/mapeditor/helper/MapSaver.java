package mapeditor.helper;

import basemod.abstracts.CustomSavable;
import com.badlogic.gdx.Gdx;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.curses.AscendersBane;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.Exordium;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.saveAndContinue.SaveAndContinue;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import com.megacrit.cardcrawl.saveAndContinue.SaveFileObfuscator;
import com.megacrit.cardcrawl.vfx.GameSavedEffect;
import mapeditor.MapEditor;
import mapeditor.savables.SerializableMap;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MapSaver{
    public static final String OBFUSCATION_KEY = "key";
    private final Type mapEditType;
    private String filePath;
    private File file;
    private static final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();


    public enum ActionType {
        ADD, LINK, REMOVE
    }
    public static class MapEditAction implements Serializable {
        ActionType action = ActionType.ADD;

        MapEditor.RoomType roomType = MapEditor.RoomType.EVENT;

        int pX = 0;
        int pY = 0;
        float pOffX = 0.0f;
        float pOffY = 0.0f;
        int cX = 0;
        int cY = 0;
        float cOffX = 0.0f;
        float cOffY = 0.0f;

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

        public MapEditAction(int[] nums) {
            this.action = ActionType.values()[nums[0]];
            this.roomType = nums[1] == -1 ? null : MapEditor.RoomType.values()[nums[1]];
            this.pX = nums[2];
            this.pY = nums[3];
            this.pOffX = nums[4];
            this.pOffY = nums[5];
            this.cX = nums[6];
            this.cY = nums[7];
            this.cOffX = nums[8];
            this.cOffY = nums[9];
            this.isBoss = !(nums[10] == 0);
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

        public int[] serialize() {
            int[] nums = new int[11];
            nums[0] = this.action.ordinal();
            nums[1] = this.roomType == null ? -1 : this.roomType.ordinal();
            nums[2] = this.pX;
            nums[3] = this.pY;
            nums[4] = (int) this.pOffX;
            nums[5] = (int) this.pOffY;
            nums[6] = this.cX;
            nums[7] = this.cY;
            nums[8] = (int) this.cOffX;
            nums[9] = (int) this.cOffY;
            nums[10] = this.isBoss ? 1 : 0;
            return nums;
        }
    }

    public static HashMap<String,ArrayList<MapEditAction>> edits = new HashMap<>();

    public static boolean isImported = false;
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
        MapEditor.logger.info("Loading Map");
        if(isImported) {
            MapEditor.logger.info("Imported Map detected, loading imported map");
            isImported = false;

            if(edits.get(AbstractDungeon.id) != null) {
                MapEditor.logger.info("Applying Map Changes");
                for (MapEditAction e : edits.get(AbstractDungeon.id)) {
                    e.execute();
                }
            }

            return;
        }
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
        MapEditor.logger.info("Saving Map");
        FileWriter fileWriter = new FileWriter(this.filePath);
        CustomSavable.saveFileGson.toJson(edits, mapEditType, fileWriter);
        fileWriter.flush();
        fileWriter.close();
    }

    public static void clear() {
        MapEditor.logger.info("Clearing Saved Map");
        MapSaver.edits.clear();
        isImported = false;
        try {
            MapEditor.mapSaver.save();
        } catch (IOException e) {
            MapEditor.logger.info("Failed to save map modifications!");
            e.printStackTrace();
        }
        MapEditor.logger.info("Map Cleared");
    }

    public static void addEdit(MapEditAction e) {
        ArrayList<MapEditAction> dungeonEdit = MapSaver.edits.get(AbstractDungeon.id);
        if(dungeonEdit == null) {
            dungeonEdit = new ArrayList<>();
            edits.put(AbstractDungeon.id, dungeonEdit);
        }
        dungeonEdit.add(e);
    }

    public static void exportMap() {
        setClipboardString(getEncodedMap());
    }
    public static void importMap() {
        MapEditor.logger.info("Decoding Map from clipboard");
        SerializableMap sMap = getDecodedMap(getClipboardString());
        if(sMap != null) {
            MapEditor.logger.info("Decoding successful, now importing");
            Settings.seed = sMap.seed;
            AbstractDungeon.closeCurrentScreen();
            AbstractDungeon.generateSeeds();
            resetDungeon();
            edits = new HashMap<>();
            isImported = true;

            for (String key : sMap.edits.keySet()) {
                ArrayList<MapEditAction> editActions = new ArrayList<>();
                for (int[] numArr : sMap.edits.get(key)) {
                    editActions.add(new MapEditAction(numArr));
                }
                edits.put(key, editActions);
            }

        }
    }

    static String getEncodedMap() {
        SerializableMap sMap = new SerializableMap(Settings.seed, edits);
        return SaveFileObfuscator.encode(CustomSavable.saveFileGson.toJson(sMap),OBFUSCATION_KEY);
    }

    static SerializableMap getDecodedMap(String encodedMap) {
        String decodedMap = SaveFileObfuscator.decode(encodedMap, OBFUSCATION_KEY);
        try {
            return CustomSavable.saveFileGson.fromJson(decodedMap, SerializableMap.typeToken);
        } catch (Exception e) {
            e.printStackTrace();
            MapEditor.logger.info("Error occurred while importing Map from clipboard");
        }
        return null;
    }

    public static void setClipboardString(String content) {
        StringSelection selection = new StringSelection(content);
        clipboard.setContents(selection,selection);
    }

    public static String getClipboardString() {
        try {
            return (String) clipboard.getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            MapEditor.logger.info("Failed to import map from Clipboard");
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Used the method from RKey from Isaac Extend Mod
     */
    private static void resetDungeon() {
        if (!AbstractDungeon.player.isDead) {
            AbstractDungeon.resetPlayer();
            AbstractDungeon.player.movePosition(Settings.WIDTH * 0.25F, AbstractDungeon.floorY);
            CardCrawlGame.nextDungeon = "Exordium";
            AbstractDungeon.isDungeonBeaten = true;

            CardCrawlGame.music.fadeOutBGM();
            CardCrawlGame.music.fadeOutTempBGM();
            AbstractDungeon.fadeOut();
            AbstractDungeon.topLevelEffects.clear();
            AbstractDungeon.actionManager.actions.clear();
            AbstractDungeon.effectList.clear();
            AbstractDungeon.effectsQueue.clear();
            AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
            AbstractDungeon.floorNum = 1;
            AbstractDungeon.actNum = 0;
            AbstractDungeon.id = Exordium.ID;
            AbstractDungeon.player.masterDeck.removeCard(AscendersBane.ID);
            if(AbstractDungeon.isScreenUp) AbstractDungeon.closeCurrentScreen();

            if (Loader.isModLoaded("actlikeit")) {
                try {
                    Class<?> cls = Class.forName("actlikeit.savefields.BehindTheScenesActNum");
                    Field field = cls.getDeclaredField("bc");
                    field.setAccessible(true);
                    Object bc = field.get(null);
                    field = cls.getDeclaredField("actNum");
                    field.setAccessible(true);
                    field.set(bc, 0);
                } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
