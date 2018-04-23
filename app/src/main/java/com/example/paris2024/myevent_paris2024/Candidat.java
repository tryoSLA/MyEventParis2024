package com.example.paris2024.myevent_paris2024;

public class Candidat
{
    private int idCandidat;
    private String email, pseudo, mdp;

    public Candidat (int idCandidat, String pseudo, String email, String mdp)
    {
        this.idCandidat = idCandidat;
        this.pseudo = pseudo;
        this.email = email;
        this.mdp = mdp;
    }

    public Candidat (String pseudo, String email, String mdp)
    {
        this.idCandidat = 0;
        this.pseudo = pseudo;
        this.email = email;
        this.mdp = mdp;
    }

    public int getIdCandidat() {
        return idCandidat;
    }

    public void setIdCandidat(int idCandidat) {
        this.idCandidat = idCandidat;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }
}
