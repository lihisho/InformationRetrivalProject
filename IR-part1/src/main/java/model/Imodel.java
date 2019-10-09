package model;

import javafx.collections.ObservableList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

public interface Imodel {
    void setPaths(String corpusPath, String postingPath);

    boolean makePosting();

    int totalNumOfDocs();
    int getTotalNumOfTerms();
    long getTotalTime();
    void setStem(boolean stem);
    void clearDictionary();

    ObservableList<String> getLanguages();

    void loadDictionaryToMemory() throws FileNotFoundException;

    ObservableList<String> getCityList();

    void setChosenCities(ObservableList<String> chosenCities);

    void search(String queryToSearch,String queryID,boolean isFromQueryFile);

    HashMap<String,Double> getQueryResults();

    LinkedHashMap<String,HashMap<String,Double>> getQueryResultsFromFileList();

    String getNextQueryID();

    LinkedHashMap<String,String> getGivenQueries();

    HashMap<String,LinkedHashMap<String,String>> getDominantPerDoc();
    String getQueryIDNotFromTextFile();

    void runSearchFromQueryDoc(String queryToSearch,String queryFilePath , boolean useSemantics) throws IOException;

    void startRunQuery(String queryToSearch,String queryID,boolean isFromQueryFile);

    Thread getCurrThread();

    String getSemantics(String falkland_petroleum_exploration);
}
