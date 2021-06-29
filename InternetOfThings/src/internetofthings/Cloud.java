/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package internetofthings;

import java.text.DecimalFormat;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author kevin
 */
public class Cloud {
    
    // attributi funzionali 
    // array interni che implemetano il buffer circolare
    private double bufferLum[]; 
    private double bufferTemp[];
    
    // puntatori logici di inserimento e rimozione per i due buffer
    private int inL, outL;
    private int inT, outT;
    
    // numero di elementi all'interno dei due buffer circolari
    private int elementInBufferLum;
    private int elementInBufferTemp;
    
    // attributi di sincronizzazione
    // semafori per la mutua esclusione a guardia di in, out
    private ReentrantLock lockL;
    private ReentrantLock lockT;
    
    // semaforo per far andare i clienti ed i sensori in ordine FIFO
    private Semaphore semT;
    private Semaphore semL;
    private Semaphore semS;
    
    // semafori che sospenderanno il produttore quando il buffer sarà pieno
    // e i consumatori quando il buffer sarà vuoto
    private Condition notFullLum;
    private Condition notEmptyLum;
    private Condition notFullTemp;
    private Condition notEmptyTemp;
    
    // variabile per il calcolo del tempo trascorso durante l'esecuzione
    private final long startTime;
    
    // semaforo per il calcolo del tempo trascorso durante l'esecuzione a guardia di startTime
    private ReentrantLock timeMutex;
    
    // varibaile per la formattazione delle cifre inerenti alla temperatura e alla luminosità
    private DecimalFormat df;

    // inizio metodo costruttore
    public Cloud(int size){
        this.bufferLum           = new double[size];
        this.bufferTemp          = new double[size];
        
        this.inL                 = 0;
        this.outL                = 0;
        this.inT                 = 0;
        this.outT                = 0;
        this.elementInBufferLum  = 0;
        this.elementInBufferTemp = 0;
        
        this.lockL               = new ReentrantLock();
        this.lockT               = new ReentrantLock();
        this.semT                = new Semaphore(1, true);
        this.semL                = new Semaphore(1, true);
        this.semS                = new Semaphore(1, true);
        this.notFullLum          = this.lockL.newCondition();
        this.notEmptyLum         = this.lockL.newCondition();
        this.notEmptyTemp        = this.lockT.newCondition();
        this.notFullTemp         = this.lockT.newCondition();
        
        this.timeMutex           = new ReentrantLock();
        this.startTime           = System.currentTimeMillis();
        
        this.df = new DecimalFormat("##,###");
    }// fine metodo costruttore
    
    // metodo WriteData per l'inserimento dei dati nel buffer da parte del/dei produttore/i (Sensori)
    // il metodo inoltre, oltre a prendere in ingresso i dati inerenti a temperatura e luminosità
    // forworderà anche l'eccezione per la terminazione deferita per il/i produttore/i
    public void writeData(double lum, double temp, int errore, boolean controllo) throws InterruptedException{
        // setto la formattazione a massimo 2 cifre dopo la virgola
        this.df.setMaximumFractionDigits(2);
        // acquisisco il permesso per l'ordine FIFO dei sensori
        this.semS.acquire();
        // INIZIO SEZIONE CRITICA
        // acquisisco il permesso per la mutua esclusione
        this.lockL.lock();
        try{
            // while per fermare il produttore qual'ora il buffer della luminosità fosse pieno. Inoltre è stato
            // scelto un while al posto di un if per rendere il codice più robusto
            while(this.elementInBufferLum == this.bufferLum.length){
                //System.out.println("\nIl sensore attende perchè il buffer lum è pieno\n");
                // faccio una await su notFullLum fermando cosi il produttore
                this.notFullLum.await();                
            }
            // se sono qui significa che il buffer non è pieno
            // incremento gli elementi nel buffer inerente alla luminosità di 1
            this.elementInBufferLum++;
            // inserisco il valore della luminosità
            this.bufferLum[this.inL] = lum;
            // valuto se il valore è decrementato o incrementato
            if(controllo == false){
                // stampo una stringa dove evidenzia il valore inserito ed in che posizione
                System.out.println("E' stato inserito il valore di luminosità ["+this.bufferLum[this.inL]+
                                    "] in posizione ["+this.inL+"] con errore decrementato pari a "+errore+"%");
            }else{
                // stampo una stringa dove evidenzia il valore inserito ed in che posizione
                System.out.println("E' stato inserito il valore di luminosità ["+this.bufferLum[this.inL]+
                                    "] in posizione ["+this.inL+"] con errore incrementato pari a "+errore+"%");
            }
            // aggiorno il puntatore logico inerente alla luminosità
            this.inL = (this.inL+1)%this.bufferLum.length;
            // faccio una signal su notEmptyLum cosi da segnalare al consumatore il fatto
            // che si è inserito un elemento in questo buffer
            this.notEmptyLum.signal();             
        }finally{
            // FINE SEZIONE CRITICA
            // rilascio il permesso
            this.lockL.unlock();
        }
        
        // INIZIO SEZIONE CRITICA
        // acquiscio il permesso per la mutua esclusione
        this.lockT.lock();
        try{
            // while per fermare il produttore qual'ora il buffer della temperatura fosse pieno. Inoltre è stato
            // scelto un while al posto di un if per rendere il codice più robusto
            while(this.elementInBufferTemp == this.bufferTemp.length)
                //System.out.println("\nIl sensore attende perchè il buffer temp è pieno\n");
                // faccio una await su notFullTemp fermando cosi il produttore
                this.notFullTemp.await();
            // se sono qui significa che il buffer non è pieno
            // incremento gli elementi nel buffer inerente alla temperatura di 1
            this.elementInBufferTemp++;
            // inserisco il valore della temperatura
            this.bufferTemp[this.inT] = temp;
            // valuto se il valore è decrementato o incrementato
            if(controllo == false){
                // stampo una stringa dove evidenzia il valore inserito ed in che posizione
                System.out.println("E' stato inserito il valore di temperatura ["+df.format(this.bufferTemp[this.inT])+
                                    "] in posizione ["+this.inT+"] con errore decrementato pari a "+errore+"%");
            }else{
                // stampo una stringa dove evidenzia il valore inserito ed in che posizione
                System.out.println("E' stato inserito il valore di temperatura ["+df.format(this.bufferTemp[this.inT])+
                                    "] in posizione ["+this.inT+"] con errore incrementato pari a "+errore+"%");
            }
            // aggiorno il puntatore logico inerente alla temperatura
            this.inT = (this.inT+1)%this.bufferTemp.length;
            // faccio una signal su notEmptyTemp cosi da segnalare al consumatore il fatto
            // che si è inserito un elemento in questo buffer
            this.notEmptyTemp.signal();        
        }finally{
            // FINE SEZIONE CRITICA
            // rilascio il permesso
            this.lockT.unlock();
        }    
        // rilascio il permesso precedentemente acquisito da parte dei sensori
        this.semS.release();
    
    }// end metodo WriteData()
    
    // metodo readAvarageLight() per la lettura, da parte del/i consumatore/i (utenti), del 
    // valore della luminosità
    public void readAvarageLight(){
        // dichiarazione variabili locali per somma e media dei valori contenuti nel buffer
        double somma = 0, 
               media;
        // la variabile item mi serve per tenere traccia del puntatore out cosi da riuscire a 
        // prelevare 4 valori consectivi precedentmente inseriti nel buffer
        int    item;
        
        try{
            // acquisco il permesso in ordine FIFO per far entrare in questa sezione un utente per volta
            this.semL.acquire();
            // INIZIO SEZIONE CRITICA
            // acquisisco il permesso
            this.lockL.lock();
            // while() per far attendere il consumatore qual'ora non ci fossero ancora 4 elementi nel buffer
            while(this.elementInBufferLum < 4){
                //System.out.println("\nL'utente attende perchè il buffer non ha ancora 4 elementi\n");
                // faccio una await bloccando cosi il consumatore
                this.notEmptyLum.await();
            }
            // se sono qui vuol dire che ci sono almeno 4 elementi nel buffer
            // assegno alla variabile item il valore del puntatore logico outL
            item = this.outL;
            // ciclo per la somma dei 4 elementi consecutivi con relativo aggiornamento della variabile item
            for (int i = 0; i < 4; i++){
                somma += this.bufferLum[item];
                item = (item+1)%30;
            }
            // faccio la media dei 4 valori consecutivi
            media = somma/4;
            // incremento il putnatore outL di 4 posizioni
            this.outL = (this.outL+4)%this.bufferLum.length;
            // decremento il numero di elementi in questo buffer di 4
            this.elementInBufferLum -=4;
            // stampo a video la media dei valori precedentemente sommati e mediati
            System.out.println("La media di lum è -> "+df.format(media));
            // faccio la signal su notFullLum per avvisare il produttore che sono stati
            // rimossi gli elementi da questo buffer
            this.notFullLum.signal(); 
            // rilascio il permesso precedentemente acqusiito
            this.semL.release();
        }catch(InterruptedException e){
            // stampo l'eventuale eccezione
            System.out.println(e);        
        }finally{
            // FINE SEZIONE CRITICA
            // rilascio il permesso
            this.lockL.unlock();
        }    
    }// end readAvarageLight
    
    // metodo readAvarageTempo() per la lettura per la lettura, da parte del/i consumatore/i (utenti), del 
    // valore della temperatura
    public void readAvarageTemp(){
        // dichiarazione variabili locali per somma e media dei valori contenuti nel buffer
        double somma = 0, 
               media;
        // la variabile item mi serve per tenere traccia del puntatore out cosi da riuscire a 
        // prelevare 4 valori consectivi precedentmente inseriti nel buffer
        int    item;
        // setto la formattazine del valore della temperatura a massimo 2 cifre dopo la virgola
        this.df.setMaximumFractionDigits(2);
         
        try{
            // acquisisco un permesso in ordine FIFO per far entrare in questa sezione un utente per volta
            this.semT.acquire();
            // INIZIO SEZIONE CRITICA
            // acquisico il permesso
            this.lockT.lock();
            // while() per far attendere il consumatore qual'ora non ci fossero ancora 4 elementi nel buffer
            while(this.elementInBufferTemp < 4){
                //System.out.println("\nL'utente attende perchè il buffer non ha ancora 4 elementi\n");
                // faccio una await bloccando cosi il consumatore
                this.notEmptyTemp.await();
            }
            // se sono qui vuol dire che ci sono almeno 4 elementi nel buffer
            // assegno alla variabile item il valore del puntatore logico outT
            item = this.outT;
            // ciclo per la somma dei 4 elementi consecutivi con relativo aggiornamento della variabile item
            for (int i = 0; i < 4; i++){
                somma += this.bufferTemp[item];
                item = (item+1)%30;
            }
            // faccio la media dei 4 valori consecutivi
            media = somma/4;
            // incremento il putnatore outT di 4 posizioni
            this.outT = (this.outT+4)%this.bufferTemp.length;
            // decremento il numero di elementi in questo buffer di 4
            this.elementInBufferTemp -=4;
            // stampo a video la media dei valori precedentemente sommati e mediati
            System.out.println("La media di temp è -> "+df.format(media));
            // faccio la signal su notFullTemp per avvisare il produttore che sono stati
            // rimossi gli elementi da questo buffer
            this.notFullTemp.signal();
            // rilascio il permesso precedentemente acqusiito
            this.semT.release();
        }catch(InterruptedException e){
            // stampo l'eventuale eccezione
            System.out.println(e);
        }finally{
            // FINE SEZIONE CRITICA
            // rilascio il permesso
            this.lockT.unlock();
        }    
    }// end readAvarageTemp
    
    // metodo per il calcolo del tempo trascorso durante l'esecuzione
    public long getElapsedTime(){ 
        // INIZIO SEZIONE CRITICA
        // acquisisco il permesso
        this.timeMutex.lock();   
        try{
            // ritorno il tempo trascorso ottenendolo facendo il tempo trascorso da quando è partita
            // la simulazione meno quello corrente
            return System.currentTimeMillis() - this.startTime;
        }finally{
            // FINE SEZIONE CRITICA
            // rilascio il permesso
            this.timeMutex.unlock();
        }
    }// end metodo getElapsedTime()    
}// end classe Cloud()
