package ru.clevertec.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.clevertec.server.Server;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ClientTest {
    private static final int DEFAULT_DATA_COUNT = 10;
    @Mock
    private Server server;

    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client(server, DEFAULT_DATA_COUNT);
    }

    @Test
    void testInitialization() {
        var dataCount = 10;
        var client = new Client(server, dataCount);

        assertEquals(dataCount, client.getData().size());
        assertEquals(0, client.getAccumulatorValue());
    }
}
