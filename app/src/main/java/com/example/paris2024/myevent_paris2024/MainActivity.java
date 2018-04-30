package com.example.paris2024.myevent_paris2024;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private Button btConnecter, btAnnuler;
    private EditText txtEmail, txtMdp;
    private static Candidat leCandidat = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        this.btAnnuler = (Button) findViewById(R.id.idAnnuler);
        this.btConnecter = (Button) findViewById(R.id.idConnecter);
        this.txtMdp = (EditText) findViewById(R.id.idMdp);
        this.txtEmail = (EditText) findViewById(R.id.idEmail);

        this.btAnnuler.setOnClickListener(this);
        this.btConnecter.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.idAnnuler:
                this.txtEmail.setText("");
                this.txtMdp.setText("");
                break;

            case R.id.idConnecter:
            {
                //verification de la connexion via l'api php
                String email = this.txtEmail.getText().toString();
                String mdp = this.txtMdp.getText().toString();

                final Candidat unCandidat = new Candidat("", email, mdp);

                //declare le this d'activity pour ne pas confondre avec le thread
                final MainActivity ma = this;
                //execution de la class Asynchrone
                Thread unT = new Thread
                        (
                                new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        //instanciation de la tache Asynchrone
                                        Conn  uneConnexion = new Conn();
                                        uneConnexion.execute(unCandidat);
                                        //test de la verif de connexion
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
                                                                if (leCandidat == null)
                                                                {
                                                                    Toast.makeText(ma, "Verifier les identifiants", Toast.LENGTH_LONG).show();
                                                                }
                                                                else
                                                                {
                                                                    Toast.makeText(ma, "Bienvenue "+leCandidat.getPseudo(), Toast.LENGTH_LONG).show();
                                                                    Intent unIntent = new Intent(ma, Menu.class);
                                                                    unIntent.putExtra("email", leCandidat.getEmail());
                                                                    startActivity(unIntent);
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
            break;
        }
    }

    public static Candidat getLeCandidat() {
        return leCandidat;
    }

    public static void setLeCandidat(Candidat leCandidat) {
        MainActivity.leCandidat = leCandidat;
    }

}

/**********************************Classe Asynchrone Task**************************************************/
class Conn extends AsyncTask<Candidat, Void, Candidat>
{
    @Override
    protected Candidat doInBackground(Candidat... candidats)
    {
        String url = "http://projet26.entreprise.lan/apiAndroidMyEvent/connexion_user.php";
        String resultat = null;

        Candidat unCandidat = candidats[0];
        Candidat candidatConnecte = null;

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
            String  parametres = "email="+unCandidat.getEmail();
            parametres += "&mdp="+unCandidat.getMdp();
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
                JSONObject unObjet = tabJson.getJSONObject(0);
                int nb = unObjet.getInt("nb");
                if (nb >= 1)
                {
                    candidatConnecte = new Candidat
                            (
                                    unCandidat.getPseudo(),
                                    unCandidat.getMdp(),
                                    unCandidat.getEmail()
                            );
                }
            }
            catch (JSONException exp)
            {
                Log.e("Erreur : ", "Impossible de parser : "+resultat);
            }
        }
        return candidatConnecte;
    }

    @Override
    protected void onPostExecute(Candidat candidat)
    {
        MainActivity.setLeCandidat(candidat);
    }
}
