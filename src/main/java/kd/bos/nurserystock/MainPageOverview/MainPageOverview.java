package kd.bos.nurserystock.MainPageOverview;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Format;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.format.FormatFactory;
import kd.bos.entity.format.FormatObject;
import kd.bos.entity.format.FormatTypes;
import kd.bos.entity.validate.BillStatus;
import kd.bos.form.control.Label;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.inte.InteServiceHelper;
import kd.fi.fa.business.utils.FaBizUtils;
import kd.fi.fa.common.util.ContextUtil;
import kd.fi.fa.utils.FaFormUtils;

public class MainPageOverview extends AbstractFormPlugin {
//	private static final String ALGO = "kd.bos.nurserystock.MainPageOverview";
//	private long orgid;
//	private long bookid;
//	private String currencySign;
//	private long curPeriodId;
//	private long beforeInitPeriodId;
//	private long prevPeriodId;
//	private List<Card> curCardLst;
//	private boolean hasPrevCard;
//	private List<Card> prevCardLst;
//	private int currencyAmtprecision;
//	private Format currencyFormat;
//
//	public MainPageOverview() {
//		this.beforeInitPeriodId = 0L;
//		this.prevPeriodId = 0L;
//		this.curCardLst = new ArrayList<Card>();
//		this.hasPrevCard = false;
//		this.prevCardLst = new ArrayList<Card>();
//	}
//
//	public void afterCreateNewData(final EventObject e) {
//		super.afterCreateNewData(e);
//		final boolean success = this.initCtx();
//		if (success) {
//			this.initCard();
//			this.initFormat();
//			this.fillData();
//		}
//	}
//
//	private void initFormat() {
//		final FormatObject fobj = InteServiceHelper.getUserFormat(ContextUtil.getUserId());
//		fobj.getCurrencyFormat().setCurrencySymbols(this.currencySign);
//		fobj.getCurrencyFormat().setMinimumFractionDigits(this.currencyAmtprecision);
//		this.currencyFormat = FormatFactory.get(FormatTypes.Currency).getFormat(fobj);
//	}
//
//	private void fillData() {
//		final BigDecimal sumNetAmount = this.all();
//		this.depre();
//		this.overage(sumNetAmount);
//		this.fillNewData();
//		this.fillDeleteData();
//	}
//
//	private void initCard() {
//		QFilter prevPeriodFilter = null;
//		if (this.prevPeriodId >= this.beforeInitPeriodId) {
//			prevPeriodFilter = new QFilter("bizperiod", "<=", (Object) this.prevPeriodId)
//					.and(new QFilter("endperiod", ">", (Object) this.prevPeriodId));
//			final QFilter[] filters = { new QFilter("assetbook", "=", (Object) this.bookid),
//					new QFilter("billstatus", "=", (Object) BillStatus.C.toString()), prevPeriodFilter };
//			this.hasPrevCard = QueryServiceHelper.exists("fa_card_fin", filters);
//		}
//		final QFilter curPeriodFilter = new QFilter("bizperiod", "<=", (Object) this.curPeriodId)
//				.and(new QFilter("endperiod", ">", (Object) this.curPeriodId));
//		final QFilter[] filters2 = { new QFilter("assetbook", "=", (Object) this.bookid),
//				new QFilter("billstatus", "=", (Object) BillStatus.C.toString()),
//				this.hasPrevCard ? curPeriodFilter.or(prevPeriodFilter) : curPeriodFilter };
//		final String selectFields = "bizperiod,netamount,monthdepre,preusingamount,depredamount,period,clearperiod";
//		try (final DataSet ds = QueryServiceHelper.queryDataSet("kd.fi.fa.formplugin.MainPageOverview",
//				"fa_card_fin", selectFields, filters2, "bizperiod asc")) {
//			for (final Row row : ds) {
//				if (!this.hasPrevCard || row.getLong("bizperiod") > this.prevPeriodId) {
//					this.appendCard(this.curCardLst, row);
//				} else {
//					this.appendCard(this.prevCardLst, row);
//				}
//			}
//		}
//	}
//
//	private void appendCard(final List<Card> cardLst, final Row row) {
//		cardLst.add(new Card(row.getLong("bizperiod"), row.getBigDecimal("netamount"), row.getBigDecimal("monthdepre"),
//				row.getBigDecimal("preusingamount"), row.getBigDecimal("depredamount"), row.getLong("period"),
//				row.getLong("clearperiod")));
//	}
//
//	protected boolean initCtx() {
//		final IDataModel model = this.getView().getParentView().getModel();
//		this.orgid = FaFormUtils.getCardOrg(model);
//		if (this.orgid == 0L) {
//			return false;
//		}
//		final String fields = "id,basecurrency.sign,basecurrency.amtprecision,periodtype,curperiod,startperiod.periodyear,startperiod.periodnumber,curperiod.periodyear,curperiod.periodnumber";
//		final DynamicObject book = FaBizUtils.getAsstBookByOrg(Long.valueOf(this.orgid), fields);
//		if (book == null) {
//			return false;
//		}
//		this.bookid = book.getLong("id");
//		this.currencySign = book.getString("basecurrency.sign");
//		this.currencyAmtprecision = book.getInt("basecurrency.amtprecision");
//		final long periodTypeId = book.getLong("periodtype");
//		this.curPeriodId = book.getLong("curperiod");
//		final int startPeriodYear = book.getInt("startperiod.periodyear");
//		final int startPeriodNumber = book.getInt("startperiod.periodnumber");
//		final int beforeInitYear = (startPeriodNumber == 1) ? (startPeriodYear - 1) : startPeriodYear;
//		final int beforeInitPeriodNumber = (startPeriodNumber == 1) ? 12 : (startPeriodNumber - 1);
//		QFilter[] filters = { new QFilter("periodtype", "=", (Object) periodTypeId),
//				new QFilter("periodyear", "=", (Object) beforeInitYear),
//				new QFilter("periodnumber", "=", (Object) beforeInitPeriodNumber) };
//		final DynamicObject beforeInitPeriod = QueryServiceHelper.queryOne("bd_period", "id", filters);
//		if (beforeInitPeriod != null) {
//			this.beforeInitPeriodId = beforeInitPeriod.getLong("id");
//		} else {
//			this.beforeInitPeriodId = periodTypeId * 10000000L + beforeInitYear * 1000 + beforeInitPeriodNumber * 10;
//		}
//		final int curPeriodYear = book.getInt("curperiod.periodyear");
//		final int curPeriodNumber = book.getInt("curperiod.periodnumber");
//		final int prevPeriodYear = curPeriodYear - 1;
//		filters = new QFilter[] { new QFilter("periodtype", "=", (Object) periodTypeId),
//				new QFilter("periodyear", "=", (Object) prevPeriodYear),
//				new QFilter("periodnumber", "=", (Object) curPeriodNumber) };
//		final DynamicObject prevPeriod = QueryServiceHelper.queryOne("bd_period", "id", filters);
//		if (prevPeriod != null) {
//			this.prevPeriodId = prevPeriod.getLong("id");
//		} else {
//			this.prevPeriodId = periodTypeId * 10000000L + prevPeriodYear * 1000 + curPeriodNumber * 10;
//		}
//		return true;
//	}
//
//	private void fillDeleteData() {
//		final List<Card> curCreateLst = this.curCardLst.stream().filter(v -> v.clearPeriodId == this.curPeriodId).collect((Collector<? super Object, ?, List<Card>>) Collectors.toList());
//		final int count = curCreateLst.size();
//		final BigDecimal curAmount = curCreateLst.stream().map(v -> v.netAmount).reduce(BigDecimal.ZERO,
//				BigDecimal::add);
//		final String flag = "delete";
//		String desc = "-";
//		if (this.hasPrevCard) {
//			final BigDecimal prevAmount = this.prevCardLst.stream().filter(v -> v.clearPeriodId == this.prevPeriodId)
//					.map(v -> v.netAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
//			if (prevAmount.compareTo(BigDecimal.ZERO) > 0) {
//				desc = curAmount.subtract(prevAmount).multiply(new BigDecimal("100")).divide(prevAmount, 2,
//						RoundingMode.HALF_UP) + "%";
//			}
//		}
//		this.setLabelText(flag, count, curAmount, ResManager.loadKDString("\u540c\u6bd4\u589e\u957f\uff1a",
//				"MainPageOverview_0", "fi-fa-formplugin", new Object[0]) + desc);
//	}
//
//	private void fillNewData() {
//		final List<Card> curCreateLst = this.curCardLst.stream().filter(v -> 
//		v.createPeriodId == this.curPeriodId).collect((Collector<? super Object, ?, List<Card>>) Collectors.toList());
//		final int count = curCreateLst.size();
//		final BigDecimal curAmount = curCreateLst.stream().map(v -> v.netAmount).reduce(BigDecimal.ZERO,
//				BigDecimal::add);
//		final String flag = "new";
//		String desc = "-";
//		if (this.hasPrevCard) {
//			final BigDecimal prevAmount = this.prevCardLst.stream().filter(v -> v.createPeriodId == this.prevPeriodId)
//					.map(v -> v.netAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
//			if (prevAmount.compareTo(BigDecimal.ZERO) > 0) {
//				desc = curAmount.subtract(prevAmount).multiply(new BigDecimal("100")).divide(prevAmount, 2,
//						RoundingMode.HALF_UP) + "%";}
//			}
//		this.setLabelText(flag, count, curAmount, ResManager.loadKDString("\u540c\u6bd4\u589e\u957f\uff1a",
//				"MainPageOverview_0", "fi-fa-formplugin", new Object[0]) + desc);
//	}
//
//	private void overage(final BigDecimal sumNetAmount) {
//		final List<Card> overAgeLst = this.curCardLst.stream()
//				.filter(v -> v.depredAmount.equals(v.preUsingAmount) && v.clearPeriodId == 0L)
//				.collect((Collector<? super Object, ?, List<Card>>) Collectors.toList());
//		final int count = overAgeLst.size();
//		final BigDecimal overAgeNetAmount = overAgeLst.stream().map(v -> v.netAmount).reduce(BigDecimal.ZERO,
//				BigDecimal::add);
//		String desc = "-";
//		if (sumNetAmount.compareTo(BigDecimal.ZERO) > 0) {
//			desc = overAgeNetAmount.multiply(new BigDecimal("100")).divide(sumNetAmount, 2, RoundingMode.HALF_UP) + "%";
//		}
//		final String flag = "overage";
//		this.setLabelText(flag, count, overAgeNetAmount, ResManager.loadKDString("\u903e\u9f84\u5360\u6bd4\uff1a",
//				"MainPageOverview_1", "fi-fa-formplugin", new Object[0]) + desc);
//	}
//
//	private void depre() {
//		final List<Card> curDepreLst = this.curCardLst.stream()
//				.filter(v -> v.bizPeriodId == this.curPeriodId && v.monthDepre.compareTo(BigDecimal.ZERO) > 0)
//				.collect((Collector<? super Object, ?, List<Card>>) Collectors.toList());
//		final int count = curDepreLst.size();
//		final BigDecimal curAmount = curDepreLst.stream().map(v -> v.monthDepre).reduce(BigDecimal.ZERO,
//				BigDecimal::add);
//		final String flag = "depre";
//		String desc = "-";
//		if (this.hasPrevCard) {
//			final BigDecimal prevAmount = this.prevCardLst.stream().map(v -> v.monthDepre).reduce(BigDecimal.ZERO,
//					BigDecimal::add);
//			if (prevAmount.compareTo(BigDecimal.ZERO) > 0) {
//				desc = curAmount.subtract(prevAmount).multiply(new BigDecimal("100")).divide(prevAmount, 2,
//						RoundingMode.HALF_UP) + "%";
//			}
//		}
//		this.setLabelText(flag, count, curAmount, ResManager.loadKDString("\u540c\u6bd4\u589e\u957f\uff1a",
//				"MainPageOverview_0", "fi-fa-formplugin", new Object[0]) + desc);
//	}
//
//	private BigDecimal all() {
//		final List<Card> liveCurCardLst = this.curCardLst.stream()
//				.filter(v -> v.clearPeriodId == 0L || v.clearPeriodId > this.curPeriodId)
//				.collect((Collector<? super Card, ?,List<Card>>) Collectors.toList());
//		final int count = liveCurCardLst.size();
//		final BigDecimal curAmount = liveCurCardLst.stream().map(v -> v.netAmount).reduce(BigDecimal.ZERO,
//				BigDecimal::add);
//		final String flag = "all";
//		String desc = "-";
//		if (this.hasPrevCard) {
//			final BigDecimal prevAmount = this.prevCardLst.stream()
//					.filter(v -> v.clearPeriodId == 0L || v.clearPeriodId > this.prevPeriodId).map(v -> v.netAmount)
//					.reduce(BigDecimal.ZERO, BigDecimal::add);
//			if (prevAmount.compareTo(BigDecimal.ZERO) > 0) {
//				desc = curAmount.subtract(prevAmount).multiply(new BigDecimal("100")).divide(prevAmount, 2,
//						RoundingMode.HALF_UP) + "%";
//			}
//		}
//		this.setLabelText(flag, count, curAmount, ResManager.loadKDString("\u540c\u6bd4\u589e\u957f\uff1a",
//				"MainPageOverview_0", "fi-fa-formplugin", new Object[0]) + desc);
//		return curAmount;
//	}
//
//	private void setLabelText(final String flag, final int count, final BigDecimal curAmount, final String desc) {
//		((Label) this.getControl(flag + "_count")).setText(count + "");
//		final BigDecimal divide = curAmount.divide(new BigDecimal("1000"), this.currencyAmtprecision,
//				RoundingMode.HALF_UP);
//		((Label) this.getControl(flag + "_amount")).setText(this.currencyFormat.format(divide));
//		((Label) this.getControl(flag + "_desc")).setText(desc);
//	}
//
//	private static class Card {
//		protected long bizPeriodId;
//		protected BigDecimal netAmount;
//		protected BigDecimal monthDepre;
//		protected BigDecimal preUsingAmount;
//		protected BigDecimal depredAmount;
//		protected long createPeriodId;
//		protected long clearPeriodId;
//
//		public Card(final long bizPeriodId, final BigDecimal netAmount, final BigDecimal monthDepre,
//				final BigDecimal preUsingAmount, final BigDecimal depredAmount, final long createPeriodId,
//				final long clearPeriodId) {
//			this.bizPeriodId = bizPeriodId;
//			this.netAmount = netAmount;
//			this.monthDepre = monthDepre;
//			this.preUsingAmount = preUsingAmount;
//			this.depredAmount = depredAmount;
//			this.createPeriodId = createPeriodId;
//			this.clearPeriodId = clearPeriodId;
//		}
//	}
}
