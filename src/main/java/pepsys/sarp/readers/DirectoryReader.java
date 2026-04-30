package pepsys.sarp.readers;

import java.io.File;

import pepsys.sarp.graph.SarpGraph;
import pepsys.sarp.graph.nodes.FileNode;
import pepsys.sarp.graph.nodes.FolderNode;
import pepsys.sarp.graph.nodes.NodeType;
import pepsys.sarp.graph.nodes.SarpNode;

public class DirectoryReader {

    private SarpGraph graph;

    public DirectoryReader(SarpGraph graph) {
        this.graph = graph;
    }


    public void addRootDir(File rootDir) {

        if (rootDir.isDirectory()) {
            FolderNode rootNode = new FolderNode(rootDir.getName(), rootDir.getAbsolutePath(), rootDir);
            graph.addNode(rootNode);
            addChilds(rootDir);
        } else {
            System.err.println("Argument " + rootDir.getAbsolutePath() + " can not be converted into a graph. Maybe there is no root directory?");
        }
    }

    public void addFile(File file) {
        //System.out.println("Adding file " + file.getAbsolutePath() + " to SarpGraph " + service);
        SarpNode node = null;
        String fileName = file.getName();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
        String rawFileName = fileName.replace("." + extension, "");
        String path = file.getAbsolutePath();
        NodeType fileType = NodeType.getType(file);
        if (fileType == NodeType.FOLDER) {
            node = new FolderNode(rawFileName, path, file);
            graph.addNode(node);
            graph.addContainsEdge(file);
            addChilds(file);
        } else if (fileType.is(NodeType.FILE)) {
            node = new FileNode(rawFileName, path, fileType);
            graph.addNode(node);
            graph.addContainsEdge(file);
        } else if (fileType == NodeType.EMPTY) {
            return;
        } else {
            System.err.println("Could not add file " + path + " because it is neither a folder nor a file");
            return;
        }
    }

    private void addChilds(File folder) {
        File[] children = folder.listFiles();
        for (File childFile : children) {
            addFile(childFile);
        }
    }

}
