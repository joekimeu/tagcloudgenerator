import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

public class TagCloudGeneratorWithJC {

    /**
     * Generates a tag cloud from user text with a specified number of words
     *
     * @author Joe Kimeu
     * @author Abby Brinkman
     *
     */

    //Additional Activity #2 completed on line 151

    /**
     * Definition of whitespace separators.
     */
    private static final String SEPARATORS = " \t\r\n.,:;?!-()/¡\"";

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code SEPARATORS}) or "separator string" (maximal length string of
     * characters in {@code SEPARATORS}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection entries(SEPARATORS) = {}
     * then
     *   entries(nextWordOrSeparator) intersection entries(SEPARATORS) = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection entries(SEPARATORS) /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of entries(SEPARATORS)  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of entries(SEPARATORS))
     * </pre>
     */
    private static String nextWordOrSeparator(String text, int position) {

        int count = position;
        if (!(SEPARATORS.indexOf(text.charAt(count)) == -1)) {
            while (count < text.length()
                    && !(SEPARATORS.indexOf(text.charAt(count)) == -1)) {
                count++;
            }
        } else {
            while (count < text.length()
                    && (SEPARATORS.indexOf(text.charAt(count)) == -1)) {
                count++;
            }
        }
        return text.substring(position, count);
    }

    /**
     * Used to sort the Map by its values in numerical order.
     */
    private static class IntegerLT implements Comparator<String> {

        Map<String, Integer> compareMap;

        IntegerLT(Map<String, Integer> compareMap) {
            this.compareMap = compareMap;
        }

        @Override
        public int compare(String o1, String o2) {
            int first = this.compareMap.get(o1);
            int second = this.compareMap.get(o2);
            if (second > first) {
                return 1;
            } else if (first > second) {
                return -1;
            } else {
                return o2.compareTo(o1);
            }
        }
    }

    /**
     * Inputs words and their respective count into a map, which is sorted
     * numerically and alphabetically. Then returns a queue with font sizes.
     *
     * @param input
     *            input containing the text to be read
     * @param sorted
     *            will store the words and occurrences in a TreeMap
     * @param n
     *            number of words in the tag cloud
     * @requires input, separators, and words are not null
     *
     * @updates sorted
     *
     * @ensures that the {@code Queue} fonts contains fonts for all words
     * @return a queue containing the font sizes of every word in the map
     */
    private static Queue<Integer> addWords(BufferedReader input,
            TreeMap<String, Integer> sorted, int n) {

        Map<String, Integer> words = new HashMap<String, Integer>();

        //storing all words into a map
        try {
            String in = input.readLine();
            while (in != null) {
                int i = 0;
                int len = in.length();
                while (i < len) {
                    String word = nextWordOrSeparator(in, i);
                    if (SEPARATORS.indexOf(in.charAt(i)) == -1) {
                        //makes program case insensitive
                        word = word.toLowerCase();
                        if (word.length() > 0) {
                            if (!words.containsKey(word)) {
                                words.put(word, 1);
                            } else {
                                int increase = words.get(word);
                                increase++;
                                words.put(word, increase);
                            }
                        }
                    }
                    i = i + word.length();
                }
                in = input.readLine();
            }
        } catch (IOException e) {
            System.err.print("Error reading file stream input");
        }

        //sorting the map by the integer values
        IntegerLT ci = new IntegerLT(words);
        TreeMap<String, Integer> si = new TreeMap<String, Integer>(ci);
        Set<Entry<String, Integer>> set = words.entrySet();
        for (Entry<String, Integer> x : set) {
            si.put(x.getKey(), x.getValue());
        }

        //adding "n" words to be sorted alphabetically
        int idx = 0;
        while (idx < n && !si.isEmpty()) {
            Map.Entry<String, Integer> countEntry = si.firstEntry();
            sorted.put(countEntry.getKey(), countEntry.getValue());
            si.remove(countEntry.getKey());
            idx++;
        }

        //finding the maximum and minimum counts for font sizes
        //using "n" words
        final int maxFont = 37;
        double max = Integer.MIN_VALUE;
        double min = Integer.MAX_VALUE;
        Set<Entry<String, Integer>> temp = sorted.entrySet();
        for (Entry<String, Integer> x : temp) {
            double tempValue = x.getValue().doubleValue();
            if (tempValue < min) {
                min = tempValue;
            } else if (tempValue > max) {
                max = tempValue;
            }
        }
        Queue<Integer> fonts = new LinkedList<Integer>();

        //adding "n" font sizes to a queue for every word in the map
        for (Map.Entry<String, Integer> entry : sorted.entrySet()) {
            final int fs = 11;
            int fontSize = fs;
            int tempValue = entry.getValue();
            if (tempValue > min) {
                double font = Math
                        .ceil((maxFont * (tempValue - min) / (max - min)));
                fontSize = fontSize + (int) font;
            }
            fonts.add(fontSize);
        }
        return fonts;
    }

    /**
     * Output the words, the number of times the words occurs, and their font
     * size in a tag cloud.
     *
     * @param fonts
     *            Queue with fonts sizes for every word
     * @param sorted
     *            TreeMap with the words and their occurence count
     * @param output
     *            to write text onto html file
     * @param n
     *            number of words in the tag cloud
     * @param fileInput
     *            the input text file name
     * @requires <pre>
     * {@code out.is_open and [inputFileName is not null]}
     * </pre>
     * @ensures information is displayed in output as a tag cloud
     */
    private static void outputList(Queue<Integer> fonts,
            TreeMap<String, Integer> sorted, PrintWriter output, int n,
            String fileInput) {
        assert output != null : "Violation of: out is not null";
        //assert output.isOpen() : "Violation of: out.is_open";

        output.println("<html>");
        output.println("<head>");
        output.println("<meta http-equiv=\"Content-Type\" content=\"text/html; "
                + "charset=UTF-8\" />");
        output.println(
                "<title>Top " + n + " words in " + fileInput + " </title>");
        output.println("<link href=\"doc/tagcloud.css\" rel=\"stylesheet\" "
                + "type =\"text/css\" >");
        output.println("</head>");
        output.println("<body>");
        output.println("<h2>Top " + n + " words in " + fileInput + "</h2>");

        output.println("<div class=\"cdiv\">");
        output.println("<p class = \"cbox\">");

        while (fonts.size() > 0) {
            output.println("<span style=\"cursor:default\" class=\"f"
                    + fonts.remove() + "\" title=\"count: "
                    + sorted.firstEntry().getValue() + "\">"
                    + sorted.firstEntry().getKey() + "</span>");
            sorted.remove(sorted.firstEntry().getKey());
        }

        output.println("</p>");
        output.println("</div>");
        output.println("</body>");
        output.println("</html>");
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in));
        BufferedReader input = null;
        String fileInput;
        PrintWriter output = null;
        String fileOutput = "";

        System.out.print("Please enter the input file name: ");
        try {
            fileInput = in.readLine();
        } catch (IOException e) {
            System.err.println("Error reading input file name");
            return;
        }
        try {
            input = new BufferedReader(new FileReader(fileInput));
        } catch (IOException e) {
            System.err.println("Error opening input file " + fileInput);
        }

        System.out.print("Please enter the output file name: ");
        try {
            fileOutput = in.readLine();
        } catch (IOException e) {
            System.err.println("Error reading output file name " + fileOutput);
            return;
        }
        try {
            output = new PrintWriter(
                    new BufferedWriter(new FileWriter(fileOutput)));
        } catch (IOException e) {
            System.err.println("Error writing to file output");
        }

        int n;
        System.out.print("Please enter the number of words in the tag cloud: ");
        try {
            n = Integer.parseInt(in.readLine());
        } catch (IOException e) {
            System.err.println("Error reading number of words in tag cloud");
            return;
        }

        //main methods
        TreeMap<String, Integer> sorted = new TreeMap<String, Integer>();

        Queue<Integer> fonts = addWords(input, sorted, n);

        outputList(fonts, sorted, output, n, fileInput);

        try {
            in.close();
            input.close();
            output.close();
        } catch (IOException e) {
            System.err.println("Error closing file");
        }
    }
}
