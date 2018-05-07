package com.example.paris2024.myevent_paris2024;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Menu extends AppCompatActivity implements View.OnClickListener
{
    private Button btEvenements, btMesEvenements;
    private String email ;
    private String id_user ;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        this.email = this.getIntent().getStringExtra("email");
        this.id_user = this.getIntent().getStringExtra("id_user");

        this.btEvenements = (Button) findViewById(R.id.idEvenements);
        this.btMesEvenements = (Button) findViewById(R.id.idMesEvenements);

        this.btMesEvenements.setOnClickListener(this);
        this.btEvenements.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        Intent unIntent = null;
        switch (v.getId())
        {
            case R.id.idEvenements: {
                unIntent = new Intent(this, Evenements.class);
                unIntent.putExtra("email", this.email);
                unIntent.putExtra("id_user", this.id_user);
            }break;
            case R.id.idMesEvenements :
            {
                unIntent = new Intent(this, MesEvenements.class);
                unIntent.putExtra("email", this.email);
                unIntent.putExtra("id_user", this.id_user);
            }break;
        }
        startActivity(unIntent);
    }
}
