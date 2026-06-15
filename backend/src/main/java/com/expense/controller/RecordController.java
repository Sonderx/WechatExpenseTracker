package com.expense.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.expense.common.Result;
import com.expense.dto.RecordCreateRequest;
import com.expense.dto.RecordUpdateRequest;
import com.expense.entity.Record;
import com.expense.interceptor.AuthInterceptor;
import com.expense.service.RecordService;
import com.expense.vo.RecordVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 账目记录控制器
 * 
 * 功能说明：
 * - 提供账目记录的 CRUD 接口
 * - 支持按月分页查询
 * - 所有接口需要登录（通过 AuthInterceptor 校验 Token）
 */
@RestController
@RequestMapping("/api/records")
public class RecordController {

    @Autowired
    private RecordService recordService;

    /**
     * 创建账目记录
     * 
     * 请求示例：
     * POST /api/records
     * {
     *   "amount": 35.5,
     *   "type": 0,
     *   "categoryId": 1,
     *   "recordTime": "2026-06-14T12:00:00",
     *   "remark": "午餐"
     * }
     */
    @PostMapping
    public Result<Record> createRecord(
            @Valid @RequestBody RecordCreateRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(AuthInterceptor.USER_ID_KEY);
        Record record = recordService.createRecord(userId, request);
        return Result.success(record);
    }

    /**
     * 更新账目记录
     * 
     * 请求示例：
     * PUT /api/records/1
     * {
     *   "amount": 40.0,
     *   "type": 0,
     *   "categoryId": 1,
     *   "remark": "午餐（修改）"
     * }
     */
    @PutMapping("/{id}")
    public Result<Void> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody RecordUpdateRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(AuthInterceptor.USER_ID_KEY);
        recordService.updateRecord(userId, id, request);
        return Result.success();
    }

    /**
     * 删除账目记录
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRecord(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(AuthInterceptor.USER_ID_KEY);
        recordService.deleteRecord(userId, id);
        return Result.success();
    }

    /**
     * 分页查询账目记录（按月，包含分类名称和图标）
     * 
     * 请求示例：
     * GET /api/records?month=2026-06&page=1&size=20
     */
    @GetMapping
    public Result<IPage<RecordVO>> listRecords(
            @RequestParam String month,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(AuthInterceptor.USER_ID_KEY);
        IPage<RecordVO> records = recordService.listRecords(userId, month, page, size);
        return Result.success(records);
    }
}
