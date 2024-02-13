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
        Plant plant = new Plant(SIM_PERIOD, scenario);
        HeaterTankController_HTC tankController = new HeaterTankController_HTC(plant, SIM_PERIOD);
        RoomTemperatureController_RTC roomController = new RoomTemperatureController_RTC(plant, SIM_PERIOD);
        //ORC orc = new ORC(tankController, SIM_PERIOD);
        //orc.start();

        roomController.start();
        tankController.start();
        plant.start();
        double waterRefTemp = 75.0;
        double roomTemperature = 24.0;

        for (int i = 0; i < scenario.getScenarioLength(); i++) {
            tankController.setWaterRefTemp(waterRefTemp);
            tankController.setTankWaterTemp(plant.getTankWaterTemperature());
            roomController.setInput(roomTemperature, plant.getRoomTemperature());
            //orc.setOutsideTemp(scenario.getOutSideTemperature(i));

            try {Thread.sleep(10);
            } catch (InterruptedException e) { e.printStackTrace();	}
        }
        tankController.stop();
        roomController.stop();
        //orc.stop();

        MainView windowTankController = FuzzyPVizualzer.visualize(tankController.getNet(),
                tankController.getRecorder());
        MainView windowTermostat = FuzzyPVizualzer.visualize(roomController.getNet(), roomController.getRecorder());
        //MainView windowOrc = FuzzyPVizualzer.visualize(orc.getNet(), orc.getRecorder());

        Plotter plotterTemperatureLog = new Plotter(plant.getTemperatureLogs());
        Plotter plotterCommandLog = new Plotter(plant.getCommandLogs());
        windowTankController.addInteractivePanel("TempLogs", plotterTemperatureLog.makeInteractivePlot());
        windowTermostat.addInteractivePanel("TempLogs", plotterTemperatureLog.makeInteractivePlot());
        windowTankController.addInteractivePanel("ComandLogs", plotterCommandLog.makeInteractivePlot());
        windowTermostat.addInteractivePanel("ComandLogs", plotterCommandLog.makeInteractivePlot());

        double[] tankTempStats = Main.calcStatistics(plant.getTemperatureLogs().get("tankTemp"));
        double[] rommTempStats = Main.calcStatistics(plant.getTemperatureLogs().get("roomTemp"));

        System.out.println("max tank temp :" + tankTempStats[0]);
        System.out.println("min tank temp :" + tankTempStats[1]);
        System.out.println("avg tank temp :" + tankTempStats[2]);
        System.out.println("max room temp :" + rommTempStats[0]);
        System.out.println("min room temp :" + rommTempStats[1]);
        System.out.println("avg room temp :" + rommTempStats[2]);
        System.out.println("heater on ratio:" + plant.heatingOnRatio());
        System.out.println("max nr of minutes continuous heating on:" + plant.maxContinuousHeaterOn());
        System.out.println("all consumption ::" + plant.gasConsumption());
        System.out.println("avg consumption in  a min ::" + plant.gasConsumption() / scenario.getScenarioLength());
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