package kd.bos.asset.business;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.validate.BillStatus;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.fi.fa.business.cardgenerate.IFinCardGenerate;
import kd.fi.fa.business.dao.factory.BdPeriodDaoFactory;
import kd.fi.fa.business.utils.FaCommonUtils;
import kd.fi.fa.business.utils.FaConstants;
import kd.fi.fa.business.utils.PeriodUtil;
import kd.fi.fa.cache.FaBusinessImportCardThreadCacheUtil;
import kd.fi.fa.common.util.ContextUtil;
import kd.fi.fa.common.util.Fa;

public abstract class AbstractFinCardGenerate implements IFinCardGenerate
{
    private static final Log log;
    private Map<Object, DynamicObject> depresystemCache;
    private Map<Object, DynamicObject> defaultDateByBookCache;
    private Map<Object, DynamicObject> periodByDateMap;
    
    public AbstractFinCardGenerate() {
        this.depresystemCache = new HashMap<Object, DynamicObject>();
        this.defaultDateByBookCache = new HashMap<Object, DynamicObject>();
        this.periodByDateMap = new HashMap<Object, DynamicObject>();
    }
    
    protected abstract List<DynamicObject> generate(final List<DynamicObject> p0, final Map<Object, DynamicObject[]> p1, final MainEntityType p2, final DynamicObjectType p3, final Long p4, final Map<Object, Object> p5);
    
    @Override
    public List<DynamicObject> generateDynamicFinCard(final List<DynamicObject> dynamicAdd, final Map<Object, DynamicObject[]> orgAssetbooksMap, final MainEntityType finCardType, final DynamicObjectType billHeadType, final Long realCardTableId, final Map<Object, Object> finCardAndrealCard) {
        return this.generate(dynamicAdd, orgAssetbooksMap, finCardType, billHeadType, realCardTableId, finCardAndrealCard);
    }
    
    protected Map<Long, Boolean> getInnerMap(final List<DynamicObject> realCards) {
        final HashMap<Long, Boolean> innerCardMap = new HashMap<Long, Boolean>();
        final HashMap<Long, Long> realcardIds = new HashMap<Long, Long>();
        for (final DynamicObject realCard : realCards) {
            final DynamicObject supplier = realCard.getDynamicObject("supplier");
            DynamicObject bizpartner = null;
            if (supplier == null) {
                realcardIds.put((Long)realCard.getPkValue(), null);
                innerCardMap.put((Long)realCard.getPkValue(), false);
            }
            else {
                bizpartner = supplier.getDynamicObject("bizpartner");
                if (bizpartner == null) {
                    realcardIds.put((Long)realCard.getPkValue(), null);
                    innerCardMap.put((Long)realCard.getPkValue(), false);
                }
                else {
                    realcardIds.put((Long)realCard.getPkValue(), (Long)bizpartner.getPkValue());
                    innerCardMap.put((Long)realCard.getPkValue(), true);
                }
            }
        }
        AbstractFinCardGenerate.log.info(String.format("%s:%s", "\u5b9e\u7269\u5361\u7247-\u4f9b\u5e94\u5546-\u4f19\u4f34\u7684pkvalue:", realcardIds));
        final Collection<Long> bizpartnerIds = realcardIds.values();
        AbstractFinCardGenerate.log.info("\u6240\u6709\u7684\u4f19\u4f34\u7684pkvalue:" + Fa.join((Collection)bizpartnerIds, ","));
        final DynamicObject[] load;
        final DynamicObject[] bizpartners = load = BusinessDataServiceHelper.load("bd_bizpartner", "id,internal_company", new QFilter[] { new QFilter("id", "in", (Object)bizpartnerIds) });
        for (final DynamicObject bizpartner2 : load) {
            final boolean hasInner = bizpartner2.getDynamicObject("internal_company") != null;
            for (final Map.Entry<Long, Long> entry : realcardIds.entrySet()) {
                final Long mapkey = entry.getKey();
                final Long mapValue = entry.getValue();
                if (mapValue != null && !hasInner) {
                    AbstractFinCardGenerate.log.info("\u5b9e\u7269\u5361\u7247\uff1a" + mapkey + "\u6709\u5546\u52a1\u4f19\u4f34" + mapValue + " \u6ca1\u6709\u5bf9\u5e94\u4e1a\u52a1\u5355\u5143 internal_company");
                    innerCardMap.put(mapkey, false);
                }
            }
        }
        return innerCardMap;
    }
    
    protected List<DynamicObject> getFinCardDynamicObject(final List<DynamicObject> realCards, final Map<Object, DynamicObject[]> orgAssetbooksMap, final MainEntityType finCardType, final DynamicObjectType billHeadType, final Long realCardTableId, final Map<Object, Object> finCardAndrealCard) {
        final List<DynamicObject> finCards = new ArrayList<DynamicObject>();
        for (final DynamicObject realCard : realCards) {
            final DynamicObject org = realCard.getDynamicObject("org");
            if (org == null) {
                continue;
            }
            final DynamicObject[] assetBooks = orgAssetbooksMap.get(org.getPkValue());
            if (assetBooks == null) {
                continue;
            }
            if (assetBooks.length <= 0) {
                continue;
            }
            final Date nowTime = new Date();
            for (final DynamicObject assetBook : assetBooks) {
                final DynamicObject finCard = (DynamicObject)finCardType.createInstance();
                finCard.set("realcard", (Object)realCard);
                finCard.set("org", (Object)org);
                finCard.set("billno", realCard.get("billno"));
                finCard.set("number", realCard.get("number"));
                finCard.set("zsf_rfid", realCard.get("zsf_rfid"));
                finCard.set("assetbook", (Object)assetBook);
                finCard.set("depreuse", assetBook.get("depreuse"));
                finCard.set("billstatus", (Object)BillStatus.A);
                this.setDefaultInfo(assetBook, realCard, finCard, nowTime);
                final DynamicObjectCollection billHeads = finCard.getDynamicObjectCollection("billhead_lk");
                final DynamicObject billHead = new DynamicObject(billHeadType);
                billHead.set("billhead_lk_stableid", (Object)realCardTableId);
                billHead.set("billhead_lk_sbillid", realCard.getPkValue());
                billHead.set("billhead_lk_sid", realCard.getPkValue());
                billHead.set("seq", (Object)1);
                billHeads.add((DynamicObject)billHead);
                finCards.add(finCard);
                finCardAndrealCard.put(finCard.get("number") + "" + finCard.get("depreuse_id"), realCard.getPkValue());
            }
        }
        return finCards;
    }
    
    protected DynamicObject getAssetPolicyEntryByBook(final long depresystemId, final DynamicObject assetCat) {
        DynamicObject dyn = null;
        if (this.depresystemCache.get(depresystemId) != null) {
            dyn = this.depresystemCache.get(depresystemId);
        }
        else {
            dyn = BusinessDataServiceHelper.loadSingle((Object)depresystemId, (DynamicObjectType)EntityMetadataCache.getDataEntityType("fa_depresystem"));
            this.depresystemCache.put(depresystemId, dyn);
        }
        final DynamicObjectCollection entrysDOC = dyn.getDynamicObjectCollection("assetpolicy_entry");
        DynamicObject selectedRow = null;
        int index = 999;
        for (int i = 0; i < entrysDOC.size(); ++i) {
            final DynamicObject wholeRow = (DynamicObject)entrysDOC.get(i);
            final DynamicObject rowAssetCat = (DynamicObject)wholeRow.get("assetcat");
            final String cardAssetNumber = assetCat.getString("longnumber");
            final String rowAssetCatNumber = rowAssetCat.getString("longnumber");
            if (cardAssetNumber.indexOf(rowAssetCatNumber) == 0) {
                final int differenceLength = cardAssetNumber.length() - rowAssetCatNumber.length();
                if (differenceLength == 0) {
                    selectedRow = wholeRow;
                    break;
                }
                if (differenceLength < index) {
                    index = differenceLength;
                    selectedRow = wholeRow;
                }
            }
        }
        return selectedRow;
    }
    
    protected void setValToCard(final DynamicObject card, final String property, final Object value) {
        card.set(property, value);
    }
    
    protected BigDecimal getBigDecimal(final Object obj) {
        return FaCommonUtils.getBigDecimal(obj);
    }
    
    private void setDefaultInfo(final DynamicObject assetBook, final DynamicObject realCard, final DynamicObject finCard, final Date nowTime) {
        finCard.set(FaCommonUtils.addString_id("realcard"), realCard.getPkValue());
        finCard.set("billno", realCard.get("billno"));
        finCard.set("zsf_rfid", realCard.get("zsf_rfid"));
        finCard.set("assetbook", (Object)assetBook);
        finCard.set("depredept", realCard.get("headusedept"));
        finCard.set("isneeddepre", (Object)"1");
        finCard.set("modifytime", (Object)nowTime);
        finCard.set("createtime", (Object)nowTime);
        finCard.set("endperiod_id", (Object)FaConstants.ENDPERIOD);
        finCard.set("creator_id", (Object)ContextUtil.getUserId());
        final DynamicObject assetcat = realCard.getDynamicObject("assetcat");
        finCard.set("assetcat", (Object)assetcat);
        this.setBookRelatedInfo(realCard, finCard, assetBook, assetcat);
    }
    
    private void setBookRelatedInfo(final DynamicObject realCard, final DynamicObject finCard, final DynamicObject assetBook, final DynamicObject assetcat) {
        finCard.set("basecurrency", assetBook.get("basecurrency"));
        finCard.set("currency", assetBook.get("basecurrency"));
        finCard.set("currencyrate", (Object)new BigDecimal(1).toString());
        if (finCard.get("finaccountdate") == null) {
            final Date realAccountDate = (Date)realCard.get("realaccountdate");
            final Date defaultDateByBook = this.getDefaultDateByBook(assetBook);
            if ((realAccountDate == null || realAccountDate.before(defaultDateByBook)) && !realCard.getBoolean("initialcard")) {
                finCard.set("finaccountdate", (Object)defaultDateByBook);
            }
            else {
                finCard.set("finaccountdate", (Object)realAccountDate);
            }
        }
        DynamicObject pd = null;
        final String periodByDateMapKey = finCard.getDate("finaccountdate").toString() + assetBook.getLong("periodtype_Id");
        if (this.periodByDateMap.get(periodByDateMapKey) != null) {
            pd = this.periodByDateMap.get(periodByDateMapKey);
        }
        else {
            pd = PeriodUtil.getPeriodByDate(finCard.getDate("finaccountdate"), "id", assetBook.getLong("periodtype_Id"));
            this.periodByDateMap.put(periodByDateMapKey, pd);
        }
        if (realCard.getBoolean("initialcard")) {
            finCard.set("bizperiod_id", (Object)0L);
            finCard.set("period_id", (Object)0L);
        }
        else {
            if (pd == null) {
                throw new KDBizException(ResManager.loadKDString("\u8bf7\u7ef4\u62a4\u542f\u7528\u65e5\u671f\u5bf9\u5e94\u7684\u671f\u95f4", "AbstractFinCardGenerate_3", "fi-fa-business", new Object[0]));
            }
            final long periodId = pd.getLong("id");
            finCard.set("period_id", (Object)periodId);
            finCard.set("bizperiod_id", (Object)periodId);
        }
        if (assetcat != null) {
            final long depresystemId = assetBook.getDynamicObject("depresystem").getLong("id");
            final DynamicObject assetPolicyEntry = this.getAssetPolicyEntryByBook(depresystemId, assetcat);
            final DynamicObject dynDepresys = this.depresystemCache.get(depresystemId);
            if (assetPolicyEntry != null) {
                final Calendar ca = Calendar.getInstance();
                ca.setTime(finCard.getDate("finaccountdate"));
                finCard.set("depremethod", (Object)assetPolicyEntry.getDynamicObject("depremethod"));
                finCard.set("preusingamount", (Object)(assetPolicyEntry.getInt("useyear") * FaBusinessImportCardThreadCacheUtil.getYearPeriodType(Calendar.getInstance().get(1), dynDepresys.getLong("periodtype_id"))));
            }
        }
    }
    
    private Date getDefaultDateByBook(final DynamicObject book) {
        if (book.getDynamicObject("curperiod") == null) {
            throw new KDBizException(ResManager.loadKDString("\u8bf7\u542f\u7528\u5bf9\u5e94\u8d26\u7c3f\u5e76\u8bbe\u7f6e\u542f\u7528", "AbstractFinCardGenerate_1", "fi-fa-business", new Object[0]));
        }
        final Object currentPeriodId = book.getDynamicObject("curperiod").getPkValue();
        DynamicObject period = null;
        if (this.defaultDateByBookCache.get(currentPeriodId) == null) {
            period = BdPeriodDaoFactory.getInstance().queryOne(book.getDynamicObject("curperiod").getPkValue());
        }
        else {
            period = this.defaultDateByBookCache.get(currentPeriodId);
        }
        final Date beginDate = period.getDate("begindate");
        final Date endDate = period.getDate("enddate");
        final Date now = Calendar.getInstance().getTime();
        if (beginDate.getTime() / 24L * 60L * 60L <= now.getTime() / 24L * 60L * 60L && now.getTime() / 24L * 60L * 60L <= endDate.getTime() / 24L * 60L * 60L) {
            return now;
        }
        return endDate;
    }
    
    static {
        log = LogFactory.getLog((Class)AbstractFinCardGenerate.class);
    }
}

