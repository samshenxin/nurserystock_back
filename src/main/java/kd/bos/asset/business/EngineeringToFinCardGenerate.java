package kd.bos.asset.business;

import kd.bos.entity.*;
import kd.bos.dataentity.metadata.dynamicobject.*;
import java.math.*;
import kd.bos.orm.query.*;
import kd.bos.servicehelper.*;
import java.util.*;
import kd.bos.dataentity.entity.*;
import kd.fi.fa.business.calc.*;

public class EngineeringToFinCardGenerate extends AbstractFinCardGenerate
{
    @Override
    protected List<DynamicObject> generate(final List<DynamicObject> dynamicInventory, final Map<Object, DynamicObject[]> orgAssetbooksMap, final MainEntityType finCardType, final DynamicObjectType billHeadType, final Long realCardTableId, final Map<Object, Object> finCardAndrealCard) {
        final List<DynamicObject> finCards = this.getFinCardDynamicObject(dynamicInventory, orgAssetbooksMap, finCardType, billHeadType, realCardTableId, finCardAndrealCard);
        final Map<Long, BigDecimal> map = this.getOriginalVal(dynamicInventory);
        this.setOriginalValue(finCards, map);
        return finCards;
    }
    
    private Map<Long, BigDecimal> getOriginalVal(final List<DynamicObject> realCards) {
        final Map<Long, BigDecimal> map = new HashMap<Long, BigDecimal>(realCards.size());
        for (final DynamicObject realCard : realCards) {
            final QFilter sourceBill = new QFilter("billno", "=", (Object)realCard.getString("srcbillnumber"));
            final QFilter assetUnitFilter = new QFilter("assetunit", "=", realCard.getDynamicObject("assetunit").getPkValue());
            final DynamicObjectCollection totalamounts = QueryServiceHelper.query("fa_engineeringbill", "assetsentry.totalamount", new QFilter[] { sourceBill, assetUnitFilter });
            BigDecimal orginalVal = BigDecimal.ZERO;
            for (final DynamicObject totalamount : totalamounts) {
                orginalVal = orginalVal.add(totalamount.getBigDecimal("assetsentry.totalamount"));
            }
            map.put((Long)realCard.getPkValue(), orginalVal);
        }
        return map;
    }
    
    private void setOriginalValue(final List<DynamicObject> finCards, final Map<Long, BigDecimal> map) {
        for (final DynamicObject finCard : finCards) {
            if (map.containsKey(finCard.getDynamicObject("realcard").get("id"))) {
                final BigDecimal originalVal = map.get(finCard.getDynamicObject("realcard").get("id"));
                this.setValToCard(finCard, "originalval", originalVal);
                this.setValToCard(finCard, "originalamount", originalVal);
                final IObjWrapper objWrapper = new DynamicObjectWrapper(finCard);
                this.setValToCard(finCard, "networth", FinCardCalc.setNetWorth(objWrapper));
                this.setValToCard(finCard, "netamount", FinCardCalc.setNetAmount(objWrapper));
                this.setValToCard(finCard, "preresidualval", FinCardCalc.setPreResidualVal(objWrapper, false));
            }
        }
    }
}

