package nationGen.GUI;

import java.util.ArrayList;
import java.util.List;

import nationGen.NationGen;

public class ConsoleGUI {


    public ConsoleGUI() {

    }

    public static void main(String[] args) {

        NationGen nationGen = new NationGen();

        List<Integer> seeds = new ArrayList<>();
        seeds.add(-216802392);

        //nationGen.settings.put("era", 2.0);
        nationGen.settings.put("drawPreview", 1.0);
        nationGen.settings.put("debug", 1.0);
        nationGen.generate(seeds);
        //nationGen.generate(10, 403);
    }


}
