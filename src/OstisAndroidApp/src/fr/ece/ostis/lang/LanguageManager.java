package fr.ece.ostis.lang;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.util.Log;


/**
 * Class used for managing languages in the application. Future uses might 
 * include changing on the fly the current language in the application.
 * @author Nicolas Schurando
 * @version 2014-01-24
 */
public class LanguageManager {

	
	/**
	 * The list of supported locales.
	 */
    protected static final List<Locale> mSupportedLocales = Arrays.asList(Locale.ENGLISH, Locale.FRENCH);
    
    
    /**
     * The default locale to be used if the locale of the system is not part of the supported ones.
     */
    protected static final Locale mDefaultLocale = Locale.ENGLISH;
    
    
    /**
     * Used to store the dertermined current locale.
     */
	protected Locale mCurrentLocale = null;
    
	
    /**
     * TODO
     * @return The locale to be used for the application.
     */
    public Locale getCurrentLocale(){
    	if(mCurrentLocale == null) mCurrentLocale = Locale.getDefault();
    	
    	String language = Locale.getDefault().getLanguage().substring(0 , 2);
    	if(language.equalsIgnoreCase("fr")) mCurrentLocale = Locale.FRENCH;
    	else if(language.equalsIgnoreCase("en")) mCurrentLocale = Locale.ENGLISH;
    	
    	Log.d("LanguageManager", "Before = " + mCurrentLocale.toString());
    	
    	if(!mSupportedLocales.contains(mCurrentLocale)) mCurrentLocale = mDefaultLocale;
    	
    	Log.d("LanguageManager", "After = " + mCurrentLocale.toString());
    	
    	return mCurrentLocale;
    }

}
