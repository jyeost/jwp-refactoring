package kitchenpos.application.menu;

import kitchenpos.domain.menu.Menu;
import kitchenpos.domain.menu.MenuProduct;
import kitchenpos.domain.menu.MenuProductRepository;
import kitchenpos.domain.menu.MenuProducts;
import kitchenpos.domain.menu.MenuRepository;
import kitchenpos.domain.menugroup.MenuGroup;
import kitchenpos.domain.menugroup.MenuGroupRepository;
import kitchenpos.domain.product.Product;
import kitchenpos.domain.product.ProductRepository;
import kitchenpos.exception.ExceptionInformation;
import kitchenpos.exception.KitchenposException;
import kitchenpos.presentation.menu.dto.MenuRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {
    private final MenuRepository menuRepository;

    private final MenuProductRepository menuProductRepository;
    private final MenuGroupRepository menuGroupRepository;
    private final ProductRepository productRepository;

    public MenuService(final MenuRepository menuRepository, final MenuProductRepository menuProductRepository, final MenuGroupRepository menuGroupRepository, final ProductRepository productRepository) {
        this.menuRepository = menuRepository;
        this.menuProductRepository = menuProductRepository;
        this.menuGroupRepository = menuGroupRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Menu create(final MenuRequest menuRequest) {
        final MenuGroup menuGroup = findMenuGroup(menuRequest);
        final MenuProducts menuProducts = MenuProducts.create(findMenuProducts(menuRequest));

        final Menu menu = Menu.create(
                menuRequest.getName(),
                menuRequest.getPrice(),
                menuGroup.getId(),
                menuProducts
        );
        final Menu savedMenu = menuRepository.save(menu);
        menuProducts.updateMenu(savedMenu);
        menuProductRepository.saveAll(menuProducts.getMenuProducts());
        return savedMenu;
    }

    private MenuGroup findMenuGroup(final MenuRequest menuRequest) {
        return menuGroupRepository.findById(menuRequest.getMenuGroupId())
                .orElseThrow(() -> new KitchenposException(ExceptionInformation.MENU_GROUP_NOT_FOUND));
    }

    private List<MenuProduct> findMenuProducts(final MenuRequest menuRequest) {
        return menuRequest.getMenuProducts().stream()
                .map(menuProductRequest -> {
                    Product product = productRepository.findById(menuProductRequest.getProductId())
                            .orElseThrow(() -> new KitchenposException(ExceptionInformation.PRODUCT_NOT_FOUND));
                    return MenuProduct.create(product, menuProductRequest.getQuantity());
                })
                .collect(Collectors.toList());
    }

    public List<Menu> list() {
        return menuRepository.findAll();
    }
}
