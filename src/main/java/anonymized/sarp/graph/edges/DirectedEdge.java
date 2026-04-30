package anonymized.sarp.graph.edges;

import anonymized.sarp.graph.nodes.SarpNode;

public class DirectedEdge extends SarpEdge{
    SarpNode source;
    SarpNode target;

    public DirectedEdge(EdgeRelation relation) {
        super(relation);
        this.setAttribute("directed");
    }
    public DirectedEdge(SarpNode source, SarpNode target) {
        super(EdgeRelation.RELATES);
        this.source = source;
        this.target = target;
        this.setAttribute("directed");
    }
    
    public DirectedEdge(SarpNode source, SarpNode target, EdgeRelation relation) {
        super(relation);
        this.source = source;
        this.target = target;
        this.setAttribute("directed");
    }
    public String toExtensiveString(){
        return "Edge is directed and connects " + this.getSource() + " " + description + " " + this.getTarget() + " %n";
    }
}
