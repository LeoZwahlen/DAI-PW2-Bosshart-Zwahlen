package ch.heigvd.dai.polling;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandHandlerTest {

    private final PollService service = new PollService();
    private final CommandHandler handler = new CommandHandler(service);

    @Test
    void createsPoll() {
        assertEquals(
                "OK 1",
                handler.handle("CREATE \"Favorite language?\" \"Java\" \"C++\"")
        );
    }

    @Test
    void listsEmptyPolls() {
        assertEquals("END", handler.handle("LIST"));
    }

    @Test
    void listsPolls() {
        handler.handle("CREATE \"Favorite language?\" \"Java\" \"C++\"");

        assertEquals(
                "1 | Favorite language?\nEND",
                handler.handle("LIST")
        );
    }

    @Test
    void showsPollOptions() {
        handler.handle("CREATE \"Favorite language?\" \"Java\" \"C++\"");

        assertEquals(
                "1 | Java\n2 | C++\nEND",
                handler.handle("SHOW 1")
        );
    }

    @Test
    void votesAndReturnsResults() {
        handler.handle("CREATE \"Favorite language?\" \"Java\" \"C++\"");

        assertEquals("OK", handler.handle("VOTE 1 2"));

        assertEquals(
                "Java: 0\nC++: 1\nEND",
                handler.handle("RESULTS 1")
        );
    }

    @Test
    void allowsMultipleVotes() {
        handler.handle("CREATE \"Favorite language?\" \"Java\" \"C++\"");

        handler.handle("VOTE 1 2");
        handler.handle("VOTE 1 2");

        assertEquals(
                "Java: 0\nC++: 2\nEND",
                handler.handle("RESULTS 1")
        );
    }

    @Test
    void rejectsUnknownCommand() {
        assertEquals(
                "ERROR 1 Unknown command",
                handler.handle("HELLO")
        );
    }

    @Test
    void rejectsInvalidArguments() {
        assertEquals(
                "ERROR 2 Invalid arguments",
                handler.handle("VOTE abc 1")
        );

        assertEquals(
                "ERROR 2 Invalid arguments",
                handler.handle("LIST 1")
        );

        assertEquals(
                "ERROR 2 Invalid arguments",
                handler.handle("CREATE \"Question\" \"Only one option\"")
        );
    }

    @Test
    void rejectsInvalidQuotedArguments() {
        assertEquals(
                "ERROR 2 Invalid arguments",
                handler.handle("CREATE \"Missing closing quote")
        );
    }

    @Test
    void rejectsUnknownPoll() {
        assertEquals(
                "ERROR 3 Poll not found",
                handler.handle("RESULTS 99")
        );
    }

    @Test
    void rejectsUnknownOption() {
        handler.handle("CREATE \"Favorite language?\" \"Java\" \"C++\"");

        assertEquals(
                "ERROR 4 Option not found",
                handler.handle("VOTE 1 99")
        );
    }

    @Test
    void acceptsQuitCommand() {
        assertEquals("OK", handler.handle("QUIT"));
    }
}