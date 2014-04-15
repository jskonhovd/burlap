package duck;

import burlap.behavior.singleagent.*;
import burlap.domain.singleagent.gridworld.*;
import burlap.oomdp.core.*;
import burlap.oomdp.singleagent.*;
import burlap.oomdp.singleagent.common.*;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.oomdp.auxiliary.StateParser;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.TFGoalCondition;
import burlap.oomdp.visualizer.Visualizer;
import burlap.behavior.singleagent.EpisodeSequenceVisualizer;
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
				

public class BasicBehavior {

	
	
	GridWorldDomain				gwdg;
	Domain						domain;
	StateParser					sp;
	RewardFunction				rf;
	TerminalFunction			tf;
	StateConditionTest			goalCondition;
	State						initialState;
	DiscreteStateHashFactory	hashingFactory;
	
	public BasicBehavior(){
		gwdg = new GridWorldDomain(11, 11);
		gwdg.setMapToFourRooms(); 
		domain = gwdg.generateDomain();
		
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
		
	}
	
	public void visualize(String outputPath){
		Visualizer v = GridWorldVisualizer.getVisualizer(domain, gwdg.getMap());
		EpisodeSequenceVisualizer evis = new EpisodeSequenceVisualizer(v, domain, sp, outputPath);
	}

	public void BFSExample(String outputPath){
		
		if(!outputPath.endsWith("/")){
			outputPath = outputPath + "/";
		}
		
		//BFS ignores reward; it just searches for a goal condition satisfying state
		DeterministicPlanner planner = new BFS(domain, goalCondition, hashingFactory); 
		planner.planFromState(initialState);
		
		//capture the computed plan in a partial policy
		Policy p = new SDPlannerPolicy(planner);
		
		//record the plan results to a file
		p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "planResult", sp);
			
	}				

	
	public void DFSExample(String outputPath){
		
		if(!outputPath.endsWith("/")){
			outputPath = outputPath + "/";
		}
		
		//DFS ignores reward; it just searches for a goal condition satisfying state
		DeterministicPlanner planner = new DFS(domain, goalCondition, hashingFactory);
		planner.planFromState(initialState);
		
		//capture the computed plan in a partial policy
		Policy p = new SDPlannerPolicy(planner);
		
		//record the plan results to a file
		p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "planResult", sp);
		
	}

	public void AStarExample(String outputPath){
		if(!outputPath.endsWith("/")){
		outputPath = outputPath + "/";
		}
		Heuristic mdistHeuristic = new Heuristic() {
		@Override
		public double h(State s) {
		String an = GridWorldDomain.CLASSAGENT;
		String ln = GridWorldDomain.CLASSLOCATION;
		ObjectInstance agent = s.getObjectsOfTrueClass(an).get(0);
		ObjectInstance location = s.getObjectsOfTrueClass(ln).get(0);
		//get agent position
		int ax = agent.getDiscValForAttribute(GridWorldDomain.ATTX);
		int ay = agent.getDiscValForAttribute(GridWorldDomain.ATTY);
		//get location position
		int lx = location.getDiscValForAttribute(GridWorldDomain.ATTX);
		int ly = location.getDiscValForAttribute(GridWorldDomain.ATTY);
		//compute Manhattan distance
		double mdist = Math.abs(ax-lx) + Math.abs(ay-ly);
		return -mdist;
		}
		};
		//provide A* the heuristic as well as the reward function so that it can keep
		//track of the actual cost
		DeterministicPlanner planner = new AStar(domain, rf, goalCondition,
		hashingFactory, mdistHeuristic);
		planner.planFromState(initialState);
		//capture the computed plan in a partial policy
		Policy p = new SDPlannerPolicy(planner);
		//record the plan results to a file
		p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "planResult", sp);
		}	
	
	
	public void ValueIterationExample(String outputPath){
		
		if(!outputPath.endsWith("/")){
			outputPath = outputPath + "/";
		}
		
		
		OOMDPPlanner planner = new ValueIteration(domain, rf, tf, 0.99, hashingFactory, 0.001, 100);
		planner.planFromState(initialState);
		
		//create a Q-greedy policy from the planner
		Policy p = new GreedyQPolicy((QComputablePlanner)planner);
		
		//record the plan results to a file
		p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "planResult", sp);
		
	}

	
	public void QLearningExample(String outputPath){
		
		if(!outputPath.endsWith("/")){
			outputPath = outputPath + "/";
		}
		
		//creating the learning algorithm object; discount= 0.99; initialQ=0.0; learning rate=0.9
		LearningAgent agent = new QLearning(domain, rf, tf, 0.99, hashingFactory, 0., 0.9);
		
		//run learning for 100 episodes
		for(int i = 0; i < 100; i++){
			EpisodeAnalysis ea = agent.runLearningEpisodeFrom(initialState);
			ea.writeToFile(String.format("%se%03d", outputPath, i), sp); 
			System.out.println(i + ": " + ea.numTimeSteps());
		}
		
	}
	
	public void SarsaLearningExample(String outputPath){
		
		if(!outputPath.endsWith("/")){
			outputPath = outputPath + "/";
		}
		
		//discount= 0.99; initialQ=0.0; learning rate=0.5; lambda=1.0
		LearningAgent agent = new SarsaLam(domain, rf, tf, 0.99, hashingFactory, 0., 0.5, 1.0);
		
		//run learning for 100 episodes
		for(int i = 0; i < 100; i++){
			EpisodeAnalysis ea = agent.runLearningEpisodeFrom(initialState);
			ea.writeToFile(String.format("%se%03d", outputPath, i), sp);
			System.out.println(i + ": " + ea.numTimeSteps());
		}
		
	}
	
	public static void main(String[] args) {
	
		

		BasicBehavior example = new BasicBehavior();
		String outputPath = "output/"; //directory to record results
		
		//we will call planning and learning algorithms here
		
		
		//run example
		example.SarsaLearningExample(outputPath);
		//run the visualizer
		example.visualize(outputPath);
		//we'll fill this in later
	
	}
	
	
}