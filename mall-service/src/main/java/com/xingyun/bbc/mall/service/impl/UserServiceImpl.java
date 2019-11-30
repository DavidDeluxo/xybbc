package com.xingyun.bbc.mall.service.impl;

import com.google.common.base.Strings;
import com.xingyun.bbc.activity.api.CouponProviderApi;
import com.xingyun.bbc.activity.enums.CouponScene;
import com.xingyun.bbc.activity.model.dto.CouponQueryDto;
import com.xingyun.bbc.activity.model.dto.CouponReleaseDto;
import com.xingyun.bbc.activity.model.vo.CouponQueryVo;
import com.xingyun.bbc.common.jwt.XyUserJwtManager;
import com.xingyun.bbc.common.redis.XyRedisManager;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.helper.api.EmailApi;
import com.xingyun.bbc.core.helper.api.SMSApi;
import com.xingyun.bbc.core.market.api.CouponApi;
import com.xingyun.bbc.core.market.api.CouponBindUserApi;
import com.xingyun.bbc.core.market.api.CouponReceiveApi;
import com.xingyun.bbc.core.market.po.Coupon;
import com.xingyun.bbc.core.market.po.CouponBindUser;
import com.xingyun.bbc.core.market.po.CouponReceive;
import com.xingyun.bbc.core.operate.api.CityRegionApi;
import com.xingyun.bbc.core.operate.po.CityRegion;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.user.api.UserAccountApi;
import com.xingyun.bbc.core.user.api.UserApi;
import com.xingyun.bbc.core.user.api.UserLoginInformationApi;
import com.xingyun.bbc.core.user.api.UserVerifyApi;
import com.xingyun.bbc.core.user.enums.UserVerifyEnums;
import com.xingyun.bbc.core.user.po.User;
import com.xingyun.bbc.core.user.po.UserAccount;
import com.xingyun.bbc.core.user.po.UserLoginInformation;
import com.xingyun.bbc.core.user.po.UserVerify;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.enums.*;
import com.xingyun.bbc.mall.base.utils.DozerHolder;
import com.xingyun.bbc.mall.base.utils.EncryptUtils;
import com.xingyun.bbc.mall.base.utils.Md5Utils;
import com.xingyun.bbc.mall.common.RedisHolder;
import com.xingyun.bbc.mall.common.constans.MallRedisConstant;
import com.xingyun.bbc.mall.common.constans.UserConstants;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import com.xingyun.bbc.mall.common.lock.XybbcLock;
import com.xingyun.bbc.mall.model.dto.*;
import com.xingyun.bbc.mall.model.vo.*;
import com.xingyun.bbc.mall.service.UserService;
import io.jsonwebtoken.Claims;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.xingyun.bbc.mall.common.constans.MallRedisConstant.USER_COUNT;
import static com.xingyun.bbc.mall.common.constans.MallRedisConstant.USER_COUNT_LOCK;

/**
 * @author ZSY
 * @Title:
 * @Description:
 * @date 2019-09-03 11:00
 */
@Service
public class UserServiceImpl implements UserService {
    private static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    /**
     * 分布式锁前缀和过期时间
     */
    private static final String LOCK_PREFIX_MOBILE = "user_add_unique_mobile_";
    private static final String LOCK_PREFIX_EMAIL = "user_add_unique_email_";
    private static final String LOCK_PREFIX_UNAME = "user_add_unique_uname_";
    private static final long LOCK_EXPIRING = 60;

    @Autowired
    private UserApi userApi;
    @Autowired
    private UserAccountApi userAccountApi;
    @Autowired
    private UserVerifyApi userVerifyApi;
    @Autowired
    private CityRegionApi cityRegionApi;
    @Autowired
    private CouponApi couponApi;
    @Autowired
    private CouponProviderApi couponProviderApi;
    @Autowired
    private CouponReceiveApi couponReceiveApi;
    @Autowired
    private CouponBindUserApi couponBindUserApi;
    @Autowired
    private SMSApi smsApi;
    @Autowired
    private EmailApi emailApi;
    @Autowired
    private XyUserJwtManager xyUserJwtManager;
    @Autowired
    private XyRedisManager xyRedisManager;
    @Autowired
    private DozerHolder dozerHolder;
    @Autowired
    private XybbcLock xybbcLock;
    @Autowired
    private RedisHolder redisHolder;

    @Autowired
    private UserLoginInformationApi userLoginInformationApi;

    @Override
    public Result<UserLoginVo> userLogin(UserLoginDto dto) {
        String passWord = EncryptUtils.aesDecrypt(dto.getPassword());
        if (passWord == null || passWord.equals("")) {
            return Result.failure(MallResultStatus.LOGIN_FAILURE);
        }
        passWord = Md5Utils.toMd5(passWord);
        Criteria<User, Object> criteria = Criteria.of(User.class);
        criteria.andEqualTo(User::getFisDelete, "0")
                .andEqualTo(User::getFpasswd, passWord)
                .andLeft().orEqualTo(User::getFmobile, dto.getUserAccount())
                .orEqualTo(User::getFmail, dto.getUserAccount())
                .orEqualTo(User::getFuname, dto.getUserAccount()).addRight();
        Result<User> userResult = userApi.queryOneByCriteria(criteria);
        if (userResult.getData() == null) {
            return Result.failure(MallResultStatus.LOGIN_FAILURE);
        }
        if (userResult.getData().getFfreezeStatus() != 1) {
            return Result.failure(MallResultStatus.ACCOUNT_FREEZE);
        }
        UserLoginVo userLoginVo = createToken(userResult.getData());
        //更新最近登录时间
        User user = new User();
        user.setFuid(userLoginVo.getFuid());
        user.setFlastloginTime(new Date());
        userApi.updateNotNull(user);
        //更新用户登录信息
        updateUserLoginInformation(dto,userResult.getData().getFuid());
        return Result.success(userLoginVo);
    }

    private void updateUserLoginInformation(UserLoginDto dto, Long fuid) {
        UserLoginInformation information = new UserLoginInformation();
        information.setFuid(fuid);
        information.setFloginMethod("手机端");
        information.setFloginSite("");
        information.setFipAdress(dto.getIpAddress());
        if(dto.getOsVersion() != null){
            information.setFoperatingSystem(dto.getOsVersion());
        }else{
            information.setFoperatingSystem("");
        }
        if(dto.getDeviceName() != null){
            information.setFunitType(dto.getDeviceName());
        }else{
            information.setFunitType("");
        }
        if(dto.getImei() != null){
            information.setFuniqueIdentificationCode(dto.getImei());
        }else{
            information.setFuniqueIdentificationCode("");
        }
        if(dto.getMac() != null){
            information.setFphysicalAddress(dto.getMac());
        }else{
            information.setFphysicalAddress("");
        }
        userLoginInformationApi.create(information);
    }

    //生成token信息
    private UserLoginVo createToken(User user) {
        UserLoginVo userLoginVo = new UserLoginVo();
        long expire = UserConstants.Token.TOKEN_AUTO_LOGIN_EXPIRATION;
        String token = xyUserJwtManager.createJwt(user.getFuid().toString(), user.getFmobile(), expire);
        userLoginVo.setExpire(expire);
        userLoginVo.setToken(token);
        userLoginVo.setFuid(user.getFuid());
        userLoginVo.setFfreezeStatus(user.getFfreezeStatus());
        userLoginVo.setFheadpic(user.getFheadpic());
        userLoginVo.setFnickname(user.getFnickname());
        userLoginVo.setFoperateType(user.getFoperateType());
        userLoginVo.setFuname(user.getFuname());
        userLoginVo.setFregisterFrom(user.getFregisterFrom());
        userLoginVo.setFverifyStatus(user.getFverifyStatus());
        userLoginVo.setFmobile(user.getFmobile());
        userLoginVo.setFmail(user.getFmail());
        //判断fnickname是否为空字符串，若为空字符串则表示用户还没修改过用户名，用户是否可修改：0否，1是
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

    @Override
    public Result<SendSmsVo> sendSmsAuthNum(UserLoginDto dto) {
        Integer isMobileCheck = 1;
        if (dto.getIsMobileCheck() != null) {
            isMobileCheck = dto.getIsMobileCheck();
        }
        SendSmsVo sendSmsVo = new SendSmsVo();
        String mobile = dto.getFmobile();
        //手机号校验
        boolean mobileCheck = mobileCheck(mobile);
        if (!mobileCheck) {
            return Result.failure(MallResultStatus.BIND_MOBILE_ERROR);
        }
        //手机号是否注册校验
        boolean CheckExist = CheckMobileExist(mobile);
        if (isMobileCheck == 1) {
            //注册校验返回
            if (!CheckExist) {
                return Result.failure(MallResultStatus.REGISTER_MOBILE_EXIST);
            }
        } else {
            //忘记密码账号校验
            if (CheckExist) {
                return Result.failure(MallResultStatus.ACCOUNT_NOT_EXIST);
            }
        }
        //手机号短信发送次数校验
        if (xyRedisManager.get(mobile) != null) {
            return Result.failure(MallResultStatus.SMS_AUTH_IS_SEND);
        }
        if (xyRedisManager.get(dto.getImei()) != null) {
            String IMEINum = String.valueOf(xyRedisManager.get(dto.getImei()));
            if (Integer.valueOf(IMEINum) < 10) {
                if (Integer.valueOf(IMEINum) > 5 && dto.getIsCheck() == 0) {
                    sendSmsVo.setIsCheck(0);
                    return Result.success(sendSmsVo);
                }
            } else {
                return Result.failure(MallResultStatus.USER_SEND_SMS_FAILD);
            }
        }
//        if(xyRedisManager.get(dto.getIpAddress()) != null){
//            String ipNum = String.valueOf(xyRedisManager.get(dto.getIpAddress()));
//            if(Integer.valueOf(ipNum) > 20){
//                return Result.failure(MallResultStatus.USER_SEND_SMS_FAILD);
//            }
//        }
        UserSecurityDto userSecurityDto = new UserSecurityDto();
        userSecurityDto.setFmobile(dto.getFmobile());
        userSecurityDto.setImei(dto.getImei());
        userSecurityDto.setIpAddress(dto.getIpAddress());
        sendSmsVo = sendSms(sendSmsVo, userSecurityDto);
        return Result.success(sendSmsVo);
    }

    private SendSmsVo sendSms(SendSmsVo sendSmsVo, UserSecurityDto dto) {
        String mobile = dto.getFmobile();
        String authNum = generateAuthNum(4);
        String SmsTemplate = "您的验证码是：" + authNum + "，请勿泄露）。若非本人操作，请忽略本短信";
        String signature = "【行云全球汇】";
        String content = signature + SmsTemplate;
        logger.info(content);
        Result<Boolean> result = smsApi.sendSms(mobile, content);
        if (!result.isSuccess()) {
            throw new BizException(MallResultStatus.SMS_SEND_FAILD);
        }
        //发送成功后增加发送次数记录
        sendSmsAddLimit(dto);
        String authNumKey = xyUserJwtManager.createJwt("", mobile, UserConstants.Sms.MOBILE_AUTH_CODE_EXPIRE_TIME);
        xyRedisManager.set(authNumKey, authNum, UserConstants.Sms.MOBILE_AUTH_CODE_EXPIRE_TIME / 1000);
        sendSmsVo.setAuthNumKey(authNumKey);
        return sendSmsVo;
    }


//    private boolean CheckSendSms(String mobile, String imei, String ipAddress) {
//        if(xyRedisManager.get(ipAddress) != null){
//            String ipNum = String.valueOf(xyRedisManager.get(ipAddress));
//            if(Integer.valueOf(ipNum) > 20){
//                return false;
//            }
//        }
//        return true;
//    }


    private boolean CheckMobileExist(String mobile) {
        //查询手机号是否已注册
        Criteria<User, Object> criteria = Criteria.of(User.class);
        criteria.andEqualTo(User::getFisDelete, "0")
                .andLeft()
                .orEqualTo(User::getFmobile, mobile)
                .orEqualTo(User::getFuname, mobile).addRight();
        Result<User> userResult = userApi.queryOneByCriteria(criteria);
        if (userResult.getData() != null) {
            return false;
        }
        return true;
    }

    private boolean mobileCheck(String mobile) {
        if (Strings.isNullOrEmpty(mobile)) {
            return false;
        }
        String regex = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$";
        if (mobile.length() != 11) {
            return false;
        } else {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(mobile);
            boolean isMatch = m.matches();
            if (!isMatch) {
                return false;
            }
        }
        return true;
    }

    private void sendSmsAddLimit(UserSecurityDto dto) {
        //获取当天剩余时间
        long secondsLeftToday = 86400 - DateUtils.getFragmentInSeconds(Calendar.getInstance(), Calendar.DATE);
        String mobile = dto.getFmobile();
        //发送短信后将手机号加入60S限制
        xyRedisManager.set(mobile, mobile, UserConstants.Sms.MOBILE_AUTH_CODE_EXPIRE_TIME / 1000);
        //设备当天短信发送次数增加
        if (xyRedisManager.get(dto.getImei()) != null) {
            String IMEINum = String.valueOf(xyRedisManager.get(dto.getImei()));
            xyRedisManager.set(dto.getImei(), Integer.valueOf(IMEINum) + 1, secondsLeftToday);
        } else {
            xyRedisManager.set(dto.getImei(), 1, secondsLeftToday);
        }
        //IP当天短信发送次数增加
//        if(xyRedisManager.get(dto.getIpAddress()) != null){
//            String ipNum = String.valueOf(xyRedisManager.get(dto.getIpAddress()));
//            xyRedisManager.set(dto.getIpAddress(),Integer.valueOf(ipNum)+1,secondsLeftToday);
//        }else{
//            xyRedisManager.set(dto.getIpAddress(),1,secondsLeftToday);
//        }

    }

    @Override
    public Result<Integer> checkPAuthNum(UserLoginDto dto) {
        if (dto.getAuthNum().length() != 4) {
            return Result.failure(MallResultStatus.SMS_AUTH_NUM_ERROR);
        }
        //校验短信验证码的key
        Claims keyClaims = xyUserJwtManager.parseJwt(dto.getAuthNumKey());
        if (keyClaims == null) {
            return Result.failure(MallResultStatus.SMS_AUTH_NUM_OUT_TIME);
        }
        //判断手机号和验证码是否匹配
        String key = keyClaims.getSubject();
        if (Strings.isNullOrEmpty(key) || !key.equals(dto.getFmobile())) {
            return Result.failure(MallResultStatus.SMS_AUTH_NUM_ERROR);
        }
        String authNum = String.valueOf(xyRedisManager.get(dto.getAuthNumKey()));
        if (Strings.isNullOrEmpty(authNum) || !dto.getAuthNum().equals(authNum)) {
            return Result.failure(MallResultStatus.SMS_AUTH_NUM_ERROR);
        }
        return Result.success();
    }

    @Override
    @GlobalTransactional
    public Result<UserLoginVo> registerUser(UserRegisterDto dto) {
        User user = new User();
        String mobile = dto.getFmobile();
        //手机号是否注册校验
        boolean CheckExist = CheckMobileExist(mobile);
        if (!CheckExist) {
            return Result.failure(MallResultStatus.REGISTER_MOBILE_EXIST);
        }
        String passWord = EncryptUtils.aesDecrypt(dto.getPassword());
        if (passWord == null || passWord.equals("")) {
            throw new BizException(MallResultStatus.LOGIN_FAILURE);
        }
        passWord = Md5Utils.toMd5(passWord);
        user.setFregisterFrom(dto.getFregisterFrom());
        user.setFmobile(dto.getFmobile());
        user.setFuname(dto.getFmobile());
        user.setFpasswd(passWord);
        user.setFfreezeStatus(1);
        user.setFverifyStatus(1);
        Date date = new Date();
        user.setFlastloginTime(date);
        user.setFmobileValidTime(date);
        Result<Integer> result = userApi.create(user);
        if (!result.isSuccess()) {
            throw new BizException((MallExceptionCode.SYSTEM_ERROR));
        }
        Criteria<User, Object> criteria = Criteria.of(User.class);
        criteria.andEqualTo(User::getFmobile, dto.getFmobile())
                .andEqualTo(User::getFpasswd, passWord)
                .andEqualTo(User::getFisDelete, "0");
        Result<User> userResult = userApi.queryOneByCriteria(criteria);
        if (userResult.getData() == null) {
            throw new BizException((MallExceptionCode.SYSTEM_ERROR));
        }
        UserAccount userAccount = new UserAccount();
        userAccount.setFuid(userResult.getData().getFuid());
        result = userAccountApi.create(userAccount);
        if (!result.isSuccess()) {
            throw new BizException((MallExceptionCode.SYSTEM_ERROR));
        }
        UserLoginVo userLoginVo = createToken(userResult.getData());
        if (userLoginVo == null) {
            throw new BizException((MallExceptionCode.SYSTEM_ERROR));
        }
        //领取注册优惠券和新人邀请券
        Integer couponNum = 0;
        couponNum = receiveCoupon(userLoginVo.getFuid());
        //当couponNum大于0时,前端提示"你获得+couponNum+张优惠券"
        userLoginVo.setCouponRegisterNum(couponNum);
        incrUserCount();
        return Result.success(userLoginVo);
    }

    /**
     * 更新用户注册总数
     */
    @Async("threadPoolTaskExecutor")
    void incrUserCount() {
        if (redisHolder.exists(USER_COUNT)) {
            redisHolder.incr(USER_COUNT);
        } else {
            xybbcLock.tryLock(USER_COUNT_LOCK, () -> {
                if (redisHolder.exists(USER_COUNT)) {
                    redisHolder.incr(USER_COUNT);
                    return;
                }
                User user = new User();
                Result<Integer> result = userApi.count(user);
                if (result.getData() != null && result.getData() > 0) {
                    redisHolder.setnx(USER_COUNT, result.getData());
                    redisHolder.incr(USER_COUNT);
                }
            });
        }
    }

    private Integer receiveCoupon(Long fuid) {
        Integer couponNum = 0;
        //查询可用注册优惠券
        Date date = new Date();
        Criteria<Coupon, Object> couponCriteria = Criteria.of(Coupon.class)
                .andEqualTo(Coupon::getFcouponStatus, 2)
                .andEqualTo(Coupon::getFreleaseType, 3)
                .andNotEqualTo(Coupon::getFsurplusReleaseQty, 0)
                .fields(Coupon::getFsurplusReleaseQty,Coupon::getFvalidityType,Coupon::getFvalidityEnd,Coupon::getFperLimit,Coupon::getFcouponId);
        Result<List<Coupon>> listResult = couponApi.queryByCriteria(couponCriteria);
        if (listResult.isSuccess()) {
            for (Coupon coupon : listResult.getData()) {
                if(coupon.getFsurplusReleaseQty().equals(0)){
                    continue;
                }
                if(coupon.getFvalidityType().equals(1)){
                    //判断是否在有效期内
                    if(date.compareTo(coupon.getFvalidityEnd()) > 0){
                        continue;
                    }
                }
                Integer fperLimit = coupon.getFperLimit();
                CouponReleaseDto couponDto = new CouponReleaseDto();
                couponDto.setCouponScene(CouponScene.REGISTER);
                couponDto.setCouponId(coupon.getFcouponId());
                couponDto.setUserId(fuid);
                //每人限领张数
                couponDto.setReceiveQty(fperLimit);
                couponProviderApi.receive(couponDto);
                couponNum = couponNum + fperLimit;
            }
        }
        return couponNum;
    }

    @Override
    public Result<Integer> forgotPwd(UserRegisterDto dto) {
        boolean check = checkPAuthNumber(dto.getAuthNum(), dto.getAuthNumKey(), dto.getFmobile());
        if (!check) {
            return Result.failure(MallResultStatus.SMS_AUTH_NUM_ERROR);
        }
        Criteria<User, Object> criteria = Criteria.of(User.class);
        criteria.andEqualTo(User::getFmobile, dto.getFmobile())
                .andEqualTo(User::getFisDelete, "0").fields(User::getFuid);
        Result<User> result = userApi.queryOneByCriteria(criteria);
        if (result.getData() == null) {
            return Result.failure(MallResultStatus.PWD_MIDIFY_FAILED);
        }
        User user = new User();
        user.setFmobile(dto.getFmobile());
        String passWord = EncryptUtils.aesDecrypt(dto.getPassword());
        if (passWord == null || passWord.equals("")) {
            throw new BizException(MallResultStatus.LOGIN_FAILURE);
        }
        passWord = Md5Utils.toMd5(passWord);
        user.setFpasswd(passWord);
        user.setFuid(result.getData().getFuid());
        return userApi.updateNotNull(user);
    }

    @Override
    @GlobalTransactional
    public Result<Integer> userVerify(UserVerifyDto dto) {
        //根据认证状态作参数校验
        Integer mistakeMsg = checkUserVerifyDto(dto);
        if (mistakeMsg != 0) {
            return Result.failure(mistakeMsg.toString(), UserVerifyResultStatus.getMessageByCode(mistakeMsg));
        }
        if (dto.getSalesVolume() != null) {
            dto.setFsalesVolume(new BigDecimal(dto.getSalesVolume()).multiply(new BigDecimal(100)).longValue());
        } else {
            dto.setFsalesVolume(BigDecimal.ZERO.longValue());
        }
        if (dto.getFpaltformId() != null) {
            dto.setFplatform(VerifyPlatform.getMessageByCode(Integer.valueOf(String.valueOf(dto.getFpaltformId()))));
        }
        if (dto.getFinterestItem() == null) {
            dto.setFinterestItem("");
        }
        if (dto.getFcategory() == null) {
            dto.setFcategory("");
        }
        //将微商名称取值改为fshopName
        if (dto.getFoperateType() == UserVerifyEnums.Type.WeiMerchantBuy.getValue()) {
            dto.setFshopName(dto.getFname());
        }

        // TODO: 2019/11/26 对营业执照号校验是否已被认证, 排重
        if (StringUtils.isNotBlank(dto.getFbusinessLicenseNo())) {
            Result<List<UserVerify>> listResult = userVerifyApi.queryByCriteria(Criteria.of(UserVerify.class).fields(UserVerify::getFuid).andEqualTo(UserVerify::getFbusinessLicenseNo, dto.getFbusinessLicenseNo()));
            logger.info("返回listResult: {}", listResult);
            if (!listResult.isSuccess()) throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
            List<UserVerify> userVerifies = listResult.getData();
            if (userVerifies.size() > 0) {
                List<Long> fuids = userVerifies.stream().map(UserVerify::getFuid).collect(Collectors.toList());
                Result<List<User>> resultUsers = userApi.queryByCriteria(Criteria.of(User.class).andIn(User::getFuid, fuids));
                logger.info("返回resultUsers: {}", resultUsers);
                if (!resultUsers.isSuccess()) throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
                List<User> users = resultUsers.getData();
                if (users.size() > 0) {
                    Optional<User> first = users.stream().filter(user -> user.getFverifyStatus().equals(3)).findFirst();
                    if (first.isPresent()) {
                        return Result.failure(UserVerifyResultStatus.BUSINESS_LICENSE_PIC_NO_CERTIFICATION.getCode().toString(),
                                UserVerifyResultStatus.BUSINESS_LICENSE_PIC_NO_CERTIFICATION.getMsg());
                    }
                }
            }
        }

        //查询是否已认证
        Criteria<UserVerify, Object> criteria = Criteria.of(UserVerify.class);
        criteria.andEqualTo(UserVerify::getFuid, dto.getFuid()).fields(UserVerify::getFuserVerifyId);
        Result<UserVerify> result = userVerifyApi.queryOneByCriteria(criteria);
        UserVerify userVerify = dozerHolder.convert(dto, UserVerify.class);
        userVerify.setFuid(dto.getFuid());
        Integer verResult = 0;
        if (result.getData() != null) {
            userVerify.setFuserVerifyId(result.getData().getFuserVerifyId());
            verResult = userVerifyApi.updateNotNull(userVerify).getData();
        } else {
            verResult = userVerifyApi.create(userVerify).getData();
        }
        if (verResult != 1) {
            return Result.failure(MallResultStatus.COMMON_UPDATE_FAIL);
        }
        User user = new User();
        user.setFverifyStatus(2);
        user.setFoperateType(dto.getFoperateType());
        user.setFuid(dto.getFuid());
        Result<Integer> updateNotNull = userApi.updateNotNull(user);
        if (!updateNotNull.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        return Result.success();
    }

    @Override
    public Result<UserVerifyVo> queryUserVerify(UserVerifyDto dto) {
        UserVerifyVo userVerifyVo = new UserVerifyVo();
        Criteria<User, Object> userCriteria = Criteria.of(User.class);
        userCriteria.andEqualTo(User::getFisDelete, "0")
                .andEqualTo(User::getFuid, dto.getFuid())
                .fields(User::getFverifyStatus, User::getFoperateType);
        Result<User> userResult = userApi.queryOneByCriteria(userCriteria);
        if (userResult.getData() == null) {
            Result.success(userVerifyVo);
        }
        if (userResult.getData().getFoperateType().equals(dto.getFoperateType())) {
            Criteria<UserVerify, Object> criteria = Criteria.of(UserVerify.class);
            criteria.andEqualTo(UserVerify::getFuid, dto.getFuid());
            Result<UserVerify> result = userVerifyApi.queryOneByCriteria(criteria);
            if (result.getData() != null) {
                userVerifyVo = dozerHolder.convert(result.getData(), UserVerifyVo.class);
//                if(userVerifyVo.getFpaltformId() != null){
//                    userVerifyVo.setFpaltformName(VerifyPlatform.getMessageByCode(userVerifyVo.getFpaltformId().intValue()));
//                }
                if (result.getData().getFplatform() != null && !result.getData().getFplatform().equals("")) {
                    if (VerifyEnums.platform.findByMsg(result.getData().getFplatform()).getCode() != null) {
                        userVerifyVo.setFpaltformId(VerifyEnums.platform.findByMsg(result.getData().getFplatform()).getCode().longValue());
                        userVerifyVo.setFpaltformName(result.getData().getFplatform());
                    }
                }
                if (userResult.getData().getFoperateType() == UserVerifyEnums.Type.WeiMerchantBuy.getValue()) {
                    //微商名称同步运营后台取值
                    if (userVerifyVo.getFshopName() != null) {
                        userVerifyVo.setFname(userVerifyVo.getFshopName());
                    }
                }
                if (userVerifyVo.getFsalesVolume() != null) {
                    userVerifyVo.setSalesVolume(new BigDecimal(userVerifyVo.getFsalesVolume()).divide(new BigDecimal(100)).toString());
                }
                if (userVerifyVo.getFshopProvinceId() != null) {
                    Criteria<CityRegion, Object> provinceCriteria = Criteria.of(CityRegion.class);
                    provinceCriteria.andEqualTo(CityRegion::getFregionId, userVerifyVo.getFshopProvinceId())
                            .fields(CityRegion::getFcrName);
                    Result<CityRegion> cityRegionResult = cityRegionApi.queryOneByCriteria(provinceCriteria);
                    if (cityRegionResult.getData() != null) {
                        userVerifyVo.setFshopProvinceName(cityRegionResult.getData().getFcrName());
                    } else {
                        userVerifyVo.setFshopProvinceName("");
                    }
                }
                if (userVerifyVo.getFshopAreaId() != null) {
                    Criteria<CityRegion, Object> areaCriteria = Criteria.of(CityRegion.class);
                    areaCriteria.andEqualTo(CityRegion::getFregionId, userVerifyVo.getFshopAreaId())
                            .fields(CityRegion::getFcrName);
                    Result<CityRegion> cityRegionResult = cityRegionApi.queryOneByCriteria(areaCriteria);
                    if (cityRegionResult.getData() != null) {
                        userVerifyVo.setFshopAreaName(cityRegionResult.getData().getFcrName());
                    } else {
                        userVerifyVo.setFshopAreaName("");
                    }
                }
                if (userVerifyVo.getFshopCityId() != null) {
                    Criteria<CityRegion, Object> cityCriteria = Criteria.of(CityRegion.class);
                    cityCriteria.andEqualTo(CityRegion::getFregionId, userVerifyVo.getFshopCityId())
                            .fields(CityRegion::getFcrName);
                    Result<CityRegion> cityRegionResult = cityRegionApi.queryOneByCriteria(cityCriteria);
                    if (cityRegionResult.getData() != null) {
                        userVerifyVo.setFshopCityName(cityRegionApi.queryOneByCriteria(cityCriteria).getData().getFcrName());
                    } else {
                        userVerifyVo.setFshopCityName("");
                    }
                }
            }
            userVerifyVo.setFverifyStatus(userResult.getData().getFverifyStatus());
        } else {
            userVerifyVo.setFverifyStatus(1);
        }
        return Result.success(userVerifyVo);
    }

    @Override
    public Result<List<VerifyCategoryVo>> queryCategory() {
        List<VerifyCategoryVo> VerifyCategoryList = new ArrayList<>();
        for (int i = 1, len = VerifyCategory.values().length; i <= len; i++) {
            VerifyCategoryVo verifyCategoryVo = new VerifyCategoryVo();
            verifyCategoryVo.setFcategoryName(VerifyCategory.getMessageByCode(i));
            verifyCategoryVo.setFcategoryId(i);
            VerifyCategoryList.add(verifyCategoryVo);
        }
        return Result.success(VerifyCategoryList);
    }

    @Override
    public Result<List<VerifyPlatformVo>> queryPlatform() {
        List<VerifyPlatformVo> VerifyPlatformVoList = new ArrayList<>();
        for (int i = 1, len = VerifyPlatform.values().length; i <= len; i++) {
            VerifyPlatformVo verifyPlatformVo = new VerifyPlatformVo();
            verifyPlatformVo.setFplatformName(VerifyPlatform.getMessageByCode(i));
            verifyPlatformVo.setFplatforId(i);
            VerifyPlatformVoList.add(verifyPlatformVo);
        }
        return Result.success(VerifyPlatformVoList);
    }

    @Override
    public Result<UserVo> queryUserVerifyStatus(Long fuid) {
        UserVo userVo = new UserVo();
        userVo.setFverifyStatus(1);
        userVo.setFoperateType(0);
        Criteria<User, Object> userCriteria = Criteria.of(User.class);
        userCriteria.andEqualTo(User::getFisDelete, "0")
                .andEqualTo(User::getFuid, fuid)
                .fields(User::getFverifyStatus, User::getFoperateType);
        Result<User> userResult = userApi.queryOneByCriteria(userCriteria);
        if (userResult.getData() != null) {
            userVo.setFverifyStatus(userResult.getData().getFverifyStatus());
            userVo.setFoperateType(userResult.getData().getFoperateType());
        }
        return Result.success(userVo);
    }

    @Override
    public Result<UserVo> queryUserInfo(Long fuid) {
        UserVo userVo = new UserVo();
        Criteria<User, Object> userCriteria = Criteria.of(User.class);
        userCriteria.andEqualTo(User::getFisDelete, "0")
                .andEqualTo(User::getFuid, fuid)
                .fields(User::getFuid, User::getFuname, User::getFnickname
                        , User::getFheadpic, User::getFoperateType
                        , User::getFfreezeStatus, User::getFverifyStatus, User::getFregisterFrom
                        , User::getFmobile, User::getFmail, User::getFwithdrawPasswd
                        , User::getFlastloginTime, User::getFuserValidTime);
        Result<User> userResult = userApi.queryOneByCriteria(userCriteria);
        if (userResult.getData() != null) {
            userVo = dozerHolder.convert(userResult.getData(), UserVo.class);
            //判断fnickname是否为空字符串，若为空字符串则表示用户还没修改过用户名，用户是否可修改：0否，1是
            if (userVo.getFnickname().equals("")) {
                userVo.setFunameIsModify(1);
            } else {
                userVo.setFunameIsModify(0);

            }
            userVo.setFnickname(userVo.getFuname());
            if (userResult.getData().getFwithdrawPasswd().equals("")) {
                userVo.setFwithdrawPasswdStatus(0);
            } else {
                userVo.setFwithdrawPasswdStatus(1);
            }
        }
        return Result.success(userVo);
    }

    @Override
    public Result<UserVo> queryPopupWindowsStatus(Long fuid) {
        UserVo userVo = new UserVo();
        Criteria<User, Object> userCriteria = Criteria.of(User.class);
        userCriteria.andEqualTo(User::getFisDelete, "0")
                .andEqualTo(User::getFuid, fuid)
                .fields(User::getFuid, User::getFlastloginTime, User::getFuserValidTime);
        Result<User> userResult = userApi.queryOneByCriteria(userCriteria);
        if (userResult.getData() != null) {
            //查询用户有无未使用的认证优惠券
            Integer couponAuthenticationNum = 0;
            //当最近登录时间大于认证审核通过时间,就不再触发弹窗
            userVo.setIsPopupWindows(0);
            if (userResult.getData().getFlastloginTime().compareTo(userResult.getData().getFuserValidTime()) < 0) {
                couponAuthenticationNum = queryAuthenticationCoupon(userResult.getData().getFuid());
                if (!couponAuthenticationNum.equals(0)) {
                    userVo.setIsPopupWindows(1);
                }
                //更新最近登录时间
                User user = new User();
                user.setFuid(userResult.getData().getFuid());
                user.setFlastloginTime(new Date());
                userApi.updateNotNull(user);
            }
            //当数量大于0,就显示发了几张认证优惠券
            userVo.setCouponAuthenticationNum(couponAuthenticationNum);
        }
        return Result.success(userVo);
    }

    @Override
    public Result couponLinkReceive(CouponLinkDto dto) {
        if (dto.getCouponLink() == null || dto.getCouponLink().equals("")) {
            return Result.failure(MallExceptionCode.COUPON_LINK_INEXUSTENCE);
        }
        Criteria<Coupon, Object> couponCriteria = Criteria.of(Coupon.class)
                .andEqualTo(Coupon::getFcouponLink, dto.getCouponLink())
                .andNotEqualTo(Coupon::getFcouponStatus, 1)
                .fields(Coupon::getFcouponId, Coupon::getFperLimit, Coupon::getFsurplusReleaseQty
                        , Coupon::getFcouponStatus, Coupon::getFcouponType);
        Result<Coupon> couponResult = couponApi.queryOneByCriteria(couponCriteria);
        if (!couponResult.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if (couponResult.getData() == null) {
            //优惠券链接不存在
            return Result.failure(MallExceptionCode.COUPON_LINK_INEXUSTENCE);
        }
        //优惠券状态 1待发布、2已发布、3已过期、4已结束、5已作废
        Integer couponStatus = couponResult.getData().getFcouponStatus();
        if (!couponStatus.equals(2)) {
            return Result.failure(MallExceptionCode.COUPON_IS_INVALID);
        }
        Long couponId = couponResult.getData().getFcouponId();
        //查询用户是否有领取资格
        CouponQueryDto couponQueryDto = new CouponQueryDto();
        List<Integer> couponTypeList = new ArrayList<>();
        couponTypeList.add(couponResult.getData().getFcouponType());
        couponQueryDto.setCouponTypes(couponTypeList);
        couponQueryDto.setUserId(dto.getFuid());
        Result<List<CouponQueryVo>> listResult = couponProviderApi.queryByUserId(couponQueryDto);
        if (!listResult.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        Boolean isReceive = false;
        for (CouponQueryVo couponQueryVo : listResult.getData()) {
            if (couponQueryVo.getFcouponId().equals(couponId)) {
                isReceive = true;
                break;
            }
        }
        if (!isReceive) {
            return Result.failure(MallExceptionCode.COUPON_INELIGIBILITY);
        }
        //剩余发放数量
        Integer surplusReleaseQty = couponResult.getData().getFsurplusReleaseQty();
        if (surplusReleaseQty.equals(0)) {
            return Result.failure(MallExceptionCode.COUPON_IS_PAID_OUT);
        }
        //每人限领
        Integer perLimit = couponResult.getData().getFperLimit();
        if (!perLimit.equals(0)) {
            //查询用户领取数量 = 待发放+已发放
            Criteria<CouponBindUser, Object> couponBindUserCriteria = Criteria.of(CouponBindUser.class)
                    .andEqualTo(CouponBindUser::getFuid, dto.getFuid())
                    .andEqualTo(CouponBindUser::getFcouponId, couponId);
            Result<Integer> integerResult = couponBindUserApi.countByCriteria(couponBindUserCriteria);
            if (!integerResult.isSuccess()) {
                throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
            }
            Integer StayOutNum = 0;
            if (integerResult.getData() != null) {
                StayOutNum = integerResult.getData();
            }
            if (StayOutNum >= perLimit) {
                //返回已领取
                return Result.failure(MallExceptionCode.COUPON_IS_MAX);
            }
            Criteria<CouponReceive, Object> couponReceiveCriteria = Criteria.of(CouponReceive.class)
                    .andEqualTo(CouponReceive::getFuid, dto.getFuid())
                    .andEqualTo(CouponReceive::getFcouponId, couponId);
            Result<Integer> integerResult1 = couponReceiveApi.countByCriteria(couponReceiveCriteria);
            if (!integerResult1.isSuccess()) {
                throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
            }
            Integer couponNum = 0;
            if (integerResult1.getData() != null) {
                couponNum = integerResult1.getData();
            }
            if (couponNum + StayOutNum >= perLimit) {
                //返回已领取
                return Result.failure(MallExceptionCode.COUPON_IS_MAX);
            }
        }
        return Result.success(couponId);
    }

    private Integer queryAuthenticationCoupon(Long fuid) {
        Integer couponAuthenticationNum = 0;
        //查询可用注册优惠券
        Criteria<Coupon, Object> couponCriteria = Criteria.of(Coupon.class)
                .andEqualTo(Coupon::getFcouponStatus, 2)
                .andEqualTo(Coupon::getFreleaseType, CouponScene.AUTHENTICATION.getCode());
        Result<List<Coupon>> listResult = couponApi.queryByCriteria(couponCriteria);
        if (listResult.isSuccess()) {
            for (Coupon coupon : listResult.getData()) {
                //查询用户是否存在当前优惠券
                Criteria<CouponReceive, Object> couponReceiveCriteria = Criteria.of(CouponReceive.class)
                        .andEqualTo(CouponReceive::getFcouponId, coupon.getFcouponId())
                        .andEqualTo(CouponReceive::getFuserCouponStatus, 1)
                        .andEqualTo(CouponReceive::getFuid, fuid)
                        .fields(CouponReceive::getFcouponId);
                Integer couponNum = 0;
                Result<List<CouponReceive>> result = couponReceiveApi.queryByCriteria(couponReceiveCriteria);
                if (result.isSuccess()) {
                    couponNum = result.getData().size();
                }
                couponAuthenticationNum = couponAuthenticationNum + couponNum;
            }
        }
        return couponAuthenticationNum;
    }

    @Override
    public Result<Integer> modifiyUserNickname(UserDto dto) {
        //查询用户是否已设置用户名,将fnickname替换成funame
        if (dto.getFnickname().equals("")) {
            Result.failure(MallResultStatus.REGISTER_NAME_IS_NULL);
        }
        //判断是否为手机号
        //手机号校验
        boolean mobileCheck = mobileCheck(dto.getFnickname());
        if (mobileCheck) {
            return Result.failure(MallResultStatus.MOBLIE_CANNOT_BE_USED_AS_UNAME);
        }
        //不能含有@字符
        boolean atCheck = dto.getFnickname().contains("@");
        if (atCheck) {
            return Result.failure(MallResultStatus.ILLEGAL_CHARACTER);
        }
        //不能含有特殊字符
        boolean symbolCheck = checkSymbol(dto.getFnickname());
        if (symbolCheck) {
            return Result.failure(MallResultStatus.NO_SPECIAL_SYMBOLS);
        }
        Criteria<User, Object> userCriteria = Criteria.of(User.class);
        userCriteria.andEqualTo(User::getFisDelete, "0")
                .andEqualTo(User::getFuid, dto.getFuid())
                .fields(User::getFuname, User::getFnickname);
        Result<User> userResult = userApi.queryOneByCriteria(userCriteria);
        if (userResult.getData() == null) {
            return Result.failure(MallResultStatus.ACCOUNT_NOT_EXIST);
        }
        if (!userResult.getData().getFnickname().equals("")) {
            return Result.failure(MallResultStatus.USER_NICKNAME_EXIST);
        }
        //分布式锁是否获得成功
        boolean lockUname = false;
        try {
            //对唯一的字段添加分布式锁,如果锁已存在,会立刻抛异常
            lockUname = xybbcLock.tryLock(LOCK_PREFIX_UNAME + dto.getFnickname(), "", LOCK_EXPIRING);
            if (!lockUname) {
                return Result.failure(MallResultStatus.COMMON_UPDATE_FAIL);
            }
            //查询是否重名
            Criteria<User, Object> userObjectCriteria = Criteria.of(User.class);
            userObjectCriteria.andEqualTo(User::getFisDelete, "0")
                    .andEqualTo(User::getFuname, dto.getFnickname());
            Result<Integer> result = userApi.countByCriteria(userObjectCriteria);
            if (result.getData() != 0) {
                return Result.failure(MallResultStatus.REGISTER_NAME_EXIST);
            }
            User user = new User();
            user.setFnickname(dto.getFnickname());
            user.setFuname(dto.getFnickname());
            user.setFuid(dto.getFuid());
            return userApi.updateNotNull(user);
        } finally {
            //释放分布式锁
            if (lockUname) {
                xybbcLock.releaseLock(LOCK_PREFIX_UNAME + dto.getFnickname(), "");
            }
        }
    }

    private boolean checkSymbol(String fnickname) {
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(fnickname);
        return m.find();
    }

    @Override
    public Result<SendSmsVo> accountSecurityVerification(UserSecurityDto dto) {
        SendSmsVo sendSmsVo = new SendSmsVo();
        //查询用户手机邮箱
        Criteria<User, Object> userCriteria = Criteria.of(User.class);
        userCriteria.andEqualTo(User::getFisDelete, "0")
                .andEqualTo(User::getFuid, dto.getFuid())
                .fields(User::getFmobile, User::getFmail);
        Result<User> userResult = userApi.queryOneByCriteria(userCriteria);
        //判断验证信息发送方式
        Integer requestType = dto.getRequestType();
        if (requestType == 1) {
            //手机发送
            dto.setFmobile(userResult.getData().getFmobile());
            String mobile = dto.getFmobile();
            //手机号校验
            boolean mobileCheck = mobileCheck(mobile);
            if (!mobileCheck) {
                return Result.failure(MallResultStatus.BIND_MOBILE_ERROR);
            }
            //短信发送次数校验
            Map<String, Object> retMsg = checkSendSmsFrequency(dto);
            if (retMsg.get("code") != null && !retMsg.get("code").equals("")) {
                if (retMsg.get("code").equals("1010")) {
                    return Result.failure(MallResultStatus.SMS_AUTH_IS_SEND);
                } else if (retMsg.get("code").equals("1007")) {
                    return Result.failure(MallResultStatus.USER_SEND_SMS_FAILD);
                }
            }
            if (retMsg.get("isCheck") != null) {
                sendSmsVo.setIsCheck(0);
                return Result.success(sendSmsVo);
            }
            sendSmsVo = sendSms(sendSmsVo, dto);
        } else {
            //邮箱发送
            dto.setFmail(userResult.getData().getFmail());
            sendSmsVo = sedEmail(sendSmsVo, dto);
        }
        return Result.success(sendSmsVo);
    }

    @Override
    public Result<Integer> checkEmailAuthNum(UserSecurityDto dto) {
        if (dto.getAuthNum().length() != 4) {
            return Result.failure(MallResultStatus.SMS_AUTH_NUM_ERROR);
        }
        //校验短信验证码的key
        Claims keyClaims = xyUserJwtManager.parseJwt(dto.getAuthNumKey());
        if (keyClaims == null) {
            return Result.failure(MallResultStatus.EMAIL_AUTH_NUM_OUT_TIME);
        }
        //判断手机号和验证码是否匹配
        String key = keyClaims.getSubject();
        if (Strings.isNullOrEmpty(key) || !key.equals(dto.getFmail())) {
            return Result.failure(MallResultStatus.SMS_AUTH_NUM_ERROR);
        }
        String authNum = String.valueOf(xyRedisManager.get(dto.getAuthNumKey()));
        if (Strings.isNullOrEmpty(authNum) || !dto.getAuthNum().equals(authNum)) {
            return Result.failure(MallResultStatus.SMS_AUTH_NUM_ERROR);
        }
        return Result.success();
    }

    @Override
    public Result<Integer> modifiyPayPwd(UserSecurityDto dto) {
        Criteria<User, Object> criteria = Criteria.of(User.class);
        criteria.andEqualTo(User::getFuid, dto.getFuid())
                .andEqualTo(User::getFisDelete, "0").fields(User::getFuid);
        Result<User> result = userApi.queryOneByCriteria(criteria);
        if (result.getData() == null) {
            return Result.failure(MallResultStatus.PWD_MIDIFY_FAILED);
        }
        User user = new User();
        String passWord = EncryptUtils.aesDecrypt(dto.getFwithdrawPasswd());
        if (passWord == null || passWord.equals("")) {
            throw new BizException(MallResultStatus.PWD_MIDIFY_FAILED);
        }
        passWord = Md5Utils.toMd5(passWord);
        user.setFwithdrawPasswd(passWord);
        user.setFuid(result.getData().getFuid());
        return userApi.updateNotNull(user);
    }

    @Override
    public Result<Integer> modifiyPwd(UserSecurityDto dto) {
        Criteria<User, Object> criteria = Criteria.of(User.class);
        criteria.andEqualTo(User::getFuid, dto.getFuid())
                .andEqualTo(User::getFisDelete, "0").fields(User::getFuid);
        Result<User> result = userApi.queryOneByCriteria(criteria);
        if (result.getData() == null) {
            return Result.failure(MallResultStatus.PWD_MIDIFY_FAILED);
        }
        User user = new User();
        String passWord = EncryptUtils.aesDecrypt(dto.getFpasswd());
        if (passWord == null || passWord.equals("")) {
            throw new BizException(MallResultStatus.PWD_MIDIFY_FAILED);
        }
        passWord = Md5Utils.toMd5(passWord);
        user.setFpasswd(passWord);
        user.setFuid(result.getData().getFuid());
        return userApi.updateNotNull(user);
    }

    @Override
    public Result<Integer> modifiyEmailAccount(UserSecurityDto dto) {
        Criteria<User, Object> criteria = Criteria.of(User.class);
        criteria.andEqualTo(User::getFuid, dto.getFuid())
                .andEqualTo(User::getFisDelete, "0").fields(User::getFuid);
        Result<User> result = userApi.queryOneByCriteria(criteria);
        if (result.getData() == null) {
            return Result.failure(MallResultStatus.ACCOUNT_NOT_EXIST);
        }
        User user = new User();
        user.setFmail(dto.getFmail());
        user.setFemailValidTime(new Date());
        user.setFmailIsValid(1);
        user.setFuid(result.getData().getFuid());
        return userApi.updateNotNull(user);
    }

    @Override
    public Result<SendSmsVo> modifiyMobileSendSMS(UserSecurityDto dto) {
        SendSmsVo sendSmsVo = new SendSmsVo();
        String mobile = dto.getFmobile();
        //手机号校验
        boolean mobileCheck = mobileCheck(mobile);
        if (!mobileCheck) {
            return Result.failure(MallResultStatus.BIND_MOBILE_ERROR);
        }
        //手机号是否注册校验
        boolean CheckExist = CheckMobileExist(mobile);
        //注册校验返回
        if (!CheckExist) {
            return Result.failure(MallResultStatus.REGISTER_MOBILE_EXIST);
        }
        //短信发送次数校验
        Map<String, Object> retMsg = checkSendSmsFrequency(dto);
        if (retMsg.get("code") != null && !retMsg.get("code").equals("")) {
            if (retMsg.get("code").equals("1010")) {
                return Result.failure(MallResultStatus.SMS_AUTH_IS_SEND);
            } else if (retMsg.get("code").equals("1007")) {
                return Result.failure(MallResultStatus.USER_SEND_SMS_FAILD);
            }
        }
        if (retMsg.get("isCheck") != null) {
            sendSmsVo.setIsCheck(0);
            return Result.success(sendSmsVo);
        }
        UserSecurityDto userSecurityDto = new UserSecurityDto();
        userSecurityDto.setFmobile(dto.getFmobile());
        userSecurityDto.setImei(dto.getImei());
        userSecurityDto.setIpAddress(dto.getIpAddress());
        sendSmsVo = sendSms(sendSmsVo, userSecurityDto);
        return Result.success(sendSmsVo);
    }

    @Override
    public Result<Integer> modifiyMobile(UserSecurityDto dto) {
        boolean check = checkPAuthNumber(dto.getAuthNum(), dto.getAuthNumKey(), dto.getFmobile());
        if (!check) {
            return Result.failure(MallResultStatus.SMS_AUTH_NUM_ERROR);
        }
        Criteria<User, Object> criteria = Criteria.of(User.class);
        criteria.andLeft().orEqualTo(User::getFmobile, dto.getFmobile())
                .orEqualTo(User::getFuname, dto.getFmobile()).addRight()
                .andEqualTo(User::getFisDelete, "0").fields(User::getFuid);
        Result<User> result = userApi.queryOneByCriteria(criteria);
        if (result.getData() != null) {
            return Result.failure(MallResultStatus.REGISTER_MOBILE_EXIST);
        }
        User user = new User();
        user.setFmobile(dto.getFmobile());
        user.setFmobileValidTime(new Date());
        user.setFuid(dto.getFuid());
        return userApi.updateNotNull(user);
    }

    private Map<String, Object> checkSendSmsFrequency(UserSecurityDto dto) {
        Integer isCheck = 0;
        if (dto.getIsCheck() != null) {
            isCheck = dto.getIsCheck();
        }
        Map<String, Object> map = new HashMap<>();
        String code = "";
        //手机号短信发送次数校验
        if (xyRedisManager.get(dto.getFmobile()) != null) {
            code = MallResultStatus.SMS_AUTH_IS_SEND.getCode();
        }
        if (xyRedisManager.get(dto.getImei()) != null) {
            String IMEINum = String.valueOf(xyRedisManager.get(dto.getImei()));
            if (Integer.valueOf(IMEINum) < 10) {
                if (Integer.valueOf(IMEINum) > 5 && isCheck == 0) {
                    map.put("isCheck", "0");
                    return map;
                }
            } else {
                code = MallResultStatus.USER_SEND_SMS_FAILD.getCode();
            }
        }
//        if(xyRedisManager.get(dto.getIpAddress()) != null){
//            String ipNum = String.valueOf(xyRedisManager.get(dto.getIpAddress()));
//            if(Integer.valueOf(ipNum) > 20){
//                code =  MallResultStatus.USER_SEND_SMS_FAILD.getCode();
//            }
//        }
        map.put("code", code);
        return map;
    }


    private SendSmsVo sedEmail(SendSmsVo sendSmsVo, UserSecurityDto dto) {
        String fmail = dto.getFmail();
        String authNum = generateAuthNum(4);
        String SmsTemplate = "您的安全验证码为：" + authNum + "，请及时验证。行云全球汇";
        String signature = "【行云全球汇】";
        String content = signature + SmsTemplate;
        logger.info(content);
        Result<Boolean> result = emailApi.commonSendEmail(content, fmail);
        if (!result.isSuccess()) {
            throw new BizException(MallResultStatus.SMS_SEND_FAILD);
        }
        String authNumKey = xyUserJwtManager.createJwt("", fmail, UserConstants.Sms.MOBILE_AUTH_CODE_EXPIRE_TIME);
        xyRedisManager.set(authNumKey, authNum, UserConstants.Sms.MOBILE_AUTH_CODE_EXPIRE_TIME / 1000);
        sendSmsVo.setAuthNumKey(authNumKey);
        return sendSmsVo;
    }


    private Integer checkUserVerifyDto(UserVerifyDto dto) {
        //认证类型：1实体门店，2网络店铺，3网络平台，4批采企业，5微商代购
        Integer mistakeMsg = 0;
        if (dto.getFoperateType() == null) {
            return UserVerifyResultStatus.OPERATE_TYPE_NOT_EXIST.getCode();
        } else {
            Integer operateType = dto.getFoperateType();
            switch (operateType) {
                case 1:
                    if (dto.getFshopName() == null || dto.getFshopName().equals("")) {
                        return UserVerifyResultStatus.SHOP_NAME_NOT_EXIST.getCode();
                    }
                    if (dto.getFshopProvinceId() == null || dto.getFshopProvinceId().equals("")
                            || dto.getFshopAreaId() == null || dto.getFshopAreaId().equals("")
                            || dto.getFshopCityId() == null || dto.getFshopCityId().equals("")) {
                        return UserVerifyResultStatus.SHOP_ADDRESS_NOT_EXIST.getCode();
                    }
                    if (dto.getFshopAddress() == null || dto.getFshopAddress().equals("")) {
                        return UserVerifyResultStatus.DETAILED_ADDRESS_NOT_EXIST.getCode();
                    }
                    if (dto.getFshopFront() == null || dto.getFshopFront().equals("")) {
                        return UserVerifyResultStatus.SHOP_FRONT_PIC_NOT_EXIST.getCode();
                    }
                    if (dto.getFbusinessLicensePic() == null || dto.getFbusinessLicensePic().equals("")) {
                        return UserVerifyResultStatus.BUSINESS_LICENSE_PIC_NOT_EXIST.getCode();
                    }
                    // TODO: 2019/11/26 需求变更  去掉身份证上传认证, 添加营业执照号码
                    if (dto.getFbusinessLicenseNo() == null || dto.getFbusinessLicenseNo().equals("")) {
                        return UserVerifyResultStatus.BUSINESS_LICENSE_PIC_NO_NOT_EXIST.getCode();
                    }
                    break;
                case 2:
                    if (dto.getFshopName() == null || dto.getFshopName().equals("")) {
                        return UserVerifyResultStatus.SHOP_NAME_NOT_EXIST.getCode();
                    }
                    if (dto.getFpaltformId() == null && dto.getFpaltformId().equals("")) {
                        return UserVerifyResultStatus.PALTFORM_NOT_EXIST.getCode();
                    }
                    if (dto.getFshopWeb() == null && dto.getFshopWeb().equals("")) {
                        return UserVerifyResultStatus.SHOP_WEB_NOT_EXIST.getCode();
                    }
                    if (dto.getFidcardBack() == null && dto.getFidcardBack().equals("")
                            || dto.getFidcardFront() == null && dto.getFidcardFront().equals("")) {
                        return UserVerifyResultStatus.USER_IDCARD_NOT_EXIST.getCode();
                    }
                    break;
                case 3:
                    if (dto.getFcompanyName() == null && dto.getFcompanyName().equals("")) {
                        return UserVerifyResultStatus.COMPANY_NAME_NOT_EXIST.getCode();
                    }
                    if (dto.getFshopName() == null && dto.getFshopName().equals("")) {
                        return UserVerifyResultStatus.PALTFORM_NAME_NOT_EXIST.getCode();
                    }
                    if (dto.getFshopWeb() == null && dto.getFshopWeb().equals("")) {
                        return UserVerifyResultStatus.PALTFORM_WEB_NOT_EXIST.getCode();
                    }
                    if (dto.getFbusinessLicensePic() == null && dto.getFbusinessLicensePic().equals("")) {
                        return UserVerifyResultStatus.BUSINESS_LICENSE_PIC_NOT_EXIST.getCode();
                    }
                    if (dto.getFidcardBack() == null && dto.getFidcardBack().equals("")
                            || dto.getFidcardFront() == null && dto.getFidcardFront().equals("")) {
                        return UserVerifyResultStatus.IDCARD_NOT_EXIST.getCode();
                    }
                    break;
                case 4:
                    if (dto.getFcompanyName() == null && dto.getFcompanyName().equals("")) {
                        return UserVerifyResultStatus.COMPANY_NAME_NOT_EXIST.getCode();
                    }
                    if (dto.getFshopProvinceId() == null && dto.getFshopProvinceId().equals("")
                            || dto.getFshopAreaId() == null && dto.getFshopAreaId().equals("")
                            || dto.getFshopCityId() == null && dto.getFshopCityId().equals("")) {
                        return UserVerifyResultStatus.COMPANY_ADDRESS_NOT_EXIST.getCode();
                    }
                    if (dto.getFshopAddress() == null && dto.getFshopAddress().equals("")) {
                        return UserVerifyResultStatus.DETAILED_ADDRESS_NOT_EXIST.getCode();
                    }
                    if (dto.getFbusinessLicensePic() == null && dto.getFbusinessLicensePic().equals("")) {
                        return UserVerifyResultStatus.BUSINESS_LICENSE_PIC_NOT_EXIST.getCode();
                    }
                    if (dto.getFidcardBack() == null && dto.getFidcardBack().equals("")
                            || dto.getFidcardFront() == null && dto.getFidcardBack().equals("")) {
                        return UserVerifyResultStatus.IDCARD_NOT_EXIST.getCode();
                    }
                    break;
                case 5:
                    if (dto.getFname() == null && dto.getFname().equals("")) {
                        return UserVerifyResultStatus.FUNAME_NOT_EXIST.getCode();
                    }
                    if (dto.getFidcardNo() == null && dto.getFidcardNo().equals("")) {
                        return UserVerifyResultStatus.USER_IDCARD_NOT_EXIST.getCode();
                    }
                    if (dto.getFidcardBack() == null && dto.getFidcardBack().equals("")
                            || dto.getFidcardFront() == null && dto.getFidcardFront().equals("")) {
                        return UserVerifyResultStatus.USER_IDCARD_NOT_EXIST.getCode();
                    }
                    break;
                default:
                    break;
            }
        }
        return mistakeMsg;
    }


    private boolean checkPAuthNumber(String pauthNum, String authNumKey, String fmobile) {
        if (pauthNum.length() != 4) {
            return false;
        }
        //校验短信验证码的key
        Claims keyClaims = xyUserJwtManager.parseJwt(authNumKey);
        if (keyClaims == null) {
            return false;
        }
        //判断手机号和验证码是否匹配
        String key = keyClaims.getSubject();
        if (Strings.isNullOrEmpty(key) || !key.equals(fmobile)) {
            return false;
        }
        String authNum = String.valueOf(xyRedisManager.get(authNumKey));
        if (Strings.isNullOrEmpty(authNum) || !pauthNum.equals(authNum)) {
            return false;
        }
        return true;
    }

    private String generateAuthNum(int authLen) {
        StringBuilder str = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < authLen; i++) {
            str.append(random.nextInt(10));
        }
        return str.toString();
    }

    @Override
    public Result<Integer> getUnusedCouponCount(Long fuid) {
        Result<Integer> couponResult = couponReceiveApi.countByCriteria(Criteria.of(CouponReceive.class).fields(
                CouponReceive::getFcouponId).andEqualTo(CouponReceive::getFuid, fuid).andEqualTo(CouponReceive::getFuserCouponStatus, 1));

        if (!couponResult.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        return Result.success(couponResult.getData());
    }

}
