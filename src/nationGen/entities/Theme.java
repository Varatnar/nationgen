package nationGen.entities;

import java.util.ArrayList;
import java.util.List;

import com.elmokki.Generic;
import nationGen.NationGen;

/**
 * Class for handling national theme effects
 */
public class Theme extends Filter {

    public List<String> nationEffects = new ArrayList<>();
    public List<String> secondaryNationEffects = new ArrayList<>();
    public List<String> bothNationEffects = new ArrayList<>();

    public Theme(NationGen nationGen) {
        super(nationGen);
    }

    @Override
    public void handleOwnCommand(String str) {

        List<String> args = Generic.parseArgs(str);

        try {

            switch (args.get(0)) {
                case "#racedefinition":
                    args.remove(0);
                    this.nationEffects.add(Generic.listToString(args));
                    break;
                case "#secondaryracedefinition":
                    args.remove(0);
                    this.secondaryNationEffects.add(Generic.listToString(args));
                    break;
                case "#bothracedefinition":
                    args.remove(0);
                    this.bothNationEffects.add(Generic.listToString(args));
                    break;
                default:
                    super.handleOwnCommand(str);
                    break;
            }

        } catch (IndexOutOfBoundsException e) {
            System.out.println("WARNING: " + str + " has insufficient arguments (" + this.name + ")");
        }
    }
}
