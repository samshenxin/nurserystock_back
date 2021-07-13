package kd.bos.asset.business;


import kd.bos.entity.*;
import kd.bos.dataentity.metadata.dynamicobject.*;
import kd.bos.orm.query.*;
import java.math.*;
import java.util.*;
import kd.bos.servicehelper.*;
import kd.bos.dataentity.entity.*;
import kd.fi.fa.business.calc.*;

public class LeaseContractToFinCardGenerate extends AbstractFinCardGenerate
{
    @Override
    protected List<DynamicObject> generate(final List<DynamicObject> dynamicInventory, final Map<Object, DynamicObject[]> orgAssetbooksMap, final MainEntityType finCardType, final DynamicObjectType billHeadType, final Long realCardTableId, final Map<Object, Object> finCardAndrealCard) {
        final List<DynamicObject> finCards = this.getFinCardDynamicObject(dynamicInventory, orgAssetbooksMap, finCardType, billHeadType, realCardTableId, finCardAndrealCard);
        final Map<Long, BigDecimal> map = this.getOriginalVal(dynamicInventory);
        final Map<String, DynamicObject> setValMap = new HashMap<String, DynamicObject>(16);
        for (final DynamicObject realCard : dynamicInventory) {
            final String number = (String)realCard.get("srcbillnumber");
            final QFilter realCardFilter = new QFilter("number", "=", (Object)number);
            final QFilter assetUnitFilter = new QFilter("assetunit", "=", (Object)realCard.getDynamicObject("assetunit").getLong("id"));
            final DynamicObject dynMonths = BusinessDataServiceHelper.loadSingle("fa_lease_contract", "depremonths", new QFilter[] { realCardFilter, assetUnitFilter });
            final DynamicObject dynConfirmDate = BusinessDataServiceHelper.loadSingle("fa_lease_contract", "initconfirmdate", new QFilter[] { realCardFilter, assetUnitFilter });
            setValMap.put("dynMonths", dynMonths);
            setValMap.put("dynConfirmDate", dynConfirmDate);
            this.setOriginalValue(finCards, map, setValMap);
        }
        return finCards;
    }
    
    private Map<Long, BigDecimal> getOriginalVal(final List<DynamicObject> realCards) {
        final Map<Long, BigDecimal> map = new HashMap<Long, BigDecimal>(realCards.size());
        for (final DynamicObject realCard : realCards) {
            final QFilter sourceBill = new QFilter("number", "=", (Object)realCard.getString("srcbillnumber"));
            final QFilter assetUnitFilter = new QFilter("assetunit", "=", realCard.getDynamicObject("assetunit").getPkValue());
            final DynamicObjectCollection totalamounts = QueryServiceHelper.query("fa_lease_contract", "leaseassets", new QFilter[] { sourceBill, assetUnitFilter });
            BigDecimal orginalVal = BigDecimal.ZERO;
            for (final DynamicObject totalamount : totalamounts) {
                orginalVal = orginalVal.add(totalamount.getBigDecimal("leaseassets"));
            }
            map.put((Long)realCard.getPkValue(), orginalVal);
        }
        return map;
    }
    
    private void setOriginalValue(final List<DynamicObject> finCards, final Map<Long, BigDecimal> map, final Map<String, DynamicObject> setValMap) {
        for (final DynamicObject finCard : finCards) {
            if (map.containsKey(finCard.getDynamicObject("realcard").get("id"))) {
                final BigDecimal originalVal = map.get(finCard.getDynamicObject("realcard").get("id"));
                this.setValToCard(finCard, "originalval", originalVal);
                this.setValToCard(finCard, "originalamount", originalVal);
                this.setValToCard(finCard, "preusingamount", setValMap.get("dynMonths").get("depremonths"));
                this.setValToCard(finCard, "finaccountdate", setValMap.get("dynConfirmDate").get("initconfirmdate"));
                final IObjWrapper objWrapper = new DynamicObjectWrapper(finCard);
                final DynamicObject dynObj = objWrapper.getDynamicObject();
                this.setValToCard(finCard, "networth", FinCardCalc.setNetWorth(objWrapper));
                this.setValToCard(finCard, "netamount", FinCardCalc.setNetAmount(objWrapper));
                final DynamicObject dynamicObject = dynObj.getDynamicObject("basecurrency");
                final int amtprecision = dynamicObject.getInt("amtprecision");
                try {
                    this.setValToCard(finCard, "preresidualval", FinCardCalc.setPreResidualVal(objWrapper, false).setScale(amtprecision, 4));
                }
                catch (Exception e) {
                    this.setValToCard(finCard, "preresidualval", new BigDecimal(0));
                }
            }
        }
    }
}

