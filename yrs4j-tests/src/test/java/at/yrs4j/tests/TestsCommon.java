package at.yrs4j.tests;

import at.yrs4j.api.Yrs4J;
import at.yrs4j.libnative.linux.LinuxLibLoader;
import at.yrs4j.libnative.windows.WindowsLibLoader;
import at.yrs4j.wrapper.interfaces.EncodingType;
import at.yrs4j.wrapper.interfaces.YDoc;
import at.yrs4j.wrapper.interfaces.YOptions;
import org.junit.jupiter.api.BeforeAll;

public class TestsCommon {
    
    @BeforeAll
    public static void setupYrs4J() {
        try {
            // Try Linux first, then Windows
            try {
                Yrs4J.init(LinuxLibLoader.create());
            } catch (Exception e) {
                Yrs4J.init(WindowsLibLoader.create());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Yrs4J with any native library", e);
        }
    }
    
    protected static YDoc createYDocWithId(long id) {
        YOptions options = YOptions.create();
        options.setEncoding(EncodingType.Y_OFFSET_UTF16);
        options.setId(id);
        options.setSkipGc(false);
        
        return YDoc.createWithOptions(options);
    }
}