package kd.bos.asset.realCardPlugin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.asset.business.CardGenerateHelper;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.IDataEntityType;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.operate.result.IOperateInfo;
import kd.bos.entity.operate.result.OperateErrorInfo;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.AddValidatorsEventArgs;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.entity.plugin.args.BeginOperationTransactionArgs;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
import kd.bos.entity.validate.AbstractValidator;
import kd.bos.entity.validate.ErrorLevel;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDBizException;
import kd.bos.exception.KDException;
import kd.bos.image.pojo.ImageInfo;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.datamanager.DataEntityCacheManager;
import kd.bos.orm.query.QFilter;
import kd.bos.orm.util.CollectionUtils;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.image.ImageServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.servicehelper.org.OrgUnitServiceHelper;
import kd.fi.fa.business.SourceFlagEnum;
import kd.fi.fa.business.coderule.CodeRuleService;
import kd.fi.fa.business.dao.factory.FaRealCardDaoFactory;
import kd.fi.fa.business.periodclose.FutureBizChecker;
import kd.fi.fa.business.utils.FaCodeRulerUtils;
import kd.fi.fa.business.utils.FaCommonUtils;
import kd.fi.fa.business.utils.FunctionMutexHelper;
import kd.fi.fa.business.utils.SystemParamHelper;
import kd.fi.fa.utils.FaOpUtils;

public class FaCardOperationPlugin extends AbstractOperationServicePlugIn {

	private static final Log log;
	public static final String isRecovery = "1";
	private static final String OP_SUBMIT = "submit";
	private static final String SOURCEFLAG = "ENGINEERINGTRANS,LEASECONTRACT,PURCHASE";
	private static final String saveAndSubmit = "submit,save";
	private static final String[] CLEAR_BILL_FIELDS;

	public void onPreparePropertys(final PreparePropertysEventArgs e) {
		final List<String> fieldKeys = (List<String>) e.getFieldKeys();
		fieldKeys.add("org");
		fieldKeys.add("assetcat");
		fieldKeys.add("billno");
		fieldKeys.add("barcode");
		fieldKeys.add("number");
		fieldKeys.add("assetname");
		fieldKeys.add("bizstatus");
		fieldKeys.add("realaccountdate");
		fieldKeys.add("assetamount");
		fieldKeys.add("unit");
		fieldKeys.add("originmethod");
		fieldKeys.add("storeplace");
		fieldKeys.add("usestatus");
		fieldKeys.add("headusedept");
		fieldKeys.add("initialcard");
		fieldKeys.add("barcoderule");
		fieldKeys.add("numberrule");
		fieldKeys.add("billnocoderule");
		fieldKeys.add("sourceflag");
		fieldKeys.add("sourceentryid");
		fieldKeys.add("usedate");
		fieldKeys.add("supplier");
		fieldKeys.add("srcbillnumber");
		fieldKeys.add("sourceentrysplitseq");
		fieldKeys.add("masterid");
		fieldKeys.add("createtime");
		fieldKeys.add("creator");
		fieldKeys.add("srcbillid");
		fieldKeys.add("zsf_rfid");
		fieldKeys.add("barcoderecovery");
		fieldKeys.add("billnorecovery");
		fieldKeys.add("numberrecovery");
	}

	public void afterExecuteOperationTransaction(final AfterOperationArgs e) {
		super.afterExecuteOperationTransaction(e);
		final String operateKey = e.getOperationKey();
		if ("delete".equals(operateKey)) {
			final DynamicObject[] dataEntities;
			final DynamicObject[] daynamicObjects = dataEntities = e.getDataEntities();
			for (final DynamicObject dataEntity : dataEntities) {
				final String orgId = dataEntity.getString("org_id");
				final CodeRuleService instance = CodeRuleService.getInstance(dataEntity, orgId);
				final boolean barcodeRecovery = "1".equalsIgnoreCase(dataEntity.getString("barcoderecovery"));
				final boolean billnoRecovery = "1".equalsIgnoreCase(dataEntity.getString("billnorecovery"));
				final boolean numberRecovery = "1".equalsIgnoreCase(dataEntity.getString("numberrecovery"));
				FaCodeRulerUtils.dealBarCode(dataEntity);
				if (!barcodeRecovery) {
					instance.recycleNumber("fa_card_real", dataEntity, orgId, dataEntity.getString("barcode"));
				}
				FaCodeRulerUtils.dealNumber(dataEntity);
				if (!numberRecovery) {
					instance.recycleNumber("fa_card_real", dataEntity, orgId, dataEntity.getString("number"));
				}
				FaCodeRulerUtils.dealBillNo(dataEntity);
				if (!billnoRecovery) {
					instance.recycleNumber("fa_card_real", dataEntity, orgId, dataEntity.getString("billno"));
				}
			}
		}
	}

	public void onAddValidators(final AddValidatorsEventArgs e) {
		super.onAddValidators(e);
		e.addValidator((AbstractValidator) new AbstractValidator() {
			public void validate() {
				final ExtendedDataEntity[] dataEntities = this.getDataEntities();
				final String operateName = this.getOperateKey();
				final Set<Date> minBeginDateList = new HashSet<Date>();
				final Map<Long, Date> minTypeDateMap = new HashMap<Long, Date>();
				Map<Long, Date> orgLtimeMap = new HashMap<Long, Date>();
				Date minMaxDate = null;
				final boolean hasImportCard = Stream.of(dataEntities)
						.anyMatch(v -> "IMPORT".equalsIgnoreCase(v.getDataEntity().getString("sourceflag")));
				Set<Object> realCardPkSet = new HashSet<Object>();
				if (hasImportCard || operateName.equals("submit")) {
					this.getMinPeriodTypeDateList(minBeginDateList, minTypeDateMap);
					if (minBeginDateList.isEmpty()) {
						throw new KDBizException(ResManager.loadKDString(
								"\u672a\u627e\u5230\u671f\u95f4\u6570\u636e\uff0c\u8bf7\u5148\u7ef4\u62a4\u4f1a\u8ba1\u671f\u95f4\u3002",
								"FaCardOperationPlugin_30", "fi-fa-opplugin", new Object[0]));
					}
					orgLtimeMap = this.getAssetBookPeriodTypeDate(dataEntities, minTypeDateMap);
					minMaxDate = Collections.max((Collection<? extends Date>) minBeginDateList);
				} else if (operateName.equals("deletefincard")) {
					final DynamicObjectCollection oldDynCards = QueryServiceHelper.query("fa_card_dynamic", "realcard",
							(QFilter[]) null);
					realCardPkSet = oldDynCards.stream().map(v -> (Long) v.get("realcard"))
							.collect((Collector<? super Object, ?, Set<Object>>) Collectors.toSet());
				}
				for (final ExtendedDataEntity extendedDataEntity : dataEntities) {
					final DynamicObject realCardObj = extendedDataEntity.getDataEntity();
					final String sourceflag = realCardObj.getString("sourceflag");
					final boolean isImport = "IMPORT".equalsIgnoreCase(sourceflag);
					final boolean isInitial = "INITIAL".equalsIgnoreCase(sourceflag);
					if (operateName.equals("submit") || (operateName.equals("save") && (isImport || isInitial))) {
						this.setOperationName(ResManager.loadKDString("\u63d0\u4ea4", "FaCardOperationPlugin_0",
								"fi-fa-opplugin", new Object[0]));
						final Date realCountDate = FaCardOperationPlugin.this
								.stripTime(realCardObj.getDate("realaccountdate"));
						final Long orgid = realCardObj.getLong("org_id");
						final Date assetBookDate = orgLtimeMap.get(orgid);
						if (!isInitial) {
							if (assetBookDate != null) {
								if (assetBookDate.after(realCountDate)) {
									this.addErrorMessage(extendedDataEntity, ResManager.loadKDString(
											"\u5b9e\u7269\u5165\u8d26\u65e5\u671f\u4e0d\u80fd\u5c0f\u4e8e\u5bf9\u5e94\u4f1a\u8ba1\u671f\u95f4\u7c7b\u578b\u7684\u6700\u5c0f\u65f6\u95f4:",
											"FaCardOperationPlugin_18", "fi-fa-opplugin", new Object[0])
											+ assetBookDate);
								}
							} else if (minMaxDate.after(realCountDate)) {
								this.addErrorMessage(extendedDataEntity, ResManager.loadKDString(
										"\u5b9e\u7269\u5165\u8d26\u65e5\u671f\u4e0d\u80fd\u5c0f\u4e8e\u6240\u6709\u4f1a\u8ba1\u671f\u95f4\u7c7b\u578b\u7684\u6700\u5c0f\u65f6\u95f4:",
										"FaCardOperationPlugin_19", "fi-fa-opplugin", new Object[0]) + minMaxDate);
							}
						}
						final long orgId = realCardObj.getLong("org_id");
						final long deptId = realCardObj.getLong("headusedept_id");
						final boolean departSharing = SystemParamHelper.getBooleanParam("depart_sharing", orgId, false);
						if (!departSharing) {
							final List<Long> allToOrg = (List<Long>) OrgUnitServiceHelper.getAllToOrg("15", "01",
									Long.valueOf(orgId));
							boolean valid = false;
							if (allToOrg != null && !allToOrg.isEmpty()) {
								valid = allToOrg.contains(deptId);
							} else {
								final List<Long> orgIdLst = new ArrayList<Long>(1);
								orgIdLst.add(orgId);
								final List<Long> allSubordinateOrgs = (List<Long>) OrgUnitServiceHelper
										.getAllSubordinateOrgs(Long.valueOf(1L), (List) orgIdLst, true);
								valid = allSubordinateOrgs.contains(deptId);
							}
							if (!valid) {
								this.addErrorMessage(extendedDataEntity, ResManager.loadKDString(
										"'\u4f7f\u7528\u90e8\u95e8'\u4e0d\u662f\u5f53\u524d\u6838\u7b97\u7ec4\u7ec7\u53ef\u7528\u7684\u4f7f\u7528\u90e8\u95e8\uff0c\u8bf7\u91cd\u65b0\u5f55\u5165",
										"FaCardOperationPlugin_25", "fi-fa-opplugin", new Object[0]));
							}
						}
					} else if (operateName.equals("audit")) {
						this.setOperationName(ResManager.loadKDString("\u5ba1\u6838", "FaCardOperationPlugin_1",
								"fi-fa-opplugin", new Object[0]));
					} else if (operateName.equals("unaudit")) {
						final Object sourceFlag = (realCardObj == null) ? "" : realCardObj.get("sourceflag");
						if (FaCommonUtils.equals(SourceFlagEnum.SPLIT, sourceFlag)) {
							this.addErrorMessage(extendedDataEntity, ResManager.loadKDString(
									"\u5361\u7247\u6765\u6e90\u65b9\u5f0f\u4e3a\u62c6\u5206\uff0c\u4e0d\u5141\u8bb8\u53cd\u5ba1\u6838",
									"FaCardOperationPlugin_20", "fi-fa-opplugin", new Object[0]));
						} else if (isImport) {
						}
					} else if (operateName.equals("delete")) {
						this.checkSourceFlag(realCardObj, extendedDataEntity);
						this.checkFinCard(realCardObj, extendedDataEntity);
						this.checkDepreSplitSetup(realCardObj, extendedDataEntity);
					} else if (operateName.equals("generatefincard")) {
						this.setOperationName(
								ResManager.loadKDString("\u751f\u6210\u521d\u59cb\u5316\u8d22\u52a1\u5361\u7247",
										"FaCardOperationPlugin_3", "fi-fa-opplugin", new Object[0]));
						this.checkExistInitailFinCard(realCardObj, extendedDataEntity);
						this.checkExistUnEnablebook(realCardObj, extendedDataEntity);
					} else if (operateName.equals("deletefincard")) {
						this.setOperationName(
								ResManager.loadKDString("\u5220\u9664\u521d\u59cb\u5316\u8d22\u52a1\u5361\u7247",
										"FaCardOperationPlugin_4", "fi-fa-opplugin", new Object[0]));
						this.checkExistUnEnablebook(realCardObj, extendedDataEntity);
						this.checkDeleteExistInitailFinCard(realCardObj, extendedDataEntity);
						if (realCardPkSet.contains(realCardObj.getPkValue())) {
							this.addErrorMessage(extendedDataEntity, ResManager.loadKDString(
									"\u5361\u7247\u5df2\u5207\u6362\u52a8\u6001\u7b97\u6cd5,\u4e0d\u80fd\u5220\u9664",
									"FaCardOperationPlugin_22", "fi-fa-opplugin", new Object[0]));
						}
					}
				}
			}

			private Map<Long, Date> getAssetBookPeriodTypeDate(final ExtendedDataEntity[] dataEntities,
					final Map<Long, Date> minTypeDateMap) {
				final Set<Long> orgids = new HashSet<Long>(dataEntities.length);
				for (final ExtendedDataEntity extendedDataEntity : dataEntities) {
					final DynamicObject realCardObj = extendedDataEntity.getDataEntity();
					orgids.add(realCardObj.getLong("org_id"));
				}
				final DynamicObjectCollection collPs = QueryServiceHelper.query("fa_assetbook", "periodtype,org",
						new QFilter[] { new QFilter("org", "in", (Object) orgids) });
				final Map<Long, Date> orgLtimeMap = new HashMap<Long, Date>();
				for (final DynamicObject collP : collPs) {
					final Long periType = collP.getLong("periodtype");
					final Long orgId = collP.getLong("org");
					final Date periTypeDate = minTypeDateMap.get(periType);
					final Date sameOrgDate = orgLtimeMap.get(orgId);
					if (sameOrgDate != null) {
						if (!sameOrgDate.before(periTypeDate)) {
							continue;
						}
						orgLtimeMap.put(orgId, periTypeDate);
					} else {
						orgLtimeMap.put(orgId, periTypeDate);
					}
				}
				return orgLtimeMap;
			}

			private void getMinPeriodTypeDateList(final Set<Date> minBeginDateList,
					final Map<Long, Date> minTypeDateMap) {
				try (final DataSet dataSet = QueryServiceHelper.queryDataSet("fa.FaCardOperationPlugin.onAddValidators",
						"bd_period", "begindate,periodtype", new QFilter[0], "begindate asc")) {
					final DataSet minBeginDate = dataSet.groupBy(new String[] { "periodtype" }).min("begindate")
							.finish();
					final int cols = minBeginDate.getRowMeta().getFieldCount();
					final Iterator<Row> iter = (Iterator<Row>) minBeginDate.iterator();
					while (minBeginDate.hasNext()) {
						final Row row = iter.next();
						for (int i = 0; i < cols; ++i) {
							final Date beginDate = row.getDate("begindate");
							minBeginDateList.add(beginDate);
							minTypeDateMap.put(row.getLong("periodtype"), beginDate);
						}
					}
				}
			}

			private void checkFinCard(final DynamicObject realCardObj, final ExtendedDataEntity extendedDataEntity) {
				final Object rCardPk = realCardObj.getPkValue();
				final boolean exists = QueryServiceHelper.exists("fa_card_fin",
						new QFilter("realcard", "=", rCardPk).toArray());
				if (exists) {
					this.addErrorMessage(extendedDataEntity, ResManager.loadKDString(
							"\u5b58\u5728\u4e0b\u6e38\u8d22\u52a1\u5361\u7247\uff0c\u4e0d\u5141\u8bb8\u624b\u5de5\u5220\u9664",
							"FaCardOperationPlugin_5", "fi-fa-opplugin", new Object[0]));
				}
			}

			private void checkDepreSplitSetup(final DynamicObject realCardObj,
					final ExtendedDataEntity extendedDataEntity) {
				final Object rCardPk = realCardObj.getPkValue();
				final boolean exists = QueryServiceHelper.exists("fa_depresplitsetup",
						new QFilter("realcard", "=", rCardPk).toArray());
				if (exists) {
					this.addErrorMessage(extendedDataEntity, ResManager.loadKDString(
							"\u5b58\u5728\u6298\u65e7\u5206\u644a\u8bbe\u7f6e\uff0c\u8bf7\u5148\u5220\u9664\u5bf9\u5e94\u8bbe\u7f6e",
							"FaCardOperationPlugin_15", "fi-fa-opplugin", new Object[0]));
				}
			}

			private void checkSourceFlag(final DynamicObject realCardObj, final ExtendedDataEntity extendedDataEntity) {
				final DynamicObject originmethod = realCardObj.getDynamicObject("originmethod");
				if (originmethod == null) {
					return;
				}
				final Object sourceFlag = realCardObj.get("sourceflag");
				if (FaCommonUtils.equals(SourceFlagEnum.DISPATCH, sourceFlag)) {
					this.addErrorMessage(extendedDataEntity, ResManager.loadKDString(
							"\u5361\u7247\u6765\u6e90\u65b9\u5f0f\u4e3a\u8c03\u62e8\uff0c\u4e0d\u5141\u8bb8\u624b\u5de5\u5220\u9664",
							"FaCardOperationPlugin_7", "fi-fa-opplugin", new Object[0]));
				} else if (FaCommonUtils.equals(SourceFlagEnum.SPLIT, sourceFlag)) {
					this.addErrorMessage(extendedDataEntity, ResManager.loadKDString(
							"\u5361\u7247\u6765\u6e90\u65b9\u5f0f\u4e3a\u62c6\u5206\uff0c\u4e0d\u5141\u8bb8\u624b\u5de5\u5220\u9664",
							"FaCardOperationPlugin_21", "fi-fa-opplugin", new Object[0]));
				}
			}

			private void checkExistInitailFinCard(final DynamicObject realCardObj,
					final ExtendedDataEntity extendedDataEntity) {
				final String sourceFlag = realCardObj.getString("sourceflag");
				if (SourceFlagEnum.INITIAL.toString().equalsIgnoreCase(sourceFlag)
						&& FaOpUtils.checkFinCard(realCardObj)) {
					this.addErrorMessage(extendedDataEntity, ResManager.loadKDString(
							"\u5f53\u524d\u5b9e\u7269\u5361\u7247\u5728\u6240\u6709\u7684\u8d26\u7c3f\u5747\u5df2\u751f\u6210\u8d22\u52a1\u5361\u7247,\u4e0d\u80fd\u91cd\u590d\u751f\u6210",
							"FaCardOperationPlugin_9", "fi-fa-opplugin", new Object[0]));
				}
			}

			private Optional<String> checkFutureBiz(final DynamicObject realCardObj) {
				final DynamicObject[] realCards = FaRealCardDaoFactory.getInstance().query("id",
						new QFilter("masterid", "=", (Object) realCardObj.getLong("masterid")));
				final Set<Object> realCardPkSet = Arrays.stream(realCards).map(v -> (Long) v.get("id"))
						.collect((Collector<? super Object, ?, Set<Object>>) Collectors.toSet());
				final Object orgId = realCardObj.get("org.id");
				return (Optional<String>) new FutureBizChecker((Object) null, orgId, (Set) realCardPkSet, (Date) null)
						.checkWhenun();
			}

			private void checkDeleteExistInitailFinCard(final DynamicObject realCardObj,
					final ExtendedDataEntity extendedDataEntity) {
				final String sourceFlag = realCardObj.getString("sourceflag");
				if (SourceFlagEnum.INITIAL.toString().equalsIgnoreCase(sourceFlag)) {
					if (!FaOpUtils.checkFinCard(realCardObj)) {
						this.addErrorMessage(extendedDataEntity,
								ResManager.loadKDString("\u672a\u751f\u6210\u8d22\u52a1\u5361\u7247",
										"FaCardOperationPlugin_10", "fi-fa-opplugin", new Object[0]));
					}
					final Optional<String> checkResult = this.checkFutureBiz(realCardObj);
					if (checkResult.isPresent()) {
						final String msg = String.format(ResManager.loadKDString(
								"\u521d\u59cb\u5316\u8d22\u52a1\u5361\u7247\u5b58\u5728\u540e\u7eed\u4e1a\u52a1\uff0c\u65e0\u6cd5\u5220\u9664\uff1a%s",
								"FaCardOperationPlugin_14", "fi-fa-opplugin", new Object[0]), checkResult.get());
						this.addErrorMessage(extendedDataEntity, msg);
					}
				}
			}

			private void checkExistUnEnablebook(final DynamicObject realCardObj,
					final ExtendedDataEntity extendedDataEntity) {
				final String sourceFlag = realCardObj.getString("sourceflag");
				if (SourceFlagEnum.INITIAL.toString().equalsIgnoreCase(sourceFlag)) {
					final QFilter filterOrg = new QFilter("org.id", "=", (Object) realCardObj.getLong("org_id"));
					final QFilter filterEnable = new QFilter("status", "=", (Object) "C");
					final boolean existBook = QueryServiceHelper.exists("fa_assetbook", new QFilter[] { filterOrg });
					if (!existBook) {
						this.addErrorMessage(extendedDataEntity,
								ResManager.loadKDString("\u6ca1\u6709\u5bf9\u5e94\u8d26\u7c3f,\u4e0d\u80fd\u64cd\u4f5c",
										"FaCardOperationPlugin_24", "fi-fa-opplugin", new Object[0]));
						return;
					}
					final boolean isExistUnEnablebook = QueryServiceHelper.exists("fa_assetbook",
							new QFilter[] { filterOrg, filterEnable });
					if (isExistUnEnablebook) {
						this.addErrorMessage(extendedDataEntity, ResManager.loadKDString(
								"\u5df2\u6709\u5bf9\u5e94\u8d26\u7c3f\u5df2\u7ed3\u675f\u521d\u59cb\u5316,\u4e0d\u80fd\u64cd\u4f5c",
								"FaCardOperationPlugin_11", "fi-fa-opplugin", new Object[0]));
					}
				}
			}
		});
	}

	public Date stripTime(final Date d) {
		return java.sql.Date.valueOf(new SimpleDateFormat("yyyy-MM-dd").format(d));
	}

	public void beginOperationTransaction(final BeginOperationTransactionArgs e) {
		final String operateName = e.getOperationKey();
		boolean checkboxrfid = false;
		if ("save".equals(operateName) || "submit".equals(operateName)) {
			DynamicObject[] realCardObjs = e.getDataEntities();
			for (DynamicObject dataEntity : realCardObjs) {
        		checkboxrfid = (boolean) dataEntity.get("zsf_checkboxrfid");       		
        	}
			final List<DynamicObject> cards = FaOpUtils.getDataEntities(e);
			for (final DynamicObject card : cards) {
				final long master = card.getLong("masterid");
				if (master == 0L) {
					card.set("masterid", (Object) card.getLong("id"));
				}
				if(checkboxrfid) {                    	
					card.set("zsf_rfid",card.get("billno"));
				}
			}
		} else if ("audit".equals(operateName) || "generatefincard".equals(operateName)) {
			this.createFinCard(e);
		} else if ("unaudit".equals(operateName) || "deletefincard".equals(operateName)) {
			this.deleteFinCardOnly(e);
		} else if ("delete".equals(operateName)) {
			this.dealPurchaseCard(e);
		}
	}

	public void endOperationTransaction(final EndOperationTransactionArgs e) {
		final DynamicObject[] finCardObjs = e.getDataEntities();
		if ("submit".equals(e.getOperationKey())) {
			this.generateImageNumberBatchAsyn(finCardObjs);
		}
	}

	private void generateImageNumberBatchAsyn(final DynamicObject[] dataEntities) {
		final List<ImageInfo> imageInfoList = new ArrayList<ImageInfo>(dataEntities.length);
		for (final DynamicObject dataEntity : dataEntities) {
			final String pkId = dataEntity.getPkValue().toString();
			Date createTime = dataEntity.getDate("createtime");
			if (null == createTime) {
				createTime = new Date();
			}
			String creator = "";
			if (null == dataEntity.getDynamicObject("creator")) {
				final RequestContext requestContext = RequestContext.get();
				creator = requestContext.getUserId();
			} else {
				creator = dataEntity.getDynamicObject("creator").getPkValue().toString();
			}
			final String orgId = dataEntity.getDynamicObject("org").getPkValue().toString();
			final Date date = new Date();
			final int datehash = date.hashCode();
			final ImageInfo imageInfo = new ImageInfo();
			imageInfo.setCreatetime(createTime);
			imageInfo.setCreator(creator);
			imageInfo.setOrgId(orgId);
			imageInfo.setBilltype(dataEntity.getDynamicObjectType().getName());
			imageInfo.setBillNo(pkId + "-" + datehash);
			imageInfo.setBillId(pkId);
			imageInfoList.add(imageInfo);
		}
		ImageServiceHelper.createImageInfoBatchAsyn((List) imageInfoList);
	}

	private void deleteFinCardOnly(final BeginOperationTransactionArgs e) {
		try {
			final List<DynamicObject> bills = FaOpUtils.getDataEntities(e);
			final Map<Object, DynamicObject> billMap = new HashMap<Object, DynamicObject>(bills.size());
			for (final DynamicObject v2 : bills) {
				billMap.put(v2.getPkValue(), v2);
			}
			final Set<Object> realIdSet = bills.stream().map(v -> v.getPkValue())
					.collect((Collector<? super Object, ?, Set<Object>>) Collectors.toSet());
			boolean hasError = false;
			OperateErrorInfo errInfo = null;
			for (final Object realId : realIdSet) {
				final DynamicObject[] finCards = BusinessDataServiceHelper.load("fa_card_fin", "id",
						new QFilter("realcard", "=", realId).toArray());
				final Object[] finCardIdArr = new Object[finCards.length];
				for (int i = 0; i < finCards.length; ++i) {
					finCardIdArr[i] = finCards[i].getPkValue();
				}
				if (finCardIdArr.length > 0) {
					final OperationResult result = OperationServiceHelper.executeOperate("delete", "fa_card_fin",
							finCardIdArr, (OperateOption) null);
					if (result.isSuccess()) {
						continue;
					}
					bills.remove(billMap.get(realId));
					this.getOperationResult().getSuccessPkIds().remove(realId);
					hasError = true;
					this.getOperationResult().setSuccess(false);
					final List<IOperateInfo> errorInfoList = (List<IOperateInfo>) result.getAllErrorOrValidateInfo();
					if (errorInfoList.size() == 0) {
						errInfo = new OperateErrorInfo();
						errInfo.setPkValue(realId);
						errInfo.setMessage(result.getMessage());
						errInfo.setLevel(ErrorLevel.FatalError);
						this.getOperationResult().getAllErrorInfo().add(errInfo);
					} else {
						for (final IOperateInfo ori : errorInfoList) {
							errInfo = (OperateErrorInfo) ori;
							errInfo.setPkValue(realId);
							this.getOperationResult().getAllErrorInfo().add(errInfo);
						}
					}
				}
			}
			e.setDataEntities((DynamicObject[]) bills.toArray(new DynamicObject[0]));
			if (hasError) {
				return;
			}
			final IDataEntityType type = FaRealCardDaoFactory.getBaseInstance().getEmptyDynamicObject()
					.getDataEntityType();
			final DataEntityCacheManager cacheManager = new DataEntityCacheManager(type);
			cacheManager.removeByPrimaryKey(realIdSet.toArray());
		} catch (Exception ex) {
			throw new KDException(
					new ErrorCode("UNKNOWN_ERR",
							ex.getMessage()
									+ ResManager.loadKDString("\r\n\u5220\u9664\u8d22\u52a1\u5361\u7247\u5931\u8d25.",
											"FaCardOperationPlugin_13", "fi-fa-opplugin", new Object[0])),
					new Object[0]);
		}
	}

	@SuppressWarnings("null")
	private void dealPurchaseCard(final BeginOperationTransactionArgs e) {
		final List<DynamicObject> purchaseObjects = FaOpUtils.getDataEntities(e).stream()
				.filter(item -> item.getString("sourceflag").equals("PURCHASE"))
				.collect(Collectors.toList());
		final Map<Object, List<DynamicObject>> collect = purchaseObjects.stream()
				.collect(Collectors.groupingBy(t -> t.getLong("sourceentryid")));
		final List<Object[]> listUpdatObject = new ArrayList<Object[]>();
		final List<Object[]> list = null;
		collect.forEach((k, v) -> {
			Object[] objects = new Object[] { (v == null) ? 0 : v.size(), k };
			list.add(objects);
			return;
		});
		if (CollectionUtils.isEmpty((Collection) listUpdatObject)) {
			FaCardOperationPlugin.log.info(
					"\u66f4\u65b0\u91c7\u8d2d\u8f6c\u56fa\u5355\u76f8\u5e94\u53ef\u751f\u6210\u6570\u91cf:listUpdatObject\u4e3a\u7a7a");
			return;
		}
		final String sql = "update T_FA_PURCHASEBILLENTRY set fassetqtyleft = fassetqtyleft + ? where fentryid= ?";
		DB.executeBatch(DBRoute.of("fa"), sql, (List) listUpdatObject);
	}

	protected void createFinCard(final BeginOperationTransactionArgs e) {
		final Map<Object, List<DynamicObject>> orgDynamicObjectMap = this.orgDynamicObjectMap(e);
		for (final Map.Entry<Object, List<DynamicObject>> entry : orgDynamicObjectMap.entrySet()) {
			FunctionMutexHelper.lockWithOrg(entry.getKey(),
					() -> CardGenerateHelper.generateFinCard((List) entry.getValue()));
		}
	}

	private Map<Object, List<DynamicObject>> orgDynamicObjectMap(final BeginOperationTransactionArgs e) {
		final Map<Object, List<DynamicObject>> dobjListMap = new HashMap<Object, List<DynamicObject>>();
		for (final DynamicObject realCard : FaOpUtils.getDataEntities(e)) {
			final Object orgPk = realCard.getDynamicObject("org").getPkValue();
			if (dobjListMap.get(orgPk) == null || dobjListMap.size() < 0) {
				final List<DynamicObject> dobj = new ArrayList<DynamicObject>();
				dobj.add(realCard);
				dobjListMap.put(orgPk, dobj);
			} else {
				final List<DynamicObject> dobj = dobjListMap.get(orgPk);
				dobj.add(realCard);
				dobjListMap.put(orgPk, dobj);
			}
		}
		return dobjListMap;
	}

	static {
		log = LogFactory.getLog((Class) FaCardOperationPlugin.class);
		CLEAR_BILL_FIELDS = new String[] { "id", "billno", "billstatus" };
	}
}