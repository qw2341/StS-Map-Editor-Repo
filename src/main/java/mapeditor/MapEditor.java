package mapeditor;

import basemod.BaseMod;
import basemod.ModPanel;
import basemod.interfaces.*;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.EventHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputAction;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.map.DungeonMap;
import com.megacrit.cardcrawl.map.LegendItem;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.screens.DungeonMapScreen;
import com.megacrit.cardcrawl.vfx.MapDot;
import mapeditor.helper.MapManipulator;
import mapeditor.helper.MapSaver;
import mapeditor.helper.NodeLinker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

@SpireInitializer
public class MapEditor implements EditStringsSubscriber, PostInitializeSubscriber, PostUpdateSubscriber, PreUpdateSubscriber, PostRenderSubscriber, StartActSubscriber, PostDeathSubscriber {

    public static final Logger logger = LogManager.getLogger(MapEditor.class.getName());

    private static final String modID ="MapEditor";

    private static final String BADGE_IMAGE = "MapEditorResources/images/Badge.png";

    public static SpireConfig config = null;
    public static Properties defaultSettings = new Properties();

    private static final String MODNAME = "Map Editor";
    private static final String AUTHOR = "JasonW";
    private static final String DESCRIPTION = "Map editor.";

    public static MapEditor INSTANCE;

    public InputAction shiftKey;
    public InputAction ctrlKey;

    ModPanel settingsPanel;

    public static MapSaver mapSaver;

    @Override
    public void receiveStartAct() {
        if(MapSaver.edits.get(AbstractDungeon.id) != null) {
            for(MapSaver.MapEditAction e : MapSaver.edits.get(AbstractDungeon.id)) {
                e.execute();
            }
        }
    }
    @Override
    public void receivePostDeath() {
        MapSaver.clear();
    }


    public enum RoomType {
        EVENT,
        ELITE,
        TREASURE,
        SHOP,
        MONSTER,
        CAMPFIRE
    }

    public static RoomType selectedRoomType = null;

    public MapEditor() {
        logger.info("Subscribe to BaseMod hooks");
        BaseMod.subscribe(this);
        logger.info("Done subscribing");

        try {
            mapSaver = new MapSaver();
        } catch (IOException e) {
            logger.info("Error loading map modifications");
            e.printStackTrace();
        }
    }

    public static void initialize() {
        logger.info("========================= Initializing Map Editor Mod.  =========================");
        INSTANCE = new MapEditor();
        logger.info("========================= /Map Editor Mod Initialized./ =========================");
    }

    public static String makeID(String idText) {
        return getModID() + ":" + idText;
    }

    private static String getModID() {
        return modID;
    }

    @Override
    public void receiveEditStrings() {
        if (!languageSupport().equals("eng"))
            loadLocStrings(languageSupport());
        else
            loadLocStrings("eng");
    }

    public static String languageSupport() {
        switch (Settings.language) {
            case ZHS:
                return "zhs";
//            case ZHT:
//                return "zht";
//            case KOR:
//                return "kor";
//            case JPN:
//                return "jpn";
//            case FRA:
//                return "fra";
//            case RUS:
//                return "rus";
        }
        return "eng";
    }

    private void loadLocStrings(String language) {
        BaseMod.loadCustomStringsFile(UIStrings.class, getModID() + "Resources/localization/" + language + "/UI-Strings.json");
    }

    @Override
    public void receivePostInitialize() {
        shiftKey = new InputAction(Input.Keys.SHIFT_LEFT);
        ctrlKey = new InputAction(Input.Keys.CONTROL_LEFT);
    }
    @Override
    public void receivePreUpdate() {
        if(AbstractDungeon.isPlayerInDungeon()) {
            if(AbstractDungeon.screen == AbstractDungeon.CurrentScreen.MAP) {
                if(selectedRoomType != null && InputHelper.pressedEscape) {
                    selectedRoomType = null;
                    InputHelper.pressedEscape = false;
                }
                if(NodeLinker.node1 != null && InputHelper.pressedEscape) {
                    NodeLinker.node1 = null;
                    InputHelper.pressedEscape = false;
                }
            }
        }
    }

    @Override
    public void receivePostUpdate() {
        if(AbstractDungeon.isPlayerInDungeon()) {
            if(InputHelper.justClickedLeft && AbstractDungeon.screen == AbstractDungeon.CurrentScreen.MAP) {
                if(AbstractDungeon.dungeonMapScreen.map.legend.items.stream().anyMatch(legendItem -> legendItem.hb.hovered)) {
                    //if clicking on the legend, return
                    return;
                }
                //Place down nodes
                if(selectedRoomType != null) {
                    boolean hasEm = shiftKey.isPressed();
                    MapRoomNode node =  MapManipulator.placeNode(selectedRoomType, InputHelper.mX, InputHelper.mY,hasEm);
                    MapSaver.addEdit(new MapSaver.MapEditAction(selectedRoomType, node.x, node.y, node.offsetX, node.offsetY, hasEm));
                    if(!ctrlKey.isPressed())
                        selectedRoomType = null;
                }
            }
        }
    }

    public static Texture getRoomImage(RoomType roomType) {
        switch (roomType) {
            default:
            case EVENT:
                return ImageMaster.MAP_NODE_EVENT;
            case ELITE:
                return ImageMaster.MAP_NODE_ELITE;
            case TREASURE:
                return ImageMaster.MAP_NODE_TREASURE;
            case SHOP:
                return ImageMaster.MAP_NODE_MERCHANT;
            case MONSTER:
                return ImageMaster.MAP_NODE_ENEMY;
            case CAMPFIRE:
                return ImageMaster.MAP_NODE_REST;
        }
    }

    @Override
    public void receivePostRender(SpriteBatch sb) {
        if(AbstractDungeon.isPlayerInDungeon()) {
            if(selectedRoomType != null) {
                sb.draw(getRoomImage(selectedRoomType), InputHelper.mX - 32.0F, InputHelper.mY - 32.0F, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 128, 128, false, false);
            }

            if(NodeLinker.node1 != null) {
                ArrayList<MapDot> dots = NodeLinker.getDots(NodeLinker.node1);
                for (MapDot dot : dots)
                    dot.render(sb);
            }
        }

    }
}
