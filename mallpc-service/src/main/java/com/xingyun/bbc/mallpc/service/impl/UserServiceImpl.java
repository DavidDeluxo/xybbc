package com.xingyun.bbc.mallpc.service.impl;

import cn.hutool.http.HttpUtil;
import com.xingyun.bbc.common.jwt.XyUserJwtManager;
import com.xingyun.bbc.common.redis.XyRedisManager;
import com.xingyun.bbc.core.activity.api.CouponProviderApi;
import com.xingyun.bbc.core.activity.enums.CouponScene;
import com.xingyun.bbc.core.activity.model.dto.CouponReleaseDto;
import com.xingyun.bbc.core.helper.api.SMSApi;
import com.xingyun.bbc.core.market.api.CouponApi;
import com.xingyun.bbc.core.market.api.CouponReceiveApi;
import com.xingyun.bbc.core.market.enums.CouponReceiveStatusEnum;
import com.xingyun.bbc.core.market.enums.CouponReleaseTypeEnum;
import com.xingyun.bbc.core.market.enums.CouponStatusEnum;
import com.xingyun.bbc.core.market.enums.CouponTypeEnum;
import com.xingyun.bbc.core.market.po.Coupon;
import com.xingyun.bbc.core.market.po.CouponReceive;
import com.xingyun.bbc.core.operate.api.GuidePageApi;
import com.xingyun.bbc.core.operate.api.MarketUserApi;
import com.xingyun.bbc.core.operate.api.MarketUserStatisticsApi;
import com.xingyun.bbc.core.operate.enums.GuideConfigType;
import com.xingyun.bbc.core.operate.enums.GuidePageType;
import com.xingyun.bbc.core.operate.po.GuidePage;
import com.xingyun.bbc.core.operate.po.MarketUser;
import com.xingyun.bbc.core.operate.po.MarketUserStatistics;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.user.api.UserAccountApi;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.core.user.po.UserAccount;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.core.utils.StringUtil;
import com.xingyun.bbc.mallpc.common.components.DozerHolder;
import com.xingyun.bbc.mallpc.common.constants.MallPcRedisConstant;
import com.xingyun.bbc.mallpc.common.constants.UserConstants;
import com.xingyun.bbc.mallpc.common.ensure.Ensure;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.common.utils.*;
import com.xingyun.bbc.mallpc.model.dto.user.ResetPasswordDto;
import com.xingyun.bbc.mallpc.model.dto.user.SendSmsCodeDto;
import com.xingyun.bbc.mallpc.model.dto.user.UserLoginDto;
import com.xingyun.bbc.mallpc.model.dto.user.UserRegisterDto;
import com.xingyun.bbc.mallpc.model.vo.user.SendSmsCodeVo;
import com.xingyun.bbc.mallpc.model.vo.user.UserLoginVo;
import com.xingyun.bbc.mallpc.model.vo.user.UserRegisterCouponVo;
import com.xingyun.bbc.mallpc.service.UserService;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * @author nick
 * @version 1.0.0
 * @date 2019-11-19
 * @copyright 本内容仅限于深圳市天行云供应链有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Service
public class UserServiceImpl implements UserService {

    /**
     * 自动登录的cookie name
     */
    private static final String AUTO_LOGIN_COOKIE = "auto_login";

    @Resource
    private DozerHolder convertor;

    @Resource
    private XyUserJwtManager xyUserJwtManager;

    @Resource
    private XyRedisManager redisManager;

    @Resource
    private UserApi userApi;

    @Resource
    private SMSApi smsApi;

    @Resource
    private CouponApi couponApi;

    @Resource
    private UserAccountApi userAccountApi;

    @Resource
    private CouponProviderApi couponProviderApi;

    @Resource
    private GuidePageApi guidePageApi;

    @Resource
    private MarketUserApi marketUserApi;

    @Resource
    private MarketUserStatisticsApi marketUserStatisticsApi;

    @Resource
    private CouponReceiveApi couponReceiveApi;

    /**
     * @author nick
     * @date 2019-11-19
     * @description :  用户登录
     * @version 1.0.0
     */
    @Override
    public Result<UserLoginVo> userLogin(UserLoginDto userLoginDto) {
        //解密
        // String passWord = EncryptUtils.aesDecrypt(userLoginDto.getPassword());
        String passWord = userLoginDto.getPassword();
        passWord = MD5Util.toMd5(passWord);
        Result<User> userResult = userApi.queryOneByCriteria(Criteria.of(User.class)
                .andEqualTo(User::getFisDelete, "0")
                .andEqualTo(User::getFpasswd, passWord)
                .andLeft().orLike(User::getFmobile, userLoginDto.getUserAccount())
                .orLike(User::getFmail, userLoginDto.getUserAccount())
                .orLike(User::getFuname, userLoginDto.getUserAccount()).addRight());
        Ensure.that(userResult).isNotNullData(MallPcExceptionCode.LOGIN_FAILED);
        Ensure.that(userResult.getData().getFfreezeStatus()).isEqual(1, MallPcExceptionCode.ACCOUNT_FREEZE);
        UserLoginVo userLoginVo = createToken(userResult.getData());
        //更新最近登录时间
        User user = new User();
        user.setFuid(userLoginVo.getFuid());
        user.setFlastloginTime(new Date());
        userApi.updateNotNull(user);
        return Result.success(userLoginVo);
    }

    private UserLoginVo createToken(User user) {
        UserLoginVo userLoginVo = convertor.convert(user, UserLoginVo.class);
        long expire = UserConstants.Token.TOKEN_AUTO_LOGIN_EXPIRATION;
        String token = xyUserJwtManager.createJwt(user.getFuid().toString(), user.getFmobile(), expire);
        userLoginVo.setExpire(expire);
        userLoginVo.setToken(token);
        if (StringUtils.isBlank(userLoginVo.getFnickname())) {
            userLoginVo.setFunameIsModify(1);
        } else {
            userLoginVo.setFunameIsModify(0);

        }
        if (StringUtils.isBlank(user.getFwithdrawPasswd())) {
            userLoginVo.setFwithdrawPasswdStatus(0);
        } else {
            userLoginVo.setFwithdrawPasswdStatus(1);
        }
        return userLoginVo;
    }

    /**
     * @author nick
     * @date 2019-11-19
     * @description :  注册
     * @version 1.0.0
     */
    @Override
    @GlobalTransactional
    public Result<UserLoginVo> register(UserRegisterDto userRegisterDto) {
        MarketUser marketUser = null;
        String fmobile = userRegisterDto.getFmobile();
        // 判断验证码合法性
        Ensure.that(checkVerifyCode(fmobile, userRegisterDto.getVerifyCode())).isTrue(MallPcExceptionCode.SMS_AUTH_NUM_ERROR);
        // 判断手机号是否注册
        Ensure.that(Objects.isNull(findUserByMobile(fmobile))).isTrue(MallPcExceptionCode.REGISTER_MOBILE_EXIST);
        //String passWord = EncryptUtils.aesDecrypt(userRegisterDto.getPassword());
        String passWord = userRegisterDto.getPassword();
        Ensure.that(StringUtils.isNotBlank(passWord)).isTrue(MallPcExceptionCode.PASSWORD_CAN_NOT_BE_NULL);
        // 校验密码长度
        Ensure.that(passWord.length()).isGt(5, MallPcExceptionCode.PASSWORD_ILLEGAL).isLt(33, MallPcExceptionCode.PASSWORD_ILLEGAL);
        // 验证推广码
        if (StringUtils.isNotBlank(userRegisterDto.getFinviter())) {
            Result<MarketUser> marketUserResult = marketUserApi.queryOneByCriteria(Criteria.of(MarketUser.class)
                    .fields(MarketUser::getFuid, MarketUser::getFextensionCode)
                    .andEqualTo(MarketUser::getFextensionCode, userRegisterDto.getFinviter()));
            Ensure.that(marketUserResult.isSuccess()).isTrue(MallPcExceptionCode.SYSTEM_ERROR);
            marketUser = marketUserResult.getData();
            Ensure.that(Objects.nonNull(marketUser)).isTrue(MallPcExceptionCode.EXTENSION_CODE_NOT_EXIST);
        }
        passWord = MD5Util.toMd5(passWord);
        User user = new User();
        user.setFregisterFrom("web");
        user.setFmobile(fmobile);
        user.setFuname(fmobile);
        user.setFpasswd(passWord);
        user.setFfreezeStatus(1);
        user.setFverifyStatus(1);
        Date date = new Date();
        user.setFlastloginTime(date);
        user.setFmobileValidTime(date);
        Result<User> userResult = userApi.saveAndReturn(user);
        Long fuid = userResult.getData().getFuid();
        Ensure.that(userResult).isSuccess(MallPcExceptionCode.SYSTEM_ERROR);
        UserAccount userAccount = new UserAccount();
        userAccount.setFuid(fuid);
        Ensure.that(userAccountApi.create(userAccount)).isSuccess(MallPcExceptionCode.SYSTEM_ERROR);
        UserLoginVo userLoginVo = createToken(userResult.getData());
        // 如果使用了推广码注册成功保存推广信息
        if (Objects.nonNull(marketUser)) {
            // 保存统计表
            MarketUserStatistics marketUserStatistics = new MarketUserStatistics();
            marketUserStatistics.setFextensionCode(marketUser.getFextensionCode());
            marketUserStatistics.setFuid(marketUser.getFuid());
            marketUserStatistics.setFinvitorUid(user.getFuid());
            Ensure.that(marketUserStatisticsApi.create(marketUserStatistics).isSuccess()).isTrue(MallPcExceptionCode.SYSTEM_ERROR);
        }
        // 注册成功系统赠送优惠券
        receiveCoupon(fuid);
        return Result.success(userLoginVo);
    }

    private boolean checkVerifyCode(String fmobile, String verifyCode) {
        String key = StringUtils.join(MallPcRedisConstant.VERIFY_CODE_PREFIX, fmobile);
        if (Objects.isNull(redisManager.get(key))) {
            return false;
        }
        if (Objects.equals(String.valueOf(redisManager.get(key)), verifyCode)) {
            return true;
        }
        return false;
    }

    /**
     * @author nick
     * @date 2019-11-19
     * @description :  发送验证码
     * @version 1.0.0
     */
    @Override
    public Result<SendSmsCodeVo> sendSmsCode(SendSmsCodeDto sendSmsCodeDto) {
        SendSmsCodeVo sendSmsCodeVo = new SendSmsCodeVo();
        Integer sourceType = sendSmsCodeDto.getSourceType();
        String fmobile = sendSmsCodeDto.getFmobile();
        Ensure.that(StringUtilExtention.mobileCheck(fmobile)).isTrue(MallPcExceptionCode.BIND_MOBILE_ERROR);
        User user = findUserByMobile(fmobile);
        switch (sourceType) {
            case 0:
                // 注册
                Ensure.that(Objects.isNull(user)).isTrue(MallPcExceptionCode.REGISTER_MOBILE_EXIST);
                break;
            default:
                // 找回密码
                Ensure.that(Objects.nonNull(user)).isTrue(MallPcExceptionCode.ACCOUNT_NOT_EXIST);
                break;
        }
        // 校验发送间隔
        Ensure.that(Objects.isNull(redisManager.get(fmobile))).isTrue(MallPcExceptionCode.SMS_AUTH_IS_SEND);
        // 校验同一个ip发送次数
        String ipAddress = HttpUtil.getClientIP(RequestHolder.getRequest());
        if (Objects.nonNull(redisManager.get(ipAddress))) {
            Integer count = (Integer) redisManager.get(ipAddress);
            Ensure.that(count).isLt(UserConstants.Sms.MAX_IP_SMS_SEND_TIME, MallPcExceptionCode.USER_SEND_SMS_FAILD);
            if (count > UserConstants.Sms.CAPTCHA_THRESHOLD) {
                // 触发滑动验证
                sendSmsCodeVo.setIsCheck(1);
            }
        }
        // 发送验证码
        sendSms(ipAddress, fmobile);
        return Result.success(sendSmsCodeVo);
    }

    private void sendSms(String ipAddress, String fmobile) {
        // 发送验证码
        String verifyCode = RandomUtils.randomString(4);
        String content = StringUtils.join("您的验证码是：", verifyCode, "，请勿泄露）。若非本人操作，请忽略本短信", "【行云全球汇】");
        smsApi.sendSms(fmobile, content);
        // 令牌放进redis
        redisManager.set(StringUtils.join(MallPcRedisConstant.VERIFY_CODE_PREFIX, fmobile), verifyCode, UserConstants.Sms.MOBILE_AUTH_CODE_EXPIRE_TIME);
        // 设置一分钟间隔
        redisManager.set(fmobile, MallPcRedisConstant.DEFAULT_LOCK_VALUE, UserConstants.Sms.MOBILE_SEND_SMS_TIME);
        // 设置ip次数上限
        redisManager.incr(ipAddress);
        //获取当天剩余时间秒
        long secondsLeftToday = 86400 - DateUtils.getFragmentInSeconds(Calendar.getInstance(), Calendar.DATE);
        redisManager.expire(ipAddress, secondsLeftToday);
    }

    public User findUserByMobile(String mobile) {
        Result<User> userResult = userApi.queryOneByCriteria(Criteria.of(User.class)
                .andEqualTo(User::getFisDelete, "0")
                .andEqualTo(User::getFmobile, mobile));
        Ensure.that(userResult.isSuccess()).isTrue(MallPcExceptionCode.SYSTEM_ERROR);
        return userResult.getData();
    }


    /**
     * @author nick
     * @date 2019-11-19
     * @description :  发放优惠券
     * @version 1.0.0
     */
    private void receiveCoupon(Long fuid) {
        Result<List<Coupon>> couponResult = couponApi.queryByCriteria(Criteria.of(Coupon.class)
                .fields(Coupon::getFcouponId)
                .andEqualTo(Coupon::getFcouponStatus, CouponStatusEnum.PUSHED.getCode())
                .andEqualTo(Coupon::getFreleaseType, CouponReleaseTypeEnum.REGISTER.getCode())
                .andGreaterThan(Coupon::getFsurplusReleaseQty, 0));
        Ensure.that(couponResult.isSuccess()).isTrue(MallPcExceptionCode.SYSTEM_ERROR);
        couponResult.getData().stream().forEach(coupon -> couponProviderApi.receive(new CouponReleaseDto()
                .setCouponId(coupon.getFcouponId())
                .setCouponScene(CouponScene.REGISTER)
                .setUserId(fuid)));
    }

    /**
     * @author nick
     * @date 2019-11-19
     * @description :  查询新人注册优惠券
     * @version 1.0.0
     */
    @Override
    public Result<List<UserRegisterCouponVo>> queryRegisterCoupon() {
        Long uid = Long.parseLong(RequestHolder.getRequest().getHeader("xyid"));
        Result<List<CouponReceive>> couponReceiveResult = couponReceiveApi.queryByCriteria(Criteria.of(CouponReceive.class)
                .andEqualTo(CouponReceive::getFuserCouponStatus, CouponReceiveStatusEnum.NOT_USED.getCode())
                .andEqualTo(CouponReceive::getFuid, uid));
        Ensure.that(couponReceiveResult.isSuccess()).isTrue(MallPcExceptionCode.SYSTEM_ERROR);
        List<CouponReceive> couponReceiveList = couponReceiveResult.getData();
        if (CollectionUtils.isEmpty(couponReceiveList)) {
            return Result.success(Lists.emptyList());
        }
        List<Long> couponIds = couponReceiveList.stream().map(CouponReceive::getFcouponId).collect(toList());
        Result<List<Coupon>> couponResult = couponApi.queryByCriteria(Criteria.of(Coupon.class)
                .fields(Coupon::getFcouponId,Coupon::getFcouponName, Coupon::getFcouponType, Coupon::getFthresholdAmount, Coupon::getFdeductionValue)
                .andIn(Coupon::getFcouponId, couponIds));
        Ensure.that(couponResult).isNotEmptyData(MallPcExceptionCode.COUPON_NOT_EXIST);
        Map<Long, Coupon> couponMap = couponResult.getData().stream().collect(toMap(Coupon::getFcouponId, Function.identity()));
        List<UserRegisterCouponVo> voList = couponReceiveList.stream().map(couponReceive -> {
            UserRegisterCouponVo vo = convertor.convert(couponReceive, UserRegisterCouponVo.class);
            Coupon coupon = couponMap.get(couponReceive.getFcouponId());
            Ensure.that(Objects.nonNull(coupon)).isTrue(MallPcExceptionCode.SYSTEM_ERROR);
            vo.setFcouponName(coupon.getFcouponName());
            vo.setFcouponType(coupon.getFcouponType());
            vo.setThresholdAmount(AccountUtil.divideOneHundred(coupon.getFthresholdAmount()));
            if (Objects.equals(vo.getFcouponType(), CouponTypeEnum.FULL_REDUCTION.getCode())) {
                vo.setDeductionValue(AccountUtil.divideOneHundred(coupon.getFdeductionValue()));
            } else if (Objects.equals(vo.getFcouponType(), CouponTypeEnum.DISCOUNT.getCode())) {
                vo.setDeductionValue(AccountUtil.divideOneHundred(coupon.getFdeductionValue() * 10));
            }
            return vo;
        }).collect(toList());
        return Result.success(voList);
    }

    /**
     * @author nick
     * @date 2019-11-19
     * @description :  重置密码
     * @version 1.0.0
     */
    @Override
    public Result resetPwd(ResetPasswordDto resetPasswordDto) {
        String fmobile = resetPasswordDto.getFmobile();
        // 手机号格式校验
        Ensure.that(StringUtilExtention.mobileCheck(fmobile)).isTrue(MallPcExceptionCode.BIND_MOBILE_ERROR);
        String newPassword = resetPasswordDto.getNewPassword();
        // 校验新密码长度
        Ensure.that(newPassword.length()).isGt(5, MallPcExceptionCode.PASSWORD_ILLEGAL).isLt(33, MallPcExceptionCode.PASSWORD_ILLEGAL);
        newPassword = MD5Util.toMd5(newPassword);
        String verifyCode = resetPasswordDto.getVerifyCode();
        // 校验验证码
        Ensure.that(checkVerifyCode(fmobile, verifyCode)).isTrue(MallPcExceptionCode.SMS_AUTH_NUM_ERROR);
        // 查询账户是否存在
        Result<User> userResult = userApi.queryOneByCriteria(Criteria.of(User.class)
                .fields(User::getFuid, User::getFpasswd).andEqualTo(User::getFmobile, fmobile));
        Ensure.that(userResult).isNotNull(MallPcExceptionCode.BIND_MOBILE_ERROR);
        // 查询密码是否相同
        User user = userResult.getData();
        String fpasswd = user.getFpasswd();
        Ensure.that(Objects.equals(newPassword, fpasswd)).isFalse(MallPcExceptionCode.PASSWORD_NOT_CHANGE);
        // 修改密码
        user.setFpasswd(newPassword);
        Ensure.that(userApi.updateNotNull(user).isSuccess()).isTrue(MallPcExceptionCode.SYSTEM_ERROR);
        return Result.success();
    }

    @Override
    public Result<String> guideLogin() {
        Result<GuidePage> guidePageResult = guidePageApi.queryOneByCriteria(Criteria.of(GuidePage.class)
                .andEqualTo(GuidePage::getFguideType, GuideConfigType.PC_CONFIG.getCode())
                .andEqualTo(GuidePage::getFisDelete, 0)
                .andEqualTo(GuidePage::getFtype, GuidePageType.LOGIN_PAGE.getCode())
                .fields(GuidePage::getFimgUrl, GuidePage::getFguideId)
        );
        Ensure.that(guidePageResult).isNotNull(MallPcExceptionCode.SYSTEM_ERROR);
        String fimgUrl = guidePageResult.getData().getFimgUrl();
        return Result.success(StringUtil.isNotBlank(fimgUrl) ? fimgUrl : "");
    }
}