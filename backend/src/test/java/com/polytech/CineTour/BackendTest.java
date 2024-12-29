package com.polytech.CineTour;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import com.polytech.utils.Console;

@SpringBootTest
public class BackendTest {
    private final CineTourApplicationTests cineTourApplicationTests = new CineTourApplicationTests();

    @Test
    void testContextLoads() {
        assertDoesNotThrow(() -> cineTourApplicationTests.contextLoads());
        Console.successln("Test contextLoads passed");
    }
}
