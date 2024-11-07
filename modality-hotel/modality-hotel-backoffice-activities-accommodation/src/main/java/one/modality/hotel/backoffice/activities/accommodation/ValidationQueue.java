package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.kit.util.properties.FXProperties;
import javafx.beans.property.BooleanProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Allows a series of validations to be run where some validations involve asynchronous code executions.
 * Examples of asynchronous code executions include executing a database query and performing validation
 * logic in the database query response handler. In these cases the validation method cannot return a
 * value indicating success or failre of the validation. This class passes a BooleanProperty with a
 * listener that waits for the validation method to set it to indicate success or failure of the
 * validation.
 */
public class ValidationQueue {

    private final Runnable onSuccess;
    private final List<Consumer<BooleanProperty>> consumers = new ArrayList<>();

    /**
     * Constructor creates an initially empty queue of validation code blocks.
     * @param onSuccess the code to be executed if all validations pass
     */
    public ValidationQueue(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    /**
     * Adds a code block containing validation code.
     * @param consumer this code block is responsible for setting the value of the BooleanProperty
     *                 it is passed. Failure to set the value of the BooleanProperty will cause this
     *                 class's run() method to hang.
     * @return this for chaining
     */
    public ValidationQueue addValidation(Consumer<BooleanProperty> consumer) {
        consumers.add(consumer);
        return this;
    }

    /**
     * Executes the validation code blocks passed to the addValidation() method in the order they were
     * added. If a code block sets the BooleanProperty to false (to indicate failure) the remaining code
     * blocks are not executed. If all code blocks execute and indicate success then the code block
     * passed to the constructor is executed.
     */
    public void run() {
        next();
    }

    private void next() {
        if (!consumers.isEmpty()) {
            Consumer<BooleanProperty> consumer = consumers.remove(0);
            consumer.accept(FXProperties.newBooleanProperty(newValue -> {
                if (newValue) {
                    next();
                }
            }));
        } else {
            onSuccess.run();
        }
    }
}
