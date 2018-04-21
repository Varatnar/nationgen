package nationGen.diagnostics;

import java.io.FileNotFoundException;
import java.util.List;

import com.elmokki.Dom3DB;
import com.elmokki.Generic;
import nationGen.NationGen;
import nationGen.entities.Filter;

public class Spellchecker {
    public static void main(String[] args) throws FileNotFoundException {
        NationGen ng = new NationGen();

        Dom3DB spells = new Dom3DB("spells.csv");
        List<String> names = spells.getColumn("name");
        for (String str : ng.spells.keySet()) {
            for (Filter f : ng.spells.get(str)) {
                for (String sp : Generic.getTagValues(f.tags, "spell"))
                    if (!names.contains(sp))
                        System.out.println(sp + " wasn't found.");
            }
        }
    }
}
