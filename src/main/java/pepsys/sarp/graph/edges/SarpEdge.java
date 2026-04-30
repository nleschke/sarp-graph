package pepsys.sarp.graph.edges;

import java.util.ArrayList;

import org.jgrapht.graph.DefaultEdge;

import pepsys.sarp.graph.nodes.SarpNode;

public class SarpEdge extends DefaultEdge{
    EdgeRelation description;

    ArrayList<String> attributes; //optional, includes direction, for Iteration 3: Knowledge Discovery, could include keywords or desciptions of the relationship between source and target node

    public SarpEdge(EdgeRelation description){
        super();
        this.description = description;
        attributes = new ArrayList<>();
    }

    public void setAttribute(String attribute) {
        attributes.add(attribute);
    }

    public EdgeRelation getDescription(){
        return description;
    }

    public SarpNode getSourceNode(){
        return (SarpNode) this.getSource();
    }

    public SarpNode getTargetNode(){
        return (SarpNode) this.getTarget();
    }

    public String toExtensiveString(){
        return "Edge: " + this.getSource() + " " + description + " " + this.getTarget() + " %n";
    }
    public String toString(){
        return description.toString();
    }
}
