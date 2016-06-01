package com.pshelf.hugo.pshelf.imageutils;

/**
 * Created by HFLopes on 01/03/2016.
 */
public class ImageDeteccoes {


    private int id;
    private String caminho;
    private int faces;
    private double luminosidade;
    private String data;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLuminosidade() {
        return luminosidade;
    }

    public void setLuminosidade(double luminosidade) {
        this.luminosidade = luminosidade;
    }

    public void setCaminho(String caminho) {
        this.caminho = caminho;
    }

    public void setFaces(int faces) {
        this.faces = faces;
    }

    public String getCaminho() {
        return caminho;
    }

    public int getFaces() {
        return faces;
    }
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
