package com.housekeeping.admin.service;

import com.housekeeping.admin.dto.AdminDashboardDto;
import com.housekeeping.admin.dto.AdminUserDto;
import com.housekeeping.aftersale.dto.AfterSaleDto;
import com.housekeeping.aftersale.service.AfterSaleService;
import com.housekeeping.audit.dto.OperationLogDto;
import com.housekeeping.audit.service.OperationLogService;
import com.housekeeping.common.DemoDataService;
import com.housekeeping.order.dto.OrderDto;
import com.housekeeping.order.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AdminReportExportService {

    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final DemoDataService demoDataService;
    private final OrderService orderService;
    private final AdminUserService adminUserService;
    private final AfterSaleService afterSaleService;
    private final OperationLogService operationLogService;

    public AdminReportExportService(DemoDataService demoDataService,
                                    OrderService orderService,
                                    AdminUserService adminUserService,
                                    AfterSaleService afterSaleService,
                                    OperationLogService operationLogService) {
        this.demoDataService = demoDataService;
        this.orderService = orderService;
        this.adminUserService = adminUserService;
        this.afterSaleService = afterSaleService;
        this.operationLogService = operationLogService;
    }

    public ExportFile exportDashboard() {
        AdminDashboardDto dashboard = demoDataService.getAdminDashboard();
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("指标", "值"));
        rows.add(List.of("总订单量", String.valueOf(dashboard.totalOrders())));
        rows.add(List.of("已完成订单", String.valueOf(dashboard.completedOrders())));
        rows.add(List.of("活跃服务人员", String.valueOf(dashboard.activeWorkers())));
        rows.add(List.of("平均评分", String.valueOf(dashboard.averageRating())));
        rows.add(List.of("", ""));
        rows.add(List.of("服务项目", "销量"));
        for (Map.Entry<String, Integer> entry : dashboard.serviceSales().entrySet()) {
            rows.add(List.of(entry.getKey(), String.valueOf(entry.getValue())));
        }
        return buildFile("dashboard-report", rows);
    }

    public ExportFile exportOrders(String status, String keyword, String dateFrom, String dateTo) {
        List<OrderDto> orders = orderService.listAllOrders().stream()
                .filter(item -> !StringUtils.hasText(status) || item.status().equalsIgnoreCase(status.trim()))
                .filter(item -> !StringUtils.hasText(keyword) || containsKeyword(item, keyword))
                .filter(item -> withinDateRange(item.bookingDate(), dateFrom, dateTo))
                .toList();

        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of(
                "订单号", "服务项目", "服务人员", "联系人", "联系电话", "预约日期", "预约时段",
                "服务地址", "订单状态", "最新进度", "订单备注", "是否已评价", "评分", "评价内容"
        ));
        orders.forEach(item -> rows.add(List.of(
                String.valueOf(item.id()),
                safe(item.serviceName()),
                safe(item.workerName()),
                safe(item.customerName()),
                safe(item.contactPhone()),
                safe(item.bookingDate()),
                safe(item.bookingSlot()),
                safe(item.serviceAddress()),
                safe(item.status()),
                safe(item.progressNote()),
                safe(item.remark()),
                item.reviewed() ? "是" : "否",
                item.reviewRating() == null ? "" : String.valueOf(item.reviewRating()),
                safe(item.reviewContent())
        )));
        return buildFile("orders-report", rows);
    }

    public ExportFile exportUsers(String roleCode, String realName, String phone, String status) {
        List<AdminUserDto> users = adminUserService.listUsers(roleCode, realName, phone, status);
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("用户ID", "姓名", "手机号", "状态", "角色", "所在城市", "已绑定服务人员档案"));
        users.forEach(item -> rows.add(List.of(
                String.valueOf(item.id()),
                safe(item.realName()),
                safe(item.phone()),
                safe(item.status()),
                String.join(" / ", item.roleCodes()),
                safe(item.city()),
                item.workerProfileBound() ? "是" : "否"
        )));
        return buildFile("users-report", rows);
    }

    public ExportFile exportAfterSales(String status, String dateFrom, String dateTo) {
        List<AfterSaleDto> afterSales = afterSaleService.listAll().stream()
                .filter(item -> !StringUtils.hasText(status) || item.status().equalsIgnoreCase(status.trim()))
                .filter(item -> withinDateRange(item.createdAt(), dateFrom, dateTo))
                .toList();

        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of(
                "售后单号", "订单号", "服务项目", "服务人员", "客户姓名", "订单状态",
                "问题类型", "问题描述", "联系电话", "售后状态", "管理员备注", "凭证数量", "提交时间", "处理时间"
        ));
        afterSales.forEach(item -> rows.add(List.of(
                String.valueOf(item.id()),
                String.valueOf(item.orderId()),
                safe(item.serviceName()),
                safe(item.workerName()),
                safe(item.customerName()),
                safe(item.orderStatus()),
                safe(item.issueType()),
                safe(item.content()),
                safe(item.contactPhone()),
                safe(item.status()),
                safe(item.adminRemark()),
                String.valueOf(item.attachments() == null ? 0 : item.attachments().size()),
                item.createdAt() == null ? "" : item.createdAt().toString(),
                item.handledAt() == null ? "" : item.handledAt().toString()
        )));
        return buildFile("after-sales-report", rows);
    }

    public ExportFile exportOperationLogs(String operatorName,
                                          String roleCode,
                                          String actionType,
                                          String dateFrom,
                                          String dateTo) {
        List<OperationLogDto> logs = operationLogService.list(operatorName, roleCode, actionType, dateFrom, dateTo);
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of(
                "日志ID", "操作人ID", "操作人", "角色", "动作类型", "目标类型",
                "目标ID", "内容", "IP 地址", "创建时间"
        ));
        logs.forEach(item -> rows.add(List.of(
                String.valueOf(item.id()),
                item.operatorUserId() == null ? "" : String.valueOf(item.operatorUserId()),
                safe(item.operatorName()),
                safe(item.roleCode()),
                safe(item.actionType()),
                safe(item.targetType()),
                item.targetId() == null ? "" : String.valueOf(item.targetId()),
                safe(item.content()),
                safe(item.ipAddress()),
                item.createdAt() == null ? "" : item.createdAt().toString()
        )));
        return buildFile("operation-logs-report", rows);
    }

    private boolean containsKeyword(OrderDto item, String keyword) {
        String normalizedKeyword = keyword.trim().toLowerCase();
        return safe(item.serviceName()).toLowerCase().contains(normalizedKeyword)
                || safe(item.workerName()).toLowerCase().contains(normalizedKeyword)
                || safe(item.customerName()).toLowerCase().contains(normalizedKeyword);
    }

    private boolean withinDateRange(String isoDate, String dateFrom, String dateTo) {
        if (!StringUtils.hasText(isoDate)) {
            return true;
        }
        LocalDate date = LocalDate.parse(isoDate.trim());
        LocalDate start = StringUtils.hasText(dateFrom) ? LocalDate.parse(dateFrom.trim()) : null;
        LocalDate end = StringUtils.hasText(dateTo) ? LocalDate.parse(dateTo.trim()) : null;
        return (start == null || !date.isBefore(start)) && (end == null || !date.isAfter(end));
    }

    private boolean withinDateRange(LocalDateTime createdAt, String dateFrom, String dateTo) {
        if (createdAt == null) {
            return true;
        }
        LocalDate date = createdAt.toLocalDate();
        LocalDate start = StringUtils.hasText(dateFrom) ? LocalDate.parse(dateFrom.trim()) : null;
        LocalDate end = StringUtils.hasText(dateTo) ? LocalDate.parse(dateTo.trim()) : null;
        return (start == null || !date.isBefore(start)) && (end == null || !date.isAfter(end));
    }

    private ExportFile buildFile(String prefix, List<List<String>> rows) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        for (List<String> row : rows) {
            csv.append(row.stream().map(this::escapeCsv).reduce((left, right) -> left + "," + right).orElse(""));
            csv.append('\n');
        }
        String fileName = prefix + "-" + LocalDateTime.now().format(FILE_TIME_FORMATTER) + ".csv";
        return new ExportFile(fileName, csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String escapeCsv(String value) {
        String safeValue = safe(value);
        String escaped = safeValue.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public record ExportFile(String fileName, byte[] content) {
    }
}
