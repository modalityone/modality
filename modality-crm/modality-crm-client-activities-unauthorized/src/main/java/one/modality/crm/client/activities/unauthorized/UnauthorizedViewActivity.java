package one.modality.crm.client.activities.unauthorized;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;
import javafx.scene.text.Text;

/**
 * @author Bruno Salmon
 */
final class UnauthorizedViewActivity extends ViewDomainActivityBase {

  @Override
  public Node buildUi() {
    return new Text("Sorry, you are not authorized to access this page");
  }
}
