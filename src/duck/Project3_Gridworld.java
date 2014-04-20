package duck;

import java.awt.Color;
import java.util.Date;
import java.util.List;

import burlap.behavior.singleagent.*;
import burlap.domain.singleagent.gridworld.*;
import burlap.oomdp.core.*;
import burlap.oomdp.singleagent.*;
import burlap.oomdp.singleagent.common.*;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.singleagent.learning.*;
import burlap.behavior.singleagent.learning.tdmethods.*;
import burlap.behavior.singleagent.planning.*;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.planning.deterministic.*;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.astar.AStar;
import burlap.behavior.singleagent.planning.deterministic.uninformed.bfs.BFS;
import burlap.behavior.singleagent.planning.deterministic.uninformed.dfs.DFS;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.singleagent.planning.stochastic.policyiteration.*;
import burlap.oomdp.visualizer.Visualizer;
import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.auxiliary.common.ConstantStateGenerator;
import burlap.behavior.singleagent.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.auxiliary.performance.LearningAlgorithmExperimenter;
import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.*;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.PolicyGlyphPainter2D.PolicyGlyphRenderStyle;
import burlap.oomdp.singleagent.common.VisualActionObserver;
import burlap.oomdp.singleagent.explorer.VisualExplorer;

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
		
		tf = new SinglePFTF(domain.getPropFunction(GridWorldDomain.PFATLOCATION)); 
		goalCondition = new TFGoalCondition(tf);
		rf = new GoalBasedRF(this.goalCondition, 5., -0.1);
		
		//set up the initial state of the task
		initialState = GridWorldDomain.getOneAgentOneLocationState(domain);
		GridWorldDomain.setAgent(initialState, 0, 0);
		GridWorldDomain.setLocation(initialState, 0, 10, 10);
		
	

			
		//set up the state hashing system
		hashingFactory = new DiscreteStateHashFactory();
		hashingFactory.setAttributesForClass(GridWorldDomain.CLASSAGENT, 
		domain.getObjectClass(GridWorldDomain.CLASSAGENT).attributeList);
		
		/*VisualActionObserver observer = new VisualActionObserver(domain, 
				GridWorldVisualizer.getVisualizer(domain, gwdg.getMap()));
	((SADomain) this.domain).setActionObserverForAllAction(observer);
	observer.initGUI();
		*/
	}
	
	public void valueFunctionVisualize(QComputablePlanner planner, Policy p){
		List <State> allStates = StateReachability.getReachableStates(initialState, 
			(SADomain)domain, hashingFactory);
		LandmarkColorBlendInterpolation rb = new LandmarkColorBlendInterpolation();
		rb.addNextLandMark(0., Color.RED);
		rb.addNextLandMark(1., Color.BLUE);
		
		StateValuePainter2D svp = new StateValuePainter2D(rb);
		svp.setXYAttByObjectClass(GridWorldDomain.CLASSAGENT, GridWorldDomain.ATTX, 
			GridWorldDomain.CLASSAGENT, GridWorldDomain.ATTY);
		
		PolicyGlyphPainter2D spp = new PolicyGlyphPainter2D();
		spp.setXYAttByObjectClass(GridWorldDomain.CLASSAGENT, GridWorldDomain.ATTX, 
			GridWorldDomain.CLASSAGENT, GridWorldDomain.ATTY);
		spp.setActionNameGlyphPainter(GridWorldDomain.ACTIONNORTH, new ArrowActionGlyph(0));
		spp.setActionNameGlyphPainter(GridWorldDomain.ACTIONSOUTH, new ArrowActionGlyph(1));
		spp.setActionNameGlyphPainter(GridWorldDomain.ACTIONEAST, new ArrowActionGlyph(2));
		spp.setActionNameGlyphPainter(GridWorldDomain.ACTIONWEST, new ArrowActionGlyph(3));
		spp.setRenderStyle(PolicyGlyphRenderStyle.DISTSCALED);
		
		ValueFunctionVisualizerGUI gui = new ValueFunctionVisualizerGUI(allStates, svp, planner);
		gui.setSpp(spp);
		gui.setPolicy(p);
		gui.setBgColor(Color.GRAY);
		gui.initGUI();
	}
	
	public void ValueIterationExample(String outputPath){
		
		if(!outputPath.endsWith("/")){
			outputPath = outputPath + "/";
		}
		
		
		OOMDPPlanner planner = new ValueIteration(domain, rf, tf, 0.99, hashingFactory,
								0.001, 100);
		planner.planFromState(initialState);
		
		//create a Q-greedy policy from the planner
		Policy p = new GreedyQPolicy((QComputablePlanner)planner);
		
		//record the plan results to a file
		p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "planResult", sp);
		
		//visualize the value function and policy
		this.valueFunctionVisualize((QComputablePlanner)planner, p);
		
	}
	
	public void QLearningExample(String outputPath){
		
		if(!outputPath.endsWith("/")){
			outputPath = outputPath + "/";
		}
		
		//creating the learning algorithm object; discount= 0.99; initialQ=0.0; learning rate=0.9
		QLearning agent = new QLearning(domain, rf, tf, 0.99, hashingFactory, 0., 0.9);
		agent.setMaximumEpisodesForPlanning(100000);
		agent.setMaxQChangeForPlanningTerminaiton(0.0001);
		agent.planFromState(initialState);
		
		Policy p = new GreedyQPolicy((QComputablePlanner)agent);
		//run learning for 100 episodes
		p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "qLearning", sp);
		this.valueFunctionVisualize((QComputablePlanner)agent, p);
		
	}				

	
	public void PolicyIterationExample(String outputPath)
	{
		if(!outputPath.endsWith("/")){
			outputPath = outputPath + "/";
		}
		
		
		OOMDPPlanner planner = new PolicyIteration(domain, rf, tf, 0.99, hashingFactory,
								0.001, 100, 100);
		planner.planFromState(initialState);
		
		//create a Q-greedy policy from the planner
		Policy p = new GreedyQPolicy((QComputablePlanner)planner);
		
		//record the plan results to a file
		p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "planResult", sp);
		
		//visualize the value function and policy
		this.valueFunctionVisualize((QComputablePlanner)planner, p);
		
	}
	
	public void experimenterAndPlotter(){
		
		//custom reward function for more interesting results
		final RewardFunction rf = new GoalBasedRF(this.goalCondition, 5., -0.1);

		/**
		 * Create factories for Q-learning agent and SARSA agent to compare
		 */

		LearningAgentFactory qLearningFactory = new LearningAgentFactory() {
			
			@Override
			public String getAgentName() {
				return "Q-learning";
			}
			
			@Override
			public LearningAgent generateAgent() {
				return new QLearning(domain, rf, tf, 0.99, hashingFactory, 0.3, 0.1);
			}
		};

		StateGenerator sg = new ConstantStateGenerator(this.initialState);

		LearningAlgorithmExperimenter exp = new LearningAlgorithmExperimenter((SADomain)this.domain, 
			rf, sg, 10, 100, qLearningFactory);

		exp.setUpPlottingConfiguration(500, 250, 2, 1000, 
			TrialMode.MOSTRECENTANDAVERAGE, 
			PerformanceMetric.CUMULATIVESTEPSPEREPISODE, 
			PerformanceMetric.AVERAGEEPISODEREWARD);

		exp.startExperiment();

		exp.writeStepAndEpisodeDataToCSV("expData");


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
		
		String outputPath = "output/"; 
		
	
		mainPITEST(outputPath);
		mainVITEST(outputPath);
		mainQLearningTEST(outputPath);
	}
	
	static void mainPITEST(String outputPath)
	{
		Project3_Gridworld example = new Project3_Gridworld();
		Date d1 = new Date();
		example.PolicyIterationExample(outputPath);
		Date d2 = new Date();
		long elapsed_time = d2.getTime() - d1.getTime(); 
		System.out.println("elapsed time " + elapsed_time  + " milliseconds");
	}
	
	static void mainVITEST(String outputPath)
	{
		Project3_Gridworld example = new Project3_Gridworld();
		Date d1 = new Date();
		example.ValueIterationExample(outputPath);
		Date d2 = new Date();
		long elapsed_time = d2.getTime() - d1.getTime(); 
		System.out.println("elapsed time " + elapsed_time  + " milliseconds");
	}
	
	static void mainQLearningTEST(String outputPath)
	{
		Project3_Gridworld example = new Project3_Gridworld();
		Date d1 = new Date();
		example.QLearningExample(outputPath);
		Date d2 = new Date();
		long elapsed_time = d2.getTime() - d1.getTime(); 
		System.out.println("elapsed time " + elapsed_time  + " milliseconds");
	}
	
	
}
