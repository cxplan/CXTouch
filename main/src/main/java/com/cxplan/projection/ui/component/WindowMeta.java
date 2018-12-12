package com.cxplan.projection.ui.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author KennyLiu
 * @created on 2017/8/15
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface WindowMeta {
    int SINGLE = 0;
    int MULTI = 1;
    /**
     * return whether this window class is allowed to create more then one instance.
     * @return true: allow to create more then one. false: not allow.
     */
    int instance() default SINGLE;
}
