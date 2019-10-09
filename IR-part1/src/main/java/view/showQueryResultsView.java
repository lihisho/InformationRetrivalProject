package view;
import controller.engineController;
import engineClasses.valueComparator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class showQueryResultsView extends Aview implements Initializable {
    private engineController myController=engineController.getInstance();
    public TableView<singleQueryResultsColumn> tableResults;
    public TableColumn<singleQueryResultsColumn,String> docIDCol;
    public TableColumn<singleQueryResultsColumn,String> rankCol;
    public TableColumn<singleQueryResultsColumn,String> entitiesButton;
    public Button btn_chooseDirectoryResult;
    public Button btn_saveResults;
    public TextField txtfld_oneQueryResultsFilePath;
    HashMap<String,Double> returnResults;

    private String qId;


    public void initialize(URL location, ResourceBundle resource){
        docIDCol.setCellValueFactory(new PropertyValueFactory<>("docID"));
        rankCol.setCellValueFactory(new PropertyValueFactory<>("rank"));
        entitiesButton.setCellValueFactory(new PropertyValueFactory<>("btn_showEntities"));
        returnResults=myController.getCurrentQueryResults();
        qId = myController.getQueryIDNotFromTextFile();
        ObservableList<singleQueryResultsColumn> data= FXCollections.observableArrayList();
        if(returnResults.size()!=0) {
            for (String docID : returnResults.keySet()) {
                data.add(new singleQueryResultsColumn(docID, ""+returnResults.get(docID), new Button()));
            }
        }
        tableResults.setItems(data);

    }

    public void setQueryID(String queryID){
        this.qId =queryID;
    }

    public void handleChooseResultsPath(ActionEvent actionEvent){
        handleBrowse(txtfld_oneQueryResultsFilePath);
    }

    /**
     * Saves to file the results of each query.
     * Results will be in the pattern: "QueryID 0 docNo. rank sim(float) runID".
     */
    public void handleSaveResults(){
        if (txtfld_oneQueryResultsFilePath.getText().equals(""))
            displayInformationMessage("Please enter a path to save results.", "Path Empty");
        else {
            File allQueryResults = new File(txtfld_oneQueryResultsFilePath.getText()+ "\\oneQueryResults.txt");
            try {

                FileWriter fw = new FileWriter(allQueryResults, false);
                BufferedWriter bw = new BufferedWriter(fw);
                String docNo;
                int docRank;
                float rank;
                StringBuilder resultString = new StringBuilder();
                for (String doc : returnResults.keySet()) {
                    docNo = doc;
                    docRank= returnResults.get(doc).intValue();
                    rank =returnResults.get(doc).floatValue();
                    if(docRank<0 || docRank>1000)
                        docRank =1;
                    resultString.setLength(0);
                    resultString.append(qId);
                    resultString.append(" 0 ");
                    resultString.append(docNo);
                    resultString.append(" ").append(docRank).append(" ");
                    resultString.append((rank/10));
                    resultString.append(" wow");
                    bw.write(resultString.toString());
                    bw.newLine();
                    bw.flush();
                }
                //close files
                fw.close();
                bw.close();


            } catch (IOException e) {
                // e.printStackTrace();
            }
        }
    }
}
