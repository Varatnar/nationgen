package nationGen.items;


import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.elmokki.Generic;
import nationGen.NationGen;
import nationGen.entities.MagicItem;


public class CustomItem extends Item {

    public LinkedHashMap<String, String> values = new LinkedHashMap<>();
    public Item oldItem = null;

    public CustomItem getCopy() {
        CustomItem item = this.getCustomItemCopy();
        item.oldItem = this.oldItem;
        for (String str : values.keySet())
            item.values.put(str, values.get(str));

        return item;
    }

    public MagicItem magicItem = null;

    public CustomItem(NationGen nationGen) {
        super(nationGen);
        this.values.put("rcost", "0");
        this.values.put("def", "0");
    }


    @Override
    public void handleOwnCommand(String str) {
        List<String> args = Generic.parseArgs(str);
        if (args.get(0).equals("#command") && args.size() > 1) {

            List<String> newArgs = Generic.parseArgs(args.get(1), "'");
            String comCommand = newArgs.get(0);
            StringBuilder comArg = new StringBuilder();

            for (int i = 1; i < newArgs.size(); i++) {
                comArg.append(newArgs.get(i)).append(" ");
            }

            comArg = new StringBuilder(comArg.toString().trim());

            if (comCommand.equals("#name")) {
                comArg = new StringBuilder("\"" + comArg + "\"");
            }

            this.values.put(comCommand.substring(1), comArg.toString());
        } else {
            super.handleOwnCommand(str);
        }
    }


    public LinkedHashMap<String, String> getHashMap() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        //todo: add info on what those are exactly
        map.put("id#", id + "");
        map.put("#att", "1");
        map.put("shots", "0");
        map.put("rng", "0");
        map.put("att", "0");
        map.put("def", "0");
        map.put("lgt", "0");
        map.put("dmg", "0");
        map.put("2h", "0");

        for (String str : values.keySet()) {
            String arg = values.get(str);
            if (arg == null)
                continue;

            //todo: all those string should be in an enum (where more doc should be present)
            switch (str) {
                case "blunt":
                    str = "dt_blunt";
                    arg = "1";
                    break;
                case "pierce":
                    str = "dt_pierce";
                    arg = "1";
                    break;
                case "slash":
                    str = "dt_slash";
                    arg = "1";
                    break;
                case "ironarmor":
                    str = "ferrous";
                    arg = "1";
                    break;
                case "secondaryeffectalways":
                    str = "aeff#";
                    break;
                case "secondaryeffect":
                    str = "eff#";
                    break;
                case "twohanded":
                    str = "2h";
                    arg = "1";
                    break;
                case "charge":
                    arg = "Charge";
                    break;
                case "bonus":
                    arg = "Bonus";
                    break;
                case "dt_cap":
                    arg = "Max dmg 1";
                    break;
                case "magic":
                    arg = "Magic";

                    break;
                case "ammo":
                    str = "shots";
                    break;
                case "armorpiercing":
                    str = "ap";
                    arg = "ap";
                    break;
                case "armornegating":
                    str = "an";
                    arg = "an";
                    break;
                case "range":
                    str = "rng";
                    break;
                case "len":
                    str = "lgt";
                    break;
                case "nratt":
                    str = "#att";
                    break;
                case "rcost":
                    str = "res";
                    break;
                case "name":
                    if (this.armor) {
                        str = "armorname";
                        arg = arg.replaceAll("\"", "");
                    } else if (!this.armor) {
                        str = "weapon_name";
                        arg = arg.replaceAll("\"", "");
                    }
                    break;
            }

            map.put(str, arg);
        }

        map.putIfAbsent("2h", "0");

        return map;
    }


    public void write(PrintWriter tw) {

        if (armor)
            tw.println("#newarmor " + id);
        else
            tw.println("#newweapon " + id);

        List<String> lines = new ArrayList<>();

        for (String command : values.keySet()) {
            if (command.equals("name")) {
                tw.println("#name " + values.get("name"));
            }
        }


        for (String command : values.keySet()) {
            String arg = "";

            if (command.equals("name"))
                continue;

            if (values.get(command) != null) {
                arg = " " + values.get(command);
            }


            lines.add("#" + command + arg);

        }

        for (String str : lines) {
            tw.println(str);
        }


        tw.println("#end");
        tw.println();
    }

}
