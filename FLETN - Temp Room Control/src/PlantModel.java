import java.util.*;

public class PlantModel {
    private volatile boolean ACOn = false;
    private volatile boolean isCool = false;
    private volatile boolean heaterOn = false;
    private volatile double gasCmd = 0.0;
    private int tickCntr = 0;
    private long period;
    private RoomModel room;
    private Scenario scenario;
    private HeaterTank tank;

    /* for logs */
    ArrayList<Double> heaterWaterTempLog = new ArrayList<>();
    ArrayList<Double> roomTempLog = new ArrayList<>();
    ArrayList<Double> waterHeaterCmdLog = new ArrayList<>();
    ArrayList<Double> heatOnCmdLog = new ArrayList<>();
    int heatOnCntr = 0;
    int continuousHeatOnMax = 0;
    int continuousHeatOnCurrent = 0;
    double tankGasCommandSum = 0.0;

    public PlantModel(long simPeriod, Scenario scen) {
        this.period = simPeriod;
        room = new RoomModel();
        tank = new HeaterTank();
        scenario = scen;  }

    public void setHeatingOn(boolean heaterOn) {
        this.heaterOn = heaterOn;  }
    ///////////////
    public void setACOn(boolean ACOn) {
        this.ACOn = ACOn;  }

    public void setIsCool(boolean isCool){
        this.isCool = isCool;
    }
    /////////////////
    public void setHeaterGasCmd(double cmd) {
        gasCmd = cmd;  }

    public double getRoomTemperature() {
        return room.getCurrentTemperature();  }

    public double heatingOnRatio() {
        return ((double) heatOnCntr / (double) tickCntr);  }

    public double gasConsumption() {
        return tankGasCommandSum;  }

    public int maxContinuousHeaterOn() {
        return continuousHeatOnMax;  }

    public void start() {
        Timer myTimer = new Timer();
        TimerTask task = new TimerTask() {
            //updates the information sent every set "period" until the scenario is completed
            @Override
            public void run() {
                if (tickCntr < scenario.getScenarioLength()) {
                    tank.updateSystem(heaterOn, gasCmd);
                    room.updateModel(heaterOn, tank.getHotWaterTemperature(), scenario.getWindowOpen(tickCntr),
                            scenario.getOutSideTemperature(tickCntr));
                    makeLogs();
                    tickCntr++;
                }
                else {
                    myTimer.cancel();
                    myTimer.purge();        }
            }
        };
        myTimer.scheduleAtFixedRate(task, period, period);  }

    public void makeLogs() {
        heaterWaterTempLog.add(tank.getHotWaterTemperature());
        roomTempLog.add(room.getCurrentTemperature());
        waterHeaterCmdLog.add(gasCmd);
        heatOnCmdLog.add(heaterOn ? 1.0 : 0.0);
        heatOnCntr += (heaterOn ? 1.0 : 0.0);
        if (heaterOn) {
            continuousHeatOnCurrent++;
        }
        else if (continuousHeatOnCurrent > 0) {
            if (continuousHeatOnCurrent > continuousHeatOnMax) {
                continuousHeatOnMax = continuousHeatOnCurrent;
            }
            continuousHeatOnCurrent = 0;
        }
        tankGasCommandSum += (gasCmd < 0.0) ? 0.0 : gasCmd;
    }
    public Double getTankWaterTemperature() {
        return tank.getHotWaterTemperature();  }

    public Map<String, List<Double>> getTemperatureLogs() {
        HashMap<String, List<Double>> logMap = new HashMap<>();
        logMap.put("tankTemp", heaterWaterTempLog);
        logMap.put("roomTemp", roomTempLog);
        return logMap;  }

    public Map<String, List<Double>> getCommandLogs() {
        HashMap<String, List<Double>> logMap = new HashMap<>();
        logMap.put("waterCmd", waterHeaterCmdLog);
        logMap.put("heaterOn", heatOnCmdLog);
        return logMap;  }

}
