package anonymized.sarp.readers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import anonymized.sarp.graph.SarpGraph;
import anonymized.sarp.graph.nodes.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import anonymized.sarp.graph.edges.EdgeRelation;
import anonymized.sarp.graph.edges.SarpEdge;
import anonymized.sarp.utils.SarFileUtils;


public class JsonSarpReader {
    SarpGraph graph;

    /**
     * read single json file,
     * that contains an array OR a JSON object
     *
     * @param file in json format
     * @return Sarp(sub-) graph with content of file
     */
    public SarpGraph readJson(File file, SarpNode hookNode) {
        System.out.println("Reading json");
        graph = new SarpGraph(file.getAbsolutePath());
        graph.addNode(hookNode);
        try {
            FileReader fileReader = new FileReader(file);
            JSONParser jsonParser = new JSONParser();
            Object parsedFile = jsonParser.parse(fileReader);

            /*
            SarpNode root = jsonToNode(null, parsedFile, SarFileUtils.createID());
             */

            /* 
            SarpNode root = new MapNode(file.getName(), SarFileUtils.createID());
            graph.addNode(root);
            graph.setRootNode(root);
            System.out.println("root: " + graph.getRootNode());
            */
            jsonToNode(hookNode, parsedFile);
//            JSONAware jsonObject = convertObjToJson(parsedFile);
//            convertObjToJsonObject(null, jsonObject);

        } catch (FileNotFoundException e) {
            System.err.println("File not found! Please try again/Check path!");
            e.printStackTrace();
            return null;
        } catch (IOException | ParseException e) {
            System.err.println("*****Something went wrong reading file" + file.getAbsolutePath());
            e.printStackTrace();
            return new SarpGraph("");
        } catch (NullPointerException e) {
            System.err.println("Nullpointer exception, maybe the outer structure is not an Object? in: " + file.getAbsolutePath());
            e.printStackTrace();

        }
        return graph;
    }


    @SuppressWarnings("unchecked")
    private SarpNode jsonToNode(SarpNode parent, Object child) {

        System.out.println("Adding json node of type: " + child.getClass().toString());

        if (child instanceof JSONObject) {
            JSONObject jso = (JSONObject) child;
            MapNode currentNode = new MapNode(parent.getName()+"_map", SarFileUtils.createID());
            graph.addVertex(currentNode);
            graph.addEdge(parent, currentNode, new SarpEdge(EdgeRelation.CONTAINS));

            jso.forEach((key, value) -> {
                System.out.println("Adding json key: " + key.toString() + " with value: " + value.toString());
                String keyStr = key.toString();
                SarpNode keyNode = new MapNode(keyStr, SarFileUtils.createID(keyStr));
                graph.addVertex(keyNode);
                graph.addEdge(currentNode,keyNode, new SarpEdge(EdgeRelation.CONTAINS));
                jsonToNode(keyNode, value);
                //graph.addVertex(valueNode);
                //graph.addEdge(keyNode, valueNode, new SarpEdge(EdgeRelation.VALUE));

                /* should be already covered?
                // collect node attributes in parent node
                if (parent != null) {
                    SarpNode colNode = graph.getChildByNameOrNull(parent, colStr);
                    if (colNode == null) {
                        colNode = new MapNode(colStr, SarFileUtils.createID(colStr));
                        graph.addVertex(colNode);
                        graph.addContainsEdge(parent, colNode);
                    }
                    graph.addContainsEdge(colNode, childNode);
                }
                */
            });
            return currentNode;
        } else if (child instanceof JSONArray) {
            ListNode currentNode = new ListNode("list", SarFileUtils.createID());
            graph.addVertex(currentNode);
            graph.addEdge(parent, currentNode, new SarpEdge(EdgeRelation.CONTAINS));
            JSONArray jsa = (JSONArray) child;
            jsa.forEach(value -> {
                SarpNode childNode = jsonToNode(currentNode, value);
                graph.addVertex(childNode);
                SarpEdge edge = new SarpEdge(EdgeRelation.CONTAINS);
                graph.addEdge(currentNode, childNode, edge);
            });
            return currentNode;

        } else {
            ValueNode currentNode = new ValueNode(child.toString(), SarFileUtils.createID(), child);
            graph.addVertex(currentNode);
            graph.addEdge(parent, currentNode, new SarpEdge(EdgeRelation.VALUE));
            return currentNode;
        }

    }

}
