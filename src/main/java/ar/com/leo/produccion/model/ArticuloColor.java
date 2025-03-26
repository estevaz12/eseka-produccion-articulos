package ar.com.leo.produccion.model;

public class ArticuloColor {

    private String numero;
    private String color;
    private String punto;

    @Override
    public String toString() {
        return "ArticuloColor{" +
                "numero='" + numero + '\'' +
                ", color='" + color + '\'' +
                ", punto=" + punto +
                '}';
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getPunto() {
        return punto;
    }

    public void setPunto(String punto) {
        this.punto = punto;
    }

}
