/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package internetofthings;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author kevin
 */
public class Environment {
    
    // attributi funzionali
    // variabili contenente il valore della temperatura e della luminosità inizialmente settati a 0
    private double temperatura = 0, 
                   luminosità  = 0;
    // variabile contatore per tener traccia del numero di lettori 
    private int count;
    // array che conterrà i valori passati dal weatherConditioner()
    protected double valori[];
    // semaforo binario a guardia di count
    private ReentrantLock mutex;
    // semaforo contatore per la gestione dei permessi tra i due metodi
    private Semaphore wrt;

    // inizio metodo costruttore
    public Environment(){
        this.count  = 0;
        this.valori = new double[2];
        this.mutex  = new ReentrantLock();
        this.wrt    = new Semaphore(1, true);
    }// fine metodo costruttore
	
    // metodo misureParameters() per la misurazione dei dati che verranno poi passati ai sensori
    public double[] misureParameters(){
        try{
            // INIZIO SEZIONE CRITICA
            this.mutex.lock();
            // aggiorno il contatore per segnalare la presenza di uno o più lettore
	    this.count++;
            // controllo per capire se è entrato solo un lettore o più di uno e nel caso
            // blocco lo scrittore nel caso stesse scrivendo
	    if(count == 1)
                this.wrt.acquire();
            // FINE SEZIONE CRITICA
	    this.mutex.unlock();
			 
            // inserisco il valore della temperatura in posizione [0] e la luminosità in posizione [1]        
            this.valori[0] = getTemp();
            this.valori[1] = getLum();
	
            // INIZIO SEZIONE CRITICA
            this.mutex.lock();
            // aggiorno il contatore per segnalare che un lettore ha smesso di leggere i dati
	    count--;
            // controllo per capire se non c'è più nessuno a leggere e nel caso lo segnalo allo scrittore
	    if(count == 0)
                this.wrt.release();
            // FINE SEZIONE CRITICA
	    this.mutex.unlock();
        }catch(InterruptedException e) {
            System.out.println(e);
        }
        // ritorno l'array appena creato
        return valori;         
    }// end metodo misureParameters
	
    // metodo updateParameters() invocato dal wwatherConsitioner per modificare i dati
    // inoltre questo metodo forworda l'eccezzione InterruptedException per la terminazione deferita del
    // weatherConditioner. Questo metodo inoltre restituisce come output la temperatuar e la 
    // luminosità aggiornata dalla classe sopra citata
    public void updateParameters(double temp, double lum) throws InterruptedException{
        
        // acquisco il permesso
        this.wrt.acquire();   
        
        // salvo i nuovi valori nelle rispettive variabili
        temperatura = temp;
        luminosità = lum;
        
        // rilascio il permesso
        this.wrt.release();
    }// end metodo updateParameters()
    
    // metodo getLum() per ottenere la luminosità aggiornata
    public double getLum(){
        // ritorna la luminosità
        return luminosità;
    }// fine metodo getLum()
    
    // metodo getTemp() per ottenere la temperatura aggiornata
    public double getTemp(){
        // ritorna la temperatura
        return temperatura;
    }// end metodo getTemp()    
}// end classe Environment()
