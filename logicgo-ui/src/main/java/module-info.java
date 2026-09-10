module cz.logicgo.ui {
    requires cz.logicgo.core;
    requires cz.logicgo.engine;

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires org.controlsfx.controls;
    requires org.apache.pdfbox;
    requires com.github.librepdf.openpdf;
    requires ch.qos.logback.classic;
    requires org.slf4j;
    requires cz.logicgo.persistence;
    requires com.fasterxml.jackson.databind;
    requires javafx.graphics;
    requires org.reflections;
    requires org.girod.javafx.svgimage;
    requires com.sun.jna;
    requires com.sun.jna.platform;

    opens cz.logicgo.ui to javafx.fxml, javafx.graphics;
    opens cz.logicgo.ui.controllers to javafx.fxml;
    opens cz.logicgo.ui.controllers.exportControllers to javafx.fxml;
    opens cz.logicgo.ui.controllers.exportControllers.cart to javafx.fxml;
    opens cz.logicgo.ui.controllers.exportControllers.exportTabs to javafx.fxml;
    opens cz.logicgo.ui.controllers.gameControllers to javafx.fxml;
    opens cz.logicgo.ui.controllers.gameControllers.gameControllers to javafx.fxml;
    opens cz.logicgo.ui.controllers.popover to javafx.fxml;
    opens cz.logicgo.ui.controllers.screenControllers to javafx.fxml;
    opens cz.logicgo.ui.controllers.settingsControllers to javafx.fxml;
    opens cz.logicgo.ui.controllers.viewer to javafx.fxml;

    exports cz.logicgo.ui.controllers.listControllers to javafx.fxml;
    exports cz.logicgo.ui.controllers.exportControllers to javafx.fxml;
    exports cz.logicgo.ui.controllers.gameControllers to javafx.fxml;
    exports cz.logicgo.ui.controllers.screenControllers to javafx.fxml;
    exports cz.logicgo.ui.controllers.settingsControllers to javafx.fxml;
    exports cz.logicgo.ui.controllers.popover to javafx.fxml;
    exports cz.logicgo.ui.controllers.helpers to javafx.fxml;
    exports cz.logicgo.ui.controllers.menu to javafx.fxml;
    exports cz.logicgo.ui.controllers.viewer to javafx.fxml;

    opens cz.logicgo.ui.controllers.listControllers to javafx.fxml;
    opens cz.logicgo.ui.controllers.helpers to javafx.fxml;
    opens cz.logicgo.ui.controllers.menu to javafx.fxml;
    exports cz.logicgo.ui.misc to com.fasterxml.jackson.databind;


}
