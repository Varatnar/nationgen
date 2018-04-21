package nationGen.GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import com.elmokki.Generic;
import nationGen.NationGen;
import nationGen.Settings;

public class GUI extends JFrame implements ActionListener, ItemListener, ChangeListener {
    private static final long serialVersionUID = 1L;

    private JTextPane textPane = new JTextPane();
    private JProgressBar progress = new JProgressBar(0, 100);
    private JButton startButton;
    private JTabbedPane tabs = new JTabbedPane();

    private JTextField settingText = new JTextField("0");
    private JTextArea amount = new JTextArea("1");
    private JTextArea modname = new JTextArea("Random");
    private JTextArea seed = new JTextArea("Random");
    private JCheckBox seedRandom = new JCheckBox("Random");
    private JCheckBox modNameRandom = new JCheckBox("Random");
    private JTextArea seeds = new JTextArea("1337, 715517, 80085");
    private JCheckBox advDesc = new JCheckBox("Write advanced descriptions");
    private JCheckBox basicDesc = new JCheckBox("Write basic descriptions");
    private JCheckBox preview = new JCheckBox("Draw sprite review image");
    private List<JCheckBox> optionChecks = new ArrayList<>();
    private Settings settings = new Settings();
    private JCheckBox seedCheckBox = new JCheckBox("Use predefined nation seeds (separate by line change and/or comma)");
    private RestrictionPane rPanel;
    private JCheckBox hideVanillaNations = new JCheckBox("Hide vanilla nations");
    private JSlider eraSlider = new JSlider(JSlider.HORIZONTAL, 1, 3, 2);
    private JSlider sacredPowerSlider = new JSlider(JSlider.HORIZONTAL, 1, 3, 1);

    private NationGen n = null;

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("-commandline")) {
            new CommandLine(args);
        } else {
            SwingUtilities.invokeLater(() -> {
                GUI g = new GUI();
                g.setVisible(true);
            });
        }
    }

    private void initGUI() {
        optionChecks.add(advDesc);
        optionChecks.add(basicDesc);
        optionChecks.add(preview);
        optionChecks.add(seedCheckBox);
        optionChecks.add(hideVanillaNations);

        seedRandom.setSelected(true);
        modNameRandom.setSelected(true);
        seed.setEnabled(false);
        modname.setEnabled(false);

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JPanel options = new JPanel(new GridLayout(2, 2));
        JPanel advoptions = new JPanel(new GridLayout(8, 1));

        // Restrictions need nationgen;
        n = new NationGen();
        n.settings = settings;

        rPanel = new RestrictionPane(n);

        // Main
        tabs.addTab("Main", panel);

        startButton = new JButton("Start!");
        startButton.addActionListener(this);
        seedRandom.addItemListener(this);
        modNameRandom.addItemListener(this);
        advDesc.addItemListener(this);
        preview.addItemListener(this);
        basicDesc.addItemListener(this);

        startButton.setPreferredSize(new Dimension(100, 50));

        textPane.setPreferredSize(new Dimension(600, 300));
        textPane.setBorder(BorderFactory.createLineBorder(Color.black));
        JScrollPane sp = new JScrollPane(textPane);

        progress.setPreferredSize(new Dimension(-1, 22));
        progress.setStringPainted(true);

        redirectSystemStreams();

        JPanel east = new JPanel(new GridLayout(12, 1));
        JPanel ncount = new JPanel(new GridLayout(1, 3));
        ncount.add(new JLabel("Nation amount"));
        amount.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));
        ncount.add(amount);
        ncount.add(new JLabel(""));

        JPanel seedpanel = new JPanel(new GridLayout(1, 3));
        seedpanel.add(new JLabel("Seed"));
        seed.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));
        seedpanel.add(seed);
        seedpanel.add(seedRandom);

        JPanel modnamepanel = new JPanel(new GridLayout(1, 3));
        modnamepanel.add(new JLabel("Mod name"));
        modname.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));
        modnamepanel.add(modname);
        modnamepanel.add(modNameRandom);

        east.add(ncount);
        east.add(seedpanel);
        east.add(modnamepanel);
        east.add(startButton);

        panel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        //panel.add(progress, BorderLayout.SOUTH);
        panel.add(sp, BorderLayout.WEST);
        panel.add(east, BorderLayout.CENTER);

        // Options
        tabs.addTab("Options", options);

        // Descriptions
        JPanel descs = new JPanel(new GridLayout(2, 1));
        descs.add(advDesc);
        descs.add(basicDesc);
        descs.add(preview);
        descs.add(hideVanillaNations);
        this.hideVanillaNations.addItemListener(this);

        options.add(descs, BorderLayout.NORTH);

        // Seeds
        JPanel predefinedSeeds = new JPanel(new BorderLayout(5, 5));
        seedCheckBox.addItemListener(this);
        this.seeds.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));
        predefinedSeeds.add(seedCheckBox, BorderLayout.NORTH);
        predefinedSeeds.add(this.seeds, BorderLayout.CENTER);
        this.seeds.setEnabled(false);
        options.add(predefinedSeeds);

        // Advanced options
        tabs.addTab("Advanced options", advoptions);
        advoptions.add(new JLabel("WARNING! Changing these options changes the setting code. Seeds produce same nations only under the same setting code and program version."));

        JPanel scodep = new JPanel(new GridLayout(1, 2));
        settingText.setText("" + settings.getSettingInteger());
        settingText.addActionListener(this);

        settingText.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                handleUpdate();
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                handleUpdate();
            }

            private void handleUpdate() {
                // Change color when non-numeric settingText
                if (Generic.isNumeric(settingText.getText())) {
                    settingText.setForeground(Color.BLACK);
                }
                if (!Generic.isNumeric(settingText.getText())) {
                    settingText.setForeground(Color.RED);
                }
                if (settingText.getText().length() > 0 && Generic.isNumeric(settingText.getText())) {
                    int i = Integer.parseInt(settingText.getText());
                    settings.setSettingInteger(i);
                    updateAdvancedSettings();
                }
            }
        });

        scodep.add(new JLabel("Setting code:"));
        scodep.add(this.settingText);
        advoptions.add(scodep);

        // Era
        JPanel era = new JPanel(new GridLayout(1, 2));
        eraSlider.addChangeListener(this);
        Hashtable<Integer, JLabel> eraLabelTable = new Hashtable<>();
        eraLabelTable.put(1, new JLabel("Early"));
        eraLabelTable.put(2, new JLabel("Middle"));
        eraLabelTable.put(3, new JLabel("Late"));
        eraSlider.setLabelTable(eraLabelTable);
        eraSlider.setPaintLabels(true);

        era.add(new JLabel("Era:"));
        era.add(eraSlider);

        advoptions.add(era);

        // Sacred power
        JPanel sacredPower = new JPanel(new GridLayout(1, 2));
        sacredPowerSlider.addChangeListener(this);
        Hashtable<Integer, JLabel> sacredPowerLabelTable = new Hashtable<>();
        sacredPowerLabelTable.put(1, new JLabel("Normal"));
        sacredPowerLabelTable.put(2, new JLabel("High"));
        sacredPowerLabelTable.put(3, new JLabel("Batshit Insane"));
        sacredPowerSlider.setLabelTable(sacredPowerLabelTable);
        sacredPowerSlider.setPaintLabels(true);

        sacredPower.add(new JLabel("Sacred Power:"));
        sacredPower.add(sacredPowerSlider);

        advoptions.add(sacredPower);

        // Restrictions
        tabs.addTab("Nation restrictions", rPanel);
        add(tabs);

        pack();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        System.out.println("Dominions 5 NationGen version " + NationGen.version + " (" + NationGen.date + ")");
        System.out.println("------------------------------------------------------------------");
    }


    private void updateAdvancedSettings() {
        this.eraSlider.setValue(settings.get("era").intValue());
        this.sacredPowerSlider.setValue(settings.get("sacredpower").intValue());

    }

    boolean hasRun = false;

    public GUI() {
        setTitle("NationGen GUI");
        this.setPreferredSize(new Dimension(1000, 450));
        this.setResizable(true);
        initGUI();

        if (this.settings.get("drawPreview") == 1.0) {
            preview.setSelected(true);
        }
        if (this.settings.get("advancedDescs") == 1.0) {
            advDesc.setSelected(true);
        }
        if (this.settings.get("basicDescs") == 1.0) {
            basicDesc.setSelected(true);
        }
        if (this.settings.get("hidevanillanations") == 1.0) {
            hideVanillaNations.setSelected(true);
        }
        this.eraSlider.setValue((int) Math.round(this.settings.get("era")));
    }

    private void updateTextPane(final String text) {
        SwingUtilities.invokeLater(() ->
        {
            Document doc = textPane.getDocument();
            try {
                doc.insertString(doc.getLength(), text, null);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
            textPane.setCaretPosition(doc.getLength() - 1);
        });
    }

    private List<Integer> parseSeeds() {
        List<Integer> l = new ArrayList<>();
        String text = seeds.getText();

        String[] parts = text.split(",");
        for (String str : parts) {
            String[] parts2 = str.split("\n");
            for (String str2 : parts2) {
                if (Generic.isNumeric(str2.trim())) {
                    l.add(Integer.parseInt(str2.trim()));
                }
            }
        }
        return l;
    }

    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(final int b) {
                updateTextPane(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                updateTextPane(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }


    private void process() {
        n = new NationGen();
        n.settings = settings;
        n.restrictions.addAll(rPanel.getRestrictions());

        Thread thread = new Thread(() -> {
            startButton.setEnabled(false);
            if (!modNameRandom.isSelected()) {
                n.modname = modname.getText();
            }
            try {
                if (seedCheckBox.isSelected()) {
                    n.generate(parseSeeds());
                } else {
                    if (!seedRandom.isSelected()) {
                        n.generate(Integer.parseInt(amount.getText()), Integer.parseInt(seed.getText()));
                    } else {
                        n.generate(Integer.parseInt(amount.getText()));
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            startButton.setEnabled(true);

        });
        thread.start();
        hasRun = true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(startButton)) {
            if (modname.getText().length() == 0) {
                System.out.println("Please enter a mod name.");
                //modNameRandom.setSelected(true);
                return;
            }

            if (!seedCheckBox.isSelected()) {
                if (!(Generic.isNumeric(seed.getText()) || seed.getText().equals("Random"))) {
                    System.out.println("Please enter a numeric seed.");
                    //seedRandom.setSelected(true);
                    return;
                }

                if (!Generic.isNumeric(amount.getText()) || Integer.parseInt(amount.getText()) < 1) {
                    System.out.println("Please enter a numeric nation amount.");
                    return;
                }
            } else if (this.seedCheckBox.isSelected() && parseSeeds().isEmpty()) {
                System.out.println("Please specify numeric seeds or disable predefined seeds.");
                return;
            }
            settingText.setText(settings.getSettingInteger() + "");
            process();
        }
        if (e.getSource().equals(settingText)) {
            settingText.setText(settings.getSettingInteger() + "");
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();

        // Main screen settings
        JTextArea target = null;
        if (source == this.modNameRandom) {
            target = this.modname;
        } else if (source == this.seedRandom) {
            target = this.seed;
        }

        if (target != null) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                target.setEnabled(true);
                if (target == this.seed) {
                    Random r = new Random();
                    target.setText(r.nextInt() + "");
                }
            } else if (e.getStateChange() == ItemEvent.SELECTED) {
                target.setEnabled(false);
                target.setText("Random");
            }
        }

        // Options settings
        //todo: what is this suppose to do ...
        if (this.optionChecks.contains(source)) {
            double value = 0;
            if (e.getStateChange() == ItemEvent.SELECTED) {
                value = 1;
            }
            if (source == this.preview) {
                settings.put("drawPreview", value);
            }
            if (source == this.advDesc) {
                settings.put("advancedDescs", value);
            }
            if (source == this.basicDesc) {
                settings.put("basicDescs", value);
            }
            if (source == this.hideVanillaNations) {
                settings.put("hidevanillanations", value);
            }
            if (source == this.seedCheckBox) {
                this.seeds.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
                this.seedRandom.setSelected(true);
                this.seedRandom.setEnabled(e.getStateChange() != ItemEvent.SELECTED);
                this.amount.setEnabled(e.getStateChange() != ItemEvent.SELECTED);
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        Object source = e.getSource();
        if (source == this.eraSlider && eraSlider.getValueIsAdjusting()) {
            settings.put("era", eraSlider.getValue());
            settingText.setText(settings.getSettingInteger() + "");
        }
        if (source == this.sacredPowerSlider && sacredPowerSlider.getValueIsAdjusting()) {
            settings.put("sacredpower", sacredPowerSlider.getValue());
            settingText.setText(settings.getSettingInteger() + "");
        }
    }
}