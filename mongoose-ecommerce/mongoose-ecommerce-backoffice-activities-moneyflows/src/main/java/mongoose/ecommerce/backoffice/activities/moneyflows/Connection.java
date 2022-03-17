package mongoose.ecommerce.backoffice.activities.moneyflows;

import mongoose.base.shared.entities.MoneyAccount;

public class Connection {

    private MoneyAccount source;
    private MoneyAccount dest;

    public Connection(MoneyAccount source, MoneyAccount dest) {
        this.source = source;
        this.dest = dest;
    }

    public MoneyAccount getSource() {
        return source;
    }

    public MoneyAccount getDest() {
        return dest;
    }
}
