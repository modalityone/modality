package one.modality.event.client.mediaview;

public final class VideoView extends MediaInfoView {

    @Override
    protected boolean isMarkedAsFavorite() {
        return false;
    }

    @Override
    protected void toggleAsFavorite() {
    }

    /*{
        GeneralUtility.onNodeClickedWithoutScroll(e -> {
            if (e.isShiftDown()) {
                EntityPropertiesSheet.editEntity((Entity) mediaInfo,
                        "[" +
                        "'title'," +
                        "'excerpt'," +
                        "'teacher'," +
                        "'public'" +
                        "]",
                        mediaPane);
            }
        }, mediaPane);
    }*/

}