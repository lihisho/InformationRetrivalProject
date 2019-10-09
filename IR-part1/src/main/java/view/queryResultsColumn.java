package view;

import controller.engineController;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

public class queryResultsColumn {
    public engineController myController =engineController.getInstance();
    public String queryID;
    public String queryText;
    public TreeMap<String,Double> queryIDResults;
    private Hyperlink hyl_viewResults;

    public queryResultsColumn(String _queryID,String _queryText, Hyperlink _hyl){
        this.queryID=_queryID;
        this.queryText=_queryText;
        this.hyl_viewResults=_hyl;
        hyl_viewResults.setText("query results");
        hyl_viewResults.setOnAction(event -> {
            myController.setCurrQueryResults(queryID);//sets in controller the results for the curr queryID
            FXMLLoader fxmlLoader = new FXMLLoader();
            try {
                InputStream is = this.getClass().getResource("/showQueryResult.fxml").openStream();
                Parent actionScreen = fxmlLoader.load(is);
                Aview queryResult = fxmlLoader.getController();
                queryResult.setMyController(this.myController);
                ((showQueryResultsView)queryResult).setQueryID(queryID);
                Scene newScene = new Scene(actionScreen, 650, 500);
                //Stage curStage = (Stage) btn_makeDictionary.getScene().getWindow();
                Stage curStage = new Stage();
                curStage.setScene(newScene);
                curStage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public String getQueryID(){
        return queryID;
    }

    public void setQueryID(String s){
        queryID=s;
    }

    public Hyperlink getHyl_viewResults(){
        return hyl_viewResults;
    }

    public void setHyl_viewResults(Hyperlink h){
        hyl_viewResults=h;
    }

    public String getQueryText(){return queryText;}

    public void setQueryText(String s){queryText=s;}

}
