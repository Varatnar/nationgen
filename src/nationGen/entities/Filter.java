package nationGen.entities;

import java.util.ArrayList;
import java.util.List;

import com.elmokki.Generic;
import nationGen.NationGen;
import nationGen.misc.Command;

public class Filter extends Entity {
    public List<Command> commands = new ArrayList<Command>();
    public List<String> chanceincs = new ArrayList<String>();
    public List<String> themeincs = new ArrayList<String>();
    public List<String> types = new ArrayList<String>();
    public double power = 1;

    public Filter(NationGen nationGen) {
        super(nationGen);
    }


    public List<Command> getCommands() {
        return this.commands;
    }

    @Override
    public void handleOwnCommand(String str) {

        List<String> args = Generic.parseArgs(str);

        try {

            switch (args.get(0)) {
                case "#command":
                case "#define":
                    args.remove(0);
                    this.commands.add(Command.parseCommand(Generic.listToString(args, " ")));
                    break;
                case "#themeinc":
                    args.remove(0);
                    this.themeincs.add(Generic.listToString(args, "", "'"));
                    break;
                case "#type":
                case "#category":
                    this.types.add(args.get(1));
                    break;
                case "#chanceinc":
                    args.remove(0);
                    this.chanceincs.add(Generic.listToString(args, "", "'"));
                    break;
                case "#power":
                    this.power = Integer.parseInt(args.get(1));
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
