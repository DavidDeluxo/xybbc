package com.xingyun.bbc.mall.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.xingyun.bbc.core.enums.ResultStatus;
import com.xingyun.bbc.core.exception.BizException;
import com.xingyun.bbc.core.operate.api.CityRegionApi;
import com.xingyun.bbc.core.operate.po.CityRegion;
import com.xingyun.bbc.core.query.Criteria;
import com.xingyun.bbc.core.user.api.UserDeliveryApi;
import com.xingyun.bbc.core.user.po.UserDelivery;
import com.xingyun.bbc.core.utils.Result;
import com.xingyun.bbc.mall.base.utils.DozerHolder;
import com.xingyun.bbc.mall.base.utils.FileUtil;
import com.xingyun.bbc.mall.base.utils.PageUtils;
import com.xingyun.bbc.mall.common.exception.MallExceptionCode;
import com.xingyun.bbc.mall.model.dto.*;
import com.xingyun.bbc.mall.model.vo.AddressFileInfoVo;
import com.xingyun.bbc.mall.model.vo.CityRegionVo;
import com.xingyun.bbc.mall.model.vo.PageVo;
import com.xingyun.bbc.mall.model.vo.UserDeliveryVo;
import com.xingyun.bbc.mall.service.UserAddressService;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections.CollectionUtils;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author lll
 * @Title:
 * @Description:
 * @date 2019-09-03 11:00
 */
@Service
public class UserAddressServiceImpl implements UserAddressService {

    public static final Logger logger = LoggerFactory.getLogger(UserAddressServiceImpl.class);

    @Autowired
    private UserDeliveryApi userDeliveryApi;

    @Autowired
    private Mapper dozerMapper;

    @Autowired
    private DozerHolder dozerHolder;

    @Autowired
    private CityRegionApi cityRegionApi;

    @Autowired
    private PageUtils pageUtils;

    @Value("${commonfile.addressFile.fileUrl}")
    private String url;

    @Value("${commonfile.versionFile.fileUrl}")
    private String versionUrl;

    @Value("${commonfile.addressFile.fileName}")
    private String fileName;

    /**
     * @author lll
     * @version V1.0
     * @Description: 查询用户收货地址列表
     * @Param: [userDeliveryDto]
     * @return: PageVo<UserDeliveryVo>
     * @date 2019/9/20 13:49
     */
    @Override
    public PageVo<UserDeliveryVo> getUserAddress(UserDeliveryDto userDeliveryDto) {
        //校验用户id
        if (userDeliveryDto.getFuid() == null) {
            throw new BizException(MallExceptionCode.REQUIRED_PARAM_MISSING);
        }
        //查询当前用户下未删除的的数据
        Criteria<UserDelivery, Object> criteria = Criteria.of(UserDelivery.class);
        if (!StringUtils.isEmpty(userDeliveryDto.getFuid())) {
            criteria.andEqualTo(UserDelivery::getFuid, userDeliveryDto.getFuid())
                    .andEqualTo(UserDelivery::getFisDelete, 0);
        }
        if (!StringUtils.isEmpty(userDeliveryDto.getKeyWord())) {

            String keyWord = "%" + userDeliveryDto.getKeyWord() + "%";

            criteria.andLeft();
            criteria.andLike(UserDelivery::getFdeliveryName,keyWord);

            criteria.orLeft();
            criteria.andLike(UserDelivery::getFdeliveryMobile,keyWord);
            criteria.addRight();

            criteria.orLeft();
            criteria.andLike(UserDelivery::getFdeliveryProvinceName,keyWord);
            criteria.addRight();

            criteria.orLeft();
            criteria.andLike(UserDelivery::getFdeliveryCityName,keyWord);
            criteria.addRight();

            criteria.orLeft();
            criteria.andLike(UserDelivery::getFdeliveryAreaName,keyWord);
            criteria.addRight();

            criteria.orLeft();
            criteria.andLike(UserDelivery::getFdeliveryAddr,keyWord);
            criteria.addRight();

            criteria.addRight();
        }
        criteria.sortDesc(UserDelivery::getFmodifyTime);
        //查询总数分页
        Result<Integer> totalResult = userDeliveryApi.countByCriteria(criteria.page(userDeliveryDto.getCurrentPage(), userDeliveryDto.getPageSize()));
        if (!totalResult.isSuccess()) {
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if (0 == totalResult.getData() || Objects.isNull(totalResult.getData())) {
            return new PageVo<>(0, userDeliveryDto.getCurrentPage(), userDeliveryDto.getPageSize(), Lists.newArrayList());
        }
        Result<List<UserDelivery>> userDeliveryList = userDeliveryApi.queryByCriteria(criteria);
        if (!userDeliveryList.isSuccess()) {
            logger.error("查询收货地址总数失败当前页{}", userDeliveryDto.getCurrentPage());
            throw new BizException(ResultStatus.REMOTE_SERVICE_ERROR);
        }
        if (CollectionUtils.isEmpty(userDeliveryList.getData())) {
            return new PageVo<>(0, userDeliveryDto.getCurrentPage(), userDeliveryDto.getPageSize(), Lists.newArrayList());
        }
        List<UserDeliveryVo> userDeliveryVoList = dozerHolder.convert(userDeliveryList.getData(), UserDeliveryVo.class);
        return pageUtils.convert(totalResult.getData(), userDeliveryVoList, UserDeliveryVo.class, userDeliveryDto);
    }

    /**
     * @author lll
     * @version V1.0
     * @Description: 查询用户所有收货地址不分页
     * @Param: [userDeliveryDto]
     * @return: PageVo<UserDeliveryVo>
     * @date 2019/9/20 13:49
     */
    public Result<List<UserDeliveryVo>> getUserAddressList(UserDeliveryDto userDeliveryDto) {
        Criteria<UserDelivery, Object> criteria = Criteria.of(UserDelivery.class);
        if (!StringUtils.isEmpty(userDeliveryDto.getFuid())) {
            criteria.andEqualTo(UserDelivery::getFuid, userDeliveryDto.getFuid()).andEqualTo(UserDelivery::getFisDelete, 0);
        }
        Result<List<UserDelivery>> userDeliveryList = userDeliveryApi.queryByCriteria(criteria);
        if (!userDeliveryList.isSuccess()) {
            logger.info("查询收货地址总数失败");
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        List<UserDeliveryVo> userDeliveryVoList = dozerHolder.convert(userDeliveryList.getData(), UserDeliveryVo.class);
        return Result.success(userDeliveryVoList);
    }

    /**
     * @author lll
     * @version V1.0
     * @Description: 新增用户收货地址
     * @Param: [userDeliveryDto]
     * @return: Result
     * @date 2019/9/20 13:49
     */
    @Override
    @GlobalTransactional
    public Result addUserAddress(UserDeliveryAddDto userDeliveryDto) {
        //校验用户id
        if (userDeliveryDto.getFuid() == null) {
            throw new BizException(MallExceptionCode.REQUIRED_PARAM_MISSING);
        }
        Integer isDefault = userDeliveryDto.getFisDefualt();
        UserDeliveryDto userDelivery = new UserDeliveryDto();
        userDelivery.setFuid(userDeliveryDto.getFuid());
        //判断此次新增地址是否设置为默认地址,是的话则需要校验原来的收货地址中是否有默认地址，并且更新默认地址
        if (!StringUtils.isEmpty(isDefault)) {
            this.checkDefualt(isDefault, userDelivery);
        }
        UserDelivery user = dozerMapper.map(userDeliveryDto, UserDelivery.class);
        Result<Integer> result = userDeliveryApi.create(user);
        if (!result.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        return Result.success();
    }

    /**
     * @author lll
     * @version V1.0
     * @Description: 校验用户收货地址中是否存在默认地址
     * @Param: [userDeliveryDto]
     * @return: Result
     * @date 2019/9/20 13:49
     */
    private void checkDefualt(Integer isDefault, UserDeliveryDto userDelivery) {
        //该地址是默认地址时
        if (isDefault == 1) {
            List<UserDeliveryVo> userDeliveryVoList = this.getUserAddressList(userDelivery).getData();
            if (CollectionUtils.isNotEmpty(userDeliveryVoList)) {
                UserDelivery update = new UserDelivery();
                for (UserDeliveryVo userDeliveryVo : userDeliveryVoList) {
                    Integer defualt = userDeliveryVo.getFisDefualt();
                    //判断该用户是已经有默认地址，有的话则需要变更为非默认地址
                    if (defualt == 1) {
                        update.setFdeliveryUserId(userDeliveryVo.getFdeliveryUserId());
                        update.setFisDefualt(0);
                        Result<Integer> result = userDeliveryApi.updateNotNull(update);
                        if (!result.isSuccess()) {
                            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
                        }
                    }
                }
            }
        }
    }

    /**
     * @author lll
     * @version V1.0
     * @Description: 编辑用户收货地址
     * @Param: [userDeliveryDto]
     * @return: Result
     * @date 2019/9/20 13:49
     */
    @Override
    @GlobalTransactional
    public Result modifyUserAddress(UserDeliveryUpdateDto userDeliveryDto) {
        //校验用户id
        if (userDeliveryDto.getFuid() == null) {
            throw new BizException(MallExceptionCode.REQUIRED_PARAM_MISSING);
        }
        Integer isDefault = userDeliveryDto.getFisDefualt();
        UserDeliveryDto userDelivery = new UserDeliveryDto();
        userDelivery.setFuid(userDeliveryDto.getFuid());
        //判断此次新增地址是否设置为默认地址,是的话则需要校验原来的收货地址中是否有默认地址，并且更新默认地址
        if (!StringUtils.isEmpty(isDefault)) {
            this.checkDefualt(isDefault, userDelivery);
        }
        Result<Integer> resultTwo = userDeliveryApi.updateNotNull(dozerMapper.map(userDeliveryDto, UserDelivery.class));
        if (!resultTwo.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        return Result.success();
    }

    /**
     * @author lll
     * @version V1.0
     * @Description: 删除用户收货地址
     * @Param: [userDeliveryDto]
     * @return: Result
     * @date 2019/9/20 13:49
     */
    @Override
    @GlobalTransactional
    public Result deleteUserAddress(UserDeliveryDeleteDto userDeliveryDto) {
        if (!StringUtils.isEmpty(userDeliveryDto.getFdeliveryUserIds())) {
            //批量删除切割字符串
            String[] fuidsStr = userDeliveryDto.getFdeliveryUserIds().split(",");
            Integer[] fuids = (Integer[]) ConvertUtils.convert(fuidsStr, Integer.class);
            List<Integer> fuidList = Arrays.asList(fuids);
            UserDelivery userDelivery = new UserDelivery();
            //循环遍历批次删除
            for (Integer item : fuidList) {
                userDelivery.setFdeliveryUserId(Long.valueOf(item));
                userDelivery.setFisDelete(1);
                Result<Integer> result = userDeliveryApi.updateNotNull(userDelivery);
                if (!result.isSuccess()) {
                    throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }
        return Result.success();
    }

    /**
     * @author lll
     * @version V1.0
     * @Description: 收件地址查询区域列表
     * @Param: [cityRegionDto]
     * @return: Result<List       <       CityRegionVo>>
     * @date 2019/9/20 13:49
     */
    @Override
    public Result<List<CityRegionVo>> getCityRegionLis(CityRegionDto cityRegionDto) {
        Criteria<CityRegion, Object> criteria = Criteria.of(CityRegion.class);
        if (null != cityRegionDto.getFRegionType()) {
            criteria.andEqualTo(CityRegion::getFregionType, cityRegionDto.getFRegionType());
        }
        if (null != cityRegionDto.getFpRegionId()) {
            criteria.andEqualTo(CityRegion::getFpRegionId, cityRegionDto.getFpRegionId());
        }
        criteria.fields(CityRegion::getFregionId, CityRegion::getFcrName);
        Result<List<CityRegion>> list = cityRegionApi.queryByCriteria(criteria);
        if (!list.isSuccess()) {
            throw new BizException(ResultStatus.INTERNAL_SERVER_ERROR);
        }
        List<CityRegionVo> listResult = dozerHolder.convert(list.getData(), CityRegionVo.class);
        return Result.success(listResult);
    }


    /**
     * @author kevin
     * @version V1.0
     * @Description: 查询省市区文件基本信息
     * @Param:
     * @return: Result<AddressFileInfoVo>
     * @date 2019/10/28 14:59
     */
    @Override
    public AddressFileInfoVo getAddressFileInfo() {
        AddressFileInfoVo info = new AddressFileInfoVo();
        info.setFileAddress(url);
        info.setFileName(fileName);
        String jsonString = FileUtil.getRemoteFile(versionUrl);
        if (!StringUtils.isEmpty(jsonString)) {
            JSONObject  jsonObject = JSON.parseObject(jsonString);
            if (jsonObject.get("version") != null) {
                info.setFileVersion(String.valueOf(jsonObject.get("version")));
            }
        }
        return info;
    }

}
