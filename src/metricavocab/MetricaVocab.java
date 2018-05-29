/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metricavocab;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Scanner;
import javafx.util.Pair;
/**
 *
 * @author mussandi
 */
public class MetricaVocab {

    static String cacheDir = "/Users/mussandi/Documents/SourcePAN12/.cache";
    static ArrayList<Pair<String, String[]>> files = new ArrayList<Pair<String, String[]>>();
    

    public static String readFile(String caminho) {
        // TODO code application logic here
        Scanner text = new Scanner(System.in);
        String readfile = null;
        Path path = Paths.get(caminho);
        try {
            byte[] textread = Files.readAllBytes(path);
            readfile = new String(textread);
            //System.out.println(readfile);
        } catch (Exception e) {
            System.out.println("file not found! " + caminho);
        }

        return readfile;
    }

    public static String[] fileToArray(String file) {

        return readFile(file).toLowerCase().split("[ ,.!;…\n]+");
    }

    public static String[] readCache(String cache) {
        String content = readFile(cache);
        if (content == null) {
            return null;
        }
        return content.split(" ");
    }

    public static void loadFiles(String dir, String ignore) {
        final File folder = new File(dir);
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.getName().charAt(0) == '.') {
                continue;
            }
            if (fileEntry.isDirectory()) {
                loadFiles(fileEntry.getPath(), ignore);
            } else {
                if (fileEntry.getPath().equals(ignore)) {
                    continue;
                }
                String path = fileEntry.getPath();
                files.add(new Pair<String, String[]>(path, fileToArray(path)));
            }
        }
    }
 public static ArrayList<String[]> fileToSenteces(String suspeciusFile) {
        ArrayList<String[]> sentences = new ArrayList<String[]>();
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.getDefault());
        String sentence = readFile(suspeciusFile);
        iterator.setText(sentence);
        int start = iterator.first();
        for (int end = iterator.next();
                end != BreakIterator.DONE;
                start = end, end = iterator.next()) {
            sentences.add(sentence.substring(start, end).toLowerCase().split("[ ,.!;…\n]+"));
        }

        return sentences;
    }
// public static ArrayList<String[]> srcfileToSenteces(String srcFile) {
//        ArrayList<String[]> srcSentences = new ArrayList<String[]>();
//        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.getDefault());
//        String srcSentence = loadFiles(srcFile);
//        iterator.setText(srcSentence);
//        int start = iterator.first();
//        for (int end = iterator.next();
//                end != BreakIterator.DONE;
//                start = end, end = iterator.next()) {
//            srcSentences.add(srcSentence.substring(start, end).toLowerCase().split("[ ,.!;…\n]+"));
//        }
//
//        return srcSentence;
//    }
    public static HashSet<String> string2Vocab(String s) {
        String[] palavras = s.split("[ ,.!;…\n]+");
        HashSet<String> vocab = new HashSet<>();
        for (String palavra : palavras) {
            vocab.add(palavra);
        }
        return vocab;
    }
//  public static HashSet<String> getIntersection(HashSet<String> a, HashSet<String> b) {
//        HashSet inter = (HashSet)a.clone();
//        inter.retainAll(b);
//        return inter;
//}
    public static HashSet<String> getIntersection(HashSet<String> a, HashSet<String> b) {
        a.retainAll(b);
        return a;
//        HashSet<String> inter = new HashSet<String>();
//        HashSet<String> menor;
//        HashSet<String> maior;
//        if (a.size() < b.size()) {
//            menor = a;
//            maior = b;
//        } else {
//            menor = b;
//            maior = a;
//        }
//
//        for (String word : menor) {
//            if (maior.contains(word)) {
//                inter.add(word);
//            }
//        }
//        return inter;
//
   }

    public static double metricSimilarity(HashSet suspeito, HashSet fonte) {

        HashSet vocabI = getIntersection(suspeito, fonte);

        HashSet vocabSuspeito = suspeito;

        return (double) vocabI.size() / (double) vocabSuspeito.size();

    }

    public static String cacheName(String name) {
        String[] path = name.split("/");
        return cacheDir + "/" + path[path.length - 1];
    }

    public static HashSet arrayToVocab(String outputName, String[] array, boolean synTranslation) throws IOException {
        File fcacheDir;
        FileWriter fw;
        BufferedWriter bw = null;
        HashSet voc = new HashSet();
        if (synTranslation) {
            if (outputName != null) {
                String[] cache = readCache(cacheName(outputName));
                if (cache != null) {
                    voc.addAll(Arrays.asList(cache));
                    return voc;
                }
                fcacheDir = new File(cacheDir);
                if (!fcacheDir.exists()) {
                    fcacheDir.mkdir();
                }

                fw = new FileWriter(cacheName(outputName));
                bw = new BufferedWriter(fw);
            }
            for (String word : array) {
                if (voc.contains(word)) {
                    continue;
                }
                String syn = LanguageTool.getLowestSynonym(word);
                voc.add(syn);
                if (outputName != null) {
                    bw.write(" " + syn);
                }
            }
            if (outputName != null) {
                bw.close();
            }
        } else {
            voc.addAll(Arrays.asList(array));
        }
        return voc;
    }

    public static void metricsFiles(String[] suspeito) throws IOException {
        HashSet vocS = arrayToVocab(null, suspeito, true);

        for (Pair<String, String[]> file : files) {
            HashSet vocF = arrayToVocab(file.getKey(), file.getValue(), true);
            double result = metricSimilarity(vocS, vocF);
            
            if (result >= 0.75) {
                System.out.println("\n Vocabulary similarity: " + result);
                System.out.println(file.getKey() + " ");
                
//                for (String word : file.getValue()) {
//                    System.out.print(word + " ");
//                }
//                System.out.println("");
       
            }
        }

    }

    public static void main(String[] args) throws MalformedURLException, IOException {
        //System.out.println("Calcular a similaridade: Sentence or File?");

        String suspeitoFile = "/Users/mussandi/Documents/SourcePAN12/DezBlocosPAN12.txt";

        loadFiles("/Users/mussandi/Documents/SourcePAN12", suspeitoFile);
        System.out.println(suspeitoFile);

        long start = System.currentTimeMillis();
        String[] suspeito = fileToArray(suspeitoFile);


        metricsFiles(suspeito);

        ArrayList<String[]> sentences = fileToSenteces(suspeitoFile);

        for (String[] sentence : sentences) {
            System.out.print("\nSuspeito: ");
            for (String word : sentence) {
                System.out.print(word + " ");
            }
            System.out.println("\n");
            metricsFiles(sentence);
        }
        
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Execution time: " + elapsed + " Millis");

    }

}
