package POJO;

/**
* Classe permettant le calcul de la distance de Levenshtein via un algorithme.
* <br>
* source : http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
* 
* @author Wikipedia (juillet 2014)
*/

public class LevenshteinDistance {
    
    /**
    * Permet le calcul de la différence entre deux chaines de caractères via l'application de l'algorithme de Levenshtein.
    * @param s0 la première chaine de caractère à comparer.
    * @param s1 la deuxième chaine de caractère à comparer.
    * @return la distance de Levenshtein sous forme d'entier, avec zéro la plus petite distance possible.
    */
    public static int LevenshteinDistanceAlgo (String s0, String s1) {
            
        s0 = s0.toUpperCase();
        s1 = s1.toUpperCase();
        
	int len0 = s0.length()+1;
	int len1 = s1.length()+1;
 
	// the array of distances
	int[] cost = new int[len0];
	int[] newcost = new int[len0];
 
	// initial cost of skipping prefix in String s0
	for(int i=0;i<len0;i++) cost[i]=i;
 
	// dynamicaly computing the array of distances
 
	// transformation cost for each letter in s1
	for(int j=1;j<len1;j++) {
 
            // initial cost of skipping prefix in String s1
            //newcost[0]=j-1; 
            newcost[0]=j;

            // transformation cost for each letter in s0
            for(int i=1;i<len0;i++) {

                // matching current letters in both strings
                int match = (s0.charAt(i-1)==s1.charAt(j-1))?0:1;

                // computing cost for each transformation
                int cost_replace = cost[i-1]+match;
                int cost_insert  = cost[i]+1;
                int cost_delete  = newcost[i-1]+1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete),cost_replace);
            }
            // swap cost/newcost arrays
            int[] swap=cost; cost=newcost; newcost=swap;
	}
 
	// the distance is the cost for transforming all letters in both strings
	return cost[len0-1];
    }
}
