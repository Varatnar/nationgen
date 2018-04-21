package nationGen.entities;

import java.util.ArrayList;
import java.util.List;

import com.elmokki.Generic;
import nationGen.NationGen;
import nationGen.misc.Command;
import nationGen.misc.ItemSet;

public class Race extends Filter {

    public String longsyllables = "";
    public String shortsyllables = "";
    public String namesuffixes = "";
    public List<Command> nationcommands = new ArrayList<>();
    public List<Command> unitcommands = new ArrayList<>();
    public List<Command> specialcommands = new ArrayList<>();
    public List<Pose> poses = new ArrayList<>();
    public List<Pose> spriteGenPoses = new ArrayList<>();

    public List<Theme> themefilters = new ArrayList<>();

    public String visiblename = null;


    public Race(NationGen nationGen) {

        super(nationGen);

        addCommand("#gcost 10");
        addCommand("#ap 12");
        addCommand("#mapmove 16");
        addCommand("#mor 10");
        addCommand("#mr 10");
        addCommand("#hp 10");
        addCommand("#str 10");
        addCommand("#att 10");
        addCommand("#def 10");
        addCommand("#prec 10");
        addCommand("#enc 3");
        addCommand("#size 2");
        addCommand("#maxage 50");
    }


    /**
     * Adds a new command replacing old one of the same type. Just so nations can have both defaults and
     * custom stuff on top of that.
     *
     */
    public void addCommand(String str) {
        if ((str.startsWith("\'") && str.endsWith("\'")) || (str.startsWith("\"") && str.endsWith("\"")))  // do final cleanup on command EA20150604
            str = str.substring(1, str.length() - 1);

        Command c = Command.parseCommand(str);

        for (int i = 0; i < unitcommands.size(); i++)
            if (unitcommands.get(i).command.equals(c.command)) {

                if (c.args.get(0).startsWith("+") || c.args.get(0).startsWith("-") || c.args.get(0).startsWith("*")) {
                    //todo: this is empty ???
                } else {
                    unitcommands.remove(unitcommands.get(i));
                }
            }
        unitcommands.add(c);

    }


    /**
     * Adds a new command replacing old one of the same type. Just so nations can have both defaults and
     * custom stuff on top of that.
     *
     */
    public void addOwnLine(String str) {
        List<String> args = Generic.parseArgs(str);
        List<String> toBeRemoved = new ArrayList<>();

        for (String tag : tags) {
            List<String> args2 = Generic.parseArgs(tag);

            if (args.size() != args2.size() || args.size() < 2 || !args.get(1).equals(args2.get(1)))
                continue;

            boolean ok = true;
            for (int j = 1; j < args.size() - 1; j++) {
                if (args.get(j).startsWith("\'") || args.get(j).startsWith("\""))
                    args.set(j, args.get(j).substring(1));
                if (args.get(j).endsWith("\'") || args.get(j).endsWith("\""))
                    args.set(j, args.get(j).substring(0, args.get(j).length() - 1));

                if (!args.get(j).equals(args2.get(j))) {
                    ok = false;
                    break;
                }

            }

            if (ok) {
                toBeRemoved.add(tag);
            }

        }

        tags.removeAll(toBeRemoved);

        this.handleOwnCommand(str);

    }

    public boolean hasRole(String role) {
        for (Pose p : poses) {
            if (p.roles.contains(role))
                return true;
        }
        return false;
    }


    //todo: inverted boolean method, not sure on what its suppose to represent
    public boolean hasSpecialRole(String role, boolean sacred) {

        String str = "elite";

        //todo: allok ?? what does that mean
        boolean allok = false;

        if (sacred && this.tags.contains("all_troops_sacred"))
            allok = true;
        else if (!sacred && this.tags.contains("all_troops_elite"))
            allok = true;


        if (sacred)
            str = "sacred";
        for (Pose p : poses) {
            for (String role2 : p.roles)
                if (role2.toLowerCase().equals((str + " " + role).toLowerCase()) || (role2.equals(role) && allok) || (role2.equals(role) && p.roles.contains(str)))
                    return true;
        }
        return false;
    }


    @Override
    public void handleOwnCommand(String str) {
        List<String> args = Generic.parseArgs(str);
        if (args.size() == 0)
            return;

        switch (args.get(0)) {
            case "#longsyllables":
                this.longsyllables = args.get(1);
                break;
            case "#shortsyllables":
                this.shortsyllables = args.get(1);
                break;
            case "#suffixes":
                this.namesuffixes = args.get(1);
                break;
            case "#visiblename":
                this.visiblename = args.get(1);
                break;
            case "#nationcommand":
                Command c = Command.parseCommandFromDefinition(args);
                this.nationcommands.add(c);
                break;
            case "#unitcommand":
                args.remove(0);
                this.addCommand(Generic.listToString(args));
                break;
            case "#specialcommand":
                this.specialcommands.add(Command.parseCommandFromDefinition(args));
                break;
            case "#pose": {
                List<Pose> set = nationGen.poses.get(args.get(1));
                if (set == null) {
                    System.out.println("Pose set " + args.get(1) + " was not found.");
                } else {
                    this.poses.addAll(set);
                }
                break;
            }
            case "#spritegenpose": {
                List<Pose> set = nationGen.poses.get(args.get(1));
                if (set == null) {
                    System.out.println("Pose set " + args.get(1) + " was not found.");
                } else {
                    this.spriteGenPoses.addAll(set);
                }
                break;
            }
            default:
                super.handleOwnCommand(str);
                break;
        }

    }


    public ItemSet getItems(String slot, String role) {
        ItemSet items = new ItemSet();
        for (Pose p : poses)
            if (p.roles.contains(role)) {
                if (p.getItems(slot) != null)
                    items.addAll(p.getItems(slot));
            }

        return items;
    }


    public List<Pose> getPoses(String role) {
        List<Pose> poses = new ArrayList<>();
        for (Pose p : this.poses) {
            if (p.roles.contains(role)) {
                poses.add(p);
            }
        }

        return poses;
    }

    @Override
    protected void finish() {
        if (visiblename == null)
            visiblename = name;
    }


    public void handleTheme(Theme t) {
        // Add race commands
        for (String str2 : t.nationEffects) {
            addOwnLine(str2);
        }
        for (String str2 : t.bothNationEffects) {
            addOwnLine(str2);
        }
    }

    public Race getCopy() {
        Race r = new Race(nationGen);
        r.longsyllables = this.longsyllables;
        r.shortsyllables = this.shortsyllables;
        r.namesuffixes = this.namesuffixes;
        r.nationcommands.addAll(this.nationcommands);
        r.unitcommands.addAll(this.unitcommands);
        r.specialcommands.addAll(this.specialcommands);
        r.poses.addAll(this.poses);
        r.spriteGenPoses.addAll(this.spriteGenPoses);
        r.chanceincs.addAll(this.chanceincs);
        r.tags.addAll(this.tags);
        r.visiblename = this.visiblename;
        r.basechance = this.basechance;
        r.commands.addAll(this.commands);
        r.name = this.name;
        r.types.addAll(this.types);
        r.tags.addAll(this.types);
        r.themes.addAll(this.themes);

        return r;
    }


}
