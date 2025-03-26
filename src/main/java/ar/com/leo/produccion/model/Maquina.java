package ar.com.leo.produccion.model;

/**
 * @author Leo
 */
public class Maquina {

    private Integer machCode;
    private String groupCode;
    private Integer state;
    //    private Integer timeOn;
//    private Integer timeOff;
//    private Integer functionKey;
    private Integer roomCode;
    private String styleCode;
    //    private Integer lastTimeOn;
//    private Integer lastTimeOff;
    private Integer pieces;
    private Integer targetOrder;
    //    private Integer shiftPieces;
//    private Integer lastPieces;
    //    private Integer bagPieces;
//    private Integer bagTarget;
    private Integer idealCycle;
//    private Integer lastCycle;
    //    private Integer lastStopCode;
//    private Integer step;
//    private Integer discards;
//    private Integer shift;
    //    private Integer stopFrequency;
//    private Integer pairCode;
//    private Integer shiftDiscards;
    //    private Integer lastDiscards;
//    private String dateStartShift;
    private Integer needles;
    private Integer diameterCylinder;
    private String modelCode;

    private int produccion;
    private int disponible;
    private int cantidadProduccion;
    private int ultimoCambio;

    public Maquina() {
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public Integer getMachCode() {
        return machCode;
    }

    public void setMachCode(Integer machCode) {
        this.machCode = machCode;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(Integer roomCode) {
        this.roomCode = roomCode;
    }

    public String getStyleCode() {
        return styleCode;
    }

    public void setStyleCode(String styleCode) {
        this.styleCode = styleCode;
    }

    public Integer getPieces() {
        return pieces;
    }

    public void setPieces(Integer pieces) {
        this.pieces = pieces;
    }

    public Integer getTargetOrder() {
        return targetOrder;
    }

    public void setTargetOrder(Integer targetOrder) {
        this.targetOrder = targetOrder;
    }

//    public Integer getShiftPieces() {
//        return shiftPieces;
//    }
//
//    public void setShiftPieces(Integer shiftPieces) {
//        this.shiftPieces = shiftPieces;
//    }
//
//    public Integer getLastPieces() {
//        return lastPieces;
//    }
//
//    public void setLastPieces(Integer lastPieces) {
//        this.lastPieces = lastPieces;
//    }

    public Integer getIdealCycle() {
        return idealCycle;
    }

    public void setIdealCycle(Integer idealCycle) {
        this.idealCycle = idealCycle;
    }

    public Integer getNeedles() {
        return needles;
    }

    public void setNeedles(Integer needles) {
        this.needles = needles;
    }

    public Integer getDiameterCylinder() {
        return diameterCylinder;
    }

    public void setDiameterCylinder(Integer diameterCylinder) {
        this.diameterCylinder = diameterCylinder;
    }

    public String getModelCode() {
        return modelCode;
    }

    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    public int getProduccion() {
        return produccion;
    }

    public void setProduccion(int produccion) {
        this.produccion = produccion;
    }

    public int getDisponible() {
        return disponible;
    }

    public void setDisponible(int disponible) {
        this.disponible = disponible;
    }

    public int getCantidadProduccion() {
        return cantidadProduccion;
    }

    public void setCantidadProduccion(int cantidadProduccion) {
        this.cantidadProduccion = cantidadProduccion;
    }

    public int getUltimoCambio() {
        return ultimoCambio;
    }

    public void setUltimoCambio(int ultimoCambio) {
        this.ultimoCambio = ultimoCambio;
    }
}
