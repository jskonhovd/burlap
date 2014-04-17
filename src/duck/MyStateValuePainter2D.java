package duck;

import java.text.DecimalFormat;

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
import burlap.oomdp.singleagent.common.VisualActionObserver;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;

import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.ArrowActionGlyph;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.LandmarkColorBlendInterpolation;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.PolicyGlyphPainter2D;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.PolicyGlyphPainter2D.PolicyGlyphRenderStyle;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.StateValuePainter2D;
import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.auxiliary.common.ConstantStateGenerator;
import burlap.behavior.singleagent.auxiliary.performance.LearningAlgorithmExperimenter;
import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;

public class MyStateValuePainter2D extends StateValuePainter2D {

	
	public MyStateValuePainter2D(LandmarkColorBlendInterpolation rb){
		super(rb);
	}
	
	@Override
	public void paintStateValue(Graphics2D g2, State s, double value, float cWidth, float cHeight) {
        DecimalFormat df = new DecimalFormat("#####.0");

		super.paintStateValue(g2, s, value, cWidth, cHeight);
		
		ObjectInstance xOb = this.xObjectInstance(s);
		ObjectInstance yOb = this.yObjectInstance(s);
		
		Attribute xAtt = xOb.getObjectClass().getAttribute(xAttName);
		Attribute yAtt = yOb.getObjectClass().getAttribute(yAttName);
		
		float domainXScale = 0f;
		float domainYScale = 0f;
		float xval = 0f;
		float yval = 0f;
		float width = 0f;
		float height = 0f;
		
		if(xAtt.type == Attribute.AttributeType.DISC){
			
			if(this.numXCells != -1){
				domainXScale = this.numXCells;
			}
			else{
				domainXScale = xAtt.discValues.size();
			}
			
			width = cWidth / domainXScale;
			xval = xOb.getDiscValForAttribute(xAttName)*width;
			
		}
		
		if(yAtt.type == Attribute.AttributeType.DISC){
			
			if(this.numYCells != -1){
				domainYScale = this.numYCells;
			}
			else{
				domainYScale = yAtt.discValues.size();
			}
			
			height = cHeight / domainYScale;
			yval = (float) (cHeight - height - yOb.getDiscValForAttribute(yAttName)*height + (height*.75));
			
		}
		
		g2.setColor(Color.black);
		Font font = new Font("sansserif", Font.BOLD, 10);
		g2.setFont(font);
		g2.drawString(df.format(value), xval, yval);			
	}

}