package jdy.zsf.nurserystock.demo;

import kd.bos.form.control.Control;
import kd.bos.form.plugin.AbstractFormPlugin;

import java.util.EventObject;

public class DemoFormPlogin extends AbstractFormPlugin {
  @Override
  public void registerListener(EventObject e) {
    super.registerListener(e);
    addClickListeners("tbTest","tbmain","buttonap");
  }

  @Override
  public void click(EventObject evt) {
    super.click(evt);
    Control control = (Control) evt.getSource();
    String key = control.getKey();
    this.getView().showMessage("点击事件");
  }
}