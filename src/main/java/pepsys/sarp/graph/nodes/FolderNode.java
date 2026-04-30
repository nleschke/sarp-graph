package pepsys.sarp.graph.nodes;

import java.io.File;

public class FolderNode extends SarpNode{
    protected String path;
    protected File content;

    public FolderNode(String name, String path, File content) {
        super(name, path);
        this.path = path;
        this.content = content;
    }
}
