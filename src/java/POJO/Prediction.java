
package POJO;

import java.util.ArrayList;
import org.apache.commons.math3.analysis.function.Exp;
import org.apache.commons.math3.analysis.function.Log;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.Covariance;

/**
 *
 * @author GGR
 */


public class Prediction {
    
    private String supplier;

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    
    private double price;

    private String devise = "";
    
    private final ArrayList<Prix> listePrix;

    public ArrayList<Prix> getListePrix() {
        return listePrix;
    }
    
    public void addPrice(Prix price) {
        
        this.listePrix.add(price);
        
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public double getCoeffa() {
        return coeffa;
    }

    public void setCoeffa(double coeffa) {
        if (coeffa == Double.POSITIVE_INFINITY || coeffa == Double.NEGATIVE_INFINITY || Double.isNaN(coeffa) ) {
           this.coeffa = 0;  
        }else{
            this.coeffa = coeffa;
        }
    }

    public double getCoeffk() {
        return coeffk;
    }

    public void setCoeffk(double coeffk) {
        // protection car Windev ne gere pas bien les infinity...
        if (coeffk == Double.POSITIVE_INFINITY || coeffk == Double.NEGATIVE_INFINITY || Double.isNaN(coeffk) ) {
           this.coeffk = 0; 
        }else{
            this.coeffk = coeffk;
        }
    }
    
    private double coeffa;
    private double coeffk;

      
    private final String infoFormula = "exp(_a*(ln(quantite) - moyX) + moyY)";

    public String getInfoFormula() {
        return infoFormula;
    }
     
     private double moyX;

    public double getMoyX() {
        return moyX;
    }

    public void setMoyX(double moyX) {
        if (moyX == Double.POSITIVE_INFINITY || moyX == Double.NEGATIVE_INFINITY || Double.isNaN(moyX) ) {
            this.moyX = 0;
        }else{
            this.moyX = moyX;
        }
    }

    public double getMoyY() {
        
        return moyY;
    }

    public void setMoyY(double moyY) {
        if (moyY == Double.POSITIVE_INFINITY || moyY == Double.NEGATIVE_INFINITY || Double.isNaN(moyY) ) {
            this.moyY = 0;
        }else{
            this.moyY = moyY;
        }
    }
     private double moyY;
     
     
    /**
    * Constructeur de Prediction vide.
    */
    public Prediction() { 
        listePrix = new ArrayList<Prix>();
    }
    
    public void calculPrediction(double Qty) {
         //
        if (this.listePrix.size() > 1 ) {
            //
            Covariance co = new Covariance();
            Exp exp = new Exp();
            Log log = new Log();
            //
            // 1. On construit les tableaux Qté (X) & Prix (Y), on calcul la somme des quantité & prix (tout en log)
            int nbPriceOKPrediction = 0;
            double tabQty [] = new double [getListePrix().size()]; // log des quantités
            double tabPrice [] = new double [getListePrix().size()]; // log des prix
            double sumX = 0; // somme des logs
            double sumY = 0; // somme des logs

            for (Prix supplierPrice : getListePrix()) {
                //
                if ( supplierPrice.getPrix() > 0) {
                    //
                    tabQty[nbPriceOKPrediction] = log.value(supplierPrice.getQuantite());
                    tabPrice[nbPriceOKPrediction] = log.value(supplierPrice.getPrix());
                    sumX += log.value(supplierPrice.getQuantite());
                    sumY += log.value(supplierPrice.getPrix());
                    //
                    if (getDevise().equals("")) {
                        setDevise(supplierPrice.getDevise());
                    }
                    //
                    nbPriceOKPrediction++;
                }
            }
            // 
            // Il faut au moins deux prix valide (farnell renvoi des prix a 0. .....)
            if (nbPriceOKPrediction > 1) {
                //
                // Calcul de la variance des X (Quantité des prix)
                // Calcul de la covariance XY 
                // Calcul de la moyenne des Quantités & prix
                // Calcul des coefficient a & k
                double varX = StatUtils.variance(tabQty);
                double covarXY = co.covariance(tabQty, tabPrice);
                setMoyX(sumX / nbPriceOKPrediction);
                setMoyY(sumY / nbPriceOKPrediction);
                //
                if (varX != 0 ) {
                    setCoeffa(covarXY / varX);
                }else {
                    setCoeffa(0);
                }
                //
                setCoeffk(exp.value(((-getCoeffa()* getMoyX()) + getMoyY())));
                //
                if (Qty > 0 ) {
                    //
                    // Calcul du prix interpolé pour la quantité passé en paramètre
                    setPrice(exp.value( (getCoeffa()*(log.value(Qty) - getMoyX())+ getMoyY())));
                    //
                }else {
                    //
                    setPrice(0);
                    //
                }
                //
            } else {
                //
                setCoeffa(0);
                setCoeffk(0);
                setPrice(0);
                //
            }
           
            //
        }else {
            //
            setCoeffa(0);
            setCoeffk(0);
            setPrice(0);
            //
        }
    }
}
