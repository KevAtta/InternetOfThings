/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package internetofthings;

import java.util.Random;

/**
 *
 * @author kevin
 */
public class Sensor extends Thread{
    
    // attributi funzionali per la condivisione dell'oggetto cloud e ambiente
    private Cloud nuvola;
    private Environment ambiente;
    // attributo per la generazione di un valore casuale per la generazione dell'errore
    private Random rnd;
    // attributo per l'errore
    private int errore;
    // array per passare i valori aggiornati al cloud
    private double valori[];
    private boolean controllo;
    
    // metodo costruttore
    public Sensor(int index, Cloud nu, Environment am){
        super("Sensore_"+index);
        this.nuvola   = nu;
        this.ambiente = am;
        this.valori   = new double [2];
        this.rnd      = new Random();
        this.controllo    = false;
    }// end costruttore
    
    // annotazione per il fatto di sovrascrivere il metodo run()
    @Override
    // metodo run() della classe thread
    public void run(){
        // setto la variabile booleana per il whle(true)
        boolean isAlive = true;
       // inizio del while potenzialmente infinito
       while(isAlive){
        // inizio della try-catch
        try{
            // genero un numero casuale con intervallo [-10,10]
            errore = (rnd.nextInt(21)-10);
            // caso che il valore sia minore di 0
            if(this.errore < 0){
               // rendo positivo l'errore
               this.errore = this.errore * (-1);
               // invoco il metodo misureParameters() della classe ambiente per prendere il valori generati
               this.valori = this.ambiente.misureParameters();
               // modifico i valori di temperatura e luminosità
               double temperatura = (this.valori[0] * (100 - this.errore)) / 100;
               double luminosità  = (this.valori[1] * (100 - this.errore)) / 100;
               // invio i dati al cloud invocando il metodo writeData della suddetta classe
               this.nuvola.writeData(luminosità, temperatura, this.errore, this.controllo);
               // dormo per 400 ms  
               Thread.sleep(400);
            }else{
               // se sono qui vuol dire che il valore di errore generato era positivo
               // invoco il metodo di ambiente per prendere i valori generati
               this.valori = this.ambiente.misureParameters();
               this.controllo = true;
               // modifico i valori di temperatura e luminosità
               double temperatura = (this.valori[0] * (100 + this.errore)) / 100;
               double luminosità  = (this.valori[1] * (100 + this.errore)) / 100;
               // invio i dati al cloud invocando il metodo writeData della suddetta classe
               this.nuvola.writeData(luminosità, temperatura, this.errore, this.controllo);
               // dormo per 400 ms
               Thread.sleep(400);
            }
        }catch(InterruptedException e){
            // cattura l'eventuale eccezione
            //System.out.println(e);
            // setto la variabile del while() a false cosi da far terminare il sensori in maniera deferita
            isAlive = false;
        }
       }// end while
    }// end metodo run()     
}// end classe Sensor()
