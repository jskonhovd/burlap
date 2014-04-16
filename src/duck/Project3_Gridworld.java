package duck;

import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.TFGoalCondition;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldStateParser;
import burlap.domain.singleagent.gridworld.GridWorldVisualizer;
import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.SinglePFTF;
import burlap.oomdp.singleagent.common.UniformCostRF;
import burlap.oomdp.singleagent.common.VisualActionObserver;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.visualizer.Visualizer;

public class Project3_Gridworld {
	GridWorldDomain				gwdg;
	Domain						domain;
	StateParser					sp;
	RewardFunction				rf;
	TerminalFunction			tf;
	StateConditionTest			goalCondition;
	State						initialState;
	DiscreteStateHashFactory	hashingFactory;
	
	public Project3_Gridworld()
	{
		gwdg = new GridWorldDomain(11, 11);
		//gwdg.setMapToFourRooms(); 
		gwdg.verticalWall(1, 7, 3);
		gwdg.verticalWall(0, 2, 1);
		gwdg.verticalWall(0, 2, 8);
		gwdg.verticalWall(7, 10, 9);
		gwdg.horizontalWall(0, 5, 7);
		gwdg.horizontalWall(7, 9, 6);
		gwdg.horizontalWall(5, 10, 4);
		gwdg.horizontalWall(0, 6, 9);
		gwdg.horizontalWall(1, 3, 4);
		domain =  gwdg.generateDomain();
		
		//create the state parser
		sp = new GridWorldStateParser(domain); 
		
		//define the task
		rf = new UniformCostRF(); 
		tf = new SinglePFTF(domain.getPropFunction(GridWorldDomain.PFATLOCATION)); 
		goalCondition = new TFGoalCondition(tf);
		
		//set up the initial state of the task
		initialState = GridWorldDomain.getOneAgentOneLocationState(domain);
		GridWorldDomain.setAgent(initialState, 0, 0);
		GridWorldDomain.setLocation(initialState, 0, 10, 10);
		
		//set up the state hashing system
		hashingFactory = new DiscreteStateHashFactory();
		hashingFactory.setAttributesForClass(GridWorldDomain.CLASSAGENT, 
		domain.getObjectClass(GridWorldDomain.CLASSAGENT).attributeList);
		
		VisualActionObserver observer = new VisualActionObserver(domain, 
				GridWorldVisualizer.getVisualizer(domain, gwdg.getMap()));
	((SADomain) this.domain).setActionObserverForAllAction(observer);
	//observer.initGUI();
		
	}
	
	public void Test()
	{
		

		//create visualizer and explorer
		Visualizer v = GridWorldVisualizer.getVisualizer(domain, gwdg.getMap());
		VisualExplorer exp = new VisualExplorer(domain, v, initialState);
		
		//set control keys to use w-s-a-d
		exp.addKeyAction("w", GridWorldDomain.ACTIONNORTH);
		exp.addKeyAction("s", GridWorldDomain.ACTIONSOUTH);
		exp.addKeyAction("a", GridWorldDomain.ACTIONWEST);
		exp.addKeyAction("d", GridWorldDomain.ACTIONEAST);
		
		exp.initGUI();
	}
	
	public static void main(String[] args)
	{
		Project3_Gridworld example = new Project3_Gridworld();
		String outputPath = "output/"; 
		example.Test();
	}
	
}
