package mapeditor.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.random.Random;
import mapeditor.MapEditor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SpirePatch(clz = AbstractDungeon.class, method = "generateEvent")
public class EventListPatch {
    public static AbstractEvent Postfix(AbstractEvent __result, Random rng) {
        if(__result == null) {
            if(AbstractDungeon.eventList.isEmpty()) {
                try {
                    Method initEvent = CardCrawlGame.dungeon.getClass().getDeclaredMethod("initializeEventList");
                    initEvent.setAccessible(true);
                    initEvent.invoke(CardCrawlGame.dungeon);
                } catch (NoSuchMethodException|InvocationTargetException|IllegalAccessException e) {
                    MapEditor.logger.info("Unable to replenish events!");
                }
            }
            if(AbstractDungeon.shrineList.isEmpty()) {
                try {
                    Method initEvent = CardCrawlGame.dungeon.getClass().getDeclaredMethod("initializeShrineList");
                    initEvent.setAccessible(true);
                    initEvent.invoke(CardCrawlGame.dungeon);
                } catch (NoSuchMethodException|InvocationTargetException|IllegalAccessException e) {
                    MapEditor.logger.info("Unable to replenish shrine events!");
                }
            }
            return AbstractDungeon.getEvent(rng);
        }
        return __result;
    }
}
