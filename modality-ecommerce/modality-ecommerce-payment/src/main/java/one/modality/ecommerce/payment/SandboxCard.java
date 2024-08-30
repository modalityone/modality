package one.modality.ecommerce.payment;

/**
 * @author Bruno Salmon
 */
public final class SandboxCard {

    private final String name;
    private final String numbers;
    private final String expirationDate;
    private final String cvv;
    private final String zip;

    public SandboxCard(String name, String numbers, String expirationDate, String cvv, String zip) {
        this.name = name;
        this.numbers = numbers;
        this.expirationDate = expirationDate;
        this.cvv = cvv;
        this.zip = zip;
    }

    public String getName() {
        return name;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public String getNumbers() {
        return numbers;
    }

    public String getCvv() {
        return cvv;
    }

    public String getZip() {
        return zip;
    }

}
