package kitchenpos.application;

import kitchenpos.menu.application.MenuService;
import kitchenpos.menu.domain.Menu;
import main.java.kitchenpos.presentation.menu.dto.MenuRequest;
import kitchenpos.menugroup.domain.MenuGroup;
import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderStatus;
import kitchenpos.order.application.OrderService;
import main.java.kitchenpos.presentation.order.dto.OrderLineItemRequest;
import main.java.kitchenpos.presentation.order.dto.OrderRequest;
import main.java.kitchenpos.presentation.order.dto.UpdateOrderStateRequest;
import kitchenpos.product.domain.Product;
import kitchenpos.product.application.ProductService;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.global.exception.KitchenposException;
import kitchenpos.menugroup.application.MenuGroupService;
import kitchenpos.support.ServiceTest;
import kitchenpos.table.application.TableService;
import main.java.kitchenpos.presentation.table.dto.ChangeOrderTableEmptyRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static kitchenpos.global.exception.ExceptionInformation.*;
import static kitchenpos.support.TestFixture.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@SuppressWarnings("NonAsciiCharacters")
@DisplayName("주문 서비스 테스트")
@ServiceTest
class OrderServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private MenuGroupService menuGroupService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private TableService tableService;

    @Autowired
    private OrderService orderService;


    @Test
    void 전체_주문내역을_조회한다() {
        // given
        final Product 상품1 = productService.create(상품);
        final Product 상품2 = productService.create(상품);
        final MenuGroup 메뉴그룹 = menuGroupService.create(메뉴_분류);
        final MenuRequest 신메뉴 = 메뉴(List.of(상품1, 상품2), 메뉴그룹);
        final Menu 저장한_신메뉴 = menuService.create(신메뉴);

        final OrderTable 테이블 = tableService.create(주문_테이블());
        final OrderLineItemRequest 주문1 = new OrderLineItemRequest(저장한_신메뉴.getId(), 1);

        final OrderRequest 주문_요청 = new OrderRequest(테이블.getId(), List.of(주문1));
        final Order 저장한_주문 = orderService.create(주문_요청);

        // when
        final List<Order> 조회한_전체_주문 = orderService.list();

        // then
        assertSoftly(soft -> {
            soft.assertThat(조회한_전체_주문).hasSize(1);
            soft.assertThat(조회한_전체_주문.get(0).getId()).isEqualTo(저장한_주문.getId());
        });
    }

    @DisplayName("주문 저장 테스트")
    @Nested
    class createOrder {

        @Test
        void 정상적인_주문을_저장한다() {
            // given
            final Product 상품1 = productService.create(상품);
            final Product 상품2 = productService.create(상품);
            final MenuGroup 메뉴그룹 = menuGroupService.create(메뉴_분류);
            final MenuRequest 신메뉴 = 메뉴(List.of(상품1, 상품2), 메뉴그룹);
            final Menu 저장한_신메뉴 = menuService.create(신메뉴);

            final OrderTable 테이블 = tableService.create(주문_테이블());
            final OrderLineItemRequest 주문1 = new OrderLineItemRequest(저장한_신메뉴.getId(), 1);

            final OrderRequest 주문_요청 = new OrderRequest(테이블.getId(), List.of(주문1));

            // when
            final Order 저장한_주문 = orderService.create(주문_요청);

            // then
            assertSoftly(soft -> {
                soft.assertThat(저장한_주문.getId()).isNotNull();
                soft.assertThat(저장한_주문.getOrderedTime()).isBefore(LocalDateTime.now());
                soft.assertThat(저장한_주문.getOrderStatus()).isEqualTo(OrderStatus.COOKING.name());
                soft.assertThat(저장한_주문.getOrderTableId()).isEqualTo(테이블.getId());
                soft.assertThat(저장한_주문.getOrderLineItems()).hasSize(1);
                soft.assertThat(저장한_주문.getOrderLineItems().get(0).getOrder().getId()).isEqualTo(저장한_주문.getId());
                soft.assertThat(저장한_주문.getOrderLineItems().get(0).getSeq()).isNotNull();
            });
        }

        @Test
        void 주문_항목이_비었다면_예외가_발생한다() {
            final OrderTable 테이블 = tableService.create(주문_테이블());
            final OrderRequest 주문_요청 = new OrderRequest(테이블.getId(), Collections.emptyList());

            assertThatThrownBy(() -> orderService.create(주문_요청))
                    .isExactlyInstanceOf(KitchenposException.class)
                    .hasMessage(ORDER_LINE_ITEMS_IS_EMPTY.getMessage());
        }

        @Test
        void 없는_메뉴를_주문하려하면_예외가_발생한다() {
            final OrderTable 테이블 = tableService.create(주문_테이블());

            // 존재하지 않는 메뉴를 넣는다.
            final OrderLineItemRequest 없는_메뉴_요청 = new OrderLineItemRequest(-1L, 1);
            final OrderRequest 주문_요청 = new OrderRequest(테이블.getId(), List.of(없는_메뉴_요청));

            assertThatThrownBy(() -> orderService.create(주문_요청))
                    .isExactlyInstanceOf(KitchenposException.class)
                    .hasMessage(ORDER_ITEM_NOT_FOUND_OR_DUPLICATE.getMessage());
        }

        @Test
        void 같은_메뉴를_두개의_주문항목으로_저장하려하면_예외가_발생한다() {
            final Product 상품1 = productService.create(상품);
            final MenuGroup 메뉴그룹 = menuGroupService.create(메뉴_분류);
            final MenuRequest 신메뉴 = 메뉴(List.of(상품1), 메뉴그룹);
            final Menu 저장한_신메뉴 = menuService.create(신메뉴);

            final OrderTable 테이블 = tableService.create(주문_테이블());

            // 같은 메뉴를 중복해서 주문에 저장한다
            final OrderLineItemRequest 주문1 = new OrderLineItemRequest(저장한_신메뉴.getId(), 1);
            final OrderLineItemRequest 주문2 = new OrderLineItemRequest(저장한_신메뉴.getId(), 1);

            final OrderRequest 주문_요청 = new OrderRequest(테이블.getId(), List.of(주문1, 주문2));

            assertThatThrownBy(() -> orderService.create(주문_요청))
                    .isExactlyInstanceOf(KitchenposException.class)
                    .hasMessage(ORDER_ITEM_NOT_FOUND_OR_DUPLICATE.getMessage());
        }

        @Test
        void 주문_테이블이_존재하지_않으면_예외가_발생한다() {
            final Product 상품1 = productService.create(상품);
            final MenuGroup 메뉴그룹 = menuGroupService.create(메뉴_분류);
            final MenuRequest 신메뉴 = 메뉴(List.of(상품1), 메뉴그룹);
            final Menu 저장한_신메뉴 = menuService.create(신메뉴);

            final OrderLineItemRequest 주문1 = new OrderLineItemRequest(저장한_신메뉴.getId(), 1);

            // 존재하지 않는 주문 테이블
            final OrderRequest 주문_요청 = new OrderRequest(-1L, List.of(주문1));

            assertThatThrownBy(() -> orderService.create(주문_요청))
                    .isExactlyInstanceOf(KitchenposException.class)
                    .hasMessage(ORDER_TABLE_NOT_FOUND.getMessage());
        }

        @Test
        void 주문_테이블의_상태가_empty라면_예외가_발생한다() {
            final Product 상품1 = productService.create(상품);
            final MenuGroup 메뉴그룹 = menuGroupService.create(메뉴_분류);
            final MenuRequest 신메뉴 = 메뉴(List.of(상품1), 메뉴그룹);
            final Menu 저장한_신메뉴 = menuService.create(신메뉴);

            // 테이블의 상태를 emtpy로 설정한다
            final OrderTable 테이블 = tableService.create(주문_테이블());
            tableService.changeEmpty(테이블.getId(), new ChangeOrderTableEmptyRequest(true));
            final OrderLineItemRequest 주문1 = new OrderLineItemRequest(저장한_신메뉴.getId(), 1);
            final OrderRequest 주문_요청 = new OrderRequest(테이블.getId(), List.of(주문1));

            assertThatThrownBy(() -> orderService.create(주문_요청))
                    .isExactlyInstanceOf(KitchenposException.class)
                    .hasMessage(ORDER_IN_EMPTY_TABLE.getMessage());
        }
    }

    @DisplayName("주문 상태 변경 테스트")
    @Nested
    class changeOrderStatus {

        private Order 주문;

        @BeforeEach
        void 주문을_1건_넣는다() {
            final Product 상품1 = productService.create(상품);
            final Product 상품2 = productService.create(상품);
            final MenuGroup 메뉴그룹 = menuGroupService.create(메뉴_분류);
            final MenuRequest 신메뉴 = 메뉴(List.of(상품1, 상품2), 메뉴그룹);
            final Menu 저장한_신메뉴 = menuService.create(신메뉴);

            final OrderTable 테이블 = tableService.create(주문_테이블());
            final OrderRequest 새로운_주문 = new OrderRequest(테이블.getId(), List.of(new OrderLineItemRequest(저장한_신메뉴.getId(), 1)));

            주문 = orderService.create(새로운_주문);
        }

        @Test
        void 저장되어있는_주문_상태가_COMPLETE면_상태를_변경할_수_없다() {
            final UpdateOrderStateRequest 주문_완료_요청 = new UpdateOrderStateRequest(OrderStatus.COMPLETION.name());
            final Order 완료된주문 = orderService.changeOrderStatus(주문.getId(), 주문_완료_요청);

            assertThatThrownBy(() -> orderService.changeOrderStatus(완료된주문.getId(), 주문_완료_요청))
                    .isExactlyInstanceOf(KitchenposException.class)
                    .hasMessage(UPDATE_COMPLETED_ORDER.getMessage());
        }
    }
}
