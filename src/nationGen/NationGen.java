package nationGen;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import com.elmokki.Dom3DB;
import com.elmokki.Drawing;
import com.elmokki.Generic;
import nationGen.entities.Entity;
import nationGen.entities.Filter;
import nationGen.entities.Flag;
import nationGen.entities.MagicItem;
import nationGen.entities.Pose;
import nationGen.entities.Race;
import nationGen.entities.Theme;
import nationGen.items.CustomItem;
import nationGen.items.Item;
import nationGen.magic.MagicPattern;
import nationGen.magic.Spell;
import nationGen.misc.Command;
import nationGen.misc.PreviewGenerator;
import nationGen.misc.ResourceStorage;
import nationGen.misc.Site;
import nationGen.naming.NameGenerator;
import nationGen.naming.NamePart;
import nationGen.naming.NamingHandler;
import nationGen.naming.NationAdvancedSummarizer;
import nationGen.nation.Nation;
import nationGen.restrictions.NationRestriction;
import nationGen.units.ShapeChangeUnit;
import nationGen.units.ShapeShift;
import nationGen.units.Unit;


public class NationGen {
    public static String version = "0.7.0-RC4-PL";
    public static String date = "21rst of April 2018";

    public List<NationRestriction> restrictions = new ArrayList<>();

    public ResourceStorage<MagicPattern> patterns = new ResourceStorage<>(MagicPattern.class, this);
    public ResourceStorage<Pose> poses = new ResourceStorage<>(Pose.class, this);
    public ResourceStorage<Filter> filters = new ResourceStorage<>(Filter.class, this);
    public ResourceStorage<NamePart> mageNames = new ResourceStorage<>(NamePart.class, this);
    public ResourceStorage<Filter> miscDef = new ResourceStorage<>(Filter.class, this);
    public ResourceStorage<Flag> flagParts = new ResourceStorage<>(Flag.class, this);
    public ResourceStorage<MagicItem> magicItems = new ResourceStorage<>(MagicItem.class, this);
    public ResourceStorage<NamePart> miscNames = new ResourceStorage<>(NamePart.class, this);
    public ResourceStorage<Filter> templates = new ResourceStorage<>(Filter.class, this);
    public ResourceStorage<Filter> descriptions = new ResourceStorage<>(Filter.class, this);
    public ResourceStorage<ShapeShift> monsters = new ResourceStorage<>(ShapeShift.class, this);
    public ResourceStorage<Theme> themes = new ResourceStorage<>(Theme.class, this);
    public ResourceStorage<Filter> spells = new ResourceStorage<>(Filter.class, this);

    public List<String> secondShapeMountCommands = new ArrayList<>();
    public List<String> secondShapeNonMountCommands = new ArrayList<>();
    public List<String> secondShapeRacePoseCommands = new ArrayList<>();

    public Dom3DB weaponDB;
    public Dom3DB armorDB;
    public Dom3DB units;
    public Dom3DB sites;
    public Dom3DB nations;

    public Settings settings;


    private List<CustomItem> defaultCustomItems = new ArrayList<>();
    @SuppressWarnings("FieldCanBeLocal")
    private List<Filter> defaultCustomSpells = new ArrayList<>();

    public List<CustomItem> customItems;
    private List<Filter> customSpells = new ArrayList<>();
    private List<ShapeShift> secondShapes = new ArrayList<>();
    public List<Race> races = new ArrayList<>();
    private IdHandler idHandler;

    public List<ShapeChangeUnit> forms = new ArrayList<>();
    private List<CustomItem> chosenCustomItems = new ArrayList<>();
    //todo: unused variable, keeping but commented
//    private List<CustomItem> pickedCustomitems = new ArrayList<>();
    private List<Spell> spellsToWrite = new ArrayList<>();
    private List<Spell> freeSpells = new ArrayList<>();

    public NationGen() {

        System.out.println("Dominions 5 NationGen version " + version + " (" + date + ")");
        System.out.println("------------------------------------------------------------------");

        System.out.print("Loading settings... ");
        settings = new Settings();
        System.out.println("done!");


        // Init bloc, todo: confirm comment please
        try {

            //default lists init

            defaultCustomItems.addAll(Item.readFile(this, "./data/items/customItems.txt", CustomItem.class));
            defaultCustomSpells.addAll(Item.readFile(this, "./data/spells/custom_spells.txt", Filter.class));

            //end default list init

            System.out.print("Loading Larzm42's Dom5 Mod Inspector database... ");
            loadDom3DB();
            System.out.println("done!");
            System.out.print("Loading definitions... ");
            customItems = new ArrayList<>(defaultCustomItems);
            customSpells = new ArrayList<>(defaultCustomSpells);
            patterns.load("./data/magic/magicpatterns.txt");
            poses.load("./data/poses/poses.txt");
            filters.load("./data/filters/filters.txt");
            mageNames.load("./data/names/mageNames/mageNames.txt");
            miscNames.load("./data/names/naming.txt");
            templates.load("./data/templates/templates.txt");
            descriptions.load("./data/descriptions/descriptions.txt");
            themes.load("./data/themes/themes.txt");
            spells.load("./data/spells/spells.txt");
            monsters.load("./data/monsters/monsters.txt");
            loadRaces("./data/races/races.txt");
            secondShapes = Entity.readFile(this, "./data/shapes/secondShapes.txt", ShapeShift.class);
            miscDef.load("./data/misc/miscDef.txt");
            flagParts.load("./data/flags/flagdef.txt");
            magicItems.load("./data/items/magicweapons.txt");
            loadSecondShapeInheritance("/data/shapes/secondshapeinheritance.txt");

            System.out.println("done!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading file " + e.getMessage());
        }

        this.customItems.forEach((CustomItem customItem) ->
        {
            if (customItem.armor) {
                NationGen.this.armorDB.addToMap(customItem.name, customItem.getHashMap());
            } else {
                NationGen.this.weaponDB.addToMap(customItem.name, customItem.getHashMap());
            }
        });
        System.gc();
        //this.writeDebugInfo();
    }

    public int seed = 0;
    public String modname = "";
    private boolean manySeeds = false;

    public void generate(int amount) {
        Random random = new Random();
        generate(amount, random.nextInt(), null);
    }

    public void generate(int amount, int seed) {
        generate(amount, seed, null);
    }

    public void generate(List<Integer> seeds) {
        Random random = new Random();
        generate(1, random.nextInt(), seeds);
    }

    /**
     * Generate a number of random nation given some parameters
     *
     * @param amount Number of nation to generate
     * @param seed   Main mod seed
     * @param seeds  List of potential seed for nations
     */
    private void generate(int amount, int seed, List<Integer> seeds) {
        this.seed = seed;

        Random random = new Random(seed);

        // If there's a list of seeds.
        if (seeds != null && seeds.size() > 0) {
            manySeeds = true;
            amount = seeds.size();
            random = new Random(0);
        }

        // Start
        idHandler = new IdHandler();
        idHandler.loadFile("forbidden_ids.txt");

        if (!manySeeds) {
            System.out.println("Generating " + amount + " nations with seed " + seed + ".");
        } else {
            System.out.println("Generating " + amount + " nations with predefined seeds.");

            if (restrictions.size() > 0) {
                restrictions.clear();
                System.out.println("Ignoring nation restrictions due to predefined seeds.");
            }
        }

        System.out.println("Generating nations...");
        List<Nation> generatedNations = new ArrayList<>();
        Nation newNation;
        int newSeed;

        int count = 0;
        int failedCount = 0;
        int totalFailed = 0;

        while (generatedNations.size() < amount) {
            count++;
            if (!manySeeds) {
                newSeed = random.nextInt();
            } else {
                newSeed = seeds.get(generatedNations.size());
            }

            System.out.print("- Generating nation " + (generatedNations.size() + 1) + "/" + amount + " (seed " + newSeed);

            if (settings.get("debug") == 1.0) {
                System.out.print(" / " + ZonedDateTime.now().toLocalTime().truncatedTo(ChronoUnit.SECONDS));
            }

            System.out.print(")... ");

            newNation = new Nation(this, newSeed, count, restrictions);

            if (!newNation.passed) {
                ++failedCount;
                System.out.println("try " + String.valueOf(failedCount) + ", FAILED RESTRICTION " + newNation.restrictionFailed);
            }

            if (newNation.passed) {
                totalFailed += failedCount;
                failedCount = 0;
                System.out.println("Done!");
            } else {
                continue;
            }

            // Handle loose ends
            newNation.nationid = idHandler.nextNationId();
            this.polishNation(newNation);

            newNation.name = "Nation " + count;

            System.gc();

            generatedNations.add(newNation);
        }

        if (restrictions.size() > 0) {
            System.out.println("Total nations that did not pass restrictions: " + String.valueOf(totalFailed));
        }

        System.out.print("Giving ids");
        for (Nation n : generatedNations) {
            // units
            for (List<Unit> ul : n.unitlists.values()) {
                for (Unit u : ul) {
                    if (!u.invariantMonster) {
                        u.id = idHandler.nextUnitId();
                    }
                    // Else the monster's ID was set in MonsterGen
                }
            }

            for (List<Unit> ul : n.comlists.values()) {
                for (Unit u : ul) {
                    u.id = idHandler.nextUnitId();
                }
            }

            for (Unit u : n.heroes) {
                u.id = idHandler.nextUnitId();
            }

            // sites
            for (Site s : n.sites) {
                s.id = idHandler.nextSiteId();
            }
            System.out.print(".");
        }

        System.out.println(" Done!");
        System.out.print("Naming things");

        NameGenerator nGen = new NameGenerator(this);
        NamingHandler nHandler = new NamingHandler(this);
        for (Nation n : generatedNations) {
            n.name = nGen.generateNationName(n.races.get(0), n);
            n.nationalitysuffix = nGen.getNationalitySuffix(n, n.name);


            // troops
            nHandler.nameTroops(n);

            // sites
            for (Site s : n.sites)
                s.name = nGen.getSiteName(n.random, s.getPath(), s.getSecondaryPath());


            // mages
            nHandler.nameMages(n);

            // priests
            nHandler.namePriests(n);

            // sacreds and elites
            nHandler.nameSacreds(n);

            // Epithet
            nHandler.giveEpithet(n);

            // Unit descriptions
            nHandler.describeNation(n);

            // Summaries
            n.summary.update();

            System.out.print(".");
        }

        // Get mod name if not custom
        if (modname.equals("")) {
            if (generatedNations.size() > 1) {
                modname = nGen.getSiteName(generatedNations.get(0).random, generatedNations.get(0).random.nextInt(8), generatedNations.get(0).random.nextInt(8));
            } else {
                modname = generatedNations.get(0).name;
            }
        }

        System.out.println(" Done!");

        String filename = modname.replaceAll(" ", "_").toLowerCase();
        try {
            this.write(generatedNations, filename);
        } catch (IOException e) {
            System.out.println("Error writing mod: " + e.getMessage());
        }

        System.out.println("------------------------------------------------------------------");
        System.out.println("Finished generating " + amount + " nations to file nationgen_" + filename + ".dm!");

        modname = "";
    }

    /**
     * Loads data from Dom3DB
     */
    private void loadDom3DB() throws Exception {
        units = new Dom3DB("units.csv");
        armorDB = new Dom3DB("armor.csv");
        weaponDB = new Dom3DB("weapon.csv");
        sites = new Dom3DB("sites.csv");
        nations = new Dom3DB("nations.csv");
    }

    /**
     * Handles spells
     */
    private void handleSpells(Nation nation) {
        int id = nation.nationid;

        for (String s : nation.getSpells()) {
            Spell spell = null;

            // check for existing free spell
            for (Spell sp : this.freeSpells) {
                if (sp.name.equals(s)) {
                    spell = sp;
                }
            }
            // create a new spell

            // check for custom spells first
            if (spell == null) {
                for (Filter sf : this.customSpells) {
                    if (sf.name.equals(s)) {
                        spell = new Spell(this);
                        spell.name = s;
                        spell.commands.addAll(sf.commands);
                        break;
                    }
                }
            }
            // copy existing spell
            if (spell == null) {
                spell = new Spell(this);
                spell.name = s;
                spell.commands.add(new Command("#copyspell", "\"" + s + "\""));
                spell.commands.add(new Command("#name", "\"" + s + " \""));
            }

            spell.nationids.add(id);

            // Handle existence in the list of spells with free space
            if (!this.freeSpells.contains(spell)) {
                this.freeSpells.add(spell);
            }

            if (spell.nationids.size() >= settings.get("maxrestrictedperspell")) {
                this.freeSpells.remove(spell);
            }

            // Add to spells to write
            if (!this.spellsToWrite.contains(spell)) {
                this.spellsToWrite.add(spell);
            }
        }
    }

    /**
     * Load races from a file and store them in {@link #races}
     *
     * @param file File to load races from
     * @throws IOException If file could not be accessed
     */
    @SuppressWarnings("SameParameterValue")
    private void loadRaces(String file) throws IOException {

        try (FileInputStream fileInputStream = new FileInputStream(file);
             DataInputStream in = new DataInputStream(fileInputStream);
             BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

            String strLine;

            while ((strLine = br.readLine()) != null) {
                List<String> args = Generic.parseArgs(strLine);
                if (args.isEmpty()) {
                    continue;
                }

                if (args.get(0).equals("#load")) {
                    List<Race> items = new ArrayList<>(Item.readFile(this, args.get(1), Race.class));
                    races.addAll(items);
                }
            }
        }

    }

    /**
     * Debug method
     */
    @SuppressWarnings("unused")
    public void writeDebugInfo() {
        double total = 0;
        for (Race r : races) {
            if (!r.tags.contains("secondary")) {
                total += r.basechance;
            }
        }

        for (Race r : races) {
            if (!r.tags.contains("secondary")) {
                System.out.println(r.name + ": " + (r.basechance / total));
            }
        }
    }

    private void writeDescriptions(List<Nation> nations, String modname) throws IOException {
        NationAdvancedSummarizer nDesc = new NationAdvancedSummarizer(armorDB, weaponDB);
        if (settings.get("advancedDescs") == 1.0) {
            nDesc.writeAdvancedDescriptionFile(nations, modname);
        }
        if (settings.get("basicDescs") == 1.0) {
            nDesc.writeDescriptionFile(nations, modname);
        }
    }

    private void drawPreviews(List<Nation> nations, String dir) throws IOException {
        if (settings.get("drawPreview") == 1) {
            System.out.print("Drawing previews");
            PreviewGenerator pGen = new PreviewGenerator();
            for (Nation n : nations) {
                pGen.savePreview(n, "./mods/" + dir + "/preview_" + n.nationid + "_" + n.name.toLowerCase().replaceAll(" ", "_") + ".png");
                System.out.print(".");
            }
            System.out.println(" Done!");
        }
    }

    /**
     * @param nations List of nation in mod
     * @param modname Name to be given to the mod
     * @throws IOException If an error occur when writing files
     */
    public void write(List<Nation> nations, String modname) throws IOException {
        String dir = "nationgen_" + modname.toLowerCase().replaceAll(" ", "_") + "/"; // nation.name.toLowerCase().replaceAll(" ", "_")
        if (! new File("./mods/" + dir).mkdir()) {
            throw new IOException("Could not create root mod directory !");
        }

        FileWriter fstream = new FileWriter("./mods/nationgen_" + modname.toLowerCase().replaceAll(" ", "_") + ".dm");
        PrintWriter tw = new PrintWriter(fstream, true);

        // Descriptions
        writeDescriptions(nations, modname);

        // Description!
        tw.println("-- NationGen - " + modname);
        tw.println("-----------------------------------");

        tw.println("-- Generated with version " + version + ".");
        tw.println("-- Generation setting code: " + settings.getSettingInteger());

        if (!manySeeds) {
            tw.println("-- Nation seeds generated with seed " + this.seed + ".");
        } else {
            tw.println("-- Nation seeds specified by user.");
        }

        for (Nation n : nations) {
            tw.println("-- Nation " + n.nationid + ": " + n.name + " generated with seed " + n.seed);
        }
        tw.println("-----------------------------------");
        tw.println();

        // Actual mod definition
        tw.println("#modname \"NationGen - " + this.modname + "\"");
        tw.println("#description \"A NationGen generated nation!\"");

        // Banner!
        generateBanner(nations.get(0).colors[0], this.modname, dir + "/banner.tga", nations.get(0).flag);
        tw.println("#icon \"" + dir + "banner.tga\"");
        tw.println("");

        // Write items!
        // This is a relic from Dom3 version, but oh well.
        System.out.print("Writing items and spells");
        this.writeCustomItems(tw);
        this.writeSpells(tw);

        for (Nation ignored : nations) {
            System.out.print(".");
        }

        System.out.println(" Done!");

        // Write units!
        System.out.print("Writing units");
        for (Nation nation : nations) {

            if (! new File("./mods/" + dir + "/" + nation.nationid + "-" + nation.name.toLowerCase().replaceAll(" ", "_") + "/").mkdir()) {
                throw new IOException("Could not create nation mod directory !");
            }

            // Unit definitions
            nation.writeUnits(tw, dir + "/" + nation.nationid + "-" + nation.name.toLowerCase().replaceAll(" ", "_") + "/");
            System.out.print(".");
        }
        System.out.println(" Done!");

        // Write sites!
        System.out.print("Writing sites");

        for (Nation nation : nations) {
            // Site definitions
            nation.writeSites(tw);
            System.out.print(".");
        }
        System.out.println(" Done!");

        // Write nation definitions!
        System.out.print("Writing nations");
        for (Nation nation : nations) {
            // Flag
            Drawing.writeTGA(nation.flag, "mods/" + dir + "/" + nation.nationid + "-" + nation.name.toLowerCase().replaceAll(" ", "_") + "/flag.tga");

            // Nation definitions
            nation.write(tw, dir + "/" + nation.nationid + "-" + nation.name.toLowerCase().replaceAll(" ", "_") + "/");
            System.out.print(".");
        }
        System.out.println(" Done!");

        // Draw previews
        drawPreviews(nations, dir);

        if (settings.get("hidevanillanations") == 1) {
            hideVanillaNations(tw, nations.size());
        }

        tw.flush();
        tw.close();
        fstream.close();

        // Displays mage names
        /*
        System.out.println();
        for(Nation n : nations)
        {
            List<Unit> mages = n.generateComList("mage");
            List<String> mnames = new ArrayList<String>();
            for(Unit u : mages)
                    mnames.add(u.name.toString());
            System.out.println("* " + Generic.listToString(mnames, ","));
        }
        System.out.println();
       */
    }

    private void hideVanillaNations(PrintWriter tw, int nationCount) {
        System.out.print("Hiding vanilla nations... ");
        tw.println("-- Hiding vanilla nations");
        tw.println("-----------------------------------");

        if (nationCount > 1) {
            tw.println("#disableoldnations");
            tw.println();
            System.out.println(" Done!");
        } else {
            System.out.println("Unable to hide vanilla nations with only one random nation!");
        }
    }

    private void writeSpells(PrintWriter tw) {

        if (spellsToWrite.isEmpty()) {
            return;
        }

        tw.println("--- Spells:");
        for (Spell s : this.spellsToWrite) {
            tw.println("#newspell");
            for (Command c : s.commands) {
                tw.println(c);
            }
            for (int id : s.nationids) {
                tw.println("#restricted " + id);
            }
            tw.println("#end");
            tw.println();
        }
    }

    private void writeCustomItems(PrintWriter tw) {
        if (chosenCustomItems.isEmpty()) {
            return;
        }

        tw.println("--- Generic custom items:");
        for (CustomItem ci : this.chosenCustomItems) {
            ci.write(tw);
            //tw.println("");
        }
    }

    /**
     * Retrieve a {@link CustomItem} based on a name.
     *
     * @param name Name of potential custom item
     * @return The {@link CustomItem} associated with that name (null if none found)
     */
    public CustomItem getCustomItem(String name) {
        for (CustomItem customItem : this.customItems) {
            if (customItem.name.equals(name) && !this.chosenCustomItems.contains(customItem)) {
                return customItem;
            }
        }
        return null;
    }

    public String getCustomItemId(String name) {

        for (CustomItem customItem : this.chosenCustomItems) {
            if (customItem.name.equals(name)) {
                return customItem.id;
            }
        }

        CustomItem cItem = null;

        for (CustomItem customItem : this.customItems) {
            if (customItem.name.equals(name) && !chosenCustomItems.contains(customItem)) {
                cItem = customItem.getCopy();
                break;
            }
        }

        if (cItem == null) {
            System.out.println("WARNING: No custom item named " + name + " was found!");
            return "-1";
        }

        if (idHandler != null) {
            if (cItem.armor) {
                cItem.id = idHandler.nextArmorId() + "";
            } else {
                cItem.id = idHandler.nextWeaponId() + "";
            }
        } else {
            System.out.println("ERROR: idHandler was not initialized!");
            cItem.id = "-1";
        }

        // -521978361
        // Check references!
        for (String str : cItem.values.keySet()) {
            if (str.equals("secondaryeffect") || str.equals("secondaryeffectalways")) {
                String customItemSecondaryEffect = cItem.values.get(str);
                boolean isNumeric = customItemSecondaryEffect.chars().allMatch(Character::isDigit);
                if (isNumeric) {
                    //todo: this parseInt does nothing ...
                    Integer.parseInt(customItemSecondaryEffect);
                } else {
                    String id;
                    id = getCustomItemId(customItemSecondaryEffect);
                    cItem.values.put(str, id);
                }
            }
        }

        this.chosenCustomItems.add(cItem);
//        this.customItems.remove(cItem);

        if (!cItem.armor) {
            weaponDB.addToMap(cItem.id, cItem.getHashMap());
        } else {
            armorDB.addToMap(cItem.id, cItem.getHashMap());
        }

        return cItem.id;
    }

    private boolean hasShapeShift(String id) {
        int realId = -1;
        if (id.isEmpty()) {
            return false;
        } else {
            boolean isNumeric = id.chars().allMatch(Character::isDigit);
            if (isNumeric) {
                realId = Integer.parseInt(id);
            }
        }

        for (ShapeChangeUnit su : this.forms) {
            if (su.id == realId) {
                return true;
            }
        }
        return false;
    }

    private void polishNation(Nation n) {
        n.finalizeUnits();
        handleShapeShifts(n);
        handleSpells(n); //before : handleSpells(n.spells, n)
    }

    private void handleShapeShifts(Nation n) {

        List<Unit> shapeShiftUnits = n.generateUnitList();
        shapeShiftUnits.addAll(n.heroes);
        List<ShapeChangeUnit> sul = new ArrayList<>();

        for (Unit u : shapeShiftUnits) {
            for (Command c : u.commands) {
                if (c.command.contains("shape") && !hasShapeShift(c.args.get(0))) {
                    if ((c.command.equals("#firstshape") && u.tags.contains("montagunit"))) {
                        handleMonTag(c, u, shapeShiftUnits);
                    } else {
                        handleShapeShift(c, u);
                    }
                } else if (c.command.equals("#montag")) {
                    handleMonTag(c, u, shapeShiftUnits);
                }
            }
        }

        for (ShapeChangeUnit su : forms) {
            if (shapeShiftUnits.contains(su.otherForm)) {
                sul.add(su);
            }
        }

        for (ShapeChangeUnit su : sul) {
            su.polish(this, n);

            // Replace command
            for (Command c : su.thisForm.commands) {
                // Weapons
                if (c.command.equals("#weapon")) {
                    String realarg = c.args.get(0);
                    if (realarg.contains(" ")) {
                        realarg = realarg.split(" ")[0];
                    }

                    if (realarg.isEmpty()) {
                        c.args.set(0, getCustomItemId(c.args.get(0)) + "");
                    } else {
                        boolean isNumeric = realarg.chars().allMatch(Character::isDigit);
                        if (isNumeric) {
                            //todo: again unused parseInt ...
                            Integer.parseInt(realarg);
                        }
                    }
                }
            }
        }
    }

    private HashMap<String, Integer> monTagMap = new HashMap<>();

    //todo: monTag -> monster tag ????
    //todo: Unit and units not used .. what for ?
    @SuppressWarnings("unused")
    private void handleMonTag(Command command, Unit u, List<Unit> units) {
        Integer monTag = monTagMap.get(command.args.get(0));
        if (monTag == null) {
            monTag = idHandler.nextMontagId();
            monTagMap.put(command.args.get(0), monTag);
            // System.out.println("Added "  + montag + " for " + c.args.get(0));
        }

        if (command.command.equals("#firstshape")) {
            command.args.set(0, "-" + monTag);
        } else if (command.command.equals("#montag")) {
            command.args.set(0, "" + monTag);
        }
    }

    private void handleShapeShift(Command c, Unit u) {
        ShapeShift shift;
        shift = null;

        for (ShapeShift s : secondShapes) {
            if (s.name.equals(c.args.get(0))) {
                shift = s;
                break;
            }
        }

        if (shift == null) {
            System.out.println("Shapeshift named " + c.args.get(0) + " could not be found.");
            return;
        }
        ShapeChangeUnit su = new ShapeChangeUnit(this, u.race, u.pose, u, shift);

        su.id = idHandler.nextUnitId();

        switch (c.command) {
            case "#shapechange":
                su.shiftcommand = "#shapechange";
                break;
            case "#secondshape":
                su.shiftcommand = "#firstshape";
                break;
            case "#firstshape":
                su.shiftcommand = "#secondshape";
                break;
            case "#secondtmpshape":
                su.shiftcommand = "";
                break;
            case "#landshape":
                su.shiftcommand = "#watershape";
                break;
            case "#watershape":
                su.shiftcommand = "#landshape";
                break;
            case "#forestshape":
                su.shiftcommand = "#plainshape";
                break;
            case "#plainshape":
                su.shiftcommand = "#forestshape";
                break;
            default:
                break;
        }

        c.args.set(0, "" + su.id);
        forms.add(su);
    }

    /**
     * Loads the list of commands that second shapes should inherit from the primary shape
     */
    @SuppressWarnings({"SameParameterValue", "UnusedReturnValue"})
    private int loadSecondShapeInheritance(String filename) {
        int amount = 0;

        try (Scanner file = new Scanner(new FileInputStream(System.getProperty("user.dir") + "/" + filename))) {

            while (file.hasNextLine()) {
                String line = file.nextLine();
                if (line.startsWith("-")) {
                    continue;
                }

                List<String> args = Generic.parseArgs(line);
                if (args.isEmpty()) {
                    continue;
                }

                switch (args.get(0)) {
                    case "all":
                        secondShapeMountCommands.add(args.get(1));
                        secondShapeNonMountCommands.add(args.get(1));
                        amount++;
                        break;
                    case "mount":
                        secondShapeMountCommands.add(args.get(1));
                        amount++;
                        break;
                    case "nonmount":
                        secondShapeNonMountCommands.add(args.get(1));
                        amount++;
                        break;
                    case "racepose":
                        secondShapeRacePoseCommands.add(args.get(1));
                        amount++;
                        break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return 0;
        }

        return amount;
    }

    @SuppressWarnings("unused")
    private static void generateBanner(Color c, String name, String output, BufferedImage flag) throws IOException {
        BufferedImage combined = new BufferedImage(256, 64, BufferedImage.TYPE_INT_RGB);
        Graphics g = combined.getGraphics();

        g.setColor(new Color(0, 0, 0));
        g.fillRect(0, 0, 256, 64);
        g.drawImage(flag, 0, -4, 64, 64, null);

        g.setColor(Color.DARK_GRAY);

        Font f = g.getFont();
        Font d = f.deriveFont(18f);
        f = f.deriveFont(24f);
        g.setFont(d);

        g.drawString("NationGen " + version + ":", 64, 18);

        g.setFont(f);
        g.drawString(name, 64, 48);

        Drawing.writeTGA(combined, "./mods/" + output);
    }

    /**
     * Copies any poses from each race's spriteGenPoses list into its poses list
     */
    public void setSpriteGenPoses() {
        for (Race race : this.races) {
            race.poses.addAll(race.spriteGenPoses);
        }
    }

    /**
     * Attempt to reset every property that might change
     */
    public void resetToDefault() {
        this.settings = null;
        this.restrictions = new ArrayList<>();
        customItems = new ArrayList<>(defaultCustomItems);
        customSpells = new ArrayList<>(defaultCustomSpells);
    }
}