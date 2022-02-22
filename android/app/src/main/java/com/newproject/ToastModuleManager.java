package com.newproject;


import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

public class ToastModuleManager extends SimpleViewManager<ToastModule> {
    @Override
    public String getName() {
        return "ToastModule";
    }
    @Override
    protected ToastModule createViewInstance(ThemedReactContext reactContext) {
        return new ToastModule(reactContext);
    }
    @ReactProp(name="isTurnedOn")
    public void setToastModuleStatus(ToastModule ToastModuleView, Boolean isTurnedOn) {
        ToastModuleView.setIsTurnedOn(isTurnedOn);
    }
}