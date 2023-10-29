package kitchenpos.application.menugroup;

import kitchenpos.domain.menugroup.MenuGroup;
import kitchenpos.domain.menugroup.MenuGroupRepository;
import kitchenpos.presentation.menugroup.dto.MenuGroupRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MenuGroupService {
    private final MenuGroupRepository menuGroupRepository;

    public MenuGroupService(final MenuGroupRepository menuGroupRepository) {
        this.menuGroupRepository = menuGroupRepository;
    }

    @Transactional
    public MenuGroup create(final MenuGroupRequest menuGroupRequest) {
        final MenuGroup menuGroup = MenuGroup.create(menuGroupRequest.getName());
        return menuGroupRepository.save(menuGroup);
    }

    public List<MenuGroup> list() {
        return menuGroupRepository.findAll();
    }
}
