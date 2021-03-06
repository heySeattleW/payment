package com.pay.api;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pay.api.parser.ObjectXmlParser;
import com.pay.api.parser.Parser;
import com.pay.api.request.PayRequest;
import com.pay.api.request.WxPayTradeCreateRequest;
import com.pay.api.request.WxPayTradeRefundRequest;
import com.pay.api.response.PayResponse;
import com.pay.api.response.WxPayTradeCreateResponse;
import com.pay.api.response.WxPayTradeRefundResponse;
import com.pay.api.util.PaySignature;
import com.pay.api.util.RequestParametersHolder;
import com.pay.api.util.WebUtils;
import com.pay.api.util.XMLUtil;
/**
 * 微信支付-客户端
 * @author sunzhengqiang
 * @date 2016年11月17日
 */
public class DefaultWxPayClient implements WxPayClient {
	
	private String appId;
	private String mchId;
	private String privateKey;
	private String url;
	
	private String deviceInfo;
	private String signType = PayConstants.SIGN_TYPE_MD5;
	private String charset = PayConstants.DEFAULT_CHARSET;
	private String format = PayConstants.FORMAT_XML;
	
	private static final Logger logger = Logger.getLogger(DefaultWxPayClient.class);
	
	public DefaultWxPayClient() {
		
	}
	
	public DefaultWxPayClient(String url,String appId, String mchId, String privateKey) {
		super();
		this.url = url;
		this.appId = appId;
		this.mchId = mchId;
		this.privateKey = privateKey;
	}
	
	
	public DefaultWxPayClient(String url,String appId, String mchId, String deviceInfo, String signType, String privateKey,
			String charset) {
		super();
		this.url = url;
		this.appId = appId;
		this.mchId = mchId;
		this.deviceInfo = deviceInfo;
		this.signType = signType;
		this.privateKey = privateKey;
		this.charset = charset;
	}

	@Override
	public <T extends PayResponse> T execute(PayRequest<T> request) {
		Parser<T> parser = null;
		if(this.format.equals(PayConstants.FORMAT_XML)){
			parser = new ObjectXmlParser<T>(request.getResponseClass());
		}else if(this.format.equals(PayConstants.FORMAT_JSON)){
			
		}
		return _execute(request, parser);
	}
	
	@Override
	public <T extends PayResponse> T execute(PayRequest<T> request, String certificatePath, String certificatePass) {
		Parser<T> parser = null;
		if(this.format.equals(PayConstants.FORMAT_XML)){
			parser = new ObjectXmlParser<T>(request.getResponseClass());
		}else if(this.format.equals(PayConstants.FORMAT_JSON)){
			
		}
		return _execute(request, parser, certificatePath, certificatePass);
	}

	private <T extends PayResponse> T _execute(PayRequest<T> request,Parser<T> parser){
		String rsp = doPost(request);
		return parser.parse(rsp);
	}
	
	private <T extends PayResponse> String doPost(PayRequest<T> request) {
		RequestParametersHolder requestHolder = getRequestHolderWithSign(request);
		String rsp = "";
		rsp = new WebUtils().post(this.url,  XMLUtil.toXML(requestHolder));
		return rsp;
	}
	
	private <T extends PayResponse> T _execute(PayRequest<T> request,Parser<T> parser,String certificatePath, String certificatePass){
		String rsp = doPost(request,certificatePath,certificatePass);
		return parser.parse(rsp);
	}
	
	private <T extends PayResponse> String doPost(PayRequest<T> request,String certificatePath, String certificatePass) {
		RequestParametersHolder requestHolder = getRequestHolderWithSign(request);
		String rsp = "";
		rsp = new WebUtils(certificatePath,certificatePass).post(this.url,  XMLUtil.toXML(requestHolder));
		return rsp;
	}
	
	private <T extends PayResponse> RequestParametersHolder getRequestHolderWithSign(PayRequest<T> request) {
		RequestParametersHolder requestHolder = new RequestParametersHolder();
		
//		requestHolder.getApplicationParams().putAll(request.getTextParams());
		
		if(!StringUtils.isNotEmpty(request.getBizContent())){
			throw new PayApiException("请设置bizContent");
		}
		//	获取bizContent
		@SuppressWarnings("unchecked")
		Map<String,String> bizContent = (Map<String,String>)JSON.parse(request.getBizContent());
		
		requestHolder.getApplicationParams().putAll(bizContent);
		
		//	设置必要参数
		requestHolder.getApplicationParams().put(PayConstants.APPID, this.appId);
		requestHolder.getApplicationParams().put(PayConstants.NONCE_STR, String.valueOf(System.currentTimeMillis()));
		
		//	设置非必要参数
		requestHolder.getApplicationParams().put(PayConstants.DEVICE_INFO, this.deviceInfo);
		requestHolder.getApplicationParams().put(PayConstants.SIGN_TYPE, this.signType);
		requestHolder.getApplicationParams().put(PayConstants.MCH_ID, this.mchId);
		
		if(this.signType.equals(PayConstants.SIGN_TYPE_MD5)){
			String content = PaySignature.getSignatureContent(requestHolder);
			content = content + "&" + PayConstants.KEY + "=" + this.privateKey;
			logger.info("内容:" + content);
			requestHolder.getApplicationParams().put(PayConstants.SIGN, PaySignature.signWithMD5(content,this.charset));
		}
		
		return requestHolder;
	}
	
	public static void main(String[] args) {
		
	}


	
	
}
