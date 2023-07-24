package one.modality.base.backoffice.operations.entities.generic;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.orm.domainmodel.formatter.ValueFormatter;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.expression.Expression;
import dev.webfx.stack.orm.reactive.entities.entities_to_grid.EntityColumn;
import dev.webfx.stack.orm.reactive.entities.entities_to_grid.EntityColumnFactory;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class CopyExecutor {

    static Future<Void> executeRequest(CopyRequest rq) {
        return execute(rq.getEntities(), rq.getColumns());
    }

    private static <E extends Entity> Future<Void> execute(
            Collection<E> entities, EntityColumn<E>... columns) {
        StringBuilder clipboardString = new StringBuilder();
        List<EntityColumn<E>> textColumns = new ArrayList<>();
        for (EntityColumn<E> column : columns) {
            if (column.isVisible()) {
                Expression<E> displayExpression = column.getDisplayExpression();
                Expression<E> textExpression =
                        ExportHelper.getTextExpression(displayExpression, false, false);
                if (textExpression != null) {
                    textColumns.add(
                            textExpression == displayExpression
                                    ? column
                                    : EntityColumnFactory.get().create(textExpression));
                    clipboardString.append(column.getName()).append('\t');
                }
            }
        }
        clipboardString.append('\n');
        for (Entity entity : entities) {
            for (EntityColumn<E> textColumn : textColumns) {
                Object value = entity.evaluate(textColumn.getDisplayExpression());
                ValueFormatter displayFormatter = textColumn.getDisplayFormatter();
                if (displayFormatter != null) value = displayFormatter.formatValue(value);
                clipboardString.append(ExportHelper.getTextExpressionValue(value)).append('\t');
            }
            clipboardString.append('\n');
        }
        ClipboardContent content = new ClipboardContent();
        content.putString(clipboardString.toString());
        Clipboard.getSystemClipboard().setContent(content);
        return Future.succeededFuture();
    }
}
