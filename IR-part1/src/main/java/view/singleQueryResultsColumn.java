package view;

import controller.engineController;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;

public class singleQueryResultsColumn {


    public engineController myController = engineController.getInstance();
    public String docID;
    public String rank;
    public Button btn_showEntities;
    String DominantResults;

    public singleQueryResultsColumn(String _docID,String _rank,Button b){

        docID=_docID;
        rank=_rank;
        btn_showEntities=b;
        btn_showEntities.setText("Show Entities");
        DominantResults=myController.getDominantForDoc(docID);
        btn_showEntities.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Dominant Entities");
            alert.setContentText(DominantResults);
            alert.setHeaderText(null);
            alert.showAndWait();
        });

    }

    public String getDocID(){
        return docID;
    }
    public void setDocID(String s){docID=s;}

    public String getRank(){return rank; }

    public void setRank(String s){rank=s;}

    public Button getBtn_showEntities() {
        return btn_showEntities;
    }

    public void setBtn_showEntities(Button b){btn_showEntities=b;}
}
