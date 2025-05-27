package com.marco.recipe.view;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.layout.InputGroup;
import atlantafx.base.theme.*;
import atlantafx.base.theme.Styles;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// Import der Model-Klasse aus dem korrekten Paket
import com.marco.recipe.model.Recipe;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import java.util.Objects;

/**
 * Erstellt und verwaltet die Hauptansicht (das Hauptfenster) der
 * Rezeptverwaltung.
 * Diese Klasse baut die Benutzeroberfläche programmatisch mit
 * JavaFX-Komponenten auf.
 * Sie folgt dem MVC-Muster und stellt UI-Elemente bereit, auf die der
 * Controller zugreifen kann.
 *
 * @version 1.1
 * @author Mohammed
 */

@SuppressWarnings("All")
public class MainView {

    // --- Layout-Container ---
    private BorderPane rootPane; // Haupt-Layoutcontainer

    // --- UI-Komponenten ---
    private ListView<Recipe> recipeListView; // Liste zur Anzeige der Rezepttitel
    private InputGroup searchFieldGroup; // Textfeld für die Suche
    private CustomTextField searchField;

    private Button newButton; // Button zum Erstellen neuer Rezepte
    private Button editButton; // Button zum Bearbeiten ausgewählter Rezepte
    private Button deleteButton; // Button zum Löschen ausgewählter Rezepte

    // Bereich für die Detailansicht
    private VBox detailViewContainer; // Container für die Detailansicht im Zentrum
    private ScrollPane detailScrollPane; // ScrollPane für die Details, falls sie zu lang werden
    private Label detailPlaceholderLabel; // Label, das angezeigt wird, wenn kein Rezept ausgewählt ist

    // Theme-Auswahl
    private ComboBox<String> themeComboBox;
    private final String[] themeNames = {"PrimerLight", "PrimerDark", "Dracula", "NordLight", "NordDark", "CupertinoLight", "CupertinoDark"};

    /**
     * Konstruktor für die MainView.
     * Initialisiert die UI-Komponenten und ordnet sie im Layout an.
     */
    public MainView() {
        initComponents();
        layoutComponents();
    }

    /**
     * Initialisiert die einzelnen UI-Komponenten der Hauptansicht.
     */
    private void initComponents() {
        // Rezeptliste
        recipeListView = new ListView<>();
        // Optional: Placeholder, wenn die Liste leer ist
        recipeListView.setPlaceholder(new Label("Keine Rezepte vorhanden."));
        recipeListView.getStyleClass().addAll(Styles.STRIPED, Styles.DENSE);

        // Suchfeld - sehr nervig gewesen, aber hat am Ende doch geklappt
        searchField = new CustomTextField();
        searchField.setPromptText("Suche nach Rezepten..."); // Platzhaltertext
        searchField.setLeft(new FontIcon(Material2OutlinedMZ.SEARCH));

        // Buttons
        newButton = new Button("Neues Rezept", new FontIcon(Material2OutlinedAL.ADD));
        newButton.setMaxWidth(Double.MAX_VALUE); // Button füllt die Breite der VBox
        newButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT); // Fügt die 'primary' Akzent-Stilklasse hinzu

        editButton = new Button("Rezept Bearbeiten", new FontIcon(Material2OutlinedAL.EDIT));
        editButton.setMaxWidth(Double.MAX_VALUE);
        editButton.setDisable(true); // Initial deaktiviert, bis ein Rezept ausgewählt wird
        editButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.WARNING);

        deleteButton = new Button("Rezept Löschen", new FontIcon(Material2OutlinedMZ.REMOVE));
        deleteButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setDisable(true); // Initial deaktiviert
        deleteButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.DANGER); // Fügt die 'danger' Akzent-Stilklasse hinzu

        // Detailansicht-Container und Platzhalter
        detailPlaceholderLabel = new Label("Bitte wählen Sie ein Rezept aus der Liste aus.");
        detailPlaceholderLabel.getStyleClass().add(Styles.TEXT_SUBTLE);

        detailViewContainer = new VBox(detailPlaceholderLabel); // Initial nur Platzhalter anzeigen
        detailViewContainer.setPadding(new Insets(15));
        detailViewContainer.setAlignment(Pos.TOP_LEFT); // Inhalt Zintrieren
        detailViewContainer.setSpacing(10);

        // ScrollPane für die Detailansicht
        detailScrollPane = new ScrollPane(detailViewContainer);
        detailScrollPane.setFitToWidth(true); // Passt die Breite des Inhalts an die ScrollPane an
        detailScrollPane.setFitToHeight(true); // Passt die Höhe an (optional)
        detailScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Horizontale Scrollbar nie anzeigen
        detailScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Vertikale Scrollbar bei Bedarf

        // Theme ComboBox initialisieren
        themeComboBox = new ComboBox<>();
        themeComboBox.getItems().addAll(themeNames);
        themeComboBox.setValue("CupertinoLight");
        themeComboBox.setMaxWidth(Double.MAX_VALUE);
        Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet()); // Initiales Theme anwenden

        themeComboBox.setOnAction(event -> {
            String selectedTheme = themeComboBox.getValue();
            if (selectedTheme != null) {
                switch (selectedTheme) {
                    case "PrimerLight":
                        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
                        break;
                    case "PrimerDark":
                        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
                        break;
                    case "Dracula":
                        Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());
                        break;
                    case "NordLight":
                        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
                        break;
                    case "NordDark":
                        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
                        break;
                    case "CupertinoLight":
                        Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
                        break;
                    case "CupertinoDark":
                        Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());
                        break;
                }
            }
        });
    }

    /**
     * Ordnet die initialisierten UI-Komponenten im Hauptlayout (BorderPane) an.
     */
    private void layoutComponents() {
        rootPane = new BorderPane();
        rootPane.setPadding(new Insets(10)); // Außenabstand für das gesamte Fenster

        // --- Linke Spalte: Suche und Liste ---
        VBox leftPane = new VBox(10); // 10px Abstand zwischen Elementen
        leftPane.setPadding(new Insets(5));
        // Suchfeld oben
        leftPane.getChildren().add(searchField);
        // Rezeptliste darunter, soll den verfügbaren Platz füllen
        leftPane.getChildren().add(recipeListView);
        VBox.setVgrow(recipeListView, Priority.ALWAYS); // ListView soll vertikal wachsen
        rootPane.setLeft(leftPane);
        BorderPane.setMargin(leftPane, new Insets(0, 10, 0, 0)); // Abstand zum mittleren Bereich

        // --- Rechte Spalte: Aktionsbuttons ---
        VBox rightPane = new VBox(10); // 10px Abstand
        rightPane.setPadding(new Insets(5));
        rightPane.setAlignment(Pos.TOP_CENTER); // Buttons oben zentriert
        rightPane.getChildren().addAll(newButton, editButton, deleteButton, new Separator(), themeComboBox);
        rootPane.setRight(rightPane);
        BorderPane.setMargin(rightPane, new Insets(0, 0, 0, 10)); // Abstand zum mittleren Bereich

        // --- Mittlerer Bereich: Detailansicht ---
        // Setzt die ScrollPane (die den detailViewContainer enthält) in die Mitte
        rootPane.setCenter(detailScrollPane);

        // Optional: Setze Mindestbreiten für die Bereiche
        leftPane.setMinWidth(200);
        rightPane.setMinWidth(150);
        // Mindestbreite für den Detailbereich ergibt sich implizit
    }

    // --- Methoden zur Aktualisierung der View ---

    /**
     * Zeigt die Details eines Rezepts im mittleren Bereich an.
     * Ersetzt den aktuellen Inhalt des Detail-Containers durch den übergebenen
     * Node.
     *
     * @param detailsNode Der JavaFX Node (z.B. eine VBox, GridPane), der die
     *                    formatierten Rezeptdetails enthält. Darf nicht null sein.
     */
    public void displayRecipeDetails(Node detailsNode) {
        Objects.requireNonNull(detailsNode, "Der anzuzeigende Detail-Node darf nicht null sein.");
        // Ersetzt alle Kinder (also den Platzhalter oder vorherige Details)
        // durch den neuen Node.
        detailViewContainer.getChildren().setAll(detailsNode);
    }

    /**
     * Setzt den Detailbereich zurück und zeigt wieder den Platzhaltertext an.
     * Wird aufgerufen, wenn kein Rezept ausgewählt ist oder ein Rezept gelöscht
     * wird.
     */
    public void clearRecipeDetails() {
        detailViewContainer.getChildren().setAll(detailPlaceholderLabel);
    }

    /**
     * Aktiviert oder deaktiviert die Buttons "Bearbeiten" und "Löschen".
     * Wird typischerweise vom Controller aufgerufen, wenn sich die Auswahl
     * in der Rezeptliste ändert.
     *
     * @param enable true, um die Buttons zu aktivieren, false zum Deaktivieren.
     */
    public void enableActionButtons(boolean enable) {
        editButton.setDisable(!enable);
        deleteButton.setDisable(!enable);
    }

    // --- Getter für UI-Komponenten (werden vom Controller benötigt) ---

    /**
     * Gibt den Haupt-Layoutcontainer (BorderPane) zurück.
     * Wird benötigt, um die Szene im Hauptanwendungsfenster zu setzen.
     *
     * @return Die BorderPane der Hauptansicht.
     */
    public BorderPane getRootPane() {
        return rootPane;
    }

    /**
     * Gibt die ListView zurück, die die Rezepte anzeigt.
     * Wird vom Controller benötigt, um die Liste zu füllen und Selektionen zu
     * überwachen.
     *
     * @return Die ListView für Rezepte.
     */
    public ListView<Recipe> getRecipeListView() {
        return recipeListView;
    }

    /**
     * Gibt das Textfeld für die Suche zurück.
     * Wird vom Controller benötigt, um Suchanfragen zu lesen.
     *
     * @return Das TextField für die Suche.
     */
    public TextField getSearchField() {
        return searchField;
    }

    /**
     * Gibt den Button zum Erstellen neuer Rezepte zurück.
     * Wird vom Controller benötigt, um einen ActionHandler zu registrieren.
     *
     * @return Der "Neues Rezept"-Button.
     */
    public Button getNewButton() {
        return newButton;
    }

    /**
     * Gibt den Button zum Bearbeiten von Rezepten zurück.
     * Wird vom Controller benötigt, um einen ActionHandler zu registrieren.
     *
     * @return Der "Rezept Bearbeiten"-Button.
     */
    public Button getEditButton() {
        return editButton;
    }

    /**
     * Gibt den Button zum Löschen von Rezepten zurück.
     * Wird vom Controller benötigt, um einen ActionHandler zu registrieren.
     *
     * @return Der "Rezept Löschen"-Button.
     */
    public Button getDeleteButton() {
        return deleteButton;
    }
}
