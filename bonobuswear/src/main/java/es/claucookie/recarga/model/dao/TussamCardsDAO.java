package es.claucookie.recarga.model.dao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import es.claucookie.recarga.model.dto.*;

/**
 * DAO for entity TussamCardsDAO, Singleton
 * @author Service Generator
 *
 * Generated Class - DO NOT MODIFY
 */
public final class TussamCardsDAO{

	// XML field constants
	private static final String CONSTANT_CARDS="cards";
	
	private static TussamCardsDAO instance=new TussamCardsDAO();

	private TussamCardsDAO(){
		
	}
	/**
	 * Gets DAO instance
	 * @returns instance
	 */
	public static TussamCardsDAO getInstance(){
		return instance;
	}
	
	/**
	 * Build an TussamCardsDTO from JSON
	 */
	public TussamCardsDTO create(JSONObject value) throws JSONException{
		TussamCardsDTO returnValue=new TussamCardsDTO();
		
		
		
		
		ArrayList<TussamCardDTO> cardsList=new ArrayList<TussamCardDTO>();
		if(value.has(CONSTANT_CARDS) && !value.get(CONSTANT_CARDS).toString().equals("null")) {
			if(value.get(CONSTANT_CARDS) instanceof JSONArray){
				JSONArray cardsArray=(JSONArray)value.get(CONSTANT_CARDS);
				for(int i=0;i!=cardsArray.length();i++){
					JSONObject obj=(JSONObject)cardsArray.get(i);
					cardsList.add(TussamCardDAO.getInstance().create(obj));
				}
			}
			else{
				cardsList.add(TussamCardDAO.getInstance().create((JSONObject)value.get(CONSTANT_CARDS)));
			}
		}
		returnValue.setCards(cardsList);

		return returnValue;
	}
	
	/**
	 * Build JSON (JSONObject) from a DTO object
	 */
	public JSONObject serialize(TussamCardsDTO object) throws JSONException {		
		JSONObject returnValue = new JSONObject();
		
		if(object.getCards()!=null){
			JSONArray cardsArray = new JSONArray();
			for (TussamCardDTO cardsObject : object.getCards()) {
				cardsArray.put(TussamCardDAO.getInstance().serialize(cardsObject));
			}
			returnValue.put(CONSTANT_CARDS,cardsArray);
		}

		return returnValue;
	}
}
