package mongoose.ecommerce.backoffice.activities.moneyflows;

import java.util.Objects;

public class MoneyTransferEntity {

	private String text;

	public MoneyTransferEntity(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MoneyTransferEntity that = (MoneyTransferEntity) o;
		return Objects.equals(text, that.text);
	}

	@Override
	public int hashCode() {
		return Objects.hash(text);
	}
}
