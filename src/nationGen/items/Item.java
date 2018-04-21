package nationGen.items;

import java.util.ArrayList;
import java.util.List;

import com.elmokki.Generic;
import nationGen.NationGen;
import nationGen.entities.Drawable;
import nationGen.entities.Filter;
import nationGen.misc.Command;

public class Item extends Drawable {

    public String id = "-1";
    public boolean armor = false;
    public Filter filter = null;


    public ArrayList<ItemDependency> dependencies = new ArrayList<>();
    //public LinkedHashMap<String, String> dependencies = new LinkedHashMap<String, String>();
    //public LinkedHashMap<String, String> typedependencies = new LinkedHashMap<String, String>();

    public List<Command> commands = new ArrayList<Command>();
    public String slot = "";
    public String set = "";


    public Item(NationGen nationGen) {
        super(nationGen);
    }


    public CustomItem getCustomItemCopy() {
        CustomItem item = new CustomItem(nationGen);
        item.sprite = sprite;
        item.mask = mask;
        item.id = id;
        item.armor = armor;
        item.offsetx = offsetx;
        item.offsety = offsety;
        item.dependencies.addAll(dependencies);
        item.commands.addAll(commands);
        item.slot = slot;
        item.set = set;
        item.renderslot = renderslot;
        item.renderprio = renderprio;
        item.name = this.name;
        item.filter = this.filter;
        item.basechance = this.basechance;
        item.tags.addAll(tags);
        return item;
    }

    public Item getCopy() {

        return (Item) this.getCustomItemCopy();
    }

    @Override
    public void handleOwnCommand(String str) {

        List<String> args = Generic.parseArgs(str);

        try {

            switch (args.get(0)) {
                case "#gameid":
                    this.id = args.get(1);
                    break;
                case "#armor":
                    this.armor = true;
                    break;
                case "#addthemeinc":
                    if (this.filter == null) {
                        this.filter = new Filter(nationGen);
                        filter.tags.add("do_not_show_in_descriptions");
                        if (this.name != null)
                            filter.name = "Item " + this.name + " generation effects";
                    }
                    args.remove(0);
                    this.filter.themeincs.add(Generic.listToString(args, " "));
                    break;
                case "#name":
                    if (filter != null)
                        filter.name = "Item " + args.get(1) + " generation effects";
                    super.handleOwnCommand(str);
                    break;
                case "#needs":
                    this.dependencies.add(new ItemDependency(args.get(1), args.get(2), false, false));
                    break;
                case "#needstype":
                    this.dependencies.add(new ItemDependency(args.get(1), args.get(2), true, false));
                    break;
                case "#forceslot":
                    this.dependencies.add(new ItemDependency(args.get(1), args.get(2), false, true));
                    break;
                case "#forceslottype":
                    this.dependencies.add(new ItemDependency(args.get(1), args.get(2), true, true));
                    break;
                case "#command":
                case "#define":
                    this.commands.add(Command.parseCommand(args.get(1)));
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
