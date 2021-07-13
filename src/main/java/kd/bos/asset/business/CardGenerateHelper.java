package kd.bos.asset.business;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.operate.result.IOperateInfo;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.property.EntryProp;
import kd.bos.exception.KDBizException;
import kd.bos.exception.KDException;
import kd.bos.orm.query.QFilter;
import kd.bos.orm.util.CollectionUtils;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.workflow.engine.impl.util.CollectionUtil;
import kd.bos.asset.business.*;
import kd.fi.fa.business.SourceFlagEnum;
import kd.fi.fa.business.cardgenerate.IRealCardGenerate;
import kd.fi.fa.business.errorcode.FinCardErrorCode;
import kd.fi.fa.business.utils.FaCommonUtils;
import kd.fi.fa.common.util.Fa;

public class CardGenerateHelper
{
    public static final Object[] generateRealCard(final DynamicObject srcBill) {
        IRealCardGenerate generate = null;
        final DynamicObjectType dt = srcBill.getDynamicObjectType();
        if ("fa_dispatch_in".equals(dt.getName()) || "fa_dispatch".equals(dt.getName())) {
            generate = new DispatchToRealCardGenerate(srcBill.getPkValue());
            return generate.generateRealCard();
        }
        throw new KDException(FinCardErrorCode.FINCARD_GENERATE_ERROR, new Object[0]);
    }
    
    public static void generateFinCard(final List<DynamicObject> objects) {
        final MainEntityType finCardType = EntityMetadataCache.getDataEntityType("fa_card_fin");
        final DynamicObjectType billHeadType = ((EntryProp)finCardType.getProperty("billhead_lk")).getDynamicCollectionItemPropertyType();
        final Long realCardTableId = EntityMetadataCache.loadTableDefine("fa_card_real", "fa_card_real").getTableId();
        AbstractFinCardGenerate generate = null;
        final List<DynamicObject> dynamicAdd = new ArrayList<DynamicObject>();
        final List<DynamicObject> dynamicPurchase = new ArrayList<DynamicObject>();
        final List<DynamicObject> dynamicDispatch = new ArrayList<DynamicObject>();
        final List<DynamicObject> dynamicInventory = new ArrayList<DynamicObject>();
        final List<DynamicObject> dynamicEnginner = new ArrayList<DynamicObject>();
        final List<DynamicObject> dynamicLease = new ArrayList<DynamicObject>();
        final List<DynamicObject> callbackFincards = new ArrayList<DynamicObject>();
        final Map<Object, DynamicObject[]> orgAssetbooksMap = new HashMap<Object, DynamicObject[]>();
        for (final DynamicObject dynamicObject : objects) {
            DynamicObject[] assetBooks = null;
            final DynamicObject org = dynamicObject.getDynamicObject("org");
            if (orgAssetbooksMap.get(org.getPkValue()) == null) {
                assetBooks = getAssetBooksByOrg(org.getPkValue());
                orgAssetbooksMap.put(org.getPkValue(), assetBooks);
            }
            else {
                assetBooks = orgAssetbooksMap.get(org.getPkValue());
            }
            final Object sourceFlag = dynamicObject.get("sourceflag");
            if (FaCommonUtils.equals(SourceFlagEnum.ADD, sourceFlag) || FaCommonUtils.equals(SourceFlagEnum.IMPORT, sourceFlag) || FaCommonUtils.equals(SourceFlagEnum.INITIAL, sourceFlag)) {
                dynamicAdd.add(dynamicObject);
            }
            else if (FaCommonUtils.equals(SourceFlagEnum.PURCHASE, sourceFlag)) {
                dynamicPurchase.add(dynamicObject);
            }
            else if (FaCommonUtils.equals(SourceFlagEnum.DISPATCH, sourceFlag)) {
                dynamicDispatch.add(dynamicObject);
            }
            else if (FaCommonUtils.equals(SourceFlagEnum.INVENTORYPROFIT, sourceFlag)) {
                dynamicInventory.add(dynamicObject);
            }
            else if (FaCommonUtils.equals(SourceFlagEnum.ENGINEERINGTRANS, sourceFlag)) {
                dynamicEnginner.add(dynamicObject);
            }
            else {
                if (!FaCommonUtils.equals(SourceFlagEnum.LEASECONTRACT, sourceFlag)) {
                    throw new KDException(FinCardErrorCode.FINCARD_GENERATE_ERROR, new Object[0]);
                }
                dynamicLease.add(dynamicObject);
            }
        }
        final Map<Object, Object> finCardAndrealCard = new HashMap<Object, Object>();
        if (CollectionUtil.isNotEmpty((Collection)dynamicAdd)) {
            generate = new AddCardToFinCardGenerate();
            callbackFincards.addAll(generate.generateDynamicFinCard(dynamicAdd, orgAssetbooksMap, finCardType, billHeadType, realCardTableId, finCardAndrealCard));
        }
        if (CollectionUtil.isNotEmpty((Collection)dynamicPurchase)) {
            generate = new PurchaseToFinCardGenerate();
            callbackFincards.addAll(generate.generateDynamicFinCard(dynamicPurchase, orgAssetbooksMap, finCardType, billHeadType, realCardTableId, finCardAndrealCard));
        }
        if (CollectionUtil.isNotEmpty((Collection)dynamicDispatch)) {
            generate = new DispatchToFinCardGenerate();
            callbackFincards.addAll(generate.generateDynamicFinCard(dynamicDispatch, orgAssetbooksMap, finCardType, billHeadType, realCardTableId, finCardAndrealCard));
        }
        if (CollectionUtil.isNotEmpty((Collection)dynamicInventory)) {
            generate = new InventoryProfitToFinCardGenerate();
            callbackFincards.addAll(generate.generateDynamicFinCard(dynamicInventory, orgAssetbooksMap, finCardType, billHeadType, realCardTableId, finCardAndrealCard));
        }
        if (CollectionUtil.isNotEmpty((Collection)dynamicEnginner)) {
            generate = new EngineeringToFinCardGenerate();
            callbackFincards.addAll(generate.generateDynamicFinCard(dynamicEnginner, orgAssetbooksMap, finCardType, billHeadType, realCardTableId, finCardAndrealCard));
        }
        if (CollectionUtil.isNotEmpty((Collection)dynamicLease)) {
            generate = new LeaseContractToFinCardGenerate();
            callbackFincards.addAll(generate.generateDynamicFinCard(dynamicLease, orgAssetbooksMap, finCardType, billHeadType, realCardTableId, finCardAndrealCard));
        }
        final OperateOption operateOption = OperateOption.create();
        operateOption.setVariableValue("genfincard", "true");
        final OperationResult result = OperationServiceHelper.executeOperate("save", "fa_card_fin", (DynamicObject[])callbackFincards.toArray(new DynamicObject[0]), operateOption);
        if (!result.isSuccess()) {
            final List<IOperateInfo> allError = (List<IOperateInfo>)result.getAllErrorOrValidateInfo();
            result.getAllErrorInfo();
            final StringBuilder errorInfo = new StringBuilder();
            if (!CollectionUtils.isEmpty((Collection)allError)) {
                for (final IOperateInfo in : allError) {
                    errorInfo.append(in.getMessage());
                }
            }
            final String msg = new LocaleString(String.format("%s%s-%s", ResManager.loadKDString("\u751f\u6210\u8d22\u52a1\u5361\u7247\u5931\u8d25,\u5ba1\u6838\u4e8b\u52a1\u5931\u8d25", "CardGenerateHelper_0", "fi-fa-business", new Object[0]), result.getMessage(), errorInfo.toString())).toString();
            throw new KDBizException(msg);
        }
    }
    
    private static DynamicObject[] getAssetBooksByOrg(final Object orgPK) {
        final String sic = Fa.join(",", new String[] { "id", "depreuse", "org", "ismainbook", "basecurrency", "periodtype", "depresystem", "curperiod", "isgroupbook" });
        final QFilter[] filters = new QFilter("org.id", "=", orgPK).toArray();
        final DynamicObject[] assetBooks = BusinessDataServiceHelper.load("fa_assetbook", sic, filters);
        return assetBooks;
    }
}

