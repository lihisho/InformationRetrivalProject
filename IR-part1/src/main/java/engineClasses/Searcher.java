package engineClasses;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Searcher {
    private String postingPath;
    private String corpusPath;
    private HashMap<String,Integer> wordCountInQuery;// map- word,c(w,q)
    private boolean stem;
    private HashMap<String,String> mainDictionary;
    private HashMap<String,String> cityDictionary;
    private RandomAccessFile mergedPosting;
    private RandomAccessFile cityMergedPosting;
    private Parse parser;
    private Parse rankerParser;
    private Stemmer stemmer;
    private Ranker ranker;
    ArrayList<String> listOfChosenCities;
    File postingFile;
    File cityPostigFile;
    File DominantPerDoc;
    HashMap<String,LinkedHashMap<String,String>> docDominantTFMap;



    //Constructor.
    public Searcher(String corpusPath, String postingPath, HashMap<String,String> mainDictionary, HashMap<String,String> cityDictionary, boolean stem, ArrayList<String> listOfChosenCities) {
        this.postingPath= postingPath;
        this.stem = stem;
        this.mainDictionary= mainDictionary;
        this.cityDictionary=cityDictionary;
        this.corpusPath=corpusPath;
        this.listOfChosenCities=listOfChosenCities;
        wordCountInQuery= new HashMap<>();
        if (stem){
            postingFile = new File(postingPath +"\\mergedFileWithStem.txt");
            cityPostigFile=new File(postingPath+"\\mergedCityPostingFileWithStem.txt");
            stemmer =new Stemmer();
        }
        else {
            postingFile = new File(postingPath + "\\mergedFile.txt");
            cityPostigFile=new File(postingPath+"\\mergedCityPostingFile.txt");
        }
        try {
            mergedPosting= new RandomAccessFile(postingFile, "rw");
            cityMergedPosting=new RandomAccessFile(cityPostigFile, "rw");
        } catch (IOException e) {
            e.printStackTrace();
        }
        parser = new Parse(stem, postingPath,true);//Tal- create parser object in order to parse query
        rankerParser = new Parse(stem, postingPath,true);//Tal- create parser object in order to parse query
        String stopWordString = null; // Reads the file content and returns it as string.
        try {
            stopWordString = new String(Files.readAllBytes((Paths.get(corpusPath + "/stop_words.txt"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        rankerParser.setStopWords(stopWordString);
        parser.setStopWords(stopWordString);
        ranker=new Ranker(postingPath,stem,mainDictionary, listOfChosenCities, rankerParser);
        getDominantFromMem();
    }
    /**
     * Returns a sorted list of documents that match the query.
     * @param query- given query to search.
     * @throws IOException
     */
    public HashMap<String,Double> queryResults(String query) throws IOException {
        clearQueryStructures(); //In every new query clear the last info saved.
        if(listOfChosenCities!=null){// if user chose a list of cities we want to retrieve docs that contains the cities so we add it to the qury
            StringBuilder newQuery=new StringBuilder(query);
            for (String city:listOfChosenCities){
                newQuery.append(" "+city);
            }
            query=newQuery.toString();
        }

        parser.resetsFields();
        parser.parser(query);
        HashMap<String,Integer> countWordsInQuery=new HashMap();
        for(String term:parser.wordsInDoc.keySet()){
            for(String doc:parser.wordsInDoc.get(term).keySet()){
                countWordsInQuery.put(term,parser.wordsInDoc.get(term).get(doc).size());
            }
        }
        Set<String> termsFromQuery= (parser.wordsInDoc.keySet());// terms in the query , here duplicate word in query will appear as one. so we cannot count here c(w,q). maybe count before parse and save it and send to rank

        int numbOfAppearance;
        for (String word:termsFromQuery) {
            String[] docsToGetRank = getDocsForWord(word);
            numbOfAppearance = countWordsInQuery.get(word);
            if (docsToGetRank==null)//the word is not in the dictionary
                continue;
            ranker.calculateRankNew(docsToGetRank, word,numbOfAppearance );//ranker saves a structure of all ranked docs.
        }
        //HashMap<String,Double> returnSorted= sortDocumentByRank(ranker.getRankedDocs());//when finish go throw all the words in query structure ranked docs is passed here
        HashMap<String,Double> returnSorted=sortDocByRank((ranker.getRankedDocs()));
        ranker.clearRankedDocsEndOfQuery();//end of query, clear ranked docs
        return returnSorted;
    }

    /**
     * Returns a list of docs for each query term.
     * @param word - the word to search for the docs list.
     * @return
     */
    public String[] getDocsForWord(String word){
        String postingLine;
        boolean isCity=false;
        String [] dicValue;
        //Search for term in the dictionary, in lower or upper case.
        if(mainDictionary.containsKey(word))
            dicValue = mainDictionary.get(word).split(",");
        else if (mainDictionary.containsKey(word.toLowerCase()))
            dicValue = mainDictionary.get(word.toLowerCase()).split(",");
        else if (mainDictionary.containsKey(word.toUpperCase()))
            dicValue = mainDictionary.get(word.toUpperCase()).split(",");
        else if (cityDictionary.containsKey(word.toUpperCase())) {
            dicValue = cityDictionary.get(word.toUpperCase()).split(",");
            isCity = true;
        } else // If the word from query does not exist in the dictionary- check the next word and there is no ranking for this word.
            return null;
        try {
            if(dicValue.length>2) {
                if (isCity) {
                    cityMergedPosting.seek((Long.parseLong(dicValue[2])));
                    postingLine = cityMergedPosting.readLine();
                } else {
                    mergedPosting.seek(Long.parseLong(dicValue[2]));
                    postingLine = mergedPosting.readLine();
                }
                return postingLine.split("\\{");//all docs and them details for word in query.
            }
        }catch (Exception e){}
        return null;

    }
    /**
     * Returns a sorted list of the 50 docs with the highest rank.
     * @param unsortMap- map of all the docs and them rank
     * @return map of 50 documents with the higher rank
     */
    private HashMap<String,Double> sortDocByRank(Map<String, Double> unsortMap) {

        List<Map.Entry<String, Double>> list = new LinkedList<>(unsortMap.entrySet());
        Collections.sort(list, new rankComparator());
        HashMap<String, Double> sortedDocsByRank = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : list) {
            sortedDocsByRank.put(entry.getKey(), entry.getValue());
        }
        HashMap<String, Double> returnedDocs = new LinkedHashMap();
        int i = 0;
        for (String currDoc : sortedDocsByRank.keySet()) {
            Double s = sortedDocsByRank.get(currDoc);
            returnedDocs.put(currDoc, sortedDocsByRank.get(currDoc));
            i++;
            if (i == 50)
                return returnedDocs;
        }
        return returnedDocs;//return a map of 50 or less higher rank docs    }
    }

    /**
     * Loads Dominant entities from file.
     */
    public void getDominantFromMem(){

        if (stem) {
            DominantPerDoc = new File(postingPath + "/mapTFWithStem.txt");
        }
        else {
            DominantPerDoc = new File(postingPath + "/mapTF.txt");
        }
        try {
            docDominantTFMap = new HashMap<>();
            String line;
            FileReader fileReader = new FileReader(DominantPerDoc);
            BufferedReader reader = new BufferedReader(fileReader);
            while (null != (line = reader.readLine())) {
                String[] termKeyAndValue = line.split("\\>");
                LinkedHashMap<String,String> listOfTerms= new LinkedHashMap<>();
                String[] dominantsTerms=termKeyAndValue[1].split(",");
                String term="";
                for(int i=0;i<dominantsTerms.length;i++) {
                    if(i%2==0){
                        term=dominantsTerms[i];
                    }
                    else
                        listOfTerms.put(term,dominantsTerms[i]);
                }
                docDominantTFMap.put(termKeyAndValue[0],listOfTerms);
            }

            fileReader.close();
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //Close files at the end of use.
    public void closeFiles(){
        try {
            mergedPosting.close();
            cityMergedPosting.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Clear all info about the last query and before the next query.
    public void clearQueryStructures(){
        wordCountInQuery.clear();
        //   wordsInDocWithTF.clear();
    }

    public HashMap<String, LinkedHashMap<String, String>> getDocDominantTFMap() {
        return docDominantTFMap;
    }
}


class rankComparator implements Comparator<Map.Entry<String, Double>> {
    @Override
    public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
        if( (o1).getValue()>( (o2).getValue()))
            return -1;
        if((o1).getValue()<( (o2).getValue()))
            return 1;
        return 0;

    }
}

