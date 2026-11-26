package ch.heigvd.dai.polling;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PollServiceTest {

    @Test
    void generatesUniquePollIds() {
        PollService service = new PollService();

        int firstId = service.createPoll(
                "First question", List.of("Yes", "No"));

        int secondId = service.createPoll(
                "Second question", List.of("Yes", "No"));

        assertEquals(1, firstId);
        assertEquals(2, secondId);
    }

    @Test
    void rejectsDuplicateOptions() {
        PollService service = new PollService();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createPoll(
                        "Question", List.of("Yes", "Yes"))
        );
    }

    @Test
    void concurrentVotingDoesNotLoseVotes() throws Exception {
        PollService service = new PollService();

        int pollId = service.createPoll(
                "Question", List.of("Yes", "No"));

        ExecutorService executor =
                Executors.newFixedThreadPool(4);

        List<Future<?>> tasks = new ArrayList<>();

        try {

            // Simulate four clients voting 100 times each.
            for (int i = 0; i < 4; i++) {

                tasks.add(executor.submit(() -> {

                    for (int j = 0; j < 100; j++) {
                        assertEquals(
                                PollService.VoteResult.OK,
                                service.vote(pollId, 1)
                        );
                    }
                }));
            }

            // Wait for all threads to finish.
            for (Future<?> task : tasks) {
                task.get(10, TimeUnit.SECONDS);
            }

            Poll poll = service.getPoll(pollId);

            assertEquals(
                    400,
                    poll.getOptions().get(0).getVotes()
            );

        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void previousResultsRemainUnchanged() {
        PollService service = new PollService();

        int pollId = service.createPoll(
                "Question", List.of("Yes", "No"));

        Poll before = service.getPoll(pollId);

        service.vote(pollId, 1);

        Poll after = service.getPoll(pollId);

        assertEquals(0, before.getOptions().get(0).getVotes());
        assertEquals(1, after.getOptions().get(0).getVotes());
    }
}