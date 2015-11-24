/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.user;

import java.util.List;
import urbosenti.core.data.dao.UserDAO;
import urbosenti.core.device.model.Instance;
import urbosenti.core.device.model.InstanceRepresentative;

/**
 *
 * @author Guilherme
 */
public final class User implements InstanceRepresentative{

    /**
     * Indica o estado termo de privacidade. Valor 4;
     */
    public static final int STATE_PRIVACY_TERM = 4;
    /**
     * Indica o estado sobre a permissão do usuário para compartilhar dados. Valor 5
     */
    public static final int STATE_PRIVACY_DATA_SHARING = 5;
    /**
     * Indica o estado sobre a opção do usuário pelos dados enviados serem
     * tratados como anônimos. Valor 6
     */
    public static final int STATE_PRIVACY_ANONYMOUS_UPLOAD = 6;

    private int id;
    private String login;
    private String password;
    private Boolean acceptedPrivacyTerm;
    private Boolean acceptedDataSharing;
    private Boolean optedByAnonymousUpload;
    private Boolean isBeingMonitored;
    private int userPosition;
    private Instance instance;
    private List<UserPreference> userPreferences;

    public User() {
    }

    public User(Instance instance, List<UserPreference> userPreferences) {
        this.instance = instance;
        this.setInstance(instance);
        this.userPreferences = userPreferences;
    }
    
    public User(Instance instance) {
        this.instance = instance;
        this.setInstance(instance);
    }
    
    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getAcceptedPrivacyTerm() {
        return acceptedPrivacyTerm;
    }

    public void setAcceptedPrivacyTerm(Boolean acceptedPrivacyTerm) {
        this.acceptedPrivacyTerm = acceptedPrivacyTerm;
    }

    public Boolean getAcceptedDataSharing() {
        return acceptedDataSharing;
    }

    public void setAcceptedDataSharing(Boolean acceptedDataSharing) {
        this.acceptedDataSharing = acceptedDataSharing;
    }

    public Boolean getOptedByAnonymousUpload() {
        return optedByAnonymousUpload;
    }

    public void setOptedByAnonymousUpload(Boolean hasChoseAnonymousUpload) {
        this.optedByAnonymousUpload = hasChoseAnonymousUpload;
    }

    public List<UserPreference> getUserPreferences() {
        return userPreferences;
    }

    public void setUserPreferences(List<UserPreference> userPreferences) {
        this.userPreferences = userPreferences;
    }

    public Boolean isBeingMonitored() {
        return isBeingMonitored;
    }

    public void setIsBeingMonitored(Boolean isBeingMonitored) {
        this.isBeingMonitored = isBeingMonitored;
    }

    @Override
    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
        this.id = instance.getId();
        this.login = UserDAO.getStateFromUserInstance(
                UserDAO.STATE_ID_OF_USER_MANAGEMENT_USER_LOGIN, instance).getCurrentValue().toString();
        this.password = UserDAO.getStateFromUserInstance(
                UserDAO.STATE_ID_OF_USER_MANAGEMENT_USER_LOGIN, instance).getCurrentValue().toString();
        this.acceptedPrivacyTerm = (Boolean)UserDAO.getStateFromUserInstance(
                UserDAO.STATE_ID_OF_USER_MANAGEMENT_USER_PRIVACY_TERM, instance).getCurrentValue();
        this.acceptedDataSharing = (Boolean)UserDAO.getStateFromUserInstance(
                UserDAO.STATE_ID_OF_USER_MANAGEMENT_SYSTEM_DATA_SHARING_PERMISSION_BY_USER, instance).getCurrentValue();
        this.optedByAnonymousUpload = (Boolean)UserDAO.getStateFromUserInstance(
                UserDAO.STATE_ID_OF_USER_MANAGEMENT_ANONYMOUS_UPLOAD, instance).getCurrentValue();
        this.isBeingMonitored = (Boolean)UserDAO.getStateFromUserInstance(
                UserDAO.STATE_ID_OF_USER_MANAGEMENT_USER_BEING_MONITORED, instance).getCurrentValue();
        this.userPosition = (Integer)UserDAO.getStateFromUserInstance(
                UserDAO.STATE_ID_OF_USER_MANAGEMENT_USER_POSITION, instance).getCurrentValue();
    }

    public int getUserPosition() {
        return userPosition;
    }

    public void setUserPosition(int userPosition) {
        this.userPosition = userPosition;
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", isBeingMonitored=" + isBeingMonitored + ", instance=" + instance.getId() + '}';
    }
    
}
