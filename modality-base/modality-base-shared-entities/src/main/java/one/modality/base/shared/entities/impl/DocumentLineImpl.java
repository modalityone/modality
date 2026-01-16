package one.modality.base.shared.entities.impl;

import dev.webfx.platform.util.Arrays;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.DocumentLine;

/**
 * @author Bruno Salmon
 */
public final class DocumentLineImpl extends DynamicEntity implements DocumentLine {

    private String[] shareOwnerMates;

    public DocumentLineImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    @Override
    public String[] getShareOwnerMatesNames() {
        if (shareOwnerMates == null)
            shareOwnerMates = Arrays.nonNulls(String[]::new,
                getShareOwnerMate1Name(),
                getShareOwnerMate2Name(),
                getShareOwnerMate3Name(),
                getShareOwnerMate4Name(),
                getShareOwnerMate5Name(),
                getShareOwnerMate6Name(),
                getShareOwnerMate7Name()
            );
        return shareOwnerMates;
    }

    @Override
    public void setShareOwnerMatesNames(String[] matesNames) {
        this.shareOwnerMates = matesNames;
        setShareOwnerMate1Name(Arrays.getValue(matesNames, 0));
        setShareOwnerMate2Name(Arrays.getValue(matesNames, 1));
        setShareOwnerMate3Name(Arrays.getValue(matesNames, 2));
        setShareOwnerMate4Name(Arrays.getValue(matesNames, 3));
        setShareOwnerMate5Name(Arrays.getValue(matesNames, 4));
        setShareOwnerMate6Name(Arrays.getValue(matesNames, 5));
        setShareOwnerMate7Name(Arrays.getValue(matesNames, 6));
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<DocumentLine> {
        public ProvidedFactory() {
            super(DocumentLine.class, DocumentLineImpl::new);
        }
    }
}
