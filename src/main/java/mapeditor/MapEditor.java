package mapeditor;

import basemod.BaseMod;
import basemod.ModPanel;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

@SpireInitializer
public class MapEditor implements EditStringsSubscriber, PostInitializeSubscriber {

    public static final Logger logger = LogManager.getLogger(MapEditor.class.getName());

    private static final String modID ="MapEditor";

    private static final String BADGE_IMAGE = "MapEditorResources/images/Badge.png";

    public static SpireConfig config = null;
    public static Properties defaultSettings = new Properties();

    private static final String MODNAME = "Map Editor";
    private static final String AUTHOR = "JasonW";
    private static final String DESCRIPTION = "Map editor.";

    ModPanel settingsPanel;

    public MapEditor() {
        logger.info("Subscribe to BaseMod hooks");
        BaseMod.subscribe(this);
        logger.info("Done subscribing");
    }

    public static void initialize() {
        logger.info("========================= Initializing Map Editor Mod.  =========================");
        MapEditor mapEditor = new MapEditor();
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

    }

    @Override
    public void receivePostInitialize() {

    }
}
