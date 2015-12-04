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

    int zaribGetDistanceLRTAstar=7;
    int zaribMatrixCellLRTAstar=1;
    int LRAStarINF=50;
    int castleBfsLimit=7;

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
        // ********************** //
        // we chan
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
        ChristopherCastle myCastle = new ChristopherCastle(wm.self.agents.get(0),wm,unitMatrix,goldMatrix,searchAlgorithms,heuristics,turnNumber);
        ArrayList<ChristopherWorker> myWorkers=new ArrayList<>();
        ArrayList<ChristopherWarrior> myWarrior=new ArrayList<>();

        initialIDMatrixAndIDMap(wm);
        initilaUnitMatrixAndData(wm);
        initialGoldMatrix(wm);
//        determineLimits();
//        turnDecide();


        if(turnNumber%2==0){

            myCastle.unit.make(Direction.E, UnitType.WORKER);
        }
        //myCastle.setTask(castleBfsLimit);
        //myCastle.doItsJob();

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
