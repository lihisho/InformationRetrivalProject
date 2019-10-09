package engineClasses;

import java.util.Comparator;
import java.util.Map;

public class valueComparator implements Comparator<String> {
    Map<String,Double> base;
    public valueComparator(Map<String,Double>base ){
        this.base= base;
    }

    public int compare(String a, String b){
       // return base.get(a).compareTo(base.get(b));
        if(base.get(a)>base.get(b))
            return -1;
        else return 1;
    }
}
