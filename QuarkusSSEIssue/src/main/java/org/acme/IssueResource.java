package org.acme;

import io.smallrye.mutiny.Multi;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestStreamElementType;

import java.util.stream.Stream;

@Path("/issue")
public class IssueResource {

    /**
     * CASE1: In this example an error will occur, but the client-side will not be notified, and will wait forever.
     */
    @GET
    @Path("/case1")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.TEXT_PLAIN)
    public Multi<Integer> case1() {
        Stream<Integer> inconsistentData = Stream.
                of("0", "1", "2", "3", "A", "4", "5").
                map(Integer::parseInt);
        return Multi.
                createFrom().
                items(inconsistentData);
    }

    /**
     * CASE2: In this example the result will be the same, even with a recover treatment.
     */
    @GET
    @Path("/case2")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.TEXT_PLAIN)
    public Multi<Integer> case2() {
        Stream<Integer> inconsistentData = Stream.
                of("0", "1", "2", "3", "A", "4", "5").
                map(Integer::parseInt);
        return Multi.
                createFrom().
                items(inconsistentData).
                onFailure().
                recoverWithMulti(throwable -> Multi.createFrom().failure(throwable) );
    }

    public record DataOrError<T>(T data, Throwable error) {

    }

    /**
     * CASE3: In this example the result will be the same, even with a more complex recover treatment.
     */
    @GET
    @Path("/case3")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.TEXT_PLAIN)
    public Multi<Integer> case3() {
        Stream<Integer> inconsistentData = Stream.
                of("0", "1", "2", "3", "A", "4", "5").
                map(Integer::parseInt);
        return Multi.
                createFrom().
                items(inconsistentData).
                onFailure().
                recoverWithMulti(throwable -> Multi.createBy().merging().streams(
                                                            Multi.createFrom().failure(throwable),
                                                            Multi.createFrom().empty()
                                                        )
                );
    }

    /**
     * CASE4 - In this example the stream will be interrupted correctly after the 3rd item,
     *         but no error event will be sent, and the client will never know about the rest of the stream.
     */
    @GET
    @Path("/case4")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.TEXT_PLAIN)
    public Multi<Integer> case4() {
        Stream<Integer> inconsistentData = Stream.
                of("0", "1", "2", "3", "A", "4", "5").
                map(Integer::parseInt);
        return Multi.
                createFrom().
                items(inconsistentData).
                onFailure().
                recoverWithCompletion();
    }

    /**
     * CASE5 - In this example the error will occur before the first event, but the issue remains.
     */
    @GET
    @Path("/case5")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.TEXT_PLAIN)
    public Multi<Integer> case5() {
        Stream<Integer> inconsistentData = Stream.
                of("A", "1", "2", "3", "4", "5").
                map(Integer::parseInt);
        return Multi.
                createFrom().
                items(inconsistentData);
    }

    /**
     * CASE6 - In this example the error will occur outside the stream, so the error will be sent to client-side (obviously).
     */
    @GET
    @Path("/case6")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.TEXT_PLAIN)
    public Multi<Integer> case6() {
        Integer.parseInt("A");
        Stream<Integer> inconsistentData = Stream.
                of("0", "1", "2", "3", "4", "5").
                map(Integer::parseInt);
        return Multi.
                createFrom().
                items(inconsistentData);
    }

    /**
     * CASE7 - In this example the error will be the unique item, and the issue will be the same the other cases (1 to 5).
     */
    @GET
    @Path("/case7")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.TEXT_PLAIN)
    public Multi<Object> case7() {
        return Multi.createFrom().failure(new RuntimeException("Teste")).onFailure().recoverWithCompletion();
    }
}
