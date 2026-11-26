package ch.heigvd.dai.polling;

import java.util.ArrayList;
import java.util.List;

public class CommandParser {

    public List<String> parse(String line) {

        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Empty command");
        }

        String[] parts = line.strip().split(" +", 2);

        List<String> arguments = new ArrayList<>();
        arguments.add(parts[0]);

        if (parts.length == 1) {
            return arguments;
        }

        if (parts[0].equals("CREATE")) {
            parseQuotedArguments(parts[1], arguments);
        } else {
            for (String argument : parts[1].split(" +")) {
                arguments.add(argument);
            }
        }

        return arguments;
    }

    private void parseQuotedArguments(
            String text, List<String> arguments) {

        int position = 0;

        while (position < text.length()) {

            if (text.charAt(position) != '"') {
                throw new IllegalArgumentException(
                        "Expected a quoted argument");
            }

            // Find the closing quote of the current argument.
            int end = text.indexOf('"', position + 1);

            if (end == -1) {
                throw new IllegalArgumentException(
                        "Missing closing quote");
            }

            arguments.add(text.substring(position + 1, end));

            position = end + 1;

            if (position < text.length()
                    && text.charAt(position) != ' ') {
                throw new IllegalArgumentException(
                        "Arguments must be separated by spaces");
            }

            // Skip spaces before reading the next argument.
            while (position < text.length()
                    && text.charAt(position) == ' ') {
                position++;
            }
        }
    }

    public int parseId(String argument) {

        if (!argument.matches("[0-9]+")) {
            throw new IllegalArgumentException("Invalid ID");
        }

        int id = Integer.parseInt(argument);

        if (id < 1) {
            throw new IllegalArgumentException("Invalid ID");
        }

        return id;
    }
}
