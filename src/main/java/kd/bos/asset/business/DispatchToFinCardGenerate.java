package kd.bos.asset.business;

import kd.bos.entity.*;
import kd.bos.dataentity.metadata.dynamicobject.*;
import java.util.*;
import kd.bos.orm.query.*;
import kd.bos.entity.validate.*;
import kd.bos.dataentity.entity.*;
import kd.fi.fa.common.util.*;
import kd.fi.fa.business.utils.*;
import kd.bos.servicehelper.*;
import java.math.*;
import kd.fi.fa.business.constants.*;
import kd.bos.dataentity.resource.*;
import kd.bos.exception.*;

public class DispatchToFinCardGenerate extends AbstractFinCardGenerate
{
    private static final String[] SELECT_CLEAR_FIELDS;
    
    @Override
    protected List<DynamicObject> generate(final List<DynamicObject> dynamicDispatch, final Map<Object, DynamicObject[]> orgAssetbooksMap, final MainEntityType finCardType, final DynamicObjectType billHeadType, final Long realCardTableId, final Map<Object, Object> finCardAndrealCard) {
        final List<DynamicObject> finCards = this.getFinCardDynamicObject(dynamicDispatch, orgAssetbooksMap, finCardType, billHeadType, realCardTableId, finCardAndrealCard);
        final int size = dynamicDispatch.size();
        final List<Object> srdIds = new ArrayList<Object>(size);
        final List<Object> srcbillNumbers = new ArrayList<Object>(size);
        final Map<Object, Object> realCardAndSrcCard = new HashMap<Object, Object>(size);
        final Map<Object, Object> realcarAndSourcenumber = new HashMap<Object, Object>(size);
        final Map<Object, Object> realcarAndSourceId = new HashMap<Object, Object>(size);
        for (final DynamicObject realCard : dynamicDispatch) {
            srdIds.add(realCard.get("sourceentryid"));
            srcbillNumbers.add(realCard.get("srcbillnumber"));
            realcarAndSourcenumber.put(realCard.getPkValue(), realCard.get("srcbillnumber"));
            realCardAndSrcCard.put(realCard.getPkValue(), realCard.get("sourceentryid"));
            realcarAndSourceId.put(realCard.getPkValue(), realCard.get("srcbillid"));
        }
        final Map<Object, Object> dispatchBills = this.getSrcBill(srcbillNumbers);
        final Map<Object, DynamicObject> srcFinCards = this.getSrcFinCards(srdIds);
        for (final DynamicObject finCard : finCards) {
            this.setFinInfo(finCard, finCardAndrealCard, realCardAndSrcCard, srcFinCards, dispatchBills, realcarAndSourcenumber, realcarAndSourceId);
        }
        return finCards;
    }
    
    private void setFinInfo(final DynamicObject finCard, final Map<Object, Object> finCardAndrealCard, final Map<Object, Object> realCardAndSrcCard, final Map<Object, DynamicObject> srcFinCards, final Map<Object, Object> dispatchBills, final Map<Object, Object> realcarAndSourcenumber, final Map<Object, Object> realcarAndSourceId) {
        final Object realCardId = finCardAndrealCard.get(finCard.get("number") + "" + finCard.get("depreuse_id"));
        final Object srcRealCardId = realCardAndSrcCard.get(realCardId);
        final DynamicObject srcFinCard = srcFinCards.get(srcRealCardId + "" + finCard.get("depreuse_id"));
        final Long baseCurrencyId = srcFinCard.getDynamicObject("basecurrency").getLong("id");
        final Long oriCurrencyId = srcFinCard.getDynamicObject("currency").getLong("id");
        final DynamicObjectCollection currencyObjs = QueryServiceHelper.query("bd_currency", "id,amtprecision", new QFilter[] { new QFilter("id", "=", (Object)oriCurrencyId), new QFilter("id", "=", (Object)baseCurrencyId) });
        int oriScale = 0;
        int baseScale = 0;
        Long curId = null;
        for (final DynamicObject obj : currencyObjs) {
            curId = obj.getLong("id");
            if (oriCurrencyId.equals(curId)) {
                baseScale = obj.getInt("amtprecision");
            }
            if (oriCurrencyId.equals(curId)) {
                oriScale = obj.getInt("amtprecision");
            }
        }
        if (srcFinCard != null) {
            final BigDecimal currency_rate = new BigDecimal("1");
            final BigDecimal nowOriginalAmount = srcFinCard.getBigDecimal("originalamount");
            final BigDecimal nowOriginalVal = srcFinCard.getBigDecimal("originalval");
            this.setValToCard(finCard, "originalamount", this.multi(nowOriginalAmount, currency_rate, oriScale));
            this.setValToCard(finCard, "originalval", this.multi(nowOriginalVal, currency_rate, baseScale));
            this.setValToCard(finCard, "incometax", this.multi(srcFinCard.get("incometax"), currency_rate, baseScale));
            this.subtract(srcFinCard, "accumdepre", "monthdepre");
            this.subtract(srcFinCard, "addupyeardepre", "monthdepre");
            this.subtract(srcFinCard, "depredamount", "addidepreamount");
            this.setValToCard(finCard, "accumdepre", this.multi(srcFinCard.get("accumdepre"), currency_rate, baseScale));
            this.setValToCard(finCard, "addupyeardepre", this.multi(srcFinCard.get("addupyeardepre"), currency_rate, baseScale));
            this.setValToCard(finCard, "depredamount", srcFinCard.getInt("depredamount"));
            this.setValToCard(finCard, "preusingamount", srcFinCard.get("preusingamount"));
            this.setValToCard(finCard, "preresidualval", this.multi(srcFinCard.get("preresidualval"), currency_rate, baseScale));
            this.setValToCard(finCard, "decval", this.multi(srcFinCard.getBigDecimal("decval"), currency_rate, baseScale));
            this.setValToCard(finCard, "networth", this.multi(srcFinCard.get("networth"), currency_rate, baseScale));
            this.setValToCard(finCard, "netamount", this.multi(srcFinCard.get("netamount"), currency_rate, baseScale));
            this.setValToCard(finCard, "billstatus", BillStatus.A);
        }
    }
    
    private void subtract(final DynamicObject finCard, final String field1, final String field2) {
        finCard.set(field1, (Object)finCard.getBigDecimal(field1).subtract(finCard.getBigDecimal(field2)));
    }
    
    private Map<Object, Object> getSrcBill(final List<Object> sourceBillNumbers) {
        final String sic = Fa.join(",", new String[] { "billno,dispatchdate" });
        final QFilter[] filter = new QFilter("billno", "in", (Object)sourceBillNumbers).toArray();
        final DynamicObjectCollection srcRealCards = QueryServiceHelper.query("fa_dispatch", sic, filter);
        final Map<Object, Object> srcBills = new HashMap<Object, Object>();
        for (final DynamicObject doj : srcRealCards) {
            srcBills.put(doj.get("billno"), doj.get("dispatchdate"));
        }
        return srcBills;
    }
    
    private Map<Object, DynamicObject> getSrcFinCards(final List<Object> srcIds) {
        final String sic = Fa.join(",", new String[] { "id", "org", "depreuse_id", "realcard_id", "assetbook_id", "bizperiod", "basecurrency", "currency", "originalval", "originalamount", "originalamount", "puroriginalval", "incometax", "accumdepre", "puraccumdepre", "depredamount", "preusingamount", "preusingamount", "preresidualval", "decval", "networth", "netamount", "addupyeardepre", "monthdepre", "addidepreamount" });
        final QFilter filters1 = new QFilter("realcard_id", "in", (Object)srcIds);
        final QFilter filters2 = new QFilter("endperiod", "=", (Object)FaConstants.ENDPERIOD);
        final DynamicObject[] colls = BusinessDataServiceHelper.load("fa_card_fin", sic, new QFilter[] { filters1, filters2 });
        final Map<Object, DynamicObject> srcFinCards = new HashMap<Object, DynamicObject>(colls.length);
        for (final DynamicObject srcFinCard : colls) {
            srcFinCards.put(srcFinCard.get("realcard_id") + "" + srcFinCard.get("depreuse_id"), srcFinCard);
        }
        return srcFinCards;
    }
    
    private Object multi(final Object object, final BigDecimal currency_rate, final int scale) {
        return this.getBigDecimal(object).multiply(currency_rate).setScale(scale, RoundingMode.HALF_UP);
    }
    
    private DynamicObject getClearEntityForDepreData(final Long srcBillId, final String srcAssetNumber, final Long realCardId, final Long srcFinCardId) {
        final QFilter clearSourceBillIdFilter = new QFilter("srcbill", "=", (Object)srcBillId);
        final QFilter clearSourceFilter = new QFilter("clearsource", "=", (Object)FaClearSourceEnum.DISPATCH.name());
        final QFilter[] selectClearFilter = { clearSourceBillIdFilter, clearSourceFilter };
        final DynamicObject clearBill = BusinessDataServiceHelper.loadSingle("fa_clearbill", Fa.join((Object[])DispatchToFinCardGenerate.SELECT_CLEAR_FIELDS, ","), selectClearFilter);
        final String clearBillNo = clearBill.getString("billno");
        final DynamicObjectCollection clearEntity = clearBill.getDynamicObjectCollection("detail_entry");
        final String clearStatus = clearBill.getString("billstatus");
        if (!clearStatus.equals(BillStatus.C.name())) {
            throw new KDBizException(String.format(ResManager.loadKDString("\u8c03\u62e8\u751f\u6210\u7684\u5b9e\u7269\u5361\u7247\uff0c\u9700\u5148\u5ba1\u6838\u8c03\u51fa\u65b9\u7684\u6e05\u7406\u5355\u3010%s\u3011", "DispatchToFinCardGenerate_03", "fi-fa-business", new Object[0]), clearBillNo));
        }
        DynamicObject clearEntityFinal = null;
        for (final DynamicObject clearBillEntityDo : clearEntity) {
            final Long clearEntity_fincardId = clearBillEntityDo.getLong("fincard_id");
            final Long clearEntity_realcardId = clearBillEntityDo.getLong("realcard_id");
            if (realCardId == (long)clearEntity_realcardId && clearEntity_fincardId == (long)srcFinCardId) {
                clearEntityFinal = clearBillEntityDo;
                break;
            }
        }
        if (clearEntityFinal == null) {
            throw new KDBizException(String.format(ResManager.loadKDString("\u5728\u8c03\u51fa\u65b9\u7684\u6e05\u7406\u5355\u3010%s\u3011\u4e2d\u672a\u627e\u5230\u5361\u7247\u3010%s\u3011\u7684\u7d2f\u8ba1\u6298\u65e7\u4fe1\u606f\uff01", "DispatchToFinCardGenerate_02", "fi-fa-business", new Object[0]), clearBillNo, srcAssetNumber));
        }
        return clearEntityFinal;
    }
    
    static {
        SELECT_CLEAR_FIELDS = new String[] { "id", "billno", "billstatus", "detail_entry.realcard", "detail_entry.fincard", "detail_entry.addupdepre", "detail_entry.depredamount" };
    }
}

