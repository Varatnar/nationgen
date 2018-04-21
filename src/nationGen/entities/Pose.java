package nationGen.entities;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.elmokki.Generic;
import nationGen.NationGen;
import nationGen.items.CustomItem;
import nationGen.items.Item;
import nationGen.misc.Command;
import nationGen.misc.ItemSet;
import nationGen.units.Unit;


public class Pose extends Filter {

    private LinkedHashMap<String, ItemSet> items = new LinkedHashMap<>();
    public List<String> roles = new ArrayList<>();
    public String renderOrder = "shadow cloakb mount basesprite legs shirt armor cloakf bonusweapon weapon offhandw hands hair helmet offhanda overlay extra1 extra2 extra3";
    //todo: Unused property below
//    public List<AbilityTemplate> templates = new ArrayList<>();

    public Pose(NationGen nationGen) {
        super(nationGen);
    }

    public Set<String> getListOfSlots() {
        return items.keySet();
    }

    public ItemSet getItems(String slot) {
        if (items.get(slot) == null)
            return null;
        return items.get(slot).getCopy();
    }

    /**
     * Loads item definitions from file.
     */
    private ItemSet loadItems(String file, int offsetx, int offsety, String slot) {

        ItemSet items = new ItemSet();

        items.addAll(Item.readFile(nationGen, file, Item.class));
        for (Item i : items) {
            try {
                i.slot = slot;
                if (i.renderslot.equals(""))
                    i.renderslot = slot;
            } catch (NullPointerException e) {
                System.out.println("WARNING! File " + file + " produced a null item! Make sure there is a #new command for each item!");
            }
        }
        items.slot = slot;


        if (offsetx != 0 || offsety != 0) {
            for (Item i : items) {
                i.setOffsetX(i.getOffsetX() + offsetx);
                i.setOffsetY(i.getOffsetY() + offsety);


            }
        }


        return items;
    }


    //todo: unused method ?
    public boolean compatibleWith(Unit u, List<String> slots, boolean include) {
        for (String slot : u.slotmap.keySet()) {
            if (slots != null && slots.contains(slot) == !include)
                continue;

            Item ui = u.getSlot(slot);

            // Same name and slot
            for (Item i : this.items.get(slot)) {
                if (i.name.equals(ui.name) && i.id.equals(ui.id))
                    return true;
            }

            // Same image and id
            for (Item i : this.items.get(slot)) {
                if (i.sprite.equals(ui.sprite) && i.id.equals(ui.id))
                    return true;
            }
        }
        return false;
    }


    public String toString() {
        String str = this.roles.toString();
        if (this.name != null && !this.name.equals(""))
            str = this.name;

        return str;
    }

    @Override
    public void handleOwnCommand(String str) {
        List<String> args = Generic.parseArgs(str);
        if (args.size() == 0)
            return;

        switch (args.get(0)) {
            case "#role":
                this.roles.add(args.get(1));
                break;
            case "#renderOrder":
                this.renderOrder = args.get(1);
                break;
            case "#command":
                this.commands.add(Command.parseCommand(args.get(1)));
                break;
            case "#load":

                int offsetx = Generic.getNextArgument(args, "offsetx", 0);
                int offsety = Generic.getNextArgument(args, "offsety", 0);

                if (offsety == 0 && offsetx == 0 && args.size() >= 5) {
                    offsetx = Integer.parseInt(args.get(3));
                    offsety = Integer.parseInt(args.get(4));
                }

                if (!args.get(2).startsWith("."))
                    args.set(2, "." + args.get(2));


                ItemSet set;
                try {
                    set = loadItems(args.get(2), offsetx, offsety, args.get(1));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error loading items from " + args.get(2) + " because of " + e.getCause() + ": " + e.getMessage());
                    return;
                }

                for (Item i : set) {

                    if (!Generic.isNumeric(i.id)) {

                        CustomItem citem = nationGen.getCustomItem(i.id);
                        if (!citem.armor)
                            nationGen.weapondb.addToMap(i.id, citem.getHashMap());
                        else {
                            nationGen.armordb.addToMap(i.id, citem.getHashMap());
                        }
                    }
                }

                // Put itemset to it's place
                if (items.get(args.get(1)) == null)
                    this.items.put(args.get(1), set);
                else
                    items.get(args.get(1)).addAll(set);

                break;
            default:
                super.handleOwnCommand(str);
                break;
        }

    }
}
