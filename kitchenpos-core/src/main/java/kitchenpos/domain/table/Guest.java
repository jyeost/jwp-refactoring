package kitchenpos.domain.table;


import kitchenpos.exception.ExceptionInformation;
import kitchenpos.exception.KitchenposException;

import javax.persistence.Embeddable;

@Embeddable
public class Guest {
    private static final int MIN_GUEST_BOUND = 0;

    private int numberOfGuests;

    protected Guest() {
    }

    private Guest(final int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }


    public static Guest create(final int numberOfGuests) {
        validateBound(numberOfGuests);
        return new Guest(numberOfGuests);
    }

    private static void validateBound(final long numberOfGuests) {
        if (numberOfGuests < MIN_GUEST_BOUND) {
            throw new KitchenposException(ExceptionInformation.ORDER_TABLE_GUEST_OUT_OF_BOUNCE);
        }
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(final int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }
}
