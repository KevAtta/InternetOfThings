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
public class User extends Thread{
    
     // attributi funzionali per la condivisione dell'oggetto cloud
    Cloud nuvola;
    // attributo per la generazione di un valore casuale per la sleep 
    Random rnd;
    // attributi per il calcolo della media del tempo di richiesta
    long InizioTempoRichiesta  = 0;
    long fineTempoRichiesta    = 0;
    double tempoRichiesta;
    double tempoMedioPerUtente = 0;
    // variabile contatore
    int count                  = 1;
    
    // metodo costruttore
    public User (int index, Cloud nu){
        super("Utente_"+index);  
        this.rnd            = new Random();
        this.nuvola         = nu; 
        this.tempoRichiesta = 0;
    }// end metodo costruttore
    
    // annotazione per il fatto di sovrascrivere il metodo run()
    @Override
    // metodo run() della classe thread
    public void run(){
    // setto a 0 il tempo di richiesta cossicchè ogni utente non si ritrova la somma dell'utente precedente
    // inizio dei 100 cicli richiesti dalla specifica
    for(int i = 0; i < 100; i++){
    
        // inizio della try-catch per eventuale cattura dell'errore della sleep()
        try{
            // estraggo un numero casuale nell'intervallo [0,99]
            int estratto = rnd.nextInt(100);
            // dormo per il numero estratto pocanzi
            Thread.sleep(estratto);  
            // stampa del numero di richieste effettuate dall'utente
            System.out.println(this.nuvola.getElapsedTime()+"> l'utente "+super.getName()+" richiede i dati per la "+count+" volta");

            // prendo il tempo di inizio richiesta dei dati da parte degli utenti
            long inizioTempoRichiesta = System.currentTimeMillis();

            // invoca il metodo per leggere la temperatura dal cloud
            this.nuvola.readAvarageTemp();
            // invoco il metodo per leggere la luminosità dal cloud
            this.nuvola.readAvarageLight();
            
            // prendo il tempo di fine richiesta dei dati da parte degli utenti
            long fineTempoRichiesta = System.currentTimeMillis();

            // calcolo il tempo di richiesta dell'utente
            tempoRichiesta += fineTempoRichiesta - inizioTempoRichiesta;
            
            // incremento il contatore per tener traccia del numero di richieste effettuato dal cliente
            count++;
            
            // stampo una stringa per capire quando l'utente finisce di leggere i dati
            System.out.println(this.nuvola.getElapsedTime()+"> l'utente "+super.getName()+" termina di leggere");
        }catch(InterruptedException e){
            // cattura l'eventuale eccezione
            System.out.println(e);
        }
    }
    // calcolo il tempo medio per utente
    tempoMedioPerUtente = tempoRichiesta/100; 
    
    // stampo una stringa con il tempo medio
    System.out.println("\nIl cliente "+super.getName()+" HA TERMINATO i suoi 100 cicli");
    
    // stampo una stringa per segnalare che l'utente ha terminato
    System.out.println("L'utente "+super.getName()+" termina...");
    }// end metodo run()     
}//end classe User()
