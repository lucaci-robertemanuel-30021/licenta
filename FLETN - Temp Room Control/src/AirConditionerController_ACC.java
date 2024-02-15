import core.FuzzyPetriLogic.Executor.AsyncronRunnableExecutor;
import core.FuzzyPetriLogic.FuzzyDriver;
import core.FuzzyPetriLogic.FuzzyToken;
import core.FuzzyPetriLogic.PetriNet.FuzzyPetriNet;
import core.FuzzyPetriLogic.PetriNet.Recorders.FullRecorder;
import core.FuzzyPetriLogic.Tables.OneXOneTable;
import core.TableParser;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AirConditionerController_ACC {
    static String reader = "" +
            "{[<NL><NM><ZR><PM><PL>]" +
            " [<NL><NM><ZR><PM><PL>]" +
            " [<NL><NM><ZR><PM><PL>]" +
            " [<NL><NM><ZR><PM><PL>]" +
            " [<NL><NM><ZR><PM><PL>]}";

    static String doubleChannelDifferentiator = ""//
            + "{[<ZR,ZR><NM,NM><NL,NL><NL,NL><NL,NL>]" //
            + " [<PM,PM><ZR,ZR><NM,NM><NL,NL><NL,NL>]" //
            + " [<PL,PL><PM,PM><ZR,ZR><NM,NM><NL,NL>]"//
            + " [<PL,PL><PL,PL><PM,PM><ZR,ZR><NM,NM>]"//
            + " [<PL,PL><PL,PL><PL,PL><PM,PM><ZR,ZR>]}";

    static String t4Table = "{[<FF,ZR>, <FF,FF>, <FF,FF>, <FF,FF>, <ZR, FF>]}";

    //tabelele noi
    static String cool_heat = "{[<FF><PL><PL><PL><PL>]" +
                              " [<NL><FF><PL><PL><PL>]" +
                              " [<NL><NL><FF><PL><PL>]" +
                              " [<NL><NL><NL><FF><PL>]" +
                              " [<NL><NL><NL><NL><FF>]}"; //should be t5

    static String reader1X2 = "{[<NL,NL><NM,NM><ZR,ZR><PM,PM><PL,PL>]}"; //t3

    //////////

    private FuzzyPetriNet net;
    private int p1RefInp;
    private int p3RealInp;
    private int p11Inp;
    private FuzzyDriver temperatureDriver;
    private FullRecorder rec;
    private AsyncronRunnableExecutor executor;
    public AirConditionerController_ACC(PlantModel plantModel, long simPeriod) {
        net = new FuzzyPetriNet();
        TableParser parser=new TableParser();

        int p0 = net.addPlace();
        net.setInitialMarkingForPlace(p0, FuzzyToken.zeroToken());
        p1RefInp = net.addInputPlace();

        int t0 = net.addTransition(0, parser.parseTwoXOneTable(reader));
        net.addArcFromPlaceToTransition(p0, t0, 1.0);
        net.addArcFromPlaceToTransition(p1RefInp, t0, 1.0);
        int p2 = net.addPlace();
        net.addArcFromTransitionToPlace(t0, p2);
        p3RealInp = net.addInputPlace();

        int t1 = net.addTransition(0, parser.parseTwoXTwoTable(doubleChannelDifferentiator));
        net.addArcFromPlaceToTransition(p2, t1, 1.0);
        net.addArcFromPlaceToTransition(p3RealInp, t1, 1.0);

        int p4 = net.addPlace();
        int p5 = net.addPlace();
        net.addArcFromTransitionToPlace(t1, p4);
        net.addArcFromTransitionToPlace(t1, p5);
        int t2 = net.addTransition(1, OneXOneTable.defaultTable());
        net.addArcFromPlaceToTransition(p4, t2, 1.0);
        net.addArcFromTransitionToPlace(t2, p0);

        int t3 = net.addTransition(0, parser.parseOneXTwoTable(reader1X2));
        net.addArcFromPlaceToTransition(p5, t3, 1.0);
        int p6 = net.addPlace();
        int p7 = net.addPlace();
        net.addArcFromTransitionToPlace(t3, p6);
        net.addArcFromTransitionToPlace(t3, p7);

        int t4 = net.addTransition(0, parser.parseOneXTwoTable(t4Table));
        net.addArcFromPlaceToTransition(p6, t4, 1.0);
        int p8 = net.addPlace();
        int p10 = net.addPlace();
        net.addArcFromTransitionToPlace(t4, p8);
        net.addArcFromTransitionToPlace(t4, p10);
        int t6Out = net.addOuputTransition(OneXOneTable.defaultTable());
        int t7Out = net.addOuputTransition(OneXOneTable.defaultTable());
        net.addArcFromPlaceToTransition(p8, t6Out, 1.0);
        net.addArcFromPlaceToTransition(p10, t7Out, 1.0);
        //?

        int t5 = net.addTransition(0, parser.parseTwoXOneTable(cool_heat));
        p11Inp = net.addInputPlace();
        net.addArcFromPlaceToTransition(p11Inp, t5, 1.0);
        net.addArcFromPlaceToTransition(p7, t5, 1.0);
        int p9 = net.addPlace();
        net.addArcFromTransitionToPlace(t5, p9);
        int t8Out = net.addOuputTransition(OneXOneTable.defaultTable());
        int t9Out = net.addOuputTransition(OneXOneTable.defaultTable());
        net.addArcFromPlaceToTransition(p9, t8Out, 1.0);
        net.addArcFromPlaceToTransition(p9, t9Out, 1.0);

        temperatureDriver = FuzzyDriver.createDriverFromMinMax(-40, 40);

        rec = new FullRecorder();
        executor = new AsyncronRunnableExecutor(net, simPeriod);
        executor.setRecorder(rec);

        net.addActionForOuputTransition(t6Out, new Consumer<FuzzyToken>() {
            @Override
            public void accept(FuzzyToken fuzzyToken) {
                plantModel.setACOn(true);
            }
        });
        net.addActionForOuputTransition(t7Out, new Consumer<FuzzyToken>() {
            @Override
            public void accept(FuzzyToken fuzzyToken) {
                plantModel.setACOn(false);
            }
        });
        net.addActionForOuputTransition(t8Out, new Consumer<FuzzyToken>() {
            @Override
            public void accept(FuzzyToken fuzzyToken) {
                plantModel.setIsCool(false); //false is heat
            }
        });
        net.addActionForOuputTransition(t9Out, new Consumer<FuzzyToken>() {
            @Override
            public void accept(FuzzyToken fuzzyToken) {
                plantModel.setIsCool(true); // true is cool
            }
        });

    }
    public void start() {    (new Thread(executor)).start();  }

    public void stop() {    executor.stop();  }

    public void setInput(double roomTemperatureRef, double roomTemperature) {
        Map<Integer, FuzzyToken> inps = new HashMap<Integer, FuzzyToken>();
        inps.put(p1RefInp, temperatureDriver.fuzzifie(roomTemperatureRef));
        inps.put(p3RealInp, temperatureDriver.fuzzifie(roomTemperature));

        //not sure what to add here
        inps.put(p11Inp, temperatureDriver.fuzzifie(roomTemperature-roomTemperatureRef));
        executor.putTokenInInputPlace(inps);
    }
    public FuzzyPetriNet getNet() {    return net;  }

    public FullRecorder getRecorder() {    return rec;  }
}
