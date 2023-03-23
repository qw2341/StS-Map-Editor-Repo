package mapeditor.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.*;

import java.util.ArrayList;

@SpirePatches2({@SpirePatch2(clz = Exordium.class, method ="generateExclusions"),@SpirePatch2(clz = TheCity.class, method ="generateExclusions"),@SpirePatch2(clz = TheBeyond.class, method ="generateExclusions"), @SpirePatch2(clz = TheEnding.class, method ="generateExclusions")})
public class EmptyMonsterListFixPatch {
    @SpirePrefixPatch
    public static SpireReturn<ArrayList<String>> Prefix() {
        if(AbstractDungeon.monsterList.isEmpty()) {
            return SpireReturn.Return(new ArrayList<>());
        } else {
            return SpireReturn.Continue();
        }
    }
}
