package com.solace.spring.boot.autoconfigure;

import com.solace.services.loader.model.SolaceServiceCredentials;
import com.solace.services.loader.model.SolaceServiceCredentialsImpl;
import com.solacesystems.jcsmp.JCSMPChannelProperties;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.SpringJCSMPFactory;
import com.solacesystems.jcsmp.SpringJCSMPFactoryCloudFactory;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

abstract class SolaceJavaAutoConfigurationBase<T extends SolaceServiceCredentials>
        implements SpringJCSMPFactoryCloudFactory<T> {

    private SolaceJavaProperties properties;

    SolaceJavaAutoConfigurationBase(SolaceJavaProperties properties) {
        this.properties = properties;
    }

    abstract T findFirstSolaceServiceCredentialsImpl();
    abstract List<T> getSolaceServiceCredentialsImpl();

    @Bean
    @Override
    public T findFirstSolaceServiceCredentials() {
        return findFirstSolaceServiceCredentialsImpl();
    }

    @Bean
    @Override
    public List<T> getSolaceServiceCredentials() {
        return getSolaceServiceCredentialsImpl();
    }

    @Bean
    @Override
    public SpringJCSMPFactory getSpringJCSMPFactory() {
        return getSpringJCSMPFactory(findFirstSolaceServiceCredentialsImpl());
    }

    @Override
    public SpringJCSMPFactory getSpringJCSMPFactory(String id) {
        return getSpringJCSMPFactory(findSolaceServiceCredentialsById(id));
    }

    @Override
    public SpringJCSMPFactory getSpringJCSMPFactory(SolaceServiceCredentials solaceServiceCredentials) {
        return new SpringJCSMPFactory(getJCSMPProperties(solaceServiceCredentials));
    }

    @Bean
    @Override
    public JCSMPProperties getJCSMPProperties() {
        return getJCSMPProperties(findFirstSolaceServiceCredentialsImpl());
    }

    @Override
    public JCSMPProperties getJCSMPProperties(String id) {
        return getJCSMPProperties(findSolaceServiceCredentialsById(id));
    }

    @Override
    public JCSMPProperties getJCSMPProperties(SolaceServiceCredentials solaceServiceCredentials) {
        Properties p = new Properties();
        Set<Map.Entry<String,String>> set = properties.getApiProperties().entrySet();
        for (Map.Entry<String,String> entry : set) {
            p.put("jcsmp." + entry.getKey(), entry.getValue());
        }

        JCSMPProperties jcsmpProps = createFromApiProperties(p);
        SolaceServiceCredentials creds = solaceServiceCredentials != null ?
                solaceServiceCredentials : new SolaceServiceCredentialsImpl();

        jcsmpProps.setProperty(JCSMPProperties.HOST, creds.getSmfHost() != null ?
                creds.getSmfHost() : properties.getHost());

        jcsmpProps.setProperty(JCSMPProperties.VPN_NAME, creds.getMsgVpnName() != null ?
                creds.getMsgVpnName() : properties.getMsgVpn());

        jcsmpProps.setProperty(JCSMPProperties.USERNAME, creds.getClientUsername() != null ?
                creds.getClientUsername() : properties.getClientUsername());

        jcsmpProps.setProperty(JCSMPProperties.PASSWORD, creds.getClientPassword() != null ?
                creds.getClientPassword() : properties.getClientPassword());

        if ((properties.getClientName() != null) && (!properties.getClientName().isEmpty())) {
            jcsmpProps.setProperty(JCSMPProperties.CLIENT_NAME, properties.getClientName());
        }

        // Channel Properties
        JCSMPChannelProperties cp = (JCSMPChannelProperties) jcsmpProps
                .getProperty(JCSMPProperties.CLIENT_CHANNEL_PROPERTIES);
        cp.setConnectRetries(properties.getConnectRetries());
        cp.setReconnectRetries(properties.getReconnectRetries());
        cp.setConnectRetriesPerHost(properties.getConnectRetriesPerHost());
        cp.setReconnectRetryWaitInMillis(properties.getReconnectRetryWaitInMillis());
        return jcsmpProps;
    }

    private JCSMPProperties createFromApiProperties(Properties apiProps) {
        return apiProps != null ? JCSMPProperties.fromProperties(apiProps) : new JCSMPProperties();
    }

    private T findSolaceServiceCredentialsById(String id) {
        for (T credentials : getSolaceServiceCredentialsImpl())
            if (credentials.getId().equals(id)) return credentials;
        return null;
    }

    void setProperties(SolaceJavaProperties properties) {
        this.properties = properties;
    }
}
