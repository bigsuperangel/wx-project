package com.service.api.impl;

import com.cache.EhCacheManager;
import com.cache.EhKeys;
import com.data.AccessTokenMapper;
import com.data.AccountMapper;
import com.domain.wx.AccessToken;
import com.domain.wx.Account;
import com.models.wx.token.TokenResp;
import com.service.api.AccessTokenService;
import com.utils.AcceptTypeEnum;
import com.utils.HttpUtils;
import com.utils.JsonUtils;
import com.service.wxutil.WxUrlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Admin on 2016/2/21.
 */
@Service("accessTokenService")
public class AccessTokenServiceImpl implements AccessTokenService {

    @Resource
    AccountMapper accountMapper;
    @Resource
    AccessTokenMapper accessTokenMapper;
    @Autowired
    EhCacheManager ehCacheManager;

    /**
     * @param accountid
     * @return
     * @throws IOException
     */
    public TokenResp getAccessToken(int accountid) throws IOException {
        TokenResp tokenResp;
        String key = String.format(EhKeys.accessTokenKey, accountid);
        AccessToken token = ehCacheManager.get(key, AccessToken.class);
        if (token != null && token.getExpiredtime().after(new Date())) {
            tokenResp = new TokenResp();
            tokenResp.setAccess_token(token.getToken());
            return tokenResp;
        }
        token = accessTokenMapper.getAccessTokenByAccount(accountid);
        if (token != null && token.getExpiredtime().after(new Date())) {
            tokenResp = new TokenResp();
            tokenResp.setAccess_token(token.getToken());
            Long seconds = token.getExpiredtime().getTime() - new Date().getTime();
            ehCacheManager.put(key, token, (int) (seconds/1000));
            return tokenResp;
        }
        Account account = accountMapper.getAccountById(accountid);
        String url = WxUrlUtils.getInstance().getAccesstoken();
        if (!StringUtils.isEmpty(url)) {
            url = String.format(url, account.getAppid(), account.getSecret());
            String json = HttpUtils.doGet(url, AcceptTypeEnum.json);
            tokenResp = JsonUtils.Deserialize(json, TokenResp.class);
            if (StringUtils.isEmpty(tokenResp.getErrcode())) {
                token = getAccessTokenByDto(tokenResp, accountid);
                ehCacheManager.put(key, token, tokenResp.getExpires_in());
                accessTokenMapper.addAccessToken(token);
                return tokenResp;
            }
        }
        return new TokenResp();
    }

    /**
     * @param dto
     * @return
     */
    private AccessToken getAccessTokenByDto(TokenResp dto, int accountId) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, dto.getExpires_in());
        AccessToken token = new AccessToken(accountId, dto.getAccess_token(),
                calendar.getTime());
        return token;
    }
}
