package mongoose.ecommerce.backoffice.activities.moneyflows;

public class Connection {

    private MoneyTransferEntity source;
    private MoneyTransferEntity dest;

    public Connection(MoneyTransferEntity source, MoneyTransferEntity dest) {
        this.source = source;
        this.dest = dest;
    }

    public MoneyTransferEntity getSource() {
        return source;
    }

    public MoneyTransferEntity getDest() {
        return dest;
    }
}
