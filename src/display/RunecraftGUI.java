package display;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import methods.CraftMethod;
import methods.CraftMethods;
import methods.RunecraftTask;
import org.rspeer.runetek.api.ClientSupplier;
import org.rspeer.runetek.api.component.tab.EquipmentSlot;
import org.rspeer.script.Script;
import org.rspeer.ui.Log;
import task_structure.TreeScript;
import utils.AbyssLoadouts;
import utils.AbyssObstacles;
import utils.FoodTypes;
import utils.RuneTypes;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class RunecraftGUI extends JFrame {
    private JComboBox<CraftMethods> methodChoice;
    private JPanel selectionPanel;
    private JButton startButton;
    private JPanel abyssPanel;
    private JComboBox<RuneTypes> abyssRuneSpecifier;
    private JPanel pouchSelection;
    private JCheckBox smallPouchCheckBox;
    private JCheckBox mediumPouchCheckBox;
    private JCheckBox largePouchCheckBox;
    private JCheckBox giantPouchCheckBox;
    private JPanel traversalSelection;
    private JComboBox<AbyssLoadouts> traversalChoice;
    private JCheckBox useOfStaminaPotionsCheckBox;
    private JPanel staminaPanel;
    private JCheckBox useOfClanWarsCheckBox;
    private JComboBox<FoodTypes> foodChoice;
    private JCheckBox useOfStaminaPotionsAbyssCheckBox;
    private JPanel selfHealPanel;
    private JPanel taskFocus;
    private JCheckBox enableTaskQueuingCheckBox;
    private JPanel queuePanel;
    private JList<RunecraftTask> taskList;
    private JButton addQueuedTaskButton;
    private JPanel queueSpecifications;
    private JTextField levelDefinedInput;
    private JCheckBox untilInsufficientSuppliesCheckBox;
    private JLabel stopLevelLabel;
    private JPanel armorPanel;
    private JCheckBox specifyArmorUsageCheckBox;
    private JPanel armorUsagePanel;
    private JLabel helm;
    private JLabel chest;
    private JLabel legs;
    private JLabel feet;
    private JLabel hands;
    private JLabel offhand;
    private JButton configureWornArmorButton;
    private Class<?> craftClass;
    private boolean hasBeenSet;
    private final Queue<RunecraftTask> runecraftTasks;
    private final java.lang.reflect.Type type;

    public RunecraftGUI() {
        super("DRunecraft Selection");
        type = new TypeToken<List<String>>() {
        }.getType();
        runecraftTasks = new LinkedList<>();
        hasBeenSet = false;
        setContentPane(selectionPanel);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setLocationRelativeTo(ClientSupplier.get().getCanvas());
        for (final CraftMethods method : CraftMethods.values())
            methodChoice.addItem(method);
        for (final RuneTypes runeType : RuneTypes.values())
            abyssRuneSpecifier.addItem(runeType);
        for (final AbyssLoadouts loadout : AbyssLoadouts.values())
            traversalChoice.addItem(loadout);
        for (final FoodTypes foodType : FoodTypes.values())
            foodChoice.addItem(foodType);
        abyssPanel.setVisible(false);
        methodChoice.addActionListener(e -> {
            final boolean isAbyssVisible = Objects.requireNonNull(methodChoice.getSelectedItem()).equals(CraftMethods.ABYSS);
            staminaPanel.setVisible(!isAbyssVisible);
            abyssPanel.setVisible(isAbyssVisible);
            pack();
        });
        armorUsagePanel.setVisible(false);
        specifyArmorUsageCheckBox.addActionListener(e -> {
            if (doesEquipmentFileExist()) loadEquipmentChoice();
            armorUsagePanel.setVisible(specifyArmorUsageCheckBox.isSelected());
            pack();
        });
        configureWornArmorButton.addActionListener(e -> {
            final String helmText = EquipmentSlot.HEAD.getItemName();
            if (!helmText.isEmpty()) helm.setText(helmText);
            final String chestText = EquipmentSlot.CHEST.getItemName();
            if (!chestText.isEmpty()) chest.setText(chestText);
            final String legsText = EquipmentSlot.LEGS.getItemName();
            if (!legsText.isEmpty()) legs.setText(legsText);
            final String feetText = EquipmentSlot.FEET.getItemName();
            if (!feetText.isEmpty()) feet.setText(feetText);
            final String handsText = EquipmentSlot.HANDS.getItemName();
            if (!handsText.isEmpty()) hands.setText(handsText);
            final String shieldText = EquipmentSlot.OFFHAND.getItemName();
            if (!shieldText.isEmpty()) offhand.setText(shieldText);
            saveEquipmentChoice();
        });
        queueSpecifications.setVisible(false);
        queuePanel.setVisible(false);
        enableTaskQueuingCheckBox.addActionListener(e -> {
            final boolean isUsingQueuing = enableTaskQueuingCheckBox.isSelected();
            queueSpecifications.setVisible(isUsingQueuing);
            queuePanel.setVisible(isUsingQueuing);
            pack();
        });
        taskList.addListSelectionListener(e -> {
            final int selectedIndex = taskList.getSelectedIndex();
            if (selectedIndex == -1) return;
            ((DefaultListModel) taskList.getModel()).remove(selectedIndex);
        });
        untilInsufficientSuppliesCheckBox.addActionListener(e -> {
            final boolean isUsingLevelDefinedStop = !untilInsufficientSuppliesCheckBox.isSelected();
            stopLevelLabel.setEnabled(isUsingLevelDefinedStop);
            levelDefinedInput.setEnabled(isUsingLevelDefinedStop);
        });
        addQueuedTaskButton.addActionListener(e -> {
            final String definedInput = getLevelDefinedInput();
            if (definedInput.equals("0") || !definedInput.matches("\\d{1,2}")) {
                Log.severe("Your level goal should be between 1 and 99!");
                return;
            }
            ((DefaultListModel<RunecraftTask>) taskList.getModel()).addElement(new RunecraftTask(this));
            pack();
        });
        useOfClanWarsCheckBox.addActionListener(e -> selfHealPanel.setVisible(!useOfClanWarsCheckBox.isSelected()));
        startButton.addActionListener(e -> {
            if (queuePanel.isVisible()) {
                final ListModel<RunecraftTask> listModel = taskList.getModel();
                final int modelSize = listModel.getSize();
                if (modelSize == 0) {
                    Log.severe("You haven't added any tasks - do so before starting!");
                    return;
                } else for (int i = 0; i < modelSize; i++) runecraftTasks.add(listModel.getElementAt(i));
            } else runecraftTasks.add(new RunecraftTask(this));
            setVisible(false);
            hasBeenSet = true;
        });
        pack();
        setVisible(true);
    }

    public boolean isHidden() {
        return !isVisible();
    }

    public boolean hasBeenSet() {
        return hasBeenSet;
    }

    public void exit() {
        setVisible(false);
    }

    public boolean isQueuePanelVisible() {
        return queuePanel.isVisible();
    }

    public boolean isUsingAbyss() {
        return abyssPanel.isVisible();
    }

    public boolean isUsingClanWars() {
        return useOfClanWarsCheckBox.isSelected();
    }

    public boolean isUsingSmallPouch() {
        return smallPouchCheckBox.isSelected();
    }

    public boolean isUsingMediumPouch() {
        return mediumPouchCheckBox.isSelected();
    }

    public boolean isUsingLargePouch() {
        return largePouchCheckBox.isSelected();
    }

    public boolean isUsingGiantPouch() {
        return giantPouchCheckBox.isSelected();
    }

    public String getTraversalSetting() {
        final AbyssLoadouts loadoutChosen = (AbyssLoadouts) traversalChoice.getSelectedItem();
        if (loadoutChosen == null) return null;
        return loadoutChosen.equals(AbyssLoadouts.MINING_LOADOUT) ? "pickaxe" : loadoutChosen.equals(AbyssLoadouts.WOODCUTTING_LOADOUT) ? "axe" : "";
    }

    public String getHelm() {
        final String text = helm.getText();
        return text.equals("None") ? "" : text;
    }

    public String getChest() {
        final String text = chest.getText();
        return text.equals("None") ? "" : text;
    }

    public String getLegs() {
        final String text = legs.getText();
        return text.equals("None") ? "" : text;
    }

    public String getFeet() {
        final String text = feet.getText();
        return text.equals("None") ? "" : text;
    }

    public String getHands() {
        final String text = hands.getText();
        return text.equals("None") ? "" : text;
    }

    public String getShield() {
        final String text = offhand.getText();
        return text.equals("None") ? "" : text;
    }

    public Class<?> getCraftClass() {
        return ((CraftMethods) Objects.requireNonNull(methodChoice.getSelectedItem())).getMethod();
    }

    public String getMethodName() {
        return Objects.requireNonNull(methodChoice.getSelectedItem()).toString();
    }

    public boolean isUsingStamina() {
        final boolean abyssVisible = isUsingAbyss(), clanWarsSelected = isUsingClanWars();
        return (!abyssVisible && useOfStaminaPotionsCheckBox.isSelected()) || (abyssVisible && !clanWarsSelected && useOfStaminaPotionsAbyssCheckBox.isSelected());
    }

    public Set<String> getObstacles() {
        final Set<String> obstacles = new HashSet<>();
        for (final AbyssObstacles obstacle : ((AbyssLoadouts) Objects.requireNonNull(traversalChoice.getSelectedItem())).getObstacles())
            obstacles.add(obstacle.toString());
        return obstacles;
    }

    public RuneTypes getAbyssRuneSpecifier() {
        return (RuneTypes) abyssRuneSpecifier.getSelectedItem();
    }

    public FoodTypes getFoodChoice() {
        return (FoodTypes) foodChoice.getSelectedItem();
    }

    public boolean willRunAdNauseam() {
        return untilInsufficientSuppliesCheckBox.isSelected();
    }

    public String getLevelDefinedInput() {
        return levelDefinedInput.getText();
    }

    public Queue<RunecraftTask> getRunecraftTasks() {
        return runecraftTasks;
    }

    public CraftMethod getMethod(final TreeScript handler) {
        final boolean abyssVisible = abyssPanel.isVisible(), clanWarsSelected = useOfClanWarsCheckBox.isSelected();
        if ((!abyssVisible && useOfStaminaPotionsCheckBox.isSelected()) || (abyssVisible && !clanWarsSelected && useOfStaminaPotionsAbyssCheckBox.isSelected()))
            handler.addNotedSetting("stamina");
        if (abyssVisible) {
            if (smallPouchCheckBox.isSelected()) handler.addNotedSetting("Small pouch");
            if (mediumPouchCheckBox.isSelected()) handler.addNotedSetting("Medium pouch");
            if (largePouchCheckBox.isSelected()) handler.addNotedSetting("Large pouch");
            if (giantPouchCheckBox.isSelected()) handler.addNotedSetting("Giant pouch");
            for (final AbyssObstacles obstacle : ((AbyssLoadouts) traversalChoice.getSelectedItem()).getObstacles())
                handler.addNotedSetting(obstacle.toString());
        }
        try {
            return (CraftMethod) (abyssVisible ? clanWarsSelected ? craftClass.getDeclaredConstructor(TreeScript.class, RuneTypes.class).newInstance(handler, (RuneTypes) abyssRuneSpecifier.getSelectedItem()) : craftClass.getConstructor(TreeScript.class, RuneTypes.class, FoodTypes.class).newInstance(handler, (RuneTypes) abyssRuneSpecifier.getSelectedItem(), (FoodTypes) foodChoice.getSelectedItem()) : craftClass.getDeclaredConstructor(TreeScript.class).newInstance(handler));
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveEquipmentChoice() {
        final File equipmentFile = new File(Script.getDataDirectory() + "/drunecraft_equipment.json");
        try {
            JsonWriter jWriter = new JsonWriter(new FileWriter(equipmentFile));
            final List<String> equipment = Arrays.asList(getHelm(), getChest(), getLegs(), getFeet(), getHands(), getShield());
            Gson gson = new GsonBuilder().create();
            JsonElement jElement = gson.toJsonTree(equipment, type);
            gson.toJson(jElement, jWriter);
            jWriter.flush();
            jWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean doesEquipmentFileExist() {
        return new File(Script.getDataDirectory() + "/drunecraft_equipment.json").exists();
    }

    private void loadEquipmentChoice() {
        final File equipmentFile = new File(Script.getDataDirectory() + "/drunecraft_equipment.json");
        try {
            final JsonReader reader = new JsonReader(new FileReader(equipmentFile));
            Gson gson = new GsonBuilder().create();
            final List<String> equipment = gson.fromJson(reader, type);
            final List<JLabel> labels = Arrays.asList(helm, chest, legs, feet, hands, offhand);
            for (int i = 0; i < labels.size(); i++) {
                final String focused = equipment.get(i);
                if (focused.isEmpty()) continue;
                labels.get(i).setText(focused);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
