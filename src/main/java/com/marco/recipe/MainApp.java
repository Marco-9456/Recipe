package com.marco.recipe;

import atlantafx.base.theme.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

// Import der MVC-Komponenten
import com.marco.recipe.model.DatabaseService;
import com.marco.recipe.view.MainView;
import com.marco.recipe.controller.MainController;

/**
 * Hauptklasse der Rezeptverwaltungsanwendung.
 * Initialisiert die JavaFX-Anwendung, erstellt die Hauptkomponenten (MVC),
 * konfiguriert die Hauptbühne (Stage) und startet die Benutzeroberfläche.
 * Kümmert sich auch um das saubere Beenden der Anwendung (z.B.
 * Datenbankverbindung trennen).
 *
 * @version 1.1
 * @author Marco's Recipe Application Team
 */
@SuppressWarnings("All")
public class MainApp extends Application {

    private DatabaseService databaseService; // Hält die Instanz des DB-Service

    /**
     * Die Hauptmethode, die die JavaFX-Anwendung startet.
     *
     * @param args Kommandozeilenargumente (werden nicht verwendet).
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Die Haupt-Einstiegsmethode für die JavaFX-Anwendung.
     * Wird nach dem Aufruf von launch() automatisch aufgerufen.
     * Initialisiert die Anwendungskomponenten und zeigt das Hauptfenster an.
     *
     * @param primaryStage Die Hauptbühne (das Hauptfenster) der Anwendung.
     */
    @Override
    public void start(Stage primaryStage) {
        // --- 1. Datenbankverbindung konfigurieren und Service initialisieren ---
        // HINWEIS: Die URL sollte ggf. aus einer Konfigurationsdatei kommen.
        String dbUrl = "jdbc:mysql://localhost:3300/rezeptdb"; // Beispiel-URL anpassen!

        // *** Änderung: Verwende leere Strings für Benutzer und Passwort ***
        // Annahme: Verbindung ist ohne spezifische Credentials möglich (z.B. root ohne
        // PW)
        String dbUser = "root"; // Oder der Benutzername, der ohne Passwort verwendet wird
        String dbPassword = ""; // Leeres Passwort

        try {
            databaseService = new DatabaseService(dbUrl, dbUser, dbPassword);
        } catch (RuntimeException e) {
            // Fehler beim Laden des JDBC-Treibers (bereits im Konstruktor von DBService)
            showInitializationError("Fehler beim Initialisieren", "JDBC Treiber Problem", e.getMessage());
            Platform.exit(); // Anwendung beenden, wenn DB-Service nicht initialisiert werden kann
            return; // Wichtig, um weitere Ausführung zu verhindern
        }

        // --- 2. View und Controller initialisieren ---
        MainView mainView = new MainView();
        // Der Controller wird erstellt und verbindet View und Service.
        // Die Initialisierung (Laden der Daten etc.) geschieht im
        // Controller-Konstruktor.
        new MainController(mainView, databaseService);

        // --- 3. Szene erstellen und konfigurieren ---
        Scene scene = new Scene(mainView.getRootPane(), 900, 650); // Startgröße des Fensters

        // --- 4. AtlantaFX Theme anwenden ---
        Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());

        // --- 5. Stage konfigurieren und anzeigen ---
        primaryStage.setTitle("Rezeptverwaltung");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(700); // Minimale Fenstergröße
        primaryStage.setMinHeight(500);

        // Setzt den Handler für das Schließen des Fensters (wird vor stop() aufgerufen)
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Schließ-Anfrage erhalten. Trenne Datenbankverbindung...");
            // Die eigentliche Trennung erfolgt in stop()
        });

        primaryStage.show(); // Zeigt das Fenster an
    }

    /**
     * Diese Methode wird aufgerufen, wenn die JavaFX-Anwendung beendet wird
     * (z.B. durch Schließen des Hauptfensters oder Aufruf von Platform.exit()).
     * Wird verwendet, um Ressourcen freizugeben, insbesondere die
     * Datenbankverbindung.
     */
    @Override
    public void stop() {
        System.out.println("Anwendung wird beendet...");
        if (databaseService != null) {
            databaseService.disconnect(); // Datenbankverbindung sicher trennen
        }
        System.out.println("Aufräumarbeiten abgeschlossen. Tschüss!");
    }

    /**
     * Zeigt einen Fehlerdialog während der Initialisierungsphase an.
     * Da die Haupt-Stage möglicherweise noch nicht bereit ist, wird ein separater
     * Alert erstellt.
     *
     * @param title   Der Titel des Dialogs.
     * @param header  Der Header-Text.
     * @param content Der Inhaltstext (Fehlermeldung).
     */
    private void showInitializationError(String title, String header, String content) {
        // Sicherstellen, dass Alerts im FX Application Thread angezeigt werden
        // (wichtig, falls Fehler sehr früh auftritt)
        if (Platform.isFxApplicationThread()) {
            createAndShowAlert(title, header, content);
        } else {
            Platform.runLater(() -> createAndShowAlert(title, header, content));
        }
    }

    /**
     * Erstellt und zeigt den eigentlichen Alert-Dialog an.
     */
    private void createAndShowAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        // Da die Hauptstage evtl. nicht existiert, setzen wir keinen Owner
        alert.showAndWait();
    }
}