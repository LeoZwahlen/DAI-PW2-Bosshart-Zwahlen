package ch.heigvd.dai.polling;

public class PollOption {

    private final int id;
    private final String text;
    private final long votes;

    public PollOption(int id, String text, long votes) {
        this.id = id;
        this.text = text;
        this.votes = votes;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public long getVotes() {
        return votes;
    }
}