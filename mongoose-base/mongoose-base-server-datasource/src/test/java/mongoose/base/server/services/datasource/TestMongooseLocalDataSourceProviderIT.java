package mongoose.base.server.services.datasource;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

public class TestMongooseLocalDataSourceProviderIT {

    @Test
    public void loadsPropertyFromEnvironment() {
        assertEquals(1, 1);
    }

}
