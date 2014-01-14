package fr.ece.ostis.lang;

import java.util.Locale;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;



public class LanguageManager {
	
	/*public static final int NUMBER = 2;
	
	public static final int FRENCH = 0;
    public static final int ENGLISH = 1;
    
    public static final int DEFAULT = ENGLISH;
    public static int Value = DEFAULT;
    */
	
	/**
	 * TODO
	 */
    protected static final Locale[] _SupportedLocales = {Locale.ENGLISH, Locale.FRENCH};
    
    /**
     * TODO
     */
    private Context _Context = null;
    
    
    /**
     * TODO
     * @return
     */
    public String getSystemLanguage(){
		return null;
    }
    
    
    /**
     * 
     */
    public void useSystemLanguage(){
    	
    	
    }
    
    
    public void useLanguage(String _Language){
    	
        Locale _Locale = new Locale(_Language);
        Resources _Resources = _Context.getResources();
        Configuration _Configuration = _Resources.getConfiguration();
        _Configuration.locale = _Locale;
        _Resources.updateConfiguration(_Configuration, _Resources.getDisplayMetrics());
    	
    }
}
