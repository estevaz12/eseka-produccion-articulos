package ar.com.leo.produccion.model;

public class ArticuloProgramada {

    private String numero;
    private String descripcion;
    private Integer talle;
    private String punto;
    private Integer producir;
    private Integer producido;

    @Override
    public String toString() {
        return "ArticuloProgramada{" +
                "numero='" + numero + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", talle='" + talle + '\'' +
                ", punto=" + punto +
                ", producir=" + producir +
                ", producido=" + producido +
                '}';
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getTalle() {
        return talle;
    }

    public void setTalle(Integer talle) {
        this.talle = talle;
    }

    public Integer getProducir() {
        return producir;
    }

    public void setProducir(Integer producir) {
        this.producir = producir;
    }

    public Integer getProducido() {
        return producido;
    }

    public void setProducido(Integer producido) {
        this.producido = producido;
    }

    public String getPunto() {
        return punto;
    }

    public void setPunto(String punto) {
        this.punto = punto;
    }

}
