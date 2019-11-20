package com.xingyun.bbc.mallpc.service.impl;

import cn.hutool.http.HttpUtil;
import com.xingyun.bbc.common.jwt.XyUserJwtManager;
import com.xingyun.bbc.common.redis.XyRedisManager;
import com.xingyun.bbc.core.activity.api.CouponProviderApi;
import com.xingyun.bbc.core.activity.enums.CouponScene;
import com.xingyun.bbc.core.activity.model.dto.CouponReleaseDto;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.helper.api.SMSApi;
import com.xingyun.bbc.core.market.api.CouponApi;
import com.xingyun.bbc.core.market.api.CouponReceiveApi;
import com.xingyun.bbc.core.market.enums.CouponReceiveStatusEnum;
import com.xingyun.bbc.core.market.enums.CouponReleaseTypeEnum;
import com.xingyun.bbc.core.market.enums.CouponStatusEnum;
import com.xingyun.bbc.core.market.enums.CouponTypeEnum;
import com.xingyun.bbc.core.market.po.Coupon;
import com.xingyun.bbc.core.market.po.CouponReceive;
import com.xingyun.bbc.core.operate.api.MarketUserApi;
import com.xingyun.bbc.core.operate.api.MarketUserStatisticsApi;
import com.xingyun.bbc.core.operate.po.MarketUser;
import com.xingyun.bbc.core.operate.po.MarketUserStatistics;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.user.api.UserAccountApi;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.core.user.po.UserAccount;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mallpc.common.components.DozerHolder;
import com.xingyun.bbc.mallpc.common.constants.MallPcRedisConstant;
import com.xingyun.bbc.mallpc.common.constants.UserConstants;
import com.xingyun.bbc.mallpc.common.ensure.Ensure;
import com.xingyun.bbc.mallpc.common.exception.MallPcExceptionCode;
import com.xingyun.bbc.mallpc.common.utils.*;
import com.xingyun.bbc.mallpc.model.dto.user.SendSmsCodeDto;
import com.xingyun.bbc.mallpc.model.dto.user.UserLoginDto;
import com.xingyun.bbc.mallpc.model.dto.user.UserRegisterDto;
import com.xingyun.bbc.mallpc.model.vo.user.SendSmsCodeVo;
import com.xingyun.bbc.mallpc.model.vo.user.UserLoginVo;
import com.xingyun.bbc.mallpc.model.vo.user.UserRegisterCouponVo;
import com.xingyun.bbc.mallpc.service.UserService;
import io.jsonwebtoken.Claims;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

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
        String passWord = EncryptUtils.aesDecrypt(userLoginDto.getPassword());
        Ensure.that(StringUtils.isNotBlank(passWord)).isTrue(MallPcExceptionCode.PASSWORD_CAN_NOT_BE_NULL);
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
        if (Objects.equals(userLoginDto.getIsAutoLogin(), 1)) {
            //勾选自动登录将token添加进cookie
            Cookie cookie = new Cookie(AUTO_LOGIN_COOKIE, userLoginVo.getToken());
            cookie.setMaxAge(UserConstants.Cookie.COOKIE_EXPIRE_TIME);
            RequestHolder.getResponse().addCookie(cookie);
        }
        return Result.success(userLoginVo);
    }

    private UserLoginVo createToken(User user) {
        UserLoginVo userLoginVo = convertor.convert(user, UserLoginVo.class);
        long expire = UserConstants.Token.TOKEN_AUTO_LOGIN_EXPIRATION;
        String token = xyUserJwtManager.createJwt(user.getFuid().toString(), user.getFmobile(), expire);
        userLoginVo.setExpire(expire);
        userLoginVo.setToken(token);
        if (userLoginVo.getFnickname().equals("")) {
            userLoginVo.setFunameIsModify(1);
        } else {
            userLoginVo.setFunameIsModify(0);

        }
        if (user.getFwithdrawPasswd().equals("")) {
            userLoginVo.setFwithdrawPasswdStatus(0);
        } else {
            userLoginVo.setFwithdrawPasswdStatus(1);
        }
        return userLoginVo;
    }

    /**
     * @author nick
     * @date 2019-11-19
     * @description :  自动登录
     * @version 1.0.0
     */
    @Override
    public Result<UserLoginVo> autoLogin() {
        String token = Arrays.stream(RequestHolder.getRequest().getCookies())
                .filter(cookie -> cookie.getName().equals(AUTO_LOGIN_COOKIE))
                .findFirst().orElseThrow(() -> new BizException(MallPcExceptionCode.AUTO_LOGIN_FAILED))
                .getValue();
        Claims claims = xyUserJwtManager.parseJwt(token);
        Ensure.that(Objects.nonNull(claims)).isTrue(MallPcExceptionCode.AUTO_LOGIN_FAILED);
        String fmobile = claims.getSubject();
        User user = findUserByMobile(fmobile);
        Ensure.that(user).isNotNull(MallPcExceptionCode.AUTO_LOGIN_FAILED);
        Ensure.that(user.getFfreezeStatus()).isEqual(1, MallPcExceptionCode.ACCOUNT_FREEZE);
        UserLoginVo userLoginVo = createToken(user);
        userLoginVo.setToken(token);
        //更新最近登录时间
        user.setFlastloginTime(new Date());
        userApi.updateNotNull(user);
        return Result.success(userLoginVo);
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
        String passWord = EncryptUtils.aesDecrypt(userRegisterDto.getPassword());
        Ensure.that(StringUtils.isNotBlank(passWord)).isTrue(MallPcExceptionCode.PASSWORD_CAN_NOT_BE_NULL);
        // 校验密码长度
        Ensure.that(passWord.length()).isGt(6, MallPcExceptionCode.PASSWORD_CAN_NOT_BE_NULL).isLt(32, MallPcExceptionCode.PASSWORD_CAN_NOT_BE_NULL);
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
        Ensure.that(userResult).isSuccess(MallPcExceptionCode.SYSTEM_ERROR);
        UserAccount userAccount = new UserAccount();
        userAccount.setFuid(userResult.getData().getFuid());
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
        receiveCoupon(user.getFuid());
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
                Ensure.that(Objects.nonNull(user)).isTrue(MallPcExceptionCode.REGISTER_MOBILE_EXIST);
                break;
        }
        // 校验发送间隔
        Ensure.that(Objects.isNull(redisManager.get(fmobile))).isTrue(MallPcExceptionCode.SMS_AUTH_IS_SEND);
        // 校验同一个ip发送次数
        String ipAddress = HttpUtil.getClientIP(RequestHolder.getRequest());
        if (Objects.nonNull(redisManager.get(ipAddress))) {
            Integer count = Integer.valueOf((String) redisManager.get(ipAddress));
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
        redisManager.set(fmobile, UserConstants.Sms.MOBILE_SEND_SMS_TIME);
        // 设置ip次数上限
        redisManager.incr(ipAddress);
        //获取当天剩余时间
        long secondsLeftToday = 86400 - DateUtils.getFragmentInSeconds(Calendar.getInstance(), Calendar.DATE);
        redisManager.expire(ipAddress, secondsLeftToday);
    }

    /**
     * @author nick
     * @date 2019-11-19
     * @description :  重置密码
     * @version 1.0.0
     */
    @Override
    public Result resetPwd(SendSmsCodeDto sendSmsCodeDto) {
        return null;
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
    public Result<List<UserRegisterCouponVo>> queryRegisterCoupon(Long uid) {
        Result<List<CouponReceive>> couponReceiveResult = couponReceiveApi.queryByCriteria(Criteria.of(CouponReceive.class)
                .andEqualTo(CouponReceive::getFuserCouponStatus, CouponReceiveStatusEnum.NOT_USED.getCode())
                .andEqualTo(CouponReceive::getFuid, uid));
        Ensure.that(couponReceiveResult.isSuccess()).isTrue(MallPcExceptionCode.SYSTEM_ERROR);
        List<CouponReceive> couponReceiveList = couponReceiveResult.getData();
        if (CollectionUtils.isEmpty(couponReceiveList)) {
            return Result.success();
        }
        List<Long> couponIds = couponReceiveList.stream().map(CouponReceive::getFcouponId).collect(toList());
        Result<List<Coupon>> couponResult = couponApi.queryByCriteria(Criteria.of(Coupon.class)
                .fields(Coupon::getFcouponName, Coupon::getFcouponType, Coupon::getFthresholdAmount, Coupon::getFdeductionValue)
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
}
