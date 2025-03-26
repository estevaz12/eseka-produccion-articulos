package ar.com.leo.produccion.model;

import java.util.Arrays;

public class ArticuloProducido {

    private String styleCode;
    private String numero;
    private String punto;
    private Integer talle;
    private String color;
    private Integer unidades;
    private Double docenas;
    private String produciendo;
    private int idealCycle;
    private Integer producir;
    private String[] maquinas;

    @Override
    public String toString() {
        return "ArticuloProducido{" +
                "styleCode='" + styleCode + '\'' +
                ", unidades=" + unidades +
                ", docenas=" + docenas +
                ", produciendo='" + produciendo + '\'' +
                ", idealCycle=" + idealCycle +
                ", punto=" + punto +
                ", producir=" + producir +
                ", maquinas=" + Arrays.toString(maquinas) +
                '}';
    }

    public Integer getUnidades() {
        return unidades;
    }

    public void setUnidades(Integer unidades) {
        this.unidades = unidades;
    }

    public Double getDocenas() {
        return docenas;
    }

    public void setDocenas(Double docenas) {
        this.docenas = docenas;
    }

    public String getStyleCode() {
        return styleCode;
    }

    public void setStyleCode(String styleCode) {
        this.styleCode = styleCode;
    }

    public String getProduciendo() {
        return produciendo;
    }

    public void setProduciendo(String produciendo) {
        this.produciendo = produciendo;
    }

    public String getPunto() {
        return punto;
    }

    public void setPunto(String punto) {
        this.punto = punto;
    }

    public Integer getProducir() {
        return producir;
    }

    public void setProducir(Integer producir) {
        this.producir = producir;
    }

    public int getIdealCycle() {
        return idealCycle;
    }

    public void setIdealCycle(int idealCycle) {
        this.idealCycle = idealCycle;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setTalle(Integer talle) {
        this.talle = talle;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getColor() {
        return color;
    }

    public Integer getTalle() {
        return talle;
    }

    public String getNumero() {
        return numero;
    }

    public String[] getMaquinas() {
        return maquinas;
    }

    public void setMaquinas(String[] maquinas) {
        this.maquinas = maquinas;
    }

}
