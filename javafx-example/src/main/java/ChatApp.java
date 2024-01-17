import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChatApp extends Application {

    private static final Logger LOGGER = LogManager.getLogger(ChatApp.class);

    private static final ObservableList<SearchAction> data = FXCollections.observableArrayList();
    private static final AnswerService docsAnswerService = new AnswerService();
    private final TableView<SearchAction> table = new TableView<>();
    private final TextArea lastAnswer = new TextArea();

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        LOGGER.info("Starting...");

        var holder = new VBox();
        holder.setStyle("-fx-padding: 15px;");

        Label label = new Label("What is your question?");
        label.setStyle("-fx-font-size: 25px");
        label.setStyle("-fx-font-weight: bold");

        TextField input = new TextField();
        input.setOnAction(e -> doSearch(input.getText()));
        input.setMinWidth(500);

        Button search = new Button("Search");
        search.setOnAction(e -> doSearch(input.getText()));

        var inputHolder = new HBox(input, search);
        inputHolder.setStyle("-fx-padding: 0 0 25px 0");

        TableColumn<SearchAction, String> timestamp = new TableColumn<>("Timestamp");
        timestamp.setCellValueFactory(cellData -> cellData.getValue().getTimestampProperty());
        timestamp.setMinWidth(250);
        TableColumn<SearchAction, String> question = new TableColumn<>("Question");
        question.setCellValueFactory(cellData -> cellData.getValue().getQuestionProperty());
        question.setMinWidth(250);
        TableColumn<SearchAction, String> answer = new TableColumn<>("Answer");
        answer.setCellValueFactory(cellData -> cellData.getValue().getAnswerProperty());
        answer.setMinWidth(300);
        TableColumn<SearchAction, Boolean> finished = new TableColumn<>("Finished");
        finished.setCellValueFactory(cellData -> cellData.getValue().getFinishedProperty());
        finished.setMinWidth(50);

        table.getColumns().addAll(timestamp, question, answer, finished);
        table.setItems(data);
        table.setStyle("-fx-padding: 0 25px 0 0");

        lastAnswer.setWrapText(true);

        holder.getChildren().addAll(label, inputHolder, new HBox(table, lastAnswer));

        Scene scene = new Scene(holder);

        stage.setTitle("JavaFX Chat Langchain4J Demo");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

        data.add(new SearchAction("Application started", true));

        var initAction = new SearchAction("Initializing search engine, please stand by...");
        data.add(initAction);
        lastAnswer.textProperty().bind(initAction.getAnswerProperty());
        new Thread(() -> docsAnswerService.init(initAction)).start();
    }

    private void doSearch(String question) {
        if (question.isEmpty()) {
            return;
        }

        var searchAction = new SearchAction(question);
        data.add(searchAction);
        lastAnswer.textProperty().bind(searchAction.getAnswerProperty());
        new Thread(() -> docsAnswerService.ask(searchAction)).start();
    }
}