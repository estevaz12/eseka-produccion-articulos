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

    private int produccion;

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

//    public Integer getTimeOn() {
//        return timeOn;
//    }
//
//    public void setTimeOn(Integer timeOn) {
//        this.timeOn = timeOn;
//    }
//
//    public Integer getTimeOff() {
//        return timeOff;
//    }
//
//    public void setTimeOff(Integer timeOff) {
//        this.timeOff = timeOff;
//    }
//
//    public Integer getRoomCode() {
//        return roomCode;
//    }
//
//    public void setRoomCode(Integer roomCode) {
//        this.roomCode = roomCode;
//    }
//
//    public Integer getFunctionKey() {
//        return functionKey;
//    }
//
//    public void setFunctionKey(Integer functionKey) {
//        this.functionKey = functionKey;
//    }

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
//
//    public Integer getLastCycle() {
//        return lastCycle;
//    }
//
//    public void setLastCycle(Integer lastCycle) {
//        this.lastCycle = lastCycle;
//    }
//
//    public Integer getDiscards() {
//        return discards;
//    }
//
//    public void setDiscards(Integer discards) {
//        this.discards = discards;
//    }
//
//    public Integer getShift() {
//        return shift;
//    }
//
//    public void setShift(Integer shift) {
//        this.shift = shift;
//    }
//
//    public Integer getShiftDiscards() {
//        return shiftDiscards;
//    }
//
//    public void setShiftDiscards(Integer shiftDiscards) {
//        this.shiftDiscards = shiftDiscards;
//    }
//
//    public String getDateStartShift() {
//        return dateStartShift;
//    }
//
//    public void setDateStartShift(String dateStartShift) {
//        this.dateStartShift = dateStartShift;
//    }

    public int getProduccion() {
        return produccion;
    }

    public void setProduccion(int produccion) {
        this.produccion = produccion;
    }
}
