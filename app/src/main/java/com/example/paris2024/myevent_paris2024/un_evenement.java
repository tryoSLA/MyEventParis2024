package com.example.paris2024.myevent_paris2024;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class un_evenement extends AppCompatActivity {

    private String email = "";
    private String id_user = "";
    private String id_event = "";
    private static ArrayList<String> unEvenement ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_un_evenement);

        this.email = this.getIntent().getStringExtra("email");
        this.id_user = this.getIntent().getStringExtra("id_user");
        this.id_event = this.getIntent().getStringExtra("id_event");

        class Extraction extends AsyncTask<String, Void, ArrayList<String>>
        {
            @Override
            protected ArrayList<String> doInBackground(String... strings)
            {
                String url = "http://192.168.0.23/apiAndroidMyEvent/voir_un_evenement.php";
                String resultat = null;

                String unId_user = strings[0];
                ArrayList<String> uneListe = new ArrayList<String>();

                try
                {
                    URL uneUrl = new URL(url);
                    HttpURLConnection uneUrlConnexion = (HttpURLConnection)uneUrl.openConnection();
                    //on fixela methode get
                    uneUrlConnexion.setRequestMethod("GET");
                    //on ouvre l'envoi et la reception des donnees
                    uneUrlConnexion.setDoInput(true);
                    uneUrlConnexion.setDoOutput(true);
                    //on fixe les tempsde connexion et d'attente
                    uneUrlConnexion.setReadTimeout(10000);
                    uneUrlConnexion.setConnectTimeout(15000);
                    //on se connecte
                    uneUrlConnexion.connect();
                    Log.e("connexion : ", "ok");
                    //envoi des parametres
                    String  parametres = "id_event="+id_event;

                    //ecriture des parametres dans un fichier de sortie
                    OutputStream fichier = uneUrlConnexion.getOutputStream();
                    BufferedWriter unBuffer = new BufferedWriter
                            (
                                    new OutputStreamWriter(fichier, "UTF-8")
                            );
                    //ecriture des parametres
                    unBuffer.write(parametres);
                    //on vide le buffer
                    unBuffer.flush();
                    unBuffer.close();
                    fichier.close();

                    //lecture de la chaine Json a partir du fichier de lecture
                    InputStream fichier2 = uneUrlConnexion.getInputStream();
                    BufferedReader unBuffer2 = new BufferedReader(new InputStreamReader(fichier2, "UTF-8"));
                    //on defini une chaine dynamique qui lit les chaines Json du fichier
                    StringBuilder sb  = new StringBuilder();
                    String ligne = null;
                    while ((ligne = unBuffer2.readLine()) != null)
                    {
                        sb.append(ligne);
                    }
                    //on ferme les buffer et fichier et on affiche resultat
                    unBuffer2.close();
                    fichier2.close();
                    resultat = sb.toString();
                    Log.e("Resultat : ", resultat);
                }
                catch (IOException exp)
                {
                    Log.e("Erreur : ", "Connexion impossible a " + url);
                    exp.printStackTrace();
                }

                if (resultat != null)
                {
                    try
                    {
                        JSONArray tabJson = new JSONArray(resultat);
                        String chaine ="";
                        for (int i = 0; i <tabJson.length(); i++)
                        {
                            JSONObject unObjet = tabJson.getJSONObject(i);
                            chaine =unObjet.getString("Titre_event")+"\n"
                                    + unObjet.getString("Date_evenement");//+"\n\n"+
                            //unObjet.getString("Description_event");
                            uneListe.add(chaine);
                        }

                    }
                    catch (JSONException exp)
                    {
                        Log.e("Erreur : ", "\n"+exp+"\n Impossible de parser : \n\t"+resultat);
                    }
                }
                return uneListe;
            }

            @Override
            protected void onPostExecute(ArrayList<String> uneListe)
            {
                un_evenement.setUnEvenement(uneListe);
            }
        }

    }

    public static void setUnEvenement(ArrayList<String> unEvenement) {
        un_evenement.unEvenement = unEvenement;
    }


}
