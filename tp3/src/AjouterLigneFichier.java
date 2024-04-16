import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class AjouterLigneFichier {

    private String nomFichier;

    public AjouterLigneFichier(String path){
        this.nomFichier = "src/"+path+"/fichier.txt";
    }
    public void ajouterLigne(int ligneNumber){
        try {
            String ligne = ligneNumber +"    Texte "+ligneNumber+" ..." ;
            BufferedWriter writer = new BufferedWriter(new FileWriter(nomFichier, true));
            writer.write(ligne);
            writer.newLine();
            writer.close();
            System.out.println("Ligne ajoutée dans  "+ nomFichier+ "  avec succès.");
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ajout de la ligne au fichier "+nomFichier+" : " + e.getMessage());
        }
    }
    public void ajouterLigne(String ligne){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(nomFichier, true));
            writer.write(ligne);
            writer.newLine();
            writer.close();
            System.out.println("Ligne ajoutée dans  "+ nomFichier+ "  avec succès.");
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ajout de la ligne au fichier "+nomFichier+" : " + e.getMessage());
        }
    }

}
