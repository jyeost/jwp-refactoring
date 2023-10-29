package main.java.kitchenpos.application.tablegroup;

import kitchenpos.global.exception.KitchenposException;
import kitchenpos.table.OrderTableRepository;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.tablegroup.TableGroupRepository;
import kitchenpos.tablegroup.domain.TableGroup;
import main.java.kitchenpos.presentation.tablegroup.dto.CreateTableGroupRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static kitchenpos.global.exception.ExceptionInformation.*;

@Service
public class TableGroupService {
    private final OrderTableRepository orderTableRepository;
    private final TableGroupRepository tableGroupRepository;
    private final OrderTablesStatusValidator orderTableStatusValidator;

    public TableGroupService(final OrderTableRepository orderTableRepository, final TableGroupRepository tableGroupRepository, final OrderTablesStatusValidator orderTableStatusValidator) {
        this.orderTableRepository = orderTableRepository;
        this.tableGroupRepository = tableGroupRepository;
        this.orderTableStatusValidator = orderTableStatusValidator;
    }

    @Transactional
    public TableGroup create(final CreateTableGroupRequest createTableGroupRequest) {
        final List<OrderTable> savedOrderTables = validateOrderTableIds(createTableGroupRequest.getOrderTableIds());
        final TableGroup tableGroup = TableGroup.create(savedOrderTables);
        final TableGroup savedTableGroup = tableGroupRepository.save(tableGroup);
        tableGroup.updateOrderTablesGrouped();

        return savedTableGroup;
    }

    private List<OrderTable> validateOrderTableIds(final List<Long> requestOrderTableIds) {
        if (CollectionUtils.isEmpty(requestOrderTableIds)) {
            throw new KitchenposException(ExceptionInformation.TABLE_GROUP_UNDER_BOUNCE);
        }
        final List<OrderTable> savedOrderTables = orderTableRepository.findByIdIn(requestOrderTableIds);

        if (requestOrderTableIds.size() != savedOrderTables.size()) {
            throw new KitchenposException(ExceptionInformation.ORDER_TABLE_IN_TABLE_GROUP_NOT_FOUND_OR_DUPLICATED);
        }
        return savedOrderTables;
    }

    @Transactional
    public void ungroup(final Long tableGroupId) {
        final TableGroup tableGroup = tableGroupRepository.findById(tableGroupId)
                .orElseThrow(() -> new KitchenposException(ExceptionInformation.TABLE_GROUP_NOT_FOUND));

        orderTableStatusValidator.validateIsComplete(tableGroup.getOrderTableIds());

        tableGroup.updateOrderTablesUngrouped();
    }
}
