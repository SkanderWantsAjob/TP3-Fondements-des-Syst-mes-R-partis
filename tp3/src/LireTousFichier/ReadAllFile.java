package LireTousFichier;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.Vector;

public class ReadAllFile {

    private String fileName;

    public ReadAllFile(String path){
        this.fileName = "src/"+path+"/fichier.txt";
    }

    public Vector<String> read() {

        Vector<String> lines = new Vector<>();
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;

            // Lire chaque ligne du fichier jusqu'à la fin
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }

            bufferedReader.close(); // Fermer le lecteur
        } catch (IOException e) {
            System.err.println("Erreur de lecture du fichier: " + e.getMessage());
        }
        return lines ;
    }
}
