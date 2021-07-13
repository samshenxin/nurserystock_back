package kd.bos.asset.business;

import kd.bos.entity.*;
import kd.bos.dataentity.metadata.dynamicobject.*;
import kd.bos.dataentity.entity.*;
import kd.fi.fa.business.utils.*;
import kd.bos.dataentity.resource.*;
import kd.bos.exception.*;
import java.util.*;
import kd.bos.orm.query.*;
import kd.bos.servicehelper.*;
import kd.fi.fa.business.calc.*;
import java.math.*;
import kd.bos.logging.*;

public class PurchaseToFinCardGenerate extends AbstractFinCardGenerate
{
    private static final Log log;
    private static final String BUILDWAY_ONE = "1";
    private static final String BUILDWAY_TWO = "2";
    private static final String BUILDWAY_THREE = "3";
    
    @Override
    protected List<DynamicObject> generate(final List<DynamicObject> realCardPurchases, final Map<Object, DynamicObject[]> orgAssetbooksMap, final MainEntityType finCardType, final DynamicObjectType billHeadType, final Long realCardTableId, final Map<Object, Object> finCardAndrealCard) {
        final List<DynamicObject> finCards = this.getFinCardDynamicObject(realCardPurchases, orgAssetbooksMap, finCardType, billHeadType, realCardTableId, finCardAndrealCard);
        final int size = realCardPurchases.size();
        final List<Object> sourceBillNumbers = new ArrayList<Object>(size);
        final Map<Object, Object> realCardAndPurchase = new HashMap<Object, Object>(size);
        final Map<Object, Object> realCardAndEntries = new HashMap<Object, Object>(size);
        final Map<Object, Integer> realCardSourceentrysplitseq = new HashMap<Object, Integer>(size);
        final Map<Object, DynamicObjectCollection> assetentriesMap = new HashMap<Object, DynamicObjectCollection>();
        for (final DynamicObject realCard : realCardPurchases) {
            sourceBillNumbers.add(realCard.get("srcbillnumber"));
            realCardAndPurchase.put(realCard.getPkValue(), realCard.get("srcbillnumber"));
            realCardAndEntries.put(realCard.getPkValue(), realCard.get("sourceentryid"));
            realCardSourceentrysplitseq.put(realCard.getPkValue(), realCard.getInt("sourceentrysplitseq"));
        }
        final Map<Object, Object> purchaseBill = new HashMap<Object, Object>();
        final Map<Object, DynamicObject> purchaseEntity = this.getPurchaseEntity(sourceBillNumbers, "fa_purchasebill", assetentriesMap, purchaseBill);
        final Date routeTime = new Date();
        PurchaseToFinCardGenerate.log.info("\u5b9e\u7269\u5361\u7247\u5ba1\u6838\u91c7\u8d2d\u8f6c\u56fa\u5faa\u73af\u5f00\u59cb\uff1a " + FaPerformanceMonitorUtils.getNowTime(routeTime));
        for (final DynamicObject finCard : finCards) {
            final Object realCardId = finCardAndrealCard.get(finCard.get("number") + "" + finCard.get("depreuse_id"));
            final Object sourceBillNumber = realCardAndPurchase.get(realCardId);
            final Object buildway = purchaseBill.get(sourceBillNumber);
            final DynamicObject assetInfo = purchaseEntity.get(realCardAndEntries.get(realCardId));
            if (assetInfo == null) {
                throw new KDBizException(ResManager.loadKDString("\u627e\u4e0d\u5230\u5bf9\u5e94\u7684\u91c7\u8d2d\u8f6c\u56fa\u8d44\u4ea7\u4fe1\u606f\uff0c\u8bf7\u786e\u8ba4\u91c7\u8d2d\u8f6c\u56fa-\u5b9e\u7269\u5361\u7247BOTP\u7684\u201c\u6765\u6e90\u5206\u5f55\u201d\u5b57\u6bb5\u914d\u7f6e\u662f\u5426\u6b63\u786e", "PurchaseToFinCardGenerate_2", "fi-fa-business", new Object[0]));
            }
            final DynamicObjectCollection assetentry = assetentriesMap.get(realCardAndPurchase.get(realCardId));
            final boolean isdeductiontax = assetInfo.getBoolean("isdeductiontax");
            final int assetqty = Double.valueOf(assetInfo.get("assetqty").toString()).intValue();
            if ("1".equals(buildway)) {
                this.splitByEntriesInfo(finCard, assetInfo, isdeductiontax, assetqty);
            }
            else if ("2".equals(buildway)) {
                this.splitByCount(finCard, assetInfo, realCardSourceentrysplitseq.get(realCardId), isdeductiontax, assetqty);
            }
            else {
                if (!"3".equals(buildway)) {
                    throw new KDException(new ErrorCode("UNKNOWN_ERR", ResManager.loadKDString("\u6ca1\u6709\u5bf9\u5e94\u7684\u5efa\u5361\u65b9\u5f0f", "PurchaseToFinCardGenerate_0", "fi-fa-business", new Object[0])), new Object[0]);
                }
                this.splitByBill(finCard, assetentry, isdeductiontax, assetqty);
            }
        }
        return finCards;
    }
    
    private Map<Object, DynamicObject> getPurchaseEntity(final List<Object> sourceBillNumbers, final String entityName, final Map<Object, DynamicObjectCollection> assetentries, final Map<Object, Object> purchaseBill) {
        final QFilter[] filter = new QFilter("billno", "in", (Object)sourceBillNumbers).toArray();
        final Map<Object, DynamicObject> purchases = (Map<Object, DynamicObject>)BusinessDataServiceHelper.loadFromCache(entityName, filter);
        final Map<Object, DynamicObject> srcBills = new HashMap<Object, DynamicObject>();
        for (final Map.Entry<Object, DynamicObject> entry : purchases.entrySet()) {
            final DynamicObjectCollection entries = entry.getValue().getDynamicObjectCollection("assetsentry");
            assetentries.put(entry.getValue().get("billno"), entries);
            purchaseBill.put(entry.getValue().get("billno"), entry.getValue().get("buildway"));
            for (final DynamicObject entryE : entries) {
                srcBills.put(entryE.getPkValue(), entryE);
            }
        }
        return srcBills;
    }
    
    private void splitByBill(final DynamicObject finCard, final DynamicObjectCollection assetentry, final boolean isDeductionTax, final int assetqty) {
        this.sumAmountByAllEntries(assetentry);
        final DynamicObject assetInfo = (DynamicObject)assetentry.get(0);
        this.setFinInfo(assetInfo, finCard, isDeductionTax, assetqty);
    }
    
    private void splitByCount(final DynamicObject finCard, final DynamicObject assetInfo, final int sourceentrysplitseq, final boolean isDeductionTax, final int assetqty) {
        final BigDecimal[] lastDiff = this.splitAmountByQty(assetInfo, assetqty);
        if (sourceentrysplitseq == assetqty) {
            this.setFinInfoByLastDiff(lastDiff, finCard, isDeductionTax, 1);
        }
        else {
            this.setFinInfo(assetInfo, finCard, isDeductionTax, assetqty);
        }
    }
    
    private void splitByEntriesInfo(final DynamicObject finCard, final DynamicObject assetInfo, final boolean isDeductionTax, final int assetqty) {
        this.setFinInfo(assetInfo, finCard, isDeductionTax, 1);
    }
    
    private void setFinInfo(final DynamicObject assetInfo, final DynamicObject finCard, final boolean isDeductionTax, final Object assetqty) {
        final Object notaxamount = assetInfo.get("notaxamount");
        final Object taxamount = assetInfo.get("taxamount");
        final Object totalamount = assetInfo.get("totalamount");
        this.setFinCardInfo(finCard, notaxamount, taxamount, totalamount, isDeductionTax, assetqty);
    }
    
    private void setFinInfoByLastDiff(final BigDecimal[] lastDiff, final DynamicObject finCard, final boolean isDeductionTax, final int assetqty) {
        final Object notaxamount = lastDiff[0];
        final Object taxamount = lastDiff[1];
        final Object totalamount = lastDiff[2];
        this.setFinCardInfo(finCard, notaxamount, taxamount, totalamount, isDeductionTax, assetqty);
    }
    
    private void setFinCardInfo(final DynamicObject finCard, final Object notaxamount, final Object taxamount, final Object totalamount, final boolean isDeductionTax, final Object assetQty) {
        final BigDecimal splitNoTaxAmount = this.divide(notaxamount, (int)assetQty);
        final BigDecimal splitTotalamount = this.divide(totalamount, (int)assetQty);
        if (isDeductionTax) {
            this.setValToCard(finCard, "originalamount", splitNoTaxAmount);
            this.setValToCard(finCard, "originalval", splitNoTaxAmount);
            this.setValToCard(finCard, "incometax", ((BigDecimal)taxamount).divide(BigDecimal.valueOf((int)assetQty), 2, 4));
        }
        else {
            this.setValToCard(finCard, "originalamount", splitTotalamount);
            this.setValToCard(finCard, "originalval", splitTotalamount);
            this.setValToCard(finCard, "incometax", BigDecimal.ZERO);
        }
        final IObjWrapper objWrapper = new DynamicObjectWrapper(finCard);
        this.setValToCard(finCard, "networth", FinCardCalc.setNetWorth(objWrapper));
        this.setValToCard(finCard, "netamount", FinCardCalc.setNetAmount(objWrapper));
        this.setValToCard(finCard, "preresidualval", FinCardCalc.setPreResidualVal(objWrapper, false));
    }
    
    private BigDecimal[] splitAmountByQty(final DynamicObject assetInfo, final int assetqty) {
        final Object notaxamount = assetInfo.get("notaxamount");
        final Object taxamount = assetInfo.get("taxamount");
        final Object totalamount = assetInfo.get("totalamount");
        final BigDecimal splitNoTaxAmount = this.divide(notaxamount, assetqty);
        final BigDecimal splitTaxamount = this.divide(taxamount, assetqty);
        final BigDecimal splitTotalamount = this.divide(totalamount, assetqty);
        final BigDecimal lastDiffNoTaxAmount = this.getLastDiff(notaxamount, splitNoTaxAmount, assetqty);
        final BigDecimal lastDiffTaxamount = this.getLastDiff(taxamount, splitTaxamount, assetqty);
        final BigDecimal lastDiffTotalamount = this.getLastDiff(totalamount, splitTotalamount, assetqty);
        final BigDecimal[] lastDiff = { lastDiffNoTaxAmount, lastDiffTaxamount, lastDiffTotalamount };
        return lastDiff;
    }
    
    private BigDecimal getLastDiff(final Object total, final BigDecimal amount, final int assetqty) {
        BigDecimal lastDiff = null;
        if (assetqty > 1) {
            lastDiff = this.getBigDecimal(total).subtract(amount.multiply(this.getBigDecimal(assetqty - 1)));
        }
        else {
            lastDiff = this.getBigDecimal(total);
        }
        return lastDiff;
    }
    
    private BigDecimal divide(final Object obj, final int i) {
        final BigDecimal d1 = this.getBigDecimal(obj);
        final BigDecimal d2 = this.getBigDecimal(i);
        if (BigDecimal.ZERO.equals(d1) || BigDecimal.ZERO.equals(d2)) {
            throw new KDException(new ErrorCode("UNKNOWN_ERR", ResManager.loadKDString("\u975e\u6cd5\u7684\u53c2\u6570:(", "PurchaseToFinCardGenerate_1", "fi-fa-business", new Object[0]) + d1 + "\u00f7" + d2 + ")"), new Object[0]);
        }
        return d1.divide(d2, 2, RoundingMode.HALF_DOWN);
    }
    
    private BigDecimal add(final BigDecimal total, final Object obj) {
        return total.add(this.getBigDecimal(obj));
    }
    
    private void sumAmountByAllEntries(final DynamicObjectCollection assetentry) {
        BigDecimal totalNoTax = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalTotalAmount = BigDecimal.ZERO;
        int totalqty = 0;
        for (final DynamicObject assetInfo : assetentry) {
            final Object notaxamount = assetInfo.get("notaxamount");
            final Object taxamount = assetInfo.get("taxamount");
            final Object totalamount = assetInfo.get("totalamount");
            final Object assetqty = assetInfo.get("assetqty");
            totalNoTax = this.add(totalNoTax, notaxamount);
            totalTax = this.add(totalTax, taxamount);
            totalTotalAmount = this.add(totalTotalAmount, totalamount);
            totalqty += Integer.parseInt(assetqty.toString());
        }
        final DynamicObject assetInfo2 = (DynamicObject)assetentry.get(0);
        assetInfo2.set("notaxamount", (Object)totalNoTax);
        assetInfo2.set("taxamount", (Object)totalTax);
        assetInfo2.set("totalamount", (Object)totalTotalAmount);
        assetInfo2.set("assetqty", (Object)totalqty);
    }
    
    static {
        log = LogFactory.getLog((Class)PurchaseToFinCardGenerate.class);
    }
}
