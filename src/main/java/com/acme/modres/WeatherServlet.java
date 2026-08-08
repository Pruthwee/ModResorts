package com.acme.modres;

import com.acme.modres.db.CustomerInformationService;
import com.acme.modres.exception.ExceptionHandler;
import com.acme.modres.mbean.AppInfo;
import com.acme.modres.security.AzureKeyVaultSecretProvider;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet({ "/resorts/weather" })
public class WeatherServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final String WEATHER_API_KEY_SECRET_ENV = "AZURE_KEY_VAULT_WEATHER_SECRET_NAME";
  private static final Logger logger = Logger.getLogger(WeatherServlet.class.getName());

  @Inject
  private CustomerInformationService customerInfo;

  private final AzureKeyVaultSecretProvider secretProvider = new AzureKeyVaultSecretProvider();
  MBeanServer server;
  ObjectName weatherON;
  ObjectInstance mbean;

  @Override
  public void init() {
    server = ManagementFactory.getPlatformMBeanServer();
    try {
      weatherON = new ObjectName("com.acme.modres.mbean:name=appInfo");
      if (weatherON != null) {
        mbean = server.registerMBean(new AppInfo(), weatherON);
      }
    } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException
        | NotCompliantMBeanException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void destroy() {
    if (mbean != null) {
      try {
        server.unregisterMBean(weatherON);
      } catch (MBeanRegistrationException | InstanceNotFoundException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  protected void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {
    logger.entering(WeatherServlet.class.getName(), "doGet");

    try {
      MBeanInfo weatherConfig = server.getMBeanInfo(weatherON);
    } catch (IntrospectionException | InstanceNotFoundException | ReflectionException e) {
      e.printStackTrace();
    }

    String city = request.getParameter("selectedCity");
    logger.log(Level.FINE, "requested city is " + city);

    String secretName = System.getenv(WEATHER_API_KEY_SECRET_ENV);
    if (secretName == null || secretName.trim().isEmpty()) {
      secretName = "weather-api-key";
    }
    String weatherAPIKey = secretProvider.getSecret(secretName);
    logger.log(Level.FINE, "weatherAPIKey is " + mockKey(weatherAPIKey));

    if (weatherAPIKey != null && weatherAPIKey.trim().length() > 0) {
      logger.info("weatherAPIKey is found in Azure Key Vault, system will provide the real time weather data for the city " + city);
      getRealTimeWeatherData(city, weatherAPIKey, response);
    } else {
      logger.info("weatherAPIKey is not found in Azure Key Vault, will provide default weather data for the city " + city);
      getDefaultWeatherData(city, response);
    }
  }

  private void getRealTimeWeatherData(String city, String apiKey, HttpServletResponse response)
      throws ServletException, IOException {
    String resturl = null;
    String resturlbase = Constants.WUNDERGROUND_API_PREFIX + apiKey + Constants.WUNDERGROUND_API_PART;

    if (Constants.PARIS.equals(city)) {
      resturl = resturlbase + "France/Paris.json";
    } else if (Constants.LAS_VEGAS.equals(city)) {
      resturl = resturlbase + "NV/Las_Vegas.json";
    } else if (Constants.SAN_FRANCISCO.equals(city)) {
      resturl = resturlbase + "/CA/San_Francisco.json";
    } else if (Constants.MIAMI.equals(city)) {
      resturl = resturlbase + "FL/Miami.json";
    } else if (Constants.CORK.equals(city)) {
      resturl = resturlbase + "ireland/cork.json";
    } else if (Constants.BARCELONA.equals(city)) {
      resturl = resturlbase + "Spain/Barcelona.json";
    } else {
      String errorMsg = "Sorry, the weather information for your selected city: " + city +
          " is not available.  Valid selections are: " + Constants.SUPPORTED_CITIES;
      ExceptionHandler.handleException(null, errorMsg, logger);
      return;
    }

    HttpURLConnection con = null;
    try {
      URL obj = new URL(resturl);
      con = (HttpURLConnection) obj.openConnection();
      con.setRequestMethod("GET");
    } catch (MalformedURLException e1) {
      ExceptionHandler.handleException(e1, "Caught MalformedURLException. Please make sure the url is correct.", logger);
      return;
    } catch (ProtocolException e2) {
      ExceptionHandler.handleException(e2,
          "Caught ProtocolException: " + e2.getMessage() + ". Not able to set request method to http connection.", logger);
      return;
    } catch (IOException e3) {
      ExceptionHandler.handleException(e3, "Caught IOException: " + e3.getMessage() + ". Not able to open connection.", logger);
      return;
    }

    int responseCode = con.getResponseCode();
    logger.log(Level.FINEST, "Response Code: " + responseCode);

    if (responseCode >= 200 && responseCode < 300) {
      try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
          ServletOutputStream out = response.getOutputStream()) {
        String inputLine;
        StringBuilder responseStr = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
          responseStr.append(inputLine);
        }
        response.setContentType("application/json");
        out.print(responseStr.toString());
        logger.log(Level.FINE, "responseStr: " + responseStr);
      } catch (Exception e) {
        ExceptionHandler.handleException(e, "Problem occured when processing the weather server response.", logger);
      }
    } else {
      ExceptionHandler.handleException(null, "REST API call " + resturl + " returns an error response: " + responseCode, logger);
    }
  }

  private void getDefaultWeatherData(String city, HttpServletResponse response)
      throws ServletException, IOException {
    DefaultWeatherData defaultWeatherData = null;
    try {
      defaultWeatherData = new DefaultWeatherData(city);
    } catch (UnsupportedOperationException e) {
      ExceptionHandler.handleException(e, e.getMessage(), logger);
    }

    try (ServletOutputStream out = response.getOutputStream()) {
      String responseStr = defaultWeatherData.getDefaultWeatherData();
      response.setContentType("application/json");
      out.print(responseStr);
      logger.log(Level.FINEST, "responseStr: " + responseStr);
    } catch (Exception e) {
      ExceptionHandler.handleException(e, "Problem occured when getting the default weather data.", logger);
    }
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }

  private static String mockKey(String toBeMocked) {
    if (toBeMocked == null || toBeMocked.length() < 3) {
      return toBeMocked;
    }
    String lastToKeep = toBeMocked.substring(toBeMocked.length() - 3);
    return "*********" + lastToKeep;
  }
}
