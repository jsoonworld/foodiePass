package foodiepass.server.currency.infra;

import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class MockApiServer {

    public static void main(String[] args) throws InterruptedException {
        WireMockServer wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        System.out.println("Mock API Server is running on port: " + wireMockServer.port());

        stubFor(get(urlPathMatching("/finance/quote/.*"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withFixedDelay(10000)
                ));

        Thread.currentThread().join();
    }
}
