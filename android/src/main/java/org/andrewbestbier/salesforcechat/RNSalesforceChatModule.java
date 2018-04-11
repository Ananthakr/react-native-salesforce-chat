
package org.andrewbestbier.salesforcechat;

import java.util.LinkedList;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.salesforce.android.chat.core.ChatConfiguration;
import com.salesforce.android.chat.core.model.AvailabilityState;
import com.salesforce.android.chat.core.model.PreChatField;
import com.salesforce.android.chat.core.model.PreChatEntityField;
import com.salesforce.android.chat.core.model.PreChatEntity;
import com.salesforce.android.chat.ui.ChatUI;
import com.salesforce.android.chat.ui.ChatUIClient;
import com.salesforce.android.chat.ui.ChatUIConfiguration;
import com.salesforce.android.service.common.utilities.control.Async;
import com.salesforce.android.chat.core.AgentAvailabilityClient;
import com.salesforce.android.chat.core.ChatCore;


public class RNSalesforceChatModule extends ReactContextBaseJavaModule {

    private static final String TAG = "RNSalesforceChat";

    private final ReactApplicationContext reactContext;

    public RNSalesforceChatModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return TAG;
    }


    private LinkedList<PreChatField> preChatFields = new LinkedList<>();

    private LinkedList<PreChatEntity> preChatEntities = new LinkedList<>();

    private ChatConfiguration chatConfiguration;


    @ReactMethod
    public void configLaunch(String SUBJECT, String ORIGIN, String CURRENCY_ISO_CODE, String STATUS, String CONTACT_TYPE, String LOCALE_C, String PARTNER_ENGINEER_EMAIL_C, String PARTNER_ENGINEER, String USER_COMPLETE_NAME) {
        
        preChatFields.clear();
        preChatEntities.clear();
        PreChatField subject = new PreChatField.Builder().required(false)
                .build("Subject", "Subject", PreChatField.STRING);

        // Some required fields (Hidden)
        PreChatField origin = new PreChatField.Builder().hidden(true)
                .value(ORIGIN).build("Origin", "Origin", PreChatField.STRING);
        PreChatField currency = new PreChatField.Builder().hidden(true)
                .value(CURRENCY_ISO_CODE).build("CurrencyIsoCode", "CurrencyIsoCode", PreChatField.STRING);
        PreChatField status = new PreChatField.Builder().hidden(true)
                .value(STATUS).build("Status", "Status", PreChatField.STRING); //Hardcoded
        PreChatField contactType = new PreChatField.Builder().hidden(true)
                .value(CONTACT_TYPE).build("ContactType__c", "ContactType__c", PreChatField.STRING); //Hardcoded
        PreChatField locale = new PreChatField.Builder().hidden(true)
                .value(LOCALE_C).build("Locale__c", "Locale__c", PreChatField.STRING);
        PreChatField engineerEmail = new PreChatField.Builder().hidden(true)
                .value(PARTNER_ENGINEER_EMAIL_C).build("PartnerEngineerEmail__c", "PartnerEngineerEmail__c", PreChatField.EMAIL);
        PreChatField suppliedName = new PreChatField.Builder().hidden(true)
                .value(USER_COMPLETE_NAME).build("SuppliedName", "SuppliedName", PreChatField.STRING);
        //An unique identification of an engineer

        // Some optional fields (Hidden)
        PreChatField engineerRef = new PreChatField.Builder().hidden(true)
                .value(PARTNER_ENGINEER).build("PartnerEngineerRef__c", "PartnerEngineerRef__c", PreChatField.STRING);
        //An unique identification of an engineer

        // Add the fields to the list
        preChatFields.add(subject);
        preChatFields.add(origin);
        preChatFields.add(currency);
        preChatFields.add(status);
        preChatFields.add(contactType);
        preChatFields.add(locale);
        preChatFields.add(engineerEmail);
        preChatFields.add(suppliedName);
        preChatFields.add(engineerRef);


        // Create an entity field builder for Case fields
        PreChatEntityField.Builder caseEntityBuilder = new PreChatEntityField.Builder()
                .doCreate(true);

        // Create the case entity
        PreChatEntity caseEntity = new PreChatEntity.Builder()
                .showOnCreate(true)
                .saveToTranscript("Case")
                .addPreChatEntityField(caseEntityBuilder.build("Subject", "Subject"))
                .addPreChatEntityField(caseEntityBuilder.build("Origin", "Origin"))
                .addPreChatEntityField(caseEntityBuilder.build("CurrencyIsoCode", "CurrencyIsoCode"))
                .addPreChatEntityField(caseEntityBuilder.build("Status", "Status"))
                .addPreChatEntityField(caseEntityBuilder.build("ContactType__c", "ContactType__c"))
                .addPreChatEntityField(caseEntityBuilder.build("Locale__c", "Locale__c"))
                .addPreChatEntityField(caseEntityBuilder.build("PartnerEngineerEmail__c", "PartnerEngineerEmail__c"))
                .addPreChatEntityField(caseEntityBuilder.build("SuppliedName", "SuppliedName"))
                .build("Case");
        // Add the entities to the list
        preChatEntities.add(caseEntity);
    }

    @ReactMethod
    public void configChat(String ORG_ID, String DEPLOYMENT_ID, String BUTTON_ID, String LIVE_AGENT_POD) {
        chatConfiguration = new ChatConfiguration.Builder(ORG_ID, BUTTON_ID, DEPLOYMENT_ID, LIVE_AGENT_POD)
                .preChatFields(preChatFields)
                .preChatEntities(preChatEntities)
                .build();
    }


    @ReactMethod
    public void launch(final Callback successCallback) {

        // Create an agent availability client
        AgentAvailabilityClient client = ChatCore.configureAgentAvailability(chatConfiguration);

        client.check().onResult(new Async.ResultHandler<AvailabilityState>() {
            @Override
            public void handleResult(Async<?> async, @NonNull AvailabilityState state) {

                switch (state.getStatus()) {
                    case AgentsAvailable: {
                        startChat();
                        break;
                    }
                    case NoAgentsAvailable: {
                        successCallback.invoke();
                        break;
                    }
                    case Unknown: {
                        break;
                    }
                }
                ;
            }
        });
    };


    private void startChat() {
        ChatUI.configure(ChatUIConfiguration.create(chatConfiguration))
                .createClient(reactContext)
                .onResult(new Async.ResultHandler<ChatUIClient>() {

                        @Override public void handleResult (Async<?> operation,
                                                        @NonNull ChatUIClient chatUIClient) {
                        chatUIClient.startChatSession((FragmentActivity) getCurrentActivity());
                        }
                });
    };

}
