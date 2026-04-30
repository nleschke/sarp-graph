package anonymized.sarp.graph;

import anonymized.sarp.graph.edges.DirectedEdge;
import anonymized.sarp.graph.edges.EdgeRelation;
import anonymized.sarp.graph.edges.SarpEdge;
import anonymized.sarp.graph.nodes.FileNode;
import anonymized.sarp.graph.nodes.ListNode;
import anonymized.sarp.graph.nodes.SarpNode;

import org.jgrapht.Graph;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.io.File;
import java.util.*;

public class SarpGraph extends DefaultDirectedGraph<SarpNode, SarpEdge> {
    private DefaultDirectedGraph<SarpNode, SarpEdge> graph = null; 
    private SarpNode rootNode = null;
    private Map<String, SarpNode> fileToNode = new LinkedHashMap<String, SarpNode>();
    String service = null; // identifies contollers, could also be convert to general metadata class
    String rootPath = null; //parent path

    public SarpGraph(String path) {
        super(SarpEdge.class);
        graph = this;
        service = "personal_graph";
    }

    public SarpGraph(String controller, String path) {
        super(SarpEdge.class);
        graph = this;
        service = controller;
        rootPath = path;
    }

    public Map<String, SarpNode> getFileToNode() {
        return fileToNode;
    }

    public String getRootPath(){
        return rootPath;
    }
//    public SarpGraph(String serviceName) {
//        this();
//        service = serviceName;
//    }

//    public SarpGraph(File file, String serviceName) {
//        this(serviceName);
//        if (file.isDirectory()) {
//            rootNode = new FolderNode(file.getName(), file.getAbsolutePath(), file);
//            addNode(rootNode);
//            addChilds(file);
//        } else {
//            System.err.println("Argument " + file.getAbsolutePath() + " can not be converted into a graph. Maybe there is no root directory?");
//            return;
//        }
//    }


    public SarpNode getRootNode() {
        return rootNode;
    }

    public String getName() {
        return service;
    }

    public void setRootNode(SarpNode rootNode) {
        this.rootNode = rootNode;
        if(rootPath == null && rootNode instanceof FileNode){
            FileNode root = (FileNode) rootNode;
            rootPath = root.getPath();
        }
    }

    public void addNode(SarpNode node) {
        fileToNode.putIfAbsent(node.getId(), node);
        graph.addVertex(node);
        if(graph.vertexSet().size()==1){
            System.out.println("Setting " + node + " as root node");
            setRootNode(node);
        }
    }

    public void addContainsEdge(SarpNode source, SarpNode target) {
        addEdge(source, target, new SarpEdge(EdgeRelation.CONTAINS));
        // for list nodes, which are ordered, we need to add the child to the list of children to create the order
        if(source instanceof ListNode){
            ListNode listNode = (ListNode) source;
            listNode.addChild(target);
        }
    }

     public void addDirectedEdge(SarpNode source, SarpNode target) {
        addEdge(source, target, new DirectedEdge(EdgeRelation.REFERS_TO));
    }

    public void addContainsEdge(File file) {
        File parent = file.getParentFile();
        SarpNode child = fileToNode.get(file.getAbsolutePath());
        SarpNode parentNode = fileToNode.get(parent.getAbsolutePath());
        SarpEdge edge = new SarpEdge(EdgeRelation.CONTAINS);
        if (child != null && parentNode != null) {
            graph.addEdge(parentNode, child, edge);
        } else {
            System.out.println("Could not add edge between source node " + child + " and target node " + parentNode);
        }
    }

    public SarpNode getChildByNameOrNull(SarpNode startNode, String name) {

        for (SarpEdge edge : iterables().outgoingEdgesOf(startNode)) {
            SarpNode node = edge.getTargetNode();
//            System.out.println("edge: " + node.getName());
            if (node.getName().equals(name)) {
                return node;
            }
        }

        return null;
    }

     public SarpNode[] getAncNodes(SarpNode node) {
        Set<SarpNode> children = getSubSet(node);
        children.remove(node);
        SarpNode[] array = new SarpNode[children.size()];
        return children.toArray(array);
    }

    public Set<SarpNode> getSubSet(SarpNode node) {
        Set<SarpNode> children = new HashSet<SarpNode>();
        BreadthFirstIterator<SarpNode, SarpEdge> iterator = new BreadthFirstIterator<>(graph, node);
        while (iterator.hasNext()) {
            SarpNode child = iterator.next();
            if (!child.equals(node)) {
                children.add(child);
            }
        }
        return children;
    }

    public Graph<SarpNode, SarpEdge> getSubGraph(SarpNode node) {
        return new AsSubgraph<>(this, getSubSet(node));
    }

    private int getNodeDepth(SarpNode node, Graph<SarpNode, SarpEdge> referenceGraph) {
        System.out.println("SarpNode " + node);
        BreadthFirstIterator<SarpNode, SarpEdge> iterator = new BreadthFirstIterator<SarpNode, SarpEdge>(referenceGraph);
        //exclude self, otherwise it returns nullpointer for depth = 0
        iterator.next();
        if (iterator.hasNext()) {
            return iterator.getDepth(node);
        } else {
            return 0;
        }
    }

    private int getSubTreeDepth(SarpNode node, Graph<SarpNode, SarpEdge> subGraph) {
        Graph<SarpNode, SarpEdge> reversedGraph = new EdgeReversedGraph<SarpNode, SarpEdge>(subGraph);
        System.out.println("Reversed Graph \n" + reversedGraph);
        return getNodeDepth(node, reversedGraph);
    }

    private int getNodeHeight(SarpNode node) {
//        node.addStat("closenessCentrality", nodeCentrality);
//        node.addStat("totalAncestors", getAncNodes(node).length);
//        node.addStat("depth", getNodeDepth(node, graph));
        if (graph.outgoingEdgesOf(node).size() > 1) {
            Set<SarpNode> subNodes = getSubSet(node);
            Graph<SarpNode, SarpEdge> subGraph = new AsSubgraph<SarpNode, SarpEdge>(graph, subNodes);
            System.out.println("Subgraph \n" + subGraph);
//            node.addStat("height", );
            return getSubTreeDepth(node, subGraph);
        } else return 0;

    }

    public String toString() {
        String answer = "Sarpgraph " + service + " has the following edges: %n";
        for (SarpEdge edge : graph.edgeSet()) {
            answer += " " + edge.toString();
        }
        return answer;
    }

    public String getGraphName() {
        // the service (controller) name is used as graph name for export, so that we can easily identify the graph in the export folder
        return service;
    }
}
