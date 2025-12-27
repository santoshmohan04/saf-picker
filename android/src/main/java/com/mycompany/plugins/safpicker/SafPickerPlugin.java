package com.mycompany.plugins.safpicker;

import com.getcapacitor.Logger;

public class SafPickerPlugin {

    public String echo(String value) {
        Logger.info("Echo", value);
        return value;
    }
}
