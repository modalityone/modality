// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.sessionrecorder {

  // Direct dependencies modules
  requires java.base;
  requires javafx.base;
  requires modality.crm.shared.authn;
  requires webfx.platform.boot;
  requires webfx.platform.console;
  requires webfx.platform.storage;
  requires webfx.platform.useragent;
  requires webfx.stack.com.bus;
  requires webfx.stack.orm.datasourcemodel.service;
  requires webfx.stack.orm.entity;
  requires webfx.stack.session.state.client.fx;

  // Exported packages
  exports one.modality.base.client.jobs.sessionrecorder;

  // Provided services
  provides dev.webfx.platform.boot.spi.ApplicationJob with
      one.modality.base.client.jobs.sessionrecorder.ClientSessionRecorderJob;
}
