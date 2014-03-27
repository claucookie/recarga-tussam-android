package es.claucookie.recarga.model.dto;

import android.os.Parcelable;
import android.os.Parcel;
import org.json.JSONObject;
import es.claucookie.recarga.model.dto.base.RecargaTussamDTOBundle;
import es.claucookie.recarga.model.dao.*;

/**
 * Extended DTO TussamCardDTO
 * This DTO CAN be extended (will not be overwritten by generator)
 * @see es.claucookie.recarga.model.dto.base.RecargaTussamDTOBundle.BaseTussamCardDTO
 * @author Service Generator
 */
public final class TussamCardDTO extends RecargaTussamDTOBundle.BaseTussamCardDTO {
    /**
     * Creates DTO with default values
     */
    public TussamCardDTO() {
      super();
  }

    /**
     * Creates DTO from Parcel Data
     */
    public TussamCardDTO(Parcel in) {
      super(in);
  }

  @Override
  public boolean equals(final Object object) {

    if (this == object) return true;
    if (!(object instanceof TussamCardDTO)) return false;
    final TussamCardDTO other = (TussamCardDTO) object;
    try {
       JSONObject thisJson = TussamCardDAO.getInstance().serialize(this);
       JSONObject otherJson = TussamCardDAO.getInstance().serialize(other);        
       return thisJson.toString().equals(otherJson.toString());
   } catch(Exception ex) {
       ex.printStackTrace();
   } 
   
   return false;
}

@Override
public int hashCode() {
   try {
       JSONObject thisJson = TussamCardDAO.getInstance().serialize(this);
       return thisJson.toString().hashCode();
   } catch(Exception ex) {
       ex.printStackTrace();
       return ex.hashCode();
   } 
}

	/**
	 * Static Parcelable serializer/deserializer
	 */
	public static final Parcelable.Creator<TussamCardDTO> CREATOR =
  new Parcelable.Creator<TussamCardDTO>() {
     public TussamCardDTO createFromParcel(Parcel in) {
        return new TussamCardDTO(in);
    }
    public TussamCardDTO[] newArray(int size) {
        return new TussamCardDTO[size];
    }
};
}
