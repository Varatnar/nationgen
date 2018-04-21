package nationGen.entities;

import java.util.ArrayList;
import java.util.List;

import com.elmokki.Generic;
import nationGen.NationGen;
import nationGen.items.Item;


public class MagicItem extends Filter {

    public MagicItem(NationGen nationGen) {
        super(nationGen);
    }

    public List<String> names = new ArrayList<>();
    // Bellow property is some legacy variable that was unused
//    public Item baseitem;
    public String effect = "-1";
    public boolean always = false;

    @Override
    public void handleOwnCommand(String str) {

        List<String> args = Generic.parseArgs(str);

        try {

            switch (args.get(0)) {
                case "#unitname":
                    names.add(args.get(1));
                    break;
                case "#eff":
                    effect = args.get(1);
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
