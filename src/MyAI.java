import client.PlayerAI;
import client.Unit;
import client.UnitType;
import client.WorldModel;
import client.commands.Direction;
import core.math.Vector2D;
import jdk.nashorn.internal.objects.NativeUint16Array;
import server.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Pouya Payandeh on 11/30/2015.
 */
public class MyAI implements PlayerAI
{
    Heuristics heuristics;//=new Heuristics(wm.cloneTerrain(),100);
    boolean first = true;
    int turnNumber = 0;
    Vector2D myCastlePos;
    Vector2D theirCastlePos;

    int maxTurn=300;
    int zaribGetDistanceLRTAstar=7;
    int zaribMatrixCellLRTAstar=1;
    int LRAStarINF=50;
    int castleBfsLimit=7;

    String turnDecision;


    Map<Integer,Unit> unitIDMap;

    int ourWorker;
    int ourWarrior;
    int theirWorker;
    int theirWarrior;

    int[][] goldMatrix;
    int[][] unitMatrix;


    @Override

    public void doTurn(WorldModel wm)
    {

        myCastlePos=new Vector2D(wm.self.agents.get(0).getPos().x,wm.self.agents.get(0).getPos().y);
        if(wm.others.size()>0)
            theirCastlePos =new Vector2D(wm.others.get(0).agents.get(0).getPos().x,wm.others.get(0).agents.get(0).getPos().y);
        SearchAlgorithms searchAlgorithms=new SearchAlgorithms(wm.cloneTerrain(),zaribGetDistanceLRTAstar,zaribMatrixCellLRTAstar,myCastlePos,theirCastlePos);//a
        turnNumber++;

        if(first){
            //peyda kardan path behine ta castle harif
            heuristics=new Heuristics(wm.cloneTerrain(),LRAStarINF);
            first = false;
        }
        ArrayList<ChristopherWorker> myWorkers=new ArrayList<>();
        ArrayList<ChristopherWarrior> myWarrior=new ArrayList<>();

        initialIDMatrixAndIDMap(wm);
        initilaUnitMatrixAndData(wm);
        initialGoldMatrix(wm);
//        determineLimits();
        turnDecide(wm);

        ChristopherCastle myCastle = new ChristopherCastle(wm.self.agents.get(0),wm,unitMatrix,goldMatrix,searchAlgorithms,heuristics,turnNumber,ourWorker,wm.goldMines.size());

        if(turnNumber%2==0){
            System.err.println("castle id:"+wm.self.id);
            myCastle.unit.make(Direction.E, UnitType.WORKER);
        }
        //myCastle.setTask(castleBfsLimit);
        //myCastle.doItsJob();

        //just for test LAstar
/*
        PathData pTest;
        pTest = searchAlgorithms.LRTAStar(heuristics, new Vector2D(3,1), new Vector2D(3,7), 100);
        System.err.println("ASTARNODES : " + searchAlgorithms.AStarNodes + "pathLentgh: "+ pTest.distance[3][7]);
        pTest = searchAlgorithms.LRTAStar(heuristics, new Vector2D(6,3), new Vector2D(1,4), 100);
        System.err.println("ASTARNODES : " + searchAlgorithms.AStarNodes+ "pathLentgh: "+ pTest.distance[1][4]);
        pTest = searchAlgorithms.LRTAStar(heuristics, new Vector2D(5,0), new Vector2D(0,5), 100);
        System.err.println("ASTARNODES : " + searchAlgorithms.AStarNodes + "pathLentgh: "+ pTest.distance[0][5]);
*/

        for (int i = 1; i < wm.self.agents.size(); i++) {
            if(wm.self.agents.get(i).getType()==UnitType.WORKER)
                myWorkers.add(new ChristopherWorker(wm.self.agents.get(i),wm,unitMatrix,goldMatrix,searchAlgorithms,heuristics,turnNumber));
            if(wm.self.agents.get(i).getType() == UnitType.WARRIOR){
                myWarrior.add(new ChristopherWarrior(wm.self.agents.get(i),wm,unitMatrix,goldMatrix,searchAlgorithms,heuristics,turnNumber));
            }
        }

        for (int i = 0; i < myWarrior.size(); i++) {
            myWarrior.get(i).setTask();
            myWarrior.get(i).doItsJob();
        }


        for (int i = 0; i < myWorkers.size(); i++) {
            myWorkers.get(i).setTask();
            myWorkers.get(i).doItsJob();
        }
    }

    private void turnDecide(WorldModel wm) {

        double total = 1
                ,maxHPOfCastle = 100.0
                ,dangerLimit1 = 0
                ,dangerLimit2 = 0
                ,dangerLimit3 = 0
                ,goldCompare = 0
                ,warriorCompare = 0
                ,workerCompare = 0;


        goldCompare = (double)wm.self.gold / (double)wm.others.get(0).gold;

        if((double)turnNumber / total < 1.0/3.0)
        {
            if(dangerAmount > dangerLimit1)
                turnDecision = "Warrior";
            else
            {
                if(goldCompare < 2.0/3.0)
                {
                    if(workerCompare < 0.9)
                        turnDecision = "Worker";
                    else
                        turnDecision = "Gold";
                }
                else
                {
                    if(warriorCompare < 0.9)
                        turnDecision = "Warrior";
                    else
                        turnDecision = "Gold";
                }
            }
        }
        else if((double)turnNumber / total < 2.0/3.0)
        {
            if(dangerAmount > dangerLimit2)
                turnDecision = "Warrior";
            else
            {
                if(goldCompare < 3.0/4.0)
                {
                    if(workerCompare < 0.9)
                        turnDecision = "Worker";
                    else
                        turnDecision = "Gold";
                }
                else
                {
                    if(warriorCompare < 0.75)
                        turnDecision = "Warrior";
                    else
                        turnDecision = "Gold";
                }
            }
        }
        else
        {
            if(dangerAmount > dangerLimit3)
            {
                turnDecision = "Warrior";
            }
            else
            {
                if(goldCompare < 1.0)
                {
                    if(workerCompare < 0.9)
                        turnDecision = "Worker";
                    else
                        turnDecision = "Gold";
                }
                else
                {
                    if(warriorCompare < 0.5)
                        turnDecision = "Warrior";
                    else
                        turnDecision = "Gold";
                }
            }
        }

    }

    int[][] IDMatrix;
    private void initialIDMatrixAndIDMap(WorldModel wm) {
        unitIDMap=new HashMap<>();
        IDMatrix=new int[wm.cloneTerrain().length][wm.cloneTerrain()[0].length];
        if(wm.others.size()>0){
            ArrayList<Unit> ag=wm.others.get(0).agents;
            for (int i = 0; i < ag.size() ;i++) {
                IDMatrix[ag.get(i).getPos().x][ag.get(i).getPos().y]=ag.get(i).getId();
                unitIDMap.put(ag.get(i).getId(),ag.get(i));
            }
        }
        ArrayList<Unit> mine= wm.self.agents;
        for (int i = 0; i < mine.size(); i++) {
            IDMatrix[mine.get(i).getPos().x][mine.get(i).getPos().y]=mine.get(i).getId();
            unitIDMap.put(mine.get(i).getId(), mine.get(i));
        }

    }

    private void initilaUnitMatrixAndData(WorldModel wm) {
        unitMatrix=new int[wm.cloneTerrain().length][wm.cloneTerrain()[0].length];
        ourWorker=0;
        ourWarrior=0;
        theirWorker=0;
        theirWarrior=0;

        if(wm.others.size()>0){
        ArrayList<Unit> ag=wm.others.get(0).agents;
        for (int i = 0; i < ag.size() ;i++) {
                if(ag.get(i).getType().equals(UnitType.WARRIOR)){
                    unitMatrix[ag.get(i).getPos().x][ag.get(i).getPos().y]=21;
                    theirWarrior++;
                }
                else if(ag.get(i).getType().equals(UnitType.WORKER)){
                    unitMatrix[ag.get(i).getPos().x][ag.get(i).getPos().y]=22;
                    theirWorker++;
                }
            }
           }
        ArrayList<Unit> mine= wm.self.agents;
        for (int i = 0; i < mine.size(); i++) {
            if(mine.get(i).getType().equals(UnitType.WARRIOR)){
                unitMatrix[mine.get(i).getPos().x][mine.get(i).getPos().y]=11;
                ourWarrior++;
            }
            else if(mine.get(i).getType().equals(UnitType.WORKER)){
                unitMatrix[mine.get(i).getPos().x][mine.get(i).getPos().y]=12;
                ourWorker++;
            }
        }

    }

    private void initialGoldMatrix(WorldModel wm) {
        goldMatrix=new int[wm.cloneTerrain().length][wm.cloneTerrain()[0].length];
        for (int i = 0; i < wm.cloneTerrain().length; i++) {
            for (int j = 0; j < wm.cloneTerrain()[0].length; j++) {
                goldMatrix[i][j]=0;
            }
        }
        for (int i = 0; i < wm.goldMines.size(); i++) {
            if(wm.goldMines.get(i).goldAmount>0)
                goldMatrix[wm.goldMines.get(i).pos.x][wm.goldMines.get(i).pos.y]=1;
        }
    }
///////*******this for debug****///////////////
    /*
    private void printheuMat() {
        int m[][]=heuristics.LRTAStarmatrix;
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                System.out.print(m[j][i]+" ");
            }
            System.out.println();
        }
    }
    private void printhillMat()
    {
        for (int i = 0; i < heuristics.darknessMatrix.length; i++) {
            for (int j = 0; j < heuristics.darknessMatrix[0].length; j++) {
                System.out.print(heuristics.lastSeenMatrix[j][i]+" ");
            }
            System.out.println();
        }
    }
    */
}
