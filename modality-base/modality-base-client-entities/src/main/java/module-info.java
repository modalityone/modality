// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.entities {

  // Direct dependencies modules
  requires java.base;
  requires javafx.base;
  requires javafx.controls;
  requires javafx.graphics;
  requires modality.base.client.presentationmodel;
  requires modality.base.shared.entities;
  requires webfx.extras.util.layout;
  requires webfx.extras.util.scene;
  requires webfx.kit.util;
  requires webfx.platform.util;
  requires webfx.stack.i18n;
  requires webfx.stack.orm.domainmodel;
  requires webfx.stack.orm.dql;
  requires webfx.stack.orm.entity.controls;
  requires webfx.stack.orm.reactive.dql;
  requires webfx.stack.ui.controls;

  // Exported packages
  exports one.modality.base.client.entities.util;
  exports one.modality.base.client.entities.util.filters;
}
