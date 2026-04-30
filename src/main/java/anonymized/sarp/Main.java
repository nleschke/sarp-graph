package anonymized.sarp;

import anonymized.sarp.analyzer.SarpGraphAnalyzer;
import anonymized.sarp.graph.SarpGraph;
import anonymized.sarp.graph.nodes.ListNode;
import anonymized.sarp.graph.nodes.SarpNode;
import anonymized.sarp.readers.ContentReader;
import anonymized.sarp.readers.DirectoryReader;
import anonymized.sarp.visualizer.jgraphViz;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import org.jgrapht.Graphs;


public class Main {
    public static void main(String[] args) {
      
        String controller =""; 
        // Phase 0. Determine Directory to Read
        String path = determineRootPath();
        long startTime = System.currentTimeMillis();
        // convert to SARP graph
       SarpGraph graph = convertGraph(path);

       /*
        System.out.println("Checking for Nodes within graph");
        for (SarpNode node : graph.iterables().vertices()) {
            System.out.println(" " + node.toExtendedString());
        }
        */
       long endTime = System.currentTimeMillis();
        System.out.println("** After Converter: Graph " + graph.getName() + " has " + graph.vertexSet().size() + " nodes and " + graph.edgeSet().size() + " edges. And was essembled in " + (endTime - startTime) + " ms.");

        startTime = System.currentTimeMillis();
        System.out.println("** Starting analysis of graph " + graph.getName() + "... at time: " + startTime);
        // Phase 3: Semantic Analysis & NER
        analyzeGraph(graph);
        endTime = System.currentTimeMillis();
        System.out.println("** After Analyzer: Graph " + graph.getName() + " has " + graph.vertexSet().size() + " nodes and " + graph.edgeSet().size() + " edges. Analysis took " + (endTime - startTime) + " ms.");
        
        // Phase 4: Visualization & Export
        visualizeGraph(graph, path);

        // Phase 5: integration of more SARPs
        SarpGraph personalGraph = new SarpGraph("personal_graph");
        SarpNode root = new ListNode("personal_graph", "main_node_personal_graph");
        personalGraph.addVertex(root);
        personalGraph.setRootNode(root);

        // for visualization: filter graph to only include profile

        Graphs.addGraph(personalGraph, graph);
        personalGraph.addContainsEdge(root, graph.getRootNode());
        integrateMoreSarps(personalGraph);

        visualizeGraph(personalGraph, path);
    }

    private static SarpGraph convertGraph(String path){
        String[] directories = path.split("\\\\|/"); // split by both \ and / to be safe across OS
        String controller = directories[directories.length - 1]; // use last part of path as controller name
        System.out.println("Using controller name: " + controller);
        File rootDir = new File(path);
        System.out.println("Converting SARP from: " + rootDir.getAbsolutePath());

        // Iteration 1: spanning tree from directory, directed but unordered
        SarpGraph graph = new SarpGraph(controller,path);
        DirectoryReader rootReader = new DirectoryReader(graph);
        rootReader.addRootDir(rootDir);
        
        // Phase 2: generate augmented graph by adding content (now it is a directed graph)
        ContentReader contentReader = new ContentReader(graph);
        contentReader.readAllFileContents();

        return graph;
    }

    private static void analyzeGraph(SarpGraph graph) {
        SarpGraphAnalyzer analyzer = new SarpGraphAnalyzer(graph);
        analyzer.analyze();
        //analyzer.printAllStats();
    }

    private static void visualizeGraph(SarpGraph graph, String path) {
        jgraphViz.createGraphImage(graph,path);
    }

    private static void integrateMoreSarps(SarpGraph personalGraph) {
        Scanner userInput = new Scanner(System.in);
        System.out.println("Want to analyze another SARP? (y/n)");
        String answer = userInput.nextLine();
        if(answer.equalsIgnoreCase("y")) {
            System.out.println("Enter path to unzipped access data");
            String path = userInput.nextLine();
            userInput.close();
            SarpGraph newGraph = convertGraph(path);
            analyzeGraph(newGraph);
            visualizeGraph(newGraph, path);
            Graphs.addGraph(personalGraph, newGraph);
            personalGraph.addContainsEdge(personalGraph.getRootNode(), newGraph.getRootNode());
            System.out.println("Integrated graph " + newGraph.getName() + " into personal graph");
            integrateMoreSarps(personalGraph); // ask again if user wants to add more graphs
        }else{
            visualizeGraph(personalGraph,"validation/results/integration/personal_graph"); // visualize personal graph with all integrated graphsn
            System.out.println("Finished. Bye!");
            userInput.close();
            return;
        }
    }
    private static String determineRootPath() {
        String path;
        // if validation exists, use that, else ask user
        if (new File("validation").exists()) {
            path = "validation/minimal"; 
        } else {
            Scanner userInput = new Scanner(System.in);
            //  System.out.println("Enter controller name");
            // controller = userInput.nextLine();
            System.out.println("Enter path to unzipped access data");
            path = userInput.nextLine();
            path.replace(" ", "\\ ");
            userInput.close();
        }
        return path;
    }
}
