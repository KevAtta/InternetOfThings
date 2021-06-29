/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package internetofthings;

import java.text.DecimalFormat;
import java.util.Scanner;

/**
 *
 * @author kevin
 */
public class InternetOfThings {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // creo l'oggetto Cloud da condividere
        Cloud nu                  = new Cloud(30);
        // creo l'oggetto Environment da condividere
        Environment am            = new Environment();
        // creo il weatherConditioner
        WeatherConditioner cn     = new WeatherConditioner(am);
        // creo l'oggetto scanner per prendere il numero di sensori ed utenti in input
        Scanner scanner           = new Scanner(System.in);
        // quseto servirà per la formattazione delle cifre inerenti alla stampa del tempo medio e della
        // deviazione standard
        DecimalFormat df          = new DecimalFormat();
        // booleano necessario per la validazione dell'input
        boolean input;
        // numero di utenti inizialmente settato a 0
        int nUtenti               = 0;
        // numero di sensori inzialmente settato a 0
        int nSensori              = 0;
        // variabile inerente al totale del tempo medio necessario per il calcolo 
        // della media e della deviazione standard, settato inizialmente a 0
        double tempoMedioTotale   = 0;
        // numeratore della varianza necessario per il calcolo della deviazione 
        // standard, settato inizialmente a 0
        double numeratoreVarianza = 0;
        // deviazione standard settata inizialmente a 0
        double deviazioneStandard = 0;
        
        // stampa del messaggio di inizio simulazione
        System.out.println("************************************************************************");
        System.out.println("* Inizio simulazione Esame Sistemi Operativi sessione Estiva 2020/2021 *");
        System.out.println("*                      INTERNET OF THINGS                              *");
        System.out.println("************************************************************************");
        System.out.println();
        System.out.println("Inserire il numero degli utenti desiderati e successivamente il numero di sensori");
        
        // ciclo di validazione dell'input
        do
        {            
            try
            {              
                // input impostato inizialmente a false
                input = false;                         
                // inserimento del numero di utenti
                System.out.print("Inserisci il numero di utenti: ");
                nUtenti = Integer.parseInt(scanner.next());
                // inserimento del numero di sensori
                System.out.print("Inserisci il numero di sensori: ");
                nSensori = Integer.parseInt(scanner.next()); 
            }
            catch(IllegalArgumentException ex)
            {     
                // controllo dell'input
                input = (ex instanceof NumberFormatException) ? true : false;
                System.out.println("Errore di input! Prego inserire un intero");                                                                 
            }            
        }
        while(input); 
        // avvio del thread weatherConditioner
        cn.start();
        // creo l'array di utenti di grandezza pari al valore inserito dall'utente
        User utenti[] = new User[nUtenti];
        // creo l'array di sensori di grandezza pari al valore inserito dall'utente
        Sensor sensori[] = new Sensor[nSensori];   
                
        // ora inizializzo ogni sensore e lo faccio partire 
        for(int i = 0; i < sensori.length; i++){
            sensori[i] = new Sensor(i, nu, am);
            sensori[i].start();
        }        
        // ora inizializzo ogni utente e lo faccio partire
        for(int i = 0; i < utenti.length; i++){
            utenti[i] = new User(i, nu);
            utenti[i].start();
        }
        
        // il thread main si mette in attesa per la terminazione dei thread figli
        try
        {
            // faccio terminare gli utenti
            for(int i = 0; i < utenti.length; i++)
                utenti[i].join();
            // faccio terminare i sensori in maniera deferita
            for(int i = 0; i < sensori.length; i++)
                sensori[i].interrupt();
            for(int i = 0; i < sensori.length; i++)
                sensori[i].join();
            // faccio terminare il weatherConditioner in maniera deferita
            cn.interrupt();
            cn.join();
        } catch (InterruptedException ex) {
            // stampo eventuali eccezioni
            System.out.println(ex);
        }
        // testo di fine simulazione
        System.out.println("\nI sensori terminano con successo!");
        System.out.println("Simulazione terminata con successo!");
        System.out.println();
        
        // formattazione dei valori a massimo 2 cifre dopo la virgola
        df.setMaximumFractionDigits(2);
        // stampa della sezione relativa ai tempi di attesa
        System.out.println("TEMPI DI ATTESA PER LA RICHIESTA DEI DATI DA PARTE DEGLI UTENTI:");
        System.out.println();
        
        // calcolo del tempo medio 
        for(int i = 0; i < utenti.length; i++){
            tempoMedioTotale += utenti[i].tempoMedioPerUtente;
        }
        // il risultato appena ottenuto lo divido per gli utenti e lo salvo nella corrispondente variabile
        double tempoMedio = tempoMedioTotale/utenti.length;
        // stampa del tempo medio
        System.out.println("GLI UTENTI HANNO UN TEMPO MEDIO DI RICHIESTA PARI A: "+df.format(tempoMedio)+"ms");
        
        // vado a calcolare il numeratore della varianza attraverso la libreria matematica
        for(int i = 0; i < utenti.length; i++){
            numeratoreVarianza += Math.pow((utenti[i].tempoMedioPerUtente - tempoMedio),2);
        }
        // calcolo ora la deviazione standard sempre tramite la libreria matematica
        deviazioneStandard = Math.sqrt(numeratoreVarianza/utenti.length);
        // stampo la deviazione standard
        System.out.println("DEVIAZIONE STANDARD: "+df.format(deviazioneStandard)+"ms");
    }// end main()    
}// end classe
