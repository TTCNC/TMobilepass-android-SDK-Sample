package kr.co.ttcnc.ucsdk.dccctrllib.nfc;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import kr.co.ttcnc.ucsdk.dccctrllib.util.TLog;
import kr.co.ttcnc.ucsdk.dccctrllib.util.Utils;

/**
 * Created by giftn on 2016-03-10.
 */
class LiberoPrefManager {
    private Context mContext;
    private SharedPreferences prefs;
    private SharedPreferences.Editor edit;

    private static LiberoPrefManager self = null;
    private final static String PrefName = "kr.co.ttcnc.ucsdk_preferences";

    public static LiberoPrefManager getInstance(Context context) {
        if (self == null) {
            self = new LiberoPrefManager(context);
        } else {
            self.setContext(context);
        }

        return self;
    }

    private LiberoPrefManager(Context context) {
        mContext = context;
    }

    private void setContext(Context context) {
        mContext = context;
    }


    /**
     * Save Int of Sound , ON / OFF
     * @param soundMode int
     */
    public void setSoundMode(int soundMode) {
        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            edit = prefs.edit();
            edit.putInt("MOBILEPASS_SOUND_MODE", soundMode);
            edit.commit();
        }
    }

    /**
     * Get Int of pause on mode , clear mode , active mode
     * @return java.lang.byte[]
     *  0 : ALWAYS ALIVE
     *  20 ~ 60 : ALIVE TIME FIX
     */
    public int getSoundMode() {
        int soundMode = 0;

        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            soundMode = prefs.getInt("MOBILEPASS_SOUND_MODE", 1);
        }

        return soundMode;
    }

    /**
     * Save Int of pause on mode , clear mode , active mode
     * @param option int
     */
    public void setActivePauseState(int option) {
        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            edit = prefs.edit();
            edit.putInt("MOBILEPASS_TOKEN_ACTIVE_PAUSE_STATE", option);
            edit.commit();
            TLog.i("PREF", "setActivePauseState=" + option);
        }
    }

    /**
     * Get Int of pause on mode , clear mode , active mode
     * @return java.lang.byte[]
     *  0 : ALWAYS ALIVE
     *  20 ~ 60 : ALIVE TIME FIX
     */
    public int getActivePauseState() {
        int option = 0;

        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            option = prefs.getInt("MOBILEPASS_TOKEN_ACTIVE_PAUSE_STATE", 0);
        }

        return option;
    }

    /**
     * Save Int of Option
     * @param option int
     */
    public void setTokenAliveOption(int option) {
        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            edit = prefs.edit();
            edit.putInt("MOBILEPASS_TOKEN_ALIVE_OPTION", option);
            edit.commit();
            TLog.i("PREF", "setTokenAliveOption=" + option);
        }
    }

    /**
     * Get Int of Option
     * @return java.lang.byte[]
     *  0 : ALWAYS ALIVE
     *  20 ~ 60 : ALIVE TIME FIX
     */
    public int getTokenAliveOption() {
        int option = 0;

        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            option = prefs.getInt("MOBILEPASS_TOKEN_ALIVE_OPTION", 0);
        }

        TLog.i("PREF", "getTokenAliveOption=" + option);
        return option;
    }

    /**
     * Save Int of enCrypt Mode
     * @param enCryptMode ints
     *  0 : Plain Text Mode
     *  1 : enCrypt Mode
     */
    public void setEnCryptModes(int enCryptMode) {
        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            edit = prefs.edit();
            edit.putInt("MOBILEPASS_TOKEN_CNCRYPT_MODE", enCryptMode);
            edit.commit();
        }
    }

    /**
     * Get Int of enCrypt Mode
     * @return java.lang.int
     *  0 : Plain Text Mode
     *  1 : enCrypt Mode
     */
    public int getEnCryptModes() {
        int enCryptMode = 0;

        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            enCryptMode = prefs.getInt("MOBILEPASS_TOKEN_CNCRYPT_MODE", 0);
        }
        return enCryptMode;
    }

    /**
     * Save Byte to HexString
     * @param bToken byte[]
     */
    public void setToken(byte[] bToken) {
        if (mContext != null) {
            String sToken = Utils.hexStringFromByteArray(bToken);

            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            edit = prefs.edit();
            edit.putString("MOBILEPASS_TOKEN", sToken);
            edit.commit();
        }
    }

    /**
     * Get HexString to Byte
     * @return java.lang.byte[]
     */
    public byte[] getToken() {
        byte[] bToken = null;

        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            String stoken = prefs.getString("MOBILEPASS_TOKEN", null);
            TLog.i("PREF", "getToken=" + stoken);
            if (stoken != null)
                bToken = Utils.bytesFromHexString(stoken);
        }

        return bToken;
    }

    /**
     * Save Plain Text to HexString
     * @param sToken String
     */
    public void setStringToken(String sToken) {
        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            edit = prefs.edit();
            edit.putString("MOBILEPASS_TOKEN", Utils.stringToHexString(sToken));
            edit.commit();
        }
    }

    /**
     * Get HexString Key to String
     * @return java.lang.String
     */
    public String getStringToken() {
        String sToken = null;

        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            sToken = prefs.getString("MOBILEPASS_TOKEN", null);
        }

        if (sToken != null)
            return Utils.hexStringToString(sToken);

        return null;
    }

    /**
     * Save Reader Model Name
     * @param sReaderModel String
     */
    public void setReaderModel(String sReaderModel) {
        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            edit = prefs.edit();
            edit.putString("MOBILEPASS_READERMODEL_NAME", sReaderModel);
            edit.commit();
        }
    }

    /**
     * Get  Reader Model Name
     * @return java.lang.String
     */
    public String getReaderModel() {
        String sReaderModelName = null;

        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            sReaderModelName = prefs.getString("MOBILEPASS_READERMODEL_NAME", null);
        }

        if (sReaderModelName != null)
            return sReaderModelName;

        return null;
    }

    /**
     * Save Plain Text to CompanyName
     * @param CompanyName String
     */
    public void setCompanyName(String CompanyName) {
        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            edit = prefs.edit();
            edit.putString("MOBILEPASS_COMPANYNAME", CompanyName);
            edit.commit();
        }
    }

    /**
     * Get CompanyName to String
     * @return java.lang.String
     */
    public String getCompanyName() {
        String sCompanyName = null;

        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            sCompanyName = prefs.getString("MOBILEPASS_COMPANYNAME", null);
        }
        return sCompanyName;
    }


    /**
     * Save Plain Text to UserName
     * @param UserName String
     */
    public void setUserName(String UserName) {
        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            edit = prefs.edit();
            edit.putString("MOBILEPASS_YOURNAME", UserName);
            edit.commit();
        }
    }

    /**
     * Get UserName to String
     * @return java.lang.String
     */
    public String getUserName() {
        String sUserName = null;

        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            sUserName = prefs.getString("MOBILEPASS_YOURNAME", null);
        }
        return sUserName;
    }

    /**
     * Save Plain Text to CompanyId
     * @param UserId String
     */
    public void setUserId(String UserId) {
        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            edit = prefs.edit();
            edit.putString("MOBILEPASS_USERID", UserId);
            edit.commit();
        }
    }

    /**
     * Get UserId                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              to String
     * @return java.lang.String
     */
    public String getUserId() {
        String sUserId = null;

        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            sUserId = prefs.getString("MOBILEPASS_USERID", null);
        }
        return sUserId;
    }

    /**
     * Save PhoneNo Text to MOBILEPASS_USERPHONENO
     * @param sPhoneNo String
     */
    public void setPhoneNo(String sPhoneNo) {

        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            edit = prefs.edit();
            edit.putString("MOBILEPASS_USERPHONENO", sPhoneNo);
            edit.commit();
        }
    }

    /**
     * Get sPhoneNo                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              to String
     * @return java.lang.String
     */
    public String getPhoneNo() {
        String sPhoneNo = null;

        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            sPhoneNo = prefs.getString("MOBILEPASS_USERPHONENO", "00000000000");
        }
        return sPhoneNo;
    }

    /**
     * Save Regist Date
     * @param RegistDate String
     */
    public void setRegistDate(String RegistDate) {

        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            edit = prefs.edit();
            edit.putString("MOBILEPASS_REGISTDATE", RegistDate);
            edit.commit();
        }
    }

    /**
     * Get RegistDate                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              to String
     * @return java.lang.String
     */
    public String getRegistDate() {
        String sRegistDate = null;

        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            sRegistDate = prefs.getString("MOBILEPASS_REGISTDATE", null);
        }
        return sRegistDate;
    }

    /**
     * Save End Date
     * @param EndDate String
     */
    public void setEndDate(String EndDate) {
        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            edit = prefs.edit();
            edit.putString("MOBILEPASS_ENDDATE", EndDate);
            edit.commit();
        }
    }

    /**
     *
     * @return
     */
    public String getEndDate() {
        String sEndDate = null;

        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            sEndDate = prefs.getString("MOBILEPASS_ENDDATE", null);
        }
        return sEndDate;
    }


    /**
     *
     * @param Key
     * @param Value
     */
    public void setKeyValuePreference(String Key, String Value) {

        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            edit = prefs.edit();
            edit.putString(Key, Value);
            edit.commit();
        }
    }

    /**
     *
     * @param Key
     * @return
     */
    public String getKeyValuePreference(String Key) {
        String sValue = null;

        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            sValue = prefs.getString(Key, null);
        }
        return sValue;
    }

    /**
     *
     * @param readerModel
     */
    public void setReaderModelPreference(ReaderModel readerModel) {
        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            edit = prefs.edit();
            edit.putString("ReaderModel", readerModel.toString());
            edit.commit();
        }
    }

    /**
     *
     * @return
     */
    public ReaderModel getReaderModelPreference() {
        String enumString = null;

        if (mContext != null) {
            prefs = mContext.getSharedPreferences(PrefName, MODE_PRIVATE);
            enumString = prefs.getString("ReaderModel", null);
        }

        return ReaderModel.valueOf(enumString);
    }
}
