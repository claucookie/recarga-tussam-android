package es.claucookie.recarga.model.dto.base;

import java.util.List;
import java.util.ArrayList;
import android.os.Parcel;
import android.os.Parcelable;
import java.io.File;
import es.claucookie.recarga.model.dto.*;

/**
 * All the DTOs in a single file
 * This file will be overwritten with each generation so place the code in the inherited classes
 * @author Service Generator
 *
 * Generated Class - DO NOT MODIFY
 */
public class RecargaTussamDTOBundle {
	/**
	 * DTO defining class TussamCardsDTO
	 */
	public static class BaseTussamCardsDTO implements Parcelable {
		public BaseTussamCardsDTO(Parcel in) {
			readFromParcel(in);
		}
	
		public BaseTussamCardsDTO() {
		}
	
		// Field name on service:cards
		private List<TussamCardDTO> cards; 
		
		// Setters y Getters
		
		/**
		 * Setter de la propiedad cards , 
		 * Field name on service:cards
		 * @param cards valor a establecer en el set
		 */
		public void setCards(List<TussamCardDTO> cards){
			this.cards=cards;
		}
		/**
		 * Getter de la propiedad cards , 
		 * Field name on service:cards
		 * @returns Valor de la propiedad cards
		 */
		public List<TussamCardDTO> getCards(){
			return cards;
		}
		
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {

			if(cards!=null){
                dest.writeByte((byte)1);
			    dest.writeTypedList(cards);
            }
			else{
			    dest.writeByte((byte)0);
			}
		}
		
		public void readFromParcel(Parcel in) {

            if(in.readByte()==1)
            {
			cards = new ArrayList<TussamCardDTO>();
			in.readTypedList(cards, TussamCardDTO.CREATOR);
			}
		}
		
		public static final Parcelable.Creator<BaseTussamCardsDTO> CREATOR =
			new Parcelable.Creator<BaseTussamCardsDTO>() {
				public BaseTussamCardsDTO createFromParcel(Parcel in) {
					return new BaseTussamCardsDTO(in);
				}
				public BaseTussamCardsDTO[] newArray(int size) {
					return new BaseTussamCardsDTO[size];
				}
			};
			
	}
	/**
	 * DTO defining class TussamCardDTO
	 */
	public static class BaseTussamCardDTO implements Parcelable {
		public BaseTussamCardDTO(Parcel in) {
			readFromParcel(in);
		}
	
		public BaseTussamCardDTO() {
		}
	
		// Field name on service:cardName
		private String cardName; 
		// Field name on service:cardNumber
		private String cardNumber; 
		// Field name on service:cardType
		private String cardType; 
		// Field name on service:cardStatus
		private String cardStatus; 
		// Field name on service:cardCredit
		private String cardCredit; 
		
		// Setters y Getters
		
		/**
		 * Setter de la propiedad cardName , 
		 * Field name on service:cardName
		 * @param cardName valor a establecer en el set
		 */
		public void setCardName(String cardName){
			this.cardName=cardName;
		}
		/**
		 * Getter de la propiedad cardName , 
		 * Field name on service:cardName
		 * @returns Valor de la propiedad cardName
		 */
		public String getCardName(){
			return cardName;
		}
		/**
		 * Setter de la propiedad cardNumber , 
		 * Field name on service:cardNumber
		 * @param cardNumber valor a establecer en el set
		 */
		public void setCardNumber(String cardNumber){
			this.cardNumber=cardNumber;
		}
		/**
		 * Getter de la propiedad cardNumber , 
		 * Field name on service:cardNumber
		 * @returns Valor de la propiedad cardNumber
		 */
		public String getCardNumber(){
			return cardNumber;
		}
		/**
		 * Setter de la propiedad cardType , 
		 * Field name on service:cardType
		 * @param cardType valor a establecer en el set
		 */
		public void setCardType(String cardType){
			this.cardType=cardType;
		}
		/**
		 * Getter de la propiedad cardType , 
		 * Field name on service:cardType
		 * @returns Valor de la propiedad cardType
		 */
		public String getCardType(){
			return cardType;
		}
		/**
		 * Setter de la propiedad cardStatus , 
		 * Field name on service:cardStatus
		 * @param cardStatus valor a establecer en el set
		 */
		public void setCardStatus(String cardStatus){
			this.cardStatus=cardStatus;
		}
		/**
		 * Getter de la propiedad cardStatus , 
		 * Field name on service:cardStatus
		 * @returns Valor de la propiedad cardStatus
		 */
		public String getCardStatus(){
			return cardStatus;
		}
		/**
		 * Setter de la propiedad cardCredit , 
		 * Field name on service:cardCredit
		 * @param cardCredit valor a establecer en el set
		 */
		public void setCardCredit(String cardCredit){
			this.cardCredit=cardCredit;
		}
		/**
		 * Getter de la propiedad cardCredit , 
		 * Field name on service:cardCredit
		 * @returns Valor de la propiedad cardCredit
		 */
		public String getCardCredit(){
			return cardCredit;
		}
		
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			if(cardName!=null){
			    dest.writeByte((byte)1);
			    dest.writeString(cardName);
			}
			else{
			    dest.writeByte((byte)0);
			}
			if(cardNumber!=null){
			    dest.writeByte((byte)1);
			    dest.writeString(cardNumber);
			}
			else{
			    dest.writeByte((byte)0);
			}
			if(cardType!=null){
			    dest.writeByte((byte)1);
			    dest.writeString(cardType);
			}
			else{
			    dest.writeByte((byte)0);
			}
			if(cardStatus!=null){
			    dest.writeByte((byte)1);
			    dest.writeString(cardStatus);
			}
			else{
			    dest.writeByte((byte)0);
			}
			if(cardCredit!=null){
			    dest.writeByte((byte)1);
			    dest.writeString(cardCredit);
			}
			else{
			    dest.writeByte((byte)0);
			}

		}
		
		public void readFromParcel(Parcel in) {

	
	        if(in.readByte()==1){
			cardName = in.readString();}
	
	        if(in.readByte()==1){
			cardNumber = in.readString();}
	
	        if(in.readByte()==1){
			cardType = in.readString();}
	
	        if(in.readByte()==1){
			cardStatus = in.readString();}
	
	        if(in.readByte()==1){
			cardCredit = in.readString();}
		}
		
		public static final Parcelable.Creator<BaseTussamCardDTO> CREATOR =
			new Parcelable.Creator<BaseTussamCardDTO>() {
				public BaseTussamCardDTO createFromParcel(Parcel in) {
					return new BaseTussamCardDTO(in);
				}
				public BaseTussamCardDTO[] newArray(int size) {
					return new BaseTussamCardDTO[size];
				}
			};
			
	}


}