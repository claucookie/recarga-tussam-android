package es.claucookie.recarga.helpers;

import org.androidannotations.annotations.sharedpreferences.SharedPref;

import java.util.ArrayList;


/**
 * Created by claucookie on 23/03/14.
 */
@SharedPref(value=SharedPref.Scope.UNIQUE)
public interface SharedPrefsHelper {


    String cardNumbersString();

}