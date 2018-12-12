package com.cxplan.projection.core;

import com.cxplan.projection.IApplication;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author KennyLiu
 * @created on 2017/8/15
 */
public class ServiceFactory {

    static Map<String, Object> serviceMap;
    static boolean isInitialized = false;
    public synchronized static void initialize(IApplication application, String... packageNames) {
        if (isInitialized) {
            return;
        }

        serviceMap = new HashMap<String, Object>();
        for (String pkg : packageNames) {
            Reflections reflections = new Reflections(pkg);

            Set<Class<?>> subTypes = reflections.getTypesAnnotatedWith(CXService.class);
            for (Class<?> claz : subTypes) {
                String beanName = claz.getAnnotation(CXService.class).value();
                Object serviceObj;
                try {
                    serviceObj = claz.newInstance();
                    serviceMap.put(beanName, serviceObj);
                    System.out.println("--business service: " + beanName);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }

                try {
                    Method appMethod = claz.getMethod("setApplication", IApplication.class);
                    appMethod.invoke(serviceObj, application);
                } catch (NoSuchMethodException e) {

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        isInitialized = true;
    }
    public static <T> T getService(String name) {
        if (!isInitialized) {
            throw new RuntimeException("The service factory has not been initialized!");
        }

        return (T)serviceMap.get(name);
    }
}
