package app.intelehealth.client.models;

public class LanguagesModel
{


    String languageName;
    String languageSpec;
    boolean isLanguageSelected;

    public LanguagesModel(String languageName, String languageSpec, boolean isLanguageSelected) {
        this.languageName = languageName;
        this.languageSpec = languageSpec;
        this.isLanguageSelected = isLanguageSelected;
    }
    public String getLanguageName() {
        return languageName;
    }
    public String getLanguageSpec() {
        return languageSpec;
    }
    public boolean getLanguageSelected() {
        return isLanguageSelected;
    }
    public void setLanguageSelected(boolean languageSelected) {
        isLanguageSelected = languageSelected;
    }


}
