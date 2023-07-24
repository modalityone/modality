package one.modality.ecommerce.payment.direct.buscall.serial;

import dev.webfx.platform.json.JsonObject;
import dev.webfx.platform.json.ReadOnlyJsonObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.direct.MakeDirectPaymentArgument;

/**************************************
 *           Serial Codec             *
 * ***********************************/

public final class MakeDirectPaymentArgumentSerialCodec
    extends SerialCodecBase<MakeDirectPaymentArgument> {

  private static final String CODEC_ID = "DirectPaymentArgument";
  private static final String AMOUNT_KEY = "amount";
  private static final String CURRENCY_KEY = "currency";
  private static final String CC_NUMBER_KEY = "ccNumber";
  private static final String CC_EXPIRY_KEY = "ccExpiry";

  public MakeDirectPaymentArgumentSerialCodec() {
    super(MakeDirectPaymentArgument.class, CODEC_ID);
  }

  @Override
  public void encodeToJson(MakeDirectPaymentArgument arg, JsonObject json) {
    json.set(AMOUNT_KEY, arg.getAmount());
    json.set(CURRENCY_KEY, arg.getCurrency());
    json.set(CC_NUMBER_KEY, arg.getCcNumber());
    json.set(CC_EXPIRY_KEY, arg.getCcExpiry());
  }

  @Override
  public MakeDirectPaymentArgument decodeFromJson(ReadOnlyJsonObject json) {
    return new MakeDirectPaymentArgument(
        json.get(AMOUNT_KEY),
        json.getString(CURRENCY_KEY),
        json.getString(CC_NUMBER_KEY),
        json.getString(CC_EXPIRY_KEY));
  }
}
