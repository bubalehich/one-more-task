package ru.clevertec.client;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.clevertec.server.Server;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientIT {
    @ParameterizedTest
    @ValueSource(ints = {1, 10, 100})
    void testClient(int dataCount) {
        var client = new Client(new Server(),dataCount);
        var expected = (1.0 + dataCount) * (dataCount / 2.0);

        client.doSend();

        assertEquals(expected, client.getAccumulatorValue());
        assertEquals(0, client.getData().size());
    }
}
