package ch.heigvd.dai.polling;

import java.util.List;

public class Poll {

    private final int id;
    private final String question;
    private final List<PollOption> options;

    public Poll(int id, String question, List<PollOption> options) {
        this.id = id;
        this.question = question;

        // Prevent other classes from modifying the list directly.
        this.options = List.copyOf(options);
    }

    public int getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public List<PollOption> getOptions() {
        return options;
    }
}