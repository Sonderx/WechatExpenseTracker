package com.expense.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.expense.common.BusinessException;
import com.expense.dto.RecordCreateRequest;
import com.expense.dto.RecordUpdateRequest;
import com.expense.entity.Record;
import com.expense.mapper.RecordMapper;
import com.expense.service.RecordService;
import com.expense.vo.RecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账目记录服务实现类
 * 
 * 功能说明：
 * - 实现账目记录的 CRUD 操作
 * - 金额约定：正数=收入，负数=支出
 * - 按月分页查询，支持下拉刷新和上拉加载
 */
@Service
public class RecordServiceImpl implements RecordService {

    @Autowired
    private RecordMapper recordMapper;

    /**
     * 创建账目记录
     * 根据 type 字段决定金额正负：0=支出（负数），1=收入（正数）
     */
    @Override
    public Record createRecord(Long userId, RecordCreateRequest request) {
        Record record = new Record();
        record.setUserId(userId);

        // 根据类型设置金额正负号
        BigDecimal amount = request.getAmount();
        if (request.getType() == 0) {
            // 支出，金额设为负数
            amount = amount.negate();
        }
        record.setAmount(amount);

        record.setCategoryId(request.getCategoryId());
        // 如果前端指定了时间则使用指定时间，否则使用当前时间
        record.setRecordTime(request.getRecordTime() != null ? request.getRecordTime() : LocalDateTime.now());
        record.setRemark(request.getRemark() != null ? request.getRemark() : "");

        recordMapper.insert(record);
        return record;
    }

    /**
     * 更新账目记录
     * 需要检查记录是否存在且属于当前用户
     */
    @Override
    public void updateRecord(Long userId, Long recordId, RecordUpdateRequest request) {
        Record record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(BusinessException.NOT_FOUND, "记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(BusinessException.FORBIDDEN, "无权修改此记录");
        }

        // 根据类型设置金额正负号
        BigDecimal amount = request.getAmount();
        if (request.getType() == 0) {
            amount = amount.negate();
        }
        record.setAmount(amount);
        record.setCategoryId(request.getCategoryId());
        if (request.getRecordTime() != null) {
            record.setRecordTime(request.getRecordTime());
        }
        record.setRemark(request.getRemark() != null ? request.getRemark() : "");

        recordMapper.updateById(record);
    }

    /**
     * 删除账目记录
     * 需要检查记录是否存在且属于当前用户
     */
    @Override
    public void deleteRecord(Long userId, Long recordId) {
        Record record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(BusinessException.NOT_FOUND, "记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(BusinessException.FORBIDDEN, "无权删除此记录");
        }
        recordMapper.deleteById(recordId);
    }

    /**
     * 分页查询账目记录（包含分类名称和图标）
     * 使用 JOIN 查询获取分类信息
     */
    @Override
    public IPage<RecordVO> listRecords(Long userId, String yearMonth, int page, int size) {
        Page<RecordVO> pageParam = new Page<>(page, size);
        return recordMapper.selectPageWithCategory(pageParam, userId, yearMonth);
    }
}
