package mapeditor.savables;

import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.core.Settings;
import mapeditor.helper.MapSaver;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class SerializableMap implements Serializable {

    public Long seed;
    public HashMap<String, ArrayList<int[]>> edits;
    public static Type typeToken = new TypeToken<SerializableMap>() { }.getType();

    public SerializableMap(Long seed, HashMap<String, ArrayList<MapSaver.MapEditAction>> edits) {
        this.seed = seed;
        this.edits = new HashMap<>();

        for (String key: edits.keySet()) {
            ArrayList<int[]> sEdits = new ArrayList<>();
            for (MapSaver.MapEditAction e : edits.get(key)) {
                sEdits.add(e.serialize());
            }
            this.edits.put(key, sEdits);
        }
    }
}
