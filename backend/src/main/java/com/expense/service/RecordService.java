package com.expense.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.expense.dto.RecordCreateRequest;
import com.expense.dto.RecordUpdateRequest;
import com.expense.entity.Record;
import com.expense.vo.RecordVO;

/**
 * 账目记录服务接口
 * 
 * 功能说明：
 * - 定义账目记录相关的业务操作
 * - 支持创建、修改、删除、查询记录
 */
public interface RecordService {

    /**
     * 创建账目记录
     * 
     * @param userId 用户ID
     * @param request 记录创建请求
     * @return 新创建的记录
     */
    Record createRecord(Long userId, RecordCreateRequest request);

    /**
     * 更新账目记录
     * 
     * @param userId 用户ID
     * @param recordId 记录ID
     * @param request 记录更新请求
     */
    void updateRecord(Long userId, Long recordId, RecordUpdateRequest request);

    /**
     * 删除账目记录
     * 
     * @param userId 用户ID
     * @param recordId 记录ID
     */
    void deleteRecord(Long userId, Long recordId);

    /**
     * 分页查询账目记录（按月，包含分类信息）
     * 
     * @param userId 用户ID
     * @param yearMonth 月份，格式：2026-06
     * @param page 页码（从1开始）
     * @param size 每页条数
     * @return 分页结果（包含分类名称和图标）
     */
    IPage<RecordVO> listRecords(Long userId, String yearMonth, int page, int size);
}
