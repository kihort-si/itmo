package ru.itmo.blps.app.security.jaas;

import java.util.Map;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.itmo.blps.app.security.xml.XmlUserStore;

@org.springframework.context.annotation.Configuration
public class JaasLoginConfiguration {
    public static final String APPLICATION_NAME = "blps-xml-store";

    @Bean
    public Configuration jaasConfiguration(XmlUserStore xmlUserStore, PasswordEncoder passwordEncoder) {
        XmlUserStoreLoginModule.configure(xmlUserStore, passwordEncoder);
        return new Configuration() {
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                if (!APPLICATION_NAME.equals(name)) {
                    return null;
                }
                return new AppConfigurationEntry[]{
                        new AppConfigurationEntry(
                                XmlUserStoreLoginModule.class.getName(),
                                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                                Map.of()
                        )
                };
            }
        };
    }
}
