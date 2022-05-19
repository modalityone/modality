package mongoose.base.server.services.datasource;

<<<<<<< HEAD
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestMongooseLocalDataSourceProviderIT {

    private static final String HOME = "http://localhost:8082/mongoose-all-backoffice-application-gwt-1.0.0-SNAPSHOT/mongoose_all_backoffice_application_gwt/";

    @Test
    @DisplayName("The home page has the expected title.")
    public void hasExpectedTitle() {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        driver.get(HOME);
        String title = driver.getTitle();
        assertEquals("Mongoose Back-Office", title);
    }

}
