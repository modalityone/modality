package one.modality.base.shared.entities;

/**
 * @author Bruno Salmon
 */
public class SimpleBoundaryPeriod implements BoundaryPeriod {

    private final ScheduledBoundary startBoundary;
    private final ScheduledBoundary endBoundary;

    public SimpleBoundaryPeriod(ScheduledBoundary startBoundary, ScheduledBoundary endBoundary) {
        this.startBoundary = startBoundary;
        this.endBoundary = endBoundary;
    }

    public ScheduledBoundary getStartBoundary() {
        return startBoundary;
    }

    public ScheduledBoundary getEndBoundary() {
        return endBoundary;
    }
}
