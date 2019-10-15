package com.xingyun.bbc.mall.service.impl;


import com.google.common.collect.Lists;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;


import com.xingyun.bbc.core.query.Criteria;

import com.xingyun.bbc.core.user.api.UserAccountTransWaterApi;
import com.xingyun.bbc.core.user.api.UserDetailApi;
import com.xingyun.bbc.core.user.po.UserAccountTransWater;
import com.xingyun.bbc.core.user.po.UserDetail;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.DateStyle;
import com.xingyun.bbc.mall.base.utils.DateUtil;

import com.xingyun.bbc.mall.base.utils.PageUtils;

import com.xingyun.bbc.mall.common.constans.PageConfigContants;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import com.xingyun.bbc.mall.model.dto.UserWalletDetailDto;

import com.xingyun.bbc.mall.model.dto.UserWalletQueryDto;
import com.xingyun.bbc.mall.model.vo.PageVo;
import com.xingyun.bbc.mall.model.vo.UserWalletDetailTotalVo;
import com.xingyun.bbc.mall.model.vo.UserWalletDetailVo;

import com.xingyun.bbc.mall.service.WalletTurningService;

import org.apache.commons.collections.CollectionUtils;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * @author lll
 * @Title:
 * @Description:
 * @date 2019-09-17 11:00
 */
@Service
public class WalletTurningServiceImpl implements WalletTurningService {

    public static final Logger logger = LoggerFactory.getLogger(WalletTurningServiceImpl.class);

    @Autowired
    private UserDetailApi userDetailApi;
    @Autowired
    private Mapper dozerMapper;
    @Autowired
    private PageUtils pageUtils;
    @Autowired
    UserAccountTransWaterApi userAccountTransWaterApi;

    /**
     * @author lll
     * @version V1.0
     * @Description: 查询钱包收支明细列表
     * @Param: [userWalletDetailDto]
     * @return: PageVo<UserWalletDetailVo>
     * @date 2019/9/20 13:49
     */
    @Override
    public PageVo<UserWalletDetailVo> queryWalletTurningList(UserWalletDetailDto userWalletDetailDto) {
        if (userWalletDetailDto.getFuid() == null) {
            throw new BizException(MallExceptionCode.NO_USER);
        }
        Criteria<UserDetail, Object> criteria = Criteria.of(UserDetail.class)
                .andEqualTo(UserDetail::getFuid, userWalletDetailDto.getFuid())
                .andLeft().andNotEqualTo(UserDetail::getFdetailType, 6)
                .andNotEqualTo(UserDetail::getFdetailType, 7)
                .andNotEqualTo(UserDetail::getFdetailType, 9)
                .andNotEqualTo(UserDetail::getFdetailType, 14)
                .addRight().sortDesc(UserDetail::getFmodifyTime);
        if (!StringUtils.isEmpty(userWalletDetailDto.getQueryType()) && userWalletDetailDto.getQueryType() == 0) {
            criteria.andGreaterThan(UserDetail::getFincomeAmount, 0);
        }
        if (!StringUtils.isEmpty(userWalletDetailDto.getQueryType()) && userWalletDetailDto.getQueryType() == 1) {
            criteria.andGreaterThan(UserDetail::getFexpenseAmount, 0);
        }
        Result<Integer> totalResult = userDetailApi.countByCriteria(criteria);
        if (!totalResult.isSuccess()) {
            logger.info("统计我的钱包收支明细数量信息失败");
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if (0 == totalResult.getData() || Objects.isNull(totalResult.getData())) {
            return new PageVo<>(0, userWalletDetailDto.getCurrentPage(), userWalletDetailDto.getPageSize(), Lists.newArrayList());
        }
        //查询用户交易明细表信息
        Result<List<UserDetail>> result = userDetailApi.queryByCriteria(criteria.fields(
                UserDetail::getFexpenseAmount
                , UserDetail::getFincomeAmount
                , UserDetail::getFbalance
                , UserDetail::getFdetailType
                , UserDetail::getFtypeId
                , UserDetail::getFmodifyTime
        ).page(userWalletDetailDto.getCurrentPage(), userWalletDetailDto.getPageSize()));
        if (!result.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if (CollectionUtils.isEmpty(result.getData())) {
            return new PageVo<>(0, userWalletDetailDto.getCurrentPage(), userWalletDetailDto.getPageSize(), Lists.newArrayList());
        }
        List<UserWalletDetailVo> list = result.getData().stream().map(userDetail -> {
            UserWalletDetailVo userWalletDetailVo = dozerMapper.map(userDetail, UserWalletDetailVo.class);
            Date modifyTime = userWalletDetailVo.getFmodifyTime();
            String modifyTimeStr = DateUtil.DateToString(modifyTime, DateStyle.YYYY_MM);
            String newTime = modifyTimeStr + "-01 00:00:00";
            //得到当月第一天开始日期
            Date beginDate = DateUtil.StringToDate(newTime);
            int year = DateUtil.getYear(modifyTime);
            int month = DateUtil.getMonth(modifyTime) + 1;
            //得到当月最后一天结束日期
            Date lastDay = DateUtil.getLastActualDayOfMonth(year, month);
            String lastDayTime = DateUtil.DateToString(lastDay, DateStyle.YYYY_MM_DD);
            String lastDayTimeStr = lastDayTime + " 23:59:59";
            Date lastTime = DateUtil.StringToDate(lastDayTimeStr);

            UserWalletQueryDto userWalletQueryDto = new UserWalletQueryDto();
            userWalletQueryDto.setFuid(userWalletDetailDto.getFuid());
            userWalletQueryDto.setStartTime(beginDate);
            userWalletQueryDto.setEndTime(lastTime);
            UserWalletDetailTotalVo userWalletDetailTotalVo = this.queryWalletTotal(userWalletQueryDto);
            userWalletDetailVo.setFincomeAmountTotal(userWalletDetailTotalVo.getFincomeAmountTotal());
            userWalletDetailVo.setFexpenseAmountTotal(userWalletDetailTotalVo.getFexpenseAmountTotal());
            BigDecimal balance = userWalletDetailVo.getFbalance()
                    .divide(PageConfigContants.BIG_DECIMAL_100, 2, BigDecimal.ROUND_HALF_UP);
            userWalletDetailVo.setFbalance(balance);
            BigDecimal incomeAmount = userWalletDetailVo.getFincomeAmount()
                    .divide(PageConfigContants.BIG_DECIMAL_100, 2, BigDecimal.ROUND_HALF_UP);
            userWalletDetailVo.setFincomeAmount(incomeAmount);
            BigDecimal expenseAmount = userWalletDetailVo.getFexpenseAmount()
                    .divide(PageConfigContants.BIG_DECIMAL_100, 2, BigDecimal.ROUND_HALF_UP);
            userWalletDetailVo.setFexpenseAmount(expenseAmount);
            //封装提现方式
            Integer detailType = userDetail.getFdetailType();
            if (detailType == 8) {
                String typeId = userDetail.getFtypeId();
                Criteria<UserAccountTransWater, Object> UserAccountTransWaterCriteria = Criteria.of(UserAccountTransWater.class);
                if (!StringUtils.isEmpty(typeId)) {
                    UserAccountTransWaterCriteria.andEqualTo(UserAccountTransWater::getFtransId, typeId);
                }
                Result<UserAccountTransWater> userAccountTransWater = userAccountTransWaterApi.queryOneByCriteria(UserAccountTransWaterCriteria);
                userWalletDetailVo.setWithdrawType(userAccountTransWater.getData().getFwithdrawType());
            }
            return userWalletDetailVo;
        }).collect(Collectors.toList());
        return pageUtils.convert(totalResult.getData(), list, UserWalletDetailVo.class, userWalletDetailDto);
    }


    public UserWalletDetailTotalVo queryWalletTotal(UserWalletQueryDto userWalletQueryDto) {
        if (userWalletQueryDto.getFuid() == null) {
            throw new BizException(MallExceptionCode.NO_USER);
        }
        Criteria<UserDetail, Object> criteria = Criteria.of(UserDetail.class)
                .andBetween(UserDetail::getFmodifyTime, userWalletQueryDto.getStartTime(), userWalletQueryDto.getEndTime())
                .andEqualTo(UserDetail::getFuid, userWalletQueryDto.getFuid())
                .andLeft().andNotEqualTo(UserDetail::getFdetailType, 6)
                .andNotEqualTo(UserDetail::getFdetailType, 7)
                .andNotEqualTo(UserDetail::getFdetailType, 9)
                .andNotEqualTo(UserDetail::getFdetailType, 14)
                .addRight().sortDesc(UserDetail::getFmodifyTime);
        Result<List<UserDetail>> result = userDetailApi.queryByCriteria(criteria.fields(
                UserDetail::getFexpenseAmount
                , UserDetail::getFincomeAmount
        ));
        if (!result.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if (CollectionUtils.isEmpty(result.getData())) {
            return null;
        }
        Long incomeAmountTotal = 0L;
        Long expenseAmountTotal = 0L;
        for (UserDetail userDetail : result.getData()) {
            Long incomeAmount = userDetail.getFincomeAmount();
            Long expenseAmount = userDetail.getFexpenseAmount();
            incomeAmountTotal += incomeAmount;
            expenseAmountTotal += expenseAmount;
        }
        BigDecimal incomeAmount = new BigDecimal(incomeAmountTotal)
                .divide(PageConfigContants.BIG_DECIMAL_100, 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal expenseAmount = new BigDecimal(expenseAmountTotal)
                .divide(PageConfigContants.BIG_DECIMAL_100, 2, BigDecimal.ROUND_HALF_UP);
        UserWalletDetailTotalVo userWalletDetailTotalVo = new UserWalletDetailTotalVo();
        userWalletDetailTotalVo.setFincomeAmountTotal(incomeAmount);
        userWalletDetailTotalVo.setFexpenseAmountTotal(expenseAmount);
        return userWalletDetailTotalVo;
    }
}
