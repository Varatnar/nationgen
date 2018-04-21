package nationGen.entities;

import java.util.ArrayList;
import java.util.List;

import com.elmokki.Generic;
import nationGen.NationGen;
import nationGen.misc.Command;
import nationGen.nation.Nation;
import nationGen.units.Unit;

public class AbilityTemplate extends Filter {

    private String desc = "";
    private List<String> addTags = new ArrayList<>();

    public AbilityTemplate(NationGen nationGen) {
        super(nationGen);

    }

    @Override
    public void handleOwnCommand(String str) {

        List<String> args = Generic.parseArgs(str);
        try {
            switch (args.get(0)) {
                case "#desc":
                    desc = args.get(1);
                    break;
                case "#addtag":
                    args.remove(0);
                    addTags.add(Generic.listToString(args));
                    break;
                default:
                    super.handleOwnCommand(str);
                    break;
            }

        } catch (IndexOutOfBoundsException e) {
            System.out.println("WARNING: " + str + " has insufficient arguments (" + this.name + ")");
        }
    }

    public void finish() {
        if (desc.equals(""))
            desc = this.name;
    }

    //todo: Unused method ??
    public void apply(Unit u, Nation n) {
        Filter f = new Filter(nationGen);
        f.commands.addAll(this.commands);
        u.tags.addAll(addTags);

        for (String tag : this.tags) {
            List<String> args = Generic.parseArgs(tag);
            if (args.get(0).equals("possiblecommand")) {
                if (n.random.nextDouble() < Double.parseDouble(args.get(2)))
                    f.commands.add(Command.parseCommand(args.get(1)));
            }
        }


        f.name = "TEMPLATE FILTER: " + this.name;
        f.tags.add("description " + desc);

        if (f.commands.size() > 0 || addTags.size() > 0) {
            u.appliedFilters.add(f);
        }
    }

}
