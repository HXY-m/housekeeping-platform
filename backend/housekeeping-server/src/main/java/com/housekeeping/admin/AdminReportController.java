package com.housekeeping.admin;

import com.housekeeping.admin.service.AdminReportExportService;
import com.housekeeping.audit.OperationLogActions;
import com.housekeeping.audit.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN') and hasAuthority('ADMIN_REPORT_EXPORT')")
@Tag(name = "管理员报表导出", description = "管理员导出仪表盘、订单、用户、售后和操作日志报表")
public class AdminReportController {

    private final AdminReportExportService adminReportExportService;
    private final OperationLogService operationLogService;

    public AdminReportController(AdminReportExportService adminReportExportService,
                                 OperationLogService operationLogService) {
        this.adminReportExportService = adminReportExportService;
        this.operationLogService = operationLogService;
    }

    @GetMapping("/dashboard/export")
    @Operation(summary = "导出运营看板报表")
    public ResponseEntity<byte[]> exportDashboard() {
        return buildResponse(adminReportExportService.exportDashboard(), "dashboard");
    }

    @GetMapping("/orders/export")
    @Operation(summary = "导出订单报表")
    public ResponseEntity<byte[]> exportOrders(@RequestParam(required = false) String status,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String dateFrom,
                                               @RequestParam(required = false) String dateTo) {
        return buildResponse(adminReportExportService.exportOrders(status, keyword, dateFrom, dateTo), "orders");
    }

    @GetMapping("/users/export")
    @Operation(summary = "导出用户报表")
    public ResponseEntity<byte[]> exportUsers(@RequestParam(required = false) String roleCode,
                                              @RequestParam(required = false) String realName,
                                              @RequestParam(required = false) String phone,
                                              @RequestParam(required = false) String status) {
        return buildResponse(adminReportExportService.exportUsers(roleCode, realName, phone, status), "users");
    }

    @GetMapping("/after-sales/export")
    @Operation(summary = "导出售后报表")
    public ResponseEntity<byte[]> exportAfterSales(@RequestParam(required = false) String status,
                                                   @RequestParam(required = false) String dateFrom,
                                                   @RequestParam(required = false) String dateTo) {
        return buildResponse(adminReportExportService.exportAfterSales(status, dateFrom, dateTo), "after_sales");
    }

    @GetMapping("/operation-logs/export")
    @Operation(summary = "导出操作日志报表")
    public ResponseEntity<byte[]> exportOperationLogs(@RequestParam(required = false) String operatorName,
                                                      @RequestParam(required = false) String roleCode,
                                                      @RequestParam(required = false) String actionType,
                                                      @RequestParam(required = false) String dateFrom,
                                                      @RequestParam(required = false) String dateTo) {
        return buildResponse(
                adminReportExportService.exportOperationLogs(operatorName, roleCode, actionType, dateFrom, dateTo),
                "operation_logs"
        );
    }

    private ResponseEntity<byte[]> buildResponse(AdminReportExportService.ExportFile exportFile, String reportType) {
        operationLogService.record(
                OperationLogActions.REPORT_EXPORT,
                "REPORT",
                null,
                "导出报表：" + reportType
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(exportFile.fileName(), StandardCharsets.UTF_8)
                .build());
        return ResponseEntity.ok()
                .headers(headers)
                .body(exportFile.content());
    }
}
