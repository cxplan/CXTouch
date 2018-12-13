package com.cxplan.projection.core.setting;

/**
 * @author kenny
 *
 */
public interface ConfigChangedListener {

     /**
      * This method will be invoked when a property item is changed on device.
      *
      * @param event The property changed event.
      */
     void changed(SettingEvent event);
}
