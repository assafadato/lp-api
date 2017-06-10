
/**
 * The MIT License
 * Copyright (c) 2017 LivePerson, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
import com.fasterxml.jackson.databind.JsonNode;
import com.liveperson.api.Idp;
import static com.liveperson.api.MessagingConsumer.*;
import java.util.Map;
import java.util.Optional;
import com.liveperson.api.infra.GeneralAPI;
import static com.liveperson.api.infra.ws.TimeoutScheduler.withIn;
import com.liveperson.api.infra.ws.WebsocketService;
import java.io.IOException;
import java.time.Duration;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.text.IsEmptyString.*;
import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.hamcrest.number.OrderingComparison.greaterThan;

import static org.junit.Assert.*;

public class MessagingTest {
    public static final String LP_ACCOUNT = System.getenv("LP_ACCOUNT");
    public static final String LP_DOMAINS = "https://" + Optional.ofNullable(System.getenv("LP_DOMAINS"))
            .orElse("adminlogin.liveperson.net");
    Map<String, String> domains;
    String jwt;

    @Before
    public void before() throws IOException {
        domains = GeneralAPI.getDomains(LP_DOMAINS, LP_ACCOUNT);
        assertThat(domains.keySet(),hasSize(greaterThan(0)));
        final JsonNode body = GeneralAPI.apiEndpoint(domains, Idp.class)
                .signup(LP_ACCOUNT)
                .execute().body();
        assertThat(body, is(not(nullValue())));
        jwt = body.path("jwt").asText();
        assertThat(jwt, not(isEmptyString()));
    }

    @Test
    public void testUMS() throws Exception {
        WebsocketService consumer = WebsocketService.create(consumerUrl(domains, LP_ACCOUNT, "wss"));
        JsonNode resp = consumer.request(initConnection(jwt), 
                withIn(Duration.ofSeconds(5))).get();        
        assertThat(resp.path("code").asInt(), is(200));
        consumer.getWs().close();
    }
}