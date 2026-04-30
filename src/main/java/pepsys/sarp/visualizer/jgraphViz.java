package pepsys.sarp.visualizer;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.awt.Color;

import javax.imageio.ImageIO;
import javax.xml.transform.Transformer;
import java.awt.image.BufferedImage;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jgrapht.ext.JGraphXAdapter;
import org.w3c.dom.Document;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxCellRenderer;

import pepsys.sarp.graph.SarpGraph;
import pepsys.sarp.graph.edges.SarpEdge;
import pepsys.sarp.graph.nodes.SarpNode;

public class jgraphViz {

    public static void createGraphImage(SarpGraph graph, String path) {
        System.out.println("Creating graph visualization...");
        JGraphXAdapter<SarpNode, SarpEdge> graphAdapter = new JGraphXAdapter<>(graph);

        // define styles per node type
        Map<String, String> nodeTypeStyles = Map.of(
            "FOLDER", "fillColor=#AED6F1;strokeColor=#2980B9;",
            "FILE", "fillColor=#AED6F1;strokeColor=#2980B9;",
            "CONTENT", "fillColor=#F9E79F;strokeColor=#B7950B;",
            "STRUCTURENODE", "fillColor=#8B8B8B;strokeColor=#8B8B8B;fontSize=0;shape=rhombus;",
            "VALUE", "fillColor=#F9E79F;strokeColor=#B7950B;shape=ellipse;",
            "EMPTY", "fillColor=#E8DAEF;strokeColor=#6C3483;shape=ellipse;dashed=1;"
        );

        // define styles per edge type
         // Apply special routing to REFERS_TO to reduce crossings
        Map<String, String> edgeTypeStyles = Map.of(
            "RELATES", "strokeColor=#000000;fontSize=0;",
            "VALUE", "strokeColor=#B7950B;fontSize=0;fontColor=#B7950B;",
            "REFERS_TO", "strokeColor=#B7950B;dashed=1;fontSize=0;"
        ); 


        // apply styles to each cell based on node type
        graphAdapter.getModel().beginUpdate();
        try {
            Map<SarpNode, mxICell> nodeToCellMap = graphAdapter.getVertexToCellMap();
            for (Map.Entry<SarpNode, mxICell> entry : nodeToCellMap.entrySet()) {
                String type = entry.getKey().getType();
                String style = nodeTypeStyles.getOrDefault(type,
                    "fillColor=#E8DAEF;strokeColor=#6C3483;"); // default style
                graphAdapter.getModel().setStyle(entry.getValue(), style);

                // Set fixed dimensions for hexagons
                if (type.equals("STRUCTURENODE")) {
                    mxGeometry geometry = graphAdapter.getModel().getGeometry(entry.getValue());
                    if (geometry != null) {
                        geometry.setWidth(10);
                        geometry.setHeight(10);
                    }
                }
            }
        } finally {
            graphAdapter.getModel().endUpdate();
            ;
        }

        // apply styles to edges
        graphAdapter.getModel().beginUpdate();
        try {
            Map<SarpEdge, mxICell> edgeToCellMap = graphAdapter.getEdgeToCellMap();
            for (Map.Entry<SarpEdge, mxICell> entry : edgeToCellMap.entrySet()) {
                String type = entry.getKey().getDescription().toString();
                String style = edgeTypeStyles.getOrDefault(type,
                    "strokeColor=#000000;fontSize=0;"); // default style
                //style += "edgeStyle=orthogonalEdgeStyle;"; // edge routing to reduce crossings, especially for lists
                graphAdapter.getModel().setStyle(entry.getValue(), style);
            }
        } finally {
            graphAdapter.getModel().endUpdate();
            ;
        }

        //mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
       
        // if there are more value nodes than structure nodes, use horizontal layout to reduce crossings, else vertical
        int valueNodeCount = 0; 
        int structureNodeCount = 0;
        for (SarpNode node : graph.vertexSet()) {
            if (node.getType().equals("VALUE")) {
                valueNodeCount++;
            } else if (node.getType().equals("STRUCTURENODE")) {
                structureNodeCount++;
            }
        }
        mxIGraphLayout layout = null;
        if(graph.getName().equals("personal_graph")){
            layout = new mxOrganicLayout(graphAdapter);
            ((mxOrganicLayout) layout).setOptimizeEdgeCrossing(true);
            ((mxOrganicLayout) layout).setOptimizeNodeDistribution(true);
        }else{
            boolean horizontal = valueNodeCount > 10;// set to false for vertical layout
            System.out.println("Applying " + (horizontal ? "horizontal" : "vertical") + " layout. Value nodes: " + valueNodeCount + ", Structure nodes: " + structureNodeCount);        
            layout = new mxCompactTreeLayout(graphAdapter, horizontal);
        }
        layout.execute(graphAdapter.getDefaultParent());

        // After layout: reduce VALUE node fontSize by 1 to fit better into the shape
        graphAdapter.getModel().beginUpdate();
        
        Map<SarpEdge, mxICell> edgeToCellMapAfter = graphAdapter.getEdgeToCellMap();
            for (Map.Entry<SarpEdge, mxICell> entry : edgeToCellMapAfter.entrySet()) {
                String style = entry.getValue().getStyle();
                if (style.contains("noEdgeStyle=1")) {
                    style = style.replace("noEdgeStyle=1","orthogonalEdgeStyle=1");
                }
                graphAdapter.getModel().setStyle(entry.getValue(), style);
            }
        try {
            Map<SarpNode, mxICell> nodeToCellMapAfter = graphAdapter.getVertexToCellMap();
            for (Map.Entry<SarpNode, mxICell> entry : nodeToCellMapAfter.entrySet()) {
                if (entry.getKey().getType().equals("VALUE")) {
                    mxICell cell = entry.getValue();
                    String style = cell.getStyle();
                    if (style == null) {
                        style = "";
                    }

                    int fs = -1;
                    int idx = style.indexOf("fontSize=");
                    if (idx != -1) {
                        int start = idx + "fontSize=".length();
                        int end = start;
                        while (end < style.length() && Character.isDigit(style.charAt(end))) {
                            end++;
                        }
                        try {
                            fs = Integer.parseInt(style.substring(start, end));
                        } catch (NumberFormatException ignored) {
                            fs = -1;
                        }
                    }

                    if (fs == -1) {
                        fs = 12; // fallback default
                    }

                    int newFs = Math.max(1, fs - 3); // reduce font size by 3, minimum 1
                    String newStyle;
                    if (style.contains("fontSize=")) {
                        newStyle = style.replaceAll("fontSize=\\d+", "fontSize=" + newFs);
                    } else {
                        if (!style.isEmpty() && !style.endsWith(";")) {
                            style = style + ";";
                        }
                        newStyle = style + "fontSize=" + newFs + ";";
                    }

                    graphAdapter.getModel().setStyle(cell, newStyle);
                }
            }
        } finally {
            graphAdapter.getModel().endUpdate();
        }


        // go to parent directory of path to save results there
        String parentPath = path;
         if(path.contains("/")){
             parentPath = path.substring(0, path.lastIndexOf("/"));
         }else if(parentPath.contains("\\")){
            parentPath = path.substring(0, path.lastIndexOf("\\"));
        }

        // create results folder if it doesn't exist
        File resultsDir = new File(parentPath + "/results/"+graph.getGraphName());
        if (!resultsDir.exists()) {
            resultsDir.mkdirs();
        }

        Document svgDocument = mxCellRenderer.createSvgDocument(graphAdapter, null, 2.0, Color.WHITE, null);
        Path imgPathSvg = Paths.get(resultsDir.getAbsolutePath()+"/" + graph.getGraphName() + "_graph.svg");
        File svgFile = new File(imgPathSvg.toString());
        // write svg document to file
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(svgDocument);
            FileWriter writer = new FileWriter(svgFile);
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            writer.close();
            System.out.println("SVG image saved to: " + svgFile.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Could not save SVG to: " + svgFile.getAbsolutePath());
        }
        // also render and save PNG
        
        Path pngPath = Paths.get(resultsDir.getAbsolutePath()+"/" + graph.getGraphName() + "_graph.png");
        File pngFile = pngPath.toFile();
        try {
                BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2.0, Color.WHITE, true, null);
                ImageIO.write(image, "PNG", pngFile);
                System.out.println("PNG saved to: " + pngFile.getAbsolutePath());
        } catch (Exception ex) {
                 System.out.println("Could not save PNG to: " + pngFile.getAbsolutePath());
        }
        }

}
