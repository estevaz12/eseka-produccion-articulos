package ar.com.leo.produccion.model;

/**
 * @author Leo
 */
public class ArticuloProducido {

    private String styleCode;
    private Integer unidades;
    private Double docenas;
    private String produciendo;

    public ArticuloProducido() {
    }

    @Override
    public String toString() {
        return "ArticuloProducido{" +
                "styleCode='" + styleCode + '\'' +
                ", unidades=" + unidades +
                ", docenas=" + docenas +
                ", produciendo='" + produciendo + '\'' +
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
}
