

import static org.junit.Assert.assertEquals;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ar.com.leo.produccion.http.Machine;
import ar.com.leo.produccion.http.MachineComandService;

public class MachineCommandServiceTest {
  
  private MachineComandService service;

  @Before
  public void setUp() {
    service = new MachineComandService();
  }

  @Test
  public void testSendStopCommand() throws Exception {
    List<Machine> machines = Arrays.asList(new Machine(301), new Machine(302));
    service.sendStopCommand(machines);

    // Verify the response code and response message
    URL url = URI.create("http://localhost:8000/machines/stop").toURL();
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    int responseCode = connection.getResponseCode();

    assertEquals(HttpURLConnection.HTTP_OK, responseCode);
  }

  @After
  public void tearDown() {}
}
