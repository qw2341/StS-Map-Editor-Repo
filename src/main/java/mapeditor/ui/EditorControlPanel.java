package mapeditor.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.UIStrings;
import mapeditor.MapEditor;
import mapeditor.helper.MapManipulator;
import mapeditor.helper.MapSaver;

import java.util.ArrayList;

public class EditorControlPanel {
    public ArrayList<Button> buttons;
    public float x;
    public float y;

    private static final float buttonSpacing = 50.0F * Settings.scale;
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(MapEditor.makeID("EditorControlPanel"));
    public static final String[] TEXT = uiStrings.TEXT;

    public EditorControlPanel(float cx, float cy) {
        this.x = cx;
        this.y = cy;
        buttons = new ArrayList<>();
        ArrayList<PowerTip> tips1 = new ArrayList<>();
        tips1.add(new PowerTip(TEXT[1], TEXT[2]));
        cx -= buttonSpacing;
        buttons.add(new Button(TEXT[0], cx, cy, ImageMaster.FILTER_ARROW, tips1, MapSaver::importMap, false));
        ArrayList<PowerTip> tips2 = new ArrayList<>();
        tips2.add(new PowerTip(TEXT[4], TEXT[5]));
        cx += 2 * buttonSpacing;
        buttons.add(new Button(TEXT[3], cx, cy, ImageMaster.FILTER_ARROW, tips2, MapSaver::exportMap, true));
        ArrayList<PowerTip> tips3 = new ArrayList<>();
        tips3.add(new PowerTip(TEXT[7], TEXT[8]));
        cx += buttonSpacing;
        buttons.add(new Button(TEXT[6], cx + buttonSpacing, cy, ImageMaster.PROFILE_DELETE, tips3, () -> {
            MapManipulator.removeAllNode();
            AbstractDungeon.dungeonMapScreen.updateImage();
            MapSaver.addEdit(new MapSaver.MapEditAction());
        }, false));

    }

    public void update() {
        for (Button b : buttons) {
            b.update();
        }
    }

    public void render(SpriteBatch sb) {
        for (Button b : buttons) {
            b.render(sb);
        }
    }
}
