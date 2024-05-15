package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.SerialCodecManager;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.document.service.LoadPolicyArgument;

/**
 * @author Bruno Salmon
 */
public class LoadPolicyArgumentSerialCodec extends SerialCodecBase<LoadPolicyArgument> {

    private static final String CODEC_ID = "LoadPolicyArgument";
    private static final String ORGANIZATION_PK_KEY = "organizationPk";
    private static final String EVENT_PK_KEY = "eventPk";
    private static final String START_DATE_KEY = "startDate";
    private static final String END_DATE_KEY = "endDate";

    public LoadPolicyArgumentSerialCodec() {
        super(LoadPolicyArgument.class, CODEC_ID);
    }

    @Override
    public LoadPolicyArgument decodeFromJson(ReadOnlyAstObject json) {
        return new LoadPolicyArgument(
                json.get(ORGANIZATION_PK_KEY),
                json.get(EVENT_PK_KEY),
                SerialCodecManager.decodeLocalDate(json.get(START_DATE_KEY)),
                SerialCodecManager.decodeLocalDate(json.get(END_DATE_KEY))
        );
    }

    @Override
    public void encodeToJson(LoadPolicyArgument o, AstObject json) {
        json    .set(ORGANIZATION_PK_KEY, o.getOrganizationPk())
                .set(EVENT_PK_KEY, o.getEventPk())
                .set(START_DATE_KEY, SerialCodecManager.encodeLocalDate(o.getStartDate()))
                .set(END_DATE_KEY, SerialCodecManager.encodeLocalDate(o.getEndDate()));
    }
}
