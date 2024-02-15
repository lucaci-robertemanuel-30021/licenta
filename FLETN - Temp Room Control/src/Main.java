import java.util.List;
import Main.FuzzyPVizualzer;
import Main.Plotter;
import View.MainView;
public class Main {
    private static final int SIM_PERIOD = 10;

    public static void main(String[] args) {
        //set desired scenario
        Scenario scenario = Scenario.winterDay();
        System.out.println("winter day");
        PlantModel plantModel = new PlantModel(SIM_PERIOD, scenario);
        HeaterTankController_HTC tankController = new HeaterTankController_HTC(plantModel, SIM_PERIOD);
        RoomTemperatureController_RTC roomController = new RoomTemperatureController_RTC(plantModel, SIM_PERIOD);
        AirConditionerController_ACC airConditionerController = new AirConditionerController_ACC(plantModel, SIM_PERIOD);
        //ORC orc = new ORC(tankController, SIM_PERIOD);
        //orc.start();

        roomController.start();
        tankController.start();
        airConditionerController.start();
        plantModel.start();
        double waterRefTemp = 75.0;
        double roomTemperature = 24.0;

        for (int i = 0; i < scenario.getScenarioLength(); i++) {
            tankController.setWaterRefTemp(waterRefTemp);
            tankController.setTankWaterTemp(plantModel.getTankWaterTemperature());
            roomController.setInput(roomTemperature, plantModel.getRoomTemperature());
            airConditionerController.setInput(roomTemperature, plantModel.getRoomTemperature());
            //orc.setOutsideTemp(scenario.getOutSideTemperature(i));

            try {Thread.sleep(10);
            } catch (InterruptedException e) { e.printStackTrace();	}
        }
        tankController.stop();
        roomController.stop();
        airConditionerController.stop();
        //orc.stop();

        MainView windowTankController = FuzzyPVizualzer.visualize(tankController.getNet(),
                tankController.getRecorder());
        MainView windowTermostat = FuzzyPVizualzer.visualize(roomController.getNet(), roomController.getRecorder());
        MainView windowAirConditioner = FuzzyPVizualzer.visualize(airConditionerController.getNet(), airConditionerController.getRecorder());
        //MainView windowOrc = FuzzyPVizualzer.visualize(orc.getNet(), orc.getRecorder());

        Plotter plotterTemperatureLog = new Plotter(plantModel.getTemperatureLogs());
        Plotter plotterCommandLog = new Plotter(plantModel.getCommandLogs());
        windowTankController.addInteractivePanel("TempLogs", plotterTemperatureLog.makeInteractivePlot());
        windowTermostat.addInteractivePanel("TempLogs", plotterTemperatureLog.makeInteractivePlot());
        windowAirConditioner.addInteractivePanel("TempLogs", plotterTemperatureLog.makeInteractivePlot());
        windowTankController.addInteractivePanel("ComandLogs", plotterCommandLog.makeInteractivePlot());
        windowTermostat.addInteractivePanel("ComandLogs", plotterCommandLog.makeInteractivePlot());
        windowAirConditioner.addInteractivePanel("ComandLogs", plotterCommandLog.makeInteractivePlot());

        double[] tankTempStats = Main.calcStatistics(plantModel.getTemperatureLogs().get("tankTemp"));
        double[] roomTempStats = Main.calcStatistics(plantModel.getTemperatureLogs().get("roomTemp"));
     //   double[] ACTempStats = Main.calcStatistics(plantModel.getTemperatureLogs().get("acAirTemp"));
        System.out.println("max tank temp :" + tankTempStats[0]);
        System.out.println("min tank temp :" + tankTempStats[1]);
        System.out.println("avg tank temp :" + tankTempStats[2]);
        System.out.println("max room temp :" + roomTempStats[0]);
        System.out.println("min room temp :" + roomTempStats[1]);
        System.out.println("avg room temp :" + roomTempStats[2]);
        System.out.println("heater on ratio:" + plantModel.heatingOnRatio());
        System.out.println("max nr of minutes continuous heating on:" + plantModel.maxContinuousHeaterOn());
        //added to update the gas consumption
        //plant.makeLogs();
        System.out.println("all consumption ::" + plantModel.gasConsumption());
        System.out.println("avg consumption in  a min ::" + plantModel.gasConsumption() / scenario.getScenarioLength());

     //   System.out.println("max tank temp :" + ACTempStats[0]);
     //   System.out.println("min tank temp :" + ACTempStats[1]);
      //  System.out.println("avg tank temp :" + ACTempStats[2]);
    }

    public static double[] calcStatistics(List<Double> list) {
        double min = 1000.0;
        double max = 0.0;
        double sum = 0.0;
        for (Double d : list) {
            min = (min > d) ? d : min;
            max = (max < d) ? d : max;
            sum += d;		}
        return new double[] { max, min, sum / list.size() };
    }
}