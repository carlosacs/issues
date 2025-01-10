package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.SseEvent;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class IssueResourceTest {

    static String EXPECTED_RESULT = """
            data:0
            
            data:1
            
            data:2
            
            data:3
            
            event:error
            data:java.lang.NumberFormatException: For input string: "A"
            
            """;

    private void runTest(String path) {
        given()
                .when()
                .get(path)
                .then()
                .statusCode(200)
                .contentType(MediaType.SERVER_SENT_EVENTS)
                .body(is(EXPECTED_RESULT));
    }

    /**
     * CASE1: In this example an error will occur, but the client-side will not be notified, and will wait forever.
     */
    @Test
    void testCase1() {
        runTest("/issue/case1");
    }


    /**
     * CASE2: In this example the result will be the same, even with a recover treatment.
     */
    @Test
    void testCase2() {
        runTest("/issue/case2");
    }

    /**
     * CASE3: In this example the result will be the same, even with a more complex recover treatment.
     */
    @Test
    void testCase3() {
        runTest("/issue/case3");
    }


    /**
     * CASE4 - In this example the stream will be interrupted correctly after the 3rd item,
     *         but no error event will be sent, and the client will never know about the rest of the stream.
     */
    @Test
    void testCase4() {
        runTest("/issue/case4");
    }

    /**
     * CASE5 - In this example the error will occur before the first event, but the issue remains.
     */
    @Test
    void testCase5() {
        runTest("/issue/case5");
    }

    /**
     * CASE6 - In this example the error will occur outside the stream, so the error will be sent to client-side (obviously).
     */
    @Test
    void testCase6() {
        runTest("/issue/case5");
    }

    /**
     * CASE7 - In this example the error will be the unique item, and the issue will be the same the other cases (1 to 5).
     */
    @Test
    void testCase7() {
        runTest("/issue/case7");
    }

    /**
     * CASE8 - In this example we tried to use an emitter and control the emissions manually, but no success.
     */
    @Test
    void testCase8() {
        runTest("/issue/case8");
    }

}