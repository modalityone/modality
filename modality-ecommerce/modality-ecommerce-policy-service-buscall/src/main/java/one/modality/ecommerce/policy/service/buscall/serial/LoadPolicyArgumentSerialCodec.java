package one.modality.ecommerce.policy.service.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.policy.service.LoadPolicyArgument;

/**
 * @author Bruno Salmon
 */
public final class LoadPolicyArgumentSerialCodec extends SerialCodecBase<LoadPolicyArgument> {

    private static final String CODEC_ID = "LoadPolicyArgument";
    private static final String ORGANIZATION_PK_KEY = "organizationPk";
    private static final String EVENT_PK_KEY = "eventPk";
    private static final String START_DATE_KEY = "startDate";
    private static final String END_DATE_KEY = "endDate";

    public LoadPolicyArgumentSerialCodec() {
        super(LoadPolicyArgument.class, CODEC_ID);
    }

    @Override
    public void encode(LoadPolicyArgument o, AstObject serial) {
        encodeObject(serial, ORGANIZATION_PK_KEY, o.getOrganizationPk());
        encodeObject(serial, EVENT_PK_KEY, o.getEventPk());
        encodeLocalDate(serial, START_DATE_KEY, o.getStartDate());
        encodeLocalDate(serial, END_DATE_KEY, o.getEndDate());
    }

    @Override
    public LoadPolicyArgument decode(ReadOnlyAstObject serial) {
        return new LoadPolicyArgument(
                decodeObject(serial, ORGANIZATION_PK_KEY),
                decodeObject(serial, EVENT_PK_KEY),
                decodeLocalDate(serial, START_DATE_KEY),
                decodeLocalDate(serial, END_DATE_KEY)
        );
    }

}
