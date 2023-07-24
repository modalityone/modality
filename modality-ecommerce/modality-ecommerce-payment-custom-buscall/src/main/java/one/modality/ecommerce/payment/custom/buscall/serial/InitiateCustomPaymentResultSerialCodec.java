package one.modality.ecommerce.payment.custom.buscall.serial;

import dev.webfx.platform.json.JsonObject;
import dev.webfx.platform.json.ReadOnlyJsonObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.custom.InitiateCustomPaymentResult;

public final class InitiateCustomPaymentResultSerialCodec
    extends SerialCodecBase<InitiateCustomPaymentResult> {

  private static final String CODEC_ID = "InitiateCustomPaymentResult";
  private static final String HTML_CONTENT_KEY = "htmlContent";

  public InitiateCustomPaymentResultSerialCodec() {
    super(InitiateCustomPaymentResult.class, CODEC_ID);
  }

  @Override
  public void encodeToJson(InitiateCustomPaymentResult arg, JsonObject json) {
    json.set(HTML_CONTENT_KEY, arg.getHtmlContent());
  }

  @Override
  public InitiateCustomPaymentResult decodeFromJson(ReadOnlyJsonObject json) {
    return new InitiateCustomPaymentResult(json.getString(HTML_CONTENT_KEY));
  }
}
