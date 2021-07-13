package kd.bos.asset.business;

import java.math.*;
import kd.fi.fa.business.dao.factory.*;
import kd.bos.orm.query.*;
import java.util.*;
import kd.bos.dataentity.entity.*;
import kd.bos.entity.*;
import kd.bos.dataentity.metadata.dynamicobject.*;

public class InventoryProfitToFinCardGenerate extends AbstractFinCardGenerate
{
    private Map<Long, BigDecimal> getEstMoney(final List<DynamicObject> realCards) {
        final Map<Long, BigDecimal> map = new HashMap<Long, BigDecimal>(realCards.size());
        for (final DynamicObject realCard : realCards) {
            final String billno = realCard.getString("srcbillnumber");
            final long entryid = realCard.getLong("sourceentryid");
            final DynamicObject obj = FaBillDaoFactory.getInstance("fa_asset_countsheet").queryOne(new QFilter("billno", "=", (Object)billno));
            final DynamicObjectCollection entries = obj.getDynamicObjectCollection("countsheet_entry");
            for (final DynamicObject entry : entries) {
                if (entry.getPkValue().equals(entryid)) {
                    final DynamicObject targetEntry = entry;
                    final BigDecimal estMoney = targetEntry.getBigDecimal("estmoney");
                    map.put((Long)realCard.getPkValue(), estMoney);
                    break;
                }
            }
        }
        return map;
    }
    
    @Override
    protected List<DynamicObject> generate(final List<DynamicObject> dynamicInventory, final Map<Object, DynamicObject[]> orgAssetbooksMap, final MainEntityType finCardType, final DynamicObjectType billHeadType, final Long realCardTableId, final Map<Object, Object> finCardAndrealCard) {
        final List<DynamicObject> finCards = this.getFinCardDynamicObject(dynamicInventory, orgAssetbooksMap, finCardType, billHeadType, realCardTableId, finCardAndrealCard);
        final Map<Long, BigDecimal> map = this.getEstMoney(dynamicInventory);
        this.setFinEstMoney(finCards, map);
        return finCards;
    }
    
    private void setFinEstMoney(final List<DynamicObject> finCards, final Map<Long, BigDecimal> map) {
        for (final DynamicObject finCard : finCards) {
            if (map.containsKey(finCard.getDynamicObject("realcard").get("id"))) {
                final BigDecimal originalVal = map.get(finCard.getDynamicObject("realcard").get("id"));
                this.setValToCard(finCard, "originalval", originalVal);
                this.setValToCard(finCard, "originalamount", originalVal);
            }
        }
    }
}
