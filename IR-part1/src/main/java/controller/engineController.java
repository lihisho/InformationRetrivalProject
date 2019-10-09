package controller;

import engineClasses.*;
import javafx.collections.ObservableList;
import model.Imodel;
import model.myModel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class engineController {

    private static engineController singleton=null;
    private Imodel mymodel;
    private static LinkedHashMap<String,HashMap<String,Double>> resultsFromQueryFile;
    private static HashMap<String,Double> currentQueryResults;
    private static LinkedHashMap<String,String> givenQueries;
    private static HashMap<String,LinkedHashMap<String,String>> dominantPerDoc;


    public engineController(){this.mymodel= myModel.getInstance();}
    //saves a singeltone in order to work with the table view columns. and prevent duplication.
    public static engineController getInstance(){
        if(singleton==null)
            singleton=new engineController();
        return singleton;
    }

    //constructor
    public engineController(Imodel mymodel) {this.mymodel= mymodel;}
    //All functions are a station on the way to the model and pass the information.
    public void setStem(boolean stem){ mymodel.setStem(stem);}

    public void setPaths(String corpusPath, String postingPath) {
        mymodel.setPaths(corpusPath,postingPath);
    }

    public boolean makePosting() {
        return mymodel.makePosting();
    }
    public int getTotalNumOfDocs() {
        return mymodel.totalNumOfDocs();
    }

    public int getTotalNumOfTerms(){
        return mymodel.getTotalNumOfTerms();
    }

    public long getTotalTime(){
        return mymodel.getTotalTime();
    }

    public void clearDictionary(){
        mymodel.clearDictionary();
    }

    //Tal
    public ObservableList<String> getLanguages() {
        return mymodel.getLanguages();
    }
    public ObservableList<String> getCityList(){
        return mymodel.getCityList();
    }

    public void loadDictionaryToMemory()throws FileNotFoundException {
        mymodel.loadDictionaryToMemory();
    }

    public void setCurrQueryResults(String queryID)// set curr query results if we are in queries file
    {
        this.currentQueryResults= this.resultsFromQueryFile.get(queryID);
    }
    public void setQueryResults(){ //set curr query results if we run single query
        this.currentQueryResults=mymodel.getQueryResults();
    }

    public void setChosenCities(ObservableList<String> chosenCities) {
        mymodel.setChosenCities(chosenCities);
    }
    public String getNextQueryID() {
        return mymodel.getNextQueryID();
    }

    public void setResultsFromFileQuery(){
        this.resultsFromQueryFile= mymodel.getQueryResultsFromFileList();
    }

    public String getQueryIDNotFromTextFile(){
        return mymodel.getQueryIDNotFromTextFile();
    }
    public HashMap<String,Double> getCurrentQueryResults(){
        return this.currentQueryResults;
    }

    public LinkedHashMap<String,HashMap<String,Double>> getResultsFromFileQuery(){
        return this.resultsFromQueryFile;
    }


    public void setMapDominantPerDoc(){
        this.dominantPerDoc=mymodel.getDominantPerDoc();
    }

    public String getDominantForDoc(String docID){
        LinkedHashMap<String,String> listOfTerms=dominantPerDoc.get(docID);
        String result="";
        if(listOfTerms==null)
            return result;
        for(String s:listOfTerms.keySet()){

            result=result+"Term: "+s+",  Dominant value: "+listOfTerms.get(s)+"\n";
        }
        return result;
    }

    public void setGivenQueries(){
        this.givenQueries= mymodel.getGivenQueries();
    }

    public LinkedHashMap<String,String> getGivenQueries(){
        return this.givenQueries;
    }

    public void runSearchFromQueryDoc(String queryToSearch, String queryFilePath, boolean useSemantics) throws IOException {
        mymodel.runSearchFromQueryDoc(queryToSearch,queryFilePath, useSemantics);
    }

    public void startRunQuery(String queryToSearch,String queryID,boolean isFromQueryFile){
        mymodel.startRunQuery(queryToSearch,queryID,isFromQueryFile);
    }

    public Thread getCurrThread() {
        return mymodel.getCurrThread();
    }
}
