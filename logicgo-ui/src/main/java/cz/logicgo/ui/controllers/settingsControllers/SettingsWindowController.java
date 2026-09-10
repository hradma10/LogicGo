package cz.logicgo.ui.controllers.settingsControllers;

import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.gameClasses.keys.KeyEventDTO;
import cz.logicgo.core.misc.enums.NodeType;
import cz.logicgo.core.misc.enums.keys.GeneralEvent;
import cz.logicgo.core.misc.enums.keys.HotkeyEvent;
import cz.logicgo.core.misc.enums.settings.SettingKey;
import cz.logicgo.core.misc.enums.settings.UserSettings;
import cz.logicgo.persistence.services.UserService;
import cz.logicgo.persistence.services.UserSettingsService;
import cz.logicgo.ui.controllers.ControllerClosable;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import cz.logicgo.ui.utils.GameUiUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cz.logicgo.core.misc.Messages.getFormatted;


public class SettingsWindowController implements ControllerClosable {

    @FXML
    private TabPane settingsTabPane;
    @FXML
    private Tab accountTabWrapper;
    @FXML
    private Tab colorSettingsTab;
    @FXML
    private Tab keybindingsTab;

    @FXML
    private ScrollPane colorScrollPane;
    @FXML
    private ScrollPane keybindingsScrollPane;

    @FXML
    private Button okButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button resetButton;

    @FXML
    private AccountTabController accountTabController;

    private final UserSettingsService userSettingsService = new UserSettingsService();
    private final UserService userService = new UserService();

    private User currentUser;
    private MainScreenController mainScreenController;

    private final Map<SettingKey, Object> settingsBuffer = new HashMap<>();
    private final List<Color> colorBuffer = new ArrayList<>();
    private final Map<HotkeyEvent, KeyEventDTO> hotkeyBuffer = new HashMap<>();

    public void initialize(User user, MainScreenController mainScreenController) {
        this.currentUser = user;
        this.mainScreenController = mainScreenController;

        if (accountTabController != null) {
            accountTabController.initData(user, mainScreenController);
        }

        buildColorTab();
        buildKeybindingsTab();
    }

    private void buildColorTab() {
        VBox colorContainer = new VBox(15);
        colorContainer.setStyle("-fx-padding: 15;");

        HBox row = new HBox(10);
        row.setStyle("-fx-alignment: CENTER_LEFT;");
        row.getStyleClass().add("settings-row");

        Label label = new Label(getFormatted("userSettings.color_theme") + ":");
        label.setPrefWidth(220);
        label.setStyle("-fx-text-fill: #dfe1e5;");

        Node colorControl = ColorSettingsUIGenerator.createColorControl(colorBuffer, currentUser);

        row.getChildren().addAll(label, colorControl);
        colorContainer.getChildren().add(row);

        for (UserSettings setting : UserSettings.values()) {
            if (setting.getNodeType() == NodeType.COLOR_PICKER) {
                continue;
            }
        }

        colorScrollPane.setContent(colorContainer);
    }

    private void buildKeybindingsTab() {
        VBox hotkeyContainer = new VBox(25);
        hotkeyContainer.setStyle("-fx-padding: 15;");

        for (Class<? extends HotkeyEvent> hotkeyClass : DefaultKeyBindings.getAllClasses()) {
            if (hotkeyClass.equals(GeneralEvent.class)) {
                continue;
            }

            VBox sectionCard = new VBox(15);
            sectionCard.getStyleClass().add("settings-card");

            String translationKey = "";
            HotkeyEvent[] constants = hotkeyClass.getEnumConstants();
            if (constants != null && constants.length > 0) {
                translationKey = constants[0].getSectionTranslationKey();
            }

            String sectionName = getFormatted(translationKey);

            Label sectionHeader = new Label(sectionName);
            sectionHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -color-primary; -fx-padding: 0 0 5 0;");
            sectionCard.getChildren().add(sectionHeader);

            boolean hasEvents = false;
            for (HotkeyEvent event : constants) {
                HBox row = new HBox(10);
                row.setStyle("-fx-alignment: CENTER_LEFT;");
                row.getStyleClass().add("settings-row");

                Label label = new Label(event.getTranslation() + ":");
                label.setPrefWidth(220);
                label.setStyle("-fx-text-fill: #dfe1e5;");

                Button keyButton = HotkeySettingsUIGenerator.createKeyCaptureButton(
                        event, hotkeyBuffer, currentUser.getSavedHotkeys()
                );

                row.getChildren().addAll(label, keyButton);
                sectionCard.getChildren().add(row);
                hasEvents = true;
            }

            if (hasEvents) {
                hotkeyContainer.getChildren().add(sectionCard);
            }
        }
        keybindingsScrollPane.setContent(hotkeyContainer);
    }

    @FXML
    public void onResetClick() {
        settingsBuffer.clear();
        for (UserSettings setting : UserSettings.values()) {
            if (!setting.isExcludedFromReset()) {
                settingsBuffer.put(setting, setting.getDefaultValue());
            }
        }

        colorBuffer.clear();
        colorBuffer.addAll(GameUiUtils.getDefaultColorTheme());

        hotkeyBuffer.clear();
        hotkeyBuffer.putAll(DefaultKeyBindings.generateDefaults());

        buildColorTab();
        buildKeybindingsTab();
    }

    @FXML
    public void onCancelClick() {
        settingsBuffer.clear();
        colorBuffer.clear();
        hotkeyBuffer.clear();

        if (mainScreenController != null) {
            mainScreenController.onHomeAction();
        }
    }

    @FXML
    public void onOKClick() {
        try {
            User freshUser = userService.getUserById(currentUser.getId());
            if (freshUser == null) {
                freshUser = currentUser;
            }

            boolean shouldSave = false;

            if (!colorBuffer.isEmpty()) {
                freshUser.setUserColors(new ArrayList<>(colorBuffer).stream().map(Color::toString).collect(Collectors.toList()));
                shouldSave = true;
            }

            if (!settingsBuffer.isEmpty()) {
                userSettingsService.saveSettingsFromBuffer(freshUser, settingsBuffer);
                shouldSave = true;
            }

            if (!hotkeyBuffer.isEmpty()) {
                Map<HotkeyEvent, KeyEventDTO> updatedKeys = new HashMap<>(freshUser.getSavedHotkeys());
                updatedKeys.putAll(hotkeyBuffer);
                freshUser.getSavedHotkeys().clear();
                freshUser.getSavedHotkeys().putAll(updatedKeys);
                shouldSave = true;
            }

            if (shouldSave) {
                userService.updateUser(freshUser);
            }

            if (currentUser != null) {
                if (!colorBuffer.isEmpty()) {
                    currentUser.setUserColors(new ArrayList<>(colorBuffer).stream().map(Color::toString).collect(Collectors.toList()));
                }
                if (!hotkeyBuffer.isEmpty()) {
                    currentUser.getSavedHotkeys().putAll(hotkeyBuffer);
                }
            }

            if (mainScreenController != null) {
                mainScreenController.onHomeAction();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void terminateAllActiveActions() {
    }

    @Override
    public void refreshContent() {
        ControllerClosable.super.refreshContent();
    }
}
