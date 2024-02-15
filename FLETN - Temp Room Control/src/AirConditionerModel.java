public class AirConditionerModel {
    //something similar like heater tank
    private static final double StartingTemperature = 24.0;

    private static final double wallConstant = 0.00055;
     // daca diff dintre temp de afara si temp camerei este 1C atunci temp. camerei se modifica constant cu <wallConstant> in fiecare minut
    private static final double diffConstant = 0.01;
    private static final double windowConstant = 0.01;
    double currentTemperature;

    public AirConditionerModel() {
        currentTemperature = StartingTemperature;
    }

    public double getCurrentTemperature() {
        return currentTemperature;
    }

    public void updateModel(boolean AC_on, boolean isCold,double AcAirTemp, boolean windowOpen, double outSideTemp) {
       double deltaTemp;

        if(AC_on==true){
           deltaTemp = (isCold) ? (currentTemperature-AcAirTemp) : (AcAirTemp-currentTemperature);
       }else {
            deltaTemp = 0;}

        double outsideDelta = currentTemperature - outSideTemp;

        currentTemperature += deltaTemp*diffConstant - outsideDelta * wallConstant
                - ((windowOpen) ? (outsideDelta * windowConstant) : 0.0);
    }
}