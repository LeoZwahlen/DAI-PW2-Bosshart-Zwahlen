package ch.heigvd.dai.polling;

import java.util.List;

public class CommandHandler {

    private final PollService pollService;
    private final CommandParser parser = new CommandParser();

    private static final String UNKNOWN_COMMAND =
            "ERROR 1 Unknown command";

    private static final String INVALID_ARGUMENTS =
            "ERROR 2 Invalid arguments";

    private static final String POLL_NOT_FOUND =
            "ERROR 3 Poll not found";

    private static final String OPTION_NOT_FOUND =
            "ERROR 4 Option not found";

    public CommandHandler(PollService pollService) {
        this.pollService = pollService;
    }

    public String handle(String message) {

        try {

            List<String> arguments = parser.parse(message);

            switch (arguments.get(0)) {

                case "CREATE":
                    return create(arguments);

                case "LIST":
                    return list(arguments);

                case "SHOW":
                    return show(arguments);

                case "VOTE":
                    return vote(arguments);

                case "RESULTS":
                    return results(arguments);

                case "QUIT":
                    requireSize(arguments, 1);
                    return "OK";

                default:
                    return UNKNOWN_COMMAND;
            }

        } catch (IllegalArgumentException e) {
            return INVALID_ARGUMENTS;
        }
    }

    private String create(List<String> arguments) {

        if (arguments.size() < 4) {
            return INVALID_ARGUMENTS;
        }

        String question = arguments.get(1);
        List<String> options =
                arguments.subList(2, arguments.size());

        int pollId = pollService.createPoll(question, options);

        return "OK " + pollId;
    }

    private String list(List<String> arguments) {

        requireSize(arguments, 1);

        StringBuilder response = new StringBuilder();

        for (Poll poll : pollService.listPolls()) {
            response.append(poll.getId())
                    .append(" | ")
                    .append(poll.getQuestion())
                    .append("\n");
        }

        // END tells the client that the response is complete.
        response.append("END");

        return response.toString();
    }

    private String show(List<String> arguments) {

        requireSize(arguments, 2);

        int pollId = parser.parseId(arguments.get(1));
        Poll poll = pollService.getPoll(pollId);

        if (poll == null) {
            return POLL_NOT_FOUND;
        }

        StringBuilder response = new StringBuilder();

        for (PollOption option : poll.getOptions()) {
            response.append(option.getId())
                    .append(" | ")
                    .append(option.getText())
                    .append("\n");
        }

        response.append("END");

        return response.toString();
    }

    private String vote(List<String> arguments) {

        requireSize(arguments, 3);

        int pollId = parser.parseId(arguments.get(1));
        int optionId = parser.parseId(arguments.get(2));

        PollService.VoteResult result =
                pollService.vote(pollId, optionId);

        switch (result) {

            case OK:
                return "OK";

            case POLL_NOT_FOUND:
                return POLL_NOT_FOUND;

            case OPTION_NOT_FOUND:
                return OPTION_NOT_FOUND;

            default:
                return INVALID_ARGUMENTS;
        }
    }

    private String results(List<String> arguments) {

        requireSize(arguments, 2);

        int pollId = parser.parseId(arguments.get(1));
        Poll poll = pollService.getPoll(pollId);

        if (poll == null) {
            return POLL_NOT_FOUND;
        }

        StringBuilder response = new StringBuilder();

        for (PollOption option : poll.getOptions()) {
            response.append(option.getText())
                    .append(": ")
                    .append(option.getVotes())
                    .append("\n");
        }

        response.append("END");

        return response.toString();
    }

    private void requireSize(List<String> arguments, int size) {

        if (arguments.size() != size) {
            throw new IllegalArgumentException(
                    "Invalid number of arguments");
        }
    }
}