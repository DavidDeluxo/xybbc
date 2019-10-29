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
        //校验必传参数用户id
        if (userWalletDetailDto.getFuid() == null) {
            throw new BizException(MallExceptionCode.REQUIRED_PARAM_MISSING);
        }
        //过滤掉明细类型为6支付宝下单，7微信下单，9客服取消订单，14售后工单调整信用额度，18信用额度-可用余额，19信用额度下单
        Criteria<UserDetail, Object> criteria = Criteria.of(UserDetail.class)
                .andEqualTo(UserDetail::getFuid, userWalletDetailDto.getFuid())
                .andLeft().andNotEqualTo(UserDetail::getFdetailType, 6)
                .andNotEqualTo(UserDetail::getFdetailType, 7)
                .andNotEqualTo(UserDetail::getFdetailType, 14)
                .andNotEqualTo(UserDetail::getFdetailType, 18)
                .andNotEqualTo(UserDetail::getFdetailType, 19)
                .addRight().sortDesc(UserDetail::getFmodifyTime);
        //查询收入
        if (!StringUtils.isEmpty(userWalletDetailDto.getQueryType()) && userWalletDetailDto.getQueryType() == 0) {
            criteria.andGreaterThan(UserDetail::getFincomeAmount, 0);
        }
        //查询支出
        if (!StringUtils.isEmpty(userWalletDetailDto.getQueryType()) && userWalletDetailDto.getQueryType() == 1) {
            criteria.andGreaterThan(UserDetail::getFexpenseAmount, 0);
        }
        //查询总数用于分页
        Result<Integer> totalResult = userDetailApi.countByCriteria(criteria);
        if (!totalResult.isSuccess()) {
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
            logger.error("统计我的钱包收支明细数量信息失败 当前页码{}",userWalletDetailDto.getCurrentPage());
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if (CollectionUtils.isEmpty(result.getData())) {
            return new PageVo<>(0, userWalletDetailDto.getCurrentPage(), userWalletDetailDto.getPageSize(), Lists.newArrayList());
        }
        //遍历数据进行当月收支合计数据填充
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
            //封装查询参数起始日期和用户身份
            UserWalletQueryDto userWalletQueryDto = new UserWalletQueryDto();
            userWalletQueryDto.setFuid(userWalletDetailDto.getFuid());
            userWalletQueryDto.setStartTime(beginDate);
            userWalletQueryDto.setEndTime(lastTime);
            //查询当月收支合计
            UserWalletDetailTotalVo userWalletDetailTotalVo = this.queryWalletTotal(userWalletQueryDto);
            //填充当月收入合计
            userWalletDetailVo.setFincomeAmountTotal(userWalletDetailTotalVo.getFincomeAmountTotal());
            //填充当月支出合计
            userWalletDetailVo.setFexpenseAmountTotal(userWalletDetailTotalVo.getFexpenseAmountTotal());
            //金额统一除以100
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
            //当数据类型为8：余额提现时需要判断提现方式
            if (detailType == 8) {
                String typeId = userDetail.getFtypeId();
                Criteria<UserAccountTransWater, Object> UserAccountTransWaterCriteria = Criteria.of(UserAccountTransWater.class);
                if (!StringUtils.isEmpty(typeId)) {
                    UserAccountTransWaterCriteria.andEqualTo(UserAccountTransWater::getFtransId, typeId);
                }
                //通过单号查询提现方式
                Result<UserAccountTransWater> userAccountTransWater = userAccountTransWaterApi.queryOneByCriteria(UserAccountTransWaterCriteria);
                if (!userAccountTransWater.isSuccess()) {
                    logger.error("通过单号查询提现方式失败 当前单号{}",typeId);
                    throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
                }
                userWalletDetailVo.setWithdrawType(userAccountTransWater.getData().getFwithdrawType());
            }
            return userWalletDetailVo;
        }).collect(Collectors.toList());
        return pageUtils.convert(totalResult.getData(), list, UserWalletDetailVo.class, userWalletDetailDto);
    }

    /**
     * @author lll
     * @version V1.0
     * @Description: 查询钱包收支当月合计
     * @Param: [userWalletDetailDto]
     * @return: PageVo<UserWalletDetailVo>
     * @date 2019/9/20 13:49
     */
    public UserWalletDetailTotalVo queryWalletTotal(UserWalletQueryDto userWalletQueryDto) {
        if (userWalletQueryDto.getFuid() == null) {
            throw new BizException(MallExceptionCode.NO_USER);
        }
        //过滤掉明细类型为6支付宝下单，7微信下单，9客服取消订单，14售后工单调整信用额度，18信用额度-可用余额，19信用额度下单
        Criteria<UserDetail, Object> criteria = Criteria.of(UserDetail.class)
                .andBetween(UserDetail::getFmodifyTime, userWalletQueryDto.getStartTime(), userWalletQueryDto.getEndTime())
                .andEqualTo(UserDetail::getFuid, userWalletQueryDto.getFuid())
                .andLeft().andNotEqualTo(UserDetail::getFdetailType, 6)
                .andNotEqualTo(UserDetail::getFdetailType, 7)
                .andNotEqualTo(UserDetail::getFdetailType, 14)
                .andNotEqualTo(UserDetail::getFdetailType, 18)
                .andNotEqualTo(UserDetail::getFdetailType, 19)
                .addRight().sortDesc(UserDetail::getFmodifyTime);
        Result<List<UserDetail>> result = userDetailApi.queryByCriteria(criteria.fields(
                UserDetail::getFexpenseAmount
                , UserDetail::getFincomeAmount
        ));
        if (!result.isSuccess()) {
            logger.error("查询用户收支明细失败 当月起始日期{} 截止日期{}",userWalletQueryDto.getStartTime(),userWalletQueryDto.getEndTime());
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        Long incomeAmountTotal = 0L;
        Long expenseAmountTotal = 0L;
        //循环累加计算出当月收支合计金额
        for (UserDetail userDetail : result.getData()) {
            Long incomeAmount = userDetail.getFincomeAmount();
            Long expenseAmount = userDetail.getFexpenseAmount();
            incomeAmountTotal += incomeAmount;
            expenseAmountTotal += expenseAmount;
        }
        //金额除以100
        BigDecimal incomeAmount = new BigDecimal(incomeAmountTotal)
                .divide(PageConfigContants.BIG_DECIMAL_100, 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal expenseAmount = new BigDecimal(expenseAmountTotal)
                .divide(PageConfigContants.BIG_DECIMAL_100, 2, BigDecimal.ROUND_HALF_UP);
        UserWalletDetailTotalVo userWalletDetailTotalVo = new UserWalletDetailTotalVo();
        //封装当月收入合计
        userWalletDetailTotalVo.setFincomeAmountTotal(incomeAmount);
        //封装当月支出合计
        userWalletDetailTotalVo.setFexpenseAmountTotal(expenseAmount);
        return userWalletDetailTotalVo;
    }
}
