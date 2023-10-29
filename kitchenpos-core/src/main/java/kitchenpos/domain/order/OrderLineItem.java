package kitchenpos.domain.order;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;

@Entity
public class OrderLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;

    private String menuName;

    private BigDecimal menuPrice;

    @Embedded
    private Quantity quantity;

    protected OrderLineItem() {
    }

    private OrderLineItem(final Order order, final String menuName, final BigDecimal menuPrice, final Quantity quantity) {
        this(null, order, menuName, menuPrice, quantity);
    }

    private OrderLineItem(final Long seq, final Order order, final String menuName, final BigDecimal menuPrice, final Quantity quantity) {
        this.seq = seq;
        this.order = order;
        this.menuName = menuName;
        this.menuPrice = menuPrice;
        this.quantity = quantity;
    }

    public static OrderLineItem create(final String menuName, final BigDecimal menuPrice, final long quantity, final Order order) {
        return new OrderLineItem(order, menuName, menuPrice, Quantity.create(quantity));
    }

    public Long getSeq() {
        return seq;
    }

    public Order getOrder() {
        return order;
    }

    public String getMenuName() {
        return menuName;
    }

    public BigDecimal getMenuPrice() {
        return menuPrice;
    }

    public long getQuantity() {
        return quantity.getQuantity();
    }
}
