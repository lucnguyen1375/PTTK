package vn.ptit.pttkm3.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public final class ApplicationContextListener implements ServletContextListener {
    public static final String DATA_SOURCE_ATTRIBUTE = DataSource.class.getName();
    public static final String CONFIG_ERROR_ATTRIBUTE = ApplicationContextListener.class.getName() + ".configError";

    private static final Logger LOGGER = Logger.getLogger(ApplicationContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        try {
            AppConfig config = AppConfig.fromEnvironment(System.getenv());
            context.setAttribute(DATA_SOURCE_ATTRIBUTE, DatabaseDataSource.create(config));
            context.setAttribute(CONFIG_ERROR_ATTRIBUTE, null);
            LOGGER.info("Application configuration loaded");
        } catch (AppConfig.InvalidConfigurationException exception) {
            context.setAttribute(CONFIG_ERROR_ATTRIBUTE, exception.getMessage());
            LOGGER.log(Level.SEVERE, "Application configuration is incomplete or invalid: {0}", exception.getMessage());
        } catch (SQLException exception) {
            context.setAttribute(CONFIG_ERROR_ATTRIBUTE, "Unable to configure database connection");
            LOGGER.log(Level.SEVERE, "Unable to configure database connection; errorType={0}",
                    exception.getClass().getSimpleName());
        }
    }
}
