package com.cxplan.projection.core.command;

import com.cxplan.projection.IApplication;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created on 2017/5/9.
 *
 * @author kenny
 */
public class CommandHandlerFactory {

    private static final Logger logger = LoggerFactory.getLogger(CommandHandlerFactory.class);

    private static Map<String, ICommandHandler> handlerMap;


    static {
        handlerMap = new HashMap<String, ICommandHandler>();
    }

    public static ICommandHandler getHandler(String command) {
        return handlerMap.get(command);
    }

    public static void addHandler(String command, ICommandHandler handler) {
        if (handlerMap.containsKey(command)) {
            logger.error("The command handler:{} has been registered! " , command);
            return;
        }
        handlerMap.put(command, handler);
    }

    public static void loadHandler(IApplication application, String... packageName) {
        Reflections reflections = new Reflections(packageName);

        Set<Class<? extends ICommandHandler>> subTypes = reflections.getSubTypesOf(ICommandHandler.class);
        for (Class<? extends ICommandHandler> clazz : subTypes) {
            if (Modifier.isAbstract(clazz.getModifiers())) {
                System.out.println("abstact class: " + clazz.getName());
                continue;
            }
            try {
                ICommandHandler handler = clazz.newInstance();
                ((AbstractCommandHandler)handler).setApplication(application);
                handlerMap.put(handler.getCommand(), handler);
            } catch (InstantiationException e) {
                logger.error("Loading handler failed: " + e.getMessage(), e);
            } catch (IllegalAccessException e) {
                logger.error("Loading handler failed: " + e.getMessage(), e);
            }
        }
    }
}
