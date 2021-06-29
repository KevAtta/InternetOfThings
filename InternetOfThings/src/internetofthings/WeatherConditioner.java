/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package internetofthings;

/**
 *
 * @author kevin
 */
public class WeatherConditioner extends Thread{
    
    // attributi funzionali per la condivisione dell'oggetto Environment
    Environment ambiente;
    // variabile per settare la luminosità inziale a 0 come da specifica
    private double luminositàIniziale;
    
    // metodo costruttore
    public WeatherConditioner (Environment am){
        super("Condizionatore del tempo");
        this.ambiente           = am;
        this.luminositàIniziale = 0;
    }// end metodo costruttore
    
    // annotazione per il fatto di sovrascrivere il metodo run()
    @Override
    // metodo run() della classe thread
    public void run(){
        // setto la variabile booleana per il whle(true)
        boolean isAlive = true;
        // inizio del while potenzialmente infinito
        while(isAlive){
            try{
                // prendo la luminosità passato e la salvo nella luminosità presente
                this.luminositàIniziale = this.ambiente.valori[1];
                // eseguo i calcoli seguendo le formule fornite nella specifica
                double lum  = this.luminositàIniziale + 1000;
                double temp = (luminositàIniziale * 0.00022) + 10;
                // passo i valori aggiornati al metodo Environment()
                this.ambiente.updateParameters(temp, lum);
                // dormo per 400 ms
                Thread.sleep(400);
            }catch(InterruptedException e){
                // cattura l'eventuale 
                //System.out.println(e);
                // setto la variabile del while() a false cosi da far terminare il weatherConditioner in maniera deferita
                isAlive = false;
            }
        }// fine while    
    }// end metodo run() 
}// end classe WeatherConditioner
