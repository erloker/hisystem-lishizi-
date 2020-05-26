package com.xgs.hisystem.service.impl;

import com.xgs.hisystem.config.Contants;
import com.xgs.hisystem.pojo.bo.PageRspBO;
import com.xgs.hisystem.pojo.entity.DrugEntity;
import com.xgs.hisystem.pojo.entity.DrugTypeEntity;
import com.xgs.hisystem.pojo.entity.EfficacyClassificationEntity;
import com.xgs.hisystem.pojo.vo.BaseResponse;
import com.xgs.hisystem.pojo.vo.drugStorage.DrugReqVO;
import com.xgs.hisystem.pojo.vo.drugStorage.DrugRspVO;
import com.xgs.hisystem.pojo.vo.drugStorage.DrugSearchReqVO;
import com.xgs.hisystem.repository.IDrugRepository;
import com.xgs.hisystem.repository.IDrugTypeRepository;
import com.xgs.hisystem.repository.IEfficacyClassificationRepository;
import com.xgs.hisystem.service.IDrugStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xgs
 * @date 2019-5-12
 * @description:
 */
@Service
public class DrugStoreServiceImpl implements IDrugStoreService {

    @Autowired
    private IDrugRepository iDrugRepository;
    @Autowired
    private IDrugTypeRepository iDrugTypeRepository;
    @Autowired
    private IEfficacyClassificationRepository iEfficacyClassificationRepository;

    @Override
    public BaseResponse<?> addNewDrug(DrugReqVO reqVO) {

        DrugEntity drug = new DrugEntity();
        drug.setName(reqVO.getName());
        drug.setDrugType(reqVO.getDrugType());
        drug.setEfficacyClassification(reqVO.getEfficacyClassification());
        drug.setLimitStatus(Integer.parseInt(reqVO.getLimitStatus()));
        drug.setManufacturer(reqVO.getManufacturer());
        drug.setSpecification(reqVO.getSpecification());
        drug.setUnit(reqVO.getUnit());
        drug.setPrice(Double.valueOf(reqVO.getPrice().toString()));
        drug.setWholesalePrice(Double.valueOf(reqVO.getWholesalePrice().toString()));
        drug.setStorageQuantity(Integer.parseInt(reqVO.getStorageQuantity()));
        drug.setProductionDate(reqVO.getProductionDate());
        drug.setQualityDate(reqVO.getQualityDate());

        try {
            iDrugRepository.saveAndFlush(drug);
            return BaseResponse.errormsg(Contants.user.SUCCESS);
        } catch (Exception e) {
            return BaseResponse.errormsg("系统异常，请稍后重试！");
        }
    }

    @Override
    public BaseResponse<?> addDrugType(String drugType) {

        DrugTypeEntity drugTypeEntityCheck = iDrugTypeRepository.findByDrugType(drugType);

        if (drugTypeEntityCheck != null) {
            return BaseResponse.errormsg("该剂型已存在！");
        }

        DrugTypeEntity drugTypeEntity = new DrugTypeEntity();
        drugTypeEntity.setDrugType(drugType);

        try {
            iDrugTypeRepository.saveAndFlush(drugTypeEntity);
            return BaseResponse.success(Contants.user.SUCCESS);
        } catch (Exception e) {
            return BaseResponse.success(Contants.user.FAIL);
        }
    }

    @Override
    public BaseResponse<?> addEfficacyClassification(String efficacyClassification) {

        EfficacyClassificationEntity efficacyClassificationEntityCheck = iEfficacyClassificationRepository.findByEfficacyClassification(efficacyClassification);

        if (efficacyClassificationEntityCheck != null) {
            return BaseResponse.errormsg("该药品功效已存在！");
        }

        EfficacyClassificationEntity efficacyClassificationEntity = new EfficacyClassificationEntity();
        efficacyClassificationEntity.setEfficacyClassification(efficacyClassification);

        try {
            iEfficacyClassificationRepository.saveAndFlush(efficacyClassificationEntity);
            return BaseResponse.success(Contants.user.SUCCESS);
        } catch (Exception e) {
            return BaseResponse.success(Contants.user.FAIL);
        }
    }

    @Override
    public List<String> getAllDrugType() {

        List<String> drugTypeList = new ArrayList<>();

        List<DrugTypeEntity> drugTypeEntityList = iDrugTypeRepository.findAll();

        drugTypeEntityList.forEach(drugTypeEntity -> {
            drugTypeList.add(drugTypeEntity.getDrugType());
        });
        return drugTypeList;
    }

    @Override
    public List<String> getAllEfficacyClassification() {

        List<String> efficacyClassificationList = new ArrayList<>();

        List<EfficacyClassificationEntity> efficacyClassificationEntityList = iEfficacyClassificationRepository.findAll();

        efficacyClassificationEntityList.forEach(efficacyClassification -> {
            efficacyClassificationList.add(efficacyClassification.getEfficacyClassification());
        });
        return efficacyClassificationList;
    }

    @Override
    public DrugRspVO getDrugInfor(String drug) {

        DrugEntity drugEntity = iDrugRepository.findByName(drug);
        if (StringUtils.isEmpty(drugEntity)) {
            return null;
        }

        DrugRspVO drugRspVO = new DrugRspVO();
        drugRspVO.setDrugType(drugEntity.getDrugType());
        drugRspVO.setEfficacyClassification(drugEntity.getEfficacyClassification());
        drugRspVO.setSpecification(drugEntity.getSpecification() + '/' + drugEntity.getUnit());
        drugRspVO.setManufacturer(drugEntity.getManufacturer());
        drugRspVO.setStorageQuantity(drugEntity.getStorageQuantity());
        return drugRspVO;
    }

    @Override
    public BaseResponse<?> addStorageQuantity(String drug, String addStorageQuantity) {

        if (StringUtils.isEmpty(drug)) {
            return BaseResponse.errormsg("请先选择添加的药品！");
        }
        if (StringUtils.isEmpty(addStorageQuantity)) {
            return BaseResponse.errormsg("请填写入库量！");
        }

        DrugEntity drugEntity = iDrugRepository.findByName(drug);
        if (StringUtils.isEmpty(drugEntity)) {
            return BaseResponse.errormsg("药品信息有误，请稍后重试！");
        }

        try {
            if (isNumer(addStorageQuantity)) {

                drugEntity.setStorageQuantity(drugEntity.getStorageQuantity() + Integer.parseInt(addStorageQuantity));

                iDrugRepository.saveAndFlush(drugEntity);
                return BaseResponse.success(Contants.user.SUCCESS);

            } else {
                return BaseResponse.success("库存量不能为非整数！");
            }
        } catch (Exception e) {
            return BaseResponse.errormsg("系统异常，请稍后重试！");
        }
    }

    /**
     * @author XueGuiSheng
     * @date 2020/2/20
     * @desciption 判断整数
     */
    private static Pattern NUMBER_PATTERN = Pattern.compile("^\\+?[1-9][0-9]*$");

    private static boolean isNumer(String str) {
        Matcher isNum = NUMBER_PATTERN.matcher(str);
        return isNum.matches();
    }

    @Override
    public PageRspBO<DrugRspVO> getAllDrug(DrugSearchReqVO reqVO) {

        Page<DrugEntity> page = iDrugRepository.findAll(new Specification<DrugEntity>() {
            @Override
            public Predicate toPredicate(Root<DrugEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicateList = new ArrayList<>();

                if (!StringUtils.isEmpty(reqVO.getDrug())) {
                    predicateList.add(cb.equal(root.get("name"), reqVO.getDrug()));
                }
                if (!StringUtils.isEmpty(reqVO.getDrugType_search())) {
                    predicateList.add(cb.equal(root.get("drugType"), reqVO.getDrugType_search()));
                }
                if (!StringUtils.isEmpty(reqVO.getEfficacyClassification_search())) {
                    predicateList.add(cb.equal(root.get("efficacyClassification"), reqVO.getEfficacyClassification_search()));
                }
                if (!StringUtils.isEmpty(reqVO.getLimitStatus_search())) {
                    if (reqVO.getLimitStatus_search().equals("0")) {
                        predicateList.add(cb.equal(root.get("limitStatus"), 0));
                    } else {
                        predicateList.add(cb.equal(root.get("limitStatus"), 1));
                    }

                }

                predicateList.add(cb.isNotNull(root.get("name")));
                query.where(predicateList.toArray(new Predicate[predicateList.size()]));
                return null;
            }
        }, PageRequest.of(reqVO.getPageNumber(), reqVO.getPageSize(), Sort.Direction.DESC, "createDatetime"));
        if (page == null) {
            return null;
        }
        List<DrugEntity> drugEntityList = page.getContent();
        List<DrugRspVO> drugRspVOList = new ArrayList<>();
        drugEntityList.forEach(drugEntity -> {
            DrugRspVO drugRspVO = new DrugRspVO();
            drugRspVO.setNum(String.valueOf(drugEntity.getNum()));
            drugRspVO.setName(drugEntity.getName());
            drugRspVO.setDrugType(drugEntity.getDrugType());
            drugRspVO.setEfficacyClassification(drugEntity.getEfficacyClassification());
            if (drugEntity.getLimitStatus() == 1) {
                drugRspVO.setLimitStatus("是");
            } else {
                drugRspVO.setLimitStatus("否");
            }
            drugRspVO.setManufacturer(drugEntity.getManufacturer());
            drugRspVO.setSpecification(drugEntity.getSpecification());
            drugRspVO.setUnit(drugEntity.getUnit());
            drugRspVO.setPrice(String.valueOf(drugEntity.getPrice()));
            drugRspVO.setWholesalePrice(String.valueOf(drugEntity.getWholesalePrice()));
            drugRspVO.setStorageQuantity(drugEntity.getStorageQuantity());
            drugRspVO.setProductionDate(drugEntity.getProductionDate());
            drugRspVO.setQualityDate(drugEntity.getQualityDate());
            drugRspVOList.add(drugRspVO);
        });
        PageRspBO pageRspBO = new PageRspBO();
        pageRspBO.setTotal(page.getTotalElements());
        pageRspBO.setRows(drugRspVOList);


        return pageRspBO;
    }

    @Override
    public BaseResponse<?> updateDrug(DrugReqVO reqVO) {

        DrugEntity drug = iDrugRepository.findByName(reqVO.getName());
        if (StringUtils.isEmpty(drug)) {
            return BaseResponse.errormsg("系统异常，请稍后重试！");
        }
        drug.setName(reqVO.getName());
        if (!StringUtils.isEmpty(reqVO.getDrugType())) {
            drug.setDrugType(reqVO.getDrugType());
        }
        if (!StringUtils.isEmpty(reqVO.getEfficacyClassification())) {
            drug.setEfficacyClassification(reqVO.getEfficacyClassification());
        }
        if (!StringUtils.isEmpty(reqVO.getLimitStatus())) {
            drug.setLimitStatus(Integer.parseInt(reqVO.getLimitStatus()));
        }
        drug.setManufacturer(reqVO.getManufacturer());
        drug.setSpecification(reqVO.getSpecification());
        drug.setUnit(reqVO.getUnit());
        drug.setPrice(Double.valueOf(reqVO.getPrice().toString()));
        drug.setWholesalePrice(Double.valueOf(reqVO.getWholesalePrice().toString()));
        drug.setProductionDate(reqVO.getProductionDate());
        drug.setQualityDate(reqVO.getQualityDate());

        try {
            iDrugRepository.saveAndFlush(drug);
            return BaseResponse.errormsg(Contants.user.SUCCESS);
        } catch (Exception e) {
            return BaseResponse.errormsg("系统异常，请稍后重试！");
        }
    }

    @Override
    public BaseResponse<?> deleteDrug(String drugName) {

        DrugEntity drugEntity = iDrugRepository.findByName(drugName);
        if (StringUtils.isEmpty(drugEntity)) {
            return BaseResponse.errormsg("系统异常，请稍后重试！");
        }

        try {
            iDrugRepository.delete(drugEntity);
            return BaseResponse.errormsg(Contants.user.SUCCESS);
        } catch (Exception e) {
            return BaseResponse.errormsg("系统异常，请稍后重试！");
        }
    }
}
