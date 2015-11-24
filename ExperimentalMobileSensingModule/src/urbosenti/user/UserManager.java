/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.user;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import urbosenti.core.data.dao.UserDAO;
import urbosenti.core.device.ComponentManager;
import urbosenti.core.device.DeviceManager;
import urbosenti.core.device.model.ActionModel;
import urbosenti.core.device.model.FeedbackAnswer;
import urbosenti.core.device.model.Parameter;
import urbosenti.core.device.model.TargetOrigin;
import urbosenti.core.events.Action;
import urbosenti.core.events.ApplicationEvent;
import urbosenti.core.events.Event;
import urbosenti.util.DeveloperSettings;

/**
 *
 * @author Guilherme
 */
public class UserManager extends ComponentManager {

    /**
     * int EVENT_USER_ADDED = 1;
     *
     * <ul><li>id: 1</li>
     * <li>evento: Usuário incluído</li>
     * <li>parâmetros: Usuário</li></ul>
     */
    public static final int EVENT_USER_ADDED = 1;
    /**
     * int EVENT_USER_UPDATED = 2;
     *
     * <ul><li>id: 2</li>
     * <li>evento: Usuário alterado</li>
     * <li>parâmetros: Usuário</li></ul>
     */
    public static final int EVENT_USER_UPDATED = 2;
    /**
     * int EVENT_USER_DELETED = 3;
     *
     * <ul><li>id: 3</li>
     * <li>evento: Usuário excluído</li>
     * <li>parâmetros: Usuário</li></ul>
     */
    public static final int EVENT_USER_DELETED = 3;
    /**
     * int EVENT_USER_READ = 4;
     *
     * <ul><li>id: 4</li>
     * <li>evento: Usuário acessado para leitura</li>
     * <li>parâmetros: Usuário</li></ul>
     */
    public static final int EVENT_USER_READ = 4;
    /**
     * int EVENT_USER_CHOSEN_TO_BE_MONITORED = 5;
     *
     * <ul><li>id: 5</li>
     * <li>evento: Perfil escolhido de usuário a ser monitorado</li>
     * <li>parâmetros: Usuário</li></ul>
     */
    public static final int EVENT_USER_CHOSEN_TO_BE_MONITORED = 5;
    /**
     * int EVENT_USER_IS_LOGGED_IN = 6;
     *
     * <ul><li>id: 6</li>
     * <li>evento: Usuário Executou Login</li>
     * <li>parâmetros: Usuário</li></ul>
     */
    public static final int EVENT_USER_IS_LOGGED_IN = 6;
    /**
     * int EVENT_USER_IS_LOGGED_OUT = 7;
     *
     * <ul><li>id: 7</li>
     * <li>evento: Usuário Executou Logoff</li>
     * <li>parâmetros: Usuário</li></ul>
     */
    public static final int EVENT_USER_IS_LOGGED_OUT = 7;
    /**
     * int EVENT_USER_CHANGED_SYSTEM_CONFIGURATION = 8;
     *
     * <ul><li>id: 8</li>
     * <li>evento: Configuração de sistema alterada pelo usuário</li>
     * <li>parâmetros: Componente,Entidade,Estado,Novo Valor</li></ul>
     */
    public static final int EVENT_USER_CHANGED_SYSTEM_CONFIGURATION = 8;
    /**
     * int EVENT_USER_CHANGED_PRIVACY_CONFIGURATION = 9;
     *
     * <ul><li>id: 9</li>
     * <li>evento: Configuração de privacidade alterada</li>
     * <li>parâmetros: Estado,Novo Valor</li></ul>
     */
    public static final int EVENT_USER_CHANGED_PRIVACY_CONFIGURATION = 9;
    private static User loggedUser;

    public UserManager(DeviceManager deviceManager) {
        super(deviceManager, UserDAO.COMPONENT_ID);
    }

    @Override
    public void onCreate() {
        // Carregar dados e configurações que serão utilizados para execução em memÃ³ria
        // Preparar configurações inicias para execução
        // Para tanto utilizar o DataManager para acesso aos dados.
        if(DeveloperSettings.SHOW_FUNCTION_DEBUG_ACTIVITY){
            System.out.println("Activating: " + getClass());
        }
    }

    /**
     * Ações disponibilizadas por esse componente:
     * <ul>
     * <li>Nenhuma ação ainda é suportada</li>
     * </ul>
     *
     * @param action contém objeto ação.
     * @return
     *
     */
    @Override
    public FeedbackAnswer applyAction(Action action) {
        // não tem ações ainda
        return new FeedbackAnswer(FeedbackAnswer.ACTION_DOES_NOT_EXIST);
    }

    /**
     *
     * <br><br>Eventos possíveis
     * <ul>
     * <li>UserManager.EVENT_USER_ADDED - Usuário incluído</li>
     * <li>UserManager.EVENT_USER_UPDATED - Usuário alterado</li>
     * <li>UserManager.EVENT_USER_DELETED - Usuário excluído</li>
     * <li>UserManager.EVENT_USER_READ - Usuário acessado para leitura</li>
     * <li>UserManager.EVENT_USER_CHOSEN_TO_BE_MONITORED - Perfil escolhido de
     * usuário a ser monitorado</li>
     * <li>UserManager.EVENT_USER_IS_LOGGED_IN - Usuário Executou Login</li>
     * <li>UserManager.EVENT_USER_IS_LOGGED_OUT - Usuário Executou Logoff</li>
     * <li>UserManager.EVENT_USER_CHANGED_SYSTEM_CONFIGURATION - Configuração de
     * sistema alterada pelo usuário</li>
     * <li>UserManager.EVENT_USER_CHANGED_PRIVACY_CONFIGURATION - Configuração
     * de privacidade alterada</li>
     * </ul>
     *
     * @param eventId indica o evento entre os acima
     * @param params
     * @see #EVENT_USER_ADDED
     * @see #EVENT_USER_UPDATED
     * @see #EVENT_USER_DELETED
     * @see #EVENT_USER_READ
     * @see #EVENT_USER_CHOSEN_TO_BE_MONITORED
     * @see #EVENT_USER_IS_LOGGED_IN
     * @see #EVENT_USER_IS_LOGGED_OUT
     * @see #EVENT_USER_CHANGED_SYSTEM_CONFIGURATION
     * @see #EVENT_USER_CHANGED_SYSTEM_CONFIGURATION
     */
    public void newInternalEvent(int eventId, Object... params) {
        User user;
        Event event;
        HashMap<String, Object> values;
        switch (eventId) {
            case EVENT_USER_ADDED: // 1 - Usuário incluído
                user = (User) params[0];

                // Parâmetros do evento
                values = new HashMap();
                values.put("user", user);

                // Cria o evento
                event = new ApplicationEvent(this);
                event.setId(1);
                event.setName("Usuário incluído");
                event.setTime(new Date());
                event.setParameters(values);
                event.setEntityId(UserDAO.ENTITY_ID_OF_USER_MANAGEMENT);

                // envia o evento
                getEventManager().newEvent(event);

                break;
            case EVENT_USER_UPDATED: // 2 - usuário alterado
                user = (User) params[0];

                // Parâmetros do evento
                values = new HashMap();
                values.put("user", user);

                // Cria o evento
                event = new ApplicationEvent(this);
                event.setId(2);
                event.setName("Usuário alterado");
                event.setTime(new Date());
                event.setParameters(values);
                event.setEntityId(UserDAO.ENTITY_ID_OF_USER_MANAGEMENT);

                // envia o evento
                getEventManager().newEvent(event);
                break;
            case EVENT_USER_DELETED: // 3 - usuário excluído
                user = (User) params[0];

                // Parâmetros do evento
                values = new HashMap();
                values.put("user", user);

                // Cria o evento
                event = new ApplicationEvent(this);
                event.setId(3);
                event.setName("Usuário excluído");
                event.setTime(new Date());
                event.setParameters(values);
                event.setEntityId(UserDAO.ENTITY_ID_OF_USER_MANAGEMENT);

                // envia o evento
                getEventManager().newEvent(event);
                break;
            case EVENT_USER_READ: // 4 - usuário acessado para leituras
                user = (User) params[0];

                // Parâmetros do evento
                values = new HashMap();
                values.put("user", user);

                // Cria o evento
                event = new ApplicationEvent(this);
                event.setId(4);
                event.setName("Usuário acessado para leitura");
                event.setTime(new Date());
                event.setParameters(values);
                event.setEntityId(UserDAO.ENTITY_ID_OF_USER_MANAGEMENT);

                // envia o evento
                getEventManager().newEvent(event);
                break;
            case EVENT_USER_CHOSEN_TO_BE_MONITORED: // 5 - usuário escolhido para ser monitorado
                user = (User) params[0];

                // Parâmetros do evento
                values = new HashMap();
                values.put("user", user);

                // Cria o evento
                event = new ApplicationEvent(this);
                event.setId(5);
                event.setName("Perfil escolhido de usuário a ser monitorado");
                event.setTime(new Date());
                event.setParameters(values);
                event.setEntityId(UserDAO.ENTITY_ID_OF_USER_MANAGEMENT);

                // envia o evento
                getEventManager().newEvent(event);
                break;
            case EVENT_USER_IS_LOGGED_IN: // 6 - usuário efetuou login
                user = (User) params[0];

                // Parâmetros do evento
                values = new HashMap();
                values.put("user", user);

                // Cria o evento
                event = new ApplicationEvent(this);
                event.setId(6);
                event.setName("Usuário Executou Login");
                event.setTime(new Date());
                event.setParameters(values);
                event.setEntityId(UserDAO.ENTITY_ID_OF_USER_AUTHENTICATION);

                // envia o evento
                getEventManager().newEvent(event);
                break;
            case EVENT_USER_IS_LOGGED_OUT: // usuário efetuou logout
                user = (User) params[0];

                // Parâmetros do evento
                values = new HashMap();
                values.put("user", user);

                // Cria o evento
                event = new ApplicationEvent(this);
                event.setId(7);
                event.setName("Usuário Executou Logoff");
                event.setTime(new Date());
                event.setParameters(values);
                event.setEntityId(UserDAO.ENTITY_ID_OF_USER_AUTHENTICATION);

                // envia o evento
                getEventManager().newEvent(event);
                break;
            case EVENT_USER_CHANGED_SYSTEM_CONFIGURATION: // 8 - Usuário alterou uma configuração de sistema
                user = (User) params[0];

                // Parâmetros do evento
                values = new HashMap();
                values.put("user", user);
                values.put("component", (ComponentManager) params[1]);
                values.put("entity", (Integer) params[2]);
                values.put("state", (Integer) params[3]);
                values.put("newValue", params[4]);

                // Cria o evento
                event = new ApplicationEvent(this);
                event.setId(8);
                event.setName("Configuração de sistema alterada pelo usuário");
                event.setTime(new Date());
                event.setParameters(values);
                event.setEntityId(UserDAO.ENTITY_ID_OF_USER_PROFILE_PREFERENCES);

                // envia o evento
                getEventManager().newEvent(event);
                break;
            case EVENT_USER_CHANGED_PRIVACY_CONFIGURATION: // 9 - usuário alterou uma configuração de privacidade
                user = (User) params[0];

                // Parâmetros do evento
                values = new HashMap();
                values.put("user", user);
                values.put("state", (Integer) params[1]);
                values.put("newValue", params[2]);

                // Cria o evento
                event = new ApplicationEvent(this);
                event.setId(9);
                event.setName("Configuração de privacidade alterada");
                event.setTime(new Date());
                event.setParameters(values);
                event.setEntityId(UserDAO.ENTITY_ID_OF_USER_PROFILE_PREFERENCES);

                // envia o evento
                getEventManager().newEvent(event);
                break;
        }
    }

    public User login(String userName, String password) throws SQLException {
        for (User user : getDeviceManager().getDataManager().getUserDAO().getUsers()) {
            // Checa se o usuário existe
            if (user.getLogin().equals(userName) && user.getPassword().equals(password)) {
                // Atribui o usuário em uma variável estática
                loggedUser = user;
                // Gera o evento do login
                this.newInternalEvent(UserManager.EVENT_USER_IS_LOGGED_IN, user);
                // retorna o usuário
                return user;
            }
        }
        return null;
    }

    public boolean logout() {
        // Gera o evento
        this.newInternalEvent(UserManager.EVENT_USER_IS_LOGGED_OUT, loggedUser);
        // torna o usuário nulo
        loggedUser = null;
        return true;
    }

    /**
     * Insere um usuário. Contudo, antes deve verificar se o nome do usuário e a
     * senha são válidas, e se ele aceitou o termo de privacidade.
     *
     * @param userName
     * @param password
     * @param hasAccepptedThePrivacyTerm
     * @param hasAccepptedShareData
     * @param hasChoseAnonymousUpload
     * @return
     */
    public boolean insertUser(String userName, String password, Boolean hasAccepptedThePrivacyTerm, Boolean hasAccepptedShareData, Boolean hasChoseAnonymousUpload) throws SQLException {
        // Verifica se o nome do usuário e a senha são válidos
        if (!this.checkUsernameValidity(userName) || !this.checkPasswordValidity(password)) {
            return false;
        }
        // Verifica se o usuário aceitou o termo de privacidade
        if (!hasAccepptedThePrivacyTerm) {
            return false;
        }
        // Cria um usuário e adiciona as informações
        User user = new User();
        user.setLogin(userName);
        user.setPassword(password);
        user.setAcceptedDataSharing(hasAccepptedShareData);
        user.setAcceptedPrivacyTerm(hasAccepptedThePrivacyTerm);
        user.setOptedByAnonymousUpload(hasChoseAnonymousUpload);

        // insere o usuário no banco
        getDeviceManager().getDataManager().getUserDAO().insert(user);

        // Gera o evento
        this.newInternalEvent(UserManager.EVENT_USER_ADDED, user);

        // retorna o valor
        return true;
    }

    /**
     * Atualiza o usuário. Contudo o usuário deve estar logado.
     *
     * @param user
     * @return
     */
    public boolean updateUser(User user) throws SQLException {
        // checa se usuário está logado
        if (loggedUser == null) {
            return false;
        }
        // checa se ambos são os mesmos e executa a ação
        if (user.getId() == loggedUser.getId()) {
            // Atualiza
            getDeviceManager().getDataManager().getUserDAO().updatePassword(user);
            loggedUser = getDeviceManager().getDataManager().getUserDAO().get(loggedUser.getId());
            // Gera o evento
            this.newInternalEvent(UserManager.EVENT_USER_UPDATED, user);
        }
        return true;
    }

    /**
     * Remove o usuário do sistema. Contdo isso somente será possível se forem
     * passados o login do usuário a senha e se esse usuário não estiver senso
     * monitorado em execução
     *
     * @param userName
     * @param password
     * @return
     */
    public boolean deleteUser(String userName, String password) throws SQLException {
        // verifica se o usuário existe
        User user = getDeviceManager().getDataManager().getUserDAO().get(password, password);
        if (user == null) {
            return false;
        } else {
            // Se o usuário está sendo monitodado ele não pode ser deletado
            if (this.userIsBeingMonitored(user)) {
                return false;
            }
            // Se o usuário existe remove ele 
            getDeviceManager().getDataManager().getUserDAO().deleteAllUserInformation(user);
            // Gera o evento -- evento exclui as outras informações associadas
            this.newInternalEvent(UserManager.EVENT_USER_DELETED, user);
            // Checa se ele está logado e realiza o logout
            if (loggedUser.getId() == user.getId()) {
                return this.logout();
            }
            return true;
        }
    }

    /**
     * O nome do usuário somente é válido se conter 4 caractéres ou mais e se
     * outro usuário não estiver utiliando o mesmo nome
     *
     * @param userName
     * @return true se passar pelas validações
     * @throws java.sql.SQLException
     */
    public boolean checkUsernameValidity(String userName) throws SQLException {
        // verifica se não está vaziu o nome ou se tem menos que 4 caracteres
        if (userName.length() < 4) {
            return false;
        }
        // vetifica se é um nome válido entre os existentes
        for (User user : getDeviceManager().getDataManager().getUserDAO().getUsers()) {
            if (user.getLogin().equals(userName)) {
                return false;
            }
        }
        // retorna veradeiro se passou por todas as validações
        return true;
    }

    /**
     * Um password somente é válido se tiver mais que 4 caracteres
     *
     * @param password
     * @return
     */
    public boolean checkPasswordValidity(String password) {
        // verifica se não está vaziu o nome ou se tem menos que 4 caracteres
        return password.length() >= 4; // retorna veradeiro se passou por todas as validações
    }

    /**
     * Verifica se o usuário passado por parâmetro está sendo monitorado.
     *
     * @param user
     * @return
     */
    private boolean userIsBeingMonitored(User user) throws SQLException {
        // Verifica se este usuário é o que está senso monitorado
        // busca todas as informações sobre o usuário
        User returnedUser = getDeviceManager().getDataManager().getUserDAO().getMonitoredUser();
        // se retornar nulo nenhum usuário está sendo monitorado
        if (returnedUser == null) {
            return false;
        }
        // verifica se o usuário existe
        return returnedUser.getId() == user.getId();
    }

    /**
     * Torna o usuário logado o usuário monitorado pelo sistema, caso ele esteja
     * em login.
     *
     * @return retorna true se o usuário existir e retorna false caso não
     * exista.
     */
    public boolean setMonitoredUser() throws SQLException {
        // busca todas as informações sobre o usuário
        User user = loggedUser;
        // verifica se o usuário existe
        if (user != null) {
            // atualiza esse usuário como sendo monitorado e remove do anterior
            getDeviceManager().getDataManager().getUserDAO().setMonitoredUser(user);
            // gera o evento
            this.newInternalEvent(UserManager.EVENT_USER_CHOSEN_TO_BE_MONITORED, user);
            // retorna o resultado do método
            return true;
        }
        return false;
    }

    /**
     * Torna o usuário que contém o login e senha passados por parâmetro o
     * usuário monitorado pelo sistema, caso ele exista.
     *
     * @param userName
     * @param password
     * @return retorna true se o usuário existir e retorna false caso não
     * exista.
     * @throws java.sql.SQLException
     */
    public boolean setMonitoredUser(String userName, String password) throws SQLException {
        // busca todas as informações sobre o usuário
        User user = getDeviceManager().getDataManager().getUserDAO().get(userName, password);
        // verifica se o usuário existe
        if (user != null) {
            // atualiza esse usuário como sendo monitorado
            getDeviceManager().getDataManager().getUserDAO().setMonitoredUser(user);
            // gera o evento
            this.newInternalEvent(UserManager.EVENT_USER_CHOSEN_TO_BE_MONITORED, user);
            // retorna o resultado do método
            return true;
        } else {
            return false;
        }
    }

    /**
     * Torna o usuário passado por parâmetro o usuário monitorado pelo sistema,
     * caso ele exista.
     *
     * @param user
     * @return retorna true se o usuário existir e retorna false caso não
     * exista.
     * @throws java.sql.SQLException
     */
    public boolean setMonitoredUser(User user) throws SQLException {
        // busca todas as informações sobre o usuário
        User returnedUser = getDeviceManager().getDataManager().getUserDAO().get(user.getId());
        // verifica se o usuário existe
        if (returnedUser != null) {
            // atualiza esse usuário como sendo monitorado
            getDeviceManager().getDataManager().getUserDAO().setMonitoredUser(returnedUser);
            // gera o evento
            this.newInternalEvent(UserManager.EVENT_USER_CHOSEN_TO_BE_MONITORED, returnedUser);
            // retorna o resultado do método
            return true;
        } else {
            return false;
        }
    }

    /**
     * Atualiza a configuração do usuário sobre seu termo de privacidade.
     *
     * @param user
     * @param newValue
     * @return
     * @throws java.sql.SQLException
     */
    public boolean updateUserPrivacyTerm(User user, boolean newValue) throws SQLException {
        user = getDeviceManager().getDataManager().getUserDAO().updatePrivacyConfiguration(User.STATE_PRIVACY_TERM, user, newValue);
        if (user != null) {
            this.newInternalEvent(UserManager.EVENT_USER_CHANGED_PRIVACY_CONFIGURATION, User.STATE_PRIVACY_TERM, newValue);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Atualiza a configuração do usuário relativa a comportilhar dados de
     * sistema.
     *
     * @param user
     * @param newValue
     * @return
     * @throws java.sql.SQLException
     */
    public boolean updateUserDataSharing(User user, boolean newValue) throws SQLException {
        user = getDeviceManager().getDataManager().getUserDAO().updatePrivacyConfiguration(User.STATE_PRIVACY_DATA_SHARING, user, newValue);
        if (user != null) {
            this.newInternalEvent(UserManager.EVENT_USER_CHANGED_PRIVACY_CONFIGURATION, User.STATE_PRIVACY_DATA_SHARING, newValue);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Atualiza a configuração do usuário relativa ao anonimado da informação
     * compartilhada.
     *
     * @param user
     * @param newValue
     * @return
     * @throws java.sql.SQLException
     */
    public boolean updateUserAnonymousUpload(User user, boolean newValue) throws SQLException {
        user = getDeviceManager().getDataManager().getUserDAO().updatePrivacyConfiguration(User.STATE_PRIVACY_ANONYMOUS_UPLOAD, user, newValue);
        if (user != null) {
            this.newInternalEvent(UserManager.EVENT_USER_CHANGED_PRIVACY_CONFIGURATION, User.STATE_PRIVACY_ANONYMOUS_UPLOAD, newValue);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Método utilizado para trocar configurações do sistema. Não implementado
     * ainda pois necessita da parte da representação do conhecimento. Além de
     * definir se será feito de maneira dinâmica ou não. Possivelmente haverá
     * validações em tempo real baseado nas características do conhecimento.
     *
     * @param user
     * @param componentManager
     * @param entityId
     * @param stateId
     * @param newValue
     * @return
     */
    public boolean updateSystemPreference(User user, ComponentManager componentManager, int entityId, int stateId, Object newValue) {
        return this.updateSystemPreference(user, componentManager.getComponentId(), entityId, stateId, newValue);
    }

    /**
     * Método utilizado para trocar configurações do sistema. Não implementado
     * ainda pois necessita da parte da representação do conhecimento. Além de
     * definir se será feito de maneira dinâmica ou não. Possivelmente haverá
     * validações em tempo real baseado nas características do conhecimento.
     *
     * @param user
     * @param componentId
     * @param entityId
     * @param stateId
     * @param newValue
     * @return
     */
    public boolean updateSystemPreference(User user, int componentId, int entityId, int stateId, Object newValue) {
        // verifica o componente
        // busca a ação que pode ser executada para alterar este estado
        ActionModel actionModel = super.getDeviceManager().getDataManager().getUserDAO().getActionStateModel(componentId, entityId, stateId);
        if (actionModel != null) {
            // Parâmetros
            HashMap<String, Object> hashMap = new HashMap();
            for (Parameter p : actionModel.getParameters()) {
                hashMap.put(p.getLabel(), newValue);
            }
            // Cria a ação
            Action action = new Action();
            action.setId(actionModel.getModelId());
            action.setParameters(hashMap);
            action.setName(actionModel.getDescription());
            action.setOrigin(TargetOrigin.APPLICATION_LAYER);
            action.setUser(user);
            action.setTargetEntityId(entityId);
            // A ação ao componente que deve receber a ação
            for (ComponentManager componentManager : super.getDeviceManager().getComponentManagers()) {
                if (componentManager.getComponentId() == componentId) {
                    componentManager.applyAction(action);
                    // Gerar evento da mudança de configuração do sistema
                    this.newInternalEvent(EVENT_USER_CHANGED_SYSTEM_CONFIGURATION, componentId, entityId, stateId, newValue);
                    break;
                }
            }
            return true;
        }
        return false;
    }
    
    public User getMonitoredUser() throws SQLException{
        return this.getDeviceManager().getDataManager().getUserDAO().getMonitoredUser();
    }
    
}
