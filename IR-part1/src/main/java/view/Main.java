package view;

import controller.engineController;
import engineClasses.ReadFile;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.myModel;

import java.io.IOException;
import java.util.Optional;


public class Main extends Application {

    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("MyEngine");
        FXMLLoader fxmlLoader = new FXMLLoader();
        DirectoryChooser DC = new DirectoryChooser();
        Parent root = (Parent) fxmlLoader.load(getClass().getResource("/MyEngine.fxml").openStream());
        Scene Scene = new Scene(root, 700, 600);
        primaryStage.setScene(Scene);
        myModel myModel = new myModel();
        engineController myController = new engineController(myModel);
        myEngineView view = fxmlLoader.getController(); //Tal
        view.setMyController(myController);

        primaryStage.show();


        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Exit");
                alert.setContentText("Are you sure that you want to exit?");
                alert.setHeaderText(null);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    if(view.t != null) {
                        if (view.t.isAlive()) {
                            view.t.interrupt();
                        }
                    }
                }
                else if (result.get() == ButtonType.CANCEL){
                    alert.close();
                    event.consume();
                }
            }
        });
    }
}

/*
public class Main {
    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        String folder = "C:\\Users\\lihi\\IdeaProjects\\old revisions\\IR-part1\\corpus\\corpus";
        ReadFile rf = new ReadFile(false, "C:\\Users\\lihi\\IdeaProjects\\old revisions\\IR-part1\\postings");
        rf.createCitiesList(folder);
        rf.setStopWordsAndCities(folder);
        rf.listFilesForFolder(folder);
        rf.sendTerms(true);

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Total runTime:" + (totalTime / 1000));


        /////queries:
        //System.out.println("count of distinct terms in corpus: " + rf.getNumOfDistinctTerms()); //3
        //System.out.println("count of number terms in corpus: " + rf.getNumberOfnumberTerms()); //3
        //System.out.println("count of cities in corpus: "+ rf.getNumOfCities());//5
        //System.out.println("count of citiesNotcapital in corpus: "+ rf.getNumOfCitiesNotCapital());//5
        //System.out.println("count of cities capital: "+ (rf.getNumOfCities()- rf.getNumOfCitiesNotCapital()));//5
        //System.out.println("count of countries: "+ (rf.getNumOfCountries())); //4
        //System.out.println("properties of document that has the max num of city references.: "+ (rf.getMaxTfCityName())); //6



    }
*/



