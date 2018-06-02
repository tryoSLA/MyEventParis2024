package com.example.paris2024.myevent_paris2024;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
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

public class MesEvenements extends AppCompatActivity implements View.OnClickListener{

    private String email;
    private String id_user;
    private ListView lvEvenements ;
    private static ArrayList<String> mesEvenements ;
    private Button btVoirLesEvents;
    private Integer item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mes_evenements);
        this.email = this.getIntent().getStringExtra("email");
        this.id_user = this.getIntent().getStringExtra("id_user");
        this.lvEvenements = (ListView) findViewById(R.id.idListe);
        this.btVoirLesEvents = (Button) findViewById(R.id.idVoirLesEvenements);

        this.btVoirLesEvents.setOnClickListener(this);
        this.lvEvenements.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String selectedItem = (String) adapterView.getItemAtPosition(i);
                System.out.println("nom  "+selectedItem+ "  Id :   "+i);
            }
        });
//        this.lvEvenements.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<String> adapterView, View view, int i, long l) {
//
//                item = lvEvenements.getCheckedItemPosition();
//
//                Toast.makeText(this,item,Toast.LENGTH_SHORT).show();
//                Toast.makeText(this, email + "  " +id_user, Toast.LENGTH_SHORT).show();
//
//                // item = adapter.getItemAtPosition(position);
//
//                //Intent unIntent = new Intent(this, unEvenement.class);
//                //startActivity(unIntent);
//            }
//        });

        Toast.makeText(this, email + "  " +id_user, Toast.LENGTH_SHORT).show();

        //declare le this d'activity pour ne pas confondre avec le thread
        final MesEvenements me = this;
        //execution de la class Asynchrone
        Thread unT = new Thread
                (
                    new Runnable()
                    {
                        @Override
                        public void run()
                        {
                        //instanciation de la tache Asynchrone
                        Extraction  uneExtraction = new Extraction();
                        uneExtraction.execute(id_user);

                        //utilise une synchronisation du thread pf avec le pp
                        try{
                            Thread.sleep(1000);
                        }
                        catch(InterruptedException exp)
                        {
                            Log.e("Retard", "retard");
                        }
                        runOnUiThread
                            (
                                new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        if (mesEvenements == null)
                                        {
                                            Toast.makeText(me, "Vous n'avez aucun evenement", Toast.LENGTH_LONG).show();
                                        }
                                        else
                                        {
                                            ArrayAdapter unAdapter = new ArrayAdapter(me, android.R.layout.simple_list_item_1, mesEvenements);
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

    @Override
    public void onClick(View v) {
        Intent unIntent = null;
        unIntent = new Intent(this, Evenements.class);
        unIntent.putExtra("email", this.email);
        unIntent.putExtra("id_user", this.id_user);
        startActivity(unIntent);

        int position=(Integer)v.getTag();

        System.out.println(position);
    }


    public static ArrayList<String> getMesEvenements() {
        return mesEvenements;
    }

    public static void setMesEvenements(ArrayList<String> mesEvenements) {
        MesEvenements.mesEvenements = mesEvenements;
    }


    /**********************************Classe Asynchrone Task**************************************************/
    class Extraction extends AsyncTask<String, Void, ArrayList<String>>
    {
        @Override
        protected ArrayList<String> doInBackground(String... strings)
        {
            String url = "http://192.168.171.177/eco/apiAndroidMyEvent/voir_mes_evenement.php";
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
                String  parametres = "id_user="+unId_user;

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
                                    + unObjet.getString("Date_evenement")+"\n\n"+
                                    unObjet.getString("Description_event");
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
            MesEvenements.setMesEvenements(uneListe);
        }
    }

}
