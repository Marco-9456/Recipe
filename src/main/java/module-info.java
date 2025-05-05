module com.marco.recipe {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires atlantafx.base;
    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.material2;
    requires mysql.connector.j;

    exports com.marco.recipe to javafx.graphics;
    opens com.marco.recipe to javafx.fxml;
}