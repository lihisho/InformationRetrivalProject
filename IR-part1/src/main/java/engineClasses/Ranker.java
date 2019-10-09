package engineClasses;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Ranker {
    private Parse rankerParser;
    private String postingPath;
    private boolean stem;
    private int M;
    private int avgD;
    private HashMap<String, String> mainDictionary;
    private ArrayList<String> listOfChosenCities;
    private HashMap<String, String[]> documentDetails;
    private HashMap<String, String> documentTitles;
    private HashMap<String,Double> rankedDocs;
    private double k;
    private double b;
    //Contructor.
    public Ranker(String postingPath, boolean stem, HashMap<String, String> mainDictionary, ArrayList<String> listOfChosenCities, Parse  rankerParser) {
        this.postingPath = postingPath;
        this.listOfChosenCities = listOfChosenCities;
        this.rankerParser = rankerParser;
        documentDetails = new HashMap<>();
        documentTitles = new HashMap<>();
        this.stem = stem;
        getDocDetailsToMemory();
        getDocTitlesToMemory();

        this.mainDictionary = mainDictionary;
        rankedDocs=new HashMap<>();
        M = 0; //Total num of docs.
        avgD = 0;
        k=1.2;
        b=0.75;
        calculateParameters();

    }

    /**
     * Brings the document details from saved files to the memory.
     */
    private void getDocDetailsToMemory() {
        FileReader fileReader;
        try {
            System.out.println(stem);
            if (stem)
                fileReader = new FileReader(new File(postingPath + "\\documentDetailsWithStem.txt"));
            else
                fileReader=new FileReader(new File(postingPath+"\\documentDetails.txt"));
            mainDictionary = new HashMap<>();
            String line;
            BufferedReader reader = new BufferedReader(fileReader);
            while ((line = reader.readLine()) != null) {
                String[] termKeyAndValue = line.split("\\|");
                documentDetails.put(termKeyAndValue[0], termKeyAndValue);
            }
            reader.close();
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getDocTitlesToMemory() {

        try {
            FileReader fr = new FileReader(new File(postingPath + "\\docTitles.txt"));
            BufferedReader reader = new BufferedReader(fr);
            String line;
            while ((line = reader.readLine()) != null) {
                String[] docTitle = line.split("\\>");
                if (docTitle.length>1 && !docTitle.equals(""))
                    documentTitles.put(docTitle[0], docTitle[1].toLowerCase());
            }
            reader.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Calculates the average length of all documents.
     */
    private void calculateParameters() {
        int totalLength=0;
        for(String docID:documentDetails.keySet()){
            totalLength += Integer.parseInt(documentDetails.get(docID)[3]);//d
        }
        M=documentDetails.size();
        avgD=totalLength/M;

    }

    /**
     * Calculates the rank of each word and doc and sums up. Saves a list of docs with their ranks.
     * @param docsToGetRank- docs in list for ranking.

     */
    public void calculateRankNew(String[] docsToGetRank, String word, int numbOfAppearance) {
        int df = docsToGetRank.length;
        int numWordsQuery;//change here if we will count this on searcher so this function get the parameter c(w,q)
        for(String docPosting:docsToGetRank){
            numWordsQuery =numbOfAppearance;
            if(docPosting.equals(""))
                continue;
            String [] splitedDocPosting=docPosting.split("\\|");
            String docID=splitedDocPosting[0];
            numWordsQuery+=addTilteRank(docID,word);
            String [] array=documentDetails.get(docID);
            if(listOfChosenCities.size()!=0 &&(array.length==5 && !listOfChosenCities.contains(array[4])))//check if there id choosen cities and if yes, check if the city of the doc appears in the chosen cities
                continue;

            int tf=Integer.parseInt(splitedDocPosting[1]);
            int d=Integer.parseInt(documentDetails.get(docID)[3]); // Get the length of the document.
            double BMrank=(numWordsQuery*(((k+1)*tf)/(tf+(k*(1-b+(b*((double)(d/avgD)) ))))))*Math.log((double)(M+1)/df) ;
            double relativeRank= getRelativePositionRank(splitedDocPosting[2],d);
            Double rank=0.7*BMrank +0.3*relativeRank ;
            if(rankedDocs.containsKey(docID)) //if this doc already has rank, so add it to the rank that was given.
                rankedDocs.put(docID,rankedDocs.get(docID)+rank);
            else
                rankedDocs.put(docID,rank);
        }
    }
    //Calculate the rank if words show in the title.
    public int addTilteRank(String docID, String word){
        String title = documentTitles.get(docID);
        int wordCount=0;
        if(title!=null && !title.equals("")) {
            if (title.contains(word.toLowerCase())) {
                wordCount++;
            }
        }
        return wordCount;
    }

    public HashMap<String,Double> getRankedDocs(){
        return rankedDocs;
    }

    public void clearRankedDocsEndOfQuery(){
        rankedDocs.clear();
    }


    /**
     * Returns a calculation of the position of each term in the document. terms that appear first, will get a higher rank.
     * @param stringPositions - string represent positions of a word in a document
     * @param docLength - length of the current document.
     * @return
     */
    private Double getRelativePositionRank (String stringPositions , int docLength){
        String [] positions=stringPositions.replaceAll("\\[|\\]", "").split(",");
        double jump=docLength/20;
        double[] positionRanks={jump,jump*2,jump*3,jump*4,jump*5};//jump*6,jump*7,jump*8,jump*9,jump*10};

        for(int i=0;i<positionRanks.length;i++) {
            if (Double.parseDouble(positions[0]) < positionRanks[i])
                return (double) 10 - (i*2);
        }
        return (double)0;
    }

    /**
     * Returns a calculation of adjacent rankings.
     * Rank is determined by: number of terms that are adjacent in the query and in the document divided by the number of possible adjacent terms.
     * @param mapPerDoc - includes all details from posting file per each term in the query.
     * @param termsInOrder - all terms from the query.
     */
    private Double getAdjacentsRank (HashMap<String , String[]>mapPerDoc, String [] termsInOrder){
        String[] positionsCurr;
        String[] positionsNext;
        int numOfAdj=0;
        int isSpace=0;
        int next;
        for(int i=0;i<termsInOrder.length;i++){// for loop on the order array of terms in query
            next=i+1;
            if(termsInOrder[i]==null || termsInOrder[i].equals("")) {
                isSpace++;
                continue;
            }
            if(i+1<termsInOrder.length){
                if(termsInOrder[next]==null) {
                    next++;
                    isSpace++;
                    if(next>=termsInOrder.length)
                        break;
                }
                positionsCurr= mapPerDoc.get(termsInOrder[i])[2].replaceAll("\\[|\\]", "").split(",");// positions in doc for the current term
                positionsNext=mapPerDoc.get(termsInOrder[next])[2].replaceAll("\\[|\\]", "").split(","); //positions in doc for the adjacent term
                for(int j=0;j<positionsCurr.length;j++){
                    for(int k=0;k<positionsNext.length;k++) {
                        if (Math.abs(Integer.parseInt(positionsCurr[j]) - Integer.parseInt(positionsNext[k])) <= 5)// check if the words are adjusent in text. the maximum length between 2 adjusent words is 5
                        {
                            numOfAdj++;
                            break;
                        }
                        if(Integer.parseInt(positionsCurr[j])+5 <= Integer.parseInt(positionsNext[k]))
                            break;
                    }
                }
            }
        }
        double rank=((double)(numOfAdj/(termsInOrder.length-isSpace-1)))*10;
        return rank;
    }

}
