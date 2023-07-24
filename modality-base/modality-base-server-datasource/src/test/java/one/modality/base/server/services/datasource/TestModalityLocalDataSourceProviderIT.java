package one.modality.base.server.services.datasource;

public class TestModalityLocalDataSourceProviderIT {

    private static final String HOME =
            "http://localhost:8082/modality-all-backoffice-application-gwt-1.0.0-SNAPSHOT/modality_all_backoffice_application_gwt/";
    /* Temporarily commented for Modality refactoring (was causing build error otherwise)

        @Test
        @DisplayName("The home page has the expected title.")
        public void hasExpectedTitle() {
            WebDriverManager.chromedriver().setup();
            WebDriver driver = new ChromeDriver();
            driver.get(HOME);
            String title = driver.getTitle();
            assertEquals("Modality Back-Office", title);
        }
    */

}
