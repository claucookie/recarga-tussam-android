package es.claucookie.recarga.model.dto;

import android.os.Parcelable;
import android.os.Parcel;
import org.json.JSONObject;
import es.claucookie.recarga.model.dto.base.RecargaTussamDTOBundle;
import es.claucookie.recarga.model.dao.*;

/**
 * Extended DTO TussamCardsDTO
 * This DTO CAN be extended (will not be overwritten by generator)
 * @see es.claucookie.recarga.model.dto.base.RecargaTussamDTOBundle.BaseTussamCardsDTO
 * @author Service Generator
 */
public final class TussamCardsDTO extends RecargaTussamDTOBundle.BaseTussamCardsDTO {
    /**
     * Creates DTO with default values
     */
    public TussamCardsDTO() {
      super();
  }

    /**
     * Creates DTO from Parcel Data
     */
    public TussamCardsDTO(Parcel in) {
      super(in);
  }

  @Override
  public boolean equals(final Object object) {

    if (this == object) return true;
    if (!(object instanceof TussamCardsDTO)) return false;
    final TussamCardsDTO other = (TussamCardsDTO) object;
    try {
       JSONObject thisJson = TussamCardsDAO.getInstance().serialize(this);
       JSONObject otherJson = TussamCardsDAO.getInstance().serialize(other);        
       return thisJson.toString().equals(otherJson.toString());
   } catch(Exception ex) {
       ex.printStackTrace();
   } 
   
   return false;
}

@Override
public int hashCode() {
   try {
       JSONObject thisJson = TussamCardsDAO.getInstance().serialize(this);
       return thisJson.toString().hashCode();
   } catch(Exception ex) {
       ex.printStackTrace();
       return ex.hashCode();
   } 
}

	/**
	 * Static Parcelable serializer/deserializer
	 */
	public static final Parcelable.Creator<TussamCardsDTO> CREATOR =
  new Parcelable.Creator<TussamCardsDTO>() {
     public TussamCardsDTO createFromParcel(Parcel in) {
        return new TussamCardsDTO(in);
    }
    public TussamCardsDTO[] newArray(int size) {
        return new TussamCardsDTO[size];
    }
};
}
