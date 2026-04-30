package pepsys.sarp.analyzer;

import opennlp.tools.langdetect.Language;
import opennlp.tools.langdetect.LanguageDetector;
import opennlp.tools.langdetect.LanguageDetectorME;
import opennlp.tools.langdetect.LanguageDetectorModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import pepsys.sarp.graph.nodes.DataType;
import pepsys.sarp.graph.nodes.SarpNode;
import pepsys.sarp.graph.nodes.ValueNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Stream;

//import org.apache.opennlp-tools

public class NodeGroupAnalyzer {

    SarpNode parentNode;
    List<ValueNode> valueNodes;

    public InputStream loadModel(String binFileName) {
        InputStream is = getClass().getResourceAsStream("/models/" + binFileName);
        return is;
    }


    public NodeGroupAnalyzer(SarpNode parentNode, List<ValueNode> valueNodes) {
        this.parentNode = parentNode;
        this.valueNodes = valueNodes;
    }

    private Stream<ValueNode> nodeStream() {
        return valueNodes.stream();
    }


    public void analyze() {

try{
        if (nodeStream().allMatch(vn -> vn.getDataType() == DataType.NUMBER)) {
            LongSummaryStatistics stats = nodeStream().mapToLong(vn -> (long) vn.getContent()).summaryStatistics();
            parentNode.addStat("min", stats.getMin());
            parentNode.addStat("max", stats.getMax());
            parentNode.addStat("avg", stats.getAverage());
            parentNode.addStat("count", stats.getCount());
            parentNode.addStat("sum", stats.getSum());
        
        }}catch(ClassCastException e){
            System.err.println("A number type is set incorrectly, can not convert String to number in the analysis!");
        }

        if (nodeStream().allMatch(vn -> vn.getDataType() == DataType.TEXT)) {
            int totalChars =
                    nodeStream().map(node -> {
                        String text = node.getContentAString();
                        int numChars = text.length();
                        node.addStat("numChars", numChars);
                        return numChars;
                    }).reduce(0, Integer::sum);

            int totalWords =
                    nodeStream().map(node -> {
                        String text = node.getContentAString();
                        int numWords = text.split(" ").length;
                        node.addStat("numWords", numWords);
                        return numWords;
                    }).reduce(0, Integer::sum);


            try {

                LanguageDetectorModel ldm = new LanguageDetectorModel(loadModel("langdetect-183.bin"));
                LanguageDetector ld = new LanguageDetectorME(ldm);
//                ld.predictLanguage()

                nodeStream().forEach(node -> {
                    Language nodeLang = ld.predictLanguage(node.getContentAString());
                    node.addStat("languagePrediction", nodeLang.getLang());
                    node.addStat("languageConfidence", nodeLang.getConfidence());
                });

                String joinedContents =
                        nodeStream().map(n -> {
                            return n.getContentAString();
                        }).reduce("", (a, b) -> {
                            return a + " " + b;
                        });

                Language joinedLang = ld.predictLanguage(joinedContents);
                parentNode.addStat("joinedLanguagePrediction", joinedLang.getLang());
                parentNode.addStat("joinedLanguageConfidence", joinedLang.getConfidence());

//                System.out.println(is);
                SentenceModel model = new SentenceModel(loadModel("opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin"));
                SentenceDetectorME sdetector = new SentenceDetectorME(model);

                int totalSentences =
                        nodeStream().map(node -> {
                            String text = node.getContentAString();
                            int numSentences = sdetector.sentDetect(text).length;
                            node.addStat("numSentences", numSentences);
                            return numSentences;
                        }).reduce(0, Integer::sum);

                parentNode.addStat("totalSentences", totalSentences);
            } catch (IOException | NullPointerException e) {
                System.out.println("could not load OpenNLP Model. Try downloading models into src/resources/models/ ");
            }


            parentNode.addStat("textNode", true);
            parentNode.addStat("totalChars", totalChars);
            parentNode.addStat("totalWords", totalWords);
        }


    }


}
