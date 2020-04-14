package com.lxk.iotcore;

import com.amazonaws.auth.AWSAbstractCognitoDeveloperIdentityProvider;
import com.amazonaws.regions.Regions;

/**
 * @author https://github.com/103style
 * @date 2020/4/9 14:08
 */
public class AuthenticationProvider extends AWSAbstractCognitoDeveloperIdentityProvider {

    public static final String developerProvider = "AuthenticationProvider";
    private String mToken;
    private String mIdentityId;

    public AuthenticationProvider(String identityPoolId, Regions region, String token, String identityId) {
        super(identityId, identityPoolId, region);
        mToken = token;
        mIdentityId = identityId;
    }


    @Override
    public String getProviderName() {
        return developerProvider;
    }

    @Override
    public String getIdentityId() {
        return mIdentityId;
    }

    @Override
    public String refresh() {
        setToken(null);
        update(mIdentityId, mToken);
        return mToken;
    }
}
