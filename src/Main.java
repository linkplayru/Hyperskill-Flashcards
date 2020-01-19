import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) {

        String importOnStartFileName = null;
        String exportOnExitFileName = null;

        if (args.length != 0) {
            for (int i=0; i<args.length; i++) {
                if (args[i].equals("-import")) {
                    importOnStartFileName = args[i+1];
                } else if (args[i].equals("-export")) {
                    exportOnExitFileName = args[i+1];
                }
            }
        }

        Map<String, String> cards = new LinkedHashMap<>();
        Map<String, Integer> mistakes = new HashMap<>();
        List<String> history = new ArrayList<>();

        if (importOnStartFileName != null) {
            importFromFile(history, cards, mistakes, importOnStartFileName);
        }

        Scanner s = new Scanner(System.in);

        boolean running = true;
        while (running) {
            output(history,"Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):\n");
            String action = input(history, s);
            switch (action) {
                case "add":
                    add(history, cards, s);
                    break;
                case "remove":
                    remove(history, cards, mistakes, s);
                    break;
                case "import":
                    importCards(history, cards, mistakes, s);
                    break;
                case "export":
                    exportCards(history, cards, mistakes, s);
                    break;
                case "ask":
                    ask(history, cards, mistakes, s);
                    break;
                case "exit":
                    output(history, "Bye bye!\n");
                    if (exportOnExitFileName != null) {
                        exportToFile(history, cards, mistakes, exportOnExitFileName);
                    }
                    running = false;
                    break;
                case "log":
                    log(history, s);
                    break;
                case "hardest card":
                    harderstCard(history, mistakes);
                    break;
                case "reset stats":
                    resetStats(history, mistakes);
                    break;
                default:
                    break;
            }
        }
    }

    private static void log(List<String> history, Scanner s) {
        output(history, "File name:\n");
        String fileName = input(history, s);
        try (FileWriter writer = new FileWriter(fileName)) {
            for (String entry : history) {
                writer.write(entry);
            }
            output(history, "The log has been saved.\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void output(List<String> history, String output) {
        history.add(output);
        System.out.print(output);
    }

    private static String input(List<String> history, Scanner s) {
        String input = s.nextLine();
        history.add(input + "\n");
        return input;
    }

    private static void resetStats(List<String> history, Map<String, Integer> mistakes) {
        mistakes.clear();
        output(history, "Card statistics has been reset.\n\n");
    }

    private static void harderstCard(List<String> history, Map<String, Integer> mistakes) {
        if (mistakes.isEmpty()) {
            output(history,"There are no cards with errors.\n\n");
        } else {
            int maxMistakes = Collections.max(mistakes.values());
            List<String> hardestCards = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : mistakes.entrySet()) {
                if (entry.getValue() == maxMistakes) {
                    hardestCards.add(entry.getKey());
                }
            }
            if (hardestCards.size() == 1) {
                output(history,"The hardest card is \"" + hardestCards.get(0) + "\". You have " + maxMistakes + " errors answering it.\n\n");
            } else {
                output(history,"The hardest cards are ");
                for (int i=0; i<hardestCards.size(); i++) {
                    if (i == hardestCards.size()-1) {
                        output(history,"\"" + hardestCards.get(i) + "\"");
                    } else {
                        output(history,"\"" + hardestCards.get(i) + "\", ");
                    }
                }
                output(history,". You have " + maxMistakes + " errors answering them.\n\n");
            }
        }
    }

    private static void ask(List<String> history, Map<String, String> cards, Map<String, Integer> mistakes, Scanner s) {
        output(history,"How many times to ask?\n");
        int n = Integer.parseInt(input(history, s));
        Random random = new Random();
        ArrayList<String> terms = new ArrayList(cards.keySet());
        for (int i=0; i<n; i++) {
            int randomNum = random.nextInt(cards.size());
            String term = terms.get(randomNum);
            String def = cards.get(term);
            output(history,"Print the definition of \"" + term + "\":\n");
            String answer = input(history, s);
            if (answer.equals(def)) {
                output(history,"Correct answer.\n\n");
            } else {
                int termMistakes = 0;
                if (mistakes.containsKey(term)) {
                    termMistakes = mistakes.get(term);
                }
                mistakes.put(term, ++termMistakes);
                output(history,"Wrong answer. The correct one is \"" + def + "\"" + getHint(cards, answer) + "\n\n");
            }
        }
    }

    private static String getHint(Map cards, String answer) {
        if (cards.containsValue(answer)) {
            ArrayList<String> terms = new ArrayList(cards.keySet());
            ArrayList<String> defs = new ArrayList(cards.values());
            return ", you've just written the definition of \"" + terms.get(defs.indexOf(answer)) + "\".";
        } else {
            return ".";
        }
    }

    private static void exportCards(List<String> history, Map<String, String> cards, Map<String, Integer> mistakes,  Scanner s) {
        output(history,"File name:\n");
        String fileName = input(history, s);
        exportToFile(history, cards, mistakes, fileName);
    }

    private static void exportToFile(List<String> history, Map<String, String> cards, Map<String, Integer> mistakes, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            for (Map.Entry<String, String> entry : cards.entrySet()) {
                writer.write(entry.getKey() + "\n");
                writer.write(entry.getValue() + "\n");
                writer.write(mistakes.getOrDefault(entry.getKey(), 0) + "\n");
            }
            output(history,cards.size() + " cards have been saved.\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void importCards(List<String> history, Map cards, Map mistakes, Scanner s) {
        output(history,"File name:\n");
        String fileName = input(history, s);
        importFromFile(history, cards, mistakes, fileName);
    }

    private static void importFromFile(List history, Map cards, Map mistakes, String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            try (Scanner fileReader = new Scanner(file)) {
                int count = 0;
                while (fileReader.hasNextLine()) {
                    String term = fileReader.nextLine();
                    String def = fileReader.nextLine();
                    int termMistakes = Integer.parseInt(fileReader.nextLine());
                    cards.put(term, def);
                    mistakes.put(term, termMistakes);
                    count++;
                }
                output(history,count + " cards have been loaded.\n\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            output(history,"File not found.\n\n");
        }
    }

    private static void remove(List<String> history, Map cards, Map mistakes, Scanner s) {
        output(history,"The card:\n");
        String term = input(history, s);
        if (cards.containsKey(term)) {
            cards.remove(term);
            if (mistakes.containsKey(term)) {
                mistakes.remove(term);
            }
            output(history,"The card has been removed.\n\n");
        } else {
            output(history,"Can't remove \"" + term + "\": there is no such card.\n\n");
        }
    }

    private static void add(List<String> history, Map cards, Scanner s) {
        output(history,"The card:\n");
        String term = input(history, s);
        if (!cards.containsKey(term)) {
            output(history,"The definition of the card:\n");
            String def = input(history, s);
            if (!cards.containsValue(def)) {
                cards.put(term, def);
                output(history,"The pair (\"" + term + "\":\"" + def + "\") has been added.\n\n");
            } else {
                output(history,"The definition \"" + def + "\" already exists. Try again:\n\n");
            }
        } else {
            output(history,"The card \"" + term + "\" already exists. Try again:\n\n");
        }
    }


}