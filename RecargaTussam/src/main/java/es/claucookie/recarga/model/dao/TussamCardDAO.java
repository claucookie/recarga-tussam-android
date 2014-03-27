package es.claucookie.recarga.model.dao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import es.claucookie.recarga.model.dto.*;

/**
 * DAO for entity TussamCardDAO, Singleton
 * @author Service Generator
 *
 * Generated Class - DO NOT MODIFY
 */
public final class TussamCardDAO{

	// XML field constants
	private static final String CONSTANT_CARDNAME="cardName";
	private static final String CONSTANT_CARDNUMBER="cardNumber";
	private static final String CONSTANT_CARDTYPE="cardType";
	private static final String CONSTANT_CARDSTATUS="cardStatus";
	private static final String CONSTANT_CARDCREDIT="cardCredit";
	
	private static TussamCardDAO instance=new TussamCardDAO();

	private TussamCardDAO(){
		
	}
	/**
	 * Gets DAO instance
	 * @returns instance
	 */
	public static TussamCardDAO getInstance(){
		return instance;
	}
	
	/**
	 * Build an TussamCardDTO from JSON
	 */
	public TussamCardDTO create(JSONObject value) throws JSONException{
		TussamCardDTO returnValue=new TussamCardDTO();
		
		
		if(value.has(CONSTANT_CARDNAME) && !value.get(CONSTANT_CARDNAME).toString().equals("null")) {
			returnValue.setCardName(value.get(CONSTANT_CARDNAME).toString());
		}
		
		if(value.has(CONSTANT_CARDNUMBER) && !value.get(CONSTANT_CARDNUMBER).toString().equals("null")) {
			returnValue.setCardNumber(value.get(CONSTANT_CARDNUMBER).toString());
		}
		
		if(value.has(CONSTANT_CARDTYPE) && !value.get(CONSTANT_CARDTYPE).toString().equals("null")) {
			returnValue.setCardType(value.get(CONSTANT_CARDTYPE).toString());
		}
		
		if(value.has(CONSTANT_CARDSTATUS) && !value.get(CONSTANT_CARDSTATUS).toString().equals("null")) {
			returnValue.setCardStatus(value.get(CONSTANT_CARDSTATUS).toString());
		}
		
		if(value.has(CONSTANT_CARDCREDIT) && !value.get(CONSTANT_CARDCREDIT).toString().equals("null")) {
			returnValue.setCardCredit(value.get(CONSTANT_CARDCREDIT).toString());
		}
		
		
		

		return returnValue;
	}
	
	/**
	 * Build JSON (JSONObject) from a DTO object
	 */
	public JSONObject serialize(TussamCardDTO object) throws JSONException {		
		JSONObject returnValue = new JSONObject();
		
		if(object.getCardName()!=null){
			returnValue.put(CONSTANT_CARDNAME, (object.getCardName() == null)? JSONObject.NULL : object.getCardName());
		}
		if(object.getCardNumber()!=null){
			returnValue.put(CONSTANT_CARDNUMBER, (object.getCardNumber() == null)? JSONObject.NULL : object.getCardNumber());
		}
		if(object.getCardType()!=null){
			returnValue.put(CONSTANT_CARDTYPE, (object.getCardType() == null)? JSONObject.NULL : object.getCardType());
		}
		if(object.getCardStatus()!=null){
			returnValue.put(CONSTANT_CARDSTATUS, (object.getCardStatus() == null)? JSONObject.NULL : object.getCardStatus());
		}
		if(object.getCardCredit()!=null){
			returnValue.put(CONSTANT_CARDCREDIT, (object.getCardCredit() == null)? JSONObject.NULL : object.getCardCredit());
		}

		return returnValue;
	}
}
