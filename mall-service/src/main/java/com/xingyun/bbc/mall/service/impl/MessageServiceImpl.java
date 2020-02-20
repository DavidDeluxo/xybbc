package com.xingyun.bbc.mall.service.impl;

import com.google.common.collect.Lists;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.operate.api.MessageSignApi;
import com.xingyun.bbc.core.operate.api.MessageUserRecordApi;
import com.xingyun.bbc.core.operate.api.SubjectApi;
import com.xingyun.bbc.core.operate.po.MessageSign;
import com.xingyun.bbc.core.operate.po.MessageUserRecord;
import com.xingyun.bbc.core.operate.po.Subject;
import com.xingyun.bbc.core.order.api.OrderPaymentApi;
import com.xingyun.bbc.core.order.po.OrderPayment;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.sku.api.GoodsSkuApi;
import com.xingyun.bbc.core.sku.po.GoodsSku;
import com.xingyun.bbc.core.supplier.api.SupplierOrderSkuApi;
import com.xingyun.bbc.core.supplier.api.SupplierTransportOrderApi;
import com.xingyun.bbc.core.supplier.po.SupplierOrderSku;
import com.xingyun.bbc.core.supplier.po.SupplierTransportOrder;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.express.api.ExpressBillProviderApi;
import com.xingyun.bbc.express.model.dto.ExpressBillDto;
import com.xingyun.bbc.express.model.vo.ExpressBillDetailVo;
import com.xingyun.bbc.express.model.vo.ExpressBillVo;
import com.xingyun.bbc.mall.base.utils.ResultUtils;
import com.xingyun.bbc.mall.common.ensure.Ensure;
import com.xingyun.bbc.mall.common.enums.MessageGroupTypeEnum;
import com.xingyun.bbc.mall.common.enums.MessagePushTypeEnum;
import com.xingyun.bbc.mall.common.enums.MessageTypeEnum;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import com.xingyun.bbc.mall.model.dto.MessageQueryDto;
import com.xingyun.bbc.mall.model.dto.MessageUpdateDto;
import com.xingyun.bbc.mall.model.vo.*;
import com.xingyun.bbc.mall.service.MessageService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Description 消息中心 - 实现类
 * @ClassName MessageServiceImpl
 * @Author ming.yiFei
 * @Date 2019/12/23 11:37
 **/
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    @Resource
    private MessageUserRecordApi userRecordApi;

    @Resource
    private SupplierTransportOrderApi transportOrderApi;

    @Resource
    private ExpressBillProviderApi expressBillProvider;

    @Resource
    private OrderPaymentApi paymentApi;

    @Resource
    private SupplierOrderSkuApi supplierOrderSkuApi;

    @Resource
    private GoodsSkuApi goodsSkuApi;

    @Resource
    private UserApi userApi;

    @Resource
    private SubjectApi subjectApi;

    @Value("${urlConfig.subjectActivity}")
    String subjectPageUrl;

    @Resource
    private MessageSignApi messageSignApi;

    private final Object object = new Object();

    /**
     * 匹配${...}
     */
    private static final Pattern COMPILE = Pattern.compile("(\\$\\{)([\\S\\s]*)(})");

    /**
     * @param userId
     * @return
     * @see MessageService#queryMessageGroupByUserId(Long)
     */
    @Override
    public Result<List<MessageCenterVo>> queryMessageGroupByUserId(Long userId) {
        // fix 新注册的用户，历史的站内信息，无需预留
        Result<User> userInfoResult = userApi.queryOneByCriteria(Criteria.of(User.class).fields(User::getFcreateTime).andEqualTo(User::getFuid, userId));
        User userInfo = userInfoResult.getData();
        Ensure.that(userInfo).isNotNull(MallExceptionCode.SYSTEM_ERROR);
        Result<List<MessageUserRecord>> userRecordResult = userRecordApi.queryByCriteria(Criteria.of(MessageUserRecord.class)
                .andEqualTo(MessageUserRecord::getFsendStatus, 2)
                .andIn(MessageUserRecord::getFuid, Lists.newArrayList(userId, 0))
                .andGreaterThanOrEqualTo(MessageUserRecord::getFexpirationDate, new Date())
                .andGreaterThan(MessageUserRecord::getFcreateTime,userInfo.getFcreateTime())
                .sortDesc(MessageUserRecord::getFcreateTime));
        if (!userRecordResult.isSuccess()) {
            throw new BizException(MallExceptionCode.SYSTEM_ERROR);
        }
        List<MessageUserRecord> userRecords = userRecordResult.getData();
        if (CollectionUtils.isEmpty(userRecords)) {
            ArrayList<MessageCenterVo> messageCenterVos = Lists.newArrayList();
            for (int i = 0; i < MessageGroupTypeEnum.values().length; i++) {
                messageCenterVos.add(new MessageCenterVo(MessageGroupTypeEnum.values()[i].getCode()));
            }
            return Result.success(messageCenterVos);
        }
        ArrayList<MessageCenterVo> messageCenterVos = Lists.newArrayList();
        Map<Integer, List<MessageUserRecord>> messageGroup = userRecords.stream().collect(Collectors.groupingBy(MessageUserRecord::getFmessageGroup));
        for (Map.Entry<Integer, List<MessageUserRecord>> recordEntry : messageGroup.entrySet()) {
            Integer recordEntryKey = recordEntry.getKey();
            List<MessageUserRecord> recordList = recordEntry.getValue();
            MessageUserRecord messageUserRecord = recordList.get(0);
            Map<Integer, List<MessageUserRecord>> records = recordList.stream().filter(r -> r.getFreaded().equals(0)).collect(Collectors.groupingBy(MessageUserRecord::getFisCommon));
            // 全局消息
            List<MessageUserRecord> global = records.get(1);
            int local = CollectionUtils.isEmpty(records.get(0)) ? 0 : records.get(0).size();
            if (CollectionUtils.isEmpty(global)) {
                messageCenterVos.add(new MessageCenterVo(recordEntryKey
                        , messageUserRecord.getFtitle()
                        , local
                        , messageUserRecord.getFcreateTime().getTime()));
                continue;
            }
            List<Long> recordIds = global.stream().map(MessageUserRecord::getFmessageUserRecordId).collect(Collectors.toList());
            Result<List<MessageSign>> readResult = messageSignApi.queryByCriteria(Criteria.of(MessageSign.class)
                    .andIn(MessageSign::getFrecordId, recordIds)
                    .andEqualTo(MessageSign::getFsubjectType, 1)
                    .andEqualTo(MessageSign::getFsubjectId, userId));
            if (!readResult.isSuccess()) {
                throw new BizException(MallExceptionCode.SYSTEM_ERROR);
            }
            // 全都未读
            List<MessageSign> signs = readResult.getData();
            if (CollectionUtils.isEmpty(signs)) {
                messageCenterVos.add(new MessageCenterVo(recordEntryKey
                        , messageUserRecord.getFtitle()
                        , local + global.size()
                        , messageUserRecord.getFcreateTime().getTime()));
                continue;
            }
            // 已读ID
            Set<Long> signSets = signs.stream().map(MessageSign::getFrecordId).collect(Collectors.toSet());
            messageCenterVos.add(new MessageCenterVo(recordEntryKey
                    , messageUserRecord.getFtitle()
                    , local + global.size() - signSets.size()
                    , messageUserRecord.getFcreateTime().getTime()));
        }
        List<Integer> types = messageCenterVos.stream().map(MessageCenterVo::getMessageGroupType).collect(Collectors.toList());
        for (int i = 0; i < MessageGroupTypeEnum.values().length; i++) {
            Integer code = MessageGroupTypeEnum.values()[i].getCode();
            if (!types.contains(code)) {
                messageCenterVos.add(new MessageCenterVo(code));
            }
        }
        return Result.success(messageCenterVos);
    }

    /**
     * @param dto
     * @return
     * @see MessageService#queryMessageList(MessageQueryDto)
     */
    @Override
    public Result<PageVo<MessageListVo>> queryMessageList(MessageQueryDto dto) {
        // fix 新注册的用户，历史的站内信息，无需预留
        Result<User> userInfoResult = userApi.queryOneByCriteria(Criteria.of(User.class).fields(User::getFcreateTime).andEqualTo(User::getFuid, dto.getUserId()));
        User userInfo = userInfoResult.getData();
        Ensure.that(userInfo).isNotNull(MallExceptionCode.SYSTEM_ERROR);
        Criteria<MessageUserRecord, Object> userRecordObjectCriteria = Criteria.of(MessageUserRecord.class)
                .andEqualTo(MessageUserRecord::getFsendStatus, 2)
                .andIn(MessageUserRecord::getFuid, Lists.newArrayList(dto.getUserId(), 0))
                .andEqualTo(MessageUserRecord::getFmessageGroup, dto.getMessageCenterType())
                .andGreaterThanOrEqualTo(MessageUserRecord::getFexpirationDate, new Date())
                .andGreaterThan(MessageUserRecord::getFcreateTime,userInfo.getFcreateTime())
                .sortDesc(MessageUserRecord::getFcreateTime);
        Result<Integer> userRecordResult = userRecordApi.countByCriteria(userRecordObjectCriteria);
        if (!userRecordResult.isSuccess()) {
            throw new BizException(MallExceptionCode.SYSTEM_ERROR);
        }
        Integer count = userRecordResult.getData();
        if (count < 1) {
            return Result.success(new PageVo(0, dto.getCurrentPage(), dto.getPageSize(), Lists.newArrayList()));
        }
        Result<List<MessageUserRecord>> userRecordsResult = userRecordApi.queryByCriteria(userRecordObjectCriteria.page(dto.getCurrentPage(), dto.getPageSize()));
        if (!userRecordsResult.isSuccess()) {
            throw new BizException(MallExceptionCode.SYSTEM_ERROR);
        }
        List<MessageUserRecord> userRecords = userRecordsResult.getData();
        ArrayList<MessageListVo> messageListVos = Lists.newArrayList();
        userRecords.forEach(record -> {
            MessageListVo messageListVo = new MessageListVo();
            messageListVo.setMessageId(record.getFmessageUserRecordId());
            messageListVo.setTitle(record.getFtitle());
            messageListVo.setReceiveTime(record.getFcreateTime());
            // 暂不需要单条消息读取状态
            // messageListVo.setIsRead(record.getFreaded());
            messageListVo.setPushType(record.getFpushType());
            messageListVo.setRedirectType(record.getFredirectType());
            // 消息类型
            Integer ftype = record.getFtype();
            messageListVo.setMessageType(ftype);
            String frefId = record.getFrefId();
            // 手动、自动
            MessagePushTypeEnum pushTypeEnum = MessagePushTypeEnum.getEnum(record.getFpushType());
            switch (pushTypeEnum) {
                // 自动消息类型
                case AUTO:
                    MessageTypeEnum autoTypeEnum = MessageTypeEnum.getEnum(ftype);
                    switch (autoTypeEnum) {
                        // 发货单发货
                        case GOODS_SHIPPED:
                            if (StringUtils.isBlank(frefId)) {
                                throw new BizException(new MallExceptionCode("", "消息未绑定发货单号"));
                            }
                            // 发货单号、商品数量、订单号、收件人
                            Result<SupplierTransportOrder> transportOrderResult = transportOrderApi.queryById(frefId);
                            if (!transportOrderResult.isSuccess()) {
                                throw new BizException(MallExceptionCode.SYSTEM_ERROR);
                            }
                            SupplierTransportOrder transportOrder = transportOrderResult.getData();
                            if (transportOrder == null) {
                                throw new BizException(new MallExceptionCode("", "消息绑定发货单号错误"));
                            }

                            String logisticsNo = transportOrder.getForderLogisticsNo();
                            String fshippingCode = transportOrder.getFshippingCode();
                            String fshippingName = transportOrder.getFshippingName();
                            Result<OrderPayment> orderPaymentResult = paymentApi.queryById(transportOrder.getForderPaymentId());
                            if (!orderPaymentResult.isSuccess()) {
                                throw new BizException(MallExceptionCode.SYSTEM_ERROR);
                            }
                            ExpressBillDto expressBillDto = new ExpressBillDto();
                            expressBillDto.setPhone(orderPaymentResult.getData().getFdeliveryMobile());
                            expressBillDto.setBillNo(logisticsNo);
                            expressBillDto.setCompanyCode(fshippingCode);
                            expressBillDto.setCompanyName(fshippingName);
                            Result<ExpressBillVo> billVoResult = expressBillProvider.query(expressBillDto);
                            if (!billVoResult.isSuccess()) {
                                throw new BizException(MallExceptionCode.SYSTEM_ERROR);
                            }
                            ExpressBillVo expressBillVo = billVoResult.getData();
                            List<ExpressBillDetailVo> expressBillVoData = expressBillVo.getData();
                            // 发货单号、订单号
                            MessageSelfInfoVo selfInfoVo = new MessageSelfInfoVo();
                            if (CollectionUtils.isNotEmpty(expressBillVoData)) {
                                ExpressBillDetailVo billDetailVo = expressBillVoData.get(0);
                                selfInfoVo.setTrajectoryContext(billDetailVo.getContext());
                                selfInfoVo.setTrajectoryTime(billDetailVo.getFtime());
                            }

                            selfInfoVo.setOrderLogisticsNo(frefId);
                            selfInfoVo.setOrderId(transportOrder.getForderId());
                            // 商品数量
                            Result<List<SupplierOrderSku>> countByCriteria = supplierOrderSkuApi.queryByCriteria(Criteria.of(SupplierOrderSku.class)
                                    .andEqualTo(SupplierOrderSku::getFsupplierOrderId, transportOrder.getFsupplierOrderId())
                                    .sortDesc(SupplierOrderSku::getFcreateTime));
                            if (!countByCriteria.isSuccess()) {
                                throw new BizException(MallExceptionCode.SYSTEM_ERROR);
                            }
                            List<SupplierOrderSku> orderSkus = countByCriteria.getData();
                            selfInfoVo.setSkuNum(orderSkus.size());
                            // 收件人
                            selfInfoVo.setDeliveryName(orderPaymentResult.getData().getFdeliveryName());
                            String fskuCode = orderSkus.get(0).getFskuCode();
                            Result<GoodsSku> goodsSkuResult = goodsSkuApi.queryOneByCriteria(Criteria.of(GoodsSku.class)
                                    .andEqualTo(GoodsSku::getFskuCode, fskuCode)
                                    .fields(GoodsSku::getFskuThumbImage));
                            if (!goodsSkuResult.isSuccess()) {
                                throw new BizException(MallExceptionCode.SYSTEM_ERROR);
                            }
                            String fskuThumbImage = goodsSkuResult.getData().getFskuThumbImage();
                            messageListVo.setImageUrl(new ImageVo(fskuThumbImage));
                            messageListVo.setSelfInfoVo(selfInfoVo);
                            break;
                        // 注册成功
                        case REGISTER_SUCCESSED:
                            // 修改手机号
                        case MODIFY_NUMBER:
                            // 优惠券到账
                        case COUPON_RECEIVE:
                            // 优惠券即将到期(24小时)
                        case COUPON_ALMOST_OVERDUE:
                            messageListVo.setDesc(record.getFcontent().replaceAll("<(?:[^\"'>]|\"[^\"]*\"|'[^']*')*>", ""));
                            break;
                        // 用户认证成功
                        case USER_VERIFY:
                            Result<User> userResult = userApi.queryById(record.getFuid());
                            if (!userResult.isSuccess()) {
                                throw new BizException(MallExceptionCode.SYSTEM_ERROR);
                            }
                            User user = userResult.getData();
                            if (user == null) {
                                throw new BizException(new MallExceptionCode("", "未查到该消息绑定用户"));
                            }
                            MessageSelfInfoVo userSelfInfoVo = new MessageSelfInfoVo();
                            userSelfInfoVo.setAuthenticationType(user.getFoperateType());
                            messageListVo.setSelfInfoVo(userSelfInfoVo);
                            messageListVo.setDesc(record.getFcontent().replaceAll("<(?:[^\"'>]|\"[^\"]*\"|'[^']*')*>", ""));
                            break;
                        default:
                            throw new BizException(new MallExceptionCode("", "不存在的自动消息类型"));
                    }
                    break;
                // 手动消息类型
                case MANUAL:
                    MessageTypeEnum manualTypeEnum = MessageTypeEnum.getEnum(ftype);
                    switch (manualTypeEnum) {
                        case XY_ANNOUNCEMENT:
                            String recordFcontent = record.getFcontent();
                            if (StringUtils.isBlank(recordFcontent)) {
                                messageListVo.setDesc(recordFcontent.replaceAll("<(?:[^\"'>]|\"[^\"]*\"|'[^']*')*>", ""));
                                break;
                            }
                            // 从${xxx}后开始截取
                            Matcher matcher = COMPILE.matcher(recordFcontent);
                            boolean isTrue = matcher.find();
                            String imageFlag = "<img";
                            if (!isTrue) {
                                if (recordFcontent.contains(imageFlag)) {
                                    messageListVo.setDesc("");
                                    break;
                                }
                                messageListVo.setDesc(recordFcontent.replaceAll("<(?:[^\"'>]|\"[^\"]*\"|'[^']*')*>", ""));
                                break;
                            }
                            int index = recordFcontent.indexOf(matcher.group(0));
                            String substring = recordFcontent.substring(index + matcher.group(0).length());
                            messageListVo.setDesc(substring.replaceAll("<(?:[^\"'>]|\"[^\"]*\"|'[^']*')*>", ""));
                            break;
                        case GOODS_MESSAGE:
                            Result<GoodsSku> goodsSkuResult = goodsSkuApi.queryOneByCriteria(Criteria.of(GoodsSku.class)
                                    .andEqualTo(GoodsSku::getFskuCode, frefId)
                                    .fields(GoodsSku::getFskuName
                                            , GoodsSku::getFskuId
                                            , GoodsSku::getFgoodsId
                                            , GoodsSku::getFskuThumbImage));
                            if (!goodsSkuResult.isSuccess()) {
                                throw new BizException(MallExceptionCode.SYSTEM_ERROR);
                            }
                            GoodsSku goodsSku = goodsSkuResult.getData();
                            if (goodsSku == null) {
                                throw new BizException(new MallExceptionCode("", "商品消息未绑定SKU编码"));
                            }
                            MessageSelfInfoVo goodsSelfInfo = new MessageSelfInfoVo();
                            goodsSelfInfo.setGoodsId(goodsSku.getFgoodsId());
                            goodsSelfInfo.setSkuId(goodsSku.getFskuId());
                            messageListVo.setImageUrl(new ImageVo(goodsSku.getFskuThumbImage()));
                            messageListVo.setDesc(goodsSku.getFskuName());
                            messageListVo.setSelfInfoVo(goodsSelfInfo);
                            break;
                        case OTHER:
                            messageListVo.setDesc(record.getFcontent().replaceAll("<(?:[^\"'>]|\"[^\"]*\"|'[^']*')*>", ""));
                            break;
                        case SUBJECT_ACTIVITY:
                            Long fsubjectId;
                            try {
                                fsubjectId = Long.parseLong(record.getFrefId());
                            } catch (NumberFormatException e) {
                                log.error("消息id:{}, 绑定引用id不正确", record.getFmessageUserRecordId(), e);
                                throw new BizException(new MallExceptionCode("", "商品消息绑定引用id不正确"));
                            }
                            Subject subject = ResultUtils.getDataNotNull(subjectApi.queryById(fsubjectId));
                            messageListVo.setTitle(subject.getFsubjectName());
                            messageListVo.setDesc(subject.getFsubjectDescription());
                            messageListVo.setImageUrl(new ImageVo(subject.getFsubjectMobilePic()));
                            MessageSelfInfoVo selfInfoVoForSubject = new MessageSelfInfoVo();
                            selfInfoVoForSubject.setUrl(subjectPageUrl + subject.getFsubjectId());
                            selfInfoVoForSubject.setFsubjectId(subject.getFsubjectId());
                            //判断是否过期
                            if (subject.getFsubjectEndTime().compareTo(new Date()) < 0) {
                                selfInfoVoForSubject.setExpire(1);
                            } else {
                                selfInfoVoForSubject.setExpire(0);
                            }
                            messageListVo.setSelfInfoVo(selfInfoVoForSubject);
                            break;
                        default:
                            throw new BizException(new MallExceptionCode("", "不存在的手动消息类型"));
                    }
                    break;
                default:
                    break;
            }
            messageListVos.add(messageListVo);
        });

        return Result.success(new PageVo<>(count, dto.getCurrentPage(), dto.getPageSize(), messageListVos));
    }

    /**
     * @param dto
     * @return
     * @see MessageService#queryMessageDetailById(MessageQueryDto)
     */
    @Override
    public Result<MessageDetailVo> queryMessageDetailById(MessageQueryDto dto) {
        Result<MessageUserRecord> userRecordResult = userRecordApi.queryById(dto.getMessageId());
        if (!userRecordResult.isSuccess()) {
            throw new BizException(MallExceptionCode.SYSTEM_ERROR);
        }
        MessageUserRecord userRecord = userRecordResult.getData();
        if (userRecord == null) {
            throw new BizException(new MallExceptionCode("", "消息不存在"));
        }
        String recordOthercontent = userRecord.getFcontent();
        // 去除${}
        Matcher matcherOther = COMPILE.matcher(recordOthercontent);
        boolean isTrueOther = matcherOther.find();
        if (!isTrueOther) {
            return Result.success(new MessageDetailVo(userRecord.getFcontent(), userRecord.getFcreateTime(), userRecord.getFtitle()));
        }
        int indexOther = recordOthercontent.indexOf(matcherOther.group(0));
        StringBuilder builder = new StringBuilder(recordOthercontent.substring(0, indexOther));
        String replaceAll = matcherOther.group(0).replaceAll("[${|}]", "");
        builder.append(replaceAll)
                .append(recordOthercontent.substring(indexOther + matcherOther.group(0).length()));
        return Result.success(new MessageDetailVo(builder.toString(), userRecord.getFcreateTime(), userRecord.getFtitle()));
    }

    /**
     * @param dto
     * @return
     * @see MessageService#updateMessageForRead(MessageUpdateDto)
     */
    @GlobalTransactional
    @Override
    public Result updateMessageForRead(MessageUpdateDto dto) {
        Criteria<MessageUserRecord, Object> userRecordObjectCriteria = Criteria.of(MessageUserRecord.class)
                .andEqualTo(MessageUserRecord::getFreaded, 0)
                .andEqualTo(MessageUserRecord::getFsendStatus, 2)
                .andIn(MessageUserRecord::getFuid, Lists.newArrayList(dto.getUserId(), 0))
                .andEqualTo(MessageUserRecord::getFmessageGroup, dto.getMessageCenterType())
                .andGreaterThanOrEqualTo(MessageUserRecord::getFexpirationDate, new Date());
        Result<List<MessageUserRecord>> userRecordResult = userRecordApi.queryByCriteria(userRecordObjectCriteria);
        List<MessageUserRecord> userRecords = userRecordResult.getData();
        if (CollectionUtils.isEmpty(userRecords)) {
            return Result.success();
        }
        Map<Integer, List<MessageUserRecord>> userGroup = userRecords.stream().collect(Collectors.groupingBy(MessageUserRecord::getFisCommon));
        List<MessageUserRecord> local = userGroup.get(0);
        if (CollectionUtils.isNotEmpty(local)) {
            local.forEach(messageUserRecord -> {
                MessageUserRecord userRecord = new MessageUserRecord();
                userRecord.setFmessageUserRecordId(messageUserRecord.getFmessageUserRecordId());
                userRecord.setFreaded(1);
                Result<Integer> updateRecord = userRecordApi.updateNotNull(userRecord);
                if (!updateRecord.isSuccess()) {
                    throw new BizException(MallExceptionCode.SYSTEM_ERROR);
                }
            });
        }

        List<MessageUserRecord> messageUserRecords = userGroup.get(1);
        if (CollectionUtils.isEmpty(messageUserRecords)) {
            return Result.success();
        }
        // 全局消息ID
        Set<Long> userRecordIds = messageUserRecords.stream().map(MessageUserRecord::getFmessageUserRecordId).collect(Collectors.toSet());
        // 单机暂用
        synchronized (object) {
            Result<List<MessageSign>> checkRecordResult = messageSignApi.queryByCriteria(Criteria.of(MessageSign.class)
                    .andIn(MessageSign::getFrecordId, userRecordIds)
                    .andEqualTo(MessageSign::getFsubjectType, 1)
                    .andEqualTo(MessageSign::getFsubjectId, dto.getUserId()));
            if (!checkRecordResult.isSuccess()) {
                throw new BizException(MallExceptionCode.SYSTEM_ERROR);
            }
            // 说明已新增
            List<MessageSign> messageSigns = checkRecordResult.getData();
            // 已插入全局消息ID
            Set<Long> set = messageSigns.stream().map(MessageSign::getFrecordId).collect(Collectors.toSet());
            userRecordIds.removeAll(set);
            if (CollectionUtils.isEmpty(userRecordIds)) {
                return Result.success();
            }
            ArrayList<MessageSign> capacity = new ArrayList<>();
            messageUserRecords.stream().filter(m -> userRecordIds.contains(m.getFmessageUserRecordId())).forEach(messageUserRecord -> {
                MessageSign messageSign = new MessageSign();
                messageSign.setFrecordId(messageUserRecord.getFmessageUserRecordId());
                messageSign.setFsubjectId(dto.getUserId());
                messageSign.setFtemplateId(messageUserRecord.getFtemplateId());
                messageSign.setFsubjectType(1);
                messageSign.setFcreateTime(new Date());
                messageSign.setFmodifyTime(new Date());
                capacity.add(messageSign);
            });
            Result<Integer> apiBatch = messageSignApi.createBatch(capacity);
            if (!apiBatch.isSuccess()) {
                throw new BizException(MallExceptionCode.SYSTEM_ERROR);
            }
        }
        return Result.success();
    }

    /**
     * @param userId
     * @return
     * @see MessageService#countMessageForUnRead(Long)
     */
    @Override
    public Result<Integer> countMessageForUnRead(Long userId) {
        Criteria<MessageUserRecord, Object> userRecordObjectCriteria = Criteria.of(MessageUserRecord.class)
                .andEqualTo(MessageUserRecord::getFsendStatus, 2)
                .andEqualTo(MessageUserRecord::getFreaded, 0)
                .andIn(MessageUserRecord::getFuid, Lists.newArrayList(userId, 0))
                .andGreaterThanOrEqualTo(MessageUserRecord::getFexpirationDate, new Date())
                .sortDesc(MessageUserRecord::getFcreateTime);
        Result<List<MessageUserRecord>> userRecordResult = userRecordApi.queryByCriteria(userRecordObjectCriteria);
        if (!userRecordResult.isSuccess()) {
            throw new BizException(MallExceptionCode.SYSTEM_ERROR);
        }
        int messageCount = 0;
        if ((messageCount = userRecordResult.getData().size()) == 0) {
            return Result.success(messageCount);
        }
        Map<Integer, List<MessageUserRecord>> map = userRecordResult.getData().stream().collect(Collectors.groupingBy(MessageUserRecord::getFisCommon));
        // 全局消息
        List<MessageUserRecord> records = map.get(1);
        if (CollectionUtils.isEmpty(records)) {
            return Result.success(messageCount);
        }
        List<Long> recordIds = records.stream().map(MessageUserRecord::getFmessageUserRecordId).collect(Collectors.toList());
        Result<List<MessageSign>> readResult = messageSignApi.queryByCriteria(Criteria.of(MessageSign.class)
                .andIn(MessageSign::getFrecordId, recordIds)
                .andEqualTo(MessageSign::getFsubjectType, 1)
                .andEqualTo(MessageSign::getFsubjectId, userId));
        if (!readResult.isSuccess()) {
            throw new BizException(MallExceptionCode.SYSTEM_ERROR);
        }
        // 全都未读
        List<MessageSign> signs = readResult.getData();
        if (CollectionUtils.isEmpty(signs)) {
            return Result.success(messageCount);
        }
        // 已读ID
        Set<Long> signSets = signs.stream().map(MessageSign::getFrecordId).collect(Collectors.toSet());
        return Result.success(messageCount - signSets.size());
    }
}
