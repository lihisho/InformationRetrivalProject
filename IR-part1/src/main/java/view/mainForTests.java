//package view;
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//
////package view;
////
////import com.sun.webkit.dom.DocumentImpl;
////import engineClasses.valueComparator;
////import model.myModel;
////import org.jsoup.Jsoup;
////import org.jsoup.nodes.Document;
////import org.jsoup.nodes.Element;
////import org.jsoup.select.Elements;
////import java.io.*;
////import java.util.*;
////
//public class mainForTests {
//    public static void main(String[] args) throws IOException {
//////        HashMap<String, Double> rankedDocs = new HashMap<>();
//////        List<String> sortedDocsByRank;
//////        rankedDocs.put("a", 1.0);
//////        rankedDocs.put("b", 2.0);
//////        rankedDocs.put("c", 7.0);
//////        rankedDocs.put("d", 3.0);
//////        TreeMap<String, Double> sortedTermsByTF = new TreeMap<>(new valueComparator1(rankedDocs));
//////        sortedTermsByTF.putAll(rankedDocs);
//////        System.out.println(sortedTermsByTF.keySet());
////
//////        Document doc = Jsoup.parse(" <top>\n" + "\n" + "<num> Number: 351 \n" + "\n"  + "<title> Falkland petroleum exploration \n" + "\n" + "<desc> Description: \n" + "What information is available on petroleum exploration in \n" + "the South Atlantic near the Falkland Islands?\n" + "\n" + "<narr> Narrative: \n" + "Any document discussing petroleum exploration in the\n" + "South Atlantic near the Falkland Islands is considered\n" + "relevant.  Documents discussing petroleum exploration in \n" + "continental South America are not relevant.\n" + "\n" + "</top>\n" + "\n" + "\n" + "<top>\n" + "\n" + "<num> Number: 352 \n" + "<title> British Chunnel impact  \n" + "\n" + "<desc> Description: \n" + "What impact has the Chunnel had on the British economy and/or \n" + "the life style of the British?\n" + "\n" + "<narr> Narrative: \n" + "Documents discussing the following issues are relevant:\n" + "\n" + " - projected and actual impact on the life styles of the British \n" + " - Long term changes to economic policy and relations\n" + " - major changes to other transportation systems linked with \n" + "   the Continent\n" + "\n" + "Documents discussing the following issues are not relevant:\n" + "\n" + " - expense and construction schedule \n" + " - routine marketing ploys by other channel crossers (i.e.,\n" + "   schedule changes, price drops, etc.) \n" + " \n" + "</top>\n" + "\n" + "\n" + "<top>\n" + "\n" + "<num> Number: 358\n" + "<title> blood-alcohol fatalities\n" + "\n" + "<desc> Description: \n" + "What role does blood-alcohol level play in \n" + "automobile accident fatalities?\n" + "\n" + "<narr> Narrative: \n" + "Relevant documents must contain information\n" + "on automobile accidents in which there was a \n" + "fatality and the blood-alcohol level of the\n" + "driver of the vehicle must be identified.\n" + " \n" + "</top>\n" + "\n" + "\n" + "<top>\n" + "\n" + "<num> Number: 359\n" + "<title> mutual fund predictors \n" + "\n" + "<desc> Description: \n" + "Are there reliable and consistent predictors of\n" + "mutual fund performance?\n" + "\n" + "<narr> Narrative: \n" + "A document must contain at least one factor \n" + "such as: rankings, risks, yields, or costs,\n" + "and fund performance to be relevant.  \n" + "Documents that discuss mutual fund rankings \n" + "are considered relevant.  \n" + "\n" + "</top>\n" + "\n" + "\n" + "<top>\n" + "\n" + "<num> Number: 362\n" + "<title> human smuggling \n" + "\n" + "<desc> Description: \n" + "Identify incidents of human smuggling.\n" + "\n" + "<narr> Narrative: \n" + "A relevant document shows an incident of humans \n" + "(at least ten) being smuggled.  The smugglers \n" + "would have to realize a monetary gain for their \n" + "actions, while the people being smuggled may or \n" + "may not be willing participants.\n" + "\n" + "</top>\n" + "\n" + "\n" + "<top>\n" + "\n" + "<num> Number: 367\n" + "<title> piracy \n" + "\n" + "<desc> Description: \n" + "What modern instances have there been of old fashioned\n" + "piracy, the boarding or taking control of boats? \n" + " \n" + "<narr> Narrative: \n" + "Documents discussing piracy on any body of water are\n" + "relevant.  Documents discussing the legal taking of \n" + "ships or their contents by a national authority are \n" + "non-relevant.  Clashes between fishing vessels over\n" + "fishing are not relevant, unless one vessel is \n" + "boarded.  \n" + "\n" + "</top>\n" + "\n" + "\n" + "<top>\n" + "\n" + "<num> Number: 373\n" + "<title> encryption equipment export \n" + "\n" + "<desc> Description: \n" + "Identify documents that discuss the concerns of\n" + "the United States regarding the export of \n" + "encryption equipment.\n" + "\n" + "<narr> Narrative: \n" + "Documents that merely mention the name of a company\n" + "or group that produces encryption equipment but does\n" + "not mention the exportation and/or commercial exploitation\n" + "of the encryption equipment are not relevant.  Documents \n" + "which refer to governmental access into the encryption \n" + "systems for the purposes of counter-intelligence or \n" + "anti-crime activities are relevant. \n" + "\n" + "</top>\n" + "\n" + "\n" + "<top>\n" + "\n" + "<num> Number: 374\n" + "<title> Nobel prize winners \n" + "\n" + "<desc> Description: \n" + "Identify and provide background information on Nobel\n" + "prize winners.\n" + "\n" + "<narr> Narrative: \n" + "At a minimum, relevant documents must contain the \n" + "following information:  year of Nobel prize award, \n" + "field of study, and recipients name.  When the \n" + "document announces what is obviously a current award,\n" + "no year is required.\n" + "\n" + "</top>\n" + "\n" + "\n" + "<top>\n" + "\n" + "<num> Number: 377\n" + "<title> cigar smoking \n" + "\n" + "<desc> Description: \n" + "Identify documents that discuss the renewed \n" + "popularity of cigar smoking.\n" + "\n" + "<narr> Narrative: \n" + "A relevant document will discuss the extent of\n" + "the resurgence of cigar smoking or the social\n" + "and economic issues attendant to it.  Documents\n" + "that discuss \"Cigar Nights\", \"Cigar Rooms\" and\n" + "cigar production are relevant.\n" + "\n" + "</top>\n" + "\n" + "\n" + "<top>\n" + "\n" + "<num> Number: 380\n" + "<title> obesity medical treatment \n" + "\n" + "<desc> Description: \n" + "Identify documents that discuss medical treatment\n" + "of obesity. \n" + "\n" + "<narr> Narrative: \n" + "A relevant document should identify prescribed\n" + "legal medications or treatments used to combat\n" + "obesity and the positive or negative affects\n" + "resulting from the applications.\n" + "\n" + "</top>\n" + "\n" + "\n" + "<top>\n" + "\n" + "<num> Number: 384\n" + "<title> space station moon \n" + "\n" + "<desc> Description: \n" + "Identify documents that discuss the building of\n" + "a space station with the intent of colonizing the\n" + "moon.\n" + " \n" + "<narr> Narrative: \n" + "A relevant document will discuss the purpose of a \n" + "space station, initiatives towards colonizing the \n" + "moon, impediments which thus far have thwarted such a \n" + "project, plans currently underway or in the planning\n" + "stages for such a venture; cost, countries prepared\n" + "to make a commitment of men, resources, facilities\n" + "and money to accomplish such a feat.\n" + "\n" + "</top>\n" + "\n" + "\n" + "<top>\n" + "\n" + "<num> Number: 385\n" + "<title> hybrid fuel cars \n" + "\n" + "<desc> Description: \n" + "Identify documents that discuss the current status of\n" + "hybrid automobile engines, (i.e., cars fueled by something\n" + "other than gasoline only).\n" + " \n" + "<narr> Narrative: \n" + "A relevant document may include research on non-gasoline \n" + "powered engines or prototypes that may be fueled by natural \n" + "gas, methanol, alcohol; cost to the consumer; health benefits \n" + "derived; and shortcomings in horsepower and passenger comfort.\n" + "\n" + "</top>\n" + "\n" + "\n" + "<top>\n" + "\n" + "<num> Number: 387\n" + "<title> radioactive waste \n" + "\n" + "<desc> Description: \n" + "Identify documents that discuss effective and safe ways to \n" + "permanently handle long-lived radioactive wastes.\n" + " \n" + "<narr> Narrative: \n" + "Documents that discuss incineration, cementation, bitumenization, \n" + "vitrification, and in underground nuclear explosion are relevant.\n" + "\n" + "</top>\n" + "\n" + "\n" + "<top>\n" + "\n" + "<num> Number: 388\n" + "<title> organic soil enhancement \n" + "\n" + "<desc> Description: \n" + "Identify documents that discuss the use of organic\n" + "fertilizers (composted sludge, ash, vegetable waste, \n" + "microorganisms, etc.) as soil enhancers. \n" + "  \n" + "<narr> Narrative: \n" + "The focus of the topic is on soil enhancement.  \n" + "Documents that discuss other uses of organic material\n" + "are not relevant, nor are documents that concentrate\n" + "solely on chemical fertilizers.\n" + "\n" + "</top>\n" + "\n" + "\n" + "<top>\n" + "\n" + "<num> Number: 390\n" + "<title> orphan drugs \n" + "\n" + "<desc> Description: \n" + "Find documents that discuss issues associated with so-called \n" + "\"orphan drugs\", that is, drugs that treat diseases affecting \n" + "relatively few people.\n" + " \n" + "<narr> Narrative: \n" + "A relevant document will discuss how the Orphan Drug Act \n" + "is working on behalf of those who suffer from orphan \n" + "diseases and conditions, or how this matter is handled in \n" + "other countries.\n" + " \n" + "</top>");
//////        Elements links = doc.select("top");
//////        String city;
//////        for (Element link : links) {
//////            String queryID=link.select("num").text();
//////            System.out.println(queryID.substring(8,11));
////////.split("Number:")[1]
//////            String description = link.select("desc").text();
//////            if (description.contains("Description:")) {
//////                String[] split = description.split("Description:");
//////                description = split[1];
//////                city = link.select("title").text();
//////                //System.out.println(city);
//////            }
//////        }
////        /*
////        File dictionaryFile = new File("C:\\Users\\lihi\\IdeaProjects\\old revisions\\IR-part1\\postings" + "\\mainDictionary.txt");
////
////        File postingFile = new File("C:\\Users\\lihi\\IdeaProjects\\old revisions\\IR-part1\\postings" + "\\mergedFile.txt");
////
////        try {
////            RandomAccessFile mergedPosting= new RandomAccessFile(postingFile, "rw");
////            HashMap<String, String> mainDictionary = new HashMap<>();
////
////            String line;
////            FileReader fileReader = new FileReader(dictionaryFile);
////            BufferedReader reader = new BufferedReader(fileReader);
////
////
////            while (null != (line = reader.readLine())) {
////                String[] termKeyAndValue = line.split(">");
////                mainDictionary.put(termKeyAndValue[0], termKeyAndValue[1]);
////            }
////            fileReader.close();
////            reader.close();
////            String s = mainDictionary.get("pan-yokja").split(",")[2];
////            System.out.println(s);
////            mergedPosting.seek(Long.parseLong(s));
////            System.out.println(mergedPosting.readLine());
////
////        } catch (
////                IOException e) {
////            e.printStackTrace();
////        }
////        */
//////        myModel mod = new myModel();
//////        System.out.println(myModel.getInstance().getSemantics("Falkland petroleum exploration"));
////
////        HashMap<String, Double> returnedDocsToSort = new HashMap<>();
////        returnedDocsToSort.put("a", 3.0);
////        returnedDocsToSort.put("b", 7.0);
////        returnedDocsToSort.put("c", 1.0);
////        returnedDocsToSort.put("d", 1.0);
////
////        Map<String,Double>sorted=sortByComparator(returnedDocsToSort);
////       // TreeMap<String, Double> sortedDocsByRank = new TreeMap<>(new valueComparator1(returnedDocsToSort));
////        //sortedDocsByRank.putAll(returnedDocsToSort);
////        System.out.println(sorted.toString());
////        for (String currDoc : sorted.keySet()) {
////            Double s = sorted.get(currDoc);
////            System.out.println(s);
////        }
////
////    }
////        public static Map<String, Double> sortByComparator(Map<String, Double> unsortMap) {
////
////            List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());
////
////            Collections.sort(list, new MyComparator());
////
////            Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
////            for (Map.Entry<String, Double> entry : list) {
////                sortedMap.put(entry.getKey(), entry.getValue());
////            }
////            return sortedMap;
////
////        }
////}
////class MyComparator implements Comparator<Map.Entry<String, Double>> {
////    @Override
////    public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
////        if( (o1).getValue()>( (o2).getValue()))
////                return -1;
////        if((o1).getValue()<( (o2).getValue()))
////            return 1;
////        return 0;
////
////    }
////}
////class valueComparator1 implements Comparator<String> {
////    Map<String,Double> base;
////    public valueComparator1(Map<String,Double>base ){
////        this.base= base;
////    }
////
////    public int compare(String a, String b){
////        // return base.get(a).compareTo(base.get(b));
////        if(base.get(a)>base.get(b))
////            return -1;
////        else return 1;
//
//        // getTitles!
//        listFilesForFolde("C:\\Users\\lihi\\IdeaProjects\\old revisions\\IR-part1\\corpus\\corpus");
//
//
//    }
//
//    private static void listFilesForFolde(String path) throws IOException {
//        // create new file
//        File fileToRead = new File(path);
//        for (final File fileEntry : fileToRead.listFiles()) {
//            if (fileEntry.isDirectory()) { //If the file is a folder- open recursively.
//                listFilesForFolde(fileEntry.getPath());
//            } else if (fileEntry.getName() == "stop_words.txt") {
//                continue;
//            } else { //File to read.
//
//                String encoded = new String(Files.readAllBytes((Paths.get(fileEntry.getPath())))); // Reads the file content and returns it as string.
//                seperateDocs(encoded);
//
//            }
//        }
//    }
//    private static void seperateDocs(String fileContent) throws IOException {
//        Document doc = Jsoup.parse(fileContent);
//        String title;
//        Elements links = doc.select("DOC");
//        String DocNo;
//        ArrayList<String> lists = new ArrayList<>();
//
//
//        for (Element link : links) {
//            DocNo = link.select("DOCNO").text();
//            title = link.select("TI").text();
//            if (title == null || title.equals(""))
//                title = link.select("HEADLINE").text();
//            lists.add(DocNo + ">" + title);
//
//
//        }
//        saveToFile(lists);
//    }
//
//    public static void saveToFile(ArrayList < String > list) {
//        String path;
//        path = "C:\\Users\\lihi\\IdeaProjects\\old revisions\\IR-part1\\postings\\docTitles.txt";
//        File docTitlesFile = new File(path);
//
//        try {
//            FileWriter fw = new FileWriter(docTitlesFile, true);
//            BufferedWriter bw = new BufferedWriter(fw);
//            for (String s : list) {
//                bw.write(s);
//                bw.newLine();
//                bw.flush();
//                bw.flush();
//            }
//            fw.close();
//            bw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
//
