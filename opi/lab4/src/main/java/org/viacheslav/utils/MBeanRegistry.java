package org.viacheslav.utils;

import jakarta.servlet.ServletContextListener;
import lombok.experimental.UtilityClass;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class MBeanRegistry implements ServletContextListener {
    private static final Map<Object, ObjectName> beans = new HashMap<>();

    public static void registerBean(Object bean, String name) {
        try {
            String domain = bean.getClass().getPackageName();
            String type = bean.getClass().getSimpleName();

            ObjectName objectName = new ObjectName(String.format("%s:type=%s,name=%s", domain, type, name));

            ManagementFactory.getPlatformMBeanServer().registerMBean(bean, objectName);
            beans.put(bean, objectName);

            System.out.printf("Зарегистрирован MBean: %s%n", objectName);
        } catch (InstanceAlreadyExistsException e) {
            System.err.printf("MBean уже зарегистрирован: %s%n", e.getMessage());
        } catch (MalformedObjectNameException | MBeanRegistrationException | NotCompliantMBeanException e) {
            System.err.printf("Не удалось зарегистрировать MBean: %s%n", e.getMessage());
            e.printStackTrace();
        }
    }

    public static void unregisterBean(Object bean) {
        ObjectName objectName = beans.get(bean);
        if (objectName == null) {
            System.err.println("Указанный бин не зарегистрирован или уже удален.");
            return;
        }

        try {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(objectName);
            beans.remove(bean);

            System.out.printf("Удален MBean: %s%n", objectName);
        } catch (InstanceNotFoundException | MBeanRegistrationException e) {
            System.err.printf("Не удалось удалить MBean: %s%n", e.getMessage());
            e.printStackTrace();
        }
    }
}
