package ar.com.leo.produccion.http;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;

import com.google.gson.Gson;

public class MachineComandService {
  private static final URI endpointUrl = URI.create("http://localhost:8000/machines/stop");

  public void sendStopCommand(List<Machine> machines) {
    HttpURLConnection connection = null;
    try {
      URL url = endpointUrl.toURL();
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
      connection.setRequestProperty("Content-Type", "application/json");

      Gson gson = new Gson();
      String json = gson.toJson(machines);

      System.out.println("Sending machines...");
      try (OutputStream os = connection.getOutputStream()) {
        byte[] input = json.getBytes("utf-8");
        os.write(input, 0, input.length);
      }

      int responseCode = connection.getResponseCode();

      if (responseCode == HttpURLConnection.HTTP_OK) {
        System.out.println("Machines received.");
      } else {
        System.out.println("Error: " + responseCode);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }
}
