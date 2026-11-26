package ch.heigvd.dai.polling;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PollService {

    private final Map<Integer, Poll> polls = new LinkedHashMap<>();
    private int nextPollId = 1;

    public enum VoteResult {
        OK,
        POLL_NOT_FOUND,
        OPTION_NOT_FOUND
    }

    // Synchronize access because several clients share this service.
    public synchronized int createPoll(
            String question, List<String> optionTexts) {

        validatePoll(question, optionTexts);

        List<PollOption> options = new ArrayList<>();

        for (int i = 0; i < optionTexts.size(); i++) {
            options.add(new PollOption(
                    i + 1, optionTexts.get(i), 0));
        }

        int pollId = nextPollId++;

        Poll poll = new Poll(pollId, question, options);
        polls.put(pollId, poll);

        return pollId;
    }

    public synchronized List<Poll> listPolls() {
        return new ArrayList<>(polls.values());
    }

    public synchronized Poll getPoll(int pollId) {
        return polls.get(pollId);
    }

    public synchronized VoteResult vote(int pollId, int optionId) {

        Poll poll = polls.get(pollId);

        if (poll == null) {
            return VoteResult.POLL_NOT_FOUND;
        }

        List<PollOption> options =
                new ArrayList<>(poll.getOptions());

        if (optionId < 1 || optionId > options.size()) {
            return VoteResult.OPTION_NOT_FOUND;
        }

        // Option IDs start at 1, but list indexes start at 0.
        PollOption option = options.get(optionId - 1);

        PollOption updatedOption = new PollOption(
                option.getId(),
                option.getText(),
                option.getVotes() + 1
        );

        options.set(optionId - 1, updatedOption);

        // Replace the poll so previous results remain unchanged.
        Poll updatedPoll = new Poll(
                poll.getId(),
                poll.getQuestion(),
                options
        );

        polls.put(pollId, updatedPoll);

        return VoteResult.OK;
    }

    private void validatePoll(
            String question, List<String> options) {

        if (question == null || question.isBlank()
                || containsInvalidCharacters(question)) {
            throw new IllegalArgumentException("Invalid question");
        }

        if (options == null
                || options.size() < 2
                || options.size() > 10) {
            throw new IllegalArgumentException(
                    "A poll must have between 2 and 10 options");
        }

        List<String> validatedOptions = new ArrayList<>();

        for (String option : options) {

            if (option == null || option.isBlank()
                    || containsInvalidCharacters(option)
                    || option.contains(":")
                    || option.contains(";")
                    || option.contains("=")
                    || option.getBytes(StandardCharsets.UTF_8).length > 64) {

                throw new IllegalArgumentException("Invalid option");
            }

            if (validatedOptions.contains(option)) {
                throw new IllegalArgumentException("Duplicate option");
            }

            validatedOptions.add(option);
        }
    }

    private boolean containsInvalidCharacters(String text) {
        return text.contains("\"")
                || text.contains("\n")
                || text.contains("\r");
    }
}