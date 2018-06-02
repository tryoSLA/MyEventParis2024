package com.example.paris2024.myevent_paris2024;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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

public class Evenements extends AppCompatActivity implements View.OnClickListener {

    private String email;
    private String id_user;
    private static ArrayList<String> lesEvenements;
    private ListView lvEvenements;
    private Button btVoirMesEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evenements);

        this.email = this.getIntent().getStringExtra("email");
        this.id_user = this.getIntent().getStringExtra("id_user");
        this.lvEvenements = (ListView) findViewById(R.id.idListeEvents);
        this.btVoirMesEvents = (Button) findViewById(R.id.idVoirMesEvenements);

        this.btVoirMesEvents.setOnClickListener(this);

        Toast.makeText(this, email + "  " + id_user, Toast.LENGTH_SHORT).show();

        //declare le this d'activity pour ne pas confondre avec le thread
        final Evenements me = this;
        //execution de la class Asynchrone
        Thread unT = new Thread
                (
                        new Runnable() {
                            @Override
                            public void run() {
                                //instanciation de la tache Asynchrone
                                ExtractionEvents uneExtraction = new ExtractionEvents();
                                uneExtraction.execute();

                                //utilise une synchronisation du thread pf avec le pp
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException exp) {
                                    Log.e("Retard", "retard");
                                }
                                runOnUiThread
                                        (
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (lesEvenements == null) {
                                                            Toast.makeText(me, "Vous n'avez aucun evenement", Toast.LENGTH_LONG).show();
                                                        } else {
                                                            ArrayAdapter unAdapter = new ArrayAdapter(me, android.R.layout.simple_list_item_1, lesEvenements);
                                                            lvEvenements.setAdapter(unAdapter);
                                                        }
                                                    }
                                                }
                                        );
                            }
                        }
                );
        //lancement du processus fils
        unT.start();
    }


    public static ArrayList<String> getLesEvenements() {
        return lesEvenements;
    }

    public static void setLesEvenements(ArrayList<String> lesEvenements) {
        Evenements.lesEvenements = lesEvenements;
    }

    @Override
    public void onClick(View view) {
        Intent unIntent = null;
        unIntent = new Intent(this, MesEvenements.class);
        unIntent.putExtra("email", this.email);
        unIntent.putExtra("id_user", this.id_user);
        startActivity(unIntent);
    }
}


/**********************************Classe Asynchrone Task**************************************************/
class ExtractionEvents extends AsyncTask<Void, Void, ArrayList<String>> {
    @Override
    protected ArrayList<String> doInBackground(Void... strings) {
        String url = "http://192.168.171.177/eco/apiAndroidMyEvent/liste_evenement.php";
        String resultat = null;


        ArrayList<String> uneListe = new ArrayList<String>();

        try {
            URL uneUrl = new URL(url);
            HttpURLConnection uneUrlConnexion = (HttpURLConnection) uneUrl.openConnection();
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

            //ecriture des parametres dans un fichier de sortie
            OutputStream fichier = uneUrlConnexion.getOutputStream();
            BufferedWriter unBuffer = new BufferedWriter
                    (
                            new OutputStreamWriter(fichier, "UTF-8")
                    );

            //on vide le buffer
            unBuffer.flush();
            unBuffer.close();
            fichier.close();

            //lecture de la chaine Json a partir du fichier de lecture
            InputStream fichier2 = uneUrlConnexion.getInputStream();
            BufferedReader unBuffer2 = new BufferedReader(new InputStreamReader(fichier2, "UTF-8"));
            //on defini une chaine dynamique qui lit les chaines Json du fichier
            StringBuilder sb = new StringBuilder();
            String ligne = null;
            while ((ligne = unBuffer2.readLine()) != null) {
                sb.append(ligne);
            }
            //on ferme les buffer et fichier et on affiche resultat
            unBuffer2.close();
            fichier2.close();
            resultat = sb.toString();
            Log.e("Resultat : ", resultat);
        } catch (IOException exp) {
            Log.e("Erreur : ", "Connexion impossible a " + url);
            exp.printStackTrace();
        }

        if (resultat != null) {
            try {
                JSONArray tabJson = new JSONArray(resultat);
                String chaine = "";
                for (int i = 0; i < tabJson.length(); i++) {
                    JSONObject unObjet = tabJson.getJSONObject(i);
                    chaine = unObjet.getString("Titre_event") + "\n"
                            + unObjet.getString("Date_evenement") + "\n\n" +
                            unObjet.getString("Description_event");
                    uneListe.add(chaine);
                }

            } catch (JSONException exp) {
                Log.e("Erreur : \n\t", "\n\t" + exp + "\n\t Impossible de parser : \n\t" + resultat);
            }
        }
        return uneListe;
    }

    @Override
    protected void onPostExecute(ArrayList<String> uneListe) {
        Evenements.setLesEvenements(uneListe);
    }
}


