import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

@WebServlet({ "/resorts/weather" })

  private static final Logger logger = Logger.getLogger(WeatherServlet.class.getName());


  private transient SecretClient secretClient;
  @Override
  }
    try {
      if (weatherON != null && !server.isRegistered(weatherON)) {
        mbean = server.registerMBean(new AppInfo(), weatherON);
      }
    } catch (MBeanRegistrationException | NotCompliantMBeanException e) {
      e.printStackTrace();
    } catch (InstanceAlreadyExistsException e) {
      // ignore if already registered
    }

    // Initialize Azure Key Vault SecretClient using Managed Identity / DefaultAzureCredential
    String keyVaultUrl = System.getenv("KEY_VAULT_URL");
    if (keyVaultUrl != null && !keyVaultUrl.isEmpty()) {
      this.secretClient = new SecretClientBuilder()
          .vaultUrl(keyVaultUrl)
          .credential(new DefaultAzureCredentialBuilder().build())
          .buildClient();
    }
      }
    }
  }

  @Override
  protected void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {

    String methodName = "doGet";
    logger.entering(WeatherServlet.class.getName(), methodName);

    try {
      MBeanInfo weatherConfig = server.getMBeanInfo(weatherON);
    } catch (IntrospectionException | InstanceNotFoundException | ReflectionException e) {
      e.printStackTrace();
    }

    String city = request.getParameter("selectedCity");
    logger.log(Level.FINE, "requested city is " + city);

    String weatherAPIKey = System.getenv(WEATHER_API_KEY);
    String mockedKey = mockKey(weatherAPIKey);
    logger.log(Level.FINE, "weatherAPIKey is " + mockedKey);
    String weatherAPIKey = null;
    // Prefer Azure Key Vault if configured
    if (secretClient != null) {
      try {
        weatherAPIKey = secretClient.getSecret(WEATHER_API_KEY).getValue();
      } catch (Exception ex) {
        logger.log(Level.WARNING, "Unable to retrieve WEATHER_API_KEY from Key Vault, falling back to environment variable", ex);
      }
    }
    if (weatherAPIKey == null || weatherAPIKey.trim().isEmpty()) {
      weatherAPIKey = System.getenv(WEATHER_API_KEY);
    }
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
    }

    URL obj = null;
    HttpURLConnection con = null;
    try {
      obj = new URL(resturl);
      con = (HttpURLConnection) obj.openConnection();
      con.setRequestMethod("GET");
    } catch (MalformedURLException e1) {
      String errorMsg = "Caught MalformedURLException. Please make sure the url is correct.";
      ExceptionHandler.handleException(e1, errorMsg, logger);
    } catch (ProtocolException e2) {
      String errorMsg = "Caught ProtocolException: " + e2.getMessage()
          + ". Not able to set request method to http connection.";
      ExceptionHandler.handleException(e2, errorMsg, logger);
    } catch (IOException e3) {
      String errorMsg = "Caught IOException: " + e3.getMessage() + ". Not able to open connection.";
      ExceptionHandler.handleException(e3, errorMsg, logger);
    }

    int responseCode = con.getResponseCode();
    logger.log(Level.FINEST, "Response Code: " + responseCode);

    if (responseCode >= 200 && responseCode < 300) {

      BufferedReader in = null;
      ServletOutputStream out = null;

      try {
        in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine = null;
        StringBuffer responseStr = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
          responseStr.append(inputLine);
        }

        response.setContentType("application/json");
        out = response.getOutputStream();
        out.print(responseStr.toString());
        logger.log(Level.FINE, "responseStr: " + responseStr);
      } catch (Exception e) {
        String errorMsg = "Problem occured when processing the weather server response.";
        ExceptionHandler.handleException(e, errorMsg, logger);
      } finally {
        if (in != null) {
          in.close();
    serverEnv += System.getenv().toString();
    } else {
      String errorMsg = "REST API call " + resturl + " returns an error response: " + responseCode;
      ExceptionHandler.handleException(null, errorMsg, logger);
    }
  }
    // In cloud-native deployments, JNDI InitialContext is typically obtained with default settings
    try {
      return new InitialContext();
    } catch (NamingException e) {
      e.printStackTrace();
      return null;
    }
    ServletOutputStream out = null;

    try {
      String responseStr = defaultWeatherData.getDefaultWeatherData();
      response.setContentType("application/json");
      out = response.getOutputStream();
      out.print(responseStr.toString());
      logger.log(Level.FINEST, "responseStr: " + responseStr);
    } catch (Exception e) {
      String errorMsg = "Problem occured when getting the default weather data.";
      ExceptionHandler.handleException(e, errorMsg, logger);
    } finally {

      if (out != null) {
        out.close();
      }

      out = null;
    }
  }

  /**
   * Returns the weather information for a given city
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    doGet(request, response);
  }

  private static String mockKey(String toBeMocked) {
    if (toBeMocked == null) {
      return null;
    }
    String lastToKeep = toBeMocked.substring(toBeMocked.length() - 3);
    return "*********" + lastToKeep;
  }

  private String configureEnvDiscovery() {

    String serverEnv = "";

    serverEnv += com.ibm.websphere.runtime.ServerName.getDisplayName();
    serverEnv += com.ibm.websphere.runtime.ServerName.getFullName();

    return serverEnv;
  }

  private InitialContext setInitialContextProps() {

    Hashtable ht = new Hashtable();

    ht.put("java.naming.factory.initial", "com.ibm.websphere.naming.WsnInitialContextFactory");
    ht.put("java.naming.provider.url", "corbaloc:iiop:localhost:2809");

    InitialContext ctx = null;
    try {
      ctx = new InitialContext(ht);
    } catch (NamingException e) {
      e.printStackTrace();
    }

    return ctx;
  }
}
