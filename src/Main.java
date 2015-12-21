import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class Main {

    public static class DummyException extends RuntimeException {
        public DummyException(String message) {
            super(message);
        }
    }

    public static void main(String[] args) throws Exception {
        ForkJoinPool pool = new ForkJoinPool(5);
        CompletableFuture<Integer> future = new CompletableFuture<>();

        CompletableFuture<String> futureMapped = future
                .thenApplyAsync(Main::transformInt, pool)
                .thenApplyAsync(Main::transformString, pool)
                .thenApplyAsync(value -> "BLA -- " + value, pool)
                .exceptionally(Main::handleFailure);

        // Replace with future.complete(13) to see the error handling
        future.complete(13);

        String result = futureMapped.get(1, TimeUnit.SECONDS);
        System.out.println(result);
    }


    private static String transformInt(Integer input) {
        if (input == 13) {
            throw new DummyException("BOOM");
        } else {
            return "I GOT: " + input;
        }
    }

    private static String transformString(String input) {
        return "MAPPED TO: " + input;
    }

    private static String handleFailure(Throwable thEx) {
        if (thEx.getCause() instanceof DummyException) {
            return "FOO I got exception: " + thEx.getMessage();
        } else if (thEx instanceof RuntimeException) {
            throw (RuntimeException) thEx;
        } else {
            throw new RuntimeException(thEx);
        }
    }
}
