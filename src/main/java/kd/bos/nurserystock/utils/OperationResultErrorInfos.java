package kd.bos.nurserystock.utils;

import java.util.List;

import kd.bos.entity.operate.result.IOperateInfo;
import kd.bos.entity.operate.result.OperateErrorInfo;
import kd.bos.entity.operate.result.OperationResult;

public class OperationResultErrorInfos {
	/**
	 *  * 获取操作错误信息  * @param operationResult  * @return  
	 */
	@SuppressWarnings("unused")
	private String getOperationResultErrorInfos(OperationResult operationResult){
	    if(operationResult.isSuccess()){
//	         return StringUtils.EMPTY;
	    	return "";
	  }

	    List<IOperateInfo> errorInfos = operationResult.getAllErrorOrValidateInfo();
	    int size = errorInfos.size() + operationResult.getSuccessPkIds().size();
	    if (size > 1) {
	        StringBuilder stringBuilder = new StringBuilder();
	        int i = 0;
	        for(int len = errorInfos.size(); i < 5 && i < len; ++i) {
	         stringBuilder.append((errorInfos.get(i)).getMessage());
	        }
	        return stringBuilder.toString();
	    } else if (!errorInfos.isEmpty()) {
	        OperateErrorInfo errorInfo = (OperateErrorInfo)errorInfos.get(0);
            String msg = errorInfo.getMessage() == null ? "" : errorInfo.getMessage();
            return msg;
	   } else {
		   String msg = operationResult.getMessage() == null ? "" : operationResult.getMessage();
		   return msg;
	   }
	}

}
