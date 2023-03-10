package mapeditor.helper;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.map.MapEdge;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;
import com.megacrit.cardcrawl.screens.DungeonMapScreen;
import com.megacrit.cardcrawl.vfx.MapDot;
import mapeditor.MapEditor;

import java.util.ArrayList;
import java.util.Objects;

public class NodeLinker {
    public static MapRoomNode node1 = null;

    public static void link(MapRoomNode parent, MapRoomNode child) {
        MapEdge e = new MapEdge(parent.x, parent.y, parent.offsetX, parent.offsetY, child.x, child.y, child.offsetX, child.offsetY, false);
        parent.addEdge(e);
        child.addParent(parent);
    }

    public static void link(int srcX, int srcY, float srcOffsetX, float srcOffsetY, int dstX, int dstY, float dstOffsetX, float dstOffsetY, boolean isBoss) {
        if(isBoss) dstY = AbstractDungeon.map.size() + 2;
        MapEdge e = (isBoss) ? new MapEdge(srcX,srcY,srcOffsetX,srcOffsetY, 3,  dstY, 0, 0, true)
        : new MapEdge(srcX,srcY,srcOffsetX,srcOffsetY,dstX,dstY,dstOffsetX,dstOffsetY,false);
        MapRoomNode parent = getNode(srcX,srcY);
        try{
            parent.addEdge(e);
            if(!isBoss) getNode(dstX,dstY).addParent(parent);
        } catch (NullPointerException nullPointerException) {
            MapEditor.logger.info("Node Obtaining failed!");
            nullPointerException.printStackTrace();
        }

    }

    public static void linkBoss(MapRoomNode parent) {
        int dstY = AbstractDungeon.map.size() + 1;
        MapEdge e = new MapEdge(parent.x, parent.y, parent.offsetX, parent.offsetY, 3,  dstY, 0, 0, true);
        parent.addEdge(e);
    }

    public static ArrayList<MapDot> getDots(MapRoomNode node) {
        ArrayList<MapDot> dots = new ArrayList<>();
        float SPACING = 17.0F * Settings.xScale;
        float START = SPACING / 2.0F;
        float tmpSX = getX(node.x) + node.offsetX;
        float tmpDX = InputHelper.mX;
        float tmpSY = (float)node.y * Settings.MAP_DST_Y + node.offsetY;
        float tmpDY = InputHelper.mY - DungeonMapScreen.offsetY - 180.0F * Settings.scale;
        Vector2 vec2 = (new Vector2(tmpDX, tmpDY)).sub(new Vector2(tmpSX, tmpSY));
        float length = vec2.len();
        float tmpRadius = 20.0F * Settings.scale;

        for(float i = START + tmpRadius; i < length - MapEdge.ICON_SRC_RADIUS; i += SPACING) {
            vec2.clamp(length - i, length - i);
            dots.add(new MapDot(tmpSX + vec2.x, tmpSY + vec2.y, (new Vector2(tmpSX - tmpDX, tmpSY - tmpDY)).nor().angle() + 90.0F, false));
        }
        return dots;
    }

    public static float getX(int x) {
        return (float)x * 128.0F * Settings.xScale + MapRoomNode.OFFSET_X;
    }
    public static float getY(MapRoomNode node) {
        return (float)node.y * Settings.MAP_DST_Y + node.offsetY;
    }

    public static MapRoomNode getNode(int x, int y) {
        if(AbstractDungeon.map == null || AbstractDungeon.map.size() <= y || AbstractDungeon.map.get(y).size() <=x) {
            MapEditor.logger.info("Coord exceeded the map! Coord given: (" + x + ", "+ y + ")");
            return null;
        }
        return AbstractDungeon.map.get(y).get(x);
    }
}
